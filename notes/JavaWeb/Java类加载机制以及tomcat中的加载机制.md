# Java 类加载机制以及 tomcat 类加载机制

我们都知道 Java 采用了双亲委派的类加载机制，目的是为了保证类的单一性与安全性

但是 tomcat 没有采用这种双亲 委派类加载机制，具体可以看这篇博文

- [图解Tomcat类加载机制(阿里面试题)](https://www.cnblogs.com/aspirant/p/8991830.html)
- [【深入理解JVM】：类加载器与双亲委派模型](https://blog.csdn.net/u011080472/article/details/51332866)

注意有个线程上下文类加载器

除了tomcat之外，JDBC,JNDI,`Thread.currentThread().setContextClassLoader();`等很多地方都一样是违反了双亲委托。