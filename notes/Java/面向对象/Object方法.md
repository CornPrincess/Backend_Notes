# Object中的方法

Object 类是 Java 中所有类的超类，在 Java 中每个类都是由它扩展来的，只有基本类型（primitive types）不是对象，**剩下的引用类型都是对象，包括对象数组或者基本类型数组。**

```java
Object obj = new int[10] // ok
```

Object 方法概览：

```java
public final native Class<?> getClass();
public native int hashCode();
public boolean equals(Object obj) {}
protected native Object clone() throws CloneNotSupportedException;
public String toString() {}
public final native void notify();
public final native void notifyAll();
public final native void wait(long timeout) throws InterruptedException;
public final void wait(long timeout, int nanos) throws InterruptedException{}
public final void wait() throws InterruptedException {}
protected void finalize() throws Throwable {} // Deprecated
```

## equals 方法

```java
public boolean equals(Object obj) {
    return (this == obj);
}
```

阅读源码可知，**Object 中的 equals 方法是比较两个对象是否有相同的引用**，但是这在有些情况下是没有意义的，更多的时候是想比较两个对象的状态是否相同。

> The `equals` method implements an equivalence relation on non-null object references:
>
> - It is *reflexive*: for any non-null reference value `x`, `x.equals(x)` should return `true`.
> - It is *symmetric*: for any non-null reference values `x` and `y`, `x.equals(y)` should return `true` if and only if `y.equals(x)` returns `true`.
> - It is *transitive*: for any non-null reference values `x`, `y`, and `z`, if `x.equals(y)` returns `true` and `y.equals(z)` returns `true`, then `x.equals(z)` should return `true`.
> - It is *consistent*: for any non-null reference values `x` and `y`, multiple invocations of `x.equals(y)` consistently return `true` or consistently return `false`, provided no information used in `equals` comparisons on the objects is modified.
> - For any non-null reference value `x`, `x.equals(null)` should return `false`.<sup>[2]</sup>

由 JDK 文档中可知，实现了 non-null 对象引用的相等关系的 equals 方法有五个特性：*reflexive*（自反性），*symmetric*（对称性），*transitive*（传递性），*consistent*（一致性），与 null 相比返回 false

```java
public class Person {
    private String name;
    private int age;

    @Override
    public boolean equals(Object o) {
        // a quick test to see if the objects are identical
        if (this == o) return true;
        // if o == null or the classes don't match, they can't be equal
        if (o == null || getClass() != o.getClass()) return false;

        // now we know o is a non-null Person
        Person person = (Person) o;

        // test whether the fields have identical values
        if (age != person.age) return false;
        return Objects.equals(name, person.name);
    }
}
```

注意，比较可能为空的变量时可以使用` Objects.equals()`

子类的 equals 方法:

```java
public class Student extends Person {
    private String school;

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        Student student = (Student) o;

        return Objects.equals(school, student.school);
    }
}
```

我们在重写自己的 equals 方法时需要注意到**自反性**这个原则，即` x.equals(y) == y.equals(x)`，在前面的代码中我们用到了 `getClass` 这个方法，在比较过程中，当发现 x 与 y 类不相等则返回 false， 在这里我们必须将 `getClass` 方法和 `instanceof` 方法进行比较。

### instanceof

> The `instanceof` operator compares an object to a specified type. You can use it to test if an object is an instance of a class, an instance of a subclass, or an instance of a class that implements a particular interface.<sup>[3]</sup>

简单来说，`instanceof` 是用来将对象与类型进行比较，我们可以通过它确定这个对象是否是一个类，一个子类或者一个接口实现类的实例。

```java
public class InstanceofTest {
    public static void main(String[] args) {
        Father father = new Father();
        System.out.println(father instanceof Father); // true
        System.out.println(father instanceof Son); // false
        System.out.println(father instanceof MyInterface); // false
        System.out.println("------------------------");

        Son son = new Son();
        System.out.println(son instanceof Father); // true
        System.out.println(son instanceof Son); // true
        System.out.println(son instanceof MyInterface); // true
        System.out.println("------------------------");

        Father father2 = new Son();
        System.out.println(father2 instanceof Father); // true
        System.out.println(father2 instanceof Son); // true
        System.out.println(father2 instanceof MyInterface); // true
        System.out.println("------------------------");
    }
}
class Father {

}

class Son extends Father implements MyInterface{

}

interface MyInterface {

}
```

上述结果我们可以看出当父类变量被赋予子类对象引用时，其结果和子类变量是一样的。

### getClass

> ```java
> public final Class<?> getClass()
> ```
>
> Returns the runtime class of this `Object`. The returned `Class` object is the object that is locked by `static synchronized` methods of the represented class.

```java
public class InstanceofTest {
    public static void main(String[] args) {
        Father father = new Father();
        System.out.println(father.getClass()); // class Father
        Son son = new Son();
        father = new Son();
        System.out.println(son.getClass()); // class Son
        System.out.println(father.getClass()); // class Son
 
    }
}

class Father {

}

class Son extends Father implements MyInterface{

}

interface MyInterface {

}
```

观察上述代码我们可以发现，`getClass `所返回的值与对象变量此刻的对象引用有关。



### 相等测试与继承

我们来看，如果复习的 equals 方法中使用 instanceof 来进行相等判断，即使用以下代码片段

```java
class Father {
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o instanceof Father) return false;
        Father father = (Father) o;
        return Objects.equals(name, father.name);
    }
}

class Son extends Father implements MyInterface{
    private int age;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o instanceof Son) return false; // not good
        if (!super.equals(o)) return false;
        Son son = (Son) o;
        return age == son.age;
    }
}
```

当运行 `f.equals(s)` 判断时（f 为 Father 对象， s 为 Son 对象，假设两个对象中 name 一致），根据代码会返回 `true`， 但是当运行`s.equals(f)` 会引用 `instanceof` 判断失败而返回 `false`， 这就不符合自反性。



综上，我们可以得出结论

- 如果子类有自己相等的概念，如 Son 对象还需要比较彼此 age 是否相等，则对程序需求将强制采用 `getClass` 进行相等性检测。
- 如果有超类绝对相等的概念，那么可以用 `instanceof` 进行检测，只需要超类提供 `equals` 方法，并且要标为 `final`， 子类全部继承超类的 `equals` 方法，这样不同子类之间也可以进行比较。



复写 equals 方法的步骤：

- 现实参数命名为 otherObject
- 检测 this 和otherObject 是否引用同一个对象： `if (this == otherObject) return true;`
- 检测 otherObject 是否为 null： `if (otherObject == null) return false;`
- 比较 otherObject 与 this 是否时同一类，如果 equals 语义在每个子类中有所改变，就使用 getClass 进行检测: `if (getClass() != otherObject.getClass()) return false;`如果所有的子类都有统一的语义，就使用 instanceof 检测： `if (!(otherObject instanceof ClassName)) return false;`
- 将 otherObject 转化为相应的类型变量：`ClassName other = (ClassName) otherObject`
- 进行域的比较，如果是基本类型使用 `==`， 如果是引用类型可以使用 `Objects.equals()`, 如果是 数组类型的域， 可以使用 `Arrays.equals()`进行比较。



## hashCode 方法

散列码（hash code）是由对象导出的一个整数值，散列码是没有规律的，不同的对象其散列码一般不同， 

> The general contract of `hashCode` is:
>
> - Whenever it is invoked on the same object more than once during an execution of a Java application, the `hashCode` method must consistently return the same integer, provided no information used in `equals` comparisons on the object is modified. This integer need not remain consistent from one execution of an application to another execution of the same application.
> - If two objects are equal according to the `equals(Object)` method, then calling the `hashCode` method on each of the two objects must produce the same integer result.
> - It is *not* required that if two objects are unequal according to the [`equals(java.lang.Object)`](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#equals(java.lang.Object)) method, then calling the `hashCode` method on each of the two objects must produce distinct integer results. However, the programmer should be aware that producing distinct integer results for unequal objects may improve the performance of hash tables.
>
> As much as is reasonably practical, the hashCode method defined by class `Object` does return distinct integers for distinct objects. (This is typically implemented by converting the internal address of the object into an integer, but this implementation technique is not required by the JavaTM programming language.)<sup>[2]</sup>
>
> 翻译：
>
> - 在一个 Java 程序的执行中，无论何时，hashcode 返回的值都应该是同一个
> - 如果 `a.equals(b)` 为 true， 那么一定有 `a.hashcode() == b.hashcode()`
> - 如果 `a.equals(b)` 为 false，那么 `a.hashcode()` 与`b.hashcode()`不一定不同

```java
public native int hashCode();
```

以上为 Object 中的 hashcode方法，我们可以看到这是用 `native` 修饰的方法，表明该方法是使用了 JNI（Java Native Interface），使用了 C 或 C++ 进行实现。在实际情况下，大部分不同对象会 生成不同的 hashcode（通过将内存地址转换为整数）

**The main objectives of native keyword are:**<sup>[5]</sup>

- To improve performance of the system.
- To achieve machine level/memory level communication.
- To use already existing legacy non-java code.

测试代码：

```java
ublic class HashCode {

    public static void main(String[] args) {
        Map<Student, String> map = new HashMap<>();
        Student s = new Student("Alice", 11);

        map.put(s, "Alice");
        map.put(new Student("Bob", 12), "Bob");
        map.put(new Student("Cici", 15), "Cici");

        if (map.containsKey(s)) {
            System.out.println("bingo"); // bingo
        }

        if (map.containsKey(new Student("Bob", 12))) {
            System.out.println("hello"); // hello
        }
    }

}

class Student {
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return age == student.age &&
                Objects.equals(name, student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

上述代码测试了，必须重写 equals 和 hashcode 方法，才能正常作为 map 的 key，如果有其中一个方法没有重写，都会造成 "hello" 不打印。



## clone 方法

```java
protected native Object clone() throws CloneNotSupportedException;
```

观察源码可知，clone 方法是 protected，子类只能调用受保护的  clone 方法来克隆它自己的对象，即只有在 Person 类中才可以克隆 Person 对象

```java
public class CloneTest {
    public static void main(String[] args) {
        Person p = new Person();
        Person p2 = p.clone(); // Error clone() has protected access in Object
    }
}

class Person {
    private String name;
    private int age;
}
```

我们可以重写 Person 中的 clone()方法为 public 即可调用

```java
public class CloneTest {
    public static void main(String[] args) {
        Person p = new Person();
        try {
            Person p2 = p.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

class Person {
    private String name;
    private int age;

    @Override
    protected Person clone() throws CloneNotSupportedException {
        return (Person) super.clone();
    }
}
```

但是当我们执行代码时，还是会抛出运行时异常：`java.lang.CloneNotSupportedException`，这是因为我们没有实现 `Cloneable` 接口,这个接口是一个标记接口（marker interface），只是作者了解克隆过程。



### 浅拷贝

使用默认的方法得到的拷贝为浅拷贝，即拷贝的对象与原对象中的引用变量指向的是同一个

```java
public class CloneTest {
    public static void main(String[] args) {
        Person p = new Person();
        try {
            Person p2 = p.clone();
            System.out.println(p.get(0));
            System.out.println(p2.get(0));
            p2.set(0, 8);
            System.out.println(p.get(0));
            System.out.println(p2.get(0));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

class Person implements Cloneable{
    private String name;
    private int[] nums;

    public Person() {
        this.nums = new int[]{1, 2, 3};
    }

    public void set(int index, int value) {
        this.nums[index] = value;
    }

    public int get(int index) {
        return this.nums[index];
    }

    @Override
    protected Person clone() throws CloneNotSupportedException {
        return (Person) super.clone();
    }
}

// output
// 1
// 1
// 8
// 8
```



### 深拷贝

为来解决上述问题，我们可以使用深拷贝

```java
public class CloneTest {
    public static void main(String[] args) {
        Person p = new Person();
        try {
            Person p2 = p.clone();
            System.out.println(p.get(0));
            System.out.println(p2.get(0));
            p2.set(0, 8);
            System.out.println(p.get(0));
            System.out.println(p2.get(0));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

class Person implements Cloneable{
    private String name;
    private int[] nums;

    public Person() {
        this.nums = new int[]{1, 2, 3};
    }

    public void set(int index, int value) {
        this.nums[index] = value;
    }

    public int get(int index) {
        return this.nums[index];
    }

    @Override
    protected Person clone() throws CloneNotSupportedException {
        Person p = (Person) super.clone();
        p.nums = p.nums.clone(); // array clone
        return p;
    }
}

// output
// 1
// 1
// 1
// 8
```

注意，数组的拷贝可以使用数组自带的 clone() 方法，该方法为 public，所以可以直接使用。若有其他引用类型变量需要拷贝，可以使用该类型的 clone 方法。



## clone() 替代方案

clone 使用频率一般较低，我们可以使用拷贝构造函数或者拷贝工程来拷贝一个对象：

```java
public class CloneTest {
    public static void main(String[] args) {
        Apple apple = new Apple();
        Apple apple2 = new Apple(apple);
        System.out.println(apple.get(0));
        System.out.println(apple2.get(0));
        apple2.set(0, 8);
        System.out.println(apple.get(0));
        System.out.println(apple2.get(0));
    }
}
class Apple {
    private int[] nums;
    private String name;

    public Apple() {
        this.nums = new int[]{1, 2, 3};
    }

    public Apple(Apple apple) {
        nums = apple.nums.clone();
        name = apple.name;
    }

    public void set(int index, int value) {
        this.nums[index] = value;
    }

    public int get(int index) {
        return this.nums[index];
    }
}

// output
// 1
// 1
// 1
// 8
```



## Referencee

1. Java核心技术卷一
2. [Class Object](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/lang/Object.html#equals(java.lang.Object))

3. [Equality, Relational, and Conditional Operators](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/op2.html)

4. [native keyword in java](https://www.geeksforgeeks.org/native-keyword-java/)
