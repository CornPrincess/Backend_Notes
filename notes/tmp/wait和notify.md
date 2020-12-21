# 生产者消费者模式

## wait notify

### wait notify存在的问题

**1.notify早期通知**

针对这种现象，解决方法，一般是添加一个状态标志，让waitThread调用wait方法前先判断状态是否已经改变了没，如果通知早已发出的话，WaitThread就不再去wait。对上面的代码进行更正

2. **等待wait的条件发生变化**
3.  **“假死”状态**



## JVM 源码

### wait 时调用的源码

```c++
// create a node to be put into the queue
// Critically, after we reset() the event but prior to park(), we must check
// for a pending interrupt.
ObjectWaiter node(Self);
node.TState = ObjectWaiter::TS_WAIT ;
Self->_ParkEvent->reset() ;
OrderAccess::fence();          // ST into Event; membar ; LD interrupted-flag

// Enter the waiting queue, which is a circular doubly linked list in this case
// but it could be a priority queue or any data structure.
// _WaitSetLock protects the wait queue.  Normally the wait queue is accessed only
// by the the owner of the monitor *except* in the case where park()
// returns because of a timeout of interrupt.  Contention is exceptionally rare
// so we use a simple spin-lock instead of a heavier-weight blocking lock.

Thread::SpinAcquire (&_WaitSetLock, "WaitSet - add") ;
AddWaiter (&node) ;
Thread::SpinRelease (&_WaitSetLock) ;
```

- 将当前线程包装为 ObjectWriter 对象，并且状态为TS_WAIT，对应的jstack看到的线程状态应该是waiting
- 调用 AddWaiter 方法，即将ObjectWaiter对象被放入了WaitSet中，WaitSet是个环形双向链表(circular doubly linked list)
- 当前线程通过park()方法开始挂起(suspend)

### 线程争抢 锁 处于 Block 状态会发生的过程：

- 偏向锁逻辑，未命中
- 如果是无所状态，就通过CAS去竞争锁，此时由于锁被其他线程占有，所以不是无所状态
- 不是无所状态，而且锁不是线程C持有，执行锁膨胀，构造ObjectMonitor对象
- 竞争锁，竞争失败就将线程加入_cxq队列的首位
- 开始无限循环，竞争锁成功就退出循环，竞争失败线程挂起，等待被唤醒后继续竞争

### notify 时调用的源码

```c++
void ObjectMonitor::notify(TRAPS) {
CHECK_OWNER();
if (_WaitSet == NULL) {
TEVENT (Empty-Notify) ;
    return ;
}
DTRACE_MONITOR_PROBE(notify, this, object(), THREAD);

int Policy = Knob_MoveNotifyee ;

Thread::SpinAcquire (&_WaitSetLock, "WaitSet - notify") ;
ObjectWaiter * iterator = DequeueWaiter() ;
    
 ObjectWaiter * List = _EntryList ;
    if (List != NULL) {
        assert (List->_prev == NULL, "invariant") ;
        assert (List->TState == ObjectWaiter::TS_ENTER, "invariant") ;
        assert (List != iterator, "invariant") ;
    }

    if (Policy == 0) {       // prepend to EntryList
        if (List == NULL) {
            iterator->_next = iterator->_prev = NULL ;
            _EntryList = iterator ;
        } else {
            List->_prev = iterator ;
            iterator->_next = List ;
            iterator->_prev = NULL ;
            _EntryList = iterator ;
        }
    } else
        if (Policy == 1) {      // append to EntryList
            if (List == NULL) {
                iterator->_next = iterator->_prev = NULL ;
                _EntryList = iterator ;
            } else {
                // CONSIDER:  finding the tail currently requires a linear-time walk of
                // the EntryList.  We can make tail access constant-time by converting to
                // a CDLL instead of using our current DLL.
                ObjectWaiter * Tail ;
                for (Tail = List ; Tail->_next != NULL ; Tail = Tail->_next) ;
                assert (Tail != NULL && Tail->_next == NULL, "invariant") ;
                Tail->_next = iterator ;
                iterator->_prev = Tail ;
                iterator->_next = NULL ;
            }
        } else
            if (Policy == 2) {      // prepend to cxq
                // prepend to cxq
                if (List == NULL) {
```

- 查看 Policy 的赋值
- 调用 DequeueWaiter() 方法将 WaitSet 队列的第一个值取出并返回
- 根据 Policy 的值对取出的 ObjectWaiter 对象进行不同的操作
  - Policy == 0：放入EntryList队列的排头位置
  - Policy == 1：放入EntryList队列的末尾位置
  - Policy == 2：EntryList队列为空就放入EntryList，否则放入_cxq队列的排头位置
  - Policy == 3：放入cxq队列中，末尾位置；更新cxq变量的值的时候，同样要通过CAS注意并发问题

### 线程释放锁时的源码

```c++
void ATTR ObjectMonitor::exit(bool not_suspended, TRAPS) {
   Thread * Self = THREAD ;
   if (THREAD != _owner) {
     if (THREAD->is_lock_owned((address) _owner)) {
       // Transmute _owner from a BasicLock pointer to a Thread address.
       // We don't need to hold _mutex for this transition.
       // Non-null to Non-null is safe as long as all readers can
       // tolerate either flavor.
       assert (_recursions == 0, "invariant") ;
       _owner = THREAD ;
       _recursions = 0 ;
       OwnerIsThread = 1 ;
     } else {
       // NOTE: we need to handle unbalanced monitor enter/exit
       // in native code by throwing an exception.
       // TODO: Throw an IllegalMonitorStateException ?
       TEVENT (Exit - Throw IMSX) ;
       assert(false, "Non-balanced monitor enter/exit!");
       if (false) {
          THROW(vmSymbols::java_lang_IllegalMonitorStateException());
       }
       return;
     }
   }

   if (_recursions != 0) {
     _recursions--;        // this is simple recursive enter
     TEVENT (Inflated exit - recursive) ;
     return ;
   }
    // ....
}
```

- 偏向锁逻辑
- 根据QMode，将ObjectWaiter 从cxq 或者 EntryList 中取出后唤醒
  - QMode = 2，并且cxq非空：取cxq队列排头位置的ObjectWaiter对象，调用ExitEpilog方法，该方法会唤醒ObjectWaiter对象的线程，此处会立即返回，后面的代码不会执行了；
  - QMode = 3，并且cxq非空：把cxq队列首元素放入_EntryList的尾部；
  - QMode = 4，并且cxq非空：把cxq队列首元素放入_EntryList的头部；
  - QMode = 0，依次从EntryList中取出线程来唤醒
- 唤醒的元素会继续执行挂起前的代码，按照我们之前的分析，线程唤醒后，就会通过CAS去竞争锁，此时由于线程B已经释放了锁，那么此时应该能竞争成功；

OpenJDK 8 中默认的 Policy == 2， QMode == 0

## Reference

1. [一篇文章，让你彻底弄懂生产者--消费者问题](https://www.jianshu.com/p/e29632593057)
2. [大佬问我: notify()是随机唤醒线程么?](https://www.jianshu.com/p/99f73827c616)
3. [生产者与消费者模型](https://www.jianshu.com/p/f53fb95b5820)
4. [javap的使用](https://www.cnblogs.com/baby123/p/10756614.html)
5. [Java的wait和notify学习三部曲之一：JVM源码分析](https://blog.csdn.net/boling_cavalry/article/details/101369287?ops_request_misc=%25257B%252522request%25255Fid%252522%25253A%252522160851682416780271135669%252522%25252C%252522scm%252522%25253A%25252220140713.130102334.pc%25255Fall.%252522%25257D&request_id=160851682416780271135669&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~first_rank_v2~rank_v29-3-101369287.first_rank_v2_pc_rank_v29&utm_term=wait%20notify%E4%B8%89%E9%83%A8%E6%9B%B2)
6. [jstack命令解析（死锁代码）](https://www.jianshu.com/p/8d5782bc596e)

