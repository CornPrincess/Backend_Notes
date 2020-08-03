# HashMap源码解析(JDK11)

## 概览

![HashMap](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/HashMap.png)

- HashMap 根据 key 的 hashCode 值进行储存数据，大多数情况喜爱可以直接定位到他的值，因此有很快的访问速度

- 遍历的顺序不确定，并且不能保证顺序不改变

- 扩容是一个特别耗性能的操作，所以 在使用时可以先给一个大致的容量

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

> Because TreeNodes are about twice the size of regular nodes, we use them only when bins contain enough nodes to warrant use (see TREEIFY_THRESHOLD). And when they become too small (due to removal or resizing) they are converted back to plain bins.  In usages with well-distributed user hashCodes, tree bins are rarely used.  Ideally, under random hashCodes, the frequency of nodes in bins follows a Poisson distribution
>
> 翻译：
>
> treeNodes 大约是 普通 nodes 两倍，因此我们只在达到 TREEIFY_THRESHOLD（8）时会使用， 并且当 treeNodes 数量变少达到 UNTREEIFY_THRESHOLD（6）时，会将树转变为正常 node。使用hashcode使得节点散列正常，红黑树在平时很少用到。理论上，在随机的 hashcode 情况下，节点出现的概率遵从 Poisson 分布


默认的装载因子为 0.75，这是因为bin（指数组中的一个桶） 中节点出现的概率遵循Poisson分布（泊松分布），此时load factor = 0.75， λ=0.5
$$
P(X=k) =\frac{\lambda^ke^{-\lambda}}{k!}
$$
假如在长度为 16 的数组中放入0.75 * 16 = 12个数据时，数组中某个下标放入 k 个数据（即数组后面链表中的数据量）的概率如下

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

我们可以看到，一个下标中存在8个nodes的概率是很低的，因此 此时将链表转为 treeNode是合乎情理的，并且性价比较高 ，可以通过增加有限的空间消耗换取时间消耗上的减少。

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

### threshold

???

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



## 重要方法

首先明白一下概念，HashMap就是使用哈希表来存储的。哈希表为解决冲突，可以采用开放地址法和链地址法等来解决问题，Java中HashMap采用了链地址法。链地址法，简单来说，就是数组加链表的结合。在每个数组元素上都一个链表结构，当数据被Hash后，得到数组下标，把数据放在对应下标元素的链表上。

### 确定哈希桶数组索引位置

```java
/**
 * Computes key.hashCode() and spreads (XORs) higher bits of hash
 * to lower.  Because the table uses power-of-two masking, sets of
 * hashes that vary only in bits above the current mask will
 * always collide. (Among known examples are sets of Float keys
 * holding consecutive whole numbers in small tables.)  So we
 * apply a transform that spreads the impact of higher bits
 * downward. There is a tradeoff between speed, utility, and
 * quality of bit-spreading. Because many common sets of hashes
 * are already reasonably distributed (so don't benefit from
 * spreading), and because we use trees to handle large sets of
 * collisions in bins, we just XOR some shifted bits in the
 * cheapest possible way to reduce systematic lossage, as well as
 * to incorporate impact of the highest bits that would otherwise
 * never be used in index calculations because of table bounds.
 * 
 * 翻译：
 * 计算 key.hashCode()的值，并且将hash的高位通过异或（XORs）扩展到地位，由于table     	
 * 使用2的指数进行masking，因此仅在其范围进行mask的hash将会发生冲突（已知的例子为在     	
 * 小table中连续的Float keys）所以这里使用了转换将高位延展到地位，这是在speed 		 	
 * utility和quality of bit-spreading之间的tradeoff，因为很多常见的hash已经合理       
 * 地分布 （所以不会从spread中受益），并且我哦们使用红黑树来处理多hash冲突，我们只XOR 	
 * 一 些位来减少系统缺失，并且否则高位的不准确影响不会影响到index 计算。
 *
 */
static final int hash(Object key) {
  int h;
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

//jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
static int indexFor(int h, int length) {  
  return h & (length-1);  
}

// JDK 1.8 源码
final Node<K,V> getNode(int hash, Object key) {
  // ...
	(first = tab[(n - 1) & hash]) != null)
  // ...
}
```

确定数组索引位置一共有三步：

- 取 hashCode值：h = key.hashCode()
- 高位参与运算：h ^ (h >>> 16)（此实现从JDK1.8开始，优化了高位运算的算法，通过hashCode()的高16位异或低16位实现的：(h = k.hashCode()) ^ (h >>> 16)，主要是从速度、功效、质量来考虑的，这么做可以在数组table的length比较小的时候，也能保证考虑到高低Bit都参与到Hash的计算中，同时不会有太大的开销。）
- 取模运算：（n - 1）& hash (n 为 capacity，这里利用位运算进行取模，效率更高，该表达式相当于 hash % n)

我们这里重点看一下取模运算，这里用了位运算，而没有使用 `%`，这是因为 数组的长度 n （capacity）是2的指数

我们这里以 x = 1 << 4 为例

```java
x: 		 00010000
x - 1: 00001111
```

令 y  与 x - 1 做 & 运算

```java
y :        10110110
x - 1:     00001111
y&(x - 1): 00000110
```

这个表达式与 y 对 x 取模结果是一样的

```java
y :     10110110
x :     00010000
y & x : 00000110
```

我们还能从运算中得出结论：**数组的 长度 n 越小，最终计算出的 索引 越有可能发生碰撞，因此我们在进行取模运算之前需要先进行高位异或运算，来减小发生碰撞的概率。**仅仅异或一下，既减少了系统的开销，也不会造成因为高位没有参与下标的计算(table长度比较小时)，从而引起的碰撞。如下表中的例子

| key.hashCode() | `00001010 00001111 00001001 00001101` |
| :------------: | :-----------------------------------: |
| 无符号右移16位 | `00000000 00000000 00001010 00001111` |
| 两者疑惑的结果 | `00001010 00001111 00000011 00000010` |

**另外有一点要注意，`HashMap` 允许 `null` 作为 `key`， 所以在 `hash` 方法中我们可以看到 `null` 对象的 `hash` 结果为 0， `HashMap` 使用第 0 个桶来存放键为 `null` 的键值对。**

### put 方法

```java
public V put(K key, V value) {
  return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
  Node<K,V>[] tab; Node<K,V> p; int n, i;
  // 1. 判断 table 是否为空或为null，如果为空或null则调用 `resize()` 进行初始化
  if ((tab = table) == null || (n = tab.length) == 0)
    n = (tab = resize()).length;
  // 2. 判断当前索引是否有值
  if ((p = tab[i = (n - 1) & hash]) == null)
    tab[i] = newNode(hash, key, value, null);
  else {
    Node<K,V> e; K k;
    // 3. 判断数组当前索引存在的 Node 的 hash 与正要加入 Node 的hash 是否相同，以及两者的 key 是否同一引用或者equals方法返回true
    if (p.hash == hash &&
        ((k = p.key) == key || (key != null && key.equals(k))))
      e = p;
    // 4. 判断目前索引的值是否在红黑树上
    else if (p instanceof TreeNode)
      e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
    else {
      // 5. 遍历当前索引链表，如果链表长度大于8，则将链表转为红黑树，在红黑树中进行插入操作，否则进行链表的插入操作，如果遍历过程中发现相同的node，则直接退出循环
      for (int binCount = 0; ; ++binCount) {
        if ((e = p.next) == null) {
          p.next = newNode(hash, key, value, null);
          if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
            treeifyBin(tab, hash);
          break;
        }
        // key 已经存在则直接覆盖
        if (e.hash == hash &&
            ((k = e.key) == key || (key != null && key.equals(k))))
          break;
        p = e;
      }
    }
    if (e != null) { // existing mapping for key
      V oldValue = e.value;
      if (!onlyIfAbsent || oldValue == null)
        e.value = value;
      afterNodeAccess(e);
      return oldValue;
    }
  }
  ++modCount;
  // 6. 判断是否超过 threshold，超过则扩容
  if (++size > threshold)
    resize();
  afterNodeInsertion(evict);
  return null;
}

/*
 * The following package-protected methods are designed to be
 * overridden by LinkedHashMap, but not by any other subclass.
 * Nearly all other internal methods are also package-protected
 * but are declared final, so can be used by LinkedHashMap, view
 * classes, and HashSet.
 */

// Create a regular (non-tree) node
Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
  return new Node<>(hash, key, value, next);
}
```

Put 方法可以抽象概括为一下六步：

1. 判断 table 是否为空或为null，如果为空或null则调用 `resize()` 进行初始化。
2. 计算 key 的 hash 值，并最终通过上面的方法求出该 key 在数组中对应的索引 i，如果 table[i] == null,直接通过 `newNode()` 新建非树节点（Node），转向 步骤6，否则转向 步骤3.
3. 判断数组当前索引存在的 **首个Node** 的 hash 与正要加入 Node 的hash 是否相同（用到`key.hashCode()`），以及两者的 key 是否同一引用或者equals方法(用到 `key.equals()`)返回true，**这一步就解释了为什么作为 key 的类一定要重写 `hashCode` 和 `equals` 方法。**比较之后如果相同，则直接覆盖 value，否则 步骤4。
4. 判断 table[i] 是否为 TreeNode，即是否是红黑树，如果是红黑树直接在树上插入键值对，否则转向 步骤5.
5. 遍历当前索引链表，如果链表长度大于8，则将链表转为红黑树，在红黑树中进行插入操作，否则进行链表的插入操作，如果遍历过程中发现相同的node，则直接覆盖。**从源码中我们可以看到，从JDK8开始，链表的插入方法由头插法改成了尾插法。**
6. 插入成功后（这里的插入指之前 i 索引处无 node时插入），判断实际存在的键值对数量是否超过了最大容量 threshold，如果超过，进行扩容。



### resize 方法

```java
/**
 * Initializes or doubles table size.  If null, allocates in
 * accord with initial capacity target held in field threshold.
 * Otherwise, because we are using power-of-two expansion, the
 * elements from each bin must either stay at same index, or move
 * with a power of two offset in the new table.
 *
 * @return the table
 */
final Node<K,V>[] resize() {
  Node<K,V>[] oldTab = table;
  int oldCap = (oldTab == null) ? 0 : oldTab.length;
  int oldThr = threshold;
  int newCap, newThr = 0;
  if (oldCap > 0) {
    // 当数组容量达到最大时不再扩容
    if (oldCap >= MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return oldTab;
    }
    else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
             oldCap >= DEFAULT_INITIAL_CAPACITY)
      newThr = oldThr << 1; // double threshold
  }
  else if (oldThr > 0) // initial capacity was placed in threshold
    newCap = oldThr;
  // 创建 hashMap 之后第一次put
  else {               // zero initial threshold signifies using defaults
    newCap = DEFAULT_INITIAL_CAPACITY;
    newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
  }
  if (newThr == 0) {
    float ft = (float)newCap * loadFactor;
    newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
              (int)ft : Integer.MAX_VALUE);
  }
  threshold = newThr;
  @SuppressWarnings({"rawtypes","unchecked"})
  Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
  table = newTab;
  // 循环遍历老map中的元素，迁移到对应新数组中去，进行扩容操作
  if (oldTab != null) {
    for (int j = 0; j < oldCap; ++j) {
      Node<K,V> e;
      if ((e = oldTab[j]) != null) {
        // 老数组中元素引用置为null
        oldTab[j] = null;
        // 该索引只有一个元素，则直接计算新索引将 e 复制到新数组
        if (e.next == null)
          newTab[e.hash & (newCap - 1)] = e;
        // 如果是红黑树
        else if (e instanceof TreeNode)
          ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
        // 如果该所以链表由多个值
        else { // preserve order
          Node<K,V> loHead = null, loTail = null;
          Node<K,V> hiHead = null, hiTail = null;
          Node<K,V> next;
          do {
            next = e.next;
            // 这里通过当前hash与数组长度进行逻辑与操作，判断是否为0，来区分该元素是不变位置还是需要重新更换位置
            if ((e.hash & oldCap) == 0) {
              if (loTail == null)
                loHead = e;
              else
                loTail.next = e;
              loTail = e;
            }
            else {
              if (hiTail == null)
                hiHead = e;
              else
                hiTail.next = e;
              hiTail = e;
            }
          } while ((e = next) != null);
          if (loTail != null) {
            loTail.next = null;
            newTab[j] = loHead;
          }
          if (hiTail != null) {
            hiTail.next = null;
            // 现有index+现有数组的长度就是新数组中的索引位置
            newTab[j + oldCap] = hiHead;
          }
        }
      }
    }
  }
  return newTab;
}
```

分析以上代码可以得出如下结论：

- 新建一个 HashMap 对象后，第一次 `put` 会触发 `resize` 操作，此时会用 `threshold`(初始threshold为12) 来初始化 `capacity` (line 687 `newCap = oldThr;`)
- 当当前数组长度 >= 16 时，数组会扩容为原来的两倍（`newCap = oldCap << 1`` newThr = oldThr << 1`）
- 扩容时不会重新计算hash值，hash值保存在 Node 对象中，只会改变 Node 对象在数组中的索引
- 扩容时，现有元素要么不动，要么索引变为原来索引 + 原来数组长度
- 扩容时判断元素是否移动是通过 `(e.hash & oldCap) == 0`，下面举例说明

我们以 `map.put("a1","a");`为例， a1 计算后的hash值为3056，经过计算，初始化时 a1存在数组的0索引处

| 初始化数组长度为16， 16 - 1 = 15 对应的二进制 | 00000000 00001111 |
| :-------------------------------------------: | :---------------: |
|               3056对应的二进制                | 00001011 11110000 |
|                   3056 & 15                   | 00000000 00000000 |

扩容后，数组长度变为32，此时计算  `(e.hash & oldCap)`

| 数组初始化长度16 |   00000000 00010000   |
| :--------------: | :-------------------: |
|  3056 二进制值   |   00001011 11110000   |
|    3056 & 16     | 00000000 000**1**0000 |

可以 看到结果不为0， 说明 a1 需要迁移，我们来计算一下迁移后的新位置索引

| 扩容后数组长度为32， 32 - 1 = 31 二进制值 | 00000000 000**1**1111 |
| ----------------------------------------- | --------------------- |
| 3056 二进制值                             | 00001011 11110000     |
| 31 & 3056                                 | 00000000 000**1**0000 |

我们可以看到计算出的索引为16，正好等于`j + oldCap` `(0 + 16)`，因为扩容后，31 相对于 15 在高位多了一个 1， 此时只要通过 `(e.hash & oldCap)` 计算出该高位多出的 1 对索引的影响是0还是2的指数即可，是0的话索引不变，不为0则变成 `原索引+oldCap`，因此扩容时node 要么不变，要么就移动2的指数。

注意：

**JDK7中HashMap使用了链表的头插法，在多线程环境下容易出现死循环，JDK8以后才有尾插法，不存在这个问题。**具体可看这篇 [面试官： HashMap 为什么线程不安全？](https://mp.weixin.qq.com/s/ZyvrxC3gs92OEME3QzfF_Q)

### treeifyBin 方法

```java
/**
 * Replaces all linked nodes in bin at index for given hash unless
 * table is too small, in which case resizes instead.
 */
final void treeifyBin(Node<K,V>[] tab, int hash) {
  int n, index; Node<K,V> e;
  // 如果table为null或者长度小于64时不转换为红黑树，此时进行扩容
  if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
    resize();
  // 如果数组该索引的元素不为null
  else if ((e = tab[index = (n - 1) & hash]) != null) {
    TreeNode<K,V> hd = null, tl = null;
    // 循环遍历链表，将链表转为红黑树
    do {
      // 根据链表的 node 创建 TreeNode
      TreeNode<K,V> p = replacementTreeNode(e, null);
      if (tl == null)
        hd = p;
      else {
        p.prev = tl;
        tl.next = p;
      }
      tl = p;
    } while ((e = e.next) != null);
    if ((tab[index] = hd) != null)
      hd.treeify(tab);
  }
}
```

我们可以看到这里用到了 `MIN_TREEIFY_CAPACITY(64)` 参数，如果链表长度大于等于8，但是Node数组长度小于64，此时不会转为红黑树，而是进行 `resize()` 扩容，红黑树虽然查询时间复杂度为O(logN)，但是空间占用大HashMap的设计是尽量不用红黑树。

### get 方法

```java
public V get(Object key) {
  Node<K,V> e;
  return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
  Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
  // 判断table不为null，且table长度大于0，且对应索引的第一个node不为null
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

通过源码我们整理出 get 的步骤大致如下：

- 判断table不为null，且table长度大于0，且对应索引的第一个node不为null，如果不满足直接返回null
- 判断通过key计算出的hash与当前索引第一个节点的hash是否相同，并且key是否相同（满足equals方法或者引用相同），相同则返回当前第一个节点，不同继续下面的步骤。
- 若第一个节点first为红黑树，则用红黑树算法取对应value，若是链表，则循环链表，按照上面同样的方法，想比较hash是否相同，在比较key是否相同。

红黑树查询时间复杂度：O(logN)

链表查询时间复杂度：O(N)

## fail fast

> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw `ConcurrentModificationException` on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: *the fail-fast behavior of iterators should be used only to detect bugs.*



## Reference

1. [Java核心技术卷一](https://book.douban.com/subject/1781451/)
2. [Java 8系列之重新认识HashMap](https://tech.meituan.com/2016/06/24/java-hashmap.html)
3. [JDK11源码--HashMap源码分析](https://blog.csdn.net/liubenlong007/article/details/87937209)
4. [面试官： HashMap 为什么线程不安全？](https://mp.weixin.qq.com/s/ZyvrxC3gs92OEME3QzfF_Q)
