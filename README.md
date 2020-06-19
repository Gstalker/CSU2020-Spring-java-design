# 中南大学 2019 - 2020年秋季 java 大作业 云笔记本

## 任意平台下的运行方式

写的很丑，就几天时间写来应付的。有些功能还没实现，过了就行了

编写环境：jdk 8

高于jdk11的java可能无法运行。因为本项目使用的可视化框架javafx在高版本中的jdk不再作为基本包附带。

**运行方式**：

首先你得配java环境。windows下的不再赘述。

ubuntu系列可以使用如下指令一次性安装好jdk8

```shell
sudo apt-get update
sudo apt-get install openjdk-8-jdk
```

配好环境之后，执行下述指令

```shell
git clone https://github.com/Gstalker/CSU2020-Spring-java-design.git

cd CloudNote

javac ./NoterClient.java

javac ./NoterServer
```

编译好两份源代码之后，打开两个命令行窗口

窗口1执行

```shell
java NoterServer
```

窗口2执行

```
java NoterClient
```

注意，窗口1的命令要先于窗口2

## 如何迁移服务器端

NoteServer可以迁移到其它ip地址使用。

需要做的更改：

1. NoterClient.java

   第49行 变量 ipAddress 修改为NoterServer所在的ip地址

   如果是本地，就修改为localhost

2. 将`NoterServer.java`和`data`文件夹一同复制到目标机器

   然后重新编译NoterServer.java

## 需要注意的地方

两份文件都是GBK编码,CRLF换行

如果想要迁移到Linux平台上，需要换成UTF-8编码，LF换行

或者在编译参数里头指明源码格式
