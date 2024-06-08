#  EasySpdf本地部署

要在没有Docker/Podman的情况下运行应用程序，可能需要手动安装所有依赖项并构建必要的组件。

请注意，某些依赖项可能在所有Linux发行版的标准存储库中都不可用，并且可能需要额外的安装步骤。

下面的指南假设您对在linux操作系统中使用命令行界面有基本的了解。

它应该可以在大多数Linux发行版和MacOS上工作。

对于Windows，您可能需要使用Windows Subsystem For Linux (WSL)来完成某些步骤。如果你的发行版有旧的或者不是所有的包，理论上你可以使用一个Distrobox/Toolbox。或者使用Docker容器。

## 1. 系统参数

1. 4核(vCPU)32 GiB服务器，系统Ubuntu 22.04 64位 UEFI版，一个基本ESSD云盘(正常nas也可)
2. mysql8.0

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
mkdir ~/.git
cd ~/.git &&\
git clone https://github.com/agl/jbig2enc.git &&\
cd jbig2enc &&\
./autogen.sh &&\
./configure &&\
make &&\
sudo make install
```

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

### 4. 步骤4：Clone and Build Easyspdf

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









cd ~/.git &&\
git@github.com:HHHHHMMMM/spdf.git

2. 









