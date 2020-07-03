# 线程

> In traditional operating systems, each process has an address space and a single thread of control. In fact, that is almost the definition of a process. Nevertheless, in many situations, it is desirable to have multiple threads of control in the same address space running in quasi-parallel, as though they were (almost) separate processes (except for the shared address space). In the following sections we will
> discuss these situations and their implications.

在传统操作系统中，每一个进程有一个地址空间和一个控制线程（control thread），事实上，这几乎就是进程的定义，不过，经常存在在同一个地址空间中准并行运行多个控制线程的情形，这些线程就像（差不多）分离的进程（共享地址空间除外）。

## 线程的使用

首先说明下需要线程的原因：

- 人们需要多线程的主要原因是，在许多应用中同时发生着多种活动。其中某些活动会随着时间的推移而被阻塞。通过将这些应用分解为可以准并行运行的多个顺序线程，程序设计模型会变得更简单。

  只有在有了多线程概念之后，我们才加入一个新的元素：并行实体共享同一个地址空间和所有可用数据的能力（the ability for the parallel entities to share an address space and all of its data among themselves. ），这正是多进程模型无法提供的（进程有不同的地址空间）。

- > A second argument for having threads is that since they are lighter weight than processes, they are easier (i.e., faster) to create and destroy than processes. In many systems, creating a thread goes 10–100 times faster than creating a process. When the number of threads needed changes dynamically and rapidly, this property is useful to have

## 在用户空间中实现线程

这种方法实现线程包时，把整个线程包都放在用户空间中，内核对线程包一无所知。从内核的角度考虑，就是按照正常的方式管理，即单线程进程。**这种方法第一个，也是最明显的优点是，用户级线程包可以在不支持线程的操作系统上实现。通过这一方法，可以用库函数实现线程。**

**线程在一个运行时系统（Run-time system）的顶部运行，这个运行时系统是一个管理线程的过程的集合。在用户空间管理线程时，每个进程需要有其专用的线程表（thread table），用来跟踪该进行中的线程。这些表和内核中的进程表类似，不过它仅仅记录各个线程的属性，如每个线程的程序计数器（program counter）、堆栈指针（stack pointer）、寄存器（register）和状态（state）**

- [ ] 补图

**线程的切换过程：如果一个线程必须进入阻塞过程，那么在线程表中保存该线程的寄存器，查看表中可运行的就绪线程，并把新线程的保存值重新装入机器的寄存器中，只要堆栈指针和程序计数器一切换，新的线程就自动投入运行。进行类似的线程切换至少要比陷入内核要快一个数量级，这是使用用户级线程包的极大的优点。**



用户级线程还有一个优点，它允许每个进程有自己定制的调度算法。

总结一下

**优点：**

- 用户级线程包可以在不支持线程的操作系统上实现
- 线程调度非常快捷，不需要陷阱，不需要上下文切换，也不需要对内存高速缓存进行刷新
- 允许每个进程有自己定制的调度算法

**缺点：**

- 如何实现阻塞系统调用（blocking system call）
- 如果一个线程开始运行，那么在该进程中的其他线程就不能运行，除非第一个线程自动放弃CPU。在一个单独的进程内部，没有时钟中断（clock interrupt），所以不可能用轮转调度（round-robin）的方式调度线程。

## 在内核中实现线程

此时不在需要运行时系统，每个进程中也没有线程表。相反，在内核中与用来记录系统中所有线程的线程表。当某个线程希望创建或销毁一个已有线程时，它进行一个系统调用，这个系统调用通过对线程表的更新完成线程创建或撤销工作。

所有能够阻塞线程的调用都以系统调用的形式实现。当一个线程阻塞时，内核根据其选择，可以运行同一个进程中的另一个线程，或者运行另一个进程中的线程。

缺点：

- 系统调用的代价比较大，如果线程的操作（创建，终止等），就会带来很大的开销。

信号（signal）是发给进程而不是线程的，当一个信号到达时，应该由哪一个线程处理它。

## 混合实现

将用户级线程与某些或全部内核线程多路复用起来

## 使单线程代码多线程化

- 由于多线程环境中存在对全局变量共同读写的情况，这样会引发一些错误，提出了一些解决方案。
  - 全面禁止全局变量：这个想法不一定合适，因为同许多已有的软件冲突。
  - 为每个线程赋予其私有的全局变量

- 另一个问题是许多库过程并不是可重入的（reentrant），也就是说，他们不是被设计成下列工作方式的：对于任何给定的过程，当前面的调用尚，可以进行第二次调用，解决方法：
  - 重写整个库
  - 为每个过程提供一个包装器（jacket），这个包装器设置一个二进制位从而标志某个库处于使用中。在先去的调用还没完成之前，任何试图使用该库的其他线程都会被阻塞，尽管这可以工作，但是它会极大地降低系统前置的并行性。

- 有些信号（signals）逻辑上线程专用的，但另外一些却不是
- 堆栈的管理



## Reference

## TODO

- 可重入和不可重入
