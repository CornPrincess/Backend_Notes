## session与cookie

Seesion有两种理解，一种是单纯地从编程中的对象的角度来理解，另一种是从广义的概念上理解，即一个session代表了浏览器和用户之间的一次对话。



SSO单点登录，一次登录，其他的产品也可以同时登录，背后的原理为：

第一次 登陆的时候服务器会下发一个token或者sessionID，并且会把这一串码存在存储介质中，可能是数据库或者一个服务或者redis，以达到sessionID共享，当你以后去登录其他的产品网站时，你会带上这一串码，对应的后端此时就会通过sessionID共享读取这一串码的信息以识别你的身份。

## cookie泄漏

首先浏览器是很安全的，除非有xss漏洞，否则不会被读取cookie，cookie只能被网站后台读取，如果后台没有对外的请求那么久不会泄漏。

csrftoken 跨域