1. Math.abs(-2147483648) = -2147483648 ，因为数据溢出（integer overflow）

2. 为什么数组的起始索引是0而不是1:

> This convention originated with machine-language programming, where the ad- dress of an array element would be computed by adding the index to the address of the beginning of an array. Starting indices at 1 would entail either a waste of space at the beginning of the array or a waste of time to subtract the 1.

3. 可以在命令行使用 `-enableassertions` 标志（简写为-ea）来启用断言

4. 为什么要区别原始类型和引用类型，为什么不只用引用类型？ 因为性能，原始类型更接近计算机硬件支持的数据类型，因此使用它们的程序比使用引用类型的程序运行地更快

5. 和Java引用一样，可以把指针看做是机器地址，在许多编程语言中，指针是一种原始数据类型。在Java中创建引用的办法只有一种（new），且改变引用的方法也只有一种（赋值语句），Java的引用被称为安全指针，因为Java保证每个引用都会指向特定类型的对象。

6. Java中可以通过反射来改变String的值，可见[例子](https://algs4.cs.princeton.edu/12oop/MutableInteger.java.html)
7. Java 中使用引用类型的赋值语句将会创建该引用的一个副本。赋值语句不会创建新的对象，而只是另那个指向某个已经存在的对象的引用。

8.因为String对象是不可变的，这种设计使得String的实现在能够在多个对象含有相同的value[]数组时节省内存。

9.当调用subString创建自字符串时，就创建了一个新的String对象（大约占用40字节），但它仍然重用了相同的value[]数组，因此该字符串的字字符串只会使用40字节的内存。含有原始字符串的字符数组的别名存在与子字符串中，子字符串对象的偏移量和长度域标记了子字符串的位置。**换句话说，一个子字符串所需的额外内存是一个常数，构造一个子字符串所需的时间也是常数，即使字符串和子字符串的长度极大也是这样的。**
