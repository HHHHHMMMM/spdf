#  EasySpdf本地部署

**注意：本地部署必须要有网络来下载必要的依赖和组件**

要在没有Docker/Podman的情况下运行应用程序，可能需要手动安装所有依赖项并构建必要的组件。

请注意，某些依赖项可能在所有Linux发行版的标准存储库中都不可用，并且可能需要额外的安装步骤。

下面的指南假设您对在linux操作系统中使用命令行界面有基本的了解。

它应该可以在大多数Linux发行版和MacOS上工作。

对于Windows，可能需要使用Windows Subsystem For Linux (WSL)来完成某些步骤。如果你的发行版有旧的或者不是所有的包，理论上你可以使用一个Distrobox/Toolbox，或者使用Docker容器。

## 1. 系统参数

1. 4核(vCPU)32 GiB服务器，系统Ubuntu 22.04 64位 UEFI版，一个基本ESSD云盘(正常nas也可)，**要有基本网络可以下载依赖。**
2. mysql8.0(首页登录使用)

## 2. 步骤

### 1. 步骤1

安装如下软件/依赖，如果本机之前没有安装过的话。

- Java 17 or later (21 recommended)
- Gradle 7.0 or later (included within repo so not needed on server)
- Git
- Python 3.8 (with pip)
- Make
- GCC/G++
- Automake
- Autoconf
- libtool
- pkg-config
- zlib1g-dev
- libleptonica-dev

针对 Debian-based系统，执行如下命令：

```
sudo apt-get update
sudo apt-get install -y git  automake  autoconf  libtool  libleptonica-dev  pkg-config zlib1g-dev make g++ openjdk-21-jdk python3 python3-pip
```

### 2. 步骤2：Clone and Build jbig2enc(只针对要使用OCR功能的情况)

针对Debian和Fedora-based系统，你可以使用如下命令对源码进行构建。

```
mkdir ~/.git
cd ~/.git &&\
git clone https://github.com/agl/jbig2enc.git &&\
cd jbig2enc &&\
./autogen.sh &&\
./configure &&\
make &&\
sudo make install
```

**注意：**

1. 如果`git clone`使用http无法下载，可以使用ssh方式，ssh的url为：`git@github.com:agl/jbig2enc.git`
2. 如果报错没有权限读取远程仓库（fatal: Could not read from remote repository），请先ssh-keygen生成key之后，配置到github上。

### 3. 步骤3：安装其他需要添加的软件

接下来我们需要为实现具体功能来安装 LibreOffice for conversions, ocrmypdf for OCR, and opencv。

Install the following software:

- libreoffice-core

- libreoffice-common

- libreoffice-writer

- libreoffice-calc

- libreoffice-impress

- python3-uno

- unoconv

- pngquant

- unpaper

- ocrmypdf

- opencv-python-headless

Debian-based系统执行如下语句：

```
#这里安装了所有libreoffice依赖，如果对磁盘空间有要求，可只安装 #libreoffice-writer libreoffice-calc libreoffice-impress 三个
sudo apt-get install -y libreoffice unpaper ocrmypdf 
#只安装部分libreoffice
#sudo apt-get install -y libreoffice-writer libreoffice-calc libreoffice-impress unpaper ocrmypdf
pip3 install uno opencv-python-headless unoconv pngquant WeasyPrint --break-system-packages
```

**注意** ：pip3安装时可能会报错`no such option: --break-system-packages`,该错误是因为服务器的pip不是最新的，该参数是pip`23.3`版本才引入的，可以尝试更新pip源。解决方案：

1. 查看pip版本

   `pip --version`

2. 更新pip

   ```
   pip install --upgrade pip
   ```

3. 再执行`pip install`

   ```
   pip3 install uno opencv-python-headless unoconv pngquant WeasyPrint --break-system-packages
   ```

   **注意(可选)：**libreoffice有较多版本，可尽量选择升级最新的版本。最新版本在转换性能上有一定提升。截止目前最新版本的libreoffice版本是24.2.3.2。

升级方法：

1. **之前已经使用命令正常安装(已执行，这里不再执行)**

   ```
   sudo apt-get install -y libreoffice unpaper ocrmypdf 
   ```

   然后使用`soffice --version`查看是否是最新版本，截止目前(24.06.09)libreoffice最新版本是24.2.3.

2. 如果不是最新版本，国内源又没有更新最新版本的libreoffice，可以尝试通过官方PPA安装升级最新版本.PPA 提供了LibreOffice 的最新稳定版本，而不是开发版本。 这使其成为在Ubuntu 上获取较新LibreOffice 版本的理想选择。

   ```
   sudo add-apt-repository ppa:libreoffice/ppa
   ```

​		如果报错：`GPG error: http://cz.archive.ubuntu.com/ubuntu xenial InRelease: The following signatures couldn't be verified because the public key is not available:NO_PUBKEY xxx yyy`表明你的 Ubuntu 系统在从 `http://cz.archive.ubuntu.com/ubuntu` 获取软件包时，无法验证某些签名，因为缺少相应的公钥。可以通过以下步骤解决这个问题：

```
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys xxx  yyy #这里的xxx和yyy是你报错里的public_key
sudo apt-get update
sudo add-apt-repository ppa:libreoffice/ppa
```

3. 安装更新

```
sudo apt update && sudo apt install libreoffice
```

如果部署的服务器在国内， 那么官方PPA源下载安装有一定几率比较慢，如果比较慢耐心点等他安装好即可。

3. 安装完成后使用`soffice --version`可以查看版本，如果是24.2.3(最新版本)即可。

### 4. 步骤4：数据库搭建

本项目持久化层使用的JPA+hibernate，更换数据库非常方便。关于如何更换库，请参考：Easyspdf如何更换库。

本项目默认使用`H2`作为数据库。H2数据库具有小巧方便，使用便捷的好处。

#### 4.1 使用H2数据库

由于本项目默认使用的是H2的数据库，故配置上无需调整，甚至无需建库建表，此步骤直接略过即可。

#### 4.2 使用Mysql数据库

Mysql库作为Oracle旗下的关系型数据库，性能上比H2高出很多，支持比较复杂的业务处理。故如果后期需要针对项目做一些业务上复杂改造或者有可预见的大数据量可能性，推荐使用Mysql数据库。

针对本项目，需要提供一个mysql数据库用户、密码、以及一个新建的库。这三者均可以自定义。其中，为了生产数据安全，数据库密码需要参考`2_配置密文加密工具并生成数据库密码密文.md`来进行密码加密。



### 5. 步骤5：Clone and Build Easyspdf

#### 5.1 安装构建工具

1. 下载安装构建工具(或使用下载好的8.7gradle压缩包)

   ```
   wget https://services.gradle.org/distributions/gradle-8.7-all.zip
   ```

1. 把gradle-8.7.zip安装包解压到/opt/gradle下

```
sudo unzip -d /opt/gradle gradle-*.zip
```

2. 配置环境变量

```
cd ~
vim .bashrc
#把如下内容加到最下面
export PATH=$PATH:/opt/gradle/gradle-8.7/bin
#保存后source
source .bashrc
```

3. 配置好后执行`gradle -v` ,如果出现gradle版本信息则说明配置完成。

4. 配置gradle国内下载源：在~/.gradle目录下新创建一个`init.gradle`,该文件内容如下：

   ```
   allprojects {
       repositories {
           maven {
               url "https://maven.aliyun.com/repository/public"
           }
           maven {
               url "https://maven.aliyun.com/repository/google"
           }
           maven {
               url "https://maven.aliyun.com/repository/gradle-plugin"
           }
           mavenCentral()
       }
   }
   ```

   

#### 5.2 clone and build Easyspdf

```
cd ~/.git &&\
git clone git@github.com:HHHHHMMMM/spdf.git 
```

修改项目里的application.properties

```
cd ~/.git/spdf/src/main/resources
vim application.properties
```

针对数据库连接串、用户名、密码进行修改。

本项目用户名密码涉及配置如下：

```
spring.datasource.username=username
spring.datasource.password=ENC(xxxx)
jasypt.encryptor.password=key
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator
```

说明如下：

`spring.datasource.username`:数据库用户名

`spring.datasource.password`:这里数据库密码使用jasypt加密，`ENC(xxxx)`为密文，在项目启动时，springboot会解密ENC包裹住的密文，这里就填写ENC(<4.2输出的密文>)

`jasypt.encryptor.password`：jasypt加密的加密因子(上面4.2有提到过)

```
cd ~/.git/spdf &&\
gradle clean build --refresh-dependencies
```

使用--refresh-dependencies来让依赖下载的时候使用刚更新的国内源。此时正在下载依赖，第一次下载内容比较多，可能需要耗费一定时间。

build成功后，项目根目录下会增加一个build目录，打好的可运行的jar包就在build/libs里面。

### 步骤6. 把build好的jar包放到目标目录(应用执行目录)

在构建过程之后，在build/libs目录中将生成一个' .jar '文件。可以将该文件移动到所需的位置，例如“/workspace/easyspdf”。
还必须将项目中的Script文件夹移动到该目录,使用OpenCV的python脚本需要此文件夹。

```yaml
sudo mkdir -p /workspace/easyspdf &&\
sudo mv ./build/libs/EasySpdf-*.jar /workspace/easyspdf &&\
sudo mv scripts /workspace/easyspdf &&\
echo "Scripts installed."
```

### 步骤7： 其他文件安装(如果要使用OCR)

如果计划使用OCR功能，则在运行非英语扫描时可能需要为Tesseract安装语言包。

1. 下载所需的语言包。您需要的语言的Traineddata文件。

2. 请查看[OCRmyPDF安装指南](https://ocrmypdf.readthedocs.io/en/latest/installation.html)了解更多信息。

   

**重要:**不删除现有的'工程。训练数据，这是必需的。

基于Debian的系统，用这个命令安装语言:

```
sudo apt update
sudo apt install -y 'tesseract-ocr-*'
#查看安装好的包
dpkg-query -W tesseract-ocr-* | sed 's/tesseract-ocr-//g'
```

下载好的包应该是在/usr/share/tesseract-ocr/4.00/tessdata下，确保`eng.traineddata`文件在，这个是必须的。

### 步骤7： 拉起应用

```
cd /workspace/easyspdf 
nohup java -jar /workspace/easypdf/EasySpdf-*.jar >nohup.out&
```

友情提示，某些极端情况下，跑大页数、大体量pdf的时候请注意监控内存、cpu使用情况，必要时请采用必要的jvm参数进行限制处理。

```
#针对2c2g的相对安全配置,其他机器请自行判定
java -Xms512m -Xmx1024m -XX:MaxDirectMemorySize=256m -XX:MaxMetaspaceSize=128m -Xss512k -jar EasySpdf-*.jar
```

应用启动日志在./nohup.out里。日常日志在jar包同级目录./logs下。本文档具体中具体路径为`/workspace/easyspdf/logs`。

## Windows版本打包

本项目提供了launch4j配置脚本，当配置好gradle.build时，在idea里应该是可以直接通过插件打包。

launch4j配置文件在项目根目录下，可自行执行配置。

需要注意的是，由于项目中用到的有些类库对windows支持兼容性问题，涉及到使用CLI方式运行的功能无法使用，

目前版本包括如下功能：

- compress-pdf
- extract-image-scans
- repair
- pdf-to-pdfa
- file-to-pdf
- xlsx-to-pdf
- pdf-to-word
- pdf-to-presentation
- pdf-to-html
- pdf-to-xml
- ocr-pdf
- html-to-pdf
- url-to-pdf
- url-to-pdf
- book-to-pdf
- pdf-to-book
- pdf-to-rtf



## 客户端定制配置

#### easyspdf支持用户自定义配置

在步骤七拉起应用后，应用同级目录下会生成`logs`,`pipeline`,`configs`,`customFiles`目录（如若没有手动生成，会自动生成一份。如若已经手动生成了，则会直接使用手动生成的文件）。

其中，

- ##### configs：

该目录下有两个配置文件：

1. `setting.yml`是一个easyspdf外置的配置文件，配置了一些常用开关/配置，里面有每种配置的解释说明。

​       这里提一嘴easyspdf的登录功能。默认情况下，easyspdf并没有开启登录功能，即应用拉起来后访问应用可以直接使用。如果要开启easyspdf的登录，则将`security.enableLogin`改为true，并重启应用，则此时访问应用会进入到登录页面。默认情况下，你可以使用登录初始用户：admin，密码:stirling来登入。初次登入需要强制修改密码。

2. `custom_setting.yml`是spring项目的外置配置文件，熟悉java和spring的用户可以自行在这里定义配置来修改默认配置。

此项目当然支持springboot的默认约定，即：直接读取并使用jar包执行目录同级或执行目录config/子目录下的配置文件。

此文档中，由于jar包执行目录是`/workspace/easyspdf `，故外置配置文件`application.properties`应该放置在同级目录`/workspace/easyspdf`或`/workspace/easyspdf/config`目录下。且优先级`/workspace/easyspdf/config`大于`/workspace/easyspdf`.

- ##### logs：

​	存放了应用日志

- ##### customFiles：

  针对easyspdf已有的页面、logo可以进行自定义修改。它相当于一个外置的resources目录。当你想修改某个存在的页面的时候，可以将该页面放置到`customFiles/templates`目录下(项目使用thymeleaf写前端页面)，项目启动的时候，会用这里的页面来替换默认页面。

  如果想修改logo，可以将要替换的logo放置到`customeFils/static`目录下，项目运行时会将新logo替换掉项目默认的favicon.svg。

- ##### `pipeline`:

  流水线高级功能。





