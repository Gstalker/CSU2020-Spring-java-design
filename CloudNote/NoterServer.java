import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
public class NoterServer{
  private List<String> userAccountInfo;//1，3，5，7...是账户名
                               //2,4,6,7...是账户密码
                               //第0项是账户数量。

  private List<String> userData;//保存了用户的文件名
                        //0:上一次打开的文本
                        //1:用户的笔记总数
                        //2~:用户的笔记名

  private static final String ACCOUNT_FILE_PATH = "./data/account";//储存文件的路径地址
  private static final String DATA_FILE_PATH_PRIFIX = "./data";
  private static final String DATA_FILE_NAME = "/userdata.dat";
  private static final int PORT = 19613;//开放端口

  private String loginedUserAccount = null;
  

  public static void main(String args[]){
    NoterServer server = new NoterServer();
    while(true)server.initServer();
  }

  public void initServer(){
    userAccountInfo = this.initAccounts(ACCOUNT_FILE_PATH);
    try(
      ServerSocket server = new ServerSocket(PORT);
      Socket socket = server.accept();
      InputStream is = socket.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      OutputStream os = socket.getOutputStream();
      PrintWriter pw = new PrintWriter(os);
    ){
      System.out.println("The server has been started");
      while(true){
        String userRequestCode = br.readLine();
        System.out.println("new Request:    "+userRequestCode);

        if(userRequestCode == null){
          try{
            Thread.sleep(500);
          }
          catch(Exception e){
            e.printStackTrace();
          }
          continue;
        }

        switch(userRequestCode){
          case "login":{
            //login
            System.out.println("request recived:  login");

            //接收用户发送的账户和密码
            String account = br.readLine();
            System.out.println("account :    "+account);
            String password = br.readLine();
            System.out.println("password:    "+password);

            //登录检测
            if(account != null && password!= null && this.loginCheck(account,password)){
              //登录成功
              System.out.println("Login Success!,user:    "+account);
              this.loginedUserAccount = account;
              //告诉客户端，登录成功
              pw.println("success");
              pw.flush();
            }
            else{
              System.out.println("Login Fail!");
              pw.println("fail");
              pw.flush();
            }
            break;
          }
          case "recvLastData":{
            //recvLastData
            System.out.println("request recived:  recvLastData");
            this.userData = this.initAccounts(this.DATA_FILE_PATH_PRIFIX+"/"+this.loginedUserAccount+this.DATA_FILE_NAME);
            String file = this.readAllLine(this.DATA_FILE_PATH_PRIFIX+"/"+this.loginedUserAccount+"/"+this.userData.get(0));
            pw.println((String)this.userData.get(0));//发送文件名
            pw.flush();
            pw.write(file);//发送文件
            pw.flush();
            pw.println("0xbadbeef");
            pw.flush();
            break;
          }
          case "OpenFile":{
            System.out.println("request recived:  OpenFile");
            pw.println(userData.get(1));
            for(int i = 2 ; i < userData.size() ;++i){
              pw.println(userData.get(i));
              pw.flush();
            }
            String fileName = br.readLine();
            System.out.println("    OpenFile: "+fileName);
            String file = readAllLine(DATA_FILE_PATH_PRIFIX+"/"+loginedUserAccount+"/"+fileName);
            pw.println(file);
            pw.flush();
            pw.println("0xbadbeef");
            pw.flush();
            System.out.println("    Send File "+fileName+' '+br.readLine());
            userData.set(0,fileName);
            String tmp = "";
            for(int i = 0 ; i < userData.size();++i){
              tmp += userData.get(i)+'\n';
            }
            saveFile(DATA_FILE_PATH_PRIFIX+'/'+loginedUserAccount+'/'+"/userdata.dat",tmp);
            break;
          }
          case "update":{
            //update
            System.out.println("request recived:  update");
            String fileName = br.readLine();
            String file = this.readAllLine(br);
            this.saveFile(this.DATA_FILE_PATH_PRIFIX+"/"+this.loginedUserAccount+"/"+fileName,file);
            pw.println("Auto Save Complete!");
            pw.flush();
            break;
          }
          case "register":{
            System.out.println("request recived:  register");
            register(pw,br);
            break;
          }
          case "createNewNote":{
            System.out.println("request recived:  createNewNote");
            createNewNote(pw,br);
            break;
          }
          case "exit":{
            //exit
            String newUserData = "";
            for(int i = 0; i < userData.size() ; ++i){
              newUserData += userData.get(i) + '\n';
            }
            saveFile(DATA_FILE_PATH_PRIFIX+'/'+loginedUserAccount+'/'+"/userdata.dat" , newUserData);
            System.out.println("request recived:  exit");
            return;
          }
          default :{
            //dead
            System.out.println("request recived:  UNDIFINED");
            break;
          }
        }
      }
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

  private void createNewNote(PrintWriter messageWriter,BufferedReader messageReader){
    try{
      String newFileName = messageReader.readLine()+".txt";
      int totalFileCount = Integer.parseInt(userData.get(1));
      for(int i = 2;i < totalFileCount+2;++i){
        if(newFileName.equals(userData.get(i))){
          messageWriter.println("exist");
          messageWriter.flush();
          return;
        }
      }
      String userPath = DATA_FILE_PATH_PRIFIX+'/'+loginedUserAccount;
      String emptyFile = "empty";
      saveFile(userPath+'/'+newFileName,emptyFile);
      userData.set(0,newFileName);
      userData.set(1,String.valueOf(totalFileCount+1));
      userData.add(newFileName);
      String newUserData = "";
      for(int i = 0; i < userData.size() ; ++i){
        newUserData += userData.get(i) + '\n';
      }
      saveFile(userPath+"/userdata.dat" , newUserData);
      messageWriter.println("success");
      messageWriter.flush();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private void register(PrintWriter messageWriter,BufferedReader messageReader){
    try{
      String account = messageReader.readLine();
      String password = messageReader.readLine();
      System.out.println("Account: "+account);
      System.out.println("Password: "+password);
      if(account.equals("account") || password.equals("")){
        messageWriter.println("fail");
        messageWriter.flush();
        return;
      }
      for(int i = 1 ; i <= Integer.parseInt(userAccountInfo.get(0))*2;i+=2){
        if(account.equals(userAccountInfo.get(i))){
          messageWriter.println("exist");
          messageWriter.flush();
          return;
        }
      }
      userAccountInfo.add(account);
      userAccountInfo.add(password);
      userAccountInfo.set(0, String.valueOf(Integer.parseInt(userAccountInfo.get(0))+1));
      messageWriter.println("success");
      messageWriter.flush();
      String userFilePath = DATA_FILE_PATH_PRIFIX+'/'+account;
      File userDir = new File(userFilePath);
      if(!userDir.exists()){
        userDir.mkdir();
      }
      String userDat = "yourfirstnote.txt\n1\nyourfirstnote.txt";
      String firstFile = "You can enrich the soul of things,\n\nbut the shining stars of the sky,\n\nand my inner moral law.";
      saveFile(userFilePath + DATA_FILE_NAME , userDat);
      saveFile(userFilePath + "/yourfirstnote.txt" , firstFile);
      String newAccountFile = String.valueOf(Integer.parseInt(userAccountInfo.get(0)))+'\n';
      for(int i = 1 ; i < userAccountInfo.size() ; ++i){
        newAccountFile += userAccountInfo.get(i)+'\n';
      }
      saveFile(ACCOUNT_FILE_PATH,newAccountFile);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  private boolean loginCheck(String account,String password){

    //有几个账户
    int total = Integer.parseInt(this.userAccountInfo.get(0));

    //对所有储存着的账户进行一对一检测
    for(int i = 1;i<=total*2;i+=2){
      if(
        account.equals(this.userAccountInfo.get(i)) && 
        password.equals(this.userAccountInfo.get(i+1))
      ){
        return true;
      }
    }
    return false;
  }

  private void saveFile(String filePath,String usrInput){
    //功能：向位于filePath位置的文件写入usrInput
    try(
      FileWriter fwriter = new FileWriter(filePath);
    ){
      fwriter.write(usrInput);
      fwriter.flush();
      fwriter.close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  private List<String> initAccounts(String filePath){
  // 函数：initAccounts
  //功能：返回一个列表，其中第一项是账户个数
  //1，3，5，7...是账户名
  //2,4,6,7...是账户密码
    List<String> result = new ArrayList<>();
    try(
      BufferedReader file = new BufferedReader(new FileReader(filePath));
    ){
      String temp;
      while((temp = file.readLine()) != null){
        result.add(temp);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return result;
  }

  private String readAllLine(String filePath){
    //全文读取一个文件并且以String形式返回
    String result = "";
    try(
      BufferedReader file = new BufferedReader(new FileReader(filePath));
    ){
      String temp;
      while((temp = file.readLine()) != null){
        result += temp + '\n';
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return result;
  }

  private String readAllLine(BufferedReader messageReader){
    //全文读取一个文件并且以String形式返回
    String result = "";
    try{
      String temp = messageReader.readLine();
      while(!temp.equals("0xbadbeef")){//文件结束
        result += temp+'\n';
        temp = messageReader.readLine();
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return result;
  }
}