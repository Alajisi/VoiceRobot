package com.zhangxin.jarvis;

import com.iflytek.cloud.speech.*;
import com.zhangxin.jarvis.util.DebugLog;
import com.zhangxin.jarvis.util.JsonParser;
import java.io.IOException;

public class VoiceTest {

    private SpeechRecognizer speechRecognizer;

    private String resultText = "";

    private int time = 0;

    public String getResultText(){return this.resultText;}

    public VoiceTest(){
        this.speechRecognizer = SpeechRecognizer.createRecognizer();
    }

    public void speech(){
        if(!speechRecognizer.isListening())
            speechRecognizer.startListening(listener);
        else
            speechRecognizer.stopListening();
    }


    //编写监听器,创建匿名内部类
    private RecognizerListener listener = new RecognizerListener() {

        /**
         * 当在说话的过程中音量发生变化时会多次调用此函数，显示音量值
         */
        @Override
        public void onVolumeChanged(int arg0) {
            DebugLog.Log("onVolumeChanged enter      "+arg0);

        }

        /**
         * 获取听写结果. 获取RecognizerResult类型的识别结果
         */
        @Override
        public void onResult(RecognizerResult result, boolean flag) {
            DebugLog.Log("onResult enter");
            //这个result就是远程解析的结果
            String strResult = null;
            //这里的捕获异常是我自己修改了JsonParser的这个静态方法，因为他在里面捕获了异常，所以我修改了，我在那里面又抛了一个异常
            //因为这个函数解析result的时候，如果不说话就会打印异常信息，所以受不了，我就把他修改了
            try{
                strResult = JsonParser.parseIatResult(result.getResultString());
            }catch(Exception e){
                strResult = "";
            }
            resultText = resultText + strResult;
            //第二次调用时结果为结尾标点符号，所以对两次结果进行了拼接
            time++;
            if(time==2){
                System.out.println(resultText);
                String str = resultText;
                //String str = "打开微信给张三发消息说明天几点上班";
                String app=str.substring(str.indexOf("打开")+2, str.indexOf("给"));
                String name = str.substring(str.indexOf("给")+1,str.indexOf("发"));
                System.out.println(name);
                String content = str.substring(str.indexOf("说")+1);
                System.out.println(content);
                RobotUtil robot = new RobotUtil();
                if(app.equals("微信")){
                    if (robot.topWeChat()) {
                        // 初始化
                        robot.init();
                        // 指定需要发送消息的联系人/群组
                        robot.queryItemForSendMessage(name); // 微信昵称/群名
                        // 发送字符串消息
                        robot.sendStrMessage(content);
                        // }
                        // 发送图片消息
                        //sendImgMessage("exceltest.jpg"); // 表情包的路径
                    }
                }
            }

            if(strResult.contains("关机")||strResult.contains("关闭电脑")){
                System.out.println("正在关机...");
                try {
                    Runtime.getRuntime().exec("shutdown /s /t " + 1);  //关机时间可以自动设置
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(strResult.contains("重启")||strResult.contains("重新启动")){
                System.out.println("正在重启...");
                try {
                    Runtime.getRuntime().exec("shutdown -r ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        /*
         * 事件 扩展用接口，由具体业务进行约定
         * @see com.iflytek.cloud.speech.RecognizerListener#onEvent(int, int, int, java.lang.String)
         */
        @Override
        public void onEvent(int arg0, int arg1, int arg2, String arg3) {

        }

        @Override
        public void onError(SpeechError arg0) {
            DebugLog.Log("onError enter");
        }

        @Override
        public void onEndOfSpeech() {
            DebugLog.Log("onEndOfSpeech enter");
        }

        /*
         * 结束听写，恢复初始状态
         * @see com.iflytek.cloud.speech.RecognizerListener#onBeginOfSpeech()
         */
        @Override
        public void onBeginOfSpeech() {
            DebugLog.Log("onBeginOfSpeech enter");
        }
    };


    public static void main(String[] args) {
        //这句是必须的，注册的时候必须建一个应用，会分配一个appid，填在这里
        SpeechUtility.createUtility("appid=1111111");
        //初始化这个类的时候，这些函数就调用了，如果不初始化，那个匿名内部类就没办法监听，监听器的方法有的需要重写，有的可以不重写
        VoiceTest speechTest = new VoiceTest();
        //新版api当startListening被调用之后，自动判断音量为静音来自动停止，所以不需要关心停止的s
        speechTest.speech();
    }
}