package com.rh.core.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.comm.FileStorage;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;

/**
 * 文件帮助类
 * 
 * @author wanglong
 * 
 */
public class FileHelper {

    /** log */
    private static Log log = LogFactory.getLog(FileHelper.class);

    /**
     * 将数据对象存储为json格式的文本文件
     * @param obj 数据对象
     * @param fileName 文件名称，全路径名称
     */
    public static void toJsonFile(Object obj, String fileName) {
        try {
            FileStorage.saveFile(IOUtils.toInputStream(JsonUtils.toJson(obj, true), Constant.ENCODING), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 从json文本文件还原Bean对象
     * @param fileName 文件名
     * @return 还原的bean对象，如果文件不存在则返回null
     */
    public static Bean fromJsonFile(String fileName) {
        try {
            InputStream is = FileStorage.getInputStream(fileName);
            return fromJsonFile(is);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 从json流还原Bean对象
     * @param in 输入流
     * @return 还原的bean对象，如果文件不存在则返回null
     */
    public static Bean fromJsonFile(InputStream in) {
        try {
            return JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (in != null) {
                IOUtils.closeQuietly(in);
            }
        }
    }

    /**
     * 从json流还原list<Bean>对象
     * @param in 输入流
     * @return 还原的bean对象，如果文件不存在则返回null
     */
    public static List<Bean> listFromJsonFile(InputStream in) {
        try {
            return JsonUtils.toBeanList(IOUtils.toString(in, Constant.ENCODING));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (in != null) {
                IOUtils.closeQuietly(in);
            }
        }
    }

    /**
     * 从文件读取所有服务的定义信息
     * @param pathName 路径名称
     * @return 服务定义列表
     */
    public static List<Bean> getJsonListByFile(String pathName) {
        List<Bean> dictList = new ArrayList<Bean>();
        try {
            String[] files = FileStorage.list(pathName);
            for (String name : files) {
                if (name.toLowerCase().endsWith("json")) {
                    Bean serv = FileHelper.fromJsonFile(pathName + name);
                    dictList.add(serv);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return dictList;
    }

    /**
     * 将对象序列化
     * 
     * @param obj 对象
     * @param fileName 文件名称
     */
    public static void ser(Object obj, String fileName) {
        try {
            ByteArrayOutputStream out = null;
            ObjectOutputStream oos = null;
            try {
                out = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(out);
                // 为对象输出流实例化
                oos.writeObject(obj);
                oos.close();
                FileStorage.saveFile(new ByteArrayInputStream(out.toByteArray()), fileName);
            } finally {
                if (oos != null) {
                    oos.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 对象反序列化
     * 
     * @param fileName 文件名称
     * @return 对象
     */
    public static Object dser(String fileName) {
        try {
            ObjectInputStream ois = null;
            InputStream input = null;
            Object obj = null;
            try {
                input = FileStorage.getInputStream(fileName);
                // 文件输入流
                ois = new ObjectInputStream(input);
                // 为对象输出流实例化
                obj = (Object) ois.readObject();
                // 读取对象数组
            } finally {
                if (ois != null) {
                    ois.close();
                }
                if (input != null) {
                    input.close();
                }
            }
            // 关闭输出
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 判断文件是否存在
     * 
     * @param fileName 文件名
     * @return true/false
     */
    public static boolean exists(String fileName) {
        try {
            return FileStorage.exists(fileName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除文件
     * 
     * @param fileName 文件名
     * @return 是否成功删除
     */
    public static boolean delete(String fileName) {
        boolean deleteFlag;
        try {
            deleteFlag = FileStorage.deleteFile(fileName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            deleteFlag = false;
        }
        return deleteFlag;
    }

    /**
     * 读取文本文件信息，得到存放文本信息的字符串
     * @param file 文件全路径名
     * @return 文本字符串
     */
    public static String readFile(String file) {
        try {
            File templateFile = new File(file);
            if (!templateFile.exists()) {
                throw new RuntimeException(Context.getSyMsg("SY_COMM_FILE_NOT_EXIST", file));
            }
            return FileUtils.readFileToString(templateFile, Constant.ENCODING);
        } catch (IOException e) {
            throw new RuntimeException(Context.getSyMsg("SY_COMM_FILE_NOT_EXIST", file));
        }
    }

    /**
     * 读取文本文件信息，得到存放文本信息的字符串
     * @param in 输入流
     * @return 文本字符串
     */
    public static String readFile(InputStream in) {
        StringBuffer result = new StringBuffer("");
        String thisLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while (null != (thisLine = br.readLine())) {
                result.append(thisLine);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(Context.getSyMsg("SY_COMM_FILE_NOT_EXIST"));
        }

        return result.toString();
    }

    /**
     * 解压缩jar文件
     * @param src 源文件
     * @param fh 行文件处理器
     */
    public static void unJar(File src, FileHandler fh) {
        JarInputStream jarIn;
        try {
            jarIn = new JarInputStream(new BufferedInputStream(new FileInputStream(src)));
            byte[] bytes = new byte[1024];
            while (true) {
                ZipEntry entry = jarIn.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                if (entry.isDirectory()) { // jar条目是空目录
                    BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(bytes));
                    int len = jarIn.read(bytes, 0, bytes.length);
                    while (len != -1) {
                        len = jarIn.read(bytes, 0, bytes.length);
                    }
                    fh.handle(entry.getName(), in);
                    in.close();
                } // end if
                jarIn.closeEntry();
            } // end while
              // 关闭JarInputStream
            jarIn.close();
        } catch (Exception e) {
            throw new RuntimeException(Context.getSyMsg("SY_COMM_FILE_NOT_EXIST", src.getName()));
        }
    }

    /**
     * 获取JSON文件路径，缺省WEB-INF/doc（设定为@WEB-INF-DOC@），否则采用实际的文件路径
     * @return 获取JSON文件路径
     */
    public static String getJsonPath() {
        String path = Context.app("JSON_LOCATION", "@WEB-INF-DOC@");
        if (path.equals("@WEB-INF-DOC@")) { // 获取文件路径，缺省WEB-INF/doc
            path = Context.appStr(APP.WEBINF_DOC);
        }
        return path;
    }

    /**
     * 解压zip文件到指定目录，如果被解压的文件存在错误，会忽略此错误继续解压
     * 
     * @param directory 要解压到的目录，没有就创建一个新目录
     * @param zip 工具包文件
     * @return 成功解压的文件和目录数量
     */
    public static int unzipFile(String directory, File zip) {
        ZipInputStream zis = null;
        int count = 0;
        try {
            zis = new ZipInputStream(new FileInputStream(zip));
            ZipEntry ze = zis.getNextEntry();
            File parent = new File(directory);
            if (!parent.exists() && !parent.mkdirs()) {
                throw new RuntimeException("create unzip directory \"" + parent.getAbsolutePath() + "\" error!");
            }
            while (ze != null) {
                FileOutputStream output = null;
                String name = ze.getName();
                try {
                    log.debug("unzip: " + name);
                    File child = new File(parent, name);
                    if (ze.isDirectory()) {
                        if (!child.exists()) {
                            child.mkdirs();
                        }
                    } else {
                        output = new FileOutputStream(child);
                        byte[] buffer = new byte[10240];
                        int bytesRead = 0;
                        while ((bytesRead = zis.read(buffer)) > 0) {
                            output.write(buffer, 0, bytesRead);
                        }
                        output.flush();
                        count++;
                    }
                    ze = zis.getNextEntry();
                } catch (Exception we) {
                    log.error(we.getMessage() + " " + name, we);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            } //end while
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(zis);
        }
        return count;
    }
    
    /**
     * 在系统的临时目录下创建一个名称唯一，且不会重复的文件，最后返回File对象。
     * @param fileId 文件的UUID，可以为null。
     * @return 临时目录下的临时文件File对象。
     */
    public static File getTempFile(String fileId) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        String tempFileName = "rh_";
        if (StringUtils.isBlank(fileId)) {
            tempFileName += RandomStringUtils.randomAlphanumeric(20);
        } else {
            tempFileName += fileId;
        }
        tempFileName += RandomStringUtils.randomAlphanumeric(10) + ".tmp";

        File tempFile = new File(tempDir, tempFileName);
        return tempFile;
    }
}
