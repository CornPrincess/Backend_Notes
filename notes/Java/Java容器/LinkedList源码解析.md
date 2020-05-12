# LinkedList 源码解析(JDK11)

## 概述

- `LinkedList` 底层由双向链表实现，插入与删除操作效率高，但是查询效率低。
- `LinkedList` 支持存错多个相同的元素（包括null），且有序。
- `LinkedList` 不是线程安全的，存在线程安全问题。
- `LinkedList` 不需要扩容

## 类名

```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
    
public abstract class AbstractSequentialList<E> extends AbstractList<E>
    
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>
    
public interface Deque<E> extends Queue<E> 
```

- 从类名上可以看出 `LinkedList` 对比 `ArrayList` 少实现了 `RandomAccess`接口，因为 `LinkedList` 是由双向链表实现的 ，不支持高效的随机访问。

- `LinkedList` 实现了 `List`， `Deque`接口，而 `Deque` 是`Queue`的子接口，因此 `LinkedList` 可以作为队列的实现类。

- `LinkedList` 支持 clone 和序列化。



## 成员变量

```java
transient int size = 0;
transient Node<E> first;
transient Node<E> last;
```



## 构造方法

```java
public LinkedList() {
}

public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);
}

private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;

    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}

public boolean addAll(Collection<? extends E> c) {
    return addAll(size, c);
}
```

从构造方法中我们可以看出 LinkedList 底层是双向链表，默认初始化 first 和 last 为 null

## add方法

```
public boolean add(E e) {
    linkLast(e);
    return true;
}

public void add(int index, E element) {
    checkPositionIndex(index);

    if (index == size)
    linkLast(element);
    else
    linkBefore(element, node(index));
}

public void addFirst(E e) {
	linkFirst(e);
}

public void addLast(E e) {
	linkLast(e);
}

private void linkFirst(E e) {
    final Node<E> f = first;
    final Node<E> newNode = new Node<>(null, e, f);
    first = newNode;
    if (f == null)
    last = newNode;
    else
    f.prev = newNode;
    size++;
    modCount++;
}

void linkLast(E e) {
    final Node<E> l = last;
    final Node<E> newNode = new Node<>(l, e, null);
    last = newNode;
    if (l == null)
    first = newNode;
    else
    l.next = newNode;
    size++;
    modCount++;
}
```

`add(E e)` 方法将元素插在链表的末尾，并更新 `last ` 节点，并且 LinkedList 比ArrayList 方法多了` addFirst` `addLast` 方法



## get方法

```java
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}

Node<E> node(int index) {
    // assert isElementIndex(index);

    if (index < (size >> 1)) {
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;
        return x;
    } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}
```

get方法必须遍历链表才可以返回指定索引的值，但是这里所了个优化：**若索引大于 size/2 ，则直接从列表尾部开始搜索元素。**



## 同步问题

> **Note that this implementation is not synchronized.** If multiple threads access a linked list concurrently, and at least one of the threads modifies the list structurally, it *must* be synchronized externally. (A structural modification is any operation that adds or deletes one or more elements; merely setting the value of an element is not a structural modification.) This is typically accomplished by synchronizing on some object that naturally encapsulates the list. If no such object exists, the list should be "wrapped" using the [`Collections.synchronizedList`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Collections.html#synchronizedList(java.util.List)) method. This is best done at creation time, to prevent accidental unsynchronized access to the list:
>
> ```java
>    List list = Collections.synchronizedList(new LinkedList(...));
> ```

LinkedList 是非同步的，可以使用 `Collections.synchronizedList` 来获得同步对象。



## fail fast

> The iterators returned by this class's `iterator` and `listIterator` methods are *fail-fast*: if the list is structurally modified at any time after the iterator is created, in any way except through the Iterator's own `remove` or `add` methods, the iterator will throw a [`ConcurrentModificationException`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/ConcurrentModificationException.html). Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.

LinkedList 中的iterator 和listIterator 支持 fail-fast机制，在创建了迭代器对象后，不能再对 LinkedList 做出结构性改变。

若一个迭代器正在进行遍历，另一个迭代器却进行add, remove, resize 等操作，一定会造成异常，如

```java
LinkedList<Integer> k = new LinkedList<>();
k.add(1);
k.add(2);
k.add(3);
Iterator<Integer> i1 = k.iterator();
Iterator<Integer> i2 = k.iterator();
i1.next();
i1.remove();
i2.next(); // throw java.util.ConcurrentModificationException
```

为避免发送并发修改的异常，可以遵循一下原则：**可以根据需要给容器附加许多迭代器，但是这些迭代器只能读取列表。另外单独附加一个技能读又能写的迭代器。**



## Reference

1. [Java核心技术卷一](https://book.douban.com/subject/1781451/)
2. [Class LinkedList](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/LinkedList.html)
