# 乐友商城总结

# 1.使用域名访问本地项目

## 1.1.域名解析

一个域名一定会被解析为一个或多个ip。这一般会包含两步：

- 本地域名解析

  浏览器会首先在本机的hosts文件中查找域名映射的IP地址，如果查找到就返回IP ，没找到则进行域名服务器解析，一般本地解析都会失败，因为默认这个文件是空的。

  - Windows下的hosts文件地址：C:/Windows/System32/drivers/etc/hosts
  - Linux下的hosts文件所在路径： /etc/hosts 

  样式：

  ```
  # My hosts
  127.0.0.1 localhost
  ```

- 域名服务器解析

  本地解析失败，才会进行域名服务器解析，域名服务器就是网络中的一台计算机，里面记录了所有注册备案的域名和ip映射关系，一般只要域名是正确的，并且备案通过，一定能找到。

## 1.2.nginx解决端口问题

域名问题解决了，但是现在要访问后台页面，还得自己加上端口：`http://manage.taotao.com:9001`。

这就不够优雅了。我们希望的是直接域名访问：`http://manage.taotao.com`。这种情况下端口默认是80，如何才能把请求转移到9001端口呢？

这里就要用到反向代理工具：Nginx（**启动Nginx目录中不能有中文**）

### 1.2.1.什么是nginx

nginx是一个高性能的web和反向代理服务器，它具有很多优越的特性：

**作为Web服务器：**相比于Apache，Nginx使用更少的资源，支持更多的并发连接，体现更高的效率，这点使Nginx尤其受到虚拟主机提供商的欢迎。

**作为负载均衡服务器：**Nginx既可以在内部直接支持Rails和PHP，也可以支持作为HTTP代理服务器，对外进行服务。

**作为邮件代理服务器：**Nignx同时也是一个非常优秀的邮件代理服务器。



nginx可以作为web服务器，但更多的时候，我们把它作为网关，因为它具备网关必备的功能：

- 反向代理
- 负载均衡
- 动态路由
- 请求过滤

### 1.2.2.nginx作为web服务器

Web服务器分2类：

- web应用服务器，如：
  - tomcat 
  - resin
  - jetty
- web服务器，如：
  - Apache 服务器 
  - Nginx
  - IIS  

**区分：web服务器不能解析jsp等页面，只能处理js、css、html等静态资源。**
**并发：web服务器的并发能力远高于web应用服务器。**

**Nginx和Zuul都可以作为负载均衡，有何区别：**本质上不是同一个东西，但都可以作为负载均衡，zuul是在程序的内部做负载均衡，而nginx是在更前面，在浏览器访问zuul网关的时候需要负载均衡Nginx。



### 1.2.3.nginx作为反向代理

什么是反向代理？

- 代理：通过客户机的配置，实现让一台服务器代理客户机，客户的所有请求都交给代理服务器处理。
- 反向代理：用一台服务器，代理真实服务器，用户访问时，不再是访问真实服务器，而是代理服务器。

nginx可以当做反向代理服务器来使用：

- 我们需要提前在nginx中配置好反向代理的规则，不同的请求，交给不同的真实服务器处理
- 当请求到达nginx，nginx会根据已经定义的规则进行请求的转发，从而实现路由功能

利用反向代理，就可以解决我们前面所说的端口问题，如图

![1526016663674](D:\IDEAWorkspace\Xuriven-leyou\assets\1526016663674.png)

Nginx配置文件：

```
 server {
        listen       80; //监听端口
        server_name  manage.leyou.com;  服务名称

        proxy_set_header X-Forwarded-Host $host;
		proxy_set_header X-Forwarded-Server $host;
		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
		
		location / {
			proxy_pass http://127.0.0.1:9001;  转发代理路径
			proxy_connect_timeout 600;
			proxy_read_timeout 600;
		}
    }
```

Nginx代理过程：

* 浏览器准备发起请求，访问http://mamage.leyou.com，但需要进行域名解析

* 优先进行本地域名解析，因为我们修改了hosts，所以解析成功，得到地址：127.0.0.1

* 请求被发往解析得到的ip，并且默认使用80端口：http://127.0.0.1:80

  本机的nginx一直监听80端口，因此捕获这个请求

* nginx中配置了反向代理规则，将manage.leyou.com代理到127.0.0.1:9001，因此请求被转发

* 后台系统的webpack server监听的端口是9001，得到请求并处理，完成后将响应返回到nginx

* nginx将得到的结果返回到浏览器

# 2.跨域问题

跨域：浏览器对于javascript的同源策略的限制 。

以下情况都属于跨域：

| 跨域原因说明       | 示例                                   |
| ------------------ | -------------------------------------- |
| 域名不同           | `www.jd.com` 与 `www.taobao.com`       |
| 域名相同，端口不同 | `www.jd.com:8080` 与 `www.jd.com:8081` |
| 二级域名不同       | `item.jd.com` 与 `miaosha.jd.com`      |

如果**域名和端口都相同，但是请求路径不同**，不属于跨域，如：

`www.jd.com/item` 

`www.jd.com/goods`

http和https也属于跨域

而我们刚才是从`manage.leyou.com`去访问`api.leyou.com`，这属于二级域名不同，跨域了。

## 2.1.为什么会有跨域问题

跨域不一定都会有跨域问题。

因为跨域问题是浏览器对于ajax请求的一种安全限制：**一个页面发起的ajax请求，只能是与当前页域名相同的路径**，这能有效的阻止跨站攻击。

因此：**跨域问题 是针对ajax的一种限制**。

但是这却给我们的开发带来了不便，而且在实际生产环境中，肯定会有很多台服务器之间交互，地址和端口都可能不同，怎么办？

## 2.2.cors解决跨域

事实上，SpringMVC已经帮我们写好了CORS的跨域过滤器：CorsFilter ,内部已经实现了刚才所讲的判定逻辑，我们直接用就好了。

在`leyou-gateway`中编写一个配置类，并且注册CorsFilter：

```java
@Configuration
public class LeyouCorsConfiguration {
    @Bean
    public CorsFilter corsFilter(){
        //初始化cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许跨域的域名，如果要携带cookie，不能写*。 *:代表所有域名都可以跨域访问
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");
        corsConfiguration.setAllowCredentials(true); //允许携带cookie
        corsConfiguration.addAllowedMethod("*"); //代表所有的请求方法:GET,POST,Delete
        corsConfiguration.addAllowedHeader("*"); //允许携带任何头信息

        //初始化cors配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);

        //返回corsFilter对象,参数:cors配置源对象
        return new CorsFilter(configurationSource);
    }
}
```

