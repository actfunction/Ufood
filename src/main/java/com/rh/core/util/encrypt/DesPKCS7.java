package com.rh.core.util.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DES 加解密算法
 * 
 * @author wanglong
 */
public class DesPKCS7 {
    
    private Cipher ecipher;
    
    private Cipher dcipher;
    
    /** log */
    private static Log log = LogFactory.getLog(DesPKCS7.class);
    
    /**
     * @param key 密码
     * @param value 偏移字节
     * @throws Exception Exception
     */
    public DesPKCS7(String key , String value) throws Exception {
        init(key.getBytes(), value.getBytes());
    }
    
    /**
     * @param keyBytes 密码
     * @param ivBytes 偏移字节
     * @throws Exception Exception
     */
    public DesPKCS7(byte[] keyBytes , byte[] ivBytes) throws Exception {
        init(keyBytes, ivBytes);
    }
    /**
     * @param keySpec 密码
     * @param ivSpec 偏移
     * @throws Exception Exception
     */
    public DesPKCS7(DESKeySpec keySpec , IvParameterSpec ivSpec) throws Exception {
        init(keySpec, ivSpec);
    }
    /**
     * 初始化环境
     * @param keyBytes  密钥
     * @param ivBytes   偏移
     * @throws Exception Exception
     */
    private void init(byte[] keyBytes , byte[] ivBytes) throws Exception {
        DESKeySpec dks = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(dks);
        byte[] ivTemp = new byte[8];
        if (ivBytes.length > 8) {
            System.arraycopy(ivBytes, 0, ivTemp, 0, 8);
        } else if (ivBytes.length == 8) {
            ivTemp = ivBytes;
        } else {
            throw new Exception("ivSpec 的长度不能小于  8");
        }
        IvParameterSpec iv = new IvParameterSpec(ivTemp);
        init(key, iv);
        
    }
    /**
     * 初始化环境
     * @param keySpec  密钥
     * @param iv   偏移
     * @throws Exception Exception
     */
    private void init(DESKeySpec keySpec , IvParameterSpec iv) throws Exception {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(keySpec);
        init(key, iv);
    }
    /**
     * 初始化环境
     * @param key  密钥
     * @param iv   偏移
     * @throws Exception Exception
     */
    private void init(SecretKey key , IvParameterSpec iv) throws Exception {
        AlgorithmParameterSpec paramSpec = (AlgorithmParameterSpec) iv;
        try {
            ecipher = Cipher.getInstance("DES/CBC/NoPadding");
            dcipher = Cipher.getInstance("DES/CBC/NoPadding");
            
            // CBC requires an initialization vector
            ecipher.init(Cipher.ENCRYPT_MODE, key, (SecureRandom) paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, (SecureRandom) paramSpec);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 加密输人流到输出流
     * 
     * @param in 输入数据流
     * @param out 输出数据流
     * @throws IOException IOException
     */
    public void encrypt(InputStream in , OutputStream out) throws IOException {
        try {
            out = new CipherOutputStream(out, ecipher);
            byte[] buf = new byte[this.ecipher.getBlockSize()];
            
            int numRead = 0;
            while (true) {
                numRead = in.read(buf);
                boolean bBreak = false;
                if (numRead == -1 || numRead < buf.length) {
                    int pos = numRead == -1 ? 0 : numRead;
                    byte byteFill = (byte) (buf.length - pos);
                    for (int i = pos; i < buf.length; ++i) {
                        buf[i] = byteFill;
                    }
                    bBreak = true;
                }
                out.write(buf);
                if (bBreak) {
                    break;
                }
            }
            out.close();
        } catch (java.io.IOException e) {
            out.close();
            throw e;
        }
    }
    
    /**
     * 加密指定字符串，并做base64编码
     * 
     * @param in 待加密字符串
     * @return  加密后字符串
     */
    public String encrypt(String in) {
        InputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            input = new ByteArrayInputStream(in.getBytes("utf-8"));
            output = new ByteArrayOutputStream();
            this.encrypt(input, output);
            
           // return new String(Hex.encodeHex(output.toByteArray()));
            return Base64.encodeBase64String(output.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    log.error(e);
                }
            }
            
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }
    
    /**
     * 解密数据流至输出流
     * 
     * @param in    输入流
     * @param out   输出流
     * @throws IOException IOException
     */
    public void decrypt(InputStream in , OutputStream out) throws IOException {
        try {
            // Bytes read from in will be decrypted
            in = new CipherInputStream(in, dcipher);
            byte[] buf = new byte[this.dcipher.getBlockSize()];
            
            // Read in the decrypted bytes and write the cleartext to out
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0) {
                if (in.available() > 0) {
                    out.write(buf, 0, numRead);
                } else {
                    byte byteBlock = buf[buf.length - 1];
                    int i = 0;
                    for (i = buf.length - byteBlock; i >= 0 && i < buf.length; ++i) {
                        if (buf[i] != byteBlock) {
                            break;
                        }
                    }
                    
                    if (i == buf.length) {
                        out.write(buf, 0, buf.length - byteBlock);
                    } else {
                        out.write(buf);
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw e;
        } finally {
            out.close();
        }
    }
    
    /**
     * 解密指定的base64编码的字符串
     * 
     * @param encryptText 待解密的串
     * @return  解密后的字符串
     */
    public String decrypt(String encryptText) {
        
        InputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            input = new ByteArrayInputStream(Base64.decodeBase64(encryptText));
            output = new ByteArrayOutputStream();
            decrypt(input, output);
            return output.toString("utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    log.error(e);
                }
            }
            
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        
    }
}

