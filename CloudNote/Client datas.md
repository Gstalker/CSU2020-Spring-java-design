#客户端的数据接口

## "login"

发送字段：login,account,password各一行

需求返回：如果登录成功，则返回"success\n",失败则返回"fail\n"

服务端需要在登陆成功后做的：自动转换为当前用户的编辑模式，等待服务端发送确认信息后，将原始数据发送给服务端。

## "recvLastData"

发送字段 "recvLastData"

接受字段：

- 上一次编辑的文件名（String)

- 上一次编辑的文件（String)

## "update"

发送字段 update，filename

服务端接收更新后的文件，并以文件形式储存到服务器端

服务端接收完成之后，返回字段”Success"

## "createNewNote"

发送字段"createNewNote"

发送filename

服务器创建filename.txt

服务器创建成功后服务器返回success

存在同名文件则返回exist

创建失败返回fail

## register

发送字段：

1. "register"
2. account
3. password

接收字段：

- exist:注册失败,同名账号已存在
- success:注册成功
- fail：注册失败，网络问题

## OpenFile

发送字段 “OpenFile"

服务器接收该字段后，发送文件总数，**分行**发送所有的文件名给客户端

客户端接收完成后，弹出窗口，等待用户选择文件名



发送字段 filename

服务器接收该字段后，向客户端发送文件内容

客户端接收完毕后向服务器答复Success

## exit

发送字段exit

重置服务器状态



## 新建账户时的操作

1. 更新账号数据列表

2. 创建用户文件夹：“.\data\\"+用户名

3. 创建用户配置文件userdata.dat

   - 用户上一次打开的文档名

   - 用户总的文档数量

   - 用户所有的文档名

   - yourfirstnote.txt

     Enjoy your note here~

     Presented by Gstalker

## 坑