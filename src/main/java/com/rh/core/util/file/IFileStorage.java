package com.rh.core.util.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件存储接口
 * @author wanglong
 *
 */
public interface IFileStorage {

    /**
     * 保存文件 保存后会close Inputstream，如果文件不存在则自动创建一个，如果文件存在则先删除再保存
     * @param absolutePath 文件绝对路径
     * @param input file input stream
     * @throws IOException 路径错误时会抛出IOException
     * @return total bytes size
     */
    long saveFile(String absolutePath, InputStream input) throws IOException;

    /**
     * 创建空白文件文件。能正常创建返回true，否则返回false
     * 
     * @param absolutePath 完整的文件路径。
     * @return 能正常创建返回true，否则返回false
     * @throws IOException - io exception
     */
    boolean createNewFile(String absolutePath) throws IOException;

    /**
     * 删除文件
     * @param absolutePath target file path
     * @return deleted ?
     * @throws IOException throws this exception
     */
    boolean deleteFile(String absolutePath) throws IOException;

    /**
     * 文件是否存在
     * @param absolutePath - file path
     * @return 文件是否存在
     * @throws IOException - IO exception
     */
    boolean exists(String absolutePath) throws IOException;
    
    /**
     * 获取文件输入流
     * @param absolutePath - 文件绝对路径
     * @return - inputStream 文件输入流 
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     */
    InputStream getInputStream(String absolutePath) throws IOException;

    /**
     * 获取文件输出流
     * @param absolutePath - 文件绝对路径
     * @return - outputStream 文件输出流
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     */
    OutputStream getOutputStream(String absolutePath) throws IOException;

    /**
     * 列出目录下所有文件
     * @param path - file path
     * @return file name array
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     */
    String[] list(String path) throws IOException;

    /**
     * 删除目录及下面所有文件
     * @param path - target Directory path
     * @return 是否删除成功
     * @throws IOException throws this exception
     */
    boolean deleteDirectory(String path) throws IOException;
    
    /**
     * 
     * @param path 文件路径
     * @return 文件修改时间
     * @throws IOException 异常
     */
    long lastModified(String path) throws IOException;
    
    /**
     * 设置文件的最后修改时间为当前时间
     * @param path 文件路径
     * @throws IOException 异常
     */
    void touch(String path) throws IOException;
}
