package com.rh.sso.util;

import java.net.URISyntaxException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rh.core.base.Context;
/*
 * @auther kfzx-xuyj01
 */
public class SSOserver {
	/**
	 * 统一认证地址
	 */
	private String SSO_URL = Context.getSyConf("SSO_URL", "http://122.21.189.89:9006/aa/");
	/**
	 * 下载证书地址,todo 配置项
	 */
	private String DOWNLOAD_CERT_URL =Context.getSyConf("DOWNLOAD_CERT_URL", "cert/mine");

	/**
	 * 转换跨级token为本级token接口地址，todo 配置项
	 */
	private String TRANSFORM_CROSS_TOKEN_URL = Context.getSyConf("TRANSFORM_CROSS_TOKEN_URL", "auth/transformCrossToken");

	/**
	 * 发行方，用于标识署级或省级，todo 配置项
	 */
	private String ISSUER = Context.getSyConf("ISSUER","www.sjs.com");

	private RestTemplate restTemplate = new RestTemplate();

	// 本级证书验证对象
	private ThreadLocal<SignerVerifier> tlverifier = new ThreadLocal<SignerVerifier>();
	// 本级证书
	private byte[] certBytes;
	/**
	 * 解析和验证
	 *
	 * @param token
	 * @return
	 */
	public SSOUser verify(String token) {
		try {
			Jwt dt = JwtHelper.decode(token);

			ObjectMapper mapper = new ObjectMapper();
			String a = dt.getClaims();
			SSOUser idToken = mapper.readValue(a.replaceAll("null", "true"), SSOUser.class);
			Long newDate = System.currentTimeMillis();
			Long exp = idToken.getExp();
			// 判断JWT有效期
			if (exp != null && newDate > exp) {
				throw new RuntimeException(String.format("TokenExpiredException:The Token has expired on %s.", exp));
			}

			// 判断jwt由本级SSO Server签发，直接下载证书验签
			if (this.ISSUER.equals(idToken.getIss())) {
				dt.verifySignature(getVerifySigner());
			} else {// 非本级JWT，调用SSO Server转换接口
				token = transformCrossToken(token);
				dt = JwtHelper.decode(token);
				idToken = mapper.readValue(dt.getClaims(), SSOUser.class);
			}
			return idToken;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取验签对象
	 *
	 * @return
	 * @throws URISyntaxException 
	 */
	private synchronized SignerVerifier getVerifySigner() throws URISyntaxException {
		SignerVerifier verifier = tlverifier.get();
		if (null != verifier) {
			return verifier;
		} else {
			if (this.certBytes == null) {
				this.certBytes = downloadCert(ISSUER);
			}
			verifier = new SSOVerifySigner(this.certBytes);
			tlverifier.set(verifier);
			return verifier;
		}
	}

	/**
	 * 下载本级证书
	 *
	 * @param issuer
	 * @return
	 * @throws URISyntaxException 
	 */
	private byte[] downloadCert(String issuer) throws URISyntaxException {
		ResponseEntity<byte[]> response 
		= restTemplate.getForEntity(SSO_URL+DOWNLOAD_CERT_URL, byte[].class);
		return response.getBody();
	}
	/**
	 * 转换非本级JWT，实现跨级访问
	 *
	 * @param token
	 * @return
	 */
	private String transformCrossToken(String token) {
		String url = SSO_URL+TRANSFORM_CROSS_TOKEN_URL + "?sso_token=" + token;
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		return response.getBody();
	}

}
