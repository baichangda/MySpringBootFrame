package com.bcd.base.security;

import com.bcd.base.exception.BaseRuntimeException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSASecurity {

    /**
     * 算法/模式/补码方式
     */
    public final static String PADDING_TYPE = "RSA/ECB/PKCS1Padding";

    /**
     * 生成密钥对。注意这里是生成密钥对KeyPair，再由密钥对获取公私钥
     *
     * @return 数组, 第一个元素为公钥, 第二个元素为私钥
     */
    public static Object[] generateKey(int size) {
        Singleton.INSTANCE.keyPairGenerator.initialize(size);
        KeyPair keyPair = Singleton.INSTANCE.keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new Object[]{publicKey, privateKey};
    }

    /**
     * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
     *
     * @param keyBytes
     * @return
     */
    public static PublicKey restorePublicKey(byte[] keyBytes) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            return Singleton.INSTANCE.keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 还原私钥，PKCS8EncodedKeySpec 用于构建私钥的规范
     *
     * @param keyBytes
     * @return
     */
    public static PrivateKey restorePrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        try {
            PrivateKey privateKey = Singleton.INSTANCE.keyFactory
                    .generatePrivate(pkcs8EncodedKeySpec);
            return privateKey;
        } catch (InvalidKeySpecException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 加密，三步走。
     *
     * @param key
     * @param plainText
     * @return
     */
    public static byte[] encode(PublicKey key, byte[] plainText) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw BaseRuntimeException.getException(e);
        }

    }

    /**
     * 解密，三步走。
     *
     * @param key
     * @param encodedText
     * @return
     */
    public static String decode(PrivateKey key, byte[] encodedText) {
        try {
            Cipher cipher = Cipher.getInstance(PADDING_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encodedText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static void main(String[] args) {
        Object[] res1 = generateKey(1024);
        PublicKey publicKey1 = (PublicKey) res1[0];
        PrivateKey privateKey1 = (PrivateKey) res1[1];
        System.out.println(Base64.getEncoder().encodeToString(publicKey1.getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(privateKey1.getEncoded()));
        Object[] res2 = generateKey(1024);
        PublicKey publicKey2 = (PublicKey) res2[0];
        PrivateKey privateKey2 = (PrivateKey) res2[1];
        System.out.println(Base64.getEncoder().encodeToString(publicKey2.getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(privateKey2.getEncoded()));

        byte[] b1 = encode(publicKey1, "abc".getBytes());
        byte[] b2 = encode(publicKey2, "def".getBytes());
        System.out.println(new String(decode(privateKey1, b1)));
        System.out.println(new String(decode(privateKey2, b2)));
    }

    enum Singleton {
        INSTANCE;
        KeyPairGenerator keyPairGenerator;
        KeyFactory keyFactory;

        Singleton() {
            try {
                keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyFactory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }
}