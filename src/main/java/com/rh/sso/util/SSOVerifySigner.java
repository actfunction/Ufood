package com.rh.sso.util;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Signature;
import java.security.cert.X509Certificate;


import org.springframework.security.jwt.crypto.sign.InvalidSignatureException;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
/*
 * @auther kfzx-xuyj01
 */
public class SSOVerifySigner implements SignerVerifier {


	/**
	 * 签名对象
	 */
	private Signature sigPublic;

	public SSOVerifySigner(byte[] bytes) {
		// 根据证书内容生成签名对象
		try {
			InputStream istreamCert = new ByteArrayInputStream(bytes);
			X509Certificate cert = PKIUtils.loadCertificate(istreamCert);
			sigPublic = PKIUtils.getSignature(cert);
		} catch (Exception e) {
			//logger.error("VerifySigner init failed.", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] sign(byte[] bytes) {
		throw new RuntimeException("This method doesnot work!");
	}

	@Override
	public String algorithm() {
		// return sigPublic.getAlgorithm();
		// Invalid or unsupported signature algorithm: SHA1withRSA
		return "SHA256withRSA";
	}

	@Override
	public void verify(byte[] content, byte[] signature) {
		try {
			sigPublic.update(content);
			boolean bool = sigPublic.verify(signature);
			if (!bool) {
				throw new InvalidSignatureException("Calculated signature did not match actual value");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
