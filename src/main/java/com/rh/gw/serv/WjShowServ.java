/*

 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.gw.serv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.rh.core.base.BaseContext;
import com.rh.core.comm.FileMgr;
import com.rh.core.comm.FileStorage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.file.TempFile;
import com.rh.core.comm.file.TempFile.Storage;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;


/**
 * 渲染非收文转收文的页面时自动刷新一个正文
 *
 * @author yxb
 */
public class WjShowServ {

    /** log */
    private static Log log = LogFactory.getLog(WjShowServ.class);
    /** service Id */
    public static final String CURRENT_SERVICE = "SY_COMM_FILE";
    /** history file service id */
    public static final String HISTFILE_SERVICE = "SY_COMM_FILE_HIS";
    /** oa history file service id */
    public static final String OA_HISTFILE_SERVICE = "OA_GW_COMM_FILE_HIS";

    /** history file id prefix */
    private static final String HISTFILE_ID_PREFIX = "HIST_";

    /** OA history file id prefix */
    private static final String OA_HISTFILE_ID_PREFIX = "OAHIST_";

    /** 默认文件ROOT路径配置Key */
    private static final String OA_TRAN_FILE_ROOT_PATH = "OA_TRAN_FILE_ROOT_PATH";
    /** icon image file id prefix */
    private static final String IMAGE_ICON_PREFIX = "ICON_";
    /** user icon image file id prefix */
    private static final String IMAGE_USER_PREFIX = "USER_";

    private static final String IMAGE_GROUP_PREFIX = "GROUP_";

    private static final String IMAGE_THUMBNAIL = "THUM_";

    /** 默认保存文件路径 */
    private static final String DEFAULT_FILE_ROOT_PATH = System.getProperty("java.io.tmpdir");

    /** 系统默认文件root路径 */
    private static final String SY_COMM_FILE_PATH_EXPR = "@SYS_FILE_PATH@";
    /** 将加密的文件复制到指定的路径 */
    private static final String HIS_COMM_FILE_PATH_EXPR = "@OA_FILE_PATH@";
    /** 当前系统部署路径 */
    private static final String CURRENT_SYSTEM_HOME_PATH_EXPR = "@" + APP.SYSPATH + "@";
    /** 当前系统部署下WEB-INF路径 */
    private static final String CURRENT_WEBINF_PATH_EXPR = "@" + APP.WEBINF + "@";
    /** 当前系统部署下WEB-INF/doc路径 */
    private static final String CURRENT_WEBINF_DOC_PATH_EXPR = "@" + APP.WEBINF_DOC + "@";
    /** 当前系统部署下WEB-INF/doc/cmpy路径 */
    private static final String CURRENT_WEBINF_DOC_CMPY_PATH_EXPR = "@" + APP.WEBINF_DOC_CMPY + "@";

    /** 默认文件ROOT路径配置Key */
    private static final String SY_COMM_FILE_ROOT_PATH = "SY_COMM_FILE_ROOT_PATH";
    /** 默认保存文件路径规则表达式 最后必须以“/”结尾 */
    private static final String DEFAULT_FILE_PATH_EXPR = SY_COMM_FILE_PATH_EXPR + "/"
            + "@CMPY_CODE@/@SERV_ID@/@DATE_YEAR@/@DATE@/";
    /**
     * can not new instance
     */


    /**
     * 上传文件
     *
     * @param paramBean
     *            - 参数
     * @return 结果
     */
    public static Bean upload(Bean paramBean) {
        InputStream input = null;
        try {
            input = new FileInputStream(BaseContext.appStr(APP.WEBINF)+"/GwTmpl/tmpl.docx");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("get file error.", e);
        }
        Bean result = null;
        // Process a file upload
        Bean fileParam = createFileBean(paramBean);
        String pathExpre = fileParam.getStr("FILE_PATH");
        String absolutePath = getAbsolutePath(pathExpre);

        // file checksum
        String checksum = "";
        // 因大多数inputstream 不支持markSupported,使用临时文件对象保存文件流.
        TempFile tmp = null;
        try {
            long start = System.currentTimeMillis();
            // save inputstream data to Temporary storage
            tmp = new TempFile(Storage.SMART, input);
            tmp.read();
            IOUtils.closeQuietly(input);
            log.debug(" read inputstream to temp storage qtime:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();

            InputStream is1 = tmp.openNewInputStream();
            // extract file md5 checksum
            checksum = Lang.getMd5checksum(is1);
            IOUtils.closeQuietly(is1);

            start = System.currentTimeMillis();
            InputStream is2 = tmp.openNewInputStream();
            long size = FileStorage.saveFile(is2, absolutePath);
            IOUtils.closeQuietly(is2);
            log.debug(" save file to storage qtime:" + (System.currentTimeMillis() - start));
            // get the real size
            fileParam.set("FILE_SIZE", size);

        } catch (NoSuchAlgorithmException ne) {
            log.error(" get file checksum error.", ne);
            throw new RuntimeException("get file checksum error.", ne);
        } catch (IOException ioe) {
            log.error(" file upload error.", ioe);
            throw new RuntimeException("file upload error.", ioe);
        } finally {
            IOUtils.closeQuietly(input);
            tmp.destroy();
        }

        // set default display name
        fileParam.set("DIS_NAME", paramBean.getStr("DIS_NAME"));
        if (fileParam.isEmpty("DIS_NAME")) {
            // replace file suffix
            String fileName = paramBean.getStr("FILE_NAME");

            fileParam.set("DIS_NAME", FilenameUtils.getBaseName(fileName));
        }

        fileParam.set("FILE_CHECKSUM", checksum);
        fileParam.set("ITEM_CODE","ZHENGWEN");
        // save file info in database
        result = ServDao.create(CURRENT_SERVICE, fileParam);
        List<Bean> dataList = new ArrayList<Bean>();
        dataList.add(result);
        Bean resultBean = new Bean();
        resultBean.set(Constant.RTN_DATA, dataList);
        return resultBean;
    }

    /**
     *
     * @param servSrcId
     *            数据对应父服务ID
     * @param dataId
     *            数据ID
     * @return 查询指定数据对应的所有文件列表
     */
    public static List<Bean> getFileListBean(String servSrcId, String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList<Bean>();
        }

        ParamBean sql = new ParamBean();
        sql.setQueryNoPageFlag(true);
        sql.set("DATA_ID", dataId);
        sql.set(Constant.PARAM_ORDER, " FILE_SORT asc, S_MTIME asc");
        // sql.set("SERV_ID", servSrcId);

        return ServDao.finds(ServMgr.SY_COMM_FILE, sql);
    }

    /**
     * 创建一个新文件，新文件为指定文件的链接文件。新文件没有实际的物理文件，实际物理文件路径为老文件的地址，达到节省磁盘空间的目的。
     *
     * @param fileBean
     *            指定文件
     * @param param
     *            新文件的参数Bean。
     * @return 新创建的链接文件。
     */
    public static Bean createLinkFile(Bean fileBean, Bean param) {
        // 生成新文件UUID
        String fileUUID = Lang.getUUID() + "." + FilenameUtils.getExtension(fileBean.getId());
        // 构造新的File Bean
        Bean newFile = fileBean.copyOf();

        // 设置新的文件ID和新的文件路径
        newFile.set("FILE_ID", fileUUID);

        // 清除一些与老数据有关，且与新数据不一致的属性值。
        newFile.remove("S_MTIME").remove("S_FLAG");
        newFile.remove("S_USER").remove("S_UNAME");
        newFile.remove("S_DEPT").remove("S_DNAME");
        newFile.remove("S_CMPY").remove("FILE_HIST_COUNT").remove("WF_NI_ID");

        if (newFile.isEmpty("ORIG_FILE_ID")) {
            newFile.set("ORIG_FILE_ID", fileBean.getId());
        }

        // 合并属性值
        extendBean(newFile, param);

        return ServDao.create("SY_COMM_FILE", newFile);
    }


    /**
     * 把参数Bean中的Value覆盖到newFileBean中
     *
     * @param newFileBean
     *            文件Bean
     * @param param
     *            参数Bean
     */
    private static void extendBean(Bean newFileBean, Bean param) {
        // 复制param里传过来的key-value
        @SuppressWarnings("rawtypes")
        Iterator it = param.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            newFileBean.set(key, param.getStr(key));
        }
    }

    /**
     * 获取文件root路径
     *
     * @return 保存文件ROOT路径
     */
    public static String getRootPath() {
        String result = "";
        result = Context.getSyConf(SY_COMM_FILE_ROOT_PATH, DEFAULT_FILE_ROOT_PATH);
        if (!result.endsWith("/") && !result.endsWith(File.separator)) {
            result += "/";
        }
        return result;
    }

    /**
     * update file
     *
     * @param file
     *            <CODE>Bean</CODE>
     */
    public static void updateFile(Bean file) {
        ServDao.update(CURRENT_SERVICE, file);
    }

    /**
     * delete file from file system
     *
     * @param files
     *            files
     * @return delete file count
     */
    public static int deleteFile(List<Bean> files) {
        int count = 0;
        for (Bean file : files) {
            try {
                if (deleteFile(file)) {
                    count++;
                }
            } catch (Exception e) {
                log.warn("delete file failed from disk.", e);
            }
        }
        return count;
    }

    /**
     *
     * @param file
     *            被删除文件Bean
     * @return 指定路径的文件是否还有其他记录使用，如果有，则返回true表示占用中，false表示未占用。
     */
    private static boolean isOccupied(Bean file) {
        Bean paramBean = new Bean();
        paramBean.set("FILE_PATH", file.getStr("FILE_PATH"));

        int count = ServDao.count(CURRENT_SERVICE, paramBean);
        if (count > 1) {
            return true;
        }

        return false;
    }

    /**
     * 删除文件 (db & fs)
     *
     * @param file
     *            - file bean
     * @return deletes result
     */
    public static boolean deleteFile(Bean file) {
        boolean result = false;
        ServDao.delete(CURRENT_SERVICE, file);

        // 判断删除文件路径是否为空
        if (null == file.getStr("FILE_PATH") || 0 == file.getStr("FILE_PATH").length()) {
            return true;
        }

        // 如果物理文件被占用，则不删除
        if (isOccupied(file)) {
            return true;
        }

        try {
            // 删除源文件
            String absolutePath = FileMgr.getAbsolutePath(file.getStr("FILE_PATH"));
            FileStorage.deleteFile(absolutePath);
            // 删除相关文件,(如果存在)
            String relatedPath = buildRelatedPathExpress(file.getStr("FILE_PATH"), "");
            relatedPath = getAbsolutePath(relatedPath);
            boolean exits = FileStorage.exists(relatedPath);
            if (exits) {
                FileStorage.deleteDirectory(relatedPath);
            }
            result = true;
        } catch (IOException e) {
            log.warn("delete file failed from disk.", e);
            result = false;
        }
        return result;
    }

    /**
     * get file mime Type
     *
     * @param suffix
     *            file name suffix
     * @return file mtype
     */
    public static String getMTypeBySuffix(String suffix) {
        String contentType = "application/octet-stream";
        if (suffix != null && suffix.length() > 0) {
            Bean result = DictMgr.getItem("SY_COMM_FILE_MTYPE", suffix);
            if (null != result && result.contains("ITEM_NAME")) {
                contentType = result.getStr("ITEM_NAME");
            }
        }
        return contentType;
    }

    /**
     * get file mime type
     *
     * @param name
     *            file name
     * @return mtype
     */
    public static String getMTypeByName(String name) {
        String suffix = getSuffix(name);
        return getMTypeBySuffix(suffix);

    }

    /**
     * 生成相关文件的路径表达式
     *
     * @param fileExpre
     *            - 源文件路径表达式
     * @param relatedFile
     *            - 相关文件名
     * @return relatedPathExpress
     */
    private static String buildRelatedPathExpress(String fileExpre, String relatedFile) {
        String targetExpre = fileExpre + "_file/" + relatedFile;
        return targetExpre;
    }


    /**
     * 截取文件后缀
     *
     * @param fileId
     *            - file id
     * @return 后缀字符串, example:png
     */
    public static String getSuffix(String fileId) {
        String suffix = "";
        if (0 < fileId.lastIndexOf(".")) {
            suffix = fileId.substring(fileId.lastIndexOf(".") + 1);
        }
        return suffix;
    }

    /**
     * 通过 mtype 返回后缀
     *
     * @param mtype
     *            - mtype
     * @return 后缀
     *
     */
    public static String getSuffixByMtype(String mtype) {
        // TODO read dict
        if (mtype.startsWith("image/jpeg")) {
            return ".jpg";
        } else if (mtype.startsWith("image/bmp")) {
            return ".bmp";
        } else if (mtype.startsWith("image/gif")) {
            return ".gif";
        } else if (mtype.startsWith("image/png")) {
            return ".png";
        } else {
            return "";
        }
    }

    /**
     * 创建文件Bean
     *
     * @param paramBean
     *            参数
     * @return 结果
     */
    private static Bean createFileBean(Bean paramBean) {
        Bean itemParam = new Bean();

        String fileName = paramBean.getStr("FILE_NAME");

        String uuid = Lang.getUUID();
        String suffix = "";
        if (0 < fileName.lastIndexOf(".")) {
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }

        if (null == suffix || 0 == suffix.length()) {
            suffix = getSuffixByMtype(paramBean.getStr("FILE_MTYPE"));
        }

        String servId = paramBean.getStr("SERV_ID");

        String pathExpr = buildPathExpr(servId, uuid + suffix);
        // String absolutePath = getAbsolutePath(relativePath);

        String fileId = paramBean.getStr("FILE_ID");

        if (null == fileId || 0 == fileId.length()) {
            itemParam.set("FILE_ID", uuid + suffix);
        } else {
            itemParam.set("FILE_ID", fileId);
        }
        String mType = paramBean.getStr("FILE_MTYPE");
        if (null == mType || mType.length() == 0) {
            mType = getMTypeByName(fileName);
        }

        long sizeInBytes = -1;
        if (paramBean.isNotEmpty("FILE_SIZE")) {
            sizeInBytes = paramBean.getLong("FILE_SIZE");
        }

        itemParam.set("FILE_NAME", fileName);
        itemParam.set("FILE_PATH", pathExpr);
        itemParam.set("FILE_SIZE", sizeInBytes);
        itemParam.set("FILE_MTYPE", mType);
        itemParam.set("FILE_MEMO", paramBean.getStr("FILE_MEMO"));
        itemParam.set("FILE_SORT", paramBean.getInt("FILE_SORT"));
        itemParam.set("SERV_ID", servId);
        itemParam.set("DATA_ID", paramBean.getStr("DATA_ID"));
        itemParam.set("FILE_CAT", paramBean.getStr("FILE_CAT"));
        itemParam.set("ITEM_CODE", paramBean.getStr("ITEM_CODE"));
        itemParam.set("WF_NI_ID", paramBean.getStr("WF_NI_ID"));
        // itemParam.set("FILE_CHECKSUM", paramBean.getStr("CHECKNUM"));
        return itemParam;
    }

    /**
     * 获取文件路径表达式
     *
     * @param servId
     *            服务ID
     * @param newName
     *            新文件名
     * @return 保存文件相对路径
     */
    public static String buildPathExpr(String servId, String newName) {
        String expresstion = getFilePathExpr(servId);
        if (null == servId || 0 == servId.length()) {
            servId = "UNKNOW";
        }
        String value = ServUtils.replaceSysVars(expresstion);
        // return default path , if replace failed
        value = value.replace("@SERV_ID@", servId);
        // validate format
        if (!value.endsWith("/")) {
            value += "/";
        }
        return value + newName;
    }

    /**
     * 获取迁移文件root路径
     *
     * @return 保存文件ROOT路径
     */
    public static String getTransFilePath() {
        String result = "";
        result = Context.getSyConf(OA_TRAN_FILE_ROOT_PATH, getRootPath());
        if (!result.endsWith("/") && !result.endsWith(File.separator)) {
            result += "/";
        }
        result += DateUtils.getDate() + "/";
        return result;
    }
    /**
     * 获取文件绝对路径
     *
     * @param expresstion
     *            文件路径表达式
     * @return 绝对路径
     */
    public static String getAbsolutePath(String expresstion) {
        // 系统文件root路径
        if (expresstion.startsWith(SY_COMM_FILE_PATH_EXPR)) {
            return expresstion.replace(SY_COMM_FILE_PATH_EXPR, getRootPath());
            // 系统home路径
        } //这个是新添加的自己定义的一个路径
        else if(expresstion.startsWith(HIS_COMM_FILE_PATH_EXPR)){
            //	return expresstion.replace(HIS_COMM_FILE_PATH_EXPR,"D:"+File.separator+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            return expresstion.replace(HIS_COMM_FILE_PATH_EXPR,getTransFilePath());
        }
        else if (expresstion.startsWith(CURRENT_SYSTEM_HOME_PATH_EXPR)) {
            return expresstion.replace(CURRENT_SYSTEM_HOME_PATH_EXPR, Context.app(APP.SYSPATH).toString());
        } else if (expresstion.startsWith(CURRENT_WEBINF_PATH_EXPR)) {
            return expresstion.replace(CURRENT_WEBINF_PATH_EXPR, Context.app(APP.WEBINF).toString());
        } else if (expresstion.startsWith(CURRENT_WEBINF_DOC_PATH_EXPR)) {
            return expresstion.replace(CURRENT_WEBINF_DOC_PATH_EXPR, Context.app(APP.WEBINF_DOC).toString());
        } else if (expresstion.startsWith(CURRENT_WEBINF_DOC_CMPY_PATH_EXPR)) {
            return expresstion.replace(CURRENT_WEBINF_DOC_CMPY_PATH_EXPR, Context.app(APP.WEBINF_DOC_CMPY).toString());
        } else {
            // 系统文件root路径
            return getRootPath() + expresstion;
        }
    }

    /**
     * 获取文件路径规则表达式 TODO 待系统稳定后取消服务路径的判断机制 jerry Li
     *
     * @param servId
     *            服务ID
     * @return 保存文件相对路径
     */
    private static String getFilePathExpr(String servId) {
        if (null == servId || 0 == servId.length()) {
            return DEFAULT_FILE_PATH_EXPR;
        }
        ServDefBean servBean = null;
        try {
            servBean = ServUtils.getServDef(servId);
        } catch (Exception e) {
            log.warn("the service not found, servId:" + servId, e);
        }
        if (null != servBean && servBean.isNotEmpty("SERV_FILE_PATH")) {
            String expr = servBean.getStr("SERV_FILE_PATH");
            if (0 == expr.length()) {
                return DEFAULT_FILE_PATH_EXPR;
            } else {
                return expr;
            }
        } else {
            return DEFAULT_FILE_PATH_EXPR;
        }
    }



    /**
     * 将文件复制到本地路径，便于对文件进行其它处理。如解压、替换文件内容等
     *
     * @param fileId
     *            文件ID
     * @param localPath
     *            本地路径
     */
    public static void copyToLocal(String fileId, String localPath) {
        Bean fileBean = FileMgr.getFile(fileId);
        String filePath = FileMgr.getAbsolutePath(fileBean.getStr("FILE_PATH"));
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = FileStorage.getInputStream(filePath);
            fos = new FileOutputStream(localPath);
            IOUtils.copyLarge(is, fos);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }
        }
    }





}
