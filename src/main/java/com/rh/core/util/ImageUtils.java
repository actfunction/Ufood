package com.rh.core.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOUtils;

/**
 * @author wanglong
 * 
 */
public class ImageUtils {

    /**
     * 图片剪切
     * @param is - inputstream ,图片处理后is将被关闭
     * @param out - outputstream, 图片处理后out将被关闭
     * @param imgType - 图片文件后缀 (必选)
     * @param x - 左上起始位置，X轴偏移位置
     * @param y - 左上起始位置，Y轴偏移位置
     * @param width - 图片宽度
     * @param height - 图片高度
     * @throws IOException - 截取错误
     */
    public static void cutting(InputStream is, OutputStream out, String imgType, int x, int y, int width, int height)
            throws IOException {
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imgType);
            // Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mtype);
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rect = new Rectangle(x, y, width, height);
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0, param);
            ImageIO.write(bi, imgType, out);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 将目标图片二值化
     * @param is - InputStream , 图片处理后将被关闭
     * @param out - OutputStream, 图片处理后将被关闭
     * @param imgType - 图片文件后缀 (必选)
     * @throws IOException - 图片处理错误将会抛出异常
     */
    public void binaryImage(InputStream is, OutputStream out, String imgType) throws IOException {
        if (null == imgType || 0 == imgType.length()) {
            imgType = "png";
        }
        try {
            BufferedImage image = ImageIO.read(is);
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_BYTE_BINARY);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int rgb = image.getRGB(i, j);
                    grayImage.setRGB(i, j, rgb);
                }
            }
            ImageIO.write(grayImage, imgType, out);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 将目标图片灰度化
     * @param is - InputStream , 图片处理后将被关闭
     * @param out - OutputStream, 图片处理后将被关闭
     * @param imgType - 图片文件后缀 (必选)
     * @throws IOException - 图片处理错误将会抛出异常
     */
    public void grayImage(InputStream is, OutputStream out, String imgType) throws IOException {
        if (null == imgType || 0 == imgType.length()) {
            imgType = "png";
        }
        try {
            BufferedImage image = ImageIO.read(is);
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_BYTE_GRAY);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int rgb = image.getRGB(i, j);
                    grayImage.setRGB(i, j, rgb);
                }
            }
            ImageIO.write(grayImage, imgType, out);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }

}
