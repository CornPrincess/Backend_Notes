# Lambda表达式

Java 语言在 JDK8 中引入了 lambda 表达式，它是一个可传递的代码块，可以在以后执行一次或多次，我们来看 一个小例子。

```java
String[] strings = {"a", "A", "b", "B"};
Arrays.sort(strings);
System.out.println(Arrays.toString(strings));

// output
// [A, B, a, b]
```

`String` 类实现了 `Comparable` 接口，所以可以当我们使用 `Arrays.sort` 进行排序时会使用字典排序，若我们想要使用其他排序方法，此时可以使用` Comparator` 比较器。

```java
Arrays.sort(strings, new Comparator<String>() {
  @Override
  public int compare(String o1, String o2) {
    return o1.toLowerCase().compareTo(o2.toLowerCase());
  }
});
System.out.println(Arrays.toString(strings));

// output
// [A, a, B, b]
```

我们可以看到上述代码中将一个代码块传递到了 sort 方法中，并且这个代码块在未来某个时间会被调用。在之前，在 Java 中传递一个代码段并不容易，因为 Java 是一种面向对象的语言，所以必须构造一个对象，这个对象的类需要有一个方法能保护所需的代码。

## lambda 表达式的语法

但 Java 引入 lambda 表达式之后，我们可以将代码改写成如下：

```java
Arrays.sort(strings, (String o1, String o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));
System.out.println(Arrays.toString(strings));
```



如果无法用一个表达式写完，可以用 `{}`

```java
(String first, String second) -> {
  if (first.length < second.length) {
    return -1;
  } else if (first.length < second.length) {
    return 1;
  } else {
    return 0;
  }
}
```

如果没有参数，也需要写 `()`

```java
() -> Systen.out.println("hello");
```

如果可以推到出参数类型，则可以忽略其类型，并且无需指定 lambda 的返回类型，可以通过上下文推到得出。

```java
(o1, o2) -> o1.length - o2.length;
```



## 函数式接口

对于只有一个抽象方法的接口，需要这种接口时，就可以提供一个 lambda 表达式，这种接口称为函数式接口（functional interface），如上例中的 Comparator(重新声明了 Object 中的 equals 方法， 该方法不是抽象的，因此只有一个 compare 抽象方法)， **Comparator 是一个函数式接口，当我们提供一个 lambda 表达式时，在底层， Arrays.sort 方法会接收实现了 `Comparator<String>` 的某个类的对象，在这个对象上调用 compare 方法会执行这个 lambda 表达式的代码，这些对象的管理完全取决于具体实现，与使用传统的内联类相比，可能高效许多 。**



想要用 lambda 表达式做某些处理，还是要谨记表达式的用途，为它建立一个特定的函数式接口。`java.util.function` 包中有一个尤其有用的接口 `Predicate`

```java
public interface Predicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    boolean test(T t);
  
  // other code ...
}
```

`Collection` 类有一个方法 `removeIf`， 参数就是 `Predicate`

```java
list.removeIf((e) -> e == null); // 删除list中的 null 元素
```



## 方法引用

有时可能已经有现成的方法可以完成想要传递到其他代码中的动作，此时可以使用方法引用（method reference），如下例

```java
x -> System.out.println(x);
// method reference
System.out::println
```

我们可以利用方法引用改进上面 lambda 表达式的代码

```
list.removeIf((e) -> e == null);
// reference method
list.removeIf(Objects::isNull);

Arrays.sort(strings, (String o1, String o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));
// method reference
Arrays.sort(strings, Comparator.comparing(String::toLowerCase));
```



我们可以看到方法引用的语法主要有以下三种：

- object::instanceMethod
- Class::staticMethod
- Class::instanceMethod

前两种情况等价于提供方法参数的 lambda 表达式：

```java
System.out.println(x) 
x -> System.out::println

(x, y) -> Math.pow(x, y)
Math::pow
```

第三种情况，第一个参数会成为方法的目标，如

```java
(x, y) -> x.compareToIgnoreCase(y);
String::compareToIgnoreCase
```



注意：

- 方法引用支持 this， 如 `this::equals` 等同于 `x -> this.equals(x)`
- 使用 super 也和合法 `super::instanceMethod`



## 变量作用域

通常情况下，我们可能希望能够在 lambda 表达式中访问外围方法或者类中的变量，观察一下代码：

```java
public static void main(String[] args) {
  String text = "hello";
  ActionListener listener = event -> {
    System.out.println(text);
  };
  new Timer(1000, listener).start();
}

// output
//hello
//...
```

观察上述代码以及输出结果可知：lambda 表达式可以访问其外围作用域的变量 `text`， 这里会存在一个问题，lambda 表达式的代码可能会在主方法调用返回很久之后才会运行，而那时这个参数已经不存在了，该如何保存这个参数呢？

lambda 表达式由三部分组成： 一个代码块，参数，自由变量的值。

在上述代码中，lambda 表达式有一个自由变量 text，表示lambda 表达式的数据结构必须存储自由变量的值，称为被 lambda 表达式捕获（captured）具体的实现细节为：**可以把 lambda 表达式转化为包含一个方法的对象，这样自由变量的值就会复制到这个对象的实例变量中。**



上述讨论的自由变量与代码块有一个术语L：闭包（closure）， lambda 表达式就是 Java中的闭包。**我们需要注意 lambda 只能引用值不会改变的变量，即该变量必须是实际上的最终变量（effectively final）：即这个自由变量在外围作用域不能被改变，在 lambda 表达式中也不能被改变，这主要是出于并发安全性考虑。**