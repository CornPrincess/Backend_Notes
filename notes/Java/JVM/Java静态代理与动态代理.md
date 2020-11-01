# Java静态代理与动态代理

我们都知道在 Java 的世界里有静态代理和动态代理，今天就来好好分析下这两种代理方式之间的不同以及其基本用法。

## 静态代理

这里静态代理简单理解即程序员在开发阶段通过代理模式写的代理类，这些静态代理代码会在编译之后在运行时生效。

静态代理可以控制对象的访问和创建，但是也有弊端。**静态代理只能为给定接口下的实现类做代理，如果接口不同就需要定义不同的代理类**，随着系统的复杂度增加，就会很难维护这么多代理类和被代理类之间的关系。

## 动态代理

当需要频繁更换接口，更换代理类时，采用动态代理是一个更好的选择，动态代理可以通过一个代理类来代理多个被代理类。**它在更换接口时，不需要重新定义代理类，因为动态代理不需要更具接口提前定义代理类，它把代理类的创建推迟到代理运行时(runtime)来完成。**

静态代理与动态代理最大的区别在于，静态代理的代理类字节码文件在编译完之后，运行前就已经生成，而动态代理的字节码文件在运行时生成。

java 中动态代理的实现一般有两种：JDK Proxy类和第三方的 cglib

### JDK Proxy

代码示例（完整的代码在[Github](https://github.com/CornPrincess/javapractice)）：

我们先以官网的一个例子来对动态代理的使用有一个简单的概念。

- 定义代理类与被代理类公共接口

  ```java
  public interface Foo {
      Object bar(Object obj) throws BazException;
  }
  ```

- 定义被代理类

  ```java
  public class FooImpl implements Foo {
      @Override
      public Object bar(Object obj) throws BazException {
          if (obj == null) {
              throw new BazException("obj is null");
          }
          return "Hello Proxy";
      }
  }
  
  ```

- 定义一个实现类 `java.lang.reflect.InvocationHandler` 接口的动态代理类

  ```java
  public class DebugProxy implements InvocationHandler {
      // 这里使用 Object 方便下次代理其他类
      private Object obj;
  
      public DebugProxy(Object obj) {
          this.obj = obj;
      }
  
      public static Object newInstance(Object obj) {
          return Proxy.newProxyInstance(
                  obj.getClass().getClassLoader(),
                  obj.getClass().getInterfaces(),
                  new DebugProxy(obj)
          );
      }
  
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          Object result;
          try {
              System.out.println("before method " + method.getName());
              result = method.invoke(obj, args);
          } catch (InvocationTargetException e) {
              throw e.getTargetException();
          } catch (Exception e) {
              throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
          } finally {
              System.out.println("after method: " + method.getName());
          }
          return result;
      }
  }
  ```

- 使用 `java.lang.reflect.Proxy` 中的 `newProxyInstance` 创建懂动态代理并使用

  ```java
  public class ProxyTest {
      public static void main(String[] args) {
          Foo foo = (Foo) DebugProxy.newInstance(new FooImpl());
          try {
              foo.bar("min");
              foo.bar(null);
          } catch (BazException e) {
              System.out.println("occur BazException: " + e.getMessage());
          }
      }
  }
  ```

我们来看先简单上述代码简单的类图

![dynamic proxy](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/dynamicProxy.png)

与静态代理对比，动态代理多了 Proxy 和 InvocationHandler 这两个类。

他们整体的处理逻辑为：

在运行时 ProxySubject(在运行时创建) 将具体的事情交给 DynamicProxy 来做，即调用复写 InvocationHandler 的 invoke 方法，而 DynamicProxy 中的 invoke方法又会最终调用 RealSubject 的方法。

## Reference

1. [Dynamic Proxy Classes](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html)
2. [静态和动态代理模式](https://juejin.im/post/6844903978342301709#heading-0)

