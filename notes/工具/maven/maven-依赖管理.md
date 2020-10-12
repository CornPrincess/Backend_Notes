# maven 依赖管理

## 继承关系

在一个项目中，经常会使用到maven的继承关系，即子项目继承父项目，父项目的配置如下：

```xml
<project>    
      <modelVersion>4.0.0</modelVersion>    
      <groupId>org.clf.parent</groupId>    
      <artifactId>my-parent</artifactId>    
      <version>2.0</version>    
      <packaging>pom</packaging>   

      <!-- 该节点下的依赖会被子项目自动全部继承 -->  
      <dependencies>  
        <dependency>  
               <groupId>org.slf4j</groupId>  
               <artifactId>slf4j-api</artifactId>  
               <version>1.7.7</version>  
               <type>jar</type>  
               <scope>compile</scope>  
        </dependency>  
      </dependencies>  

      <dependencyManagement>  
        <!-- 该节点下的依赖关系只是为了统一版本号，不会被子项目自动继承，-->  
        <!--除非子项目主动引用，好处是子项目可以不用写版本号 -->  
        <dependencies>  
           <dependency>  
                <groupId>org.springframework</groupId>  
                <artifactId>spring-orm</artifactId>  
                <version>4.2.5.RELEASE</version>  
            </dependency>  

            <dependency>  
                <groupId>org.springframework</groupId>  
                <artifactId>spring-web</artifactId>  
                <version>4.2.5.RELEASE</version>  
            </dependency>  
            <dependency>  
                <groupId>org.springframework</groupId>  
                <artifactId>spring-context-support</artifactId>  
                <version>4.2.5.RELEASE</version>  
            </dependency>  
            <dependency>  
                <groupId>org.springframework</groupId>  
                <artifactId>spring-beans</artifactId>  
                <version>4.2.5.RELEASE</version>  
            </dependency>  
        </dependencies>  
       </dependencyManagement>  

       <!-- 这个元素和dependencyManagement相类似，它是用来进行插件管理的-->  
       <pluginManagement>    
       ......  
       </pluginManagement>  
</project>
```

注意，**此时 packaging 必须为 pom**。

为了项目的正确运行，必须让所有的子项目使用依赖项的统一版本，必须确保应用的各个项目的依赖项和版本一致，才能保证测试的和发布是相同的结果。

**父项目在dependencies声明的依赖，子项目会从全部自动地继承**。**Maven 使用dependencyManagement 元素来提供了一种管理依赖版本号的方式**。

## 聚合关系

maven 工程经常用到多模块管理，此时我们可以使用一个 pom 文件来对多个模块进行配置

```xml
<project>   
       <modelVersion>4.0.0</modelVersion>   
       <groupId>org.clf.parent</groupId>   
       <artifactId>my-parent</artifactId>   
       <version>2.0</version>   

       <!-- 打包类型必须为pom -->  
       <packaging>pom</packaging>  

       <!-- 声明了该项目的直接子模块 -->  
       <modules>  
       <!-- 这里配置的不是artifactId，而是这个模块的目录名称-->  
        <module>module-1</module>  
        <module>module-2</module>  
        <module>module-3</module>  
    </modules>  

       <!-- 聚合也属于父子关系，总项目中的dependencies与dependencyManagement、pluginManagement用法与继承关系类似 -->  
       <dependencies>  
        ......  
       </dependencies>  

       <dependencyManagement>  
        ......  
       </dependencyManagement>  

       <pluginManagement>   
       ......  
       </pluginManagement>  
</project>
```



## Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. Maven总结之依赖管理