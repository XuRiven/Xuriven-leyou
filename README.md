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

**跨域：ajax请求才会出现跨域，当一个请求url的协议、域名、端口三者之间任意一个与当前页面url不同即为跨域,浏览器对于javascript的同源策略的限制 。**

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

# 3.品牌的新增

## 3.1.表单提交

在submit方法中添加表单提交的逻辑：

```js
submit() {
    console.log(this.$qs);
    // 表单校验
    if (this.$refs.myBrandForm.validate()) {
        // 定义一个请求参数对象，通过解构表达式来获取brand中的属性{categories letter name image}
        const {categories, letter, ...params} = this.brand; // params:{name, image, cids, letter}
        // 数据库中只要保存分类的id即可，因此我们对categories的值进行处理,只保留id，并转为字符串
        params.cids = categories.map(c => c.id).join(",");
        // 将字母都处理为大写
        params.letter = letter.toUpperCase();
        // 将数据提交到后台
        // this.$http.post('/item/brand', this.$qs.stringify(params))
        this.$http({
            method: this.isEdit ? 'put' : 'post',
            url: '/item/brand',
            data: params
        }).then(() => {
            // 关闭窗口
            this.$emit("close");
            this.$message.success("保存成功！");
        })
            .catch(() => {
            this.$message.error("保存失败！");
        });
    }
}
```

1. 通过`this.$refs.myBrandForm`选中表单，然后调用表单的`validate`方法，进行表单校验。返回boolean值，true代表校验通过
2. 通过解构表达式来获取brand中的值，categories需要处理，单独获取。其它的存入params对象中
3. 品牌和商品分类的中间表只保存两者的id，而brand.categories中保存的是对象数组，里面有id和name属性，因此这里通过数组的map功能转为id数组，然后通过join方法拼接为字符串
4. 发起请求
5. 弹窗提示成功还是失败，这里用到的是我们的自定义组件功能message组件

## 3.2.后台实现新增

### 3.2.1.controller

还是一样，先分析四个内容：

- 请求方式：POST
- 请求路径：/brand
- 请求参数：brand对象，外加商品分类的id数组cids
- 返回值：无，只需要响应状态码

代码：

```java
    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids){
        this.brandService.saveBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
```



### 3.2.2.Service

这里要注意，我们不仅要新增品牌，还要维护品牌和商品分类的中间表。

```java
    /**
     * 新增品牌
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {

        // 先新增brand
        this.brandMapper.insertSelective(brand);

        // 在新增中间表
        cids.forEach(cid -> {
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        });
    }
```

这里调用了brandMapper中的一个自定义方法，来实现中间表的数据新增

### 3.2.3.Mapper

通用Mapper只能处理单表，也就是Brand的数据，因此我们手动编写一个方法及sql，实现中间表的新增：

```java
public interface BrandMapper extends Mapper<Brand> {

    /**
     * 新增商品分类和品牌中间表数据
     * @param cid 商品分类id
     * @param bid 品牌id
     * @return
     */
    @Insert("INSERT INTO tb_category_brand(category_id, brand_id) VALUES (#{cid},#{bid})")
    int insertBrandAndCategory(@Param("cid") Long cid, @Param("bid") Long bid);
}
```

### 3.2.4.测试

![](D:\IDEAWorkspace\Xuriven-leyou\assets\1532827997361.png)

400：请求参数不合法



## 3.3.解决400

### 3.3.1.原因分析

我们填写表单并提交，发现报错了。查看控制台的请求详情：

![](D:\IDEAWorkspace\Xuriven-leyou\assets\1530696121642.png)

发现请求的数据格式是JSON格式。

> 原因分析：

**axios处理请求体的原则会根据请求数据的格式来定：**

- **如果请求体是对象：会转为json发送，如果发送的时json数据，后台controller方法中只能写一个对象来接收，不能写多个，然而brand对象中又没有cids属性，所以只能通过qs将前台传过来的参数进行转换。**

- **如果请求体是String：会作为普通表单请求发送，但需要我们自己保证String的格式是键值对。**

  **如：name=jack&age=12**

### 3.3.2.QS工具

QS是一个第三方库，我们可以用`npm install qs --save`来安装。不过我们在项目中已经集成了，大家无需安装

**这个工具的名字：QS，即Query String，请求参数字符串。**

**什么是请求参数字符串？例如： name=jack&age=21**

**QS工具可以便捷的实现 JS的Object与QueryString的转换。**

在我们的项目中，将QS注入到了Vue的原型对象中，我们可以通过`this.$qs`来获取这个工具：

![](D:\IDEAWorkspace\Xuriven-leyou\assets\1539821449329.png)

### 3.3.3.解决问题

修改页面，对参数处理后发送：

![](D:\IDEAWorkspace\Xuriven-leyou\assets\1545223244002.png)

然后再次发起请求，发现请求成功：![](D:\IDEAWorkspace\Xuriven-leyou\assets\1530698685973.png)

