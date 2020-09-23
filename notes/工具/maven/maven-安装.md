Maven 是 APache 开源组织奉献的一个开源项目。 Maven 的本质是一个项目管理工具，将项目开发和管理过程抽象成一个项目对象模型（Project Object Model, POM）。开发人员只需要做一些简单的配置，就可以批量完成项目的构建、报告和文档的生成工作。

回顾现实开发，程序员每一天都有相当一部分时间花在编译、运行单元测试、生成文档、打包和部署中，这一系列的工作，maven 都可以自动化地帮我们完成。

# Maven 的优点

Maven 是一个项目管理和整合工具。**Maven 为开发者提供了一套完整的构建生命周期框架**。开发团队几乎不用花多少时间就能够自动完成工程的基础构建配置，因为 **Maven 使用了一个标准的目录结构和一个默认的构建生命周期**。

最主要优势可以总结以下四点：

- 生命周期管理，便捷的构建过程；
- 依赖管理，方便引入所需依赖 Jar 包；
- 仓库管理，提供统一管理所有 Jar 包的工具；
- 目录结构管理，提供了一套标准的目录结构（基本上所有的web项目，目录结构几乎都是相同的），这里即我们所说的约定由于配置（Convention Over Configuration）

当然还有其他的优点：

- 插件式架构，大量的可重用插件；
- 很方便集成IDE；
- 开源项目都使用Maven

# 安装maven

> Maven 3.3+ require JDK 1.7 or above to execute - they still allow you to build against 1.3 and other JDK versions

安装 maven 前必须先安装 JDK。

##  Unix-based 系统

这些系统包括 Mac OS，Linux 和 FreeBSD 等，步骤如下：

- 安装 JDK， 并在环境变量中设置 `JAVA_HOME`
- 在官网下载对应的压缩包， 如 [ apache-maven-3.6.3-bin.tar.gz](https://mirror.bit.edu.cn/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz)
- 将上述文件解压并放在对应的安装目录: `tar -xvf apache-maven-3.6.3-bin.tar.gz`
- 设置环境变量：
  - `vi ~/.bash_profile`
  - `export M2_HOME=/usr/local/apache-maven-3.6.1`
  - `export PATH=$M2_HOME/bin:$PATH`
  - `source ~/.bash_profile`
- 验证安装是否成功： `mvn -v`

## Windows 系统

- 在官网下载对应的压缩包，如 [ apache-maven-3.6.3-bin.zip](https://mirror.bit.edu.cn/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip)
- 利用解压工具将上述压缩包解压放在指定的安装目录下
- 配置环境变量
- 输入 `mvn -v` 来检查安装是否成功

因为 maven 是利用 Java 运行的，所以在安装时可以设置 `MAVEN_OPTS` 参数（JVM）来使编译过程快一点， 如 ` export MAVEN_OPTS="-Xms256m -Xmx512m"`



# 最佳实践

- 可以将 `maven` 安装目录下的 `conf/settings.xml` 放到 `~/.m2/settings.xml`，以此来在用户范围内限制 `Maven` 的行为。

- 如果公司有安全限制，可以在 `settings.xml` 文件中配置 `<proxy> `

- 国内由于网络原因，我们可以在 `settings.xml` 中配置镜像，如阿里镜像

  ```xml
  <mirror>
    <id>alimaven</id>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
  ```

  

# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [Maven总结之入门指南（一）](https://sq.163yun.com/blog/article/170711046627450880)