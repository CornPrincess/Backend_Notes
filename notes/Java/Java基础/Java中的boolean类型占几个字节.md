准备面试时看到了这个问题，觉得很有趣，而且网上靠谱的资料也比较少，在这里总结下。

首先第一反映是去 Java 语言规范中查找相关资料，找到结果如下，不能得到准确的答案：

> The `boolean` data type has only two possible values: `true` and `false`. Use this data type for simple flags that track true/false conditions. This data type represents one bit of information, but its "size" isn't something that's precisely defined.[1] 
>
> boolean 只有两个值：false和true，只代表了1bit 的信息，但是它的真实大小是没有精确数据的

继续在 JVM 规范中查找相关信息。

> Although the Java Virtual Machine defines a `boolean` type, it only provides very limited support for it. There are no Java Virtual Machine instructions solely dedicated to operations on `boolean` values. Instead, expressions in the Java programming language that operate on `boolean` values are compiled to use values of the Java Virtual Machine `int` data type.
>
> The Java Virtual Machine does directly support `boolean` arrays. Its *newarray* instruction ([§*newarray*](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-6.html#jvms-6.5.newarray)) enables creation of `boolean` arrays. Arrays of type `boolean` are accessed and modified using the `byte` array instructions *baload* and *bastore* ([§*baload*](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-6.html#jvms-6.5.baload), [§*bastore*](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-6.html#jvms-6.5.bastore)).
>
> In Oracle’s Java Virtual Machine implementation, `boolean` arrays in the Java programming language are encoded as Java Virtual Machine `byte` arrays, using 8 bits per `boolean` element.
>
> The Java Virtual Machine encodes `boolean` array components using `1` to represent `true` and `0` to represent `false`. Where Java programming language `boolean` values are mapped by compilers to values of Java Virtual Machine type `int`, the compilers must use the same encoding.
>
> Java 虚拟机虽然定义了 boolean 类型，但是支持是有限的，没有专门的虚拟机指令处理 boolean 值，对 boolean 值的操作被替换成 int 数据类型。
>
> Java 虚拟机没有直接支持 boolean 数组， boolean 类型数组和 byte 数组共有指令。
>
> 在 Oracle 的 Java 虚拟机中，Java 语言的 boolean 数组被编码为 byte 数组，每个元素 8 bits
>
> Java 虚拟机使用 1 表示 true ，0 表示 false 来编码 boolean 数组。Java 语言的 boolean 值被编译器映射成 Java 虚拟机的 int 类型的时候，也是采用一样的编码。

如果要储存多个Boolean对象时，可以使用BitSet，占有内存空间更小。

## Reference

1. [The Java™ Tutorials](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html)
2. [The Structure of the Java Virtual Machine](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-2.html#jvms-2.3.4)

