Maven 中的 pom.xml 文件是我们平时接触最多的文件，但在最近的工作中经常发现有些基础的概念不明白而导致项目最终不能构建成功

> POM stands for "Project Object Model". It is an XML representation of a Maven project held in a file named `pom.xml`. When in the presence of Maven folks, speaking of a project is speaking in the philosophical sense, beyond a mere collection of files containing code. A project contains configuration files, as well as the developers involved and the roles they play, the defect tracking system, the organization and licenses, the URL of where the project lives, the project's dependencies, and all of the other little pieces that come into play to give code life.  It is a one-stop-shop for all things concerning the project. In fact, in the Maven world, a project does not need to contain any code at all, merely a `pom.xml`.<sup>[2]</sup>

我们不需要任何实际的 Java 代码，就可以定义一个 maven 项目的 POM，这体现 maven 的一大优点：**它可以让项目对象模型最大程度地于实际代码想独立，我们可以称之为解藕或者正交性。**

# Quick Overview

基本的 pom.xml 文件结构如下，其中 `<modelVersion>4.0.0</modelVersion>`  是不变的，目前 maven 只支持这种版本。

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <!-- The Basics -->
  <groupId>...</groupId>
  <artifactId>...</artifactId>
  <version>...</version>
  <packaging>...</packaging>
  <dependencies>...</dependencies>
  <parent>...</parent>
  <dependencyManagement>...</dependencyManagement>
  <modules>...</modules>
  <properties>...</properties>
 
  <!-- Build Settings -->
  <build>...</build>
  <reporting>...</reporting>
 
  <!-- More Project Information -->
  <name>...</name>
  <description>...</description>
  <url>...</url>
  <inceptionYear>...</inceptionYear>
  <licenses>...</licenses>
  <organization>...</organization>
  <developers>...</developers>
  <contributors>...</contributors>
 
  <!-- Environment Settings -->
  <issueManagement>...</issueManagement>
  <ciManagement>...</ciManagement>
  <mailingLists>...</mailingLists>
  <scm>...</scm>
  <prerequisites>...</prerequisites>
  <repositories>...</repositories>
  <pluginRepositories>...</pluginRepositories>
  <distributionManagement>...</distributionManagement>
  <profiles>...</profiles>
</project>
```



## Dependencies

### 依赖配置

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      https://maven.apache.org/xsd/maven-4.0.0.xsd">
  ...
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <type>jar</type>
      <scope>test</scope>
      <optional>true</optional>
    </dependency>
    ...
  </dependencies>
  ...
</project>
```

依赖配置是最常使用的部分，但是有些参数平时没用到，在这里做简单记录。

- groupId： 库组织名称，但是不应该只定义到组织名或者公司名，一般具体到项目一级，如org.sonatype.nexus(与对应的域名相反：nexus.sonatype.org)
- artifactId: 具体的实际项目中的一个 Maven 项目（模块），推荐的做法是使用实际项目名称作为 artifactId 的前缀，如 nexus-indexer
- version: 选择的 Maven 项目的版本（**dependency version requirement specification**)
- classifier：区分 JDK版本
- type：区分依赖包的类型，默认为 jar
- scope: 区分在编译，测试或运行时作用的 classpath，以及限制这些依赖的传递
  - compile：默认值，表示在编译，测试，运行阶段都需要这个模块对应的 jar 包在 classpath中，并且依赖会传递
  - provided：仅在 compilation 和 test 的 classpaths中有效，并且没有依赖传递，即表示该包应该在 JDK 或 container 中提供。
  - runtime：表示该 jar 包在compile阶段不需要，在runtime 和 test 阶段需要
  - test：表示该 jar 包仅在test 和 execution 阶段需要，没有传递性
  - system：this scope is similar to `provided` except that you have to provide the JAR which contains it explicitly. The artifact is always available and is not looked up in a repository.

对于 scope 我们可以使用表格进行记录

| 依赖范围 （scope） | compile classpath | test classpath | execution classpath |              例子               |
| :----------------: | :---------------: | :------------: | :-----------------: | :-----------------------------: |
|      compile       |         Y         |       Y        |          Y          |           Spring-core           |
|        test        |         -         |       Y        |          Y          |              Junit              |
|      Provided      |         Y         |       Y        |          -          |           Servlet-api           |
|      Runtime       |         -         |       Y        |          Y          |          JDBC驱动实现           |
|       System       |         Y         |       Y        |          -          | 本地的，maven仓库之外的类库文件 |

### 依赖传递

依赖传递的概念很好理解，熟悉 spring 的朋友应该都知道，`spring-core` 依赖于 `commons-logging`，如果 `account-email` 依赖于 `spring-core`，那么 `account-email` 就会间接依赖于 `commons-logging`

![maven dependency transitive](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/maven/maven-dependency-translate.png)

有了传递性依赖机制，在使用 Spring 时就不用去考虑它依赖了什么，也不用担心引入多余的依赖。 Maven 会解析各个直接依赖的 POM， 将那些必要的间接依赖，以传递性依赖的形式引入到当前的项目中。

这里会涉及到传递性依赖和依赖范围，可以直接看 《maven实战》5.6 节。

### 依赖调解和可选依赖

依赖调解即发生依赖冲突时使用 maven 自带的原则进行调解：路径最近者优先和第一声明者优先。

可选依赖即 `optional`，该字段表明此依赖不会传递，该字段试情况使用。



### 最佳实践

#### 排除依赖

有些时候，一些依赖会引入 SNAPSHOT 版本的传递依赖，此时可以使用 `exclusions` 标签进行排除，并且自己重新引入这个依赖，指定其版本。

```xml
<dependency>
  <groupId>com.minmin.mvndemo.account</groupId>
  <artifactId>project-b</artifactId>
  <version>1.0.0</version>
  <exclusions>
    <exclusion>
      <groupId>com.minmin.mvndemo.account</groupId>
      <artifactId>project-c</artifactId>
    </exclusion>
  </exclusions>
</dependency>

<dependency>
  <groupId>com.minmin.mvndemo.account</groupId>
  <artifactId>project-c</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### 归类依赖

将同一个项目下的模块进行归类，即将 `version` 抽取到 `properties` 字段中，方面统一管理。

```xml
<prerequisites>
  <springframework.version>5.1.1</springframework.version>
</prerequisites>
```

#### 优化依赖

maven 会自动解析所有项目的直接依赖和传递性依赖，并且根据规则正确判断每个依赖的范围，对于一些依赖冲突，也能进行调节，确保任何一个构件只有唯一的版本在依赖中存在。

我们可以通过 `mvn dependency:list` 和 `mvn dependency:tree` 来查看当前已解析依赖，并且可以使用 `mvn dependency:analyze` 来分析依赖情况，输出如下：

```xml
[WARNING] Used undeclared dependencies found:
[WARNING]    org.springframework:spring-context:jar:2.5.6:compile
[WARNING] Unused declared dependencies found:
[WARNING]    org.springframework:spring-core:jar:2.5.6:compile
[WARNING]    org.springframework:spring-beans:jar:2.5.6:compile
```

对于 `Used undeclared dependencies`，属于项目中用到，但是没有显示说明，这种需要将其添加到依赖中，这里的依赖是由直接依赖引进的，当直接依赖改变，导致传递性依赖改变时有可能导致当前项目出错，并且这种改变难以感知。

对于`Unused declared dependencies`，也需要谨慎对待，因为该命令只是对编译主代码和测试代码时进行分析，一些执行测试和运行时需要的依赖它就发现不了。

# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [POM Reference](https://maven.apache.org/pom.html#pom-reference)
3. [Maven总结之Pom.xml解析（五）上篇](https://sq.163yun.com/blog/article/170717137729937408)
4. [Maven总结之Pom.xml解析（五）中篇](https://sq.163yun.com/blog/article/170718066311094272)
5. [Maven总结之Pom.xml解析（五）下篇](https://sq.163yun.com/blog/article/170718516808704000)

