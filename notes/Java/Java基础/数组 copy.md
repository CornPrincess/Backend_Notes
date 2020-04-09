数组

## 概述

- 数组是一种数据结构

- Java中要求数组中的数据是相同的类型
- 数组的长度在初始化时就定好，以后不可变
- 创建数字数组时，所有元素都初始化为0，boolean数组元素初始化为false，对象数组初始化为null。
- 在Java中，允许数组长度为0，如 `new elementType[0]`, 在编写一个结果为数组的方法时，如果结果为空，这种语法形式就很有用。



## 数组的拷贝

在Java中允许将一个数组变量拷贝给另一个数组变量，此时两个变量将引用同一个数组，且各自对数组的操作会影响到另一方。

```java
int[] a = {1, 2, 3};
int[] b = a;
b[1] = 8 // now a[1] is also 8
```

- [ ] 补充图片

### Arrays.copyOf()

如果希望将一个数组的值拷贝到一个新数组中可以用 `Arrays.copyOf()` 方法

```java
int[] a = {1, 2, 3};
int[] b = Arrays.copyOf(a, a.length);
```

通过查看源码可知，Arrays.copyOf() 底层调用的是 `System.arraycopy`，返回的是一个新的数组。

```java
// JDK 11
public static <T> T[] copyOf(T[] original, int newLength) {
	return (T[]) copyOf(original, newLength, original.getClass());
}
    
public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType {
    @SuppressWarnings("unchecked")
    T[] copy = ((Object)newType == (Object)Object[].class)
    ? (T[]) new Object[newLength]
    : (T[]) Array.newInstance(newType.getComponentType(), newLength);
    System.arraycopy(original, 0, copy, 0,
    Math.min(original.length, newLength));
    return copy;
}
```

## foreach

foreach 是 JDK5 引入的新特性，用来简化遍历数组的操作，语法如下

> for (variable : collection) statement

其中有几个需要注意的点：

### collection

对于要进行迭代的 collection 对象，必须满足 collection 是一个数组或者是实现了 `Iterable`  接口的对象。

### 不能在 foreach 中修改集合中的元素

代码示例：

```java
int[] test = {1, 2, 3, 4, 5};
for (int i : test) {
    i++;
    System.out.println(i); // 2, 3, 4, 5, 6
}
System.out.println(Arrays.toString(test)); //[1, 2, 3, 4, 5]
```

反编译后的代码：

```java
int[] var1 = new int[]{1, 2, 3, 4, 5};
int[] var2 = var1;
int var3 = var1.length;

for(int var4 = 0; var4 < var3; ++var4) {
    int var5 = var2[var4];
    ++var5;
    System.out.println(var5);
}

System.out.println(Arrays.toString(var1));
```

通过观察反编译的代码可知， `foreach` 的内部逻辑是先通过 `[]` 方法把数组中的值取出，然后再将其拷贝给一个临时遍历，**我们在 `foreach` 中所有的操作都是对这个临时变量操作，不会对原来的数组造成影响，所以不能在 foreach 中对集合中的元素进行操作。**



## 多维数组

Java实际没有多维数组，只有一维数组，多维数组被解释为“数组的数组”。

- [ ] 补图 corejava

多维数组可以用 `Arrays.deepToString()`  来进行打印

```java
int[][] demo = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
System.out.println(Arrays.deepToString(demo)); // [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
```

动态初始化不规则数组

````java
int[][] arr = new int[3][];
int[0] = new int[2];
int[0] = new int[1];
int[0] = new int[3];
````

