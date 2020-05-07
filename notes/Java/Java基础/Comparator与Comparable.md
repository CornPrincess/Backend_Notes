# Comparable与Comparator接口

我们在使用 `Arrays.sort`， `Collections.sort` 时会发现，调用该方法的类必须实现 `Comparable`接口，才能保证方法的正确调用，我们先来看`Comparable` 接口。

## Comparable

```java
public interface Comparable<T> {
	public int compareTo(T o);
}
```

> This interface imposes a total ordering on the objects of each class that implements it. This ordering is referred to as the class's *natural ordering*, and the class's `compareTo` method is referred to as its *natural comparison method*.
>
> Lists (and arrays) of objects that implement this interface can be sorted automatically by [`Collections.sort`](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/Collections.html#sort(java.util.List)) (and [`Arrays.sort`](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/Arrays.html#sort(java.lang.Object[]))). Objects that implement this interface can be used as keys in a [sorted map](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/SortedMap.html) or as elements in a [sorted set](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/SortedSet.html), without the need to specify a [comparator](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/Comparator.html).
>
> This interface imposes a total ordering on the objects of each class that implements it. This ordering is referred to as the class's *natural ordering*, and the class's `compareTo` method is referred to as its *natural comparison method*.
>
> Lists (and arrays) of objects that implement this interface can be sorted automatically by [`Collections.sort`](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/Collections.html#sort(java.util.List)) (and [`Arrays.sort`](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/Arrays.html#sort(java.lang.Object[]))). Objects that implement this interface can be used as keys in a [sorted map](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/SortedMap.html) or as elements in a [sorted set](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/SortedSet.html), without the need to specify a [comparator](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/util/Comparator.html).

实现了 `Comparable` 接口的类可以进行排序，这种排序称为类的 `natural ordering`， 该类的 Lists 或 arrays 可以使用`Collections.sort()`或 `Arrays.sort()` 方法进行排序，并且该对象可以作为 sorted map 的键 或 sorted set的元素（不用显示声明 comparator）

代码示例：

```java
public class Student implements Comparable<Student>{
    private String name;
    private double salary;

    public Student(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                '}';
    }

    @Override
    public int compareTo(Student other) {
        return Double.compare(salary, other.salary);
    }
}

public class StudentSortTest {
    public static void main(String[] args) {
        Student[] students = new Student[3];

        students[0] = new Student("Alice", 2000);
        students[1] = new Student("Bob", 500);
        students[2] = new Student("Cici", 400);

        Arrays.sort(students);

        for (Student student : students) {
            System.out.println(student);
        }
    }
}

// output
// Student{name='Cici', salary=400.0}
// Student{name='Bob', salary=500.0}
// Student{name='Alice', salary=2000.0}
```

注意：
`compareTo` 方法在实现是有可能使用两个整数相减来进行比较（o1 - o2)， 当所得结果范围过大时可能会造成溢出，因此推荐使用 `Integer.compare()` 方法， **当比较两个浮点数时，因为存在舍入误差，推荐使用 `Double.compare()`进行比较。**



在这里我们思考一个问题，我们为什么不能直接在 `Student` 类中实现一个 `compareTo`方法来实现排序，而是通过实现接口的形式来完成呢？主要原因是 Java 是强类型(Strongly typed)语言，在调用方法时，编译器会检查这个方法是否存在。



## Comparator

Comparator 接口也是用来进行排序的，但是当我们的类本身没有实现 Comparable 接口，并且是不能修改（用final修饰， 此时不能通过创建其子类并实现 comparable），又或者该类本身已实现了 Comparable，但想要自定义排序，此时就可以使用 Comparator接口。



```java
public final class Student2 {
    private String name;
    private int age;

    public Student2(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Student2{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

public class StudentSortTest2 {
    public static void main(String[] args) {
        List<Student2> list = new ArrayList<>();

        list.add(new Student2("Alice", 18));
        list.add(new Student2("Bob", 14));
        list.add(new Student2("Cici", 12));

        list.sort(new Comparator<Student2>() {
            @Override
            public int compare(Student2 o1, Student2 o2) {
                return o1.getAge() - o2.getAge();
            }
        });
        
        // lambda
		// list.sort((o1, o2) -> o1.getAge() - o2.getAge());
        
        // method reference:comparingInt
        // list.sort(Comparator.comparingInt(Student2::getAge));

        for (Student2 student2 : list) {
            System.out.println(student2);
        }
    }
}

// output
// Student2{name='Cici', age=12}
// Student2{name='Bob', age=14}
// Student2{name='Alice', age=18}
```

> 在《Effective Java》一书中，作者Joshua Bloch推荐大家在编写自定义类的时候尽可能的考虑实现一下Comparable接口，一旦实现了 `Comparable` 接口，它就可以跟许多泛型算法以及依赖于该接口的集合实现进行协作。你付出很小的努力就可以获得非常强大的功能。
>
> 事实上，Java平台类库中的所有值类都实现了`Comparable` 接口。如果你正在编写一个值类，它具有非常明显的内在排序关系，比如按字母顺序、按数值顺序或者按年代顺序，那你就应该坚决考虑实现这个接口。
>
> `compareTo` 方法不但允许进行简单的等同性进行比较，而且语序执行顺序比较，除此之外，它与Object的equals方法具有相似的特征，它还是一个泛型。类实现了 `Comparable` 接口，就表明它的实例具有内在的排序关系，为实现Comparable接口的对象数组进行排序就这么简单： Arrays.sort(a);
>
> 对存储在集合中的Comparable对象进行搜索、计算极限值以及自动维护也同样简单。<sup>[2]</sup>



## 总结

`Comparable` 是一个排序接口，值类可以实现其接口来支持排序，相当于内部比较器，与类耦合。

`Comparator` 是一个比较器接口，可以被各种需要比较功能的类使用，相当与外部比较器，与类解耦。



## Reference

1. Java核心技术卷一
2. [Comparable与Comparator浅析](https://blog.csdn.net/u013256816/article/details/50899416)

