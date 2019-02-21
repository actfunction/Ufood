package com.rh.core.comm.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.rh.core.base.Bean;
import com.rh.core.comm.FileMgr;
import com.rh.core.util.Lang;

/**
 * UEditor文件上传辅助类
 * 
 */
public class Uploader {
    // 输出文件地址
    private String url = "";
    // 上传文件名
    private String fileName = "";
    // 状态
    private String state = "";
    // 文件类型
    private String type = "";
    // 原始文件名
    private String originalName = "";
    // 文件大小
    private String size = "";

    private HttpServletRequest request = null;
    private String title = "";

    // 保存路径
    // private String savePath = "upload";
    // 文件允许格式
    private String[] allowFiles = { ".rar", ".doc", ".docx", ".zip", ".pdf", ".txt", ".swf", ".wmv",
            ".gif", ".png", ".jpg", ".jpeg", ".bmp" };
    // 文件大小限制，单位KB
    private int maxSize = 10000;

    private HashMap<String, String> errorInfo = new HashMap<String, String>();

    /**
     * 初始化文件上传
     * @param request - http request
     */
    public Uploader(HttpServletRequest request) {
        this.request = request;
        HashMap<String, String> tmp = this.errorInfo;
        tmp.put("SUCCESS", "SUCCESS"); // 默认成功
        tmp.put("NOFILE", "未包含文件上传域");
        tmp.put("TYPE", "不允许的文件格式");
        tmp.put("SIZE", "文件大小超出限制");
        tmp.put("ENTYPE", "请求类型ENTYPE错误");
        tmp.put("REQUEST", "上传请求异常");
        tmp.put("IO", "IO异常");
        tmp.put("DIR", "目录创建失败");
        tmp.put("UNKNOWN", "未知错误");
    }

    /**
     * 文件上传
     * @throws Exception - upload exception
     */
    public void upload() throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(this.request);
        if (!isMultipart) {
            this.state = this.errorInfo.get("NOFILE");
            return;
        }
        DiskFileItemFactory dff = new DiskFileItemFactory();
        // String savePath = this.getFolder(this.savePath);
        // dff.setRepository(new File(savePath));
        try {
            ServletFileUpload sfu = new ServletFileUpload(dff);
            sfu.setSizeMax(this.maxSize * 1024);
            sfu.setHeaderEncoding("utf-8");
            FileItemIterator fii = sfu.getItemIterator(this.request);
            while (fii.hasNext()) {
                FileItemStream fis = fii.next();
                if (!fis.isFormField()) {
                    this.originalName = fis.getName().substring(
                            fis.getName().lastIndexOf(System.getProperty("file.separator")) + 1);
                    if (!this.checkFileType(this.originalName)) {
                        this.state = this.errorInfo.get("TYPE");
                        continue;
                    }
                    // this.fileName = this.getName(this.originalName);
                    // this.type = this.getFileExt(this.fileName);
                    // this.url = savePath + "/" + this.fileName;
                    BufferedInputStream in = new BufferedInputStream(fis.openStream());
                    // FileOutputStream out = new FileOutputStream(new File(this.getPhysicalPath(this.url)));
                    // BufferedOutputStream output = new BufferedOutputStream(out);
                    // Streams.copy(in, output, true);

                    String servId = this.request.getParameter("SERV_ID");
                    String dataId = this.request.getParameter("DATA_ID");
                    String category = this.request.getParameter("FILE_CAT");
                    Bean file = FileMgr.upload(servId, dataId, category, in, this.originalName);
                    IOUtils.closeQuietly(in);
                    this.type = this.getFileExt(this.originalName);
                    this.url = file.getId();

                    this.state = this.errorInfo.get("SUCCESS");
                    // UE中只会处理单张上传，完成后即退出
                    break;
                } else {
                    String fname = fis.getFieldName();
                    // 只处理title，其余表单请自行处理
                    if (!fname.equals("pictitle")) {
                        continue;
                    }
                    BufferedInputStream in = new BufferedInputStream(fis.openStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuffer result = new StringBuffer();
                    while (reader.ready()) {
                        result.append((char) reader.read());
                    }
                    this.title = new String(result.toString().getBytes(), "utf-8");
                    reader.close();

                }
            }
        } catch (SizeLimitExceededException e) {
            this.state = this.errorInfo.get("SIZE");
        } catch (InvalidContentTypeException e) {
            this.state = this.errorInfo.get("ENTYPE");
        } catch (FileUploadException e) {
            this.state = this.errorInfo.get("REQUEST");
        } catch (Exception e) {
            this.state = this.errorInfo.get("UNKNOWN");
        }
    }

    /**
     * 接受并保存以base64格式上传的文件
     * @param fieldName - 文件名称
     */
    public void uploadBase64(String fieldName) {
        // String savePath = this.getFolder(this.savePath);
        String base64Data = this.request.getParameter(fieldName);
        // this.fileName = this.getName("test.png");
        // this.url = savePath + "/" + this.fileName;
        try {
            // File outFile = new File(this.getPhysicalPath(this.url));
            // OutputStream ro = new FileOutputStream(outFile);
            byte[] b = Lang.decodeBase64(base64Data).getBytes();
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }

            String servId = this.request.getParameter("SERV_ID");
            String dataId = this.request.getParameter("DATA_ID");
            String category = this.request.getParameter("FILE_CAT");

            InputStream in = new ByteArrayInputStream(b);
            Bean file = FileMgr.upload(servId, dataId, category, in, "");
            IOUtils.closeQuietly(in);
           // this.type = this.getFileExt(this.originalName);
            this.url = file.getId();
            this.fileName = file.getId();

            // ro.write(b);
            // ro.flush();
            // ro.close();
            this.state = this.errorInfo.get("SUCCESS");
        } catch (Exception e) {
            this.state = this.errorInfo.get("IO");
        }
    }

    /**
     * 文件类型判断
     * 
     * @param fileName - 文件名
     * @return - 是否问允许类型
     */
    private boolean checkFileType(String fileName) {
        for (String ext : this.allowFiles) {
            if (fileName.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     * 
     * @param fileName - 文件名
     * @return string
     */
    private String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 依据原始文件名生成新文件名
     * @param fileName - 文件名
     * @return 新文件名
     */
    // private String getName(String fileName) {
    // Random random = new Random();
    // return this.fileName = "" + random.nextInt(10000)
    // + System.currentTimeMillis() + this.getFileExt(fileName);
    // }

    /**
     * 根据字符串创建本地目录 并按照日期建立子目录返回
     * @param path
     * @return
     */
    // private String getFolder(String path) {
    // SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
    // path += "/" + formater.format(new Date());
    // File dir = new File(this.getPhysicalPath(path));
    // if (!dir.exists()) {
    // try {
    // dir.mkdirs();
    // } catch (Exception e) {
    // this.state = this.errorInfo.get("DIR");
    // return "";
    // }
    // }
    // return path;
    // }

    /**
     * 根据传入的虚拟路径获取物理路径
     * 
     * @param path - 相对路径
     * @return - 绝对路径
     */
    // private String getPhysicalPath(String path) {
    // String servletPath = this.request.getServletPath();
    // String realPath = this.request.getSession().getServletContext()
    // .getRealPath(servletPath);
    // return new File(realPath).getParent() + "/" + path;
    // }

    /**
     * 设置保存路径
     * @param savePath - 保存路径
     */
    public void setSavePath(String savePath) {
        // this.savePath = savePath;
    }

    /**
     * 设置同意上传文件类型
     * @param allowFiles - 允许上传文件类型数组 example:{.jpg,.png}
     */
    public void setAllowFiles(String[] allowFiles) {
        this.allowFiles = allowFiles;
    }

    /**
     * 设置文件上传最大值
     * @param size - size in bytes
     */
    public void setMaxSize(int size) {
        this.maxSize = size;
    }

    /**
     * 获取文件大小
     * @return size
     */
    public String getSize() {
        return this.size;
    }

    /**
     * 获取文件URL
     * @return - url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * 获取文件名称
     * @return - 文件名
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * 文件上传状态
     * @return - 状态码
     */
    public String getState() {
        return this.state;
    }

    /**
     * 文件标题
     * @return - 文件标题
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * 文件类型
     * @return - 文件类型
     */
    public String getType() {
        return this.type;
    }

    /**
     * 文件原始名称
     * @return - 文件原始名称
     */
    public String getOriginalName() {
        return this.originalName;
    }
}
