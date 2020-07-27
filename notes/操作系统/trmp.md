因为现代CPU都采用了流水线 pipeline 的设计，这可以加快速度，但是同时也存在了问题(如果正在写的指令改变要读的指令的中的数据，此时会出现脏读)，即指令之间的相互依赖关系必须要先理清，为了执行的最快速度，这个时候会使用指令重排，预测指令会出现在哪里。

![pipeline](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/pipeline.png)

多线程中出现的禁止指令重排的命令，大概就是出于此处，我认为面试计算机基础的时候，可以由一种很简单的语言特性，进而深入分析到计算机操作系统，以及计算机组成原理中来。

![pipeline super scalar](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/pipelinesuperscalar.png)

![multicore ](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/operating%20system/multicore.png)

举一反三：计算机中所遇到的问题，以及遇到问题所要解决的思路很多时候都类似