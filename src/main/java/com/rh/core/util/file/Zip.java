package com.rh.core.util.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 将一组文件压缩成一个zip文件，或者将压缩后的数据输出到OutputStream中
 * @author wanglong
 */
public class Zip {

    private OutputStream out = null;

    /**
     * 用于缓冲数据流，与FileOutputStream配合使用提高写磁盘的效率。
     */
    private BufferedOutputStream bos = null;

    private ZipArchiveOutputStream zaos = null;

    /**
     * 新建一个Zip文件，并将压缩后的数据写到文件中
     * @param zipname Zip文件路径
     * @throws IOException IO错误
     */
    public Zip(String zipname) throws IOException {
        File f = new File(zipname);
        FileUtils.forceMkdir(f.getParentFile());
        out = new FileOutputStream(f);
        init();
    }

    /**
     * 新建一个ZIP文件，并将压缩后的数据写到文件中
     * @param zipname Zip文件的名称
     * @throws IOException IO错误
     */
    public Zip(File zipname) throws IOException {
        FileUtils.forceMkdir(zipname.getParentFile());
        out = new FileOutputStream(zipname);
        init();
    }

    /**
     * 新建一个Zip压缩对象，并将压缩后的文件流写到输出流
     * @param output 输出流对象。将压缩文件的结果输出到此对象中。
     * @throws IOException IO错误
     */
    public Zip(OutputStream output) throws IOException {
        out = output;
        init();
    }
    
    /**
     * 创建Zip输出对象
     * @throws IOException IO错误
     */
    private void init() throws IOException {
        bos = new BufferedOutputStream(out);
        zaos = new ZipArchiveOutputStream(bos);
        zaos.setEncoding("GBK");
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        IOUtils.closeQuietly(zaos);
        IOUtils.closeQuietly(bos);
        IOUtils.closeQuietly(out);
    }

    /**
     * 把一个文件或目录打包到zip文件中的某目录
     * @param file 文件或目录对象
     * @exception IOException IO错误
     */
    public void addFile(File file) throws IOException {
        addFile(file, null);
    }

    /**
     * 把一个目录或文件添加到zip文件中的指定目录
     * @param dirpath 目录绝对地址
     * @param pathName zip中目录
     * @exception IOException IO错误
     */
    public void addFile(String dirpath, String pathName) throws IOException {
        File file = new File(dirpath);
        addFile(file, pathName);
    }
    
    /**
     * 把一个文件或目录中的文件放到ZIP文件的指定目录中
     * @param file 被压缩的文件或目录
     * @param pathName 在zip文件中的目录名
     * @throws IOException IO错误
     */
    public void addFile(File file, String pathName) throws IOException {
        pathName = this.getPathName(pathName);
        addFile2Zip(file, pathName);
    }
    
    /**
     * 格式化路径，在最后面增加斜线
     * @param pathName 路径
     * @return 返回最终路径
     */
    private String getPathName(String pathName) {
        if (StringUtils.isNotEmpty(pathName)) {
            final String unixDirSepar = CharUtils.toString(IOUtils.DIR_SEPARATOR_UNIX);
            if (!pathName.endsWith(File.separator) || !pathName.endsWith(unixDirSepar)) {
                pathName = pathName + unixDirSepar;
            }
        } else {
            pathName = "";
        }
        return pathName;
    }

    /**
     * 把一个目录打包到一个指定的zip文件中
     * @param file 目录绝对地址
     * @param pathName zip文件抽象地址
     * @exception IOException IO错误
     */
    private void addFile2Zip(File file, String pathName) throws IOException {
        if (file.isDirectory()) {
            // 返回此绝对路径下的文件
            File[] files = file.listFiles();
            if (files == null || files.length < 1) {
                return;
            }

            for (int i = 0; i < files.length; i++) {
                // 判断此文件是否是一个文件夹
                if (files[i].isDirectory()) {
                    addFile2Zip(files[i], pathName + file.getName()
                            + File.separator);
                } else {
                    addOneFile2Zip(files[i], pathName);
                }
            }
        } else if (file.isFile()) {
            addOneFile2Zip(file, pathName);
        }
    }

    /**
     * 在Zip文件中添加一个压缩文件。
     * @param file 被压缩的文件
     * @param pathName 在zip文件中的目录名称
     * @throws IOException IO错误
     */
    private void addOneFile2Zip(File file, String pathName) throws IOException {
        FileInputStream is = new FileInputStream(file);
        this.addOneFile2Zip(is, file.getName(), pathName);
    }
    
    /**
     * 在Zip文件中添加一个输入流文件
     * @param is 输入流
     * @param fileName 输入流转换成文件之后的名称
     * @param pathName 路径名
     * @throws IOException 
     */
    public void addOneFile2Zip(InputStream is, String fileName, String pathName) throws IOException {
        try {
            pathName = this.getPathName(pathName);
            zaos.putArchiveEntry(new ZipArchiveEntry(pathName + fileName));
            IOUtils.copy(is, zaos);
            zaos.closeArchiveEntry();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
