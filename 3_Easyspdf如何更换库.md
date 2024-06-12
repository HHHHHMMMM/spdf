# Easyspdf如何更换库

Easyspdf采用了Jpa+hibernate方式，此种方式针对业务逻辑较为简单的系统比较便捷，甚至无需自行建表。本项目默认采用H2库。

将以Mysql为例，告知如何更换库。

1. `build.gradle`文件增加mysql驱动依赖

```
dependencies {
   ......
    implementation 'mysql:mysql-connector-java:8.0.33'
  ....
  }
```

2. application.properties增加mysql配置

```
####################Mysql configuration start ##############
spring.datasource.url=jdbc:mysql://localhost:3307/easy_spdf_db?useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useUnicode=true&allowPublicKeyRetrieval=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=username
spring.datasource.password=ENC(password)
jasypt.encryptor.password=username
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
####################Mysql configuration end ##############
```

