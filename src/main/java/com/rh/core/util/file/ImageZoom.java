package com.rh.core.util.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 按照比例缩小jpg图片，目前只支持jpg图片。
 * @author wanglong
 * 
 */
public class ImageZoom {
    private int width = 100;
    private int height = 100;
    private float quality = 0.8f;

    /**
     * 按照比例缩小图片
     * @param width 缩小后的宽度
     * @param height 缩小后的图片高度
     */
    public ImageZoom(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }

    /**
     * 
     * @param origFile 原始图片路径
     * @param targetFile 缩小后的图片路径
     * @throws IOException IO错误
     */
    public void resize(File origFile, File targetFile) throws IOException {
        BufferedImage images = null;
        BufferedImage zoomOutImg = null;
        
        // 缩小图片
        try {
            images = ImageIO.read(origFile);
            float zoomRatio = getZoomRatio(images);
            if (zoomRatio >= 1) {
                zoomOutImg = images;
            } else {
                zoomOutImg = resize(images, zoomRatio);
            }
            images.flush();
            images = null;
        } finally {
            if (images != null) {
                images.flush();
                images = null;
            }
        }
        
        try {
            this.writeHighQuality(zoomOutImg, new FileOutputStream(targetFile));
        } finally {
            if (zoomOutImg != null) {
                zoomOutImg.flush();
                zoomOutImg = null;
            }
        }
    }

    /**
     * 
     * @param is 原始图片
     * @param out 目标图片输出
     * @throws IOException IO错误
     */
    public void resize(InputStream is, OutputStream out) throws IOException {
        BufferedImage images = null;
        BufferedImage zoomOutImg = null;
        File tempFile = null;
        // 缩小图片
        try {
            // 先把流保存到临时文件中
            tempFile = FileHelper.getTempFile(null);
            FileUtils.copyInputStreamToFile(is, tempFile);
            
            images = ImageIO.read(tempFile);
            float zoomRatio = getZoomRatio(images);
            
            if (zoomRatio >= 1) {
                FileUtils.copyFile(tempFile, out);
            } else {
                zoomOutImg = resize(images, zoomRatio);
                this.writeHighQuality(zoomOutImg, out);
            }
        } finally {
            FileUtils.deleteQuietly(tempFile); // 删除临时文件
            flushBufferedImage(images);
            flushBufferedImage(zoomOutImg);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }
    }
    
    /**
     * 关闭图像
     * @param img BufferedImage 对象
     */
    private void flushBufferedImage(BufferedImage img) {
        if (img == null) {
            return;
        }
        img.flush();
        img = null;
    }

    /**
     * @param im 原始图像
     * @param resizeTimes 倍数,比如0.5就是缩小一半,0.98等等double类型
     * @return 返回处理后的图像
     */
    private BufferedImage resize(BufferedImage im, float resizeTimes) {
        /* 原始图像的宽度和高度 */
        int imgWidth = im.getWidth();
        int imgHeight = im.getHeight();

        /* 调整后的图片的宽度和高度 */
        int toWidth = (int) (Float.parseFloat(String.valueOf(imgWidth)) * resizeTimes);
        int toHeight = (int) (Float.parseFloat(String.valueOf(imgHeight)) * resizeTimes);

        /* 新生成结果图片 */
        BufferedImage result = new BufferedImage(toWidth, toHeight,
                BufferedImage.TYPE_INT_RGB);

        result.getGraphics().drawImage(
                im.getScaledInstance(toWidth, toHeight,
                        java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        return result;
    }

    /**
     * 输出高质量的图片JPG普通，图片质量为默认为1。
     * @param im 图片对象
     * @param output 图片输出
     * @throws IOException io错误
     */

    private void writeHighQuality(BufferedImage im, OutputStream output) throws IOException {
        try {
            /* 输出到文件流 */
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
			JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(im);
            /* 压缩质量 */
            jep.setQuality(this.quality, true);
            encoder.encode(im, jep);

            IOUtils.closeQuietly(output);

        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * 计算图片的缩放比例，根据图片的宽高。
     * 
     * @param images 原图片
     * @return 计算图片的缩放比例
     */
    private float getZoomRatio(BufferedImage images) {
        float ratio = 1f;
        // 原图大小
        int srcWidth = images.getWidth();
        int srcHeight = images.getHeight();
        // 取得缩放比例
        double sx = (double) srcWidth / width;
        double sy = (double) srcHeight / height;

        if (sx > sy) {
            ratio = (float) (1 / sx);
        } else {
            ratio = (float) (1 / sy);
        }

        return ratio;
    }

    /**
     * 
     * @return JPG 图片的质量
     */
    public float getQuality() {
        return quality;
    }

    /**
     * 
     * @param aQuality JPG图片的质量;值为0.0至1.0
     */
    public void setQuality(float aQuality) {
        this.quality = aQuality;
    }
}
