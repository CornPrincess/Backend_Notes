# 常用命令

- `mvn help:system`: 该命令会打印出所有 Java 系统属性和环境变量
- `mvn dependency:list`：查看当前已解析依赖。
- `mvn dependency:tree`：查看当前已解析依赖树。
- `mvn dependency:analyze`：分析当前项目的依赖情况
- `mvn clean install-U`：使用 `-U` 参数 ，强制让maven 更新本地镜像
- `mvn help:describe -Dplugin=compiler -Ddetail` 命令可以查看插件的具体信息，包括该插件默认绑定的生命周期阶段
- `mvn -h` 可以查看支持的参数
- `-am, -and, -pl, -rf` 参数可以在构建多模块项目时只构建指定的模块或者对模块进行裁剪，具体用例如下：
  - `mvn install -pl account-email,account-persist` 构建指定的模块
  - `mvn install -pl account-email -am` 同时构建所列模块的依赖模块
  - `mvn install -pl account-parent -amd` 同时构建依赖于所列模块的模块
  - `mvn install -rf account-email` 在完整的反应堆构建顺序技术上指定从哪个模块开始构建

# 常用配置

- 如果公司有需要或者无法访问公共的中央 maven 仓库（ping maven.org 失败）可以通过 `settings.xml` 中的 `proxy` 标签进行设置代理。

- 可以根据电脑配置设置 maven 运行时的 JVM 参数： ` export MAVEN_OPTS="-Xms256m -Xmx512m"`


# Debug

- 不要使用 IDE 自带的 maven，因为可能与自己安装的版本不一致，造成构建错误。
- 当执行 maven 命令失败时可以在后面带上 `-X` 或 `-e` 参数来查看堆栈信息

# 常见错误

**Q**: Source option 5 is no longer supported. Use 7 or later.

**A**: 出现此类错误，一般都是因为 Java 语言版本选择不对，可以在 POM 文件中加入以下代码解决

 ```xml
<properties>
  <maven.compiler.source>1.8</maven.compiler.source>
  <maven.compiler.target>1.8</maven.compiler.target>
</properties>
 ```

或者可以加入如下插件：

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <source>1.8</source>
        <target>1.8</target>
      </configuration>
    </plugin>
  </plugins>
</build>
```



**Q**: 需要用 maven 打成一个可执行的 jar 包

**A**： 使用 shade 插件：

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.2.4</version>
      <executions>
        <execution>
          <goals>
            <goal>shade</goal>
          </goals>
          <configuration>
            <transformers>
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <manifestEntries>
                  <Main-Class>${app.main.class}</Main-Class>
                  <X-Compile-Source-JDK>${maven.compile.source}</X-Compile-Source-JDK>
                  <X-Compile-Target-JDK>${maven.compile.target}</X-Compile-Target-JDK>
                </manifestEntries>
              </transformer>
            </transformers>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

# 常用插件

[Maven总结之插件管理](https://sq.163yun.com/blog/article/170713335421394944)

