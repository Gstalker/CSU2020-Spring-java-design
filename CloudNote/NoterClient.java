import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.value.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class NoterClient extends Application{
  //窗口初始长度，宽度
  public static final double ORIGINAL_HEIGHT = 600.0d;
  public static final double ORIGINAL_WIDTH = 580.0d;

  private String theEditingFileName;
  private long textLength = 0;
  private TextArea textArea;
  private TextField accountTF;
  private PasswordField passwordTF;
  private Button loginButton;
  private Button registerButton;

  private Stage mainWindow;//程序的主窗口

  private Stage getFileNameWindow;
  private String ipAddress = "localhost";

  //全局变量组:和服务端的连接
  private Socket connector;
  private BufferedReader messageReader;
  private PrintWriter messageSender;
  public static void main(String[] args){
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception{
    //客户端的开始地点
    try{
      //创建并保留和服务器的连接
      this.connector = new Socket(ipAddress,19613);
      this.messageSender = new PrintWriter(
        this.connector.getOutputStream()
      );
      
      this.messageReader = new BufferedReader(
        new InputStreamReader(
          this.connector.getInputStream()
        )
      );
    }
    catch(Exception e){
      e.printStackTrace();
    }

    //设定GUI
    this.mainWindow = primaryStage;
    this.setInitOptions(mainWindow);//设定title，图标，透明度
    this.setLoginWindowOptions(mainWindow);//设定窗口组件及其功能

    primaryStage.show();//展示登录窗口

    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {//退出事件
      @Override
      public void handle(WindowEvent event){
        //关闭程序的时候，保存当前正在编辑的文件，并且切断和服务器的连接
        try{
          if(connector.isConnected()){
            if(theEditingFileName!=null)uploadFile();
            messageSender.println("exit");
            messageSender.flush();
            connector.close();
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
        finally{
          System.out.println("Noter exited");
          System.exit(0);
        }
      }
    });
  }

  private BufferedReader getBufferedReader(){
    return this.messageReader;
  }

  private PrintWriter getPrintWriter(){
    return this.messageSender;
  }

  private Stage getMainWindow(){
    return this.mainWindow;
  }

  private void setEditorWindowOptions(Stage primaryStage){
    //函数：setLoginWindowOptions
    //功能：设定editor窗口的参数
    //窗口最小的大小
    primaryStage.setMinWidth(400.0d);
    primaryStage.setMinHeight(200.0d);
    //Editor窗口的大小
    primaryStage.setWidth(ORIGINAL_HEIGHT);
    primaryStage.setHeight(ORIGINAL_WIDTH);
    primaryStage.setResizable(true);
    primaryStage.setScene(this.editorScene());
    this.recvLastData();
    this.setEditorMathod();
  }

  private void recvLastData(){
    //函数 recvLastData()
    //功能：从客户端接受上一次用户编辑过的数据
    try{
      PrintWriter pw = this.getPrintWriter();
      pw.println("recvLastData");
      pw.flush();
      BufferedReader br = this.getBufferedReader();
      this.theEditingFileName = br.readLine();
      String temp = br.readLine();
      String data = "";
      while(!temp.equals("0xbadbeef")){//文件结束
        data += temp+'\n';
        temp = br.readLine();
      }
      textLength = data.length();
      textArea.setText(data);
    }
    catch(Exception e){
      e.printStackTrace();
      alertWindow("fail", "Cannot read the recent file!");
    }
  }
  
  private void createNewFile(){
    System.out.println("createNewFile");
    try{
      uploadFile();
      System.out.println("file uploaded!");
      this.getFileNameWindow = new Stage();
      getFileNameWindow.setScene(setGetFileNameWindow());
      getFileNameWindow.setWidth(533.34);
      getFileNameWindow.setHeight(133.34);
      getFileNameWindow.setResizable(false);
      getFileNameWindow.initOwner(getMainWindow());
      getFileNameWindow.initModality(Modality.WINDOW_MODAL);
      getFileNameWindow.setTitle("Create New Note");
      getFileNameWindow.setOpacity(0.90d);
      getFileNameWindow.show();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private Scene setGetFileNameWindow(){
    BorderPane borderPane = new BorderPane();
    
    //填充
    AnchorPane topFilling = new AnchorPane();
    Label tip = new Label("Please Input the name of the new file");
    AnchorPane.setTopAnchor(tip, 20.0d);
    AnchorPane.setLeftAnchor(tip, 50.0d);
    topFilling.setPrefHeight(30.0d);
    topFilling.getChildren().add(tip);
    borderPane.setTop(topFilling);
    
    //文本框
    AnchorPane textAP = new AnchorPane();
    TextField getFileName = new TextField();
    AnchorPane.setLeftAnchor(getFileName,50.0);
    AnchorPane.setRightAnchor(getFileName,50.0);
    textAP.getChildren().add(getFileName);
    borderPane.setCenter(textAP);

    //按钮
    AnchorPane buttonAP = new AnchorPane();
    Button confirmButton = new Button("Confirm");
    AnchorPane.setLeftAnchor(confirmButton,402.44);
    AnchorPane.setRightAnchor(confirmButton,50.0);
    AnchorPane.setBottomAnchor(confirmButton, 10.0);
    buttonAP.getChildren().add(confirmButton);
    borderPane.setBottom(buttonAP);

    confirmButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        try{
          getFileName.setDisable(true);
          PrintWriter pw = getPrintWriter();
          BufferedReader br = getBufferedReader();
          pw.println("createNewNote");
          pw.flush();
          String newFileName = getFileName.getText();
          pw.println(newFileName);
          pw.flush();
          String result = br.readLine();
          if(result.equals("success")){
            theEditingFileName = newFileName+".txt";
            textArea.clear();
          }
          else if(result.equals("exist")){
            alertWindow("Fail!","The note have existed");
          }
          else if(result.equals("fail")){
            alertWindow("Fail!","Fail in Create New Note\nTry again!");
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
        finally{
          getFileNameWindow.close();
          getFileNameWindow = null;
        }
      }
    });

    return new Scene(borderPane);
  }

  private void uploadFile(){
    String file = this.textArea.getText();
    try{
      PrintWriter pw = getPrintWriter();
      pw.println("update");
      pw.flush();
      pw.println(theEditingFileName);
      pw.flush();
      pw.println(file);
      pw.flush();
      pw.println("0xbadbeef");
      pw.flush();
      BufferedReader br = getBufferedReader();
      System.out.println(br.readLine());
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private void setEditorMathod(){
    //函数setEditorMathod
    //功能：设定编辑模式的基本设置
    textArea.textProperty().addListener(new ChangeListener<String>() {
      //设定textArea中的文本监听器
      //当用户修改文本超过20个字符的时候，上传新的文本信息。
      @Override
      public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue){
        if(Math.abs(newValue.length() - textLength) > 20){
          uploadFile();
          textLength = newValue.length();
        }
      }
    });
  }

  private void setInitOptions(Stage primaryStage){
    //函数setInitOptions
    //功能:为窗体设定初始参数
    primaryStage.setTitle("Cloud Note");
    primaryStage.getIcons().add(new Image("./sources/icon/white.jpg"));//设置图标
    primaryStage.setOpacity(0.95d);//设置透明度
    //窗口出现时的位置，坐标为左上角的位置
  }
  
  private void setLoginWindowOptions(Stage primaryStage){
    //函数：setLoginWindowOptions
    //功能：设定登录窗口的参数

    //设定窗口出现时距离屏幕左上角的距离
    primaryStage.setX(100);
    primaryStage.setY(100);

    //设定窗口的长 和 宽
    primaryStage.setWidth(468.0d);
    primaryStage.setHeight(350.0d);

    //设定窗口具体的组件配置
    primaryStage.setScene(this.loginScene());

    //设置窗口是否可以调整大小
    primaryStage.setResizable(false);

    //设定登录窗口的登录功能
    this.setLoginMathod();
  }

  private void setLoginMathod(){
    loginButton.setOnAction(new EventHandler<ActionEvent>() {
      //监听这个组件，当他被点击的时候，启用下面这些代码
      @Override
      public void handle(ActionEvent event){
        //获取账号/密码栏中的字符串
        String password = passwordTF.getText();
        String account = accountTF.getText();
        try{
          PrintWriter pw = getPrintWriter();
          //告诉服务器，客户端要干什么
          pw.println("login");
          pw.flush();

          //把用户输入的账户和密码发送给服务器
          pw.println(account);
          pw.flush();
          pw.println(password);
          pw.flush();

          //从服务端那边接收消息，登陆成功与否
          BufferedReader br = getBufferedReader();
          String result = br.readLine();
          System.out.println("login "+result);
          if(result.equals("fail") || result.equals("")){//登陆失败时的处理
            //登陆失败，弹窗提示
            alertWindow("warning", "Login fail,Please try again");
          }
          else if(result.equals("success")){
            //登陆成功以后，进入主界面
            setEditorWindowOptions(getMainWindow());
          }
        }
        catch(Exception e){
          e.printStackTrace();
          alertWindow("warning", "Login fail,Please try again");
        }
      }
    });
    registerButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        try{
          PrintWriter pw = getPrintWriter();
          BufferedReader br = getBufferedReader();
          pw.println("register");
          pw.flush();
          pw.println(accountTF.getText());
          pw.flush();
          pw.println(passwordTF.getText());
          pw.flush();
          String result = br.readLine();
          switch (result){
            case "exist":{
              alertWindow("Register fail", "This account has been existed!\nIf you forget the password,please connect Gstalker");
              break;
            }
            case "success":{
              infomationWindow("Congratulations", "Register success!\nPlease press the \"Login\" button");
              break;
            }
            case "fail":{
              alertWindow("Connection Error", "Cannot connect the server!");
              break;
            }
            default:{
              System.out.println("regisiterButton:UNDEFINED");
            }
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
    });
  }

  private void alertWindow(String title,String message){
    //弹出一个警告窗口
    Alert alert = new Alert(AlertType.ERROR);//设定警告图标类型
    alert.titleProperty().set(title);
    alert.headerTextProperty().set(message);
    alert.showAndWait();
  }
  private void infomationWindow(String title,String message){
    //弹出一个警告窗口
    Alert alert = new Alert(AlertType.INFORMATION);//设定警告图标类型
    alert.titleProperty().set(title);
    alert.headerTextProperty().set(message);
    alert.showAndWait();
  }

  private Scene loginScene(){
    //函数：loginScene
    //功能：返回登录窗口的场景
    return new Scene(this.loginGUI());
  }

  private BorderPane loginGUI(){
    //函数 loginGUI
    //功能 返回登陆窗口的BorderPane
    BorderPane BP = new BorderPane();
    
    //使用窗格布局来布置密码框和账号框
    //位置：中部
    GridPane loginPane = new GridPane();
    //标签：Account和Password
    Label accountLable = new Label("Account");
    Label passwordLabel = new Label("Password");
    //账号栏和密码栏
    accountTF = new TextField();
    passwordTF = new PasswordField();
    
    //向窗格布局中添加组件
    loginPane.add(accountLable,0,0);
    loginPane.add(passwordLabel,0,1);
    loginPane.add(accountTF,1,0);
    loginPane.add(passwordTF,1,1);
    
    //设置对齐方式
    loginPane.setAlignment(Pos.CENTER);
    
    //网格布局模式中，各个网格之间的间隙，垂直和水平间隙
    loginPane.setHgap(10.0d);
    loginPane.setVgap(10.0d);

    //把这个窗格布局放在窗口的中间位置
    BP.setCenter(loginPane);

    //使用锚定布局来拉伸登录按键
    //位置：下方
    AnchorPane loginButtonAP = new AnchorPane();

    loginButton = new Button("Login");
    //设置该组件在AnchorPane布局中距离某个方向的像素点数量
    AnchorPane.setLeftAnchor(loginButton,284.0d);
    AnchorPane.setRightAnchor(loginButton,100.0d);
    AnchorPane.setBottomAnchor(loginButton,30.0d);
    loginButtonAP.getChildren().add(loginButton);

    registerButton = new Button("Register");
    AnchorPane.setLeftAnchor(registerButton,100.0d);
    AnchorPane.setRightAnchor(registerButton,284.0d);
    AnchorPane.setBottomAnchor(registerButton,30.0d);
    loginButtonAP.getChildren().add(registerButton);
    BP.setBottom(loginButtonAP);


    //使用锚定布局填充上方
    AnchorPane topFiller = new AnchorPane();
    topFiller.setPrefHeight(150.0d);
    BP.setTop(topFiller);
    
    return BP;
  }

  private Scene editorScene(){
    //函数 editorScene
    //功能：返回文本编辑器的Scene
    return new Scene(this.initGUI());
  }

  private AnchorPane initGUI(){
    //函数 initGUI
    //功能 返回文本编辑器的AnchorPane
    AnchorPane noterPane = new AnchorPane();

    //设置上方菜单栏
    noterPane.getChildren().add(this.initMenu());
    //添加文本编辑区域
    noterPane.getChildren().add(this.initTextArea());
    return noterPane;
  }


  private MenuBar initMenu(){
    //函数：initMenu
    //功能：返回设置好的菜单栏对象
    MenuBar menuBar = new MenuBar();
    //菜单：文件操作
    Menu fileOpt = new Menu("File");

    MenuItem openFile = new MenuItem("open");
    openFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        openFile();
      }
    });
    openFile.setAccelerator(KeyCombination.valueOf("CTRL+O"));

    MenuItem newFile = new MenuItem("new");
    newFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        createNewFile();
      }
    });
    newFile.setAccelerator(KeyCombination.valueOf("CTRL+N"));

    MenuItem saveFile = new MenuItem("save");
    saveFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        uploadFile();
      }
    });
    //设置快捷键
    saveFile.setAccelerator(KeyCombination.valueOf("CTRL+S"));
    //把组件添加到Menu中
    fileOpt.getItems().addAll(newFile,openFile,saveFile);

    //菜单：编辑操作
    Menu editOpt = new Menu("Edit");
    MenuItem copyEdit = new MenuItem("Copy");
    copyEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.copy();
      }
    });
    copyEdit.setAccelerator(KeyCombination.valueOf("CTRL+C"));

    MenuItem pasteEdit = new MenuItem("Paste");
    pasteEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.paste();
      }
    });
    pasteEdit.setAccelerator(KeyCombination.valueOf("CTRL+V"));

    MenuItem cutEdit = new MenuItem("cut");
    cutEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.cut();
      }
    });
    cutEdit.setAccelerator(KeyCombination.valueOf("CTRL+X"));

    MenuItem selectAllEdit = new MenuItem("Select ALL");
    selectAllEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.selectAll();
      }
    });
    selectAllEdit.setAccelerator(KeyCombination.valueOf("CTRL+A"));
    editOpt.getItems().addAll(
      copyEdit,
      pasteEdit,
      cutEdit,
      selectAllEdit
    );

    //菜单：视觉样式
    Menu viewOpt = new Menu("View");

    //菜单：帮助
    Menu helpOpt = new Menu("Help");
    MenuItem authorHelp = new MenuItem("Author's Blog");
    helpOpt.getItems().addAll(authorHelp);

    //打开作者博客
    authorHelp.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent event) {
        HostServices host = getHostServices();
        host.showDocument("http://139.155.83.108/");
      }
    });

    //菜单栏:添加所有控件
    menuBar.getMenus().addAll(fileOpt,editOpt,viewOpt,helpOpt);

    //设置菜单栏的位置
    AnchorPane.setLeftAnchor(menuBar, 0.0d);
    AnchorPane.setRightAnchor(menuBar, 0.0d);
    AnchorPane.setTopAnchor(menuBar, 0.0d);
    AnchorPane.setBottomAnchor(menuBar, 20.0d);
    return menuBar;
  }

  
  private TextArea initTextArea(){
    //函数：initTextArea
    //功能：返回设定好的TextArea
    textArea = new TextArea();

    //设置提示
    textArea.setPromptText("Write down your knowladge here ~");

    //是否默认聚焦在该部件上:否
    textArea.setFocusTraversable(false);

    //文本自动换行
    textArea.setWrapText(true);

    //去除边框默认样式
    textArea.setStyle(
      "-fx-background-insets: 0;"+
      "-fx-focus-color: transparent;"+
      "-fx-padding: 0;"
    );
    
    //左右自动拉伸
    AnchorPane.setLeftAnchor(textArea, 0.0d);
    AnchorPane.setRightAnchor(textArea,0.0d);

    //menuBar采用默认高度20.0像素点
    AnchorPane.setTopAnchor(textArea, 20.0d);

    //底部提示区长度20.0个像素点
    AnchorPane.setBottomAnchor(textArea, 20.0d);
    return textArea;
  }

  private void openFile(){
    PrintWriter pw = getPrintWriter();
    BufferedReader br = getBufferedReader();
    uploadFile();
    try{
      pw.println("OpenFile");
      pw.flush();
      int totalFileCount = Integer.parseInt(br.readLine());
      List<String> files = new ArrayList<>();
      System.out.println("Available:");
      for(int i = 0; i < totalFileCount ; ++i){
        files.add(br.readLine());
        System.out.println("    "+files.get(i));
      }
      openFileWindow(files);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  private void openFileWindow(List<String> files){
    Stage openFileStage = new Stage();
    System.out.println("file uploaded!");
    openFileStage.setScene(setOpenFileWindow(openFileStage,files));
    openFileStage.setResizable(false);
    openFileStage.setX(100.0d);
    openFileStage.setY(100.0d);
    openFileStage.initOwner(getMainWindow());
    openFileStage.initModality(Modality.WINDOW_MODAL);
    openFileStage.setTitle("Open File");
    openFileStage.setOpacity(0.90d);
    openFileStage.show();
  }
  
  private Scene setOpenFileWindow(Stage stage,List<String> files){
    FlowPane fp = new FlowPane();
    fp.setPrefHeight(0.0d);
    fp.setOrientation(Orientation.VERTICAL);
    fp.setVgap(10.0d);
    fp.setAlignment(Pos.CENTER);
    for(int i = 0;i < files.size() ; ++i){
      Button file = new Button(files.get(i));
      fp.getChildren().add(file);
      file.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event){
          try{
            PrintWriter pw = getPrintWriter();
            BufferedReader br = getBufferedReader();
            pw.println(file.getText());
            pw.flush();
            theEditingFileName = file.getText();
            String file = readAllLine(br);
            System.out.println("Open file "+theEditingFileName);
            pw.println("success!");
            pw.flush();
            textArea.setText(file);
            stage.close();
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
      });
    }
    return new Scene(fp);
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


