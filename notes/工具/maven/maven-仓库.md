# maven仓库

maven 世界中的坐标和依赖是任何一个构件的逻辑表示方法，而构件的物理表示方法则为文件，maven 通过仓库来统一管理这些文件。

## 仓库的分类

对于 maven 来说，仓库分为两类：本地仓库和远程仓库，maven 会先寻找本地仓库是否有对应的构建，如果没有则去远程仓库寻找，再没有则报错。

## 私服

一般公司内部都会配置 maven  的私服，私服的好处有很多，最大的优点在于可以节省网络带宽并且可以在项目中使用内部构件，而且也提高了构建项目的稳定性，比较主流的搭建私服的技术为 Nexus。

## 远程仓库

### 配置远程仓库

远程仓库，先从最核心的中央仓库开始，**中央仓库是默认的远程仓库**，id 为 `central` 除了中央仓库，还有其它很多公共的远程仓库，如中央仓库的镜像仓库。在很多情况下，默认的中央仓库无法满足项目的需求，可能项目需要的构件存在于另一个远程仓库中，如 JBoss Maven 仓库，此时可以在 POM 中通过 `<repository>` 字段配置远程仓库，具体配置如下，可以参考 《maven实战》

```xml
<repositories>  
<repository>  

   <!--仓库唯一标识 -->  
  <id>repoId </id>  

   <!--远程仓库名称  -->  
  <name>repoName</name>  

    <!--远程仓库URL，如果该仓库配置了镜像，这里的URL就没有意义了，因为任何下载请求都会交由镜像仓库处理，前提是镜像（也就是设置好的私服）需要确保该远程仓库里的任何构件都能通过它下载到  -->  
  <url>http://……</url>  

   <!--如何处理远程仓库里发布版本的下载 -->  
  <releases>  

      <!--true或者false表示该仓库是否为下载某种类型构件（发布版，快照版）开启。   -->  
    <enabled>false</enabled>  

      <!-- 该元素指定更新发生的频率。Maven会比较本地POM和远程POM的时间戳。这里的选项是：-->  
      <!-- always（一直），daily（默认，每日），interval：X（这里X是以分钟为单位的时间间隔），或者never（从不）。  -->  
    <updatePolicy>always</updatePolicy>  

      <!--当Maven验证构件校验文件失败时该怎么做:-->  
      <!--ignore（忽略），fail（失败），或者warn（警告）。 -->  
    <checksumPolicy>warn</checksumPolicy>  

  </releases>  

   <!--如何处理远程仓库里快照版本的下载，与发布版的配置类似 -->  
  <snapshots>       
    <enabled/>  
    <updatePolicy/>  
    <checksumPolicy/>        
  </snapshots>  

</repository>      
</repositories>
```

### 远程仓库认证

一般使用远程仓库时还需要对远程仓库进行认证，认证的信息保存在 `settings.xml` 中，配置信息如下，一般会有管理员提供。

```xml
<settings>
  <servers>
    <server>
      <id>my-proj</id>
      <username>repo-user</username>
      <password>repo-pwd</password>
    </server>
  </servers>
</settings>
```

这里需要注意 `id` 的值必须于 `POM` 文件中 `repository` 元素中的 `id` 完全一致。

### 部署至远程仓库

私服的一大作用时部署第三方构件，包括组织内部生成的构件以及 一些无法从外部仓库直接获取的构件，都需要部署到远程仓库中，让其他成员使用。

此时我们可以通过编辑 `POM` 文件，配置 `distributionManagement` 定义要部署到远程仓库。

```xml
<project>
  <distributionManagement>
    <repository>
      <id>proj-release</id>
      <name>Proj Release Repository</name>
      <url>http://192.168.1.100/content/repositories/proj-release</url>
    </repository>
    <snapshotRepository>
      <id>proj-snapshots</id>
      <name>Proj Snapshots Repository</name>
      <url>http://192.168.1.100/content/repositories/proj-snapshot</url>
    </snapshotRepository>
  </distributionManagement>
</project>
```

`distributionManagement` 中包含的`repository `和  `snapshotRepository`，前者表示发布构件的仓库，后者表示快照版本的仓库。当然这两个部署仓库也需要在 `settings.xml` 中配置相应的认证信息，并且确保 `id` 一致。

上述信息配置晚后可以通过 `mvn clean deploy` 命令来将构件部署到远程仓库中，注意它与 `mvn clean install` 的区别在于，后者只是将构件部署在本地仓库。

### 镜像

如果仓库 X 可以提供仓库 Y 存储的所以内容，那么就可以认为 X 是 Y 的一个镜像，由于私服可以代理多个远程仓库，所以一般镜像可以和私服配合使用，使用一个私服地址就等于使用了所以需要的外部仓库。

```xml
<mirrors>  
    <mirror>  
       <!--该镜像的唯一标识符。id用来区分不同的mirror元素。 -->  
       <id>nexus</id>  
       <!-- 镜像名，起注解作用，应做到见文知意。可以不配置  -->  
       <name>Human Readable Name </name>  
       <!--  所有仓库的构件都要从镜像下载  -->  
       <mirrorOf>*</mirrorOf>  
       <!-- 私服的局域网地址-->  
       <url>http://192.168.0.1:8081/nexus/content/groups/public/</url>  
    </mirror>  
</mirrors>
```

这里推荐几个速度较好的镜像：

```xml
<mirror>
    <id>nexus-163</id>
    <mirrorOf>*</mirrorOf>
    <name>Nexus 163</name>
    <url>http://mirrors.163.com/maven/repository/maven-public/</url>
</mirror>
```

```xml
<mirror>
  <id>nexus-aliyun</id>
  <name>Nexus aliyun</name>
  <url>http://maven.aliyun.com/nexus/content/groups/public/</url> 
  <mirrorOf>central</mirrorOf> 
</mirror>
```

## Reference

1. [《maven实战》](https://book.douban.com/subject/5345682/)
2. [Maven总结之仓库（八）](https://sq.163yun.com/blog/article/170723230942158848)