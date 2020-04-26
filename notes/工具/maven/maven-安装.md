Maven 是 APache 开源组织奉献的一个开源项目。 Maven 的本质是一个项目管理工具，将项目开发和管理过程抽象成一个项目对象模型（POM）。开发人员只需要做一些简单的配置，就可以批量完成项目的构建、报告和文档的生成工作。



回顾现实开发，程序员每一天都有相当一部分时间花在编译、运行单元测试、生成文档、打包和部署中，这一系列的工作，maven 都可以自动化地帮我们完成。

# 安装maven

> Maven 3.3+ require JDK 1.7 or above to execute - they still allow you to build against 1.3 and other JDK versions

安装 maven 前必须先安装 JDK。

##  Unix-based 系统， 如 MacOs 系统

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



因为 maven 是利用 Java 运行的，所以在安装时可以设置 `MAVEN_OPTS` 参数（JVM）来使编译过程快一点， 如 ` export MAVEN_OPTS="-Xms256m -Xmx512m"`



# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [Maven总结之入门指南（一）](https://sq.163yun.com/blog/article/170711046627450880)