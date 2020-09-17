package com.bcd;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class VoiceUtil {

    static final LinkedBlockingQueue<String> blockingQueue=new LinkedBlockingQueue<>();

    /**
     * 开启单线程执行语音播报
     */
    public static void startSpeakVoiceText_windows(){
        Executors.newSingleThreadExecutor().execute(()->{
            try {
                while (true) {
                    //阻塞式调用
                    String text = blockingQueue.take();
                    ActiveXComponent ax=null;
                    Dispatch dispatch=null;
                    try {
                        while(text!=null){
                            //当有文本需要播报时候
                            //初始化windows组件
                            if(ax==null) {
                                ax = new ActiveXComponent("Sapi.SpVoice");
                                // 音量 0-100
                                ax.setProperty("Volume", new Variant(80));
                                // 语音朗读速度 -10 到 +10
                                ax.setProperty("Rate", new Variant(1));
                                // 朗读
                                dispatch = ax.getObject();
                            }
                            //文本不为空则进行播报
                            if (!StringUtils.isEmpty(text)) {
                                Dispatch.call(dispatch, "Speak", new Variant(text));
                            }
                            //播报完后非阻塞式调用检测queue中是否还有消息需要播报,没有则text=null会导致退出while
                            text = blockingQueue.poll();
                        }
                    }finally {
                        //释放组件
                        if(dispatch!=null){
                            dispatch.safeRelease();
                        }
                        if(ax!=null){
                            ax.safeRelease();
                        }
                    }
                }
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        });
    }

    public static void addVoiceText_window(String text){
        blockingQueue.add(text);
    }

    public static void main(String[] args) throws InterruptedException {
        startSpeakVoiceText_windows();
//        for (int i=0;i<2;i++){
//            addVoiceText_window(i+"测试");
////            Thread.sleep(2000L);
//        }
//        Thread.sleep(5000);
        Scanner scanner=new Scanner(System.in);
        while(scanner.hasNext()){
            addVoiceText_window(scanner.next()+"测试");
        }
    }

}
