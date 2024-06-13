package com.bcd.sys.schedule;

import com.bcd.base.exception.BaseException;
import com.bcd.base.util.DateZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Component
@ConditionalOnProperty("database.backup.enable")
public class DataBaseBackupSchedule {

    Logger logger = LoggerFactory.getLogger(DataBaseBackupSchedule.class);

    @Value("${database.backup.host}")
    String host;
    @Value("${database.backup.port}")
    int port;
    @Value("${database.backup.username}")
    String username;
    @Value("${database.backup.password}")
    String password;
    //备份数据库、多个数据库以空格分开
    @Value("${database.backup.databases}")
    String databases;
    @Value("${database.backup.maxFileNum:10}")
    int maxFileNum;

    public DataBaseBackupSchedule() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "mysqldump -h127.0.0.1 -P3306 -uroot -p123456 --databases msbf > backup-20210112143021"});
        process.waitFor();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    @Scheduled(cron = "${database.backup.cron}")
    public void backup() {
        String fileName = "backup-" + DateZoneUtil.dateToString_second(new Date());
        String cmd = "mysqldump -h" + host + " -P" + port + " -u" + username + " -p" + password + " --databases " + databases + " > " + fileName;
        String[] command = {"/bin/sh", "-c", cmd};
        logger.info("execute backup[{}]", cmd);
        Path temp = Paths.get(fileName);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            try (InputStream is = process.getErrorStream()) {
                int len = is.available();
                if (len > 0) {
                    byte[] result = new byte[len];
                    int readRes = is.read(result);
                    logger.error("database backup error read[{}] result:\n{}", readRes, new String(result));
                }
            }

            if (Files.size(temp) == 0) {
                throw BaseException.get("backup failed,can't find temp backup file");
            } else {
                try (InputStream is = Files.newInputStream(temp)) {
                    //上传文件
                    saveBackup(fileName, is);
                    //删除多余文件
                    String[] allFileNames = listBackupFileNames();
                    if (allFileNames.length > maxFileNum) {
                        for (int i = 0; i < allFileNames.length - maxFileNum; i++) {
                            deleteBackup(allFileNames[i]);
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            throw BaseException.get(ex);
        } finally {
            try {
                Files.deleteIfExists(temp);
                logger.info("delete temp backup file[{}]", fileName);
            } catch (IOException e) {
                logger.error("delete temp backup file failed", e);
            }
        }
    }

    public void saveBackup(String fileName, InputStream is) {
        logger.info("add backup file[{}]", fileName);
    }

    public void deleteBackup(String fileName) {
        logger.info("delete backup file[{}]", fileName);
    }

    public String[] listBackupFileNames() {
        return null;
    }
}


