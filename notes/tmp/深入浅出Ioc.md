# 深入浅出Ioc

对于每一个 Java 程序员，Ioc 和 AOP 两个词一定不陌生，这也是面试时的送分题，今天就来对 Ioc 进行一次深入浅出的总结。

## 历史

## Ioc 的实现方式

### 引入

> Ioc: Inversion of Control 中文名即控制反转，这是一种设计思想，有点类似于设计模式，并不是一门具体的技术。其核心为将你设计好的对象由容器控制，而不是传统的在你的对象内部直接控制。
>
> DI: Dependency Injecetion 中文名为依赖注入，即组件之间依赖关系由容器在运行期决定，形象地说，即由容器动态地将某个依赖关系注入到组件中。

对于以上两个概念，他们的关系可以这样理解：DI 是用来实现 Ioc 的一种方式，Ioc 还可以有其他的方法来实现，如依赖遍历。

以前对于这两个概念一直很模糊，但是工作一段时间后，慢慢对这两个概念有了一定的理解，前辈们的建议没有错：**对于你不熟悉的技术 ，先用它，积累了一定的时间，你就会对其有所理解，不能一开始就钻牛角尖。**

<hr>

回到正文，首先我们要了解一个前提，即Ioc主要解决的是各个类之间依赖的关系，目前有两种方法解决，**即 xml 和注解，而这两种的方式都是将 bean 的 id 和 类名进行映射，当需要用到这个 bean 时查找这个 map，然后再用反射机制生成 bean**。Ioc 一共有以下两类，共四种实现方式。

### 依赖查找 lookup

#### 依赖拉取 dependency pull

1.定义基本的类

```java
@Getter
@Setter
@ToString
public class User {
    private String name;

    public void hello() {
        System.out.println(this.name + " hello word");
    }
}
```

注意这里都 bean 一定要有无参构造方法

2. 配置 ApplicationContextXml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="user" class="love.minmin.model.User"></bean>
</beans>

```

3. 依赖拉取

```java
public void test() {
  ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ApplicationContext.xml");
  User user = (User) applicationContext.getBean("user");
  user.setName("minmin");
  user.hello();
  System.out.println(user);
}
```

#### 上下文查找Contextuallized Dependency Lookup

上下文查找是基于依赖拉取实现，基本原理还是基于 xml 配置每一个 bean，但是不同的地方在于上下文查找会基于多个 bean，**而且多个bean之间还存在依赖关系。**

### 依赖注入 DI

#### 构造器注入 constructor

1. 构造bean

```java
public class Read {
    private Book book;
    private String name;

    public Read(Book book, String name) {
        this.book = book;
        this.name = name;
    }

    public void getBookMessage() {
        String bookMessage = this.book.getBookMessage();
        // work code
        System.out.println("message: " + this.name + bookMessage);
    }
}
```

2. 配置xml 构造器注入

```xml
<bean id="bookdi" class="love.minmin.model.di.Book"></bean>
<bean id="readdi" class="love.minmin.model.di.Read">
  <constructor-arg ref="bookdi"></constructor-arg>
  <constructor-arg value="JK"></constructor-arg>
</bean>
```

3. 测试

```java
public static void main(String[] args) {
  ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ApplicationContext.xml");
  Read read = (Read) applicationContext.getBean("readdi");
  read.getBookMessage();
}
```

#### setter注入 setter

Setter 注入和 构造器注入类似，不同的是在配置 xml 时将配置构造器改为配置 setter

1. 生成 bean

```java
@Setter
@Getter
public class Read {
    private Book book;
    private String name;

    public void getBookMessage() {
        String bookMessage = this.book.getBookMessage();
        // work code
        System.out.println("message: " + this.name + bookMessage);
    }
}
```

2. 配置 xml

```xml
<bean id="bookdi2" class="love.minmin.model.di.setter.Book"></bean>
<bean id="readdi2" class="love.minmin.model.di.setter.Read">
  <property name="book" ref="bookdi2"></property>
  <property name="name" value="JK"></property>
</bean>
```

3. 测试

```java
public class TestRead {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        Read read = (Read) applicationContext.getBean("readdi2");
        read.getBookMessage();
    }
}
```

### 依赖查找 vs 依赖注入

对于这两种方法，我们优先选择依赖注入，其中主要有两个原因

- 依赖注入对代码组件的影响为0，依赖查找必须要手动去获取对象的引用
- 依赖注入写更少的代码，结构更清晰，依赖查找这种解决方案，比依赖注入更复杂。Ioc主要解决的就是各个类之间依赖的问题，其中依赖注入时更优解

### 构造出入 vs setter 注入

构造注入：

- 可以指定注入顺序
- 不用担心后续代码改变掉依赖关系

setter 注入：

- 结构清晰，容易理解
- 避免注入数据过多的情况下，过于臃肿

### TODO

为什么依赖注入在完成lookup相同的功能都情况下，使用的代码更少，配置文件背后spring帮我们完成了什么

[Autowired 和 Inject 之间的区别](https://stackoverflow.com/questions/7142622/what-is-the-difference-between-inject-and-autowired-in-spring-framework-which)

![inject resource autowired](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/inject_resource_autowired.png)