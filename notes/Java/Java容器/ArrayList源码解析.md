# ArrayList 源码解析(JDK11)

之前有写过关于 `ArrayList` 的源码解析，当时是基于 JDK8 的，现在 JDK11 变成了 `LTS` 版本，重新看一遍源码并做相关记录。



## 概览

- ArrayList 底层基于动态数组，并且容量可变
- ArrayList 是线程不安全的，效率较高
- ArrayList 中 `size()`, `isEmpty()`, `get()`, `set()`, `iterator()`, and `listIterator()`操作耗时为常数时间
- ArrayList 中 `add()` 耗时为 `amortized constant time`, 也就是所，增加 n 个元素时间复杂度为 `O(n) `
- 其他的操作运行时间为`linear time`
- `constant facto`r 的值比` LinkedList` 的小



- [ ] ```
  amortized constant time
  ```

> ```
> amortized constant time
> 
> The details of the growth policy are not specified beyond the fact that adding an element has constant amortized time cost.
> ```


## 类名

```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```

```java
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>
```

```java
public abstract class AbstractCollection<E> implements Collection<E>
```

- `ArrayList` 继承自  `AbstractList`，这样做的好处是可以减少重复代码， ArrayList 只需要关注自己独有的方法即可。
- `AbstractList` 实现了 `List` 接口，`ArrayList` 又实现了一遍 `List` 接口



## 同步问题

> **Note that this implementation is not synchronized.** If multiple threads access an `ArrayList` instance concurrently, and at least one of the threads modifies the list structurally, it *must* be synchronized externally. (A structural modification is any operation that adds or deletes one or more elements, or explicitly resizes the backing array; merely setting the value of an element is not a structural modification.) This is typically accomplished by synchronizing on some object that naturally encapsulates the list. If no such object exists, the list should be "wrapped" using the [`Collections.synchronizedList`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Collections.html#synchronizedList(java.util.List)) method.  [2]
>
> 翻译：
>
> 注意到 `ArrayList` 的实现是不同步的，如果有多个线程同时访问 `ArrayList` 实例，并且其中至少有一个线程改变` ArrayList` 的结构，此时必须用同步方法(结构上的改变包括新增或删除元素，或者明显改变底层数组大小，仅仅用 `set()`改变其中一个值不是结构性修改)，实现同步的方法通常是用某个类封装 list，通常可以使用 `Collections.synchronizedList()` 方法

 ```
List list = Collections.synchronizedList(new ArrayList(...));
 ```



## fail fast

> The iterators returned by this class's [`iterator`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ArrayList.html#iterator()) and [`listIterator`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ArrayList.html#listIterator(int)) methods are *fail-fast*: if the list is structurally modified at any time after the iterator is created, in any way except through the iterator's own [`remove`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ListIterator.html#remove()) or [`add`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ListIterator.html#add(E)) methods, the iterator will throw a [`ConcurrentModificationException`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ConcurrentModificationException.html). Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
>
> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw `ConcurrentModificationException` on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: *the fail-fast behavior of iterators should be used only to detect bugs.* [2]
>
> 翻译：
>
> 由 `iterator` 或  `listIterator` 方法生成的 iterators遵从 `fail-fast` 规则：如果在创建了 iterator 之后，  list 被以任何方法（除了 iterator 自身的 remove, add 方法）进行结构性修改， iterator 会抛出 `ConcurrentModificationException` ,因此，当存在并行修改时， iterator 会快速失败，而不是在未来位置的时间做出冒险的位置操作。
>
> 需要注意 `fail-fast`行为 不能保证正如设计的那样，通常来说，发生非同步并发修改时，很难做出强有力的保证 `fail-fast`， Fail-fast iterators 抛出 `ConcurrentModificationException` 是基于尽力而为的基础。因此，编程时基于这个 exception 来保证准确性是错误的做法， fail-fast 应该仅仅用来检测 bugs。

代码示例：

```java
List<Integer> list = new ArrayList<>();
list.add(null);
list.add(null);
list.add(2);

Iterator<Integer> i = list.iterator();
list.remove(1);
i.next(); // throw java.util.ConcurrentModificationException
```

```java
Iterator<Integer> i = list.iterator();
i.next(); // it's ok
list.remove(1); 
```





## Reference

1. [Java核心技术卷一](https://book.douban.com/subject/1781451/)
2. [Class ArrayList](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ArrayList.html)
3. [Java container source code-ArrayList source code analysis (based on JDK8)](https://programming.vip/docs/java-container-source-code-arraylist-source-code-analysis-based-on-jdk8.html)

