package com.rh.core.plug;

import java.io.OutputStream;

/**
 * 条形码输出接口类
 * @author wanglong
 *
 */
public interface IBarcode {
    
    /**
     * 生成value对应的二维码，写入out中
     * @param out 输出流
     * @param value 值
     * @param w 宽度
     * @param h 高度
     * @throws Exception 例外
     */
    void genQRCode(OutputStream out, String value, int w, int h) throws Exception;
    
    /**
     * 生成二维码图片文件
     * @param fileName 生成的图片文件名
     * @param width 宽度
     * @param height 高度
     * @param value 需要生成二维码的串
     * @throws Exception 例外
     */
    void genQRCodeFile(String fileName, String value, int width, int height) throws Exception;
    
    /**
     * 生成value对应的PDF417二维码，写入out中
     * @param out 输出流
     * @param value 值
     * @throws Exception 例外
     */
    void genPDF417(OutputStream out, String value) throws Exception;
}
