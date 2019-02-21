package com.rh.sso.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


import org.springframework.util.StringUtils;

/**
 * PKI相关工具方法
 * 
 * @author kfzx-xuyj01
 *
 */
public class PKIUtils {

	/**
	 * 加载KeyStore
	 * 
	 * @param type            KeyStore类型，可选值：jks/PKCS12，null代表jks
	 * @param istreamKeyStore KeyStore字节流
	 * @param password        KeyStore访问口令
	 * @return KeyStore实例
	 * @throws Exception
	 */
	public static KeyStore loadKeyStore(String type, InputStream istreamKeyStore, String password) throws Exception {
		KeyStore ks = KeyStore.getInstance(StringUtils.isEmpty(type) ? KeyStore.getDefaultType() : type);
		ks.load(istreamKeyStore, password.toCharArray());
		return ks;
	}

	/**
	 * 获取KeyStore中的私钥
	 * 
	 * @param ks
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(KeyStore ks, String alias, String password) throws Exception {
		PrivateKey priKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
		return priKey;
	}

	/**
	 * 获取KeyStore中的数字证书中的公钥
	 * 
	 * @param ks
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(KeyStore ks, String alias) throws Exception {
		X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
		PublicKey pubKey = cert.getPublicKey();
		return pubKey;
	}

	/**
	 * 获取KeyStore中私钥对应的数字签名对象，用于私钥签名
	 * 
	 * @param ks
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static Signature getSignature(KeyStore ks, String alias, String password) throws Exception {
		X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
		PrivateKey priKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
		Signature signature = Signature.getInstance(cert.getSigAlgName());
		signature.initSign(priKey);
		return signature;
	}

	/**
	 * 加载数字证书
	 * 
	 * @param istreamCert
	 * @return
	 * @throws Exception
	 */
	public static X509Certificate loadCertificate(InputStream istreamCert) throws Exception {
		CertificateFactory fac = CertificateFactory.getInstance("X.509");
		Certificate cert = fac.generateCertificate(istreamCert);
		return (X509Certificate) cert;
	}

	/**
	 * 获取数字证书中的公钥
	 * 
	 * @param istreamCert
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(Certificate cert) throws Exception {
		PublicKey pubKey = cert.getPublicKey();
		return pubKey;
	}

	/**
	 * 获取数字证书中的签名对象，用于使用证书中的公钥验签
	 * 
	 * @param cert
	 * @return
	 * @throws Exception
	 */
	public static Signature getSignature(X509Certificate cert) throws Exception {
		Signature signature = Signature.getInstance(cert.getSigAlgName());
		signature.initVerify(cert);
		return signature;
	}

	/**
	 * 基于签发者证书和被签发证书验证证书
	 * 
	 * @param signerCert 签发者证书
	 * @param signedCert 被签发的证书
	 * @return true/false
	 * @throws Exception
	 */
	public static boolean verifyCert(Certificate signerCert, Certificate signedCert) throws Exception {
		// 获取签发者的公钥
		PublicKey pubKeyCA = getPublicKey(signerCert);
		try {
			// 验证证书
			signedCert.verify(pubKeyCA);
			return true;
		} catch (SignatureException e) {
			return false;
		}
	}

	/**
	 * 对数据进行签名，通常是使用私钥签名
	 * 
	 * @param signature 私钥对应的签名对象
	 * @param data      需要签名的数据
	 * @return
	 * @throws Exception
	 */
	public static byte[] sign(Signature signature, byte[] data) throws Exception {
		signature.update(data);
		return signature.sign();
	}

	/**
	 * 对数据签名进行验签，通常是使用公钥验签
	 * 
	 * @param signature 公钥对应的签名对象
	 * @param data      需要签名的数据
	 * @param sigdata   私钥签名的数据
	 * @return true/false
	 * @throws Exception
	 */
	public static boolean verify(Signature signature, byte[] data, byte[] sigdata) throws Exception {
		signature.update(data);
		boolean ok = signature.verify(sigdata);
		return ok;
	}

	public static void main(String[] args) {
		/*// KeyStore type
		String keyStoreType = "PKCS12";

		// CA: KeyStore
		// String ksCAPath = "D:\\drap-sjs\\cert\\certs\\ca.p12";
		// CA: Cert
		String certCAPath = "D:\\drap-sjs\\cert\\certs\\ca.cer";

		// sso1.bjsasc.com: KeyStore
		// String ksSSO1Path = "D:\\drap-sjs\\cert\\sso1.bjsasc.com\\sso1.bjsasc.com.p12";
		// sso1.bjsasc.com: Cert
		String certSSO1Path = "D:\\drap-sjs\\cert\\sso1.bjsasc.com\\sso1.bjsasc.com.cer";

		// sdsso.bjsasc.com: KeyStore
		String ksSDSSOPath = "D:\\drap-sjs\\cert\\sdsso.bjsasc.com\\sdsso.bjsasc.com.p12";
		// sdsso.bjsasc.com: Cert
		String certSDSSOPath = "D:\\drap-sjs\\cert\\sdsso.bjsasc.com\\sdsso.bjsasc.com.cer";

		try {
			// 场景：验证证书是由指定的CA签发的
			try (InputStream istreamCertCA = new FileInputStream(certCAPath);
					InputStream istreamCert = new FileInputStream(certSSO1Path)) {
				// 加载签发者证书
				Certificate certCA = loadCertificate(istreamCertCA);
				// 获取需要验证的证书
				Certificate cert = loadCertificate(istreamCert);
				// 验证：应该为true
				boolean ok = verifyCert(certCA, cert);
				System.out.println("Verify: " + certCAPath + " signed " + certSSO1Path + ": " + ok);
			}

			// 场景：验证证书不是由指定的证书签发的
			try (InputStream istreamCertCA = new FileInputStream(certSDSSOPath);
					InputStream istreamCert = new FileInputStream(certSSO1Path)) {
				// 加载签发者证书
				Certificate certCA = loadCertificate(istreamCertCA);
				// 获取需要验证的证书
				Certificate cert = loadCertificate(istreamCert);
				// 验证：应该为false
				boolean ok = verifyCert(certCA, cert);
				System.out.println("Verify: " + certSDSSOPath + " signed " + certSSO1Path + ": " + ok);
			}

			// 场景：验证SDSSO签名的数据
			byte[] data = "Hello world!".getBytes();
			try (InputStream istreamKS = new FileInputStream(ksSDSSOPath);
					InputStream istreamCert = new FileInputStream(certSDSSOPath)) {
				// 加载密钥库
				KeyStore ks = loadKeyStore(keyStoreType, istreamKS, "123456");
				// 使用SDSSO私钥签名
				Signature sigPrivate = getSignature(ks, "sdsso.bjsasc.com", "123456");
				byte[] sign = sign(sigPrivate, data);

				// 使用SDSSO的证书验签
				X509Certificate cert = loadCertificate(istreamCert);
				Signature sigPublic = getSignature(cert);
				boolean ok = verify(sigPublic, data, sign);
				System.out.println(
						"Sign data by " + ksSDSSOPath + ", Verify sign by " + certSDSSOPath + ", verify result: " + ok);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
	}

}
