# IO软件层次

IO软件通常组织成四个层次，每一层具有一个要执行的定义明确的功能和一个定义明确的与邻近层次的接口。功能和接口随系统的不同而不同。

![layer of I/O software system](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/layerofIOSoftWare.png)

## 中断处理程序 Interrupt Handlers

对于多数IO而言，中断是令人不愉快的事情，应当将其隐藏在操作系统内部，以便系统的其他部分尽量不与它发送联系。**隐藏 的办法为将启动一个I/O操作的驱动程序阻塞起来，直到IO操作完成且产生一个中断，驱动程序阻塞自己的手段有：在一个我信号量（ semaphore,）上执行down操作，在一个条件变量（condition variable）上执行wait操作，在一个消息（message）上执行receive操作。**

当中断发生时，中断处理程序会做它必须要做的全部工作以便对中断进行处理。然后，它可以将启动中断的驱动程序解除阻塞。在一些情形中，它制式在一个信号量上执行up操作，或对管程中的条件变量执行signal操作，或向被阻塞的驱动程序发一个消息。在所有这些情形中，中断最终的结果是使先前被阻塞的驱动程序现在能够继续运行。

硬件中断完成之后必须在软件中执行的部分步骤，不同的系统会与差异：

> 1. Save any registers (including the PSW) that have not already been saved by the interrupt hardware.
> 2. Set up a context for the interrupt-service procedure. Doing this may involve setting up the TLB, MMU and a page table.
> 3. Set up a stack for the interrupt service-procedure.
> 4. Acknowledge the interrupt controller. If there is no centralized interrupt controller, reenable interrupts.
> 5. Copy the registers from where they were saved (possibly some stack) to the process table.
> 6. Run the interrupt-service procedure. It will extract information from the interrupting device controller’s registers.
> 7. Choose which process to run next. If the interrupt has caused some high-priority process that was blocked to become ready, it may be chosen to run now.
> 8. Set up the MMU context for the process to run next. Some TLB setup may also be needed.
> 9. Load the new process’ registers, including its PSW.
> 10. Start running the new process

## 设备驱动程序 Device Drivers

每个连接到计算机上的IO设备都需要某些特定的代码对齐进行控制，这样的代码称为**设备驱动程序（device driver）。**每个设备驱动程序通常处理一类设备。

为了访问设备的硬件（意味着访问设备控制器的寄存器），设备驱动程序通产必须是操作系统内核的一部分，大多数桌面操作系统要求驱动程序运行在内核中，不过MINIX3让驱动程序运行在用户空间，避免了有问题的驱动程序干扰内核。

要有一个定义明确的模型，规定驱动程序做什么事情以及如何与操作系统的其余部分相互作用。**设备驱动程序通常位于操作系统的其余部分的下面。操作系统通常将驱动程序归类于少数的类别之一。最为通用的类别是设备（block device）和字符设备（character device）**

![device driver](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/deviceDriver.png)

 大多数操作系统都定义了一个所有块设备必须支持的标准接口，并且还定义了另一个所有字符设备必须支持的标准接口，这些接口由许多过程组成，操作系统的其余部分可以调用他们让驱动程序工作。

从MS-DOS开始，操作系统转向驱动程序在执行期间动态地加载到系统中。设备驱动程序有若干功能，最明显的是接收来自其上方与设备无关的软件发出的抽象的读写请求，并目睹这些请求被执行。除此之外，还有一些其他的功能必须执行，如对设备的初始化，还有对电影需求和日志事件进行管理。

**驱动程序必须是可重入的（reentrant），这意味着一个正在运行的驱动程序必须预料到第一次调用完成之前第二次被调用。**

驱动程序不运行进行系统调用，但是他们经常需要与内核的其余部分进行交互，对某些内核过程的调用通常是允许的。

## 与设备无关的I/O软件 Device-Independent I/O Software

设备驱动程序和与设备无关的软件之间的确切界限依赖与具体系统（和设备）。与设备无关软件的基本功能是执行对所有设备公共的I/O功能，并向用户层软件提供一个同一个的接口。

![function of device indpendent IO software](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/functionsOftheDeviceIndependentIOSoftware.png)

### 设备驱动程序的统一接口

操作系统的一个主要问题是如何使所有I/O设备和驱动程序看起来或多或少是相同的。设备驱动程序与操作系统其余部分之间的接口是这一问题的一个方面，如果每个设备驱动程序有不同的与操作系统的接口，这意味着，可供系统调用的驱动程序函数随驱动程序的不同而不同，也意味着每个新的驱动程序提供接口都需要大量权限的编程工作。

![uniform inferface for device drivers](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/uniformInterfaceForDevice.png)

图b的设计的工作方式如下，**对于每一种设备类型，例如磁盘或打印机，操作系统定义一组驱动程序必须支持的函数。驱动程序通常包含一张表格，这张表格具有针对这些函数指向驱动程序自身的指针。当驱动程序装载时，操作系统记录下这张函数指针表的地址，所有当操作系统选用调用一个函数时，它可以通过这张表间接调用。这张函数指针表定义了驱动程序与操作系统其余部分之间的接口。给定类型（磁盘、打印机等）的所有设备都必须服从这一要求。**

> Another aspect of having a uniform interface is how I/O devices are named. The device-independent software takes care of mapping symbolic device names onto the proper driver. **For example, in UNIX a device name, such as `/dev/disk0`, uniquely specifies the i-node for a special file, and this i-node contains the major device number, which is used to locate the appropriate driver. The i-node also contains the minor device number, which is passed as a parameter to the driver in order to specify the unit to be read or written. All devices have major and minor numbers, and all drivers are accessed by using the major device number to select the driver.**

主设备号（major device number）用于定位相应的驱动程序，次设备号（minor device number）作为参数传递给驱动程序，用来驱动读或写 的具体单元。

在UNIX和Windows 中，设备是作为命名对象出现在文件系统中，这意味着针对文件的常规保护规则也适用于I/O设备。系统管理员可以为每一个设备设置适当的访问权限。

### 缓冲

考虑一个想要从调制解调器中读入数据的进程，让用户进程执行read系统调用并阻塞自己以等待字符的到来，这是对到来的字符进行处理的一种可能的策略。每个字符的到来都将引起中断。**中断服务过程负责将字符递交给用户进程并将其解除阻塞。**用户进程将字符放在某个地方之后可以对另一个字符执行读操作并且再次阻塞。这一模型如图a

![buffer](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/buffer.png)

图a的问题在于对于每个到来的字符，都必须启动用户进程，对于短暂的数据流量让一个进程运行多次效率会很低。

图b是一种改进措施，用户进程在用户空间提供了一个包含n个字符的缓冲区，并且执行读入n个字符的读操作。**中断服务负责将到来的字符放入该缓冲区直到缓冲区填满，然后唤醒用户进程。**这以方案比以前的方案效率高很多，但是存在一个问题，但字符到来时，如果缓冲区被分页而调出内存会存在什么问题。可用的方案是将缓冲区锁定在内存中，当如果许多进程都在内存中锁定页面，会造成页面池资源收缩而性能降低。

图c的方法为在内核空阿金创建一个缓冲区，并且让中断处理程序将字符放在这个缓冲区中，当缓冲区被填满时，将包含用户缓冲区的页面调入内存（如果需要的话），并且在一次操作中将内核缓冲区的内容复制到用户缓冲区中。

图c的问题是：正当包含用户缓冲区的页面从磁盘调入的时候有新的字符到来，这是缓冲区已满，没有地方放置新到来的字符。**可行的方案是使用两个缓冲区，当一个缓冲区正在被复制到用户空间的时候，另一个缓冲区正在手机新的输入。这种模式被称为双缓冲（double buffering）**

广泛使用的另一个中形式的缓存是**循环缓冲区（circular buffer）**，他有一个内存区域和两个指针组成。一个指针指向下一个空闲的字，新的数据放置在此处。另一个指针指向缓冲区中数据的第一个字。

缓冲对于输出也是是否重要的，但是它也有不利的地方，如果数据被缓冲太多次，性能就会降低。**我们可以看到下图这些所有复制操作都会在很大程度上降低传输速率，因为所有这些步骤都必须是有序地发送。**

![copy of a packet](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/copyAPacket.png)

### 错误报告

当错误发生时，操作系统必须尽最大努力处理。许多错误是设备特定的并且必须由适当的驱动程序来程序来处理，但是错误处理的框架是设备无关的。

一种类型是I/O错误的编程错误，可以直接将错误代码报告给调用者。

另一种是实际的I/O错误，应该由驱动程序决定做什么。

### 分配与释放专用设备

某些设备在任意给定的时刻只能由一个进程使用。一种简单方法是要求进程在代表设备的特殊文件上执行open操作，如果设备是不可用的，那么open就会失败，于是就关闭这样的一个专用设备。另一种方式是让调用者阻塞进行排队。

### 与设备无关的块大小

不同的磁盘可能具有不同的扇区大小，应该由与设备无关的软件来隐藏这一事实并向高层提供一个统一的块大小。

## 用户空间的I/O软件

尽管大部分的I/O软件都在操作系统内部，但是仍然有一小部分在用户空间。系统调用（包括I/O系统调用）通常由库过程实现，如C语言的 `printf` 和 `scanf`

并非所有用户层I/O软件都是由库过程实现的，另一个重要类别是假脱机系统（spooling）。假脱机是多道程序设计系统中独占IO设备的一种方法。

![layers of the I/O software system](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/layerofIOSoftWare.png)

当一个用户程序试图从一个文件中读一个块时，

- 操作系统被调用以完成这一请求
- 与设备无关的软件在缓冲区高速缓存中查找有无要读的块，如果没有，则调用设备驱动程序
- 设备驱动程序让硬件从磁盘中获取该块，然后，进程被阻塞直到磁盘操作完成

当磁盘操作完成时：

- 硬件产生中断，是的中断处理程序运行
- 中断处理程序运行，查明发生了什么事情，唤醒休眠的进程

