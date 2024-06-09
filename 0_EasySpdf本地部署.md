#  EasySpdf本地部署

要在没有Docker/Podman的情况下运行应用程序，可能需要手动安装所有依赖项并构建必要的组件。

请注意，某些依赖项可能在所有Linux发行版的标准存储库中都不可用，并且可能需要额外的安装步骤。

下面的指南假设您对在linux操作系统中使用命令行界面有基本的了解。

它应该可以在大多数Linux发行版和MacOS上工作。

对于Windows，您可能需要使用Windows Subsystem For Linux (WSL)来完成某些步骤。如果你的发行版有旧的或者不是所有的包，理论上你可以使用一个Distrobox/Toolbox。或者使用Docker容器。

## 1. 系统参数

1. 4核(vCPU)32 GiB服务器，系统Ubuntu 22.04 64位 UEFI版，一个基本ESSD云盘(正常nas也可)
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

针对 Fedora-based 系统，可以用如下命令：

```
sudo dnf install -y git automake autoconf libtool leptonica-devel pkg-config zlib-devel make gcc-c++ java-21-openjdk python3 python3-pip
```

### 2. 步骤2：Clone and Build jbig2enc(只针对要使用OCR功能的情况)

针对Debian和Fedora-based系统，你可以使用如下命令对源码进行构建。

```
mkdir -p /workspace/.git
cd /workspace/.git &&\
git clone https://github.com/agl/jbig2enc.git &&\
cd jbig2enc &&\
./autogen.sh &&\
./configure &&\
make &&\
sudo make install
```

注意：

1. 如果`git clone`使用http无法下载，可以使用ssh方式，ssh的url为：`git@github.com:agl/jbig2enc.git`
2. 如果报错没有权限读取远程仓库（fatal: Could not read from remote repository），请先ssh-keygen生成key之后，配置到git上。
3. 如果实在无法拉取，可使用软件目录的项目内容。

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

Fedora系列的系统执行如下语句：

```
sudo dnf install -y libreoffice-writer libreoffice-calc libreoffice-impress unpaper ocrmypdf
pip3 install uno opencv-python-headless unoconv pngquant WeasyPrint
```

pip3安装时可能会报错`no such option: --break-system-packages`,该错误是因为服务器的pip不是最新的，该参数是pip`23.3`版本才引入的，可以尝试更新pip源。解决方案：

1. 查看pip版本

   `pip --version`

2. 更新pip

   ```
   pip install --upgrade pip
   ```

Fedora系列的系统执行如下语句：

```
sudo dnf install -y libreoffice-writer libreoffice-calc libreoffice-impress unpaper ocrmypdf
pip3 install uno opencv-python-headless unoconv pngquant WeasyPrint
```

注意(可选)：libreoffice有较多版本，可尽量选择升级最新的版本。最新版本在转换性能上有一定提升。截止目前最新版本的libreoffice版本是24.2.3.2。

升级方法：

1. 上面命令正常安装

   ```
   sudo apt-get install -y libreoffice unpaper ocrmypdf 
   ```

   然后使用`soffice --version`查看是否是最新版本，截止目前(24.06.09)libreoffice最新版本是24.2.3.

2. 如果不是最新版本，国内源又没有更新最新版本的libreoffice，可以尝试通过官方PPA安装升级最新版本

   ```
   sudo add-apt-repository ppa:libreoffice/ppa
   ```

​		如果报错：`GPG error: http://cz.archive.ubuntu.com/ubuntu xenial InRelease: The following signatures couldn't be verified because the public key is not available:NO_PUBKEY xxx yyy`表明你的 Ubuntu 系统在从 `http://cz.archive.ubuntu.com/ubuntu` 获取软件包时，无法验证某些签名，因为缺少相应的公钥。可以通过以下步骤解决这个问题：

```
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys xxx  yyy #这里的xxx和yyy是你报错里的public_key
sudo apt-get update
sudo add-apt-repository ppa:libreoffice/ppa
```

如果部署的服务器在国内， 那么官方PPA源下载安装有一定几率比较慢，如果比较慢耐心点等他安装好即可。

3. 安装完成后使用`soffice --version`可以查看版本，如果是24.2.3(最新版本)即可。

### 4. 创建库表

​	在mysql中建库表sql如下：

` users`表

```
CREATE DATABASE  IF NOT EXISTS `stirling_pdf_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `stirling_pdf_db`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: stirling_pdf_db
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `USER_ID` bigint NOT NULL AUTO_INCREMENT,
  `API_KEY` varchar(255) DEFAULT NULL,
  `AUTHENTICATIONTYPE` varchar(255) DEFAULT NULL,
  `ENABLED` tinyint(1) DEFAULT NULL,
  `IS_FIRST_LOGIN` tinyint(1) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `ROLE_NAME` varchar(255) DEFAULT NULL,
  `USERNAME` varchar(255) DEFAULT NULL,
  `chargedate` date DEFAULT NULL,
  `expire_date` date DEFAULT NULL,
  `is_expire` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`),
  UNIQUE KEY `USERNAME` (`USERNAME`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;



```

`persistent_logins`表：

```
CREATE DATABASE  IF NOT EXISTS `stirling_pdf_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `stirling_pdf_db`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: stirling_pdf_db
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `persistent_logins`
--

DROP TABLE IF EXISTS `persistent_logins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `persistent_logins` (
  `SERIES` varchar(255) NOT NULL,
  `LAST_USED` timestamp NOT NULL,
  `TOKEN` varchar(64) NOT NULL,
  `USERNAME` varchar(64) NOT NULL,
  PRIMARY KEY (`SERIES`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `persistent_logins`
--

LOCK TABLES `persistent_logins` WRITE;
/*!40000 ALTER TABLE `persistent_logins` DISABLE KEYS */;
/*!40000 ALTER TABLE `persistent_logins` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-05 14:44:49

```

`user_settings`表：

```
CREATE DATABASE  IF NOT EXISTS `stirling_pdf_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `stirling_pdf_db`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: stirling_pdf_db
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user_settings`
--

DROP TABLE IF EXISTS `user_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_settings` (
  `USER_ID` bigint NOT NULL,
  `SETTING_VALUE` varchar(255) DEFAULT NULL,
  `SETTING_KEY` varchar(255) NOT NULL,
  PRIMARY KEY (`USER_ID`,`SETTING_KEY`),
  CONSTRAINT `FK8V82NJ88RMAI0NYCK19F873DW` FOREIGN KEY (`USER_ID`) REFERENCES `users` (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_settings`
--

LOCK TABLES `user_settings` WRITE;
/*!40000 ALTER TABLE `user_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_settings` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-05 14:44:49

```

`authorities`表：

```
CREATE DATABASE  IF NOT EXISTS `stirling_pdf_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `stirling_pdf_db`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: stirling_pdf_db
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `authorities`
--

DROP TABLE IF EXISTS `authorities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `authorities` (
  `ID` bigint NOT NULL AUTO_INCREMENT,
  `AUTHORITY` varchar(255) DEFAULT NULL,
  `USER_ID` bigint DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FKK91UPMBUEYIM93V469WJ7B2QH` (`USER_ID`),
  CONSTRAINT `FKK91UPMBUEYIM93V469WJ7B2QH` FOREIGN KEY (`USER_ID`) REFERENCES `users` (`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-06-05 14:44:49

```

### 5. 步骤4：Clone and Build Easyspdf

#### 5.1 安装构建工具

1. 安装构建工具：把gradle-8.7.zip安装包解压到/opt/gradle下

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
sudo mkdir /workspace/easyspdf &&\
sudo mv ./build/libs/EasySpdf-*.jar /workspace/easyspdf &&\
sudo mv scripts /workspace/easyspdf &&\
echo "Scripts installed."
```





### 步骤6： 其他文件安装(如果要使用OCR)

如果计划使用OCR功能，则在运行非英语扫描时可能需要为Tesseract安装语言包。

1. 下载所需的语言包。您需要的语言的Traineddata文件。

2. 请查看[OCRmyPDF安装指南](https://ocrmypdf.readthedocs.io/en/latest/installation.html)了解更多信息。

   

**重要:**不删除现有的'工程。训练数据，这是必需的。

基于Debian的系统，用这个命令安装语言:

```
sudo apt update
sudo apt upgrade
sudo apt install -y 'tesseract-ocr-*'
#查看安装好的包
dpkg-query -W tesseract-ocr- | sed 's/tesseract-ocr-//g'
```

下载好的包应该是在/usr/share/tesseract-ocr/4.00/tessdata下，确保`eng.traineddata`文件在，这个是必须的。





### 步骤7： 拉起应用

```
nohup java -jar /workspace/easypdf/EasySpdf-*.jar >nohup.out&
```

友情提示，某些极端情况下，跑大页数、大体量pdf的时候请注意监控内存、cpu使用情况，必要时请采用必要的jvm参数进行限制处理。

```
java -Xms8192m -Xmx8192m -XX:MaxDirectMemorySize=4096m -XX:MaxMetaspaceSize=1024m -Xss8192k -jar EasySpdf-*.jar

#针对2c2g的相对安全配置
java -Xms512m -Xmx1024m -XX:MaxDirectMemorySize=256m -XX:MaxMetaspaceSize=128m -Xss512k -jar EasySpdf-*.jar

```



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







