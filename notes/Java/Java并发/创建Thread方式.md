# 创建 Thread 方式

## 继承Thread

```java
public class ThreadByExtend extends Thread{
    public static volatile int  count = 0;

    @Override
    public void run() {
        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "sold " + (i + 1) + "tickets total Sold:" + (++count));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadByExtend thread1 = new ThreadByExtend();
        ThreadByExtend thread2 = new ThreadByExtend();
        ThreadByExtend thread3 = new ThreadByExtend();
        ThreadByExtend thread4 = new ThreadByExtend();

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }
}
```

我们可以查看相应的源码

```java
/* Java thread status for tools,
 * initialized to indicate thread 'not yet started'
 */
private volatile int threadStatus = 0;

public Thread() {
  init(null, null, "Thread-" + nextThreadNum(), 0);
}

/**
 * Initializes a Thread with the current AccessControlContext.
 * @see #init(ThreadGroup,Runnable,String,long,AccessControlContext,boolean)
 */
private void init(ThreadGroup g, Runnable target, String name,
                  long stackSize) {
  init(g, target, name, stackSize, null, true);
}

public synchronized void start() {
  /**
   * This method is not invoked for the main method thread or "system"
   * group threads created/set up by the VM. Any new functionality added
   * to this method in the future may have to also be added to the VM.
   *
   * A zero status value corresponds to state "NEW".
   */
  if (threadStatus != 0)
    throw new IllegalThreadStateException();

  /* Notify the group that this thread is about to be started
   * so that it can be added to the group's list of threads
   * and the group's unstarted count can be decremented. */
  group.add(this);

  boolean started = false;
  try {
    start0();
    started = true;
  } finally {
    try {
      if (!started) {
        group.threadStartFailed(this);
      }
    } catch (Throwable ignore) {
      /* do nothing. If start0 threw a Throwable then
                  it will be passed up the call stack */
    }
  }
}
```

使用 new 创建线程时， `threadStatus` 默认为0，表示此时线程的状态为 new，然后调用 start 方法时，会先检查当前线程状态是否为 new，如果不是，则抛出 		`IllegalThreadStateException` 异常，从中可以看到线程只可以被 start 一次。

当调用 start() 方法启动一个线程时，虚拟机会将该线程放入就绪队列中等待被调度，当一个线程被调度时会执行该线程的 run() 方法。

## 实现 Runnable 接口

```java
public class ThreadByRunnable implements Runnable {
    public static volatile int count = 0;

    @Override
    public void run() {
        for(int i=0;i<50;i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "sold "+(i+1)+" tickets total Sold:" + (++count));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadByRunnable runnable = new ThreadByRunnable();

        Thread thread1 = new Thread(runnable, "Thread1");
        Thread thread2 = new Thread(runnable, "Thread1");
        Thread thread3 = new Thread(runnable, "Thread1");
        Thread thread4 = new Thread(runnable, "Thread1");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }
}
```

## 实现Callable接口

```java
public class ThreadByCallable implements Callable<String> {
    private static int count = 0;

    @Override
    public String call() throws Exception {
        for(int i=0;i<50;i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "sold "+(i+1)+" tickets total Sold:" + (++count));
        }
        return Thread.currentThread().getName()+"sale out";
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<String> callable = new ThreadByCallable();
        FutureTask<String> futureTask1 = new FutureTask<>(callable);
        FutureTask<String> futureTask2 = new FutureTask<>(callable);
        FutureTask<String> futureTask3 = new FutureTask<>(callable);
        FutureTask<String> futureTask4 = new FutureTask<>(callable);

        Thread thread1 = new Thread(futureTask1);
        Thread thread2 = new Thread(futureTask2);
        Thread thread3 = new Thread(futureTask3);
        Thread thread4 = new Thread(futureTask4);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        String retVal1 = futureTask1.get();
        String retVal2 = futureTask2.get();
        String retVal3 = futureTask3.get();
        String retVal4 = futureTask4.get();

        System.out.println("Return value "+retVal1);
        System.out.println("Return value "+retVal2);
        System.out.println("Return value "+retVal3);
        System.out.println("Return value "+retVal4);

    }
}
```

这种方法最主要的区别是，利用Callable可以获取子线程执行结束后的返回值

通过Callable和Future来创建Thread需要四个步骤：

- 创建Callable的实现类，并实现call方法，该方法将作为线程执行体，且该方法有返回值，再创建Callable实现类的实例
- 使用FutureTask类包装Callable对象，该FutureTask对象封装了该Callable对象的call方法的返回值
- 使用FutureTask对象作为Thread对象的target创建并启动新线程
- 使用FutureTask对象 的get方法来获得子线程执行结束后的值

## 线程池

这里简单记录一下线程池的基本应用

```java
public class ThreadPoolRunnable implements Runnable{
    private static volatile AtomicInteger count = new AtomicInteger(0);

    public void run() {
        for(int i=0;i<50;i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " sold "+(i+1)+" tickets total Sold:" + count.incrementAndGet());
        }
    }

    public static void main(String[] argv) throws InterruptedException, ExecutionException {
        ExecutorService ex = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            ex.submit(new ThreadPoolRunnable());
        }

        ex.shutdown();
    }
}
```

```java
public class ThreadPoolCallable implements Callable<String> {
    private static int count = 0;

    @Override
    public String call() throws Exception {
        for(int i=0;i<50;i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "sold "+(i+1)+" tickets total Sold:" + (++count));
        }
        return "sale out";
    }

    public static void main(String[] args) {
        ExecutorService ex = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 4; i++) {
            ex.submit(new ThreadPoolCallable());
        }

        ex.shutdown();
    }

}
```



## 实现Runnable接口比继承Thread类的好处

- 实现Runnable接口可以避免单继承的局限，因为我们的线程对象有可能是别的类的子类，那么强制必须是Thread类的子类就会带来很大的麻烦
- **通过Runnable方式，多个线程可以共享一个对象，意味着线程间通信会来得更容易。而在继承Thread的方式上，线程之间因为是不同的对象，因此只能通过静态变量的方式才能使得多个线程之间共享数据。** 在Runnable的示例代码中，我们可以把count改为非静态变量，这任然是可行的，因为多个thread中的构造使用的是同一个Runnable对象。

## Reference

1. [Future](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/concurrent/Future.html)