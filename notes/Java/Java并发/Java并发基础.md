# Java并发基础

## 并发与并行

多任务操作系统的核心原理：时间片轮转与异步IO

并发Concurrent：指多个事件在同一时间间隔内发生

并行 Parallel：指多个时间字同一个时刻发生

![并发与并行](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/%E5%B9%B6%E5%8F%91%E4%B8%8E%E5%B9%B6%E8%A1%8C.png)

并发与并行使用得当，都可以提升程序的性能

- 并发的场景更多与IO相关：
  - 如果所有任务都是纯计算的任务，这里并发有上下文切换成本，并发不太能提高性能
  - GUI的例子，单独的UI线程使得用户响应得到保证
  - 同时多个IO的场景，如多线程网页抓取，并发提升总吞吐量
- 并行则更多是利用多CPU或GPU的性能

## 同步与异步

讨论同步和异步，必然涉及到多个部件的协作

同步Sync：所谓同步，就是调用一个功能，在没有得到结果之前，该调用就不继续执行后续操作。**根据这个定义，Java中的所有方法都是同步调用，因为必须要等到结果后才会继续执行。**

异步 Async： **异步与同步相对，当一个异步过程调用发出后，调用者在没有得到结果之前，就可以继续执行后续操作，当这个调用完成后，一般通过状态、通知和回调来通知调用者。对于异步调用，调用的返回并不受调用者控制。**

![sync](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/%E5%90%8C%E6%AD%A5%E8%B0%83%E7%94%A8.png)

![异步调用](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/%E5%BC%82%E6%AD%A5%E8%B0%83%E7%94%A8.png)

## 进程与线程

一个程序可以有多个进程，一个进程可以有多个线程。

进程是程序执行中的一个实例，线程是进程在执行中的独立单元。

> In computing, a **process** is the [instance](https://en.wikipedia.org/wiki/Instance_(computer_science)) of a [computer program](https://en.wikipedia.org/wiki/Computer_program) that is being executed by one or many threads. It contains the program code and its activity. Depending on the [operating system](https://en.wikipedia.org/wiki/Operating_system) (OS), a process may be made up of multiple [threads of execution](https://en.wikipedia.org/wiki/Thread_(computing)) that execute instructions [concurrently](https://en.wikipedia.org/wiki/Concurrency_(computer_science)).[[1\]](https://en.wikipedia.org/wiki/Process_(computing)#cite_note-OSC_Chap4-1)[[2\]](https://en.wikipedia.org/wiki/Process_(computing)#cite_note-Vah96-2)

> In [computer science](https://en.wikipedia.org/wiki/Computer_science), a **thread** of execution is the smallest sequence of programmed instructions that can be managed independently by a [scheduler](https://en.wikipedia.org/wiki/Scheduling_(computing)), which is typically a part of the [operating system](https://en.wikipedia.org/wiki/Operating_system).[[1\]](https://en.wikipedia.org/wiki/Thread_(computing)#cite_note-1) The implementation of threads and [processes](https://en.wikipedia.org/wiki/Process_(computing)) differs between operating systems, but in most cases a thread is a component of a process. [Multiple threads](https://en.wikipedia.org/wiki/Thread_(computing)#Multithreading) can exist within one process, executing [concurrently](https://en.wikipedia.org/wiki/Concurrent_computation) and sharing resources such as [memory](https://en.wikipedia.org/wiki/Shared_memory_(interprocess_communication)), while different processes do not share these resources. In particular, the threads of a process share its executable code and the values of its [dynamically allocated](https://en.wikipedia.org/wiki/Memory_management#HEAP) variables and non-[thread-local](https://en.wikipedia.org/wiki/Thread-local_storage) [global variables](https://en.wikipedia.org/wiki/Global_variable) at any given time.



## Reference

1. [Process](https://en.wikipedia.org/wiki/Process_(computing)#External_links)
2. [Thread](https://en.wikipedia.org/wiki/Thread_(computing))