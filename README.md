# 若依框架整合微信小程序

## 依赖

+ [RuoYi-Vue 若依框架前后端分离版 v3.8.9](https://gitee.com/y_project/RuoYi-Vue)
+ [WxJava 微信sdk](https://github.com/binarywang/WxJava)

## 整合功能

+ 微信小程序登录。
+ 微信用户信息同步。
+ 微信小程序支付。

## 整体设计

+ 新增`微信小程序用户体系`：新增用户表，区别于若依原有的管理后台用户角色权限体系，单独用于保存微信小程序用户。小程序用户仅操作微信小程序，不登录若依后台。
+ 新增`微信小程序后端接口鉴权体系`：新增过滤器，单独用于拦截微信小程序后端模块接口做鉴权。
+ 新增`微信小程序后端模块`。

## 整合改动说明

### 项目模块改动

在根项目，新增了`ruoyi-wxmini`module模块。在根`pom.xml`文件内：

+ `<modules>`标签内，会自动将新增模块纳入管理。增加内容如下。
    ```xml
    <modules>
        <module>ruoyi-wxmini</module>
    </modules>
    ```
+ `<dependencies>`标签内，手动添加新增的模块。增加内容如下。
   ```xml
   <dependency>
       <groupId>com.ruoyi</groupId>
       <artifactId>ruoyi-wxmini</artifactId>
       <version>${ruoyi.version}</version>
   </dependency>
   ```

### ruoyi-admin模块改动

在`ruoyi-admin`模块的`pom.xml`中，`<dependencies>`依赖中手动增加了`ruoyi-wxmini`模块依赖，使得`ruoyi-wxmini`
模块中的`controller`控制器被加载和url生效。增加内容如下。

```xml

<dependency>
    <groupId>com.ruoyi</groupId>
    <artifactId>ruoyi-wxmini</artifactId>
</dependency>
```

### ruoyi-framework模块改动

修改了过滤器配置，修改`com.ruoyi.framework.config.SecurityConfig`对象的`filterChain`
方法，追加匹配规则，对自定义的微信小程序接口url不做登录等鉴权。该部分接口鉴权将交由`ruoyi-wxmini`模块定义的过滤器处理。增加内容如下。

```java
.antMatchers("/wxmini/**").permitAll()
```

### ruoyi-system模块改动

使用了若依自带的代码生成工具，自动生成业务层代码，并放置在`ruoyi-system`模块中，对应`com.ruoyi.wxmini`包。

### 数据库改动

新增微信用户表，执行`sql\wx_user.sql`建表。

### 项目配置文件改动

在`ruoyi-admin`模块里的`application.yml`配置文件，增加了微信对接配置，示例如下。

```yml
wx:
  miniapp:
    configs:
      - appid: test #微信小程序的appid
        secret: test #微信小程序的Secret
        token: #微信小程序消息服务器配置的token
        aesKey: #微信小程序消息服务器配置的EncodingAESKey
        msgDataFormat: JSON
  # 支付配置
  pay:
    appId: test #微信小程序的appid
    mchId: 110 #商户id
    apiV3Key: test #V3密钥
    # https://pay.weixin.qq.com/doc/v3/merchant/4012365345
    # openssl x509 -in apiclient_cert.pem -noout -serial
    certSerialNo: test
    privateKeyPath: classpath:cert/apiclient_key.pem #apiclient_key.pem证书文件的绝对路径或者以classpath:开头的类路径
    privateCertPath: classpath:cert/apiclient_cert.pem #apiclient_cert.pem证书文件的绝对路径或者以classpath:开头的类路径
```
在`ruoyi-admin`模块的`resources`资源文件目录下，增加`cert`目录，放置支付用的证书。

## ruoyi-wxmini模块设计说明

TODO

## 参考
+ [小程序对接demo](https://github.com/binarywang/weixin-java-miniapp-demo)
+ [支付V3版本](https://github.com/binarywang/WxJava/tree/develop/spring-boot-starters/wx-java-pay-spring-boot-starter)