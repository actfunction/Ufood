package com.rh.core.util.encrypt;

/**
 * DES加密常用方法
 * 
 * @author wanglong
 */
public class DesUtils {
    /** 密钥 */
    private static final String KEY = "ruaho&Des7*kob8";
    /** 偏移 */
    private static final String IV = "ruaho%NU5";
    
    /**
     * 加密指定的字符串，并编码成16进制格式
     * 
     * @param str 待加密的字符串
     * @return 加密后的字符串
     */
    public static String encrypt(String str) {
        try {
            DesPKCS7 des = new DesPKCS7(KEY, IV);
            return des.encrypt(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 解密被编码成16进制的字符串
     * 
     * @param str 被解密字符串
     * @return 解密后的字符串
     */
    public static String decrypt(String str) {
        try {
            DesPKCS7 des = new DesPKCS7(KEY, IV);
            return des.decrypt(str);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

