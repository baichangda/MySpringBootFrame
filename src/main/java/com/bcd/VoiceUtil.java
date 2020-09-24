package com.bcd;

import com.bcd.base.exception.BaseRuntimeException;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class VoiceUtil {

    static LinkedBlockingQueue<String> windows_blockingQueue;

    volatile static boolean windows_run;

    static ExecutorService windows_pool;

    public static void stopSpeakVoiceText_windows(){
        //停止线程池
        windows_pool.shutdown();
        //打上停止循环标记
        windows_run=false;
        //等待任务结束
        try {
            while(!windows_pool.awaitTermination(60, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException e) {
            throw BaseRuntimeException.getException(e);
        }
        //销毁队列
        windows_blockingQueue=null;
    }

    /**
     * 开启单线程执行语音播报
     */
    public static void startSpeakVoiceText_windows(){
        windows_run=true;
        windows_blockingQueue=new LinkedBlockingQueue<>();
        windows_pool=Executors.newSingleThreadExecutor();
        windows_pool.execute(()->{
            try {
                while (windows_run) {

                    //阻塞式调用
                    String text = windows_blockingQueue.take();
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
                            text = windows_blockingQueue.poll();
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

    public static void addVoiceText_windows(String text){
        windows_blockingQueue.add(text);
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
            addVoiceText_windows(scanner.next()+"测试");
        }
    }

}
