package com.bcd.base.security;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.tomcat.util.buf.HexUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESSecurity {

    public static final String KEY_ALGORITHM = "AES";

    /**
     * 算法/模式/补码方式
     */
    public final static String PADDING_TYPE = "AES/ECB/PKCS5Padding";

    /**
     * 生成
     *
     * @param size
     * @return
     */
    public static SecretKey generateKey(int size) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            kg.init(size);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 还原key
     *
     * @param key
     * @return
     */
    public static SecretKeySpec restoreKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
     * 根据mysql key字符串得到AES key
     *
     * @param key
     * @return
     */
    public static SecretKeySpec restoreMysqlKey(byte[] key) {
        final byte[] finalKey = new byte[16];
        int i = 0;
        for (byte b : key)
            finalKey[i++ % 16] ^= b;
        return new SecretKeySpec(finalKey, KEY_ALGORITHM);
    }

    /**
     * 加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encode(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException
                | IllegalBlockSizeException | InvalidKeyException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 解密
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] decode(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException
                | IllegalBlockSizeException | InvalidKeyException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static void main(String[] args){
        SecretKey key = generateKey(128);
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
        byte[] res = encode("鄂A12345".getBytes(), key);
        System.out.println(Base64.getEncoder().encodeToString(res));
        System.out.println(new String(decode(res, key)));


        SecretKeySpec mysqlKey = restoreMysqlKey("encryptKey".getBytes());
        byte[] mysqlRes = encode("鄂A12345".getBytes(), mysqlKey);
        System.out.println(HexUtils.toHexString(mysqlRes));
        System.out.println(new String(decode(mysqlRes, mysqlKey)));
        System.out.println(new String(decode(HexUtils.fromHexString("D823BE22DF06BB2E451E96123FED0735"), mysqlKey)));
    }
}