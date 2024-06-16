# Docker部署easyspdf

docker部署easyspdf有几种方式，现罗列在下方。本项目主要在用户登录功能方面使用到了数据库存储。项目默认使用`H2`作为数据存储，但数据库可拓展性较强，可以随时更换库。

1. **ubuntu安装docker**

   ```
   sudo apt-get update
   
   sudo apt-get install apt-transport-https ca-certificates curl software-properties-common
   
   curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg | sudo apt-key add -
   
   sudo add-apt-repository "deb [arch=amd64] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable"
   
   sudo apt-get update
   
   sudo apt-get install docker-ce
   
   sudo systemctl start docker
   
   sudo systemctl enable docker
   ```

   同时创建/etc/docker/daemon.json，更换docker镜像仓库。

   ```
   sudo vim /etc/docker/daemon.json
   ```

   ```
   {
     "registry-mirrors": ["https://registry.docker-cn.com"]
   }
   ```

   ```
   sudo systemctl daemon-reload
   sudo systemctl restart docker
   ```

下面展示mysql和H2两种库的docker部署方式。

### 一、使用H2数据库（开箱即用）

作为一种内嵌式数据库，优点是使用及其方便，甚至省略掉了建库步骤。直接使用镜像即可。

缺点是性能较弱，无法支持大数据量和大并发量。

如果对项目登录操作没有要求，或要求较低、或压根不需要登录操作，那么选择此种方式是最好不过了的，什么都不需要自己改，开箱即用，拉起来就能用。

1. 先拉镜像

```
docker pull registry.cn-hangzhou.aliyuncs.com/sunhm3/easyspdf-full-with-h2:latest
```

2. docker run拉起来

```
docker run -d \
  -p 8080:8080 \
  -v /location/of/trainingData:/usr/share/tessdata \
  -v /location/of/extraConfigs:/configs \
  -v /location/of/logs:/logs \
  -e DOCKER_ENABLE_SECURITY=false \
  -e INSTALL_BOOK_AND_ADVANCED_HTML_OPS=false \
  -e LANGS=en_GB \
  --name easyspdf-full-with-h2 \
  registry.cn-hangzhou.aliyuncs.com/sunhm3/easyspdf-full-with-h2:latest

  Can also add these for customisation but are not required

  -v /location/of/customFiles:/customFiles \
```

**参数解释说明：**

1. `-p 8080:8080`:端口映射，项目使用8080端口
2. trainingData、extraConfigs、logs三个目录均在应用被拉起的时候自动创建。
   * trainingData:OCR语言训练集所在文件夹。如果要使用OCR功能，则该参数必须。
   * extraConfigs: 外部配置文件所在目录。本应用支持使用外部配置文件。该目录下会有两个yml，其中，`custome_setting.yml`是spring应用的外置配置文件，主要用于对spring应用本身的application.properties做一个补充。即，如果该文件为空，则默认以应用内部的application.properties为准。如果该文件和应用内部application.properties均有配置，则以custom_setting.yml为准。`setting.yml`主要对应用做一些自定义的配置，比如是否需要登录、登录限制、应用名称、描述等等，都可以在这里做限制。
   * logs:日志目录，应用日志存放位置。



应用拉起后，浏览器访问http://ip:8080/即可访问应用。初次访问应用有一定概率出现页面加载不全的现象，这是因为项目中使用到了一个字体，该字体比较大，有可能加载起来没有那么快。

**注意**: 如果开启登录操作，那么默认的可用的登录用户名/密码是：admin/stirling。登录进去以后需要强制修改密码。

### 二、使用mysql数据库

​	Mysql库适用于**有登录验证需求**且**预计数据量会比较大**的情况。后期可实现一些复杂查询操作，业务可拓展性比较强。

1. 先拉镜像

   ```
   docker pull registry.cn-hangzhou.aliyuncs.com/sunhm3/easyspdf-full-with-mysql:latest
   ```

2. 自行创建mysql库:登录mysql后，执行：

   ```
   create database easy_spdf_db;
   ```

3. 对数据库密码进行加密，配置步骤请参考 `配置密文加密工具并生成数据库密码密文`

4. 在/location/of/extraConfigs/目录下新建custom_settings.yml中添加如下内容：

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

   ![image-20240615190950063](H:\work\workspace\mantudocs\docs\.vuepress\public\img\image-20240615190950063.png)

4. docker run拉起来

   ```
   docker run -d \
     -p 8080:8080 \
     -v /location/of/trainingData:/usr/share/tessdata \
     -v /location/of/extraConfigs:/configs \
     -v /location/of/logs:/logs \
     -e DOCKER_ENABLE_SECURITY=false \
     -e INSTALL_BOOK_AND_ADVANCED_HTML_OPS=false \
     -e LANGS=en_GB \
     --name easyspdf-full-with-mysql \
     registry.cn-hangzhou.aliyuncs.com/sunhm3/easyspdf-full-with-mysql:latest
   ```

   应用拉起后，浏览器访问http://ip:8080/即可访问应用。初次访问应用有一定概率出现页面加载不全的现象，这是因为项目中使用到了一个字体，该字体比较大，有可能加载起来没有那么快。
