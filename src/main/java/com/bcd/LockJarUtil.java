package com.bcd;

import io.xjar.XCryptos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LockJarUtil {

    /**
     *
     * 加密jar并在jar对应的文件夹下面生成
     * result.jar 加密后的jar
     * xjar.go 未编译之前的go脚本文件
     * xjar.? 根据windows、linux环境不同生成的对应可执行程序
     *
     * 可以通过 xjar.? java -jar result.jar 启动jar
     *
     * 注意:
     * 打包的服务器必须安装golang环境
     *
     * @param sourceJarPath
     * @param includeClass
     */
    public static void lockJar(String sourceJarPath,String includeClass){
        try {
            String dirPath=runXjar(sourceJarPath, includeClass);
            goBuild(dirPath);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 依赖xjar对jar进行加密,生成加密后的jar和go脚本文件
     * @param sourceJarPath 源jar路径
     * @param includeClass 需要加密class包
     * @return
     * @throws Exception
     */
    public static String runXjar(String sourceJarPath,String includeClass) throws Exception {
        Path jar= Paths.get(sourceJarPath);
        if(!Files.exists(jar)){
            System.err.println("jar["+sourceJarPath+"] not exists");
        }

        Path result= Paths.get(sourceJarPath.substring(0,sourceJarPath.lastIndexOf("."))+"-lock.jar");
        Files.deleteIfExists(result);

        XCryptos
                .encryption()
                .from(sourceJarPath)
                .use("test")
                .include(includeClass)
                .to(result.toString());
        return result.getParent().toString();
    }

    /**
     * 编译go脚本文件,根据环境不同生成对应的执行程序
     * windows生成xjar.exe
     * linux生成生成xjar.sh
     * @param path go脚本文件夹的路径
     * @throws InterruptedException
     * @throws IOException
     */
    public static void goBuild(String path) throws InterruptedException, IOException {
        String goPath=path+File.separator+"xjar.go";
        String cmdPath=path+File.separator;
        String cmd = "go build -o " + cmdPath + " " + goPath;
        System.out.println("run ["+cmd+"]");
        Process process ;
        if(isWindows()){
            process= Runtime.getRuntime().exec("cmd /c"+cmd);
        }else {
            String[] command = { "/bin/sh", "-c", cmd };
            process= Runtime.getRuntime().exec(command);
        }


        process.waitFor();
        try (InputStream is = process.getErrorStream()) {
            int len = is.available();
            if (len > 0) {
                byte[] result = new byte[len];
                is.read(result);
                System.err.println("go build error result:\n"+new String(result));
            }
        }
    }

    /**
     * 判断是否是windows操作系统
     * @return
     */
    public static boolean isWindows(){
        String os=System.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows")>=0){
            return true;
        }else{
            return false;
        }
    }



    public static void main(String[] args) throws Exception {
        lockJar("D:\\workspace\\zhaoshang\\bwt-vms-user\\target\\bwt-vms-user-0.0.1-SNAPSHOT.jar",
                "/com/bwt/**/*.class");
//        lockJar("/Users/baichangda/hlj/workspace/bwt-vms-electrice-fence/target/electricfence-1.0-SNAPSHOT.jar",
//                "/com/bwt/**/*.class");
    }
}
