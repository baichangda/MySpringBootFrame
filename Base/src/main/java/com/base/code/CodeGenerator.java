package com.base.code;

import com.base.exception.BaseRuntimeException;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/28.
 */
public class CodeGenerator {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    private String templateDirPath = "template";

    public void generate(List<ConfigProperties> configList) {

    }

    private void generate(ConfigProperties config) {
        String dirPath=config.getDirPath();
        Path templateDirPathObj= Paths.get(templateDirPath);
        try {
            Files.walkFileTree(templateDirPathObj, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    //1、先创建对应的文件
                    Path sub= file.subpath(templateDirPathObj.getNameCount(),file.getNameCount());
                    StringBuffer newPath=new StringBuffer();
                    newPath.append(dirPath.toString());
                    newPath.append(File.pathSeparator);
                    newPath.append(sub.toString().replace("\\",File.pathSeparator));
                    Path newPathObj= Paths.get(newPath.toString());
                    Files.deleteIfExists(newPathObj);
                    createPath(newPathObj,1);
                    //2、获取模版文件流@{}
                    try(
                    BufferedReader br=new BufferedReader(new FileReader(file.toFile()));
                    PrintWriter pw=new PrintWriter(new FileWriter(newPathObj.toFile(),true))
                    ){
//                        String str=br.readLine();
//                        str.
//                        pw.print(str);
//                        pw.flush();
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String replaceVarWithValue(String templateStr){
        Pattern pattern=Pattern.compile("^(@\\{).+(\\}@)$");
        return "";
    }

    /**
     * 创建文件或者文件夹
     *
     * @param path
     * @param flag
     * @return
     */
    public static Path createPath(Object path,int flag) {
        Path pathObj;
        if(Path.class.isAssignableFrom(path.getClass())){
            pathObj=(Path)path;
        }else if(String.class.isAssignableFrom(path.getClass())){
            pathObj = Paths.get((String)path);
        }else{
            return null;
        }
        if(Files.exists(pathObj)){
            return pathObj;
        }
        try {
            if(flag==1){
                Files.createDirectories(pathObj.getParent());
                Files.createFile(pathObj);
            } else {
                Files.createDirectories(pathObj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathObj;
    }

    public static void main(String[] args) throws IOException {
//        Path p=createPath("d:/a/b/c",2);
//        createPath("d:/m/m/c/g.txt",1);
//        System.out.println(p.getNameCount());
//        System.out.println(p.subpath(0,p.getNameCount()));

        Pattern pattern=Pattern.compile("(.*@\\{)(.+)(\\}@.*)");
        Matcher matcher= pattern.matcher("public void main @{abc}@ aaaaa @{bbb}@");
        matcher.matches();
        for(int i=1;i<=matcher.groupCount();i++){
            System.out.println(matcher.group(i));
        }
    }
}
