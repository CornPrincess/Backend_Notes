# Vector 源码解析(JDK11)

## 概览

- Vector 底层有数组实现，支持动态扩容
- Vector 是线程安全的，是由 `synchronized`实现的， 效率较低
- Vector 扩容时数组长度会变成原来两倍或者原长度加上capacityIncrement， ArrayList 扩大为原来1.5倍。
- Vector 支出存储多个相同的元素（包括多个null）

## 类名

```java
public class Vector<E>
    extends AbstractList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```



## 参数

```java
// 储存元素的数组，初始长度为10
protected Object[] elementData;

// Vector 中元素的个数，类似于ArrayList中的 size
protected int elementCount;

// 数组扩容参数，初始为0
protected int capacityIncrement;
private static final long serialVersionUID = -2767605614048989439L;
private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```



## 构造方法

```java
public Vector() {
    this(10);
}

public Vector(int initialCapacity) {
    this(initialCapacity, 0);
}

public Vector(int initialCapacity, int capacityIncrement) {
    super();
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    this.elementData = new Object[initialCapacity];
    this.capacityIncrement = capacityIncrement;
}

public Vector(Collection<? extends E> c) {
    elementData = c.toArray();
    elementCount = elementData.length;
    // defend against c.toArray (incorrectly) not returning Object[]
    // (see e.g. https://bugs.openjdk.java.net/browse/JDK-6260652)
    if (elementData.getClass() != Object[].class)
        elementData = Arrays.copyOf(elementData, elementCount, Object[].class);
}
```

从以上代码中可以看到，初始化一个 Vector 对象时，若不指定 initialCapacity，默认初始化长度为10的数组，若不指定 capacityIncreament，默认为0



## add方法

```java
public synchronized boolean add(E e) {
    modCount++;
    add(e, elementData, elementCount);
    return true;
}

private void add(E e, Object[] elementData, int s) {
    if (s == elementData.length)
        elementData = grow();
    elementData[s] = e;
    elementCount = s + 1;
}

public void add(int index, E element) {
    insertElementAt(element, index);
}

public synchronized void insertElementAt(E obj, int index) {
    if (index > elementCount) {
        throw new ArrayIndexOutOfBoundsException(index
                                                 + " > " + elementCount);
    }
    modCount++;
    final int s = elementCount;
    Object[] elementData = this.elementData;
    if (s == elementData.length)
        elementData = grow();
    System.arraycopy(elementData, index,
                     elementData, index + 1,
                     s - index);
    elementData[index] = obj;
    elementCount = s + 1;
}
```

从源码中可以得知 `add(E e)`方法默认将数据插入到数组当前元素的末尾，`add(int index, E element)`使用` System.arraycopy` 实现在数组之间插入元素的操作。这些方法都使用了 `synchronized` 关键字实现了多线程同步。

### 扩容

```java
private Object[] grow() {
    return grow(elementCount + 1);
}

private Object[] grow(int minCapacity) {
    return elementData = Arrays.copyOf(elementData,
                                       newCapacity(minCapacity));
}

private int newCapacity(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                     capacityIncrement : oldCapacity);
    if (newCapacity - minCapacity <= 0) {
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
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
    MAX_ARRAY_SIZE;
}
```

**发生扩容的条件**：`s == elementData.length` 即 `elementCount == elementData.length` 数组中元素的个数等于数组长度。

**基本扩容规则**：若创建 Vector 对象是指定 capacityIncrement(>0)，则数组长度变为原长度加上`capacityIncrement`，若没有指定则数组长度变为原来的两倍。

## fail fast

> The iterators returned by this class's [`iterator`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Vector.html#iterator()) and [`listIterator`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Vector.html#listIterator(int)) methods are *fail-fast*: if the vector is structurally modified at any time after the iterator is created, in any way except through the iterator's own [`remove`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ListIterator.html#remove()) or [`add`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ListIterator.html#add(E)) methods, the iterator will throw a [`ConcurrentModificationException`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ConcurrentModificationException.html).Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future. The [`Enumerations`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Enumeration.html) returned by the [`elements`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Vector.html#elements()) method are *not* fail-fast; if the Vector is structurally modified at any time after the enumeration is created then the results of enumerating are undefined.[2]

从文档中我们可以看出 Vector 中的 `iterator` 和`listIterator`是符合 fail fast机制的，但是由` elements()` 方法返回的 `Enumerations` 不支持 fail fast。

## Reference

1. [Java核心技术卷一](https://book.douban.com/subject/1781451/)
2. [Class Vector](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Vector.html)

