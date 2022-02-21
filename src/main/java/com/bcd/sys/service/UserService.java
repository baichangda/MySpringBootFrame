package com.bcd.sys.service;

import cn.dev33.satoken.secure.SaBase64Util;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.support_spring_init.SpringInitializable;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.RSAUtil;
import com.bcd.base.support_jpa.service.BaseService;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.keys.KeysConst;

import java.util.Base64;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/4/18.
 */
@Service
public class UserService extends BaseService<UserBean, Long> implements SpringInitializable {

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    @Qualifier("string_string_redisTemplate")
    private RedisTemplate<String, String> redisTemplate;


    public UserBean getUser(String username) {
        return findOne(new StringCondition("username", username));
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

    @Override
    public void init(ContextRefreshedEvent event) {
        UserBean userBean = findById(CommonConst.ADMIN_ID);
        if (userBean == null) {
            userBean = new UserBean();
            userBean.setId(CommonConst.ADMIN_ID);
            userBean.setUsername(CommonConst.ADMIN_USERNAME);
            String password;
            if (CommonConst.IS_PASSWORD_ENCODED) {
                password = encryptPassword(CommonConst.ADMIN_USERNAME, CommonConst.INITIAL_PASSWORD);
            } else {
                password = CommonConst.INITIAL_PASSWORD;
            }
            userBean.setPassword(password);
            userBean.setStatus(1);
            save(userBean);
        }
    }

    /**
     * 手机号、验证码登陆
     *
     * @param phone
     * @param phoneCode
     * @return
     */
    public UserBean login_phone(String phone, String phoneCode) {
        final UserBean userBean = findOne(new StringCondition("phone", phone));
        StpUtil.login(userBean.getUsername(), "phone");
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
                password = SaSecureUtil.md5BySalt(RSAUtil.decode(privateKey, Base64.getDecoder().decode(encryptPassword)),username);
            } else {
                password = encryptPassword;
            }
            //验证密码
            final String dbPassword = userBean.getPassword();
            if (password.equals(dbPassword)) {
                StpUtil.login(userBean.getUsername(), "web");
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
        UserBean userBean = findById(userId);
        //2、根据是否加密处理选择不同处理方式
        if (CommonConst.IS_PASSWORD_ENCODED) {
            //2.1、获取私钥
            PrivateKey privateKey = KeysConst.PRIVATE_KEY;
            //2.2、解密密码
            String oldPassword = RSAUtil.decode(privateKey, Base64.getDecoder().decode(encryptOldPassword));
            String newPassword = RSAUtil.decode(privateKey, Base64.getDecoder().decode(encryptNewPassword));
            //2.3、将原始密码MD5加密后与数据库中进行对比
            if (userBean.getPassword().equals(encryptPassword(userBean.getUsername(), oldPassword))) {
                //2.4、使用MD5加密、盐值使用用户名
                userBean.setPassword(encryptPassword(userBean.getUsername(), newPassword));
                save(userBean);
                return true;
            } else {
                return false;
            }
        } else {
            //3、如果不加密,则直接对比
            if (userBean.getPassword().equals(encryptOldPassword)) {
                userBean.setPassword(encryptNewPassword);
                save(userBean);
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
        //1、重置密码
        UserBean userBean = findById(userId);
        //2、设置默认密码
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("password", encryptPassword(userBean.getUsername(), CommonConst.INITIAL_PASSWORD));
        update(new NumberCondition("id", userId), paramMap);
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
            userBean = findOne(new StringCondition("phone", phone));
        }

        if (userBean == null) {
            return;
        }

        StpUtil.kickout(userBean.getUsername());
    }

    public void saveUser(UserBean user) {
        if (user.getId() == null) {
            user.setPassword(encryptPassword(user.getUsername(), CommonConst.INITIAL_PASSWORD));
            user.setStatus(1);
            save(user);
        } else {
            UserBean dbUser = findById(user.getId());
            user.setPassword(dbUser.getPassword());
            save(user);
        }
    }
}
