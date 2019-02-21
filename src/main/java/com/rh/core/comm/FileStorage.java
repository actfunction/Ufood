package com.rh.core.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rh.core.base.Context;
import com.rh.core.util.Lang;
import com.rh.core.util.file.IFileStorage;

/**
 * @author wanglong
 * 
 *         文件存储(storage)
 * 
 */
public class FileStorage {

    /**
     * 保存文件 保存后会close Inputstream
     * @param input file input stream
     * @param absolutePath 文件绝对路径
     * @throws IOException 路径错误时会抛出IOException
     * @return total bytes size
     */
    public static long saveFile(InputStream input, String absolutePath) throws IOException {
        return getStorage().saveFile(absolutePath, input);
    }

    /**
     * 创建文件。能正常创建返回true，否则返回false
     * 
     * @param absolutePath 完整的文件路径。
     * @return 如果能创建文件，则返回true，否则返回false
     * @throws IOException - io exception
     */
    public static boolean createFile(String absolutePath) throws IOException {
        return getStorage().createNewFile(absolutePath);
    }

    /**
     * 下载文件
     * @param absolutePath 文件绝对路径
     * @return InputStream file inputstream
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     * @deprecated - 请使用getInputStream()
     */
    public static InputStream downloadFromPath(String absolutePath) throws IOException {
        return getInputStream(absolutePath);
    }

    /**
     * 获取文件输入流
     * @param absolutePath - 文件绝对路径
     * @return - output
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     */
    public static InputStream getInputStream(String absolutePath) throws IOException {
        return getStorage().getInputStream(absolutePath);
    }

    /**
     * 获取文件输出流
     * @param absolutePath - 文件绝对路径
     * @return - output
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     */
    public static OutputStream getOutputStream(String absolutePath) throws IOException {
        return getStorage().getOutputStream(absolutePath);
    }

    /**
     * list
     * @param absolutePath - file path
     * @return file name array
     * @throws IOException - IOException throws this exception,if the file not found or the path is a folder
     */
    public static String[] list(String absolutePath) throws IOException {
        return getStorage().list(absolutePath);
    }

    /**
     * 文件是否存在
     * @param absolutePath - file path
     * @return 文件是否存在
     * @throws IOException - IO exception
     */
    public static boolean exists(String absolutePath) throws IOException {
        return getStorage().exists(absolutePath);
    }

    /**
     * delete file
     * @param absolutePath target file path
     * @return deleted ?
     * @throws IOException throws this exception
     */
    public static boolean deleteFile(String absolutePath) throws IOException {
        return getStorage().deleteFile(absolutePath);
    }

    /**
     * delete Directory
     * @param absolutePath - target Directory path
     * @return deleted ?
     * @throws IOException throws this exception
     */
    public static boolean deleteDirectory(String absolutePath) throws IOException {
        return getStorage().deleteDirectory(absolutePath);
    }
    
    /**
     * 
     * @param absolutePath  文件路径
     * @return 文件的最后修改时间，如果为-1，表示文件不存在。
     * @throws IOException 异常
     */
    public static long lastModified(String absolutePath) throws IOException {
        return getStorage().lastModified(absolutePath);
    }
    
    /**
     * 
     * @param absolutePath 文件路径
     * @throws IOException 异常
     */
    public static void touch(String absolutePath) throws IOException {
        getStorage().touch(absolutePath);
    }
    
    /**
     * 获取文件服务实现类
     * @return 文件服务实现类
     */
    private static IFileStorage getStorage() {
        return (IFileStorage) Lang.createObject(IFileStorage.class, Context.getInitConfig("rh.file", 
                "com.rh.core.util.file.CommonFileStorage"));
    }

}
