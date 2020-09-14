package com.bcd;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 文字转语音测试 jdk bin文件中需要导入jacob-1.17-M2-x64.dll
 * 
 * @author zk
 * @date: 2019年6月25日 上午10:05:21
 */
public class JacobTest {

    public static void main(String[] args) {
//        textToSpeech("test测试重庆重要一二三二一gog");
        textToSpeech("test测试");
    }

    /**
     * 语音转文字并播放
     * 
     * @param text
     */
    public static void textToSpeech(String text) {
        ActiveXComponent ax = null;
        try {
            ax = new ActiveXComponent("Sapi.SpVoice");

            // 运行时输出语音内容
            Dispatch spVoice1 = ax.getObject();
            // 音量 0-100
            ax.setProperty("Volume", new Variant(80));
            // 语音朗读速度 -10 到 +10
            ax.setProperty("Rate", new Variant(-5));
            // 执行朗读
//            Dispatch.call(spVoice, "Speak", new Variant(text));

            // 运行时输出语音内容
            Dispatch spVoice2 = ax.getObject();
            // 音量 0-100
            ax.setProperty("Volume", new Variant(50));
            // 语音朗读速度 -10 到 +10
            ax.setProperty("Rate", new Variant(1));

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
                // 执行朗读
                Dispatch.call(spVoice1, "Speak", new Variant("一二三四"));
            },100,10, TimeUnit.MILLISECONDS);

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
                // 执行朗读
                Dispatch.call(spVoice2, "Speak", new Variant("我是哪吒"));
            },100,10, TimeUnit.MILLISECONDS);


            Thread.sleep(10*1000);
            // 下面是构建文件流把生成语音文件
//
//            ax = new ActiveXComponent("Sapi.SpFileStream");
//            Dispatch spFileStream = ax.getObject();
//
//            ax = new ActiveXComponent("Sapi.SpAudioFormat");
//            Dispatch spAudioFormat = ax.getObject();
//
//            // 设置音频流格式
//            Dispatch.put(spAudioFormat, "Type", new Variant(22));
//            // 设置文件输出流格式
//            Dispatch.putRef(spFileStream, "Format", spAudioFormat);
//            // 调用输出 文件流打开方法，创建一个.wav文件
//            Dispatch.call(spFileStream, "Open", new Variant("./text.mp3"), new Variant(3), new Variant(true));
//            // 设置声音对象的音频输出流为输出文件对象
//            Dispatch.putRef(spVoice, "AudioOutputStream", spFileStream);
//            // 设置音量 0到100
//            Dispatch.put(spVoice, "Volume", new Variant(100));
//            // 设置朗读速度
//            Dispatch.put(spVoice, "Rate", new Variant(-2));
//            // 开始朗读
//            Dispatch.call(spVoice, "Speak", new Variant(text));
//
//            // 关闭输出文件
//            Dispatch.call(spFileStream, "Close");
//            Dispatch.putRef(spVoice, "AudioOutputStream", null);
//
//            spAudioFormat.safeRelease();
//            spFileStream.safeRelease();
            spVoice1.safeRelease();
            spVoice2.safeRelease();
            ax.safeRelease();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}