# Maven生命周期

我们在开发项目的时候，不断地在编译、测试、打包、部署等过程，**maven的生命周期就是对所有构建过程抽象与统一**，生命周期包含项目的清理、初始化、编译、测试、打包、集成测试、验证、部署、站点生成等几乎所有的过程。

Maven 的生命周期是抽象的，其实际行为都是由插件来完成，如 `package` 阶段的任务可能就会由 `maven-jar-plugin` 来完成，生命周期和插件两者协同工作，密不可分，这种思想与设计模式中的**模版方法**非常相似。

生命周期抽象了构建的各个步骤，定义了他们的次序，但是没有提供具体实现，其中具体的实现由插件来完成，每个够爱你步骤都可以绑定一个或者多个插件行为，而且 Maven 为大多数构建步骤编写并绑定了默认插件。

## 三套生命周期

Maven 有三套**相互独立**的生命周期，每个生命周期包含一些阶段（Phase），并且这些阶段是有顺序的，后面的阶段依赖于前面的阶段，用户和 Maven 最直接的交互方式就是调用这些生命周期阶段。相互独立体现在用户 可以单独调用一个生命周期的某个阶段，而不会对其他生命周期产生影响。

### clean 生命周期

| Clean生命周期阶段 | 完成工作                              |
| ----------------- | ------------------------------------- |
| pre-clean         | 执行一些需要在clean之前完成的工作     |
| clean             | 移除所有上一次构建生成的文件          |
| post-clean        | 执行一些需要在clean之后立刻完成的工作 |

命令“mvn clean”中的就是代表执行上面的clean阶段，**在一个生命周期中，运行某个阶段的时候，它之前的所有阶段都会被运行，也就是说，“mvn clean” 等同于 “mvn pre-clean clean” ，如果我们运行“mvn post-clean” ，那么 “pre-clean”，“clean” 都会被运行**。这是Maven很重要的一个规则，可以大大简化命令行的输入。

### default 生命周期

**Maven最重要就是的Default生命周期，也称构建生命周期，绝大部分工作都发生在这个生命周期中**，每个阶段的名称与功能如下：

| Default生命周期阶段     | 完成工作                                                     |
| ----------------------- | ------------------------------------------------------------ |
| validate                | 验证项目是否正确，以及所有为了完整构建必要的信息是否可用     |
| initialize              | 初始化构建状态，比如设置属性，创建目录等                     |
| generate-sources        | 生成所有需要包含在编译过程中的源代码                         |
| process-sources         | 处理源代码，比如过滤一些值                                   |
| generate-resources      | 生成所有需要包含在打包过程中的资源文件                       |
| process-resources       | 复制并处理资源文件至目标目录，准备打包                       |
| compile                 | 编译项目的源代码                                             |
| process-classes         | 后处理编译生成的文件，例如对Java类进行字节码增强（bytecode enhancement） |
| generate-test-sources   | 生成所有包含在测试编译过程中的测试源码                       |
| process-test-sources    | 处理测试源码，比如过滤一些值                                 |
| generate-test-resources | 生成测试需要的资源文件                                       |
| process-test-resources  | 复制并处理测试资源文件至测试目标目录                         |
| test-compile            | 编译测试源码至测试目标目录                                   |
| process-test-classes    | 后处理编译测试生成的文件，例如对Java类进行字节码增强（bytecode enhancement） |
| test                    | 使用合适的单元测试框架运行测试。这些测试应该不需要代码被打包或发布 |
| prepare-package         | 在真正的打包之前，执行一些准备打包必要的操作                 |
| package                 | 将编译好的代码打包成可分发的格式，如JAR，WAR，或者EAR        |
| pre-integration-test    | 执行一些在集成测试运行之前需要的动作。如建立集成测试需要的环境 |
| integration-test        | 如果有必要的话，处理包并发布至集成测试可以运行的环境         |
| post-integration-test   | 执行一些在集成测试运行之后需要的动作。如清理集成测试环境。   |
| verify                  | 执行所有检查，验证包是有效的，符合质量规范                   |
| install                 | 安装包至本地仓库，以备本地的其它项目作为依赖使用             |
| deploy                  | 复制最终的包至远程仓库，共享给其它开发人员和项目（通常和一次正式的发布相关） |

### site 生命周期

| Site生命周期阶段 | 完成工作                                                   |
| ---------------- | ---------------------------------------------------------- |
| pre-site         | 执行一些需要在生成站点文档之前完成的工作                   |
| site             | 生成项目的站点文档                                         |
| post-site        | 执行一些需要在生成站点文档之后完成的工作，并且为部署做准备 |
| site-deploy      | 将生成的站点文档部署到特定的服务器上                       |

这里经常用到的是site阶段和site-deploy阶段，用以生成和发布Maven站点，这是Maven相当强大的功能。

## 插件

我们知道，Maven 的核心仅仅定义了抽象的生命周期，具体的任务交由插件完成，插件以独立的构件形式存在。为了实现代码的复用，每个插件都可以实现多个功能，称之为目标（goal），如 `maven-dependency-plugin` 插件有十多个目标，常用的 `mvn dependency: list` 就是其中一个目标。

#### 插件绑定

Maven 的生命周期与插件相互绑定，具体来说，**是生命周期的阶段与插件的目标相互绑定**，如项目编译这一任务，它对应了 `default` 生命周期 `compile` 这一阶段，而 `maven-compiler-plugin` 这一插件的 `compile` 可以完成该任务，将他们绑定，就可以实现项目编译的目的。

![maven goal](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/maven/maven_plugin_goal_compile_example.png)

##### 内置绑定

Maven 在核心为一些主要的生命周期阶段绑定了很多插件的目标，当用户通过命令行调用生命周期阶段的时候，对应的插件目标就会执行相应的任务。

三个生命周期中默认绑定的插件目标可以通过参考2查询得到。

##### 自定义绑定

自定义绑定的简单实用案例：

```xml
 <plugin>
   <groupId>com.mycompany.example</groupId>
   <artifactId>display-maven-plugin</artifactId>
   <version>1.0</version>
   <executions>
     <execution>
       <phase>process-test-resources</phase>
       <goals>
         <goal>time</goal>
       </goals>
     </execution>
   </executions>
 </plugin>
```

#### 插件配置

插件配置也是平常经常会用到的功能

##### 命令行配置

`mvn install -Dmaven.test.skip=true`

##### POM 中插件全局配置

最常用的是配置编译时的 Java 版本

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.1</version>
      <configuration>
        <source>1.8</source>
        <target>1.8</target>
      </configuration>
    </plugin>
  </plugins>
</build>
```



## Reference

1. 《maven实战》
2. [Introduction to the Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
3. [Maven总结之生命周期（二）](https://sq.163yun.com/blog/article/170712610677657600)

