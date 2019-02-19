package com.bcd.sys.rdb.service;

import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.security.RSASecurity;
import com.bcd.rdb.service.BaseService;
import com.bcd.sys.UserDataInit;
import com.bcd.sys.rdb.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.define.MessageDefine;
import com.bcd.sys.keys.KeysConst;
import com.bcd.sys.rdb.shiro.MyShiroRealm;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/4/18.
 */
@Service
public class UserService extends BaseService<UserBean,Long>  implements UserDataInit {

    private final static Logger logger= LoggerFactory.getLogger(UserService.class);

    @Autowired
    private MyShiroRealm myShiroRealm;

    @Override
    public void init() {
        String username="admin";
        UserBean userBean= findOne(new StringCondition("username",username));
        if(userBean==null){
            userBean=new UserBean();
            userBean.setUsername(username);
            String password;
            if(CommonConst.IS_PASSWORD_ENCODED){
                password=encryptPassword(username, CommonConst.INITIAL_PASSWORD);
            }else{
                password= CommonConst.INITIAL_PASSWORD;
            }
            userBean.setPassword(password);
            userBean.setStatus(1);
            save(userBean);
        }
    }

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
        //2、根据是否加密处理选择不同处理方式
        if(CommonConst.IS_PASSWORD_ENCODED){
            //2.1、使用私钥解密密码
            PrivateKey privateKey = KeysConst.PRIVATE_KEY;
            String password= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptPassword));
            //2.2、构造登录对象
            token = new UsernamePasswordToken(username, password);
        }else{
            token = new UsernamePasswordToken(username, encryptPassword);
        }
        //2.3、设置记住我
        token.setRememberMe(true);
        //3、获取当前subject
        Subject currentUser = SecurityUtils.getSubject();
        //4、进行登录操作
        currentUser.login(token);
        //5、设置用户信息到session中
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
        UserBean userBean= findById(userId);
        //2、根据是否加密处理选择不同处理方式
        if(CommonConst.IS_PASSWORD_ENCODED){
            //2.1、获取私钥
            PrivateKey privateKey = KeysConst.PRIVATE_KEY;
            //2.2、解密密码
            String oldPassword= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptOldPassword));
            String newPassword= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptNewPassword));
            //2.3、将原始密码MD5加密后与数据库中进行对比
            if(userBean.getPassword().equals(encryptPassword(userBean.getUsername(), oldPassword))) {
                //2.4、使用MD5加密、盐值使用用户名
                userBean.setPassword(encryptPassword(userBean.getUsername(), newPassword));
                save(userBean);
                return true;
            }else{
                return false;
            }
        }else{
            //3、如果不加密,则直接对比
            if(userBean.getPassword().equals(encryptOldPassword)){
                userBean.setPassword(encryptNewPassword);
                save(userBean);
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
    public String encryptPassword(String username,String password){
        if(CommonConst.IS_PASSWORD_ENCODED){
            return new Md5Hash(password,username).toBase64();
        }else{
            return password;
        }
    }


    public static void main(String [] args){
        String username="admin";
        String password="123qwe";
        // 加密
        String encodedText = Base64.encodeBase64String(RSASecurity.encode(KeysConst.PUBLIC_KEY, password.getBytes()));

        System.out.println(String.format("RSA encoded: %s",encodedText));

        //数据库密码
        String dbPwd=new Md5Hash(password,username).toBase64();
        System.out.println(String.format("dbPwd: %s",dbPwd));

        // 解密
        String decodedText=RSASecurity.decode(KeysConst.PRIVATE_KEY, Base64.decodeBase64(encodedText));
        System.out.println(String.format("RSA decoded: %s",decodedText));
    }

    public void resetPassword(Long userId) {
        //1、重置密码
        UserBean userBean= findById(userId);
        //2、设置默认密码
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("password",encryptPassword(userBean.getUsername(),CommonConst.INITIAL_PASSWORD));
        update(new NumberCondition("id",userId),paramMap);
    }

    public void runAs(Long ... ids) {
        Subject subject= SecurityUtils.getSubject();
        if(subject.isRunAs()){
            throw MessageDefine.ERROR_HAS_RUN_AS.toRuntimeException();
        }
        List<UserBean> userBeanList= findAllById(ids);
        SimplePrincipalCollection simplePrincipalCollection=new SimplePrincipalCollection();
        simplePrincipalCollection.add(userBeanList.stream().map(UserBean::getUsername).collect(Collectors.toList()),myShiroRealm.getName());
        subject.runAs(simplePrincipalCollection);
    }

    public void releaseRunAs() {
        Subject subject= SecurityUtils.getSubject();
        if(!subject.isRunAs()){
            throw MessageDefine.ERROR_NOT_RUN_AS.toRuntimeException();
        }
        subject.releaseRunAs();
    }
}
