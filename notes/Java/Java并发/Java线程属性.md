# Java线程的属性

## 线程优先级

在Java中，每个线程有一个优先级。默认情况下，一个线程继承它的父线程的优先级。

```java
/**
  * The minimum priority that a thread can have.
  */
public final static int MIN_PRIORITY = 1;

/**
  * The default priority that is assigned to a thread.
  */
public final static int NORM_PRIORITY = 5;

/**
  * The maximum priority that a thread can have.
  */
public final static int MAX_PRIORITY = 10;
```

每当线程调度器有机会选择新线程时，它首先选择具有较高优先级的线程。**但是线程的优先级是高度依赖于系统的。当虚拟机依赖于宿主机平台的线程实现机制时，Java线程的优先级被映射到宿主机平台的优先级上，优先级个数也许更多，也许更少。**

如 Windows 有 7 个优先级别，一些 Java 优先级被映射到相同的操作系统优先级。在 Oracle 为 Linux 提供的 Java 虚拟机中，线程的优先级被忽略——所有线程具有相同的优先级。

**我们要避免将程序的功能正确性依赖于优先级，如果几个高优先级的线程没有进入非活动状态，低优先级的线程可能永远也不能执行。**

## 守护线程

可以使用 `t.setDaemon(true)` 将线程转换为守护线程（daemon thread），注**意这个方法要在线程启动前调用，否则会报错。**

守护线程的唯一用途是给其他线程提供服务。即时线程就是一个例子，它定时地发送“计时器滴答”信号给其他线程或情况过时的高速缓存项的线程。

当只剩下守护线程时，虚拟机就退出了，由于如果只剩守护线程，就没必要继续运行程序了。

**守护线程应该永远不去访问访问固有资源，如文件、数据库，因为它会在任何时刻甚至在一个操作的中间发生中断。**

## 未捕获异常处理器

线程的 run 方法不能抛出任何受查异常。**但是，非受查异常会导致线程终止。在这种情况下，线程就死亡 了。**

> JVM的这种设计源自于这样一种理念：“线程是独立执行的代码片断，线程的问题应该由线程自己来解决，而不要委托到外部。”基于这样的设计理念，在Java中，线程方法的异常（无论是checked还是unchecked exception），都应该在线程代码边界之内（run方法内）进行try catch并处理掉。换句话说，我们不能捕获从线程中逃逸的异常。

我们来看个例子就能明白上述描述的是什么意思

```java
public static void main(String[] args) {
    Thread thread = new Thread(() -> {
        int a = 1 / 0;
    });
    try {
        thread.start();
    } catch (Exception e) {
        System.out.println("catch it, " + e.getMessage());
    }
}
```

```html
Exception in thread "Thread-0" java.lang.ArithmeticException: / by zero
	at com.ThreadPractice2.lambda$main$0(ThreadPractice2.java:12)
	at java.lang.Thread.run(Thread.java:748)
```

我们可以看到在主线程中并不能捕获线程中抛出的错误，对于这种情况应该怎么办。

通过查看 Thread 源码我们可以看到，Thread 中有一个用来处理未捕获异常的方法

```java
/**
  * Dispatch an uncaught exception to the handler. This method is
  * intended to be called only by the JVM.
  */
private void dispatchUncaughtException(Throwable e) {
    getUncaughtExceptionHandler().uncaughtException(this, e);
}
```

```java
/**
  * Returns the handler invoked when this thread abruptly terminates
  * due to an uncaught exception. If this thread has not had an
  * uncaught exception handler explicitly set then this thread's
  * <tt>ThreadGroup</tt> object is returned, unless this thread
  * has terminated, in which case <tt>null</tt> is returned.
  * @since 1.5
  * @return the uncaught exception handler for this thread
  */
public UncaughtExceptionHandler getUncaughtExceptionHandler() {
    return uncaughtExceptionHandler != null ?
        uncaughtExceptionHandler : group;
}
```

在默认情况下发生未捕获异常时会使用 ThreadGroup 中的方法，即通过 System.err 打印到控制台。

```java
public void uncaughtException(Thread t, Throwable e) {
    if (parent != null) {
        parent.uncaughtException(t, e);
    } else {
        Thread.UncaughtExceptionHandler ueh =
            Thread.getDefaultUncaughtExceptionHandler();
        if (ueh != null) {
            ueh.uncaughtException(t, e);
        } else if (!(e instanceof ThreadDeath)) {
            System.err.print("Exception in thread \""
                             + t.getName() + "\" ");
            e.printStackTrace(System.err);
        }
    }
}
```

我们也可以自定义未捕获异常，我们可以看到异常已经被捕获了。

```java
public static void main(String[] args) {
    Thread thread = new Thread(() -> {
        int a = 1 / 0;
    });
    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.out.println("Main Thread catch it, " + e.getMessage());
        }
    });
    thread.start();
}
```

```html
Main Thread catch it, / by zero
```

## Reference

1. 《Java核心技术卷1》
2.  [Java多线程——<七>多线程的异常捕捉](https://www.cnblogs.com/brolanda/p/4725138.html)

