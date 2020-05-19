# HashMap源码解析(JDK11)

## 概览

![HashMap](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/HashMap.png)

- HashMap 根据 key 的 hashCode 值进行储存数据，大多数情况喜爱可以直接定位到他的值，因此有很快的访问速度

- 遍历的顺序不确定，并且不能保证顺序不改变

- 允许一条记录的 key 为 null，且允许多条记录的 value 为 null

- 线程不安全，即同一时刻有多个线程同时写 HashMap，对HashMap 造成结构性变化（A structural modification is any operation that adds or deletes one or more mappings; merely changing the value associated with a key that an instance already contains is not a structural modification.），可能会导致数据不一致，此时可以使用一下方法获得同步方法：

  ```java
  Map m = Collections.synchronizedMap(new HashMap(...));
  ```

  或者使用 ConcurrentHashMap

- 提供了耗时 constant-time 的基本操作（get， put）

- Iteration over collection views requires time proportional to the "capacity" of the `HashMap` instance (the number of buckets) plus its size (the number of key-value mappings). Thus, it's very important not to set the initial capacity too high (or the load factor too low) if iteration performance is important.

- As a general rule, the default load factor (0.75) offers a good tradeoff between time and space costs. 

- Note that using many keys with the same `hashCode()` is a sure way to slow down performance of any hash table. To ameliorate impact, when keys are [`Comparable`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/lang/Comparable.html), this class may use comparison order among keys to help break ties.

## 类名

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
```



## 参数

```java
private static final long serialVersionUID = 362498820763181265L;

/**
 * The default initial capacity - MUST be a power of two.
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

/**
 * The maximum capacity, used if a higher value is implicitly specified
 * by either of the constructors with arguments.
 * MUST be a power of two <= 1<<30.
 */
static final int MAXIMUM_CAPACITY = 1 << 30;

/**
 * The load factor used when none specified in constructor.
 */
static final float DEFAULT_LOAD_FACTOR = 0.75f;

/**
 * The bin count threshold for using a tree rather than list for a
 * bin.  Bins are converted to trees when adding an element to a
 * bin with at least this many nodes. The value must be greater
 * than 2 and should be at least 8 to mesh with assumptions in
 * tree removal about conversion back to plain bins upon
 * shrinkage.
 */
static final int TREEIFY_THRESHOLD = 8;

/**
 * The bin count threshold for untreeifying a (split) bin during a
 * resize operation. Should be less than TREEIFY_THRESHOLD, and at
 * most 6 to mesh with shrinkage detection under removal.
 */
static final int UNTREEIFY_THRESHOLD = 6;

/**
 * The smallest table capacity for which bins may be treeified.
 * (Otherwise the table is resized if too many nodes in a bin.)
 * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
 * between resizing and treeification thresholds.
 */
static final int MIN_TREEIFY_CAPACITY = 64;

```

### MAXIMUM_CAPACITY

`MAXIMUM_CAPACITY` 表示 table 的最大容量，它是 int 类型，因此最大为 `1 << 30`(最高位为符号位)

### 为什么 Map 的容量要限制为2的整数次方

Map 的容量作出这样的限制主要是为了快速定位到数组的位置，并且使数据更加分散，减少碰撞，查看 getNode 源码我们可以发现 ：

```java
final Node<K,V> getNode(int hash, Object key) {
  Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
  if ((tab = table) != null && (n = tab.length) > 0 &&
      (first = tab[(n - 1) & hash]) != null) {
    if (first.hash == hash && // always check first node
        ((k = first.key) == key || (key != null && key.equals(k))))
      return first;
    if ((e = first.next) != null) {
      if (first instanceof TreeNode)
        return ((TreeNode<K,V>)first).getTreeNode(hash, key);
      do {
        if (e.hash == hash &&
            ((k = e.key) == key || (key != null && key.equals(k))))
          return e;
      } while ((e = e.next) != null);
    }
  }
  return null;
}
```

`first = tab[(n - 1) & hash]` 这行代码的作用是通过hash值取出数组中的值，其中 tab 为数组，n 为数组长度，hash为key 的hash值，在默认情况下，n = 16，则n - 1 = 15，二进制为 1111， 这样进行 & hash 操作最终结果还是 hash， 假设n = 15 ，n - 1 = 14（1110）， 这样会造成某些位置（如0001， 0011， 0101...）无法存放元素，空间浪费很大，并且数组中可以使用的位置比数组长度小了很多，这意味着进一步增加了碰撞的几率，减慢了查询的效率。因此数组长度使用 2 的整数次方，可以是数据分布得更分散，减少碰撞。

### DEFAULT_LOAD_FACTOR

默认的装载因子为 0.75，这是因为bin（指数组中的一个桶） 中节点出现的概率遵循Poisson分布（泊松分布），此时load factor = 0.75， λ=0.5
$$
P(X=k) =\frac{\lambda^ke^{-\lambda}}{k!}
$$
假如在长度为 16 的数组中放入0.75 * 16 = 12个数据时，数组中某个下表放入 k 个数据（即数组后面链表中的数据量）的概率如下

| 存储数据量 |    概率    |
| :--------: | :--------: |
|     0      | 0.60653066 |
|     1      | 0.30326533 |
|     2      | 0.07581633 |
|     3      | 0.01263606 |
|     4      | 0.00157952 |
|     5      | 0.00015795 |
|     6      | 0.00001316 |
|     7      | 0.00000094 |
|     8      | 0.00000006 |



## 域

```java
/**
 * The table, initialized on first use, and resized as
 * necessary. When allocated, length is always a power of two.
 * (We also tolerate length zero in some operations to allow
 * bootstrapping mechanics that are currently not needed.)
 */
transient Node<K,V>[] table;

/**
 * Holds cached entrySet(). Note that AbstractMap fields are used
 * for keySet() and values().
 */
transient Set<Map.Entry<K,V>> entrySet;

/**
 * The number of key-value mappings contained in this map.
 */
transient int size;

/**
 * The number of times this HashMap has been structurally modified
 * Structural modifications are those that change the number of mappings in
 * the HashMap or otherwise modify its internal structure (e.g.,
 * rehash).  This field is used to make iterators on Collection-views of
 * the HashMap fail-fast.  (See ConcurrentModificationException).
 */
transient int modCount;

/**
 * The next size value at which to resize (capacity * load factor).
 *
 * @serial
 */
// (The javadoc description is true upon serialization.
// Additionally, if the table array has not been allocated, this
// field holds the initial array capacity, or zero signifying
// DEFAULT_INITIAL_CAPACITY.)
int threshold;

/**
 * The load factor for the hash table.
 *
 * @serial
 */
final float loadFactor;
```



 ## 构造函数

```java
/**
 * Constructs an empty {@code HashMap} with the default initial capacity
 * (16) and the default load factor (0.75).
 */
public HashMap() {
  this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}

public HashMap(int initialCapacity) {
  this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

public HashMap(int initialCapacity, float loadFactor) {
  if (initialCapacity < 0)
    throw new IllegalArgumentException("Illegal initial capacity: " +
                                       initialCapacity);
  if (initialCapacity > MAXIMUM_CAPACITY)
    initialCapacity = MAXIMUM_CAPACITY;
  if (loadFactor <= 0 || Float.isNaN(loadFactor))
    throw new IllegalArgumentException("Illegal load factor: " +
                                       loadFactor);
  this.loadFactor = loadFactor;
  this.threshold = tableSizeFor(initialCapacity);
}

static final int tableSizeFor(int cap) {
  int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
  return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

public HashMap(Map<? extends K, ? extends V> m) {
  this.loadFactor = DEFAULT_LOAD_FACTOR;
  putMapEntries(m, false);
}

final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
  int s = m.size();
  if (s > 0) {
    if (table == null) { // pre-size
      float ft = ((float)s / loadFactor) + 1.0F;
      int t = ((ft < (float)MAXIMUM_CAPACITY) ?
               (int)ft : MAXIMUM_CAPACITY);
      if (t > threshold)
        threshold = tableSizeFor(t);
    }
    else if (s > threshold)
      resize();
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      K key = e.getKey();
      V value = e.getValue();
      putVal(hash(key), key, value, false, evict);
    }
  }
}
```

通过四个构造函数我们可以看到，其主要工作为初始化这些参数：`loadFactor`， `threshold`，**并没有实际初始化链表和数组，这样可以节省空间。**

在 JDK8 中， threshold 的初始化方法：

```java
/**
 * Returns a power of two size for the given target capacity.
 */
static final int tableSizeFor(int cap) {
  int n = cap - 1;
  n |= n >>> 1;
  n |= n >>> 2;
  n |= n >>> 4;
  n |= n >>> 8;
  n |= n >>> 16;
  return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

JDK11 中调用了 `Integer.numberOfLeadingZeros()` 方法，该方法主要作用：指定 int 值的二进制补码表示形式中最高位（最左边）的 1 位之前，返回零位的数量。 可以参考博客：[jdk11源码--Integer.numberOfLeadingZeros](https://it007.blog.csdn.net/article/details/87946142)

```java
/**
 * Returns a power of two size for the given target capacity.
 */
static final int tableSizeFor(int cap) {
  int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
  return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

以上两个 `tableSizeFor(int cap)` 都是返回最近的不小于输入参数的2的整数次幂。比如10，则返回16，集体步骤如下：

- Integer.numberOfLeadingZeros(cap - 1) = 28，二进制表示为 `00000000 00000000 00000000 00011100`
- n = -1 >>> 28:
  - -1 的原码：`10000000 00000000 00000000 00000001`
  - -1 的反码：`11111111 11111111 11111111 11111110`
  - -1 的补码：`11111111 11111111 11111111 11111111`
  - -1 无符号 右移28位：  `00000000 00000000 00000000 00001111` = 15
- 最终结果：15 + 1 = 16

##  存储结构

HashMap 的存储结构为 数组 + 链表 + 红黑树(JDK8后新加)，我们看源码需要弄清楚两个问题：数据底层具体存储的是什么？这样的存储方式有什么好处？ 



![hashMap structure](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/HashMapStructure.png)

### Node

HashMap 中的 Node 内部类实现了  Map.Entry 接口，本质是一个键值对，并且 HashMap 内部有一个Node<K, V>[] table 字段，这是一个 哈希桶数组

> ```
> public static interface Map.Entry<K,V>
> ```
>
> A map entry (key-value pair). The `Map.entrySet` method returns a collection-view of the map, whose elements are of this class. The *only* way to obtain a reference to a map entry is from the iterator of this collection-view. **These `Map.Entry` objects are valid *only* for the duration of the iteration; more formally, the behavior of a map entry is undefined if the backing map has been modified after the entry was returned by the iterator, except through the `setValue` operation on the map entry.**

```java
/**
 * The table, initialized on first use, and resized as
 * necessary. When allocated, length is always a power of two.
 * (We also tolerate length zero in some operations to allow
 * bootstrapping mechanics that are currently not needed.)
 */
transient Node<K,V>[] table;

/**
 * Basic hash bin node, used for most entries.  (See below for
 * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
 */
static class Node<K,V> implements Map.Entry<K,V> {
  // 用来定位数据索引的位置
  final int hash;
  final K key;
  V value;
  // 链表的下一个 Node
  Node<K,V> next;

  Node(int hash, K key, V value, Node<K,V> next) {
    this.hash = hash;
    this.key = key;
    this.value = value;
    this.next = next;
  }

  public final K getKey()        { return key; }
  public final V getValue()      { return value; }
  public final String toString() { return key + "=" + value; }

  public final int hashCode() {
    return Objects.hashCode(key) ^ Objects.hashCode(value);
  }

  public final V setValue(V newValue) {
    V oldValue = value;
    value = newValue;
    return oldValue;
  }

  public final boolean equals(Object o) {
    if (o == this)
      return true;
    if (o instanceof Map.Entry) {
      Map.Entry<?,?> e = (Map.Entry<?,?>)o;
      if (Objects.equals(key, e.getKey()) &&
          Objects.equals(value, e.getValue()))
        return true;
    }
    return false;
  }
}
```



## fail fast

> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw `ConcurrentModificationException` on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: *the fail-fast behavior of iterators should be used only to detect bugs.*



## Reference

1. [Java核心技术卷一](https://book.douban.com/subject/1781451/)
2. [Java 8系列之重新认识HashMap](https://tech.meituan.com/2016/06/24/java-hashmap.html)
3. [JDK11源码--HashMap源码分析](https://blog.csdn.net/liubenlong007/article/details/87937209)