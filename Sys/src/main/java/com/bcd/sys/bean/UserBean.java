package com.bcd.sys.bean;


import com.alibaba.fastjson.annotation.JSONField;
import com.bcd.rdb.annotation.ReferCollection;
import com.bcd.rdb.bean.BaseBean;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户基础信息
 * 
 * @author Aaric
 * @since 2017-04-26
 */
@Entity
@Table(name = "t_sys_user")
public class UserBean extends BaseBean {
	private String username;  //用户名
	private String email;  //邮箱
	private String phone;  //手机号
	private String realName;  //真实姓名
	private String sex;  //性别
	private Date birthday;  //生日
	private String cardNumber;  //身份证号
	private Integer status;    //状态 0：禁用；1：启用
	private Long orgId;

	@JSONField(serialize = false ,deserialize = false)
	private String password;  //密码凭证

	//用户关联角色
	@ReferCollection
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "t_sys_user_role",
			joinColumns=@JoinColumn(name = "user_id",referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id")
	)
	private Set<RoleBean> roleBeanSet = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "orgId",insertable = false,updatable = false)
	private OrgBean org;


	//临时字段、供登陆时使用
	@Transient
	@JSONField(serialize = false ,deserialize = false)
	private String timeZone;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<RoleBean> getRoleBeanSet() {
		return roleBeanSet;
	}

	public void setRoleBeanSet(Set<RoleBean> roleBeanSet) {
		this.roleBeanSet = roleBeanSet;
	}

	public OrgBean getOrg() {
		return org;
	}

	public void setOrg(OrgBean org) {
		this.org = org;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
}
