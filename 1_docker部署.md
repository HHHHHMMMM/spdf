# Docker部署easyspdf

docker部署easyspdf有几种方式，现罗列在下方。本项目主要在用户登录功能方面使用到了数据库存储。项目默认使用`H2`作为数据存储，但数据库可拓展性较强，可以随时更换库。



下面展示mysql和H2两种库的docker部署方式。

### 一、使用H2数据库（开箱即用）

作为一种内嵌式数据库，优点是使用及其方便，甚至省略掉了建库步骤。直接使用镜像即可。

缺点是性能较弱，无法支持大数据量和大并发量。

如果对项目登录操作没有要求，或要求较低、或压根不需要登录操作，那么选择此种方式是最好不过了的，什么都不需要自己改，开箱即用，拉起来就能用。

```
docker run -d \
  -p 8080:8080 \
  -v /location/of/trainingData:/usr/share/tessdata \
  -v /location/of/extraConfigs:/configs \
  -v /location/of/logs:/logs \
  -e DOCKER_ENABLE_SECURITY=false \
  -e INSTALL_BOOK_AND_ADVANCED_HTML_OPS=false \
  -e LANGS=en_GB \
  --name stirling-pdf \
  frooodle/s-pdf:latest


  Can also add these for customisation but are not required

  -v /location/of/customFiles:/customFiles \
```



### 二、使用mysql数据库

​	Mysql库适用于**有登录验证需求**且**预计数据量会比较大**的情况。后期可实现一些复杂查询操作，业务可拓展性比较强。

#### 1. 使用外置mysql数据库

​	则在/location/of/extraConfigs/目录下的custom_setting.yml中添加如下内容：

```
jasypt:
  encryptor:
    algorithm: PBEWithMD5AndDES
    iv-generator-classname: org.jasypt.iv.NoIvGenerator
    password: encryption_password
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    password: ENC(password)
    url: jdbc:mysql://xx.xxx.xx.xx:3306/easy_spdf_db?useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useUnicode=true&allowPublicKeyRetrieval=true
    username: username
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

```

**注意** : 请修改上述文件内容的

1. `jasypt.encryptor.password`：这个是jasypt的加密因子，具体内容请参考数据库密码加密.md
2. `spring.datasource.password`：数据库密码
3. ``spring.datasource.url`：将ip或域名补充完整，后面东西都可以不用动
4. `spring.datasource.username`：数据库用户名

​	