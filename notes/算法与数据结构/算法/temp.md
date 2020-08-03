## 散列表

如果所有键都是小整数，我们可以使用一个数组来实现无序的符号表，将键作为数组的索引而数组中 i

处储存的就是它对应的值，这样我们就可以快速访问任意键的值。

使用散列的查找算法分为两步：

- 使用散列函数hash functions 将转换的键转换为数组的一个索引
- 处理碰撞冲突collision-resolution的过程，可以用**拉链法和线性探测法**

### 散列函数

我们要找的散列函数应该易于计算并且能够均匀分布所有的键，即如果有一个能够保存M个键值对的数组，对于任意键，0 到 M-1 之间的每个整数都有相等的可能性与之一一对应。

散列函数和键的类型有关，**对于每种类型的键我们都需要有一个与之对应的散列函数。**

如果 a.equals(b) == true ，则 hashcode(a) == hashcode(b)

如果  hashcode(a) != hashcode(b)， 则 a.equals(b) == false

如果 hashcode(a) == hashcode(b)，则 a.equals(b) 可能为true，也可能为 false

如计算 hashcode 是很耗时的操作，我们可以将每个键的散列值缓存起来，即在每个键中使用一个 hash 变量来保存他的 hascode() 的返回值。Java 中的 String 就使用了软缓存。

要为数据类型实现一个优秀的散列算法需要满足三个条件：

- 一致性 —— 等价的键必然产生相等的散列值
- 高效性 —— 计算简便
- 均匀性 —— 均匀地散列所有的键

> - It should be  deterministic —equal keys must produce the same hash value
> - It should be efficient to compute
> - It should *uniformly distribute the keys*.

### 基于拉链法的散列表 Hashing with separate chaining

- [ ] 补图

当发生冲突时，一种直接的办法是将大小为 M 的数组中的每个元素指向一条链表，链表中的每个结点都储存了散列值为该元素的索引的键值对，这种方法称为**拉链法。**

这个方法的基本思想是选择足够大的M，使得所有链表都尽可能短以保证高效的查找。

散列最主要的目的在于均匀地将键散布开，因此在**计算散列后键的顺序信息就丢失了**。基于拉链法的散列表的实现简单，**在键的顺序不重要的应用中，它可能是最快的符号表的实现。**

> **Proposition K.** In a separate-chaining hash table with M lists and N keys, the probability (under Assumption J) that the number of keys in a list is within a small constant factor of N/M is extremely close to 1. of N/M is extremely close to 1. (Assumes an idealistic hash function.)

> **Property L.** In a separate-chaining hash table with M lists and N keys, **the number of compares (equality tests) for search and insert is proportional to N/M.**

### 基于线性探测法的散列表 Hashing with linear probing

实现散列表的另一种方式就是用大小为M的数组保存N个键值对，其中 M > N，我们需要依靠数组中的空位解决碰撞冲突，基于这种策略的所有方法称为**开放地址散列表（open-addressing hashing methods.）**

开放地址散列表中最简单的方法叫做**线性探测法（linear probing）**：当碰撞发生时，我们直接检查散列表中的下一个位置（将索引加1），这样的线性探测可能会产生三种结果：

- 命中，该位置的键和被查找的键相同
- 未命中，键为空（该位置没有键）
- 继续查找，该位置的键和被查找的键不同

- [ ] 补图

开放地址类的散列表的核心思想是与其将内存用作链表，不如将它们作为在散列表的空元素，这些空元素可以作为查找结束的标志。



**散列表的查找比红黑树快吗？**

**这取决于键的类型，它决定了 hashCode() 的计算成本是否大于 compareTo() 的比较成本。对于常见的键类型以及 Java 的默认实现，这两者的成本是近似的。因此散列表会比红黑树块得多，因为它所需的操作次数是固定的。但需注意的是，如果要进行有序性相关的操作，这个问题就没有意义了，因为散列表无法高效地支持这些操作。**



相对于二叉查找树，散列表的优点在于代码更简单，且查找时间最优（常数级别，只有键的数据类型是标准的或者简单到我们可以为它写出满足（或者接近满足）均匀性假设的高效散列函数即可）。二叉查找数的优点在于抽象结构更简单（不需要设计散列函数），红黑树可以保证最坏情况下的性能且它能够支持的操作更多（如排名，选择，排序和范围查找）

Q & A
Why does Java use 31 in the hashCode() for String?
It's prime, so that when the user mods out by another number, they have no common factors (unless it's a multiple of 31). 31 is also a Mersenne prime (like 127 or 8191) which is a prime number that is one less than a power of 2. This means that the mod can be done with one shift and one subtract if the machine's multiply instruction is slow.

How do you extract the bits from a variable of type double for use in hashing?
`Double.doubleToLongBits(x)` returns a 64-bit long integer whose bit representation is the same as the floating-point representation of the double value x.

What's wrong with using (s.hashCode() % M) or Math.abs(s.hashCode()) % M to hash to a value between 0 and M-1?
The % operator returns a non-positive integer if its first argument is negative, and this would create an array index out-of-bounds error. Surprisingly, the absolute value function can even return a negative integer. This happens if its argument is Integer.MIN_VALUE because the resulting positive integer cannot be represented using a 32-bit two's complement integer. This kind of bug would be excruciatingly difficult to track down because it would only occur one time in 4 billion! [ The String hash code of "polygenelubricants" is -2^31. ]



- [ ] 补图 各种符号表的性能



