package com.bcd.sys.service;

import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.security.RSASecurity;
import com.bcd.rdb.service.BaseService;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author acemma
 * Created by Administrator on 2017/4/18.
 */
@Service
public class UserService  extends BaseService<UserBean,Long> {
    /**
     * 登录
     * @param username
     * @param encryptPassword 使用公钥加密后的密码
     * @param timeZone
     * @return
     */
    public UserBean login(String username,String encryptPassword,String timeZone){
        //1、构造shiro登录对象
        UsernamePasswordToken token;
        //根据是否加密处理选择不同处理方式
        if(CommonConst.IS_PASSWORD_ENCODED){
            //2.1、使用私钥解密密码
            PrivateKey privateKey = RSASecurity.restorePrivateKey(RSASecurity.keyMap.get(RSASecurity.PRIVATE_KEY));
            String password= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptPassword));
            //2.2、构造登录对象
            token = new UsernamePasswordToken(username, password);
        }else{
            token = new UsernamePasswordToken(username, encryptPassword);
        }
        //3、获取当前subject
        Subject currentUser = SecurityUtils.getSubject();
        //4、进行登录操作
        currentUser.login(token);
        //5、设置过期时间
        currentUser.getSession().setTimeout(-1000l);
        //6、设置用户信息到session中
        UserBean user= findOne(
                new StringCondition("username",username, StringCondition.Handler.EQUAL)
        );
        user.setTimeZone(timeZone);
        currentUser.getSession().setAttribute("user",user);
        return user;
    }

    /**
     * 修改密码
     * @param userId
     * @param encryptOldPassword 使用公钥加密后的原始密码
     * @param encryptNewPassword 使用公钥加密后的新密码
     */
    public boolean updatePassword(Long userId,String encryptOldPassword,String encryptNewPassword){
        //1、查找当前用户
        UserBean sysUserDTO= findById(userId);
        //2、根据是否加密处理选择不同处理方式
        if(CommonConst.IS_PASSWORD_ENCODED){
            //2.1、获取私钥
            PrivateKey privateKey = RSASecurity.restorePrivateKey(RSASecurity.keyMap.get(RSASecurity.PRIVATE_KEY));
            //2.2、解密密码
            String oldPassword= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptOldPassword));
            String newPassword= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptNewPassword));
            //2.3、将原始密码MD5加密后与数据库中进行对比
            if(sysUserDTO.getPassword().equals(encryptPassword(sysUserDTO.getUsername(), oldPassword))) {
                //2.4、使用MD5加密、盐值使用用户名
                sysUserDTO.setPassword(encryptPassword(sysUserDTO.getUsername(), newPassword));
                save(sysUserDTO);
                return true;
            }else{
                return false;
            }
        }else{
            //3、如果不加密,则直接对比
            if(sysUserDTO.getPassword().equals(encryptOldPassword)){
                sysUserDTO.setPassword(encryptNewPassword);
                save(sysUserDTO);
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * 加密密码
     * @param username
     * @param password
     * @return
     */
    private String encryptPassword(String username,String password){
        if(CommonConst.IS_PASSWORD_ENCODED){
            return new Md5Hash(password,username).toBase64();
        }else{
            return password;
        }
    }


    public static void main(String [] args){
        Map<String, byte[]> keyMap = RSASecurity.generateKeyBytes();
        String pwd="123qwe";
        String publicKeyStr="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAighwVytpiItt2nqesNkS12uxr575syUJXk+bwZqQteRCgCRfOjV+bCrLNTEAP4JozyAzbTYF8lEbOn86V5nq9SDqxBCN/+m6kysGrGOTFcHbBA8oCdykhgMUdok7rUBu2b4TLy29dYg9560xsku1g4i4+P8cv0Z2+T7TgeqaganlGQF5wJZ3NmIvVztSTn8HLLZOyy6QHQh1xTeYOq+3O9GbOUwwJK3i62115V2jHWhykhlt+wxkq5YSQGsZzT0VDevjfDZa37QWySitBg9K1YwC5Rqfe3tA+ezPR8yfj/cm2eOeeZw1gPohDy1CNuCDUUMlu8qr5PGBUq99Hdd2wQIDAQAB";
        String privateKeyStr="";
        PublicKey publicKey= RSASecurity.restorePublicKey(Base64.decodeBase64(publicKeyStr));
        // 加密
        byte[] encodedText = RSASecurity.encode(publicKey, pwd.getBytes());
        System.out.println("RSA encoded: " + Base64.encodeBase64String(encodedText));

        // 解密
//        PrivateKey privateKey = RSASecurity.restorePrivateKey(Base64.decodeBase64(privateKeyStr));
//        System.out.println("RSA decoded: "
//                + RSASecurity.decode(privateKey, Base64.decodeBase64(Base64.encodeBase64String(encodedText))));
    }

    public void resetPassword(Long userId) {
        //1、重置密码
        UserBean sysUserDTO= findById(userId);
        //2、设置默认密码
        update(new NumberCondition("id",userId),new HashMap<String,Object>(){{
            put("password",encryptPassword(sysUserDTO.getUsername(),CommonConst.INITIAL_PASSWORD));
        }});
    }
}
