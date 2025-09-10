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

### 接口鉴权

#### 请求头
- **请求头Key**: `Wx-Authorization`
- **Token格式**: `Bearer {JWT_TOKEN}`
- **设计原因**: 避免与若依框架默认的`Authorization`请求头冲突，确保两套鉴权体系独立运行

#### Token生成与验证
- **Token生成**: 用户登录成功后，通过`IWxMiniJwtService.createToken(userId)`生成JWT令牌
- **Token验证**: 过滤器`WxMiniJwtFilter`拦截请求，验证Token的有效性和签名
- **用户ID提取**: 从Token中解析出用户ID，用于后续业务逻辑处理

#### 线程上下文传递
- **上下文工具类**: `WxMiniUserContext`使用ThreadLocal存储当前用户ID
- **设置时机**: 在过滤器验证Token成功后，将用户ID设置到线程上下文
- **获取方式**: 业务代码中通过`WxMiniUserContext.getCurrentUserId()`获取当前用户ID
- **清理机制**: 在过滤器finally块中自动清理ThreadLocal，防止内存泄漏

#### 自定义鉴权URL配置
- **拦截路径**: 所有以`/wxmini`开头的请求都会被拦截鉴权
- **白名单路径**: `WxMiniJwtFilter.checkIsExcludeUri(path)`

### 支付模板

#### 核心抽象类：AbsWxPayBaseService
- **泛型设计**: 使用泛型`<P>`支持不同类型的支付请求参数
- **模板方法模式**: 定义了完整的支付流程，子类只需实现具体的业务逻辑
- **并发控制**: 使用`ConcurrentHashMap`实现无锁化的资源占用控制，避免重复订单

#### 支付流程
1. **资源锁定**: 通过`getResourceId()`获取资源ID，使用原子操作避免并发创建重复订单
2. **业务核验**: `checkBeforeCreatOrder()`进行业务前置检查
3. **参数构建**: `buildOrderParam()`构建微信支付所需参数
4. **订单创建**: 调用微信支付API创建订单
5. **数据保存**: `saveOrderInfo()`保存订单信息到数据库
6. **结果返回**: 返回支付参数给前端

#### 模板优势
- **高并发支持**: 无锁化的并发控制，避免线程阻塞
- **业务隔离**: 通过泛型和抽象方法，支持不同业务场景的支付需求
- **流程标准化**: 统一的支付流程，减少重复代码
- **扩展性强**: 新增支付业务只需继承基类并实现抽象方法

## 参考
+ [微信官方文档-小程序登录](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html)
+ [小程序对接demo](https://github.com/binarywang/weixin-java-miniapp-demo)
+ [支付V3版本](https://github.com/binarywang/WxJava/tree/develop/spring-boot-starters/wx-java-pay-spring-boot-starter)
