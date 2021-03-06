# Java线程安全问题

## 竞争条件

现代操作系统中将竞争条件定义为：两个或多个进程读写某些**共享数据**，而最后的结果确取决于**进程运行的精确时序**，称为**竞争条件（race condition）**

## 临界区

凡是设计共享内存、共享文件以及共享任何资源的情况都会引发多线程安全问题，要避免这种错误，关键是找出某种途径来阻止多个进程同时读写共享的数据。换言之，我们需要的是互斥（mutual exclusion），即以某种手段确保当一个进程在使用一个共享变量或文件时，其他进程不能做同样的操作。

**我们把对共享内存进行访问的程序片段称作临界区域（critical region）或临界区（critical section）如果我们能够适当地安排，是的两个进程不可能同时处于临界区中，就能避免竞争条件。**

尽管这样的要求避免了竞争条件，但它还不能保证使用了共享数据的并发进程能够正确和高效地进行协作，对于一个好的解决方案，需要满足以下四个条件：

- 任何两个进程不能同时处于临界区
- 不应对CPU的数量和速度做出任何假设
- 临界区外运行的进程不得阻塞其他进程
- 不得使进程无限期等待进入临界区

## 竞争条件的例子

我们这里简单看一段代码，来看多线程环境下操作共享变量会发送什么情况。

```java
 @Test
public void test_atomic() {
    ThreadUnsafeExample example = new ThreadUnsafeExample();
    ExecutorService executorService = Executors.newFixedThreadPool(1000);
    for (int i = 0; i < 1000; i++) {
        executorService.execute(() -> {
            example.add();
        });
    }
    System.out.println(example.getCnt());
}

class ThreadUnsafeExample {
    @Getter
    @Setter
    private int cnt = 0;

    public void add() {
        cnt++;
    }
}
```

```html
994
```

由于存在竞争条件，最终的结果并不是1000，而是994。这是因为 ++ 并不是一个原子操作，它包括三个操作：

- 将变量 cnt 从内存加载到寄存器
- 在寄存器中执行 +1 操作
- 将结果写入内存
- 只有wait会释放锁，yield，join，sleep都不会释放锁

![原子性示意图](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/%E5%8E%9F%E5%AD%90%E6%80%A7%E7%A4%BA%E6%84%8F%E5%9B%BE.png)

## 解决方法

### 锁对象

锁机制有两个特性：**原子性**和**可见性**

这里我们首先使用 JDK5 引入的 `ReentrantLock`，Java 文档推荐使用形式如下

```java
class X {
    private final ReentrantLock lock = new ReentrantLock();
    // ...

    public void m() {
        lock.lock();  // block until condition holds
        try {
            // ... method body
        } finally {
            lock.unlock()
        }
    }
}
```

这一结构确保任意时刻只有一个线程进入临界区，**一旦一个线程获取了锁对象，其他任意线程都无法通过 lock 语句。当其他线程调用 lock 时，它会被阻塞，直到第一个线程释放锁对象。**

我们将代码改为如下，就能使结果正确

```java
@Getter
@Setter
static class ThreadSafeExample {
    private volatile int cnt = 0;

    private CountDownLatch cdl;

    public ThreadUnsafeExample(CountDownLatch cdl) {
        this.cdl = cdl;
    }

    private final Lock addLock = new ReentrantLock();

    public void add() {
        addLock.lock();
        try {
            cnt++;
            cdl.countDown();
        } finally {
            addLock.unlock();
        }
    }
}
```

当两个线程访问不同的 `ThreadSafeExample` 对象时，两个线程会获取不同的锁，两个线程都不会被阻塞。

`ReentrantLock` 重入锁，**就是支持重进入的锁，它表示该锁能够支持一个线程对资源的重复加锁，即一个已经获得锁的进程，能再次调用 lock 方法获取锁而不被阻塞。除此之外，该锁还支持获取锁时的公平和非公平性选择。**

**可重入**：**锁保持有一个持有计数（hold count）来跟踪对 lock 方法的嵌套调用**，线程在每一次调用 lock 都要调用 unlock 来释放锁。由于这一特性，被一个锁保护的代码可以调用另一个使用相同的锁的方法。我们看以下的例子。

```java
@Test
public void test_holdcount() {
    HoldCountTest holdCountTest = new HoldCountTest();
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    executorService.execute(() -> {
        holdCountTest.methodA();
    });
}

class HoldCountTest {
    private ReentrantLock lock;

    public HoldCountTest() {
        lock = new ReentrantLock();
    }

    public void methodB() {
        lock.lock();
        try {
            System.out.println("**B** holdCount current: " + lock.getHoldCount());
        } finally {
            lock.unlock();
            System.out.println("**B** after unlock, hodlcount: " + lock.getHoldCount());
        }
    }

    public void methodA() {
        lock.lock();
        try {
            System.out.println("**A** coming, holdCount: " + lock.getHoldCount());
            System.out.println("**A** jump into B lock method");
            methodB();
        } finally {
            System.out.println("**A** after return B, hodlcount: " + lock.getHoldCount());
            lock.unlock();
            System.out.println("**A** after unlock, hodlcount: " + lock.getHoldCount());
        }
    }
}
```

```html
**A** coming, holdCount: 1
**A** jump into B lock method
**B** holdCount current: 2
**B** after unlock, hodlcount: 1
**A** after return B, hodlcount: 1
**A** after unlock, hodlcount: 0
```

**公平性**

通过 ReentrantLock 的带参构造可以构建公平锁，**这里的公平指的是：等待时间最长的线程最优先获取锁，反之就是不公平的。**但是无法确保线程调度器是公平的，如果线程调度器选择忽略一个线程，而该线程为了这个锁已经等待了很久时间，那么就没机会公平地处理这个锁了。

**公平锁的机制往往没有非公平的效率高，但是当不以TPS为唯一指标时，公平锁能够减少饥饿发生的概率。**

JDK 中默认使用的是非公平性锁，观察源码可知，当一个线程请求锁时，只要获取了同步状态即成功获取锁，在这个前提下，刚释放锁的线程再次获取同步状态的几率非常大。如果把每次不同线程获取锁定义为1次切换，那么显然非公平锁切换次数更少。

**即公平性锁保证了锁的获取按照FIFO原则，而代价是大量的线程切换。非公平锁虽然可能造成线程“饥饿”，但是自己烧的线程切换，保证了其更大的吞吐量。**                              

### 条件对象

通常，线程进入临界区，却发现在某一条件满足之后它才能执行。**要使用一个条件对象来管理哪些已经获得了一个锁但是却不能做有用工作的线程。**（由于历史原因，条件对象经常被称为条件变量conditional variable）

#### await

一个锁可以有一个或者多个相关的条件对象，可以通过 `newCondition()` 方法获得。等待获得锁的线程和调用 await 方法的线程存在本质的不同，一旦一个线程调用` await` 方法，它进入该条件的**等待集**。当锁可用时，该线程不能马上解除阻塞。相反，它处于阻塞状态，直到另一个线程调用同一条件上的 `signalAll` 方法为止。

并且 await 调用之后会失去锁，这也是很正常的，如果不失去锁，那其他线程就不能进来 signalAll，就会造成死锁。

#### signalAll

- 这一调用会重新激活因为这一条件而等待的所有线程。当这些线程从等待集中移除时，他们再次成为可运行的，调度器再起激活他们。同时，他们将试图重新进入该对象。**一旦锁成为可用的，他们中断某个将从 await 返回，获得该锁并从阻塞的地方继续执行。**
- signalAll 不会立即激活一个等待的线程，**它仅仅是解除等待线程的阻塞，以便这些线程可以在当前线程退出同步方法后，通过竞争实现对对象的访问。**

为了理解上面这句话，我们来看以下代码

```java
public class WaitTest {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ConditionTest conditionTest = new ConditionTest();
        executorService.execute(() -> {
            conditionTest.before();
        });
        executorService.execute(() -> {
            conditionTest.after();
        });
    }

    static class ConditionTest {
        private Lock lock = new ReentrantLock();
        private Condition run = lock.newCondition();
        private int cnt = 0;

        public void before() {
            lock.lock();
            try {
                System.out.println("Enter before");
                if (cnt == 0) {
                    System.out.println("before await");
                    run.await();
                    System.out.println("after await");
                }
                System.out.println("before bingo!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void after() {
            lock.lock();
            try {
                System.out.println("Enter After");
                cnt = 1;
                run.signalAll();
                System.out.println("Signal All");
            } finally {
                lock.unlock();
            }
        }
    }
}
```

```html
Enter before
before await
Enter After
Signal All
after await
before bingo!!!
```

通过打印我们可用得知，**当其他线程调用 signalAll 后，之前 await 的线程被唤醒，并且确实是从 await 返回，执行 await 以后的代码。**

##### 经典实践

这里有一个经典实践，即 await 的线程被唤醒后，应该再次进行条件检测。原因是无法确保该条件被满足——signalAll 方法仅仅是通知正在等待的线程，此时有可能已经满足条件，值得再次去检测条件，一般对 await 的调用应该在如下形式中的循环体中。

```java
while (!(ok to procced)) {
    condition.await();
}
```

因此，我们可以把上述 await 处的代码进行修改。

```java
static class ConditionTest {
    private Lock lock = new ReentrantLock();
    private Condition run = lock.newCondition();
    private int cnt = 0;

    public void before() {
        lock.lock();
        try {
            System.out.println("Enter before");
            while (condition()) {
                System.out.println("before await");
                run.await();
                System.out.println("after await");
            }
            System.out.println("before bingo!!!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    
    private boolean condition() {
        System.out.println("Enter condition");
        return cnt == 0;
    }
}
```

```html
Enter before
Enter condition
before await
Enter After
Signal All
after await
Enter condition
before bingo!!!
```

**这样就可以保证 await 被唤醒之后依然会进行条件判断。**

##### 死锁问题

程序中使用 await 和 signal 使很容易出现死锁问题：**如果所有其他线程被阻塞，最后一个活动线程在解除其他线程的阻塞状态之前就调用了 await 方法，那么它也被阻塞。没有任何线程可以解除其他线程的阻塞，那么程序就挂起了。**

应该何时调用 signalAll ，从经验上讲，在对象的状态有利于等待线程的方向改变是调用 signalAll，如

```java
public void transfer(int from, int to, double amount) throws InterruptedException {
    bankLock.lock();
    try {
        while (accounts[from] < amount)
            sufficientFunds.await();
        System.out.print(Thread.currentThread());
        accounts[from] -= amount;
        System.out.printf(" %10.2f from %d to %d", amount, from, to);
        accounts[to] += amount;
        System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
        sufficientFunds.signalAll();
    }
    finally {
        bankLock.unlock();
    }
}
```

#### signal

signal 方法会随机解除等待集中的某个线程的阻塞状态，这比 signalAll 更高效，但是也存在危险。如果随机选择的线程发现自己仍然不能运行，那么它再次被阻塞。如果没有其他线程再次调用 signal，那么系统就死锁了。

### synchronize

Lock 和 Condition 为程序设计人员提供了高度锁定机制，然而，大多数情况下，并不需要那样的控制，并且可以使用一种嵌入到 Java 语言内部的机制。从 1.0 版本开始， Java 中的每一个对象都有一个**内部锁****，**这种锁是由 JVM 实现的。如果一个方法用 synchronize 修饰，那么对象的锁将保护整个方法。也就是说，要调用该方法，线程必须获得内部的对象锁。**

即以下两个代码是等价的

```java
public synchronize void method() {
}

public void method() {
    this.lock.lock();
    try {
        
    } finally {
        this.lock.unlock();
    }
}
```

注意这个内部对象锁只有一个相关条件，wait 方法添加一个线程到等待集中，notifyAll/notify 方法解除等待线程的阻塞状态，即以下代码是等待的。

```java
wait();
notifyAll();

condition.await();
condition.signalAll();
```

`synchronize` 内部锁和条件存在一些局限：

- 不能中断一个正在试图获取锁的线程
- 试图获得锁时不能设定超时
- 每个锁仅有单一的条件，可能不够

synchronize 根据锁的分类有两种用法

 #### 对象锁

在 Java 中，每个对象都会有一个 monitor 对象，这个对象其实就是 Java 对象的锁，通常被称为内置锁或对象锁。类的对象可以有多个，所以每个对象都有其独立的锁，互不干扰，即不同的线程访问被不同的对象锁保护的代码不受影响。

对象锁有两种用法

- synchronized(this|object) {} 修饰代码块
- 修饰非静态方法

```java
public class SyncThread implements Runnable {

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("A")) {
            async();
        } else if (threadName.startsWith("B")) {
            sync0();
        } else if (threadName.startsWith("C")) {
            sync();
        }
    }

    // synchronized 修饰代码块
    private void sync0() {
        System.out.println(Thread.currentThread().getName() + "-sync0:" + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        synchronized (this) {
            try {
                System.out.println(Thread.currentThread().getName() + "_Sync0_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                Thread.sleep(2000);
                System.out.println(Thread.currentThread().getName() + "_Sync0_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // synchronize 修饰非静态方法
    private synchronized void sync() {
        System.out.println(Thread.currentThread().getName() + "_Sync2: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        try {
            System.out.println(Thread.currentThread().getName() + "_Sync2_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + "_Sync2_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步方法
     */
    private void async() {
        try {
            System.out.println(Thread.currentThread().getName() + "_Async_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + "_Async_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

```java
public static void main(String[] args) {
  SyncThread syncThread = new SyncThread();
  Thread A_thread1 = new Thread(syncThread, "A_thread1");
  Thread A_thread2 = new Thread(syncThread, "A_thread2");
  Thread B_thread1 = new Thread(syncThread, "B_thread1");
  Thread B_thread2 = new Thread(syncThread, "B_thread2");
  Thread C_thread1 = new Thread(syncThread, "C_thread1");
  Thread C_thread2 = new Thread(syncThread, "C_thread2");
  A_thread1.start();
  A_thread2.start();
  B_thread1.start();
  B_thread2.start();
  C_thread1.start();
  C_thread2.start();
}
```

我们分别同时运行三组线程，来查看同步与异步方法的区别

##### 异步方法

我们可以看到在多线程运行不加锁的方法时，两个现场可以同时进入相同的代码块，不存在互斥现象。

```html
A_thread2_Async_Start: 22:29:49:549
A_thread1_Async_Start: 22:29:49:549
A_thread1_Async_End: 22:29:51:550
A_thread2_Async_End: 22:29:51:550
```

##### synchronized修饰代码块

```java
B_thread1-sync0:22:32:45:727
B_thread2-sync0:22:32:45:727
B_thread1_Sync0_Start: 22:32:45:728
B_thread1_Sync0_End: 22:32:47:731
B_thread2_Sync0_Start: 22:32:47:732
B_thread2_Sync0_End: 22:32:49:734
```

观察结果可以发现 synchronized 代码块以外的代码，多个线程访问不受限制，但是对于代码块以内的代码，对象锁进行了互斥的保护，即同一时间只能由一个获得锁的线程进行访问，其他线程要访问必须等待拥有锁的线程释放锁才可以。**上面的代码也证实了 sleep 操作并不会释放锁。**

##### sycnhronized 修饰非静态方法

```html
C_thread1_Sync2: 22:37:19:371
C_thread1_Sync2_Start: 22:37:19:372
C_thread1_Sync2_End: 22:37:21:377
C_thread2_Sync2: 22:37:21:378
C_thread2_Sync2_Start: 22:37:21:378
C_thread2_Sync2_End: 22:37:23:379
```

上述结果表明，synchronized 修饰的静态方法，该方法内的所有代码都被对象锁保护，同一时间是能有一个线程获得锁进行访问。

##### 测试不同对象

以上的测试代码都是针对同一个带有同步方法类的对象，我们现在测试多线程访问不同对象。

```java
public static void main(String[] args) {
  Thread A_thread1 = new Thread(new SyncThread(), "A_thread1");
  Thread A_thread2 = new Thread(new SyncThread(), "A_thread2");
  Thread B_thread1 = new Thread(new SyncThread(), "B_thread1");
  Thread B_thread2 = new Thread(new SyncThread(), "B_thread2");
  Thread C_thread1 = new Thread(new SyncThread(), "C_thread1");
  Thread C_thread2 = new Thread(new SyncThread(), "C_thread2");
  A_thread1.start();
  A_thread2.start();
  B_thread1.start();
  B_thread2.start();
  C_thread1.start();
  C_thread2.start();
}
```

```html
C_thread2_Sync2: 22:44:10:527
C_thread1_Sync2: 22:44:10:527
C_thread1_Sync2_Start: 22:44:10:527
C_thread2_Sync2_Start: 22:44:10:527
C_thread1_Sync2_End: 22:44:12:531
C_thread2_Sync2_End: 22:44:12:531
```

分析部分结果可知，多线程访问不同对象的同步方法互不影响。

#### 类锁

和对象锁相似，类锁同样也有两种用法

- synchronized(X.class)
- 修饰静态方法

我们先来测试多线程对于同一个对象的调用

##### synchronized(类.class) {} 

来看对应的运行结果

```html
B_thread1_Sync1: 23:03:29
B_thread2_Sync1: 23:03:29
B_thread1_Sync1_Start: 23:03:29
B_thread1_Sync1_End: 23:03:31
B_thread2_Sync1_Start: 23:03:31
B_thread2_Sync1_End: 23:03:33
```

观察结果可知，代码块内的方法是被锁保护的

##### sycnhronized 修饰静态方法

```html
C_thread1_Sync2: 23:07:27
C_thread1_Sync2_Start: 23:07:27
C_thread1_Sync2_End: 23:07:29
C_thread2_Sync2: 23:07:29
C_thread2_Sync2_Start: 23:07:29
C_thread2_Sync2_End: 23:07:31
```

同样修饰的静态方法也是被类锁保护的。

##### 测试不同对象

```java
public static void main(String[] args) {
  Thread B_thread1 = new Thread(new ClassLockSyncThread(), "B_thread1");
  Thread B_thread2 = new Thread(new ClassLockSyncThread(), "B_thread2");
  Thread C_thread1 = new Thread(new ClassLockSyncThread(), "C_thread1");
  Thread C_thread2 = new Thread(new ClassLockSyncThread(), "C_thread2");

  B_thread1.start();
  B_thread2.start();
  C_thread1.start();
  C_thread2.start();
}
```

先来看用 synchronized(X.class) 代码块

```html
B_thread2_Sync1: 23:12:30
B_thread1_Sync1: 23:12:30
B_thread2_Sync1_Start: 23:12:30
B_thread2_Sync1_End: 23:12:32
B_thread1_Sync1_Start: 23:12:32
B_thread1_Sync1_End: 23:12:34
```

**可以看出即使是不同对象，类锁还是可以保证线程的互斥**，再来看对于修饰的静态方法

```html
C_thread1_Sync2: 23:16:35
C_thread1_Sync2_Start: 23:16:35
C_thread1_Sync2_End: 23:16:37
C_thread2_Sync2: 23:16:37
C_thread2_Sync2_Start: 23:16:37
C_thread2_Sync2_End: 23:16:39
```

可以看出对于修饰的静态方法，即使是不同对象，在同一时刻还是只能有一个线程可以获得锁。

最后，我们要明确一点，**所有对象锁和类锁是独立的，互不干扰。**Java 设计者以不是很精确的方式采用来监视器 monitor 的概念，**Java 中每一个对象有一个内部的锁和内部的条件。如果一个方法用 synchronized 关键字声明，那么，他表现的就像一个监视器方法。**

## Reference

1. Java核心技术卷1
2. [Java 之 synchronized 详解](https://juejin.cn/post/6844903482114195463#heading-0)

































