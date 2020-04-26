Maven 中的 pom.xml 文件是我们平时接触最多的文件，但在最近的工作中经常发现有些基础的概念不明白而导致项目最终不能构建成功，我们先从最简单的 pom.xml 来看其结构。

# hello-world

```xml
<!-- 声明xml的版本和编码方法 -->
<?xml version="1.0" encoding="UTF-8"?>  
<!-- 声明pom中的命名空间和xsd元素，这些元素虽然不是必须的，但是可以让IDE识别帮助更快地写xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  	<!-- 当POM模型的版本，maven2和maven3都是4.0.0 -->
    <modelVersion>4.0.0</modelVersion>

    <!-- 组id，如mycom公司有一个项目为myapp，则groupId为com.mycom.myapp -->
    <groupId>com.minmin.maven</groupId>
    <!-- 组中子项目的Id -->
    <artifactId>hello-world</artifactId>
    <version>1.0-SNAPSHOT</version>
  
    <dependencies>
        <!-- https://mvnrepository.com/artifact/junit/junit -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

		<build>
        <plugins>
            <plugin>
                <!-- 设置编译时的JDK版本和目标JDK版本 -->
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

在 maven 工程中，一个个将项目的主代码放在 `src/main/java` 中，并且主目录的报名一般为 `groupId`  加上 `artifactId`， 即 `com.minmin.maven.helloworld`

注意：

`<Dependency>` 中的 `<scope>test</scope>` 表示依赖只对测试有效，即在测试代码中可以导入，但是在主代码中不能导入，会编译失败。**如果不写，默认是 `<scope>compile</scope>` ，即测试和主代码都有效。**



# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [Maven总结之入门指南（一）](