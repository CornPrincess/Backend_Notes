在 Spring 中的applicationContent.xml 中可以设置 bean 的 

- init-method：在构造方法之后，被调用方法之前执行
- destroy-method：调用 applicationContext 的 close 方法且bean为单例时调用

Spring 在初始化 bean时会把 xml 中配置的一起初始化

destroy-method 被执行有两个条件：

- 工厂被关闭 
- bean为单例(默认单例)



scope可以选择5个参数

- singleton：默认，单例模式创建对象
- prototype：多例模式创建对象
- request：在web项目中，Spring创建类后存入request范围内
- session：创建后存入session范围内
- globalsession：必须在porlet环境中使用，如登陆了QQ，就可以同时登陆QQ空间

P 名称空间属性注入

```xml
<bean id="pbook" class="love.minmin.model.pnamespace.Book" p:name="Atom" p:price="12.4" p:o-ref=""></bean>
```



xml 