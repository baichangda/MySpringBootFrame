package com.bcd.base.support_satoken;

public interface UserServiceInterface {
    /**
     * 根据登陆的id获取用户对象
     * @param loginId 登陆的id、即{@link cn.dev33.satoken.stp.StpUtil#login(Object)}中参数
     * @return
     */
    Object getUserByLoginId(String loginId);
}
