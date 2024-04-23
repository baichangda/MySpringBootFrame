package com.bcd.base.util;


import com.bcd.base.exception.MyException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class FileUtil {
    /**
     * 删除文件
     * 或者递归删除文件夹
     *
     * @param path
     */
    public static void deleteDirRecursion(Path path) {
        if (Files.notExists(path) || !Files.isDirectory(path)) {
            return;
        }
        clearDirRecursion(path);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw MyException.get(ex);
        }
    }

    /**
     * 递归删除文件夹
     *
     * @param path
     */
    public static void clearDirRecursion(Path path) {
        if (Files.notExists(path) || !Files.isDirectory(path)) {
            return;
        }
        try (final Stream<Path> stream = Files.list(path)) {
            List<Path> collect = stream.toList();
            for (Path p : collect) {
                try {
                    if (Files.isDirectory(p)) {
                        clearDirRecursion(p);
                    }
                    Files.delete(p);
                } catch (IOException ex) {
                    throw MyException.get(ex);
                }
            }
        } catch (IOException ex) {
            throw MyException.get(ex);
        }
    }

    public static void main(String[] args) {
        clearDirRecursion(Paths.get("/Users/baichangda/Downloads/PDAA202142010000520557"));
        deleteDirRecursion(Paths.get("/Users/baichangda/Downloads/PDAA202142010000520557"));
    }
}
