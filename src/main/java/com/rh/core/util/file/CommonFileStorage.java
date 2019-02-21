package com.rh.core.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author wanglong
 * 
 *         文件存储(storage)
 * 
 */
public class CommonFileStorage implements IFileStorage {

    /** log */
    private Log log = LogFactory.getLog(CommonFileStorage.class);

    /** Server Message Block 用于共享例如文件、打印机、串口或者是命名管道等用于通讯的抽象对象； */
    private static final String SMB_PREFIX = "smb://";

    @Override
    public long saveFile(String absolutePath, InputStream input) throws IOException {
        long size = -1;
        if (absolutePath.startsWith(SMB_PREFIX)) {
            // save to windows share file system
            SmbFile target = new SmbFile(absolutePath);
            SmbFile parent = new SmbFile(target.getParent());
            if (!parent.exists()) {
                //yangjinyun：在大并发情况下，其它线程可能已经创建了此目录，因此在此处忽略此类错误。
                try {
                    parent.mkdirs();
                } catch (Exception e) {
                    log.error("create directory error:" + parent.getPath(), e);
                }
            }
            OutputStream os = target.getOutputStream();
            try {
                size = IOUtils.copyLarge(input, os);
            } catch (Exception e) {
                log.error("copy file error.", e);
                throw new IOException(e);
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(os);
            }

        } else {
            File target = new File(absolutePath);
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            // move file to upload_file_path
            OutputStream newOut = new FileOutputStream(target);
            try {
                size = IOUtils.copyLarge(input, newOut);
            } catch (Exception e) {
                log.error("move file error.", e);
                throw new IOException(e);
            } finally {
                IOUtils.closeQuietly(newOut);
                IOUtils.closeQuietly(input);
            }
        }
        return size;
    }

    @Override
    public boolean createNewFile(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            // save to windows share file system
            SmbFile target = new SmbFile(path);
            SmbFile parent = new SmbFile(target.getParent());
            if (!parent.exists()) {
                parent.mkdirs();
            }
            // 文件存在，则返回false
            if (target.exists()) {
                return false;
            }

            try {
                // 创建文件成功，则返回true
                target.createNewFile();
                return true;
            } catch (Exception e) {
                // 创建文件失败，返回false
                return false;
            }
        } else {
            File target = new File(path);
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            // 文件存在则返回false
            if (target.exists()) {
                return false;
            }

            return target.createNewFile();
        }
    }

    @Override
    public InputStream getInputStream(String absolutePath) throws IOException {
        try {
            absolutePath = URLDecoder.decode(absolutePath, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.warn("url decode error" + e1);
        }
        if (absolutePath.startsWith(SMB_PREFIX)) {
            // get from windows share file system
            SmbFileInputStream in = null;
            try {
                in = new SmbFileInputStream(absolutePath);
            } catch (Exception e) {
                throw new FileNotFoundException(absolutePath);
            }
            return in;
        } else {
            // get from local file system
            File file = new File(absolutePath);
            return new FileInputStream(file);
        }
    }

    @Override
    public OutputStream getOutputStream(String absolutePath) throws IOException {
        try {
            absolutePath = URLDecoder.decode(absolutePath, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.warn("url decode error" + e1);
        }
        if (absolutePath.startsWith(SMB_PREFIX)) {
            // get from windows share file system
            SmbFileOutputStream out = null;
            try {
                SmbFile target = new SmbFile(absolutePath);
                SmbFile parent = new SmbFile(target.getParent());
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                out = new SmbFileOutputStream(absolutePath);
            } catch (Exception e) {
                throw new FileNotFoundException(absolutePath);
            }
            return out;
        } else {
            File target = new File(absolutePath);
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            // get from local file system
            File file = new File(absolutePath);
            return new FileOutputStream(file);
        }
    }

    @Override
    public String[] list(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            // get from windows share file system
            SmbFile target = new SmbFile(path);
            if (!target.exists()) {
                log.warn(" the path is not a file:" + path);
            }
            if (!target.isFile()) {
                log.warn(" the path is not a file:" + path);
                // throw new IOException(" the path is not a file:" + path);
            }
            return target.list();
        } else {
            // get from local file system
            File target = new File(path);
            if (!target.exists()) {
                // throw new IOException(" the file not found:" + path);
                log.warn(" the path not exists:" + path);
            }
            if (target.isFile()) {
                log.warn(" the path is a file:" + path);
                // throw new IOException(" the path is not a file:" + path);
            }
            return target.list();
        }
    }

    @Override
    public boolean exists(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            // get file from windows share file system
            SmbFile target = new SmbFile(path);
            return target.exists();
        } else {
            // get file from local file system
            File target = new File(path);
            return target.exists();
        }
    }

    @Override
    public boolean deleteFile(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            // delete from windows share file system
            SmbFile target = new SmbFile(path);
            if (!target.exists()) {
                log.warn(" the path can not be found:" + path);
                return false;
            }
            if (!target.isFile()) {
                log.warn(" the path is not a file:" + path);
                return false;
            }
            //捕获异常
            try {
            target.delete();
            } catch (Exception e) {
                throw new IOException(e);
            }
            return true;
        } else {
            // delete from local file system
            File target = new File(path);
            if (!target.exists()) {
                // throw new IOException(" the file not found:" + path);
                log.warn(" the path can not be found:" + path);
                return false;
            }
            if (!target.isFile()) {
                log.warn(" the path is not a file:" + path);
                return false;
                // throw new IOException(" the path is not a file:" + path);
            }
            return target.delete();
        }

    }

    @Override
    public boolean deleteDirectory(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            // delete from windows share file system
            SmbFile target = new SmbFile(path);
            if (!target.exists()) {
                log.warn(" the path can not be found:" + path);
                return false;
            }
            if (!target.isDirectory()) {
                log.warn(" the path is not a file:" + path);
                return false;
            }
            target.delete();
            return true;
        } else {
            // delete from local file system
            File target = new File(path);
            if (!target.exists()) {
                // throw new IOException(" the file not found:" + path);
                log.warn(" the path can not be found:" + path);
                return false;
            }
            if (!target.isDirectory()) {
                log.warn(" the path is not a file:" + path);
                return false;
                // throw new IOException(" the path is not a file:" + path);
            }
            return target.delete();
        }
    }

    @Override
    public long lastModified(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            SmbFile target = new SmbFile(path);
            if (target.exists()) {
                return target.lastModified();
            }
        } else {
            File target = new File(path);
            if (target.exists()) {
                return target.lastModified();
            }
        }
        return -1;
    }

    @Override
    public void touch(String path) throws IOException {
        if (path.startsWith(SMB_PREFIX)) {
            SmbFile target = new SmbFile(path);
            if (target.exists()) {
                target.setLastModified(System.currentTimeMillis());
            }
        } else {
            File target = new File(path);
            if (target.exists()) {
                boolean success = target.setLastModified(System.currentTimeMillis());
                if (!success) {
                    throw new IOException("Unable to set the last modification time for " + path);
                }
            }
        }        

    }
}
