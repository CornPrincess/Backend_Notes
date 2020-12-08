Broker中存放多个 partiton，其中 partition 有leader和follow，正常情况生产者和消费者都会走 leader 的partition，只有当他宕机才会使用follow，此时消费者和生产者都要重新连接 follow

消费者有消费者组的概念，consumer group，一个 partition只能被在同一个消费者组里的一个消费者消费，一个群组里的消费者订阅的是同一个主题

不要让一个消费者组中消费者的数量超过分区的数量，这样会让一部分消费者闲置。

一个消费者可以对映多个 partition

一个 partition 也可以对映多个 消费者？



简单理解，消费者组中有多个消费者，一个主题中有多个partition，消费者负责将所有partition都消费，当然原则是，一个partition只能对映一个消费者



在同一个群组中，无法让一个线程运行多个消费者，也无法让多个线程安全地共享一个消费者。**按照规则，一个消费者使用一个线程。**

**如果要在同一个消费者群组里运行多个消费者，需要让每个消费者运行在自己的线程里 。最好是把消费者的逻辑封装在自己的对象上，然后使用 ExecutorService 启动多线程，是每个消费者运行在自己的线程上。**



zookeeper 中储存的信息：kafka集群元数据，consumer的消费信息如offset

0.9版本之前consumer offset保存在zk，但是0.9之后，保存在本地，即系统topic，存在磁盘，因为要默认保留7天