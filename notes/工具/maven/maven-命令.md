# mvn compile

编译源代码，一般编译模块下的src/main/Java目录。

在执行此命令前可以先执行 `maven clean`， 默认情况下 maven 所构建的所有输出都在 target 目录中，maven compile 会执行以下操作：

- Resources: resources
- Compiler: compile



# mvn test

测试命令,或执行src/test/java/下junit的测试用例.

执行过程：

- Resources: resources 主资源处理
- Compiler: compile 主代码编译
- resources： testResources 测试资源处理
- compiler：testCompile 测试代码编译



# mvn package

maven 中默认打包类型为 jar 包，

执行过程：

- mvn test
- mvn package

我们可以看到运行完这条命令后，会在 target 文件夹中生成 `$(artifactId)-$(version).jar` ，至此我们就得到了项目的输出，如果有需要的话可以将这个 jar 包放入其他项目的 Classpath 中，从而就可以使用这个包

# mvn install

先进行 `mvn package` 操作，生成对应的 jar/war包，然后将打包的jar/war文件复制到你的本地仓库中,供其他模块使用

执行过程：

- mvn package
- mvn install



# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [Maven总结之入门指南（一）](

