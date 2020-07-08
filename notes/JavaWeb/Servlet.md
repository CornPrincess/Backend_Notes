servlet 没有 main 方法，他们需要 container 来进行统一管理，其中一个 container 就是 tomcat

- [ ] 补图 what is a container

如果没有 servlet 和 container 要你自己来实现一个web应用，你只有JavaSE， 此时你需要自己实现哪些功能

- [ ] servlet的前世今生

> When you deploy your servlet into your web Container, you’ll create a fairly simple XML document called the Deployment Descriptor (DD) to tell the Container how to run your servlets and JSPs.



> Q: So Tomcat is a standalone web Container... does that mean there are standalone EJB Containers?A:A: In the old days, say, the year 2000, you could find complete J2EE application servers, standalone web Containers, and standalone EJB Containers. But today, virtually all EJB Containers are part of full J2EE servers, although there are still a few standlone web Containers, including Tomcat and Resin. Standalone web Containers are usually configured to work with an HTTP web server (like Apache), although the Tomcat Container does have the ability to act as a basic HTTP server. But for HTTP server capability, Tomcat is not nearly as robust as Apache, **so the most common non-EJB web apps usually use Apache and Tomcat configured together—with Apache as the HTTP web Server, and Tomcat as the web Container.Some of the most common J2EE servers are BEA’s WebLogic, the open source JBoss AS, and IBM’s WebSphere.**

