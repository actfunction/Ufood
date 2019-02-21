package com.rh.sso.util;


import java.io.Serializable;
import java.util.List;

/*
 * @auther kfzx-xuyj01
 */
public class SSOUser implements Serializable {

	private static final long serialVersionUID = 6134777180234951959L;

	private String userLoginName;

	private String userName;

	private String email;

	private String userId;

	private String clientIp;

	private String domainId;

	private String iss;

	private Long iat;

	private Long exp;
	
	private String crossToken;
	// 单点登录身份令牌
	private String token;
	
	private String apps;
	
	private String type;
	
	private String typeName;
	
	private List<String> roles;

	public List<String> getRoles() {
		return roles;
	}
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public Long getExp() {
		return exp;
	}

	public void setExp(Long exp) {
		this.exp = exp;
	}

	public Long getIat() {
		return iat;
	}

	public void setIat(Long iat) {
		this.iat = iat;
	}

	public String getIss() {
		return iss;
	}

	public void setIss(String iss) {
		this.iss = iss;
	}

	public String getUserLoginName() {
		return userLoginName;
	}

	public void setUserLoginName(String userLoginName) {
		this.userLoginName = userLoginName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getType() {
		return type;
	}

	public void setApps(String apps) {
		this.apps = apps;
	}
	public String getApps() {
		return apps;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getCrossToken() {
		return crossToken;
	}

	public void setCrossToken(String crossToken) {
		this.crossToken = crossToken;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}
