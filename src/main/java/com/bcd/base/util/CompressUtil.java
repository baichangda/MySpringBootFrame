package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtil {
    /**
     * gzip压缩
     *
     * @param data
     * @return
     */
    public static byte[] gzip(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             GZIPOutputStream gos = new GZIPOutputStream(os)) {
            gos.write(data);
            return os.toByteArray();
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 解压zip格式数据
     *
     * @param data
     * @param batchSize 中间缓存临时数组长度
     * @return
     */
    public static byte[] unGzip(byte[] data, int batchSize) {
        byte[] res;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            unGzip(bis, os, batchSize);
            res = os.toByteArray();
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
        return res;
    }

    /**
     * 解压指定输入流到输出流中
     *
     * @param is
     * @param batchSize 中间缓存临时数组长度
     */
    public static void unGzip(InputStream is, OutputStream os, int batchSize) {
        try (GZIPInputStream gis = new GZIPInputStream(is)) {
            int count;
            byte[] bytes = new byte[batchSize];
            while ((count = gis.read(bytes, 0, bytes.length)) != -1) {
                os.write(bytes, 0, count);
            }
        } catch (IOException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
