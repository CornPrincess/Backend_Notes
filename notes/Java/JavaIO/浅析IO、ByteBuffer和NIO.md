# 浅析 IO、ByteBuffer 和 NIO

## 基础知识

![io](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/io.png)

![io](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/io2.png)

首先我们要具备上图的基础知识

- 使用原始的 IO 时，如果不使用缓存，那么每一次write都会调用一次系统调用，性能不好
- write 时，如果没有调用 flush ，此时数据会存放在内核的缓存中
- 把数据放在内存中，可以减少IO操作，提高性能
- 内核缓存合适会将数据写入磁盘：
  - 缓存满
  - 调用 flush（fsync系统调用）

传统的IO，先从用户态拷贝到内核，在从内核拷贝到磁盘，如下图

![io](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/io3.png)

但是使用了 FileChannel之后，可以通过 channel.map 在 JVM 进程中的堆外空间开辟一块区域byteBuffer ，可以直接将 byteBuffer 中的数据拷贝到磁盘 如下图

![io4](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/io4.png)

当然也会存在数据在堆内的情况 ，这种情况我们需要先将其转移到堆外

![io](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/io5.png)

[浅析Linux中的零拷贝技术](https://www.jianshu.com/p/fad3339e3448)

我们来看下 kafka 中使用到的 零拷贝技术

![kafka](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/kafka.png)



## NIO

首先看IO全局图

![nio](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/nio.png)bio nio aio 主要是对于网络io而言 

![nio](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/nio2.png)

在传统的bio需要使用多个线程，如果只有一个线程，会阻塞住，这其实也是多线程的一个很重要的应用点

bio多线程增加内存负担，而且线程切换也是成本。nio只用一个线程，因为在同一时间cpu只能处理一件事情，此时就去轮训哪个io是处于ready状态，ready就去执行。如果有1000个socket链接，但是只有10个是有用的，此时轮训就会做很多无用功。

selector模型，selector是系统调用，nio不用轮训，可以将轮询的功能交给selector来做，selector在内核中运行，内核可以理解为一个软件。selector不好的是每次要传输 大量的描述符 fd



**内核空间和用户空间是隔离的，好处是可以保护内核，但是坏处是数据必须从内核 到用户空间 之间相互拷贝。**



nio 有3种buffer

- heap
- 堆外
- mmap（堆外，但是内核能访问，不需要触发拷贝）
  - 这种需要调用 channel.map
  - **socket channel 不行，所以当网卡接收到发过来的数据时一定会有一次直接DMA到内核到用户态的拷贝？**
  - data file 类型的 channel 是可以的

![epoll](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/epoll.png)

epoll 的流程：

- create
- ctl 有一个客户端进行连接，就会掉一次ctl，就会写入到红黑树
- wait 只需要掉一次，从链表中取出准备好的
- read，**read这个操作一定会有从内核态到用户态到拷贝，这无法避免**，但是若想将数据存到 kafka中，如需从内核态拷贝到用户态，只需要 mmap就行

kafka为什么快：

- 用磁盘但是存数据快，使用 mmap，且是对磁盘顺序对追增，不是随机的拷贝，接受数据时用了内存映射空间
- **内核里有一个方法叫做 sendfile(infd, outfd)，直接通过输入fd和输出fd通过mmap将文件发到目标socket，如果没有sendfile，需要 先 read 读到内核 空间，再拷贝到用户空间，然后用户空间拷到内核空间，再 write  ，需要两次系统调用。**即给别人提供数据的时候用了零拷贝，这个过程没有内核态到用户态到拷贝

redis 使用了 nio 的epoll 模型

虽然 tcp 可以保持长链接，但是一般需要使用心跳保活



socket 是内核级的，



用户程序 调用 read 系统调用之后有可能被阻塞！！（这里就是io准备阶段？）

## tips

yum install nc

nc -l localhost 8888 服务端

nc localhost 81 客户端

ps -fe | grep nc

Netstat -natp

cd /proc/{pid}/fd -- 程序都有三个流：输出 输入 错误



yum install tcpdump 抓包工具

tcpdump -nn -i eth0 port 80

