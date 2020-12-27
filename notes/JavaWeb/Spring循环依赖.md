# Spring循环依赖

要明白这个问题，首先要明白 Spring bean 的创建，其本质还是一个对象的创建。既然是对象的创建，那么一个完整的对象就包括了两部分：**当前对象的实例化和对象属性的实例化。**

在Spring中，**对象的实例化是通过反射实现的，而对象的属性则是在对象实例化之后通过一定的方式设置的。**

## 对象创建过程分析

Spring实例化bean是通过ApplicationContext.getBean()方法来进行的。**如果要获取的对象依赖了另一个对象，那么其首先会创建当前对象，然后通过递归的调用ApplicationContext.getBean()方法来获取所依赖的对象，最后将获取到的对象注入到当前对象中。**

```java
@Component
public class A {
  private B b;
  public void setB(B b) {
    this.b = b;
  }
}
@Component
public class B {
  private A a;
  public void setA(A a) {
    this.a = a;
  }
}
```

我们以初始化 A 对象为例

- 首先，因为Spring容器中没有A对象，因此Spring尝试通过ApplicationContext.getBean()方法获取A对象的实例，由于Spring容器中还没有A对象实例，因而其会创建一个A对象
- 然后发现其依赖了B对象，因而会尝试递归的通过ApplicationContext.getBean()方法获取B对象的实例
- 但是Spring容器中此时也没有B对象的实例，因而其还是会先创建一个B对象的实例。
  - **此刻，A对象和B对象都已经创建了，并且保存在Spring容器中了，只不过A对象的属性b和B对象的属性a都还没有设置进去。**
- 在创建完 B对象后，Spring发现B对象依赖了属性A，因而还是会尝试递归的调用ApplicationContext.getBean()方法获取A对象的实例，**因为Spring中已经有一个A对象的实例，虽然只是半成品（其属性b还未初始化），但其也还是目标bean，因而会将该A对象的实例返回。**
  - 注意，**此时，B对象的属性a就设置进去了，然后还是ApplicationContext.getBean()方法递归的返回，也就是将B对象的实例返回，此时就会将该实例设置到A对象的属性b中。**
  - 这个时候，注意A对象的属性b和B对象的属性a都已经设置了目标对象的实例了

总结一下，Spring 解决循环依赖问题的思路：

- Spring 通过递归的方式获取目标bean 及其依赖的 bean
- Spring 实力化一个 bean 的时候，是分两步进行的，首先实力话目标 bean，然后为其注入属性。
- **关键点在于，要在B实例创建后，注入A的时候，能够拿到A的实例，如果拿不到，那么这里就会一直循环下去，拿到A的实例才可以打破这个循环。**为了做到这一点，Spring使用了缓存。



## Spring 的三级缓存的设计

```text
一级缓存，缓存正常的bean实例
二级缓存，缓存还未进行依赖注入和初始化方法调用的bean实例
三级缓存，缓存bean实例的ObjectFactory
```