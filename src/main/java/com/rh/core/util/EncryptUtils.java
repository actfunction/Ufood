/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 关于加解密的工具类 需要额外包：jce.zip
 * @author wanglong
 */
public class EncryptUtils {
    /** MD5加密，不可还原 */
    public static final String MD5 = "MD5";
    /** SHA加密，不可还原 */
    public static final String SHA = "SHA";
    /** DES加密，可以还原 */
    public static final String DES = "DES";

    /**
     * DES加密密钥，解密时也要用到 该变量不可以改变，否则将无法对密码进行解密
     */
    private static final String PASSWORD_CRYPT_KEY = "theta networks";

    /** log */
    private static Log log = LogFactory.getLog(EncryptUtils.class);

    /**
     * 按照加密类型加密字串
     * @param orgStr 要加密的字串
     * @param encType 加密类型
     * @return 加密后的字串，如果没有匹配的加密方式，则返回原字串
     */
    public static String encrypt(String orgStr, String encType) {
        try {
            if ((encType.compareToIgnoreCase("SHA") == 0)) {
                MessageDigest md = MessageDigest.getInstance(encType);
                byte[] digest = md.digest(orgStr.getBytes());
                return new String(Lang.encodeBase64(digest));
            } else if (encType.compareToIgnoreCase("MD5") == 0) {
                MessageDigest alga = MessageDigest.getInstance("MD5");
                alga.update(orgStr.getBytes());
                byte[] digesta = alga.digest();
                return Lang.byteTohex(digesta);
            } else if (encType.compareToIgnoreCase("DES") == 0) {
                return desEncrypt(orgStr);
            }
            return orgStr;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return orgStr;
        }
    }

    /**
     * 按照解密密类型解密字串
     * @param encryptedStr 要解密的字串
     * @param encType 解密类型
     * @return 解密后的字串，如果不是DES方式，则返回原字串
     */
    public static String decrypt(String encryptedStr, String encType) {
        if ("DES".equals(encType)) {
            try {
                return desDecrypt(encryptedStr);
            } catch (Exception ex) {
                return encryptedStr;
            }
        }
        return encryptedStr;
    }

    /**
     * 加密
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回加密后的数据
     * @throws Exception 例外
     */
    public static byte[] desEncrypt(byte[] src, byte[] key) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密匙工厂，然后用它把DESKeySpec转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
        // 现在，获取数据并加密
        // 正式执行加密操作
        return cipher.doFinal(src);
    }

    /**
     * 解密
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回解密后的原始数据
     * @throws Exception 例外
     */
    public static byte[] desDecrypt(byte[] src, byte[] key) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建一个DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        // 现在，获取数据并解密
        // 正式执行解密操作
        return cipher.doFinal(src);
    }

    /**
     * 数据解密
     * @param data 待解密的数据
     * @return  解密后的数据
     */
    public static String desDecrypt(String data) {
        try {
            return new String(desDecrypt(Lang.hexTobyte(data.getBytes()), PASSWORD_CRYPT_KEY.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 数据加密
     * @param data 待加密的数据
     * @return 加密后的数据
     */
    public static String desEncrypt(String data) {
        try {
            return Lang.byteTohex(desEncrypt(data.getBytes(), PASSWORD_CRYPT_KEY.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
