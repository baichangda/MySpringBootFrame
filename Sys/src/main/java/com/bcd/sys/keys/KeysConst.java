package com.bcd.sys.keys;

import com.bcd.base.security.RSASecurity;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.event.ContextRefreshedEvent;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class KeysConst {
    /**
     * 公钥
     */
    public static String PUBLIC_KEY_BASE64="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsvr4c0hE5irw5Ye6Kn" +
            "yyHmUgPv5bYGx5cYRDhyyK1NuACXzvW2CFwNmh65LoaPChE/R31MKuKkud4izjR6qCBMaaOg9YQmN" +
            "wIQpqWU4MOIcaBW30ylccjaYoXzumXG34CibBam/RyYF5k+FTDyrD+fCJXpBK9er/pAE1em/kkBQI" +
            "DAQAB";
    public static PublicKey PUBLIC_KEY;
    /**
     * 私钥
     */
    public static String PRIVATE_KEY_BASE64 ="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKy+vhzS" +
            "ETmKvDlh7oqfLIeZSA+/ltgbHlxhEOHLIrU24AJfO9bYIXA2aHrkuho8KET9HfUwq4qS53iLONHqo" +
            "IExpo6D1hCY3AhCmpZTgw4hxoFbfTKVxyNpihfO6ZcbfgKJsFqb9HJgXmT4VMPKsP58IlekEr16v+" +
            "kATV6b+SQFAgMBAAECgYBPK4xOASjLyn3BftSoy5LJAsM4FIK5wJQFmqb2FPdvPhskeykdqiiJGSa" +
            "BTFrOs0txcuBMA2ZbOEDFymjcLEAqtQ3KIhcw0LWL9oQ/E5y4fjb1vyfG3sn3yseGUSCDgoUXSp7U" +
            "IKSlxxjbZzzu+wwz6N2EWnJ83DnPCjb0EU7EgQJBANlOxtLQTiJ65Ng68gddUjmqlpyzAHWawET0u" +
            "XX5Frm87p8BjBVXMKiAGPPIObmWksiz2FJmyftdp6T1MpU99W0CQQDLgLtimUC1H5ti3xmK4RIraY" +
            "L755Dy7PQQPHI3vk/gZwY2ywvCpPmdbJyLEybBeGNoR58gqBrIdIj8G3fPwAH5AkEAvSWIge62U+T" +
            "MVDnaePaNn4wQVIyqFbOBL4Qj+b+6PClrOhPKrriZrdDx5x+cvyGE2hVQcUju/lBin36dbLHlcQJA" +
            "Gxx0WmdmWnryfZKRWZIwlH4DCEJKakKtJTiYUtrU02WGS2hzkaPe6V0d4d1UTXQXcj4Qcg5TOx9jX" +
            "IrgRFolKQJBAM6Q8OSKkBCd95B1lqtd2pxsbLRryKeBGGtS4bS9AU6QETCmpl25z9e5TCaPQKjmTA" +
            "kPotkbN3cYC8KUb1CBbdg=";
    public static PrivateKey PRIVATE_KEY;


    /**
     * 是否是集群环境
     */
    public static final boolean IS_CLUSTER=false;


    /**
     * 单机环境参数
     */

    /**
     * 是否在启动时候重新生成 公钥私钥(仅在单机环境有效)
     */
    public static final boolean IS_GENERATE_KEY_ON_STARTUP=false;

    /**
     * 单机环境初始化
     */
    static{
        if(!IS_CLUSTER) {
            if (IS_GENERATE_KEY_ON_STARTUP) {
                Object[] keys = RSASecurity.generateKey();
                KeysConst.PUBLIC_KEY = (RSAPublicKey) keys[0];
                KeysConst.PRIVATE_KEY = (RSAPrivateKey) keys[1];
                KeysConst.PUBLIC_KEY_BASE64 = Base64.encodeBase64String(KeysConst.PUBLIC_KEY.getEncoded());
                KeysConst.PRIVATE_KEY_BASE64 = Base64.encodeBase64String(KeysConst.PRIVATE_KEY.getEncoded());
            } else {
                KeysConst.PUBLIC_KEY = RSASecurity.restorePublicKey(Base64.decodeBase64(KeysConst.PUBLIC_KEY_BASE64));
                KeysConst.PRIVATE_KEY = RSASecurity.restorePrivateKey(Base64.decodeBase64(KeysConst.PRIVATE_KEY_BASE64));
            }
        }
    }



    /**
     * 集群环境参数
     * 初始化见
     * @see RedisKeysInit#onApplicationEvent(ContextRefreshedEvent)
     */

    /**
     * redis 存储公钥私钥redis key名(仅在集群环境有效)
     */
    public static final String REDIS_KEY_NAME="publicPrivateKeys";


}
