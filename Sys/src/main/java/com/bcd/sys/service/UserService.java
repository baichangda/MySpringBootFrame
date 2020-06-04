package com.bcd.sys.service;

import com.bcd.base.condition.impl.NumberCondition;
import com.bcd.base.condition.impl.StringCondition;
import com.bcd.base.config.init.SpringInitializable;
import com.bcd.base.config.shiro.realm.MyAuthorizingRealm;
import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.security.RSASecurity;
import com.bcd.base.util.DateZoneUtil;
import com.bcd.rdb.jdbc.sql.SqlUtil;
import com.bcd.rdb.service.BaseService;
import com.bcd.sys.bean.UserBean;
import com.bcd.sys.define.CommonConst;
import com.bcd.sys.define.MessageDefine;
import com.bcd.sys.keys.KeysConst;
import com.bcd.sys.shiro.PhoneCodeRealm;
import com.bcd.sys.shiro.PhoneCodeToken;
import com.bcd.sys.shiro.ShiroUtil;
import com.bcd.sys.shiro.UsernamePasswordRealm;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/4/18.
 */
@Service
public class UserService extends BaseService<UserBean,Long>  implements SpringInitializable {

    private final static Logger logger= LoggerFactory.getLogger(UserService.class);

    @Autowired
    @Qualifier("string_string_redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private UsernamePasswordRealm myShiroRealm;

    @Override
    public void init(ContextRefreshedEvent event) {
        UserBean userBean= findById(CommonConst.ADMIN_ID);
        if(userBean==null){
            userBean=new UserBean();
            userBean.setId(CommonConst.ADMIN_ID);
            userBean.setUsername(CommonConst.ADMIN_USERNAME);
            String password;
            if(CommonConst.IS_PASSWORD_ENCODED){
                password=encryptPassword(CommonConst.ADMIN_USERNAME, CommonConst.INITIAL_PASSWORD);
            }else{
                password= CommonConst.INITIAL_PASSWORD;
            }
            userBean.setPassword(password);
            userBean.setStatus(1);
            save(userBean);
        }
    }

    /**
     * 手机号、验证码登陆
     * @param phone
     * @param phoneCode
     * @return
     */
    public UserBean login(String phone,String phoneCode){
        PhoneCodeToken token=new PhoneCodeToken(phone,phoneCode);
        return login(token, DateZoneUtil.ZONE_OFFSET.getId(),()->{
            return findOne(new StringCondition("phone",phone));
        });
    }

    /**
     * 发送随机验证码
     * @param phone
     */
    public void sendPhoneCode(String phone){
        String key="phoneCode:"+phone;
        long expireTimeInSeconds=redisTemplate.getExpire(key);
        if(expireTimeInSeconds>0){
            throw BaseRuntimeException.getException("等待"+expireTimeInSeconds+"秒后重试");
        }else{
            if(expireTimeInSeconds==-1){
                //如果没有过期时间,则删除异常key
                redisTemplate.delete(key);
            }else{
                //如果不存在,则构造key发送短信
                String phoneCode= RandomStringUtils.randomNumeric(6);
                boolean res=redisTemplate.opsForValue().setIfAbsent("phoneCode:"+phone,phoneCode,3*60, TimeUnit.SECONDS);
                if(res){
                    //todo 发送短信

                }else{
                    //如果有其他服务器抢先发送了验证码,则再次获取时间
                    expireTimeInSeconds=redisTemplate.getExpire(key);
                    throw BaseRuntimeException.getException("等待"+expireTimeInSeconds+"秒后重试");
                }
            }
        }
    }


    /**
     * 用户名、密码登陆
     * @param username
     * @param encryptPassword 使用公钥加密后的密码
     * @param offsetId
     * @return
     */
    public UserBean login(String username,String encryptPassword,String offsetId){
        UsernamePasswordToken token;
        //根据是否加密处理选择不同处理方式
        if(CommonConst.IS_PASSWORD_ENCODED){
            //使用私钥解密密码
            PrivateKey privateKey = KeysConst.PRIVATE_KEY;
            String password= RSASecurity.decode(privateKey, Base64.decodeBase64(encryptPassword));
            //构造登录对象
            token = new UsernamePasswordToken(username, password);
        }else{
            token = new UsernamePasswordToken(username, encryptPassword);
        }
        return login(token,offsetId,()->{
            return findOne(new StringCondition("username",username));
        });
    }

    private UserBean login(AuthenticationToken token,String offsetId, Supplier<UserBean> supplier){
        //获取当前subject
        Subject subject = SecurityUtils.getSubject();
        //进行登录操作
        subject.login(token);
        //设置用户信息到session中
        UserBean user=supplier.get();
        user.setOffsetId(offsetId);
        subject.getSession().setAttribute("user",user);
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

    /**
     * 踢出用户名用户、手机号用户
     * 根据传参数不同踢出对应登陆的用户
     * @param username
     * @param phone
     * @param kickMessage
     */
    public void kickUser(String username,String phone,String kickMessage){
        if(username==null&&phone==null){
            return;
        }
        Serializable curSessionId=SecurityUtils.getSubject().getSession().getId();
        UserBean curUser= ShiroUtil.getCurrentUser();
        String curUserName=curUser.getUsername();
        String curPhone=curUser.getPhone();
        DefaultWebSecurityManager securityManager= (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        DefaultWebSessionManager sessionManager=(DefaultWebSessionManager)securityManager.getSessionManager();
        SessionDAO sessionDAO= sessionManager.getSessionDAO();
        Collection<Session> sessionCollection= sessionDAO.getActiveSessions();
        Collection<Realm> realms= securityManager.getRealms();
        //清除session
        Set<String> kickSessionSet=new HashSet<>();
        Set<PrincipalCollection> principalCollectionSet=new HashSet<>();
        sessionCollection.forEach(e->{
            //忽略踢出自己
            if(!e.getId().equals(curSessionId)){
                UserBean userBean = (UserBean) e.getAttribute("user");
                boolean isDel=false;

                if(username!=null){
                    if (username.equals(userBean.getUsername())) {
                        isDel=true;
                    }
                }
                if(!isDel&&phone!=null){
                    if (phone.equals(userBean.getPhone())) {
                        isDel=true;
                    }
                }
                if(isDel){
                    //清除session
                    sessionDAO.delete(e);
                    kickSessionSet.add(e.getId().toString());
                    //判断当前session用户名是否是当前登陆用户名,是则添加进入set,用于清除权限缓存
                    PrincipalCollection principalCollection= (PrincipalCollection)e.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                    if(!principalCollection.getPrimaryPrincipal().equals(curUserName)&&
                            !principalCollection.getPrimaryPrincipal().equals(curPhone)){
                        principalCollectionSet.add(principalCollection);
                    }
                }
            }
        });
        //清除对应realm的缓存信息
        principalCollectionSet.forEach(principalCollection -> {
            realms.forEach(realm -> {
                if(principalCollection.getRealmNames().contains(realm.getName())){
                    ((MyAuthorizingRealm)realm).clearCachedAuthenticationInfo(principalCollection);
                    ((MyAuthorizingRealm)realm).clearCachedAuthorizationInfo(principalCollection);
                }
            });
        });

        //记录踢出用户的sessionId到redis中,便于其他用户如果检测到属于被踢出的,返回对应的错误信息
        if(!kickSessionSet.isEmpty()){
            for (String s : kickSessionSet) {
                redisTemplate.opsForValue().set(CommonConst.KICK_SESSION_ID_PRE+s,kickMessage
                        ,CommonConst.KICK_SESSION_EXPIRE_IN_SECOND,TimeUnit.SECONDS);
            }
        }
    }
}
