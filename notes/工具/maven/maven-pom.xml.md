Maven 中的 pom.xml 文件是我们平时接触最多的文件，但在最近的工作中经常发现有些基础的概念不明白而导致项目最终不能构建成功

> POM stands for "Project Object Model". It is an XML representation of a Maven project held in a file named `pom.xml`. When in the presence of Maven folks, speaking of a project is speaking in the philosophical sense, beyond a mere collection of files containing code. A project contains configuration files, as well as the developers involved and the roles they play, the defect tracking system, the organization and licenses, the URL of where the project lives, the project's dependencies, and all of the other little pieces that come into play to give code life. <sup>[2]</sup>

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
- version: 选择的 Maven 项目的版本（**dependency version requirement specification**
- classifier：



`<Dependency>` 中的 `<scope>test</scope>` 表示依赖只对测试有效，即在测试代码中可以导入，但是在主代码中不能导入，会编译失败。**如果不写，默认是 `<scope>compile</scope>` ，即测试和主代码都有效。**



# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [POM Reference](https://maven.apache.org/pom.html#pom-reference)
3. [Maven总结之Pom.xml解析（五）上篇](https://sq.163yun.com/blog/article/170717137729937408)
4. [Maven总结之Pom.xml解析（五）中篇](https://sq.163yun.com/blog/article/170718066311094272)
5. [Maven总结之Pom.xml解析（五）下篇](https://sq.163yun.com/blog/article/170718516808704000)