package com.rh.core.util.encrypt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.rh.core.util.Lang;

/**
 * 密钥生成器
 * 
 * @author wanglong
 */
public class KeyGenerator {

    private byte[] priKey;

    private byte[] pubKey;

    /**
     * 构建体方法
     */
    public KeyGenerator() {
        generate("平台是null，null是平台");
    }

    /**
     * 生成公钥和私钥
     * @param seed 种子字符
     */
    public void generate(String seed) {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            SecureRandom secrand = new SecureRandom();
            secrand.setSeed(seed.getBytes()); // 初始化随机产生器
            keygen.initialize(1024, secrand);
            KeyPair keys = keygen.genKeyPair();
            PublicKey pubkey = keys.getPublic();
            PrivateKey prikey = keys.getPrivate();
            pubKey = Lang.encodeBase64(pubkey.getEncoded());
            priKey = Lang.encodeBase64(prikey.getEncoded());
        } catch (java.lang.Exception e) {
            System.out.println("生成密钥对失败");
            e.printStackTrace();
        }
    }

    /**
     * 获取私钥
     * @return 私钥
     */
    public byte[] getPriKey() {
        return priKey;

    }

    /**
     * 获取公钥
     * @return 公钥
     */
    public byte[] getPubKey() {
        return pubKey;
    }

    /**
     * 通过私钥对文本信息加密签名
     * @param priKeyText 私钥
     * @param plainText 文本信息
     * @return 加密签名后的记过
     */
    public static byte[] sign(byte[] priKeyText, String plainText) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Lang.decodeBase64(priKeyText));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey prikey = keyf.generatePrivate(priPKCS8); // 取私钥对象
            // 用私钥对信息生成数字签名
            Signature signet = java.security.Signature.getInstance("MD5withRSA");
            signet.initSign(prikey);
            signet.update(plainText.getBytes());
            byte[] signed = Lang.encodeBase64(signet.sign());
            return signed;
        } catch (java.lang.Exception e) {
            System.out.println("签名失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证加密后的文本信息
     * @param pubKeyText 公钥
     * @param plainText  文本信息
     * @param signText 签名信息
     * @return 是否一致
     */
    public static boolean verify(byte[] pubKeyText, String plainText, byte[] signText) {
        try {
            // 解密由base64编码的公钥,并构造X509EncodedKeySpec对象
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(Lang.decodeBase64(pubKeyText));
            // RSA对称加密算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // 取公钥匙对象
            PublicKey pubKey = keyFactory.generatePublic(bobPubKeySpec);
            // 解密由base64编码的数字签名
            byte[] signed = Lang.decodeBase64(signText);
            Signature signatureChecker = Signature.getInstance("MD5withRSA");
            signatureChecker.initVerify(pubKey);
            signatureChecker.update(plainText.getBytes());
            // 验证签名是否正常
            return signatureChecker.verify(signed);
        } catch (Throwable e) {
            System.out.println("校验签名失败");
            e.printStackTrace();
            return false;
        }
    }

}
