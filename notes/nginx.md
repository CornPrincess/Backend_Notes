MacOs系统安装nginx几简单使用

nginx作为程序员必备技能，必须学习一下，这篇博文作为记录安装与初步使用nginx学习笔记。



# 安装nginx

官网提供了两种主流安装nginx的方式，分别是Linux系统与FreeBSD系统，由于笔者使用的MacOs系统是基于FreeBSD系统， 可以使用 `pkg` 或者 `port` 安装nginx



我们可以选择 `homebrew` 进行快速安装，对新手很友好。

```bash
# brew install nginx
```



安装成功之后，命令后应该会出现以下的语句：

```bash
Docroot is: /usr/local/var/www

The default port has been set in /usr/local/etc/nginx/nginx.conf to 8080 so that
nginx can run without sudo.

nginx will load all files in /usr/local/etc/nginx/servers/.

To have launchd start nginx now and restart at login:
  brew services start nginx
Or, if you don't want/need a background service you can just run:
  nginx
```



启动nginx

```bash
sudo nginx 
```



nginx默认的端口号为8080，以上命令执行完后在浏览器中打开localhost:8080, 即可看到nginx的默认主页

![](/Users/zhoutianbin/Code/Backend_Nodets/asserts/nginx.png)



# Nginx Hello World

nginx有一个主线程（main process）和多个工作线程（worker process），nginx利用基于事件模型（event-based model）和系统依赖机制（OS-dependent mechanisms），在工作线程间有效地分发请求，主线程用来读取和运行配置，以及维护工作线程，工作线程个数可以在配置文件中定义，也可更加可用的CPU核心数自动变化



## Nginx常用命令

nginx默认的配置文件nginx.conf在`/usr/local/nginx/conf`, `/etc/nginx`, 或`/usr/local/etc/nginx`中

当nginx启动之后，我们可以通过以下命令来控制它。 `ngixn -s <command>`

```bash
stop — fast shutdown
quit — graceful shutdown
reload — reloading the configuration file
reopen — reopening the log files
```

当我们修改nginx的配置文件时，必须reload配置文件或者重启nginx才能生效。



停止nginx进程，可以通过 `ps` 命令来查找 `nginx` 的 `pid`， 也可以打开 `/usr/local/var/run/nginx.pid` 文件查看pid

```bash
ps -ax | grep nginx
kill -s QUIT <pid>
```



## 几个路径

在MacOs10.15.3系统中，用homebrew安装nginx，以下路径需要注意/

`/usr/local/var/run/nginx.pid` 存放nginx运行线程信息

`/usr/local/etc/nginx/nginx.conf` 存放nginx的默认配置文件，如果nginx.conf被改名，nginx将会启动失败

`/usr/local/var/log/nginx` 存放nginx运行日志，包括access.log,  error.log



## Nginx配置文件

nginx安装完毕后，默认的配置文件 `nginx.conf` 会存放在`/usr/local/etc/nginx/nginx.conf` 目录，为了学习配置文件，我们可以先将nginx.conf 重命名为 `nginx.back.conf`，再新建 `nginx.conf` 文件，如下：

```nginx
events {
    worker_connections  1024;
}

http {
    server {
        location / {
             root /data/www;
        }
        location /images/ {
            root /data;
        }
    }
}
```

以上配置文件定义两个路由，需要注意的是路由的匹配规则为**最长路径匹配**： 即若url以 `/images` 开头， `location /` 也能匹配到，但是他不是最长路径，所以会匹配到下面的 `location /images/`



更新以上配置文件（不设置端口号时默认端口为80），并且重启nginx后，

- 访问 `localhost`， nginx将会读取 `/data/www/index.html`， 若不存在将会报404
- 访问localhost/images/test.png， nginx将会读取 `/data/images/test.png`(**这里要注意路径**)，若不存在将会报404



## Nginx配置代理

nginx可以用来配置代理，即当服务器收到请求时将请求先转发给代理服务器，然后将从代理服务器收到的返回信息发送给客户端



client   --->  nginx ---> proxy server

​            <---              <---



我们简单修改之前的配置文件：

```nginx
events {
    worker_connections  1024;
}


http {
    server {
        location / {
            proxy_pass http://localhost:8080;
        }
        location ~ \.(gif|jpg|png)$ {
            root /data/images;
        }
    }

    server {
        listen 8080;
        root /data/proxy;

        location / {
        }
    }
} 
```



- `Proxy_paas`：设置对应的代理服务器URL
- `~ \.(gif|jpg|png)$ `：正则表达式，将以 `.gif`, `.jpg`, `png`结尾的请求转到 `/data/images` 路径



# 参考

[Beginner’s]: http://nginx.org/en/docs/beginners_guide.html
[Installing Nginx in Mac OS X Maverick With Homebrew]: https://medium.com/@ThomasTan/installing-nginx-in-mac-os-x-maverick-with-homebrew-d8867b7e8a5a

