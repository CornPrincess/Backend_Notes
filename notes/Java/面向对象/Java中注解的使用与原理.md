# Java中注解的使用与原理

## 注解语法

注解是由注解接口来定义的

```java
modifiers @interface AnnotationName {
  elementDeclaration1;
  elementDeclaration1;
}
```

每个元素的声明都有如下的形式：

```java
type elementName();

type elementname() default value;
```

我们从来不需要提供那些实现了注解接口的类，相反地，虚拟机会在需要的时候产生于血代理类和对象。

注解接口中的元素声明实际上是方法声明，一个注解接口的方法不能有任何参数和任何throws语句，并且他们也不能是泛型的。

注解元素的类型包括：

- 基本类型（byte, short, char, int, long, double, float, boolean）
- String
- Class
- enum类型
- 注解类型
- 有前面所述类型组成的一维数组

例子：

```java
public @interface Demo {
    enum Status { UNCONFIRMED, CONFIRMED, FIXED, NOTABUG};
    boolean show() default false;
    String assignTo() default "Bob";
    Class<?> testCase() default Void.class;
    Status status() default Status.CONFIRMED;
    Reference ref() default @Reference;
    String[] reportBy();
}
```

注意：

- 因为注解是由编译器计算而来的，因此，所有元素都必须是编译期常量
- 注解元素不能设为null，默认值也不可以

## 标准注解



## 使用注解

注解是那些插入到源代码中使用其他工具可以对其进行处理的标签。**这些工具可以在源码层次上进行操作，或者可以处理编译器在其中放置类注解的类文件。**

注解可能的用法：

- 附属文件的自动生成，例如部署描述符或者bean类信息
- 测试，日志，事务语义等代码的自动生成

在 Java 中，注解是当作一个修饰符来使用的，他被置于注解项之前。注解本身并不会做任何事情，他需要工具支持才会有用，每个注解都需要通过一个注解接口来进行定义，如我们经常使用的 @Test

```java
package org.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Test {
    Class<? extends Throwable> expected() default Test.None.class;

    long timeout() default 0L;

    public static class None extends Throwable {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }
}
```

我们分析上述代码，`@interface` 声明创建了一个真正的 Java 接口，处理注解的工具将接受那些实现了这个注解接口的对象。这类工具可以调用上述的 `timeout` 方法来检索某个特定 Test 注解的 `timeout` 元素。

注解 Target 和 Retention 是元注解，他们将 Test 注解表识成一个只能运用到方法上的注解，并且当类文件在入到虚拟机的时候，仍可以保留下来。

注解本身不会做任何事，他们只是存在于源文件中。编译器将他们置于类文件中，并且虚拟机会将他们载入。

### 定义指定义注解类

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionListenerFor {
    String source();
}
```

需要注意的是 `java.lang.annotation.Annotation` 中的注释 `The common interface extended by all annotation types.` 即所有的注解类型都继承自这个接口（Annotation），对于经常用到的 @Override 来说，它相当于

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {

}

public interface Override extends Annotation{
    
}
```

### 解析注解代码

注解本身不会做任何事情，他们只是存在于源文件中，其本质是一个接口。从这个角度看一个注解更像是注释。此时需要解析注解的代码，他们才能生效，一般有两种，一种是编译器 直接扫描，一种是运行时反射。

#### 编译期扫描

这种方法指编译器在对java 代码编译字节码的过程会检测到某个类或者方法被一些注解修饰，这是它会对这些注解进行某些处理。

典型的就是注解 @Override，一旦编译器检测到某个方法被修饰了 @Override 注解，编译器就会检查当前方法的方法签名是否真正重写了父类的某个方法，也就是比较父类中是否具有一个同样的方法签名。

这一种情况只适用于那些编译器已经熟知的注解类，比如 JDK 内置的几个注解，而你自定义的注解，编译器是不知道你这个注解的作用的，当然也不知道该如何处理，往往只是会根据该注解的作用范围来选择是否编译进字节码文件，仅此而已。



#### 运行时反射

我们这里的示例代码使用反射来解析注解，首先来看使用注解的类

```java
public class ButtonFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;

    private JPanel panel;
    private JButton yellowButton;
    private JButton blueButton;
    private JButton redButton;

    public ButtonFrame() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        panel = new JPanel();
        add(panel);

        yellowButton = new JButton("Yellow");
        blueButton = new JButton("Blue");
        redButton = new JButton("Red");

        panel.add(yellowButton);
        panel.add(blueButton);
        panel.add(redButton);

        ActionListenerInstaller.processAnnotations(this);
    }

    @ActionListenerFor(source = "yellowButton")
    public void yellowBackground() {
        panel.setBackground(Color.YELLOW);
    }

    @ActionListenerFor(source = "blueButton")
    public void blueBackground() {
        panel.setBackground(Color.BLUE);
    }

    @ActionListenerFor(source = "redButton")
    public void redBackground() {
        panel.setBackground(Color.RED);
    }
}
```

接着看解析注解的类：

```java
public class ActionListenerInstaller {

    public static void processAnnotations(Object object) {
        try {
            Class<?> cl = object.getClass();
            for (Method m : cl.getDeclaredMethods()) {
                ActionListenerFor a = m.getAnnotation(ActionListenerFor.class);
                System.out.println(a.annotationType());
                System.out.println(a);
                System.out.println(a instanceof Annotation);
                if (a != null) {
                    Field f = cl.getDeclaredField(a.source());
                    f.setAccessible(true);
                    addListener(f.get(object), object, m);
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void addListener(Object source, final Object param, final Method m) throws ReflectiveOperationException {
        InvocationHandler handler = (proxy, method, args) -> m.invoke(param);
        Object listener = Proxy.newProxyInstance(null, new Class[] {java.awt.event.ActionListener.class}, handler);
        Method adder = source.getClass().getMethod("addActionListener", ActionListener.class);
        adder.invoke(source, listener);
    }
}
```

测试代码：

```java
public class ButtonTest {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ButtonFrame frame = new ButtonFrame();
            frame.setTitle("ButtonTest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
```

在这个例子中，我们通过**反射的机制让注解在运行时进行处理**

# 修得可以：

## 合照：

0Z1A0866

0Z1A0926

0Z1A0972

0Z1A1008

0Z1A0986

0Z1A1001

0Z1A1037

0Z1A1045 

0Z1A1070

0Z1A1152

0Z1A1175

0Z1A1125

0Z1A1185

 ## 单人照：

0Z1A0873

0Z1A0878

0Z1A0897

0Z1A0905

0Z1A0980

0Z1A1177

#修得有问题：

0Z1A0974 女方脸拉得太长

## Reference

1. [JAVA 注解的基本原理](https://juejin.im/post/6844903636733001741#heading-3)

