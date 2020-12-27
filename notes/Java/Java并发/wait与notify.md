# wait与notify

最近面试互联网公司时经常出现这类代码题，相信 talk is cheap, show me the code 的面试官还是占多数的，其中 wait 和 notify 是重点。早点将这些知识点都掌握好，就可以早点享受这些知识的福利。

随着学习的深入，真感觉算法与数据结构这门课很关键，看底层原理和源码的时候处处可以感受到算法与数据结构的影子。

## wait与notify的通常用法

Wait 和 notify 都是 Object 自带的方法，我们首先看一下相应的源码和注释

