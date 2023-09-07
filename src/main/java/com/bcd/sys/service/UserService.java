package com.bcd.sys.service;

import cn.dev33.satoken.secure.SaBase64Util;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.support_jdbc.service.BaseService;
import com.bcd.base.support_jdbc.service.ParamPairs;
import com.bcd.base.util.RSAUtil;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.keys.KeysConst;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/4/18.
 */
@Service
public class UserService extends BaseService<Long,UserBean> implements ApplicationListener<ContextRefreshedEvent> {

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    @Qualifier("string_string_redisTemplate")
    private RedisTemplate<String, String> redisTemplate;


    public UserBean getUser(String username) {
        return get(StringCondition.EQUAL("username", username));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        UserBean userBean = get(CommonConst.ADMIN_ID);
        if (userBean == null) {
            userBean = new UserBean();
            userBean.id = CommonConst.ADMIN_ID;
            userBean.username = CommonConst.ADMIN_USERNAME;
            String password;
            if (CommonConst.IS_PASSWORD_ENCODED) {
                password = encryptPassword(CommonConst.ADMIN_USERNAME, CommonConst.INITIAL_PASSWORD);
            } else {
                password = CommonConst.INITIAL_PASSWORD;
            }
            userBean.password = password;
            userBean.status = 1;
            insert(userBean);
        }
    }

    public static void main(String[] args) {
        String username = "admin";
        String password = "123qwe";
        // 加密
        String encodedText = SaBase64Util.encodeBytesToString(RSAUtil.encode(KeysConst.PUBLIC_KEY, password.getBytes()));

        logger.info("encodedBase64: {}", encodedText);

        //数据库密码
        String dbPwd = SaSecureUtil.md5BySalt(password, username);

        logger.info("dbPwd: {}", dbPwd);

        // 解密
        String decodedText = RSAUtil.decode(KeysConst.PRIVATE_KEY, SaBase64Util.decodeStringToBytes(encodedText));

        logger.info("decodedText: {}", decodedText);
    }

    /**
     * 手机号、验证码登陆
     *
     * @param phone
     * @param phoneCode
     * @return
     */
    public UserBean login_phone(String phone, String phoneCode) {
        final UserBean userBean = get(StringCondition.EQUAL("phone", phone));
        StpUtil.login(userBean.username, "phone");
        return userBean;
    }

    /**
     * 发送随机验证码
     *
     * @param phone
     */
    public void sendPhoneCode(String phone) {
        String key = "phoneCode:" + phone;
        long expireTimeInSeconds = redisTemplate.getExpire(key);
        if (expireTimeInSeconds > 0) {
            throw BaseRuntimeException.getException("等待" + expireTimeInSeconds + "秒后重试");
        } else {
            if (expireTimeInSeconds == -1) {
                //如果没有过期时间,则删除异常key
                redisTemplate.delete(key);
            } else {
                //如果不存在,则构造key发送短信
                String phoneCode = RandomStringUtils.randomNumeric(6);
                boolean res = redisTemplate.opsForValue().setIfAbsent("phoneCode:" + phone, phoneCode, 3 * 60, TimeUnit.SECONDS);
                if (res) {
                    //todo 发送短信

                } else {
                    //如果有其他服务器抢先发送了验证码,则再次获取时间
                    expireTimeInSeconds = redisTemplate.getExpire(key);
                    throw BaseRuntimeException.getException("等待{0}秒后重试", expireTimeInSeconds);
                }
            }
        }
    }

    /**
     * 用户名、密码登陆
     *
     * @param username
     * @param encryptPassword 使用公钥加密后的密码
     * @return
     */
    public UserBean login(String username, String encryptPassword) {
        final UserBean userBean = getUser(username);
        if (userBean == null) {
            throw BaseRuntimeException.getException("用户不存在");
        } else {
            //根据是否加密处理选择不同处理方式
            String password;
            if (CommonConst.IS_PASSWORD_ENCODED) {
                //使用私钥解密密码
                PrivateKey privateKey = KeysConst.PRIVATE_KEY;
                password = SaSecureUtil.md5BySalt(RSAUtil.decode(privateKey, Base64.getDecoder().decode(encryptPassword)), username);
            } else {
                password = encryptPassword;
            }
            //验证密码
            final String dbPassword = userBean.password;
            if (password.equals(dbPassword)) {
                StpUtil.login(userBean.username, "web");
                return userBean;
            } else {
                throw BaseRuntimeException.getException("密码错误");
            }
        }
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param encryptOldPassword 使用公钥加密后的原始密码
     * @param encryptNewPassword 使用公钥加密后的新密码
     */
    public boolean updatePassword(Long userId, String encryptOldPassword, String encryptNewPassword) {
        //1、查找当前用户
        UserBean userBean = get(userId);
        //2、根据是否加密处理选择不同处理方式
        if (CommonConst.IS_PASSWORD_ENCODED) {
            //2.1、获取私钥
            PrivateKey privateKey = KeysConst.PRIVATE_KEY;
            //2.2、解密密码
            String oldPassword = RSAUtil.decode(privateKey, Base64.getDecoder().decode(encryptOldPassword));
            String newPassword = RSAUtil.decode(privateKey, Base64.getDecoder().decode(encryptNewPassword));
            //2.3、将原始密码MD5加密后与数据库中进行对比
            if (userBean.password.equals(encryptPassword(userBean.username, oldPassword))) {
                //2.4、使用MD5加密、盐值使用用户名
                update(userId, ParamPairs.build("password", encryptPassword(userBean.username, newPassword)));
                return true;
            } else {
                return false;
            }
        } else {
            //3、如果不加密,则直接对比
            if (userBean.password.equals(encryptOldPassword)) {
                update(userId, ParamPairs.build("password", encryptNewPassword));
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 加密密码
     *
     * @param username
     * @param password
     * @return
     */
    public String encryptPassword(String username, String password) {
        if (CommonConst.IS_PASSWORD_ENCODED) {
            return SaSecureUtil.md5BySalt(password, username);
        } else {
            return password;
        }
    }

    public void resetPassword(Long userId) {
        update(userId, ParamPairs.build("password", CommonConst.INITIAL_PASSWORD));
    }

    /**
     * 踢出用户名用户、手机号用户
     * 根据传参数不同踢出对应登陆的用户
     *
     * @param username
     * @param phone
     */
    public void kickUser(String username, String phone) {
        UserBean userBean = null;
        if (username != null) {
            userBean = getUser(username);
        }

        if (userBean == null && phone != null) {
            userBean = get(StringCondition.EQUAL("phone", phone));
        }

        if (userBean == null) {
            return;
        }

        StpUtil.kickout(userBean.username);
    }

    public void saveUser(UserBean user) {
        if (user.id == null) {
            user.password = encryptPassword(user.username, CommonConst.INITIAL_PASSWORD);
            user.status = 1;
        } else {
            UserBean dbUser = get(user.id);
            user.password = dbUser.password;
        }
        save(user);
    }
}
