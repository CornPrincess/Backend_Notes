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

> This class provides a skeletal implementation of the [`List`](https://docs.oracle.com/javase/7/docs/api/java/util/List.html) interface to minimize the effort required to implement this interface backed by a "random access" data store (such as an array). For sequential access data (such as a linked list), [`AbstractSequentialList`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractSequentialList.html) should be used in preference to this class.
>
> To implement an unmodifiable list, the programmer needs only to extend this class and provide implementations for the [`get(int)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#get(int)) and [`size()`](https://docs.oracle.com/javase/7/docs/api/java/util/List.html#size()) methods.
>
> To implement a modifiable list, the programmer must additionally override the [`set(int, E)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#set(int, E)) method (which otherwise throws an `UnsupportedOperationException`). If the list is variable-size the programmer must additionally override the [`add(int, E)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#add(int, E)) and [`remove(int)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#remove(int)) methods.
>
> The programmer should generally provide a void (no argument) and collection constructor, as per the recommendation in the [`Collection`](https://docs.oracle.com/javase/7/docs/api/java/util/Collection.html) interface specification.
>
> Unlike the other abstract collection implementations, the programmer does *not* have to provide an iterator implementation; the iterator and list iterator are implemented by this class, on top of the "random access" methods: [`get(int)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#get(int)), [`set(int, E)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#set(int, E)), [`add(int, E)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#add(int, E)) and [`remove(int)`](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html#remove(int)).[4]

- 由上述引用可知，JDK 设计 `AbstractList`的目的是为了提供一个实现 List (底层为数组，支持 random access)的骨架，开发者可以继承 `AbstractList` 开发自己的 `List` 实现类。`ArrayList` 继承自  `AbstractList`，这样做的好处是可以减少重复代码， ArrayList 只需要关注自己独有的方法即可。
- `AbstractList` 实现了 `List` 接口，`ArrayList` 又实现了一遍 `List` 接口，是为了重写一些自己特有的方法。

```java
public interface RandomAccess {
}
```

```java
public interface Cloneable {
}
```

```java
public interface Serializable {
}
```

`RandomAccess`， `Cloneable` ，`Serializable` 三个都是标记接口，用来表式 `ArrayList` 支持 随机读取，克隆和序列化，反序列化。

## 参数

```java
private static final long serialVersionUID = 8683452581122892189L;

// 默认初始化 ArrayList capacity 10
private static final int DEFAULT_CAPACITY = 10;

// 给空实例的共享空数组
private static final Object[] EMPTY_ELEMENTDATA = {};

// 为默认size的空实例提供的空数组
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

/**
 * The array buffer into which the elements of the ArrayList are stored.
 * The capacity of the ArrayList is the length of this array buffer. Any
 * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
 * will be expanded to DEFAULT_CAPACITY when the first element is added.
 */
transient Object[] elementData; // non-private to simplify nested class access

// The size of the ArrayList (the number of elements it contains).
private int size;

/**
 * The maximum size of array to allocate (unless necessary).
 * Some VMs reserve some header words in an array.
 * Attempts to allocate larger arrays may result in
 * OutOfMemoryError: Requested array size exceeds VM limit
 */
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```

ArrayList 设置了默认的最大 `size` 为 `Integer.MAX_VALUE - 8`，因为存储了 Array 的头部信息，所以这里需要减去8，我们可以在 IBM 的技术博客上看到关于 Array 的头文件描述

> ## Anatomy of a Java array object
>
> The shape and structure of an array object, such as an array of `int` values, is similar to that of a standard Java object. The primary difference is that the array object has an additional piece of metadata that denotes the array's size. An array object's metadata, then, consists of:
>
> - **Class** : A pointer to the class information, which describes the object type. In the case of an array of `int` fields, this is a pointer to the `int[]` class.
> - **Flags** : A collection of flags that describe the state of the object, including the hash code for the object if it has one, and the shape of the object (that is, whether or not the object is an array).
> - **Lock** : The synchronization information for the object — that is, whether the object is currently synchronized.
> - **Size** : The size of the array.
>
> ![Array Object 32bit](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/ArrayObject.png)
>
> ![Array Object 64bit](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/ArrayObject2.png)
>
> Source from: [5]

`elementData` 使用 `transient `进行修饰意味着可以不用被序列化.

## 构造器

```java
public ArrayList() {
  this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
```

```java
public ArrayList(int initialCapacity) {
  if (initialCapacity > 0) {
    this.elementData = new Object[initialCapacity];
  } else if (initialCapacity == 0) {
    this.elementData = EMPTY_ELEMENTDATA;
  } else {
    throw new IllegalArgumentException("Illegal Capacity: "+
                                       initialCapacity);
  }
}
```



## add方法

```java
public boolean add(E e) {
  modCount++;
  add(e, elementData, size);
  return true;
}

/**
 * This helper method split out from add(E) to keep method
 * bytecode size under 35 (the -XX:MaxInlineSize default value),
 * which helps when add(E) is called in a C1-compiled loop.
 */
private void add(E e, Object[] elementData, int s) {
  if (s == elementData.length)
    elementData = grow();
  elementData[s] = e;
  size = s + 1;
}

public void add(int index, E element) {
    rangeCheckForAdd(index);
    modCount++;
    final int s;
    Object[] elementData;
    if ((s = size) == (elementData = this.elementData).length)
        elementData = grow();
    System.arraycopy(elementData, index,
                     elementData, index + 1,
                     s - index);
    elementData[index] = element;
    size = s + 1;
}

private Object[] grow() {
  return grow(size + 1);
}

private Object[] grow(int minCapacity) {
  return elementData = Arrays.copyOf(elementData,
                                     newCapacity(minCapacity));
}

/**
 * Returns a capacity at least as large as the given minimum capacity.
 * Returns the current capacity increased by 50% if that suffices.
 * Will not return a capacity greater than MAX_ARRAY_SIZE unless
 * the given minimum capacity is greater than MAX_ARRAY_SIZE.
 */
private int newCapacity(int minCapacity) {
  // overflow-conscious code
  int oldCapacity = elementData.length;
  int newCapacity = oldCapacity + (oldCapacity >> 1);
  if (newCapacity - minCapacity <= 0) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
      return Math.max(DEFAULT_CAPACITY, minCapacity);
    if (minCapacity < 0) // overflow
      throw new OutOfMemoryError();
    return minCapacity;
  }
  return (newCapacity - MAX_ARRAY_SIZE <= 0)
    ? newCapacity
    : hugeCapacity(minCapacity);
}

private static int hugeCapacity(int minCapacity) {
  if (minCapacity < 0) // overflow
    throw new OutOfMemoryError();
  return (minCapacity > MAX_ARRAY_SIZE)
    ? Integer.MAX_VALUE
    : MAX_ARRAY_SIZE;
}
```

### 扩容

elementData的扩容机制如下：

- 若（当前数组长度的1.5倍）<=（当前数组元素个数+1）

  - 若 ArrayList 对象是用无参构造器创建的，第一次调用 `add` 时计算出的 `newCapacity` 为 `DEFAULT_CAPACITY = 10`
  - 若 size  + 1 < 0 (overflow)，抛出`OutOfMemoryError`
  - 其他情况返回 size + 1

- 若（当前数组长度的1.5倍）>（当前数组元素个数+1）

  - 若 newCapacity <= MAX_ARRAY_SIZE， 返回 newCapacity（当前数组长度的1.5倍）
- 否则`return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;`

扩容使用的方法为：`Arrays.copyOf(T[] original, int newLength)` `System.arraycopy(Object src,  int  srcPos, Object dest, int destPos, int length)`

**扩容的时机**：当 `size == elementData.length`时，会进行扩容。

```java
public static <T> T[] copyOf(T[] original, int newLength) {
  return (T[]) copyOf(original, newLength, original.getClass());
}

@HotSpotIntrinsicCandidate
public static native void arraycopy(Object src,  int  srcPos,
                                    Object dest, int destPos,
                                    int length);

public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
    @SuppressWarnings("unchecked")
    T[] copy = ((Object)newType == (Object)Object[].class)
        ? (T[]) new Object[newLength]
        : (T[]) Array.newInstance(newType.getComponentType(), newLength);
    System.arraycopy(original, 0, copy, 0,
                     Math.min(original.length, newLength));
    return copy;
}
```

### 原理

我们可以看到，两个 add 方法都是调用 ` System.arraycopy` 方法，通过数组的拷贝来完成 add 操作，因此 ArrayList 增加元素速度较慢,并且每次 add 操作都会 `modCount++`

### 对应 JDK8 源码

```java
public boolean add(E e) {
  ensureCapacityInternal(size + 1);  // Increments modCount!!
  elementData[size++] = e;
  return true;
}

private void ensureCapacityInternal(int minCapacity) {
  ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
}

private void ensureExplicitCapacity(int minCapacity) {
  modCount++;

  // overflow-conscious code
  if (minCapacity - elementData.length > 0)
    grow(minCapacity);
}

private void grow(int minCapacity) {
  // overflow-conscious code
  int oldCapacity = elementData.length;
  int newCapacity = oldCapacity + (oldCapacity >> 1);
  if (newCapacity - minCapacity < 0)
    newCapacity = minCapacity;
  if (newCapacity - MAX_ARRAY_SIZE > 0)
    newCapacity = hugeCapacity(minCapacity);
  // minCapacity is usually close to size, so this is a win:
  elementData = Arrays.copyOf(elementData, newCapacity);
}

private static int hugeCapacity(int minCapacity) {
  if (minCapacity < 0) // overflow
    throw new OutOfMemoryError();
  return (minCapacity > MAX_ARRAY_SIZE) ?
    Integer.MAX_VALUE :
  MAX_ARRAY_SIZE;
}
```

我们观察源码可知，JDK8 中 add 方法大体上和 JDK11 中相同，扩容的原理一样，不过 JDK11 将 `add(E element)` 进行了拆分，这样可以使用内联方法，减少函数调用的成本，做了改进。

```java
public boolean add(E e) {
  modCount++;
  add(e, elementData, size);
  return true;
}

/**
 * This helper method split out from add(E) to keep method
 * bytecode size under 35 (the -XX:MaxInlineSize default value),
 * which helps when add(E) is called in a C1-compiled loop.
 */
private void add(E e, Object[] elementData, int s) {
  if (s == elementData.length)
    elementData = grow();
  elementData[s] = e;
  size = s + 1;
}
```

- [ ] todo 分析内联减少函数调用成本的原理

## remove方法

 ```java
public E remove(int index) {
    Objects.checkIndex(index, size);
    final Object[] es = elementData;

    @SuppressWarnings("unchecked") E oldValue = (E) es[index];
    fastRemove(es, index);

    return oldValue;
}

public boolean remove(Object o) {
    final Object[] es = elementData;
    final int size = this.size;
    int i = 0;
    found: {
        if (o == null) {
            for (; i < size; i++)
                if (es[i] == null)
                    break found;
        } else {
            for (; i < size; i++)
                if (o.equals(es[i]))
                    break found;
        }
        return false;
    }
    fastRemove(es, i);
    return true;
}

private void fastRemove(Object[] es, int i) {
    modCount++;
    final int newSize;
    if ((newSize = size - 1) > i)
        System.arraycopy(es, i + 1, es, i, newSize - i);
    es[size = newSize] = null;
}
 ```

### 原理

观察上述代码我们可以知道 remove 方法主要通过底层调用 `System.arraycopy(es, i + 1, es, i, newSize - i);` 通过数组的拷贝来完成，这也是为什么 ArrayList 删除元素较慢，并且每次remove 操作都会 `modCount++`



## set方法

```java
public E set(int index, E element) {
    Objects.checkIndex(index, size);
    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
}

public static int checkIndex(int index, int length) {
    return Preconditions.checkIndex(index, length, null);
}

public static <X extends RuntimeException> int checkIndex(int index, int length,
                   BiFunction<String, List<Integer>, X> oobef) {
    if (index < 0 || index >= length)
        throw outOfBoundsCheckIndex(oobef, index, length);
    return index;
}
```

 **Set 方法很简单，但是这里会有一个 bug 需要注意：**

```java
List<Integer> list = new ArrayList<>(11);
System.out.println(list.size()); // 0
list.set(0, 1); // throw java.lang.IndexOutOfBoundsException
```

通过 `new ArrayList<>(11);` `new ArrayList<>();` 方法创建的 **ArrayList 对象初始的 size 都为 0，set 中的 index 必须小于 size**， 因此 进行 set 操作是就会报 `java.lang.IndexOutOfBoundsException`



## get方法

```java
public E get(int index) {
    Objects.checkIndex(index, size);
    return elementData(index);
}
```



## 序列化

```java
transient Object[] elementData; // non-private to simplify nested class access
```

ArrayList 基于数组实现，具有动态扩容属性，因此 elementData 可能不会被完全使用，这种情况下就没必要全部进行序列化，所以会用 `transient` 进行修饰，我们可以看到` ArrayList` 实现了自己的` writeObject` `readObject` 方法，**来控制只序列化数组中 有元素填充的那部分内容**，并且 `writeObject` 过程中会检查是否有结构性修改，使用了 `fail-fas` t机制。

```java
private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
    // Write out element count, and any hidden stuff
    int expectedModCount = modCount;
    s.defaultWriteObject();

    // Write out size as capacity for behavioral compatibility with clone()
    s.writeInt(size);

    // Write out all elements in the proper order.
    for (int i=0; i<size; i++) {
        s.writeObject(elementData[i]);
    }

    if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
    }
}

private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {

    // Read in size, and any hidden stuff
    s.defaultReadObject();

    // Read in capacity
    s.readInt(); // ignored

    if (size > 0) {
        // like clone(), allocate array based upon size not capacity
        SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Object[].class, size);
        Object[] elements = new Object[size];

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++) {
            elements[i] = s.readObject();
        }

        elementData = elements;
    } else if (size == 0) {
        elementData = EMPTY_ELEMENTDATA;
    } else {
        throw new java.io.InvalidObjectException("Invalid size: " + size);
    }
}
```



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
4. [Class AbstractList](https://docs.oracle.com/javase/7/docs/api/java/util/AbstractList.html)
5. [From Java code to Java heap](https://www.ibm.com/developerworks/java/library/j-codetoheap/index.html)
