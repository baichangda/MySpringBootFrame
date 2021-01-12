package com.bcd.sys.schedule;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.DateZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Component
@ConditionalOnProperty("database.backup.enable")
public class DataBaseBackupSchedule {

    Logger logger= LoggerFactory.getLogger(DataBaseBackupSchedule.class);

    @Value("${database.backup.host}")
    String host;
    @Value("${database.backup.port}")
    int port;
    @Value("${database.backup.username}")
    String username;
    @Value("${database.backup.password}")
    String password;
    @Value("${database.backup.databases}")
    String databases;
    @Value("${database.backup.maxFileNum:10}")
    int maxFileNum;

    public DataBaseBackupSchedule() {
    }

    @Scheduled(cron = "${database.backup.cron}")
    public void backup(){
        String fileName= "backup-"+ DateZoneUtil.dateToString(new Date(),"yyyyMMddHHmmss");
        String cmd="mysqldump -h"+host+" -P"+port+" -u"+username+" -p"+password+" --databases "+databases+" > "+fileName;
        String[] command = { "/bin/sh", "-c", cmd };
        logger.info("execute backup[{}]",cmd);
        Path temp = Paths.get(fileName);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            try (InputStream is = process.getErrorStream()) {
                int len = is.available();
                if (len > 0) {
                    byte[] result = new byte[len];
                    int readRes=is.read(result);
                    logger.error("database backup error read[{}] result:\n{}",readRes, new String(result));
                }
            }

            if (Files.size(temp)==0) {
                throw BaseRuntimeException.getException("backup failed,can't find temp backup file");
            } else {
                try (InputStream is = Files.newInputStream(temp)) {
                    //上传文件
                    saveBackup(fileName,is);
                    //删除多余文件
                    String[] allFileNames= listBackupFileNames();
                    if(allFileNames.length>maxFileNum){
                        for(int i=0;i<allFileNames.length-maxFileNum;i++){
                            deleteBackup(allFileNames[i]);
                        }
                    }
                }
            }
        }catch (IOException | InterruptedException ex){
            throw BaseRuntimeException.getException(ex);
        }finally {
            try {
                Files.deleteIfExists(temp);
                logger.info("delete temp backup file[{}]",fileName);
            } catch (IOException e) {
                logger.error("delete temp backup file failed",e);
            }
        }
    }



    public void saveBackup(String fileName,InputStream is){
        logger.info("add backup file[{}]", fileName);
    }

    public void deleteBackup(String fileName){
        logger.info("delete backup file[{}]", fileName);
    }

    public String[] listBackupFileNames(){
        return null;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Process process=Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c","mysqldump -h127.0.0.1 -P3306 -uroot -p1234561 --databases msbf > backup-20210112143021"});
        process.waitFor();
        try(BufferedReader br=new BufferedReader(new InputStreamReader(process.getErrorStream()))){
            String line;
            while((line=br.readLine())!=null){
                System.out.println(line);
            }
        }
    }
}


