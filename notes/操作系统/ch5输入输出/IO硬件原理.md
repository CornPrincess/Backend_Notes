# I/O硬件原理

除了提供抽象（进程，地址空间，文件）以外，操作系统还要控制计算机的所有I/O设备，操作系统必须向设备发送命令，捕捉中断，并处理设备的各种错误。它还应该在设备和系统的其他部分之间提供简单且易于使用的接口。如果有可能，这个接口对于所有设备都应该是相同的，这就是所谓的设备无关性（device independent）

## I/O设备 I/O Devices

I/O设备可以大致分为两类：**块设备（block device）**和**字符设备（character device）。**块设备把信息存储在固定大小的块中，每个块都有自己的地址。通常块的大小在512字节到65536字节之间。所以传输以一个或多个完整（连续）的块为单位。**块设备的基本特征是每个块都能独立于其他块而读写。硬盘，CD-OM和USB都是最常见的块设备。**

另一类I/O设备是字符设备，**字符设备以字符为单位发送或接收一个字符流（character stream），而不用考虑任何块结构。字符设备是不可寻址的（not addressable）并且没有任何寻道操作（seek operation）。**打印机、网络接口、鼠标以及大多数和磁盘不同的设备都可以看做是字符设备。

这种分类方法并不完美，有些设备没有包括进去，如时钟（clocks）既不是块可寻址，也不产生和接收字符流，它所做的工作就是按照预先规定好的时间间隔产生中断。内存映射的显示器也不适用于次模型。**但是，块设备和字符设备的模型具有足够的一般性（general），可以用作处理IO设备的某些操作系统软件具有设备无关性的基础。**

## 设备控制器 Device Controllers

I/O设备一般由机械部件（mechanical component）和电子部件（electronic compon）两部分组成。通常可以将这两部分分开处理，以提供更加模块化和通用化的设计，电子部件称作**设备控制器（device controller）**或**适配器（adapter）**。在个人计算机上它经常以主板上的芯片的形式出现，或者以插入（PCI）扩展槽中的印刷电路板的形式出现。机械部件则是设备本身。

控制器卡上通常有一个**连接器（connector）**，通过设备本身的电缆可以插入到这个连接器中。控制器和设备之间的接口通常是一个很低层次的接口。**如实际从磁盘出来的是一个串行比特流（a serial bit stream），它以一个前导符（preamble）开始，接着是一个扇区中的4096位，最后是一个检验和（checksum）,也称为错误校正码（Error Correct Code， ECC）。前导符是在格式化时写上去的，它包括柱面数（cylinder number）、扇区号（sector number）和扇区大小（sector size）以及一些同步信息。**

**控制器的任务就是把串行的位流转化为字节块，并进行必要的错误矫正工作。**字节块通常首先咋控制器的内部的一个缓冲区中按位进行组装，然后校验通过后把它复制到内存中。

## 内存映射I/O Memory-Mapped I/O

每个控制器有几个寄存器（registers）用来和CPU通信，通过写入这些寄存器，操作系统可以命令设备发送数据、接收数据、开启和关闭，或者执行某些操作。通过读取这些寄存器，操作系统可以了解设备的状态，收费准备好接收一个新的命令等。

除了这些寄存器，许多设备还有一个操作系统可以读写的**数据缓冲区（data buffer）。**例如，在屏幕上显示像素的常规方法是使用一个视屏RAM，这一RAM基本上只是一个数据数据缓冲区，可供程序或操作系统写入数据。

于是，问题就出现了，**CPU如何与设备的控制寄存器和数据缓冲区进行通信？**

存在两个方法，**第一个方法，每个控制寄存器被分配一个I/O端口（I/O port）号，这是一个8-16位的整数，所有I/O端口形成I/O端口空间（I/O port space），并且收到保护使得普通的用户程序不能对其进行访问（只有操作系统可以访问）**

在这种方案中，内存地址空间和I/O地址空间是不同的，如图a，有以下指令

> IN R0, 4
>
> MOV R0, 4

第一条指令是读取I/O端口4的内容并将其存入R0，而后者则读取内存字4的内容将其存入R0。因此，这个例子中的4引用的是不同且不相关的地址空间。

多数早期的计算机是使用的这种方式。

![memory mapped IO](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/memoryMappedIO.png)

**第二个方法是PDP-11引入的，它将所有控制器寄存器映射到内存空间中，如图b。每个控制寄存器被分配唯一的一个内存地址，并且不会有内存被分配这一地址。这样的系统称为内存映射I/O（memory-mapped I/O）。通常分配给控制器的地址位于地址空间的顶端。**

**如图c所示的为混合方案，这一方案具有内存映射I/O的数据缓冲区，而控制寄存器则具有单独的I/O端口。**

这些方案是怎么工作的？**在各种情形中，当CPU想要读入一个字的时候，不论是从内存中读入还是从I/O端口中读入，它都要将需要的地址(address it needs)放到总线的地址线（bus' address lines）上，然后总线的一条控制线上置起READ信号。还要用到第二条信号线（signal line）来表明需要的是I/O空间还是内存空间，如果是IO空间，那么IO设备响应，如果是内存空间，内存空间响应。因为不会有地址既分给内存又分给IO设备。所以不会存在歧义和冲突。**

内存映射IO的优点：

- 对于内存映射I/O，I/O驱动设备程序完全可以用C语言写，否则要用到汇编代码
- 不需要特殊的保护机制来阻止用户进程执行I/O操作，可以减小内核大小并防止驱动程序之间的干扰
- 可以引用内存的每一条指令也可以引用控制寄存器

内存映射I/O的缺点：

- 对一个设备控制寄存器采用高速缓存会引起问题，硬件必须针对每个页面具备选择性禁用高速缓存的能力，操作系统必须管理选择性高速缓存， 这为硬件和操作系统添加了额外的复杂性
- 若计算机采用单独的内存总线，则I/O设备没法查看内存地址，因为内存地址旁路到内存总线上，必须采用其他的方法是内存映射IO可用。

  ![bus architecture](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/busArchitecture.png)


## 直接存储器存取 Direct Memory Access

无论一个CPU是否具有内存映射I/O，他都需要寻址（address）设备寄存器以便与他们交换数据。CPU可以从IO控制器每次请求一个字节数据，但是这样做浪费CPU的时间，所有经常用到一种称为直接存储器存取（Direct Memory Access， DMA）。只有硬件具有DMA控制器时操作系统才能使用DMA，而多数系统都有DMA控制器，且一般只有一个DMA控制器可用（如在主板上），由它调控到多个设备的数据传送，而这些数据传送经常是同时发生的。

无论DMA控制器在物理上处于什么地方，它都能够独立于CPU而访问系统总线。

>  It contains several registers that can be written and read by the CPU. These include a memory address register, a byte count register, and one or more control registers. The control registers specify the I/O port to use, the direction of the transfer (reading from the I/O device or writing to the I/O device), the transfer unit (byte at a time or word at a time), and the number of bytes to transfer in one burst

![DMA](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/DMA.png)

我们首先看一下没有DMA时，磁盘如何读：

- 控制器从磁盘驱动器串行地（serially）、一位一位（bit by bit）地读一个块（block）（一个或多个扇区 sectors），直到将整块信息放入控制器的内部缓冲区（internal buffer）中
- 它计算checksum保证没有读错误发生，然后控制器产生一个中断（interrupt）。
- 当操作系统开始运行时，它重复地从控制器的缓冲区中一次一个字节或者一个字地取出该块的信息，并将其放入内存中。

当使用DMA时，具体操作结合图，简述如下：

- CPU通过设置DMA控制器的寄存器对它进行编程，所以DMA控制器知道将什么数据传送到什么地方
- DMA控制器通过在总线上发出一个读请求到磁盘控制器而发起DMA传送，**磁盘控制器并不知道或者并不关心读请求是来自CPU还是来自DMA控制器。**
- 磁盘将数据写进内存中
- 写操作完成后磁盘控制器在总线上发送应答信号（acknowledgement signal）到DMA控制器上
- DMA控制器步增（increment）内存地址并且步减（decremt）字节计数，如果字节数大于0，那么重复以上四步，知道字节计数达到0.

**字节计数为0时，DMA控制器将中断CPU以便让CPU知道传送现在已经完成了。当操作系统开始工作时，不用将磁盘块复制到内存中，因为它已经在内存中了。**

许多总线有两种模式操作：每次一字模式（word-at-a-time mode）和块模式（block mode），某些DMA控制器也能以这两种模式操作。**在前一种模式中，DMA控制器请求传送一个字并且得到这个字。如果CPU也想使用总线，它必须等待。这以机制称为周期窃取（cycle stealing），这会轻微延迟CPU**。**在块模式中，DMA控制器通知设备获得总线，发起一连串的传送，然后释放总线，这称为突发模式（brust mode），其缺点是，如果正在进行的是长时间的突发传送，有可能将CPU和其他设备阻塞相当长的周期。**

**在我们上述讨论的模型（有时称为飞越模型fly-by mode）中，DMA控制器通知设备控制器直接将数据传送到内存。有些DMA会让设备控制器将字节发送给DMA，DMA然后发起第二个总线请求，将该字写到它应该去的任何地方，虽然多了一个总线周期，但是这种模式更灵活，它也执行设备到设备的复制甚至是内存到内存的复制。**

为什么控制器从磁盘中读出数据后立即将其存入内存，而是放在缓冲区中：

- 可以在传送开始之前进行校验
- 如果其他设备占用总线（突发模式），控制器只能等待，但磁盘读出数据是以固定速率到达的，此时就需要缓冲区暂时存放这些数据。

## 重温中断 Interrupt Revisited

在一台典型的个人计算机系统中，中断结构如果所示。

![interrupt](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/interrupt.png)

>  When an I/O device has finished the work given to it, it causes an interrupt (assuming that interrupts have been enabled by the operating system). It does this by asserting a signal on a bus line that it has been assigned. This signal is detected by the interrupt controller chip on the parentboard, which then decides what to do.

在硬件层面，中断的工作如下所述。当一个I/O设备完成交给他的工作时，它就产生一个中断（假设操作系统已经放开中断），**它是通过在分配给他的一条总线信号线上置起信号而产生中断的。该信号被主板上的中断控制芯片（interrupt controller chip）检测到，由中断控制器芯片决定做什么。**

 设备与中断控制器之间的链接实际上是使用的总线上的**中断线（interrupt lines）而不是专用连线（dedicated wires）。**

如果没有其他中断悬而未决，中断控制器会立即对中断进行处理，如果有另一种中断正在处理或者同时有一个优先级更高的中断，该设备的中断暂时不被处理。在这种情况下，该设备将继续在总线上置起中断信号，知道得到CPU服务。

中断处理过程：

- 为了处理中断，**中断控制器在地址线（address lines）上放置一个数字表明哪个设备需要关注，并且置起一个中断CPU的信号。**
- 中断信号导致CPU停止当前正在做的事情并且开始做其他事情，地址线上的数字用来作为一个指向**中断向量（interrupt vector）**的表格的索引，**以便读取一个新的程序计数器。这以程序计数器指向相应的中断服务过程的开始。一般情况下，陷阱和中断从这一点上看使用相同的机制，并且常常指向相同的中断向量。**
- **中断服务过程开始运行后，它立刻通过将一个确定的值写到中断控制器的某个IO端口来对中断做出应答，这以应答告诉中断控制器可以自由地发出另一个中断。**
- 在开始服务前，硬件作为最低限度，必须保存程序计数器，这样被中断的进程才能够重新开始。

### 精确中断和不精确中断

现代CPU都普遍使用了流水线（pipelined）和超标量（superscalar internally parallel），这导致的结果为：当在流水线满的时候（通常的情形），如果出现一个中断，那么会发生什么情况？许多指令正处于不同的执行阶段。

![precise interrupt](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/preciseInterrupt.png)
将机器留在一个明确状态的中断称为**精确中断（precise interrupt）**，精确中断与具有以下四个特性：

- PC（程序计数器）保存在一个已知的地方
- PC所指向的指令之前的所有指令已经完全执行
- PC所指向的指令之后的所有指令都没有执行
- PC所指向的指令的执行状态是已知的

不满足上述要求的中断为**不精确中断（imprecise interrupt）**，一般IO中断为精确中断。

