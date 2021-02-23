# 责任链模式

## 意图

使多个对象都有机会处理请求，从而避免请求的发送者和接受者之间的耦合关系。

## 动机

考虑一个图形用户界面中的上下文有关的帮助机制。用户在界面的任一部分上点击就可以得到帮助信息，锁提供的帮助依赖于点击的是界面的哪一部分及其上下文。

这一模式的想法是：**给多个对象处理一个请求的机会，从而解藕发送者和接收者。**

![chain of responsibility](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/design%20pattern/chainofresponsibility.png)

## 适用性

- 有多个对象可以处理一个请求，哪个对象处理该请求运行时自动确定
- 你想在不明确制定接收者的情况下，向多个对象中的一个提交一个请求
- 可处理一个请求的对象集合应被动态指定

## 参与者

- Handler
  - 定义一个处理请求的接口
- ConcreteHandler
  - 处理它所负责的请求
  - 可访问它的后继者
  - 如果可处理请求，处理之，否则将其传递给后一个
- Client
  - 一条链上的具体处理者对象提交请求

## 代码实现

[**Chain of Responsibility** in Java](https://refactoring.guru/design-patterns/chain-of-responsibility/java/example#example-0)

