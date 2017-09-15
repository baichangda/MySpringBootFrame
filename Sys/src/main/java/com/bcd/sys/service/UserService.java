package com.bcd.sys.service;

import com.bcd.base.condition.Condition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.security.RSASecurity;
import com.bcd.rdb.service.BaseService;
import com.bcd.sys.bean.UserBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

/**
 * @author acemma
 * Created by Administrator on 2017/4/18.
 */
@Service
public class UserService  extends BaseService<UserBean,Long> {
    @Autowired
    private RoleService roleService;

    @Autowired
    private OrgService orgService;




    /**
     * 登录
     * @param username
     * @param encryptPassword 使用公钥加密后的密码
     * @param timeZone
     * @return
     */
    public UserBean login(String username,String encryptPassword,String timeZone){
        //1、构造shiro登录对象
        //1.1、使用私钥解密密码
        PrivateKey privateKey = RSASecurity.restorePrivateKey(RSASecurity.keyMap.get(RSASecurity.PRIVATE_KEY));
        String password= RSASecurity.RSADecode(privateKey, Base64.decodeBase64(encryptPassword));
        //1.2、构造登录对象
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        //2、获取当前subject
        Subject currentUser = SecurityUtils.getSubject();
        //3、进行登录操作
        currentUser.login(token);
        //4、设置过期时间
        currentUser.getSession().setTimeout(-1000l);
        //5、设置用户信息到session中
        UserBean user= findOne(Condition.and(
                new StringCondition("username",username, StringCondition.Handler.EQUAL)
        ));
        //5.1、设置当前登录用户的时区
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
        UserBean sysUserDTO= findOne(userId);
        //2、获取私钥
        PrivateKey privateKey = RSASecurity.restorePrivateKey(RSASecurity.keyMap.get(RSASecurity.PRIVATE_KEY));
        //3、解密密码
        String oldPassword= RSASecurity.RSADecode(privateKey, Base64.decodeBase64(encryptOldPassword));
        String newPassword= RSASecurity.RSADecode(privateKey, Base64.decodeBase64(encryptNewPassword));
        //4、将原始密码MD5加密后与数据库中进行对比
        if(sysUserDTO.getPassword().equals(encryptPassword(sysUserDTO.getUsername(), oldPassword))) {
            //5、使用MD5加密、盐值使用用户名
            sysUserDTO.setPassword(encryptPassword(sysUserDTO.getUsername(), newPassword));
            save(sysUserDTO);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 加密密码
     * @param username
     * @param password
     * @return
     */
    public String encryptPassword(String username,String password){
        return new Md5Hash(password,username).toBase64();
    }

}
