# 默认插件

## maven-compiler-plugin

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.8.1</version>
  <configuration>
  <source>1.8</source>
  <target>1.8</target>
  </configuration>
</plugin>
```



# maven 常用插件

## Apache Maven Shade Plugin

默认打包生成的 jar 包是不能直接运行的，因为带有 main 方法的类信息不会添加到 mainfest 中（打开 jar 文件中的 META-INF/MANIFEST.MF文件， 无法看到 Main-Class 一行）。为了生成可执行的 jar 文件，需要借助 maven-shade-plugin， 配置如下：

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.2.3</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals>
      	<goal>shade</goal>
      </goals>
      <configuration>
        <transformers>
          <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
          	<mainClass>org.sonatype.haven.HavenCli</mainClass>
          </transformer>
        </transformers>
      </configuration>
    </execution>
  </executions>
</plugin>
```

上述的 xml 中我们可以看到，在打包时，我们配置了 mainClass 的信息，在打包过程中这一信息会放入 MANIFEST.MF中

当我们执行 `mvn clean package` 完毕后，可以在 target 目录下看到两个 jar 包， origin 开头的为原始 jar 包， 而另一个为添加了 mainClass 信息的 可执行 jar 包。

# Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [Maven总结之插件管理（三）](https://sq.163yun.com/blog/article/170713335421394944)
3. [Apache Maven Shade Plugin](http://maven.apache.org/plugins/maven-shade-plugin/)

