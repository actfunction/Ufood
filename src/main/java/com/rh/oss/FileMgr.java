package com.rh.oss;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.FileStorage;
import com.rh.core.comm.file.TempFile;
import com.rh.core.comm.file.TempFile.Storage;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.ImageUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;
import com.rh.core.util.TaskLock;
import com.rh.core.util.file.ImageZoom;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

/**
 * 文件管理(storage + metadata)
 *
 * @author wanglong
 */
public class FileMgr {

    /**
     * log
     */
    private static Log log = LogFactory.getLog(FileMgr.class);
    /**
     * service Id
     */
    public static final String CURRENT_SERVICE = "SY_COMM_FILE";
    /**
     * history file service id
     */
    public static final String HISTFILE_SERVICE = "SY_COMM_FILE_HIS";
    /**
     * oa history file service id
     */
    public static final String OA_HISTFILE_SERVICE = "OA_GW_COMM_FILE_HIS";

    /**
     * history file id prefix
     */
    private static final String HISTFILE_ID_PREFIX = "HIST_";

    /**
     * OA history file id prefix
     */
    private static final String OA_HISTFILE_ID_PREFIX = "OAHIST_";

    /**
     * 默认文件ROOT路径配置Key
     */
    private static final String OA_TRAN_FILE_ROOT_PATH = "OA_TRAN_FILE_ROOT_PATH";
    /**
     * icon image file id prefix
     */
    private static final String IMAGE_ICON_PREFIX = "ICON_";
    /**
     * user icon image file id prefix
     */
    private static final String IMAGE_USER_PREFIX = "USER_";

    private static final String IMAGE_GROUP_PREFIX = "GROUP_";

    private static final String IMAGE_THUMBNAIL = "THUM_";

    /**
     * 默认保存文件路径
     */
    private static final String DEFAULT_FILE_ROOT_PATH = System.getProperty("java.io.tmpdir");

    /**
     * 系统默认文件root路径
     */
    private static final String SY_COMM_FILE_PATH_EXPR = "@SYS_FILE_PATH@";
    /**
     * 将加密的文件复制到指定的路径
     */
    private static final String HIS_COMM_FILE_PATH_EXPR = "@OA_FILE_PATH@";
    /**
     * 当前系统部署路径
     */
    private static final String CURRENT_SYSTEM_HOME_PATH_EXPR = "@" + Context.APP.SYSPATH + "@";
    /**
     * 当前系统部署下WEB-INF路径
     */
    private static final String CURRENT_WEBINF_PATH_EXPR = "@" + Context.APP.WEBINF + "@";
    /**
     * 当前系统部署下WEB-INF/doc路径
     */
    private static final String CURRENT_WEBINF_DOC_PATH_EXPR = "@" + Context.APP.WEBINF_DOC + "@";
    /**
     * 当前系统部署下WEB-INF/doc/cmpy路径
     */
    private static final String CURRENT_WEBINF_DOC_CMPY_PATH_EXPR = "@" + Context.APP.WEBINF_DOC_CMPY + "@";

    /**
     * 默认文件ROOT路径配置Key
     */
    private static final String SY_COMM_FILE_ROOT_PATH = "SY_COMM_FILE_ROOT_PATH";
    /**
     * 默认保存文件路径规则表达式 最后必须以“/”结尾
     */
    private static final String DEFAULT_FILE_PATH_EXPR = SY_COMM_FILE_PATH_EXPR + "/"
            + "@CMPY_CODE@/@SERV_ID@/@DATE_YEAR@/@DATE@/";

    /**
     * can not new instance
     */
    private FileMgr() {
    }

    /**
     * update file content upload file and save old file to history
     *
     * @param fileId file id
     * @param input  - inputstream
     * @return file bean
     */
    public static Bean update(String fileId, InputStream input) {
        return update(fileId, input, "");
    }

    /**
     * update file content upload file and save old file to history
     *
     * @param fileId file id
     * @param input  - inputstream
     * @param mType  - mime type
     * @return file bean
     */
    public static Bean update(String fileId, InputStream input, String mType) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.warn("url decode error", e1);
        }

        // HISTFILE_SERVICE
        Bean result = null;
        // get file info
        Bean fileSrc = getFile(fileId);
        String fileServId = fileSrc.getStr("SERV_ID");

        // save current file
        String uuid = Lang.getUUID();
        String suffix = getSuffix(fileId);
        if (suffix.length() > 0) {
            uuid += "." + suffix;
        }
        String currentPathExpr = buildPathExpr(fileServId, uuid);
        String currentFilePath = getAbsolutePath(currentPathExpr);
        String checksum = "";

        long bytesInSize = -1;
        TempFile tmp = null;
        try {
            long start = System.currentTimeMillis();
            tmp = new TempFile(Storage.SMART, input);
            tmp.read();
            IOUtils.closeQuietly(input);
            log.debug(" read inputstream to temp storage qtime:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();

            // extract file md5 checksum
            InputStream is1 = tmp.openNewInputStream();
            checksum = Lang.getMd5checksum(is1);
            IOUtils.closeQuietly(is1);

            InputStream is2 = tmp.openNewInputStream();
            bytesInSize = FileStorage.saveFile(is2, currentFilePath);
            IOUtils.closeQuietly(is2);
        } catch (NoSuchAlgorithmException ne) {
            log.error(" get file checksum error.", ne);
            throw new RuntimeException("get file checksum error.", ne);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            tmp.destroy();
        }

        // get max version
        // TODO : select max +1
        int histVers = ServDao.count(HISTFILE_SERVICE, new Bean().set("FILE_ID", fileSrc.getId())) + 1;

        // save history file info in database
        Bean histFile = new Bean();
        String surfix = "";
        if (0 < fileSrc.getId().lastIndexOf(".")) {
            surfix = fileSrc.getId().substring(fileSrc.getId().lastIndexOf("."));
        }
        histFile.set("HISTFILE_ID", HISTFILE_ID_PREFIX + uuid + surfix);
        histFile.set("FILE_ID", fileSrc.getId());
        histFile.set("HISTFILE_PATH", fileSrc.getStr("FILE_PATH"));
        histFile.set("HISTFILE_SIZE", fileSrc.getStr("FILE_SIZE"));
        histFile.set("HISTFILE_MTYPE", fileSrc.get("FILE_MTYPE"));
        histFile.set("HISTFILE_VERSION", histVers);
        histFile.set("FILE_CHECKSUM", fileSrc.get("FILE_CHECKSUM"));
        ServDao.save(HISTFILE_SERVICE, histFile);

        // update file path in database
        fileSrc.set("FILE_PATH", currentPathExpr);
        fileSrc.set("FILE_SIZE", bytesInSize);
        fileSrc.set("FILE_MTYPE", mType);
        fileSrc.set("FILE_CHECKSUM", checksum);
        fileSrc.set("FILE_HIST_COUNT", histVers);
        fileSrc.remove("S_MTYPE");
        result = ServDao.update(CURRENT_SERVICE, fileSrc);

        return result;
    }

    /**
     * over Write file content
     *
     * @param fileId       file id
     * @param input        - InputStream
     * @param fileName     - file name
     * @param keepMetaData ture:keep file name and meta data, false: rename and overwrite
     *                     file meta data
     * @return file bean
     */
    public static Bean overWrite(String fileId, InputStream input, String fileName, boolean keepMetaData) {
        return overWrite(fileId, input, fileName, null, keepMetaData);
    }

    /**
     * @param fileId       文件ID
     * @param input        文件流
     * @param fileName     文件名
     * @param disName      显示名
     * @param keepMetaData ture:keep file name and meta data, false: rename and overwrite
     *                     file meta data
     * @return file bean
     */
    public static Bean overWrite(String fileId, InputStream input, String fileName, String disName,
                                 boolean keepMetaData) {
        return overWrite(fileId, input, fileName, null, keepMetaData, new Bean());
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
        result += DateUtils.getDate();
        return result;
    }

    /**
     * @param fileId       文件ID
     * @param input        文件流
     * @param fileName     文件名
     * @param disName      显示名
     * @param keepMetaData ture:keep file name and meta data, false: rename and overwrite
     *                     file meta data
     * @param paramBean    参数Bean
     * @return file bean
     */
    public static Bean overWrite(String fileId, InputStream input, String fileName, String disName,
                                 boolean keepMetaData, Bean paramBean) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.warn("url decode error" + e1);
        }

        // file mimetype
        String mType = getMTypeByName(fileName);

        Bean result = null;
        Bean fileParam = getFile(fileId);
        String pathExpre = fileParam.getStr("FILE_PATH");
        String absolutePath = getAbsolutePath(pathExpre);

        try {
            FileStorage.deleteFile(absolutePath);
        } catch (IOException e) {
            // ingore..
            log.warn(e);
        }
        long byteInSize = -1;
        String checksum = "";

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

            InputStream is2 = tmp.openNewInputStream();
            byteInSize = FileStorage.saveFile(is2, absolutePath);
            IOUtils.closeQuietly(is2);
        } catch (NoSuchAlgorithmException ne) {
            log.error(" get file checksum error.", ne);
            throw new RuntimeException("get file checksum error.", ne);
        } catch (IOException ioe) {
            log.error("save file error.", ioe);
            throw new RuntimeException("save file failed. path:" + absolutePath, ioe);
        } finally {
            tmp.destroy();
        }

        // save file info in database
        fileParam.set("FILE_SIZE", byteInSize);
        // String fileName = FilenameUtils.getName(item.getName());
        if (!keepMetaData) {
            fileName = FilenameUtils.getName(fileName);
            if (null != fileName && 0 < fileName.length()) {
                fileParam.set("FILE_NAME", fileName);
                if (StringUtils.isNotEmpty(disName)) {
                    fileParam.set("DIS_NAME", disName);
                } else {
                    fileParam.set("DIS_NAME", FilenameUtils.getBaseName(fileName));
                }
            }
            if (null != mType && 0 < mType.length()) {
                fileParam.set("FILE_MTYPE", mType);
            } else {
                fileParam.remove("S_MTYPE");
            }

            if (paramBean.containsKey("updateWfNiId") && paramBean.isNotEmpty("WF_NI_ID")) {
                fileParam.set("WF_NI_ID", paramBean.getStr("WF_NI_ID"));
            }
        }
        fileParam.set("FILE_CHECKSUM", checksum);
        result = ServDao.update(CURRENT_SERVICE, fileParam);
        return result;
    }

    /**
     * upload file use inputstream data
     *
     * @param servId   the servId of file
     * @param dataId   the dataId of file ,can be null
     * @param category the file category , can be null
     * @param is       <CODE>InputStream</CODE>
     * @param name     file name
     * @return out Bean
     */
    public static Bean upload(String servId, String dataId, String category, InputStream is, String name) {
        return upload(servId, dataId, "", category, name, is, name, "");
    }

    /**
     * upload file use from data
     *
     * @param servId   the servId of file
     * @param dataId   the dataId of file ,can be null (option)
     * @param fileId   the fileId , can be null (option)
     * @param category the file category , can be null (option)
     * @param name     file name
     * @param is       <CODE>InputStream</CODE>
     * @param disName  display name
     * @param mtype    - file mime type
     * @return out Bean
     */
    public static Bean upload(String servId, String dataId, String fileId, String category, String name, InputStream is,
                              String disName, String mtype) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.warn("decode error", e1);
        }

        // file mimetype
        if (null == mtype || 0 == mtype.length()) {
            mtype = getMTypeByName(name);
        }
        Bean fileParam = createFileBean(servId, dataId, fileId, category, name, mtype, -1, "");
        fileParam.set("DIS_NAME", disName);
        return upload(fileParam, is);
    }

    /**
     * 上传文件
     *
     * @param paramBean - 参数
     * @param input     - inputstream
     * @return 结果
     */
    public static Bean upload(Bean paramBean, InputStream input) {

        Bean result = null;
        // Process a file upload
        Bean fileParam = createFileBean(paramBean);
        String pathExpre = fileParam.getStr("FILE_PATH");
        String absolutePath = getAbsolutePath(pathExpre);
        //set File Name
        String fil = paramBean.getStr("ITEM_CODE");
        if(paramBean.getStr("ITEM_CODE") .equals("ZHENGWEN") ) {
            paramBean.set("Filename","正文.docx");
            paramBean.set("FILE_NAME","正文.docx");
            fileParam.set("FILE_NAME","正文.docx");
        }
        if(paramBean.getStr("ITEM_CODE") .equals("OFD") ) {
            paramBean.set("Filename","红章.ofd");
            paramBean.set("FILE_NAME","红章.ofd");
            fileParam.set("FILE_NAME","红章.ofd");
        }
        //在这里是上传红头文件
        if(paramBean.getStr("FILE_CAT").equals("ZHENGWEN") && paramBean.getStr("ITEM_CODE").equals("WENGAO")){
            //首先像数据库中判断是否有文稿
            List<Bean> fileBean = ServDao.finds(FileMgr.CURRENT_SERVICE, "and DATA_ID = '"+paramBean.getStr("DATA_ID")+"' and SERV_ID='"+paramBean.getStr("SERV_ID")+"'");
            //定义计数器
            int count=0;
            //用来存储主键File_Id
            String zhengWenFileId="";
            //遍历list
            for(Bean bean:fileBean){
                String itemCode = bean.getStr("ITEM_CODE");
                //判断是否有定稿
                if("WENGAO".equals(itemCode))
                {
                    count++;
                }
                if("ZHENGWEN".equals(itemCode)){
                    zhengWenFileId=bean.getStr("FILE_ID");
                }
            }
            //如果没有定稿并且有正文
            if(count==0 && !fileBean.isEmpty()){
                //如果没有文稿则旧版本正文变成文稿，新版本正文覆盖旧版本正文
                Bean oldFileBean = ServDao.find(FileMgr.CURRENT_SERVICE, zhengWenFileId);
                //oldFileBean.setId("").set("FILE_ID", Lang.getUUID()+FileMgr.getSuffix(fileId));
                oldFileBean.set("ITEM_CODE", "WENGAO");
                oldFileBean.set("DIS_NAME", "文稿");
                ServDao.save(FileMgr.CURRENT_SERVICE, oldFileBean);
            }
            paramBean.set("DIS_NAME","正文");
            fileParam.set("DIS_NAME","正文");
            fileParam.set("ITEM_CODE","ZHENGWEN");
        }
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

        // save file info in database
        result = ServDao.create(CURRENT_SERVICE, fileParam);
        return result;
    }

    /**
     * 下载文件 bean中包含outputstream ,使用后需要关闭。
     *
     * @param fileBean fileBean
     * @return InputStream file inputstream
     * @throws IOException file not found
     */
    public static InputStream download(Bean fileBean) throws IOException {
        String relativePath = fileBean.getStr("FILE_PATH");
        // validate
        if (null == relativePath || 0 == relativePath.length()) {
            throw new RuntimeException("FILE_PATH can not be null");
        }
        return downloadFromExpre(relativePath);
    }

    /**
     * 打开文件input stream
     *
     * @param fileBean - 目标文件bean
     * @return inputstream
     * @throws IOException - file not found
     */
    public static InputStream openInputStream(Bean fileBean) throws IOException {
        return download(fileBean);
    }

    /**
     * 打开文件output stream
     *
     * @param fileBean - 目标文件bean
     * @return outputstream
     * @throws IOException - exception
     */
    public static OutputStream openOutputStream(Bean fileBean) throws IOException {
        String pathExpre = fileBean.getStr("FILE_PATH");
        // validate
        if (null == pathExpre || 0 == pathExpre.length()) {
            throw new RuntimeException("FILE_PATH can not be null");
        }
        String absolutePath = getAbsolutePath(pathExpre);
        return FileStorage.getOutputStream(absolutePath);
    }

    /**
     * 获取图片文件对象 TODO 如果该尺寸图片不存在，异步生成
     *
     * @param fileId     - 文件ID
     * @param sizePatten - 文件size表达式 example: 40x40, 100x100，缺省为60x60
     * @return - 文件 Bean
     * @throws FileNotFoundException 文件不存在Exception
     */
    public static Bean getImgFile(String fileId, String sizePatten) throws FileNotFoundException {
        if (-1 < fileId.lastIndexOf(",")) {
            fileId = fileId.substring(0, fileId.lastIndexOf(","));
        }
        Bean file = null;
        boolean isIcon = fileId.startsWith(IMAGE_ICON_PREFIX);
        // 头像图片 为了兼容老版本的头像截取
        if (isIcon) {
            file = getIconFile(fileId);
        } else if (fileId.startsWith(IMAGE_USER_PREFIX)) { // 用户头像
            file = getUserIconFile(fileId, sizePatten);
        } else if (fileId.startsWith(IMAGE_GROUP_PREFIX)) { // 群组头像
            file = getGroupIconFile(fileId, sizePatten);
        } else if (fileId.startsWith(IMAGE_THUMBNAIL)) { // 是否为图片缩略图
            if (null == sizePatten || 0 == sizePatten.length()) {
                sizePatten = "100";
            }
            file = getThumFile(fileId, sizePatten);
            // 获取缩略图后直接返回
            // 因为缩略图已经指定了缩放后的目标尺寸,不需要再次进行resize
        } else {
            file = getFile(fileId);
            // 根据指定尺寸访问图片
            // resize
            if (null != sizePatten && 0 < sizePatten.length()) {
                file = getTargetSizeFile(file, sizePatten);
            }
        }
        return file;
    }

    /**
     * 获取缩略图(按比例进行缩放)
     *
     * @param fileId     - 文件Id
     * @param sizePatten - 缩放后的目标最大尺寸
     * @return 文件bean
     * @throws FileNotFoundException - 如果源文件不存在,我们会抛出该异常
     */
    public static Bean getThumFile(String fileId, String sizePatten) throws FileNotFoundException {
        fileId = fileId.substring(IMAGE_ICON_PREFIX.length());
        Bean file = getFile(fileId);
        if (file == null) {
            throw new FileNotFoundException("fileId:" + fileId);
        }
        return getThumFile(file, sizePatten);
    }

    /**
     * 获取缩略图(按比例进行缩放)
     *
     * @param file       - 文件bean
     * @param sizePatten - 缩放后的目标最大尺寸(比如: 75), 强制指定缩放尺寸(比如:75x75)
     * @return 文件bean
     */
    public static Bean getThumFile(Bean file, String sizePatten) {
        // 获取后缀
        String suffix = getSuffix(file.getId());
        if (null == suffix || 0 == suffix.length()) {
            String mtype = file.getStr("FILE_MTYPE");
            suffix = getSuffixByMtype(mtype);
        }

        // 获取路径
        String targetName = sizePatten + "." + suffix;
        String sourceExpre = file.getStr("FILE_PATH");
        String targetExpre = buildRelatedPathExpress(sourceExpre, targetName);
        String source = getAbsolutePath(sourceExpre);
        String target = getAbsolutePath(targetExpre);

        // 检查图片是否存在
        boolean exits = false;
        try {
            exits = FileStorage.exists(target);
        } catch (IOException e) {
            log.error(e);
        }

        if (!exits) {
            boolean stored = storeThumbnailFile(source, target, sizePatten, suffix);
            if (stored) {
                file.set("FILE_PATH", targetExpre);
            }
        } else {
            file.set("FILE_PATH", targetExpre);
        }

        return file;
    }

    /**
     * 获取用户头像图片
     *
     * @param userCode   - USER_#{userId}.png
     * @param sizePatten - 期望文件尺寸
     * @return bean
     * @throws FileNotFoundException - 文件不存在
     */
    private static Bean getUserIconFile(String userCode, String sizePatten) throws FileNotFoundException {
        // 去掉前缀
        String userId = userCode.replace(IMAGE_USER_PREFIX, "");
        // 去掉后缀
        if (-1 < userId.lastIndexOf(".")) {
            userId = userId.substring(0, userId.lastIndexOf("."));
        }
        UserBean user = UserMgr.getUser(userId);
        String imgId = user.getImg();
        if (null == imgId || 0 == imgId.length()) {
            return null;
        }
        // 去掉前缀
        if (imgId.startsWith("/file/")) {
            imgId = imgId.replace("/file/", "");
        }
        // 去掉后缀
        if (0 < imgId.lastIndexOf("?")) {
            imgId = imgId.substring(0, imgId.lastIndexOf("?"));
        }
        if (null == sizePatten || 0 == sizePatten.length()) {
            sizePatten = "100";
        }
        return getImgFile(imgId, sizePatten);
    }

    /**
     * 获取用户头像图片 core里面没有群组概念, 为了兼容众信的群组头像 TODO :代码重构
     *
     * @param groupCode  - USER_#{userId}.png
     * @param sizePatten - 期望文件尺寸
     * @return bean
     * @throws FileNotFoundException - 文件不存在
     */
    private static Bean getGroupIconFile(String groupCode, String sizePatten) throws FileNotFoundException {
        // 去掉前缀
        String groupId = groupCode.replace(IMAGE_GROUP_PREFIX, "");
        // 去掉后缀
        if (-1 < groupId.lastIndexOf(".")) {
            groupId = groupId.substring(0, groupId.lastIndexOf("."));
        }
        ParamBean paramBean = new ParamBean();
        paramBean.setId(groupId);
        OutBean outBean = ServMgr.act("CC_ORG_GROUP", "byid", paramBean);
        if (null == outBean) {
            return null;
        }
        String imgId = outBean.getStr("GROUP_IMG");
        if (null == imgId || 0 == imgId.length()) {
            return null;
        }
        // 去掉前缀
        if (imgId.startsWith("/file/")) {
            imgId = imgId.replace("/file/", "");
        }
        // 去掉后缀
        if (0 < imgId.lastIndexOf("?")) {
            imgId = imgId.substring(0, imgId.lastIndexOf("?"));
        }
        if (null == sizePatten || 0 == sizePatten.length()) {
            sizePatten = "100";
        }
        return getImgFile(imgId, sizePatten);
    }

    /**
     * 获取头像图片
     *
     * @param fileId - 文件ID
     * @return bean
     * @throws FileNotFoundException - 文件不存在
     */
    private static Bean getIconFile(String fileId) throws FileNotFoundException {
        // 获取源文件ID
        fileId = fileId.substring(IMAGE_ICON_PREFIX.length());
        Bean file = getFile(fileId);
        if (file == null) {
            throw new FileNotFoundException("fileId:" + fileId);
        }
        String sourceExpre = file.getStr("FILE_PATH");
        String source = getAbsolutePath(sourceExpre);
        file = buildIconImgBean(file);
        String targetExpre = file.getStr("FILE_PATH");
        String target = getAbsolutePath(targetExpre);
        // 检查头像图片是否存在
        boolean exits = false;
        try {
            exits = FileStorage.exists(target);
        } catch (IOException e) {
            log.error(e);
        }
        if (!exits) {
            // 如果头像图片不存在，返回原始图片路径
            // 如果头像图片不存在,生成原图80x80的图片作为头像图片
            final String size = Context.getSyConf("SY_ICON_FILE_SIZE", "80x80");
            log.debug("icon_size:" + size);
            storeImgFile(source, target, size);
        }
        return file;
    }

    /**
     * 获取目标尺寸的图片文件, 如果没有,我们进行创建
     *
     * @param file       - file bean
     * @param sizePatten -size example: 100x100
     * @return file bean
     */
    private static Bean getTargetSizeFile(Bean file, String sizePatten) {
        String sourceExpre = file.getStr("FILE_PATH");
        String suffix = getSuffix(sourceExpre);
        String targetName = sizePatten + "." + suffix;
        String targetExpre = buildRelatedPathExpress(sourceExpre, targetName);
        String source = getAbsolutePath(sourceExpre);
        String target = getAbsolutePath(targetExpre);

        // 检查图片是否存在
        boolean exits = false;
        try {
            exits = FileStorage.exists(target);
        } catch (IOException e) {
            log.error(e);
        }
        if (!exits) {
            boolean stored = storeImgFile(source, target, sizePatten);
            if (stored) {
                file.set("FILE_PATH", targetExpre);
            }
        } else {
            file.set("FILE_PATH", targetExpre);
        }
        return file;
    }

    /**
     * 根据源文件，创建头像文件
     *
     * @param fileId - 源文件ID
     * @param x      - x轴坐标
     * @param y      - y轴坐标
     * @param width  - 宽
     * @param height - 高
     */
    public static void createIconImg(String fileId, int x, int y, int width, int height) {
        // 截取文件后缀
        String surfix = "";
        if (0 < fileId.lastIndexOf(".")) {
            surfix = fileId.substring(fileId.lastIndexOf(".") + 1);
        }

        Bean src = FileMgr.getFile(fileId);
        Bean target = buildIconImgBean(src);
        // 删除已存在头像文件
        // String iconPath = getAbsolutePath(target.getStr("FILE_PATH"));
        // try {
        // FileStorage.deleteFile(iconPath);
        // } catch (IOException e) {
        // log.error("delete current icon error:" + iconPath, e);
        // }

        InputStream is = null;
        OutputStream out = null;
        try {
            is = FileMgr.download(src);
            out = FileMgr.openOutputStream(target);
        } catch (IOException e) {
            log.error(e);
        }

        try {
            // 删除相关文件
            String relatedPath = buildRelatedPathExpress(target.getStr("FILE_PATH"), "");
            relatedPath = getAbsolutePath(relatedPath);
            FileStorage.deleteDirectory(relatedPath);

            ImageUtils.cutting(is, out, surfix.toLowerCase(), x, y, width, height);
        } catch (IOException e) {
            log.error("image cutting error, we will delete it:" + target);
            // 删除已创建的图片
            try {
                FileStorage.deleteFile(target.getStr("FILE_PATH"));
            } catch (IOException e1) {
                // ignore
                log.warn(" delete error file:" + target, e1);
            }
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }

    }

    /**
     * 根据原始图片文件创建缩略图
     *
     * @param source     - 原始图片路径(绝对路径)
     * @param target     - 目标缩略图路径(绝对路径)
     * @param sizePatten 缩放尺寸 50:保持比例最大尺寸为50, 50x50:破坏原尺寸强制尺寸为50x50
     * @param suffix     - 文件后缀
     * @return 是否创建成功
     */
    private static boolean storeThumbnailFile(String source, String target, String sizePatten, String suffix) {
        String[] indexArray = sizePatten.split("x");
        if (1 == indexArray.length) {
            int maxThumSize = Integer.valueOf(sizePatten);
            return storeThumbnailFile(source, target, maxThumSize, suffix);
        } else if (2 == indexArray.length) {
            int width = Integer.valueOf(indexArray[0]);
            int height = Integer.valueOf(indexArray[1]);
            return storeThumbnailFile(source, target, width, height, suffix);
        } else {
            log.error("error sizePatten:" + sizePatten);
            return false;
        }
    }

    /**
     * 根据原始图片文件创建缩略图 (保持原比例)
     *
     * @param source      - 原始图片路径(绝对路径)
     * @param target      - 目标缩略图路径(绝对路径)
     * @param maxThumSize 最大缩放尺寸
     * @param suffix      - 文件后缀
     * @return 是否创建成功
     */
    private static boolean storeThumbnailFile(String source, String target, int maxThumSize, String suffix) {
        long start = System.currentTimeMillis();
        if (suffix.startsWith(".")) {
            suffix = suffix.substring(1);
        }
        if (suffix.length() == 0) {
            suffix = "jpg";
        }
        InputStream is = null;
        OutputStream out = null;
        try {
            is = FileStorage.getInputStream(source);
            out = FileStorage.getOutputStream(target);
        } catch (IOException e) {
            log.error(e);
        }

        // 实现一
        // try {
        // BufferedImage originalImage = ImageIO.read(is);
        //
        // // 在不破坏图片比例的情况下, 获得最佳的压缩比例
        // double hScale = (double) originalImage.getHeight() / maxThumSize;
        // double wScale = (double) originalImage.getWidth() / maxThumSize;
        // double maxScale = hScale;
        // if (wScale > hScale) {
        // maxScale = wScale;
        // }
        // if (0 == maxScale) {
        // maxScale = 1;
        // }
        // int width = (int) (originalImage.getWidth() / maxScale);
        // int height = (int) (originalImage.getHeight() / maxScale);
        // BufferedImage buff = Thumbnails.of(originalImage).size(width,
        // height).asBufferedImage();
        // ImageIO.write(buff, suffix, out);
        // } catch (IOException e) {
        // log.error("创建缩略图失败!", e);
        // } finally {
        // IOUtils.closeQuietly(is);
        // IOUtils.closeQuietly(out);
        // }

        // 实现2
        try {
            BufferedImage originalImage = ImageIO.read(is);
            BufferedImage thumbnail = Thumbnails.of(originalImage).size(maxThumSize, maxThumSize).asBufferedImage();
            ImageIO.write(thumbnail, suffix, out);
        } catch (IOException e) {
            log.error("创建缩略图失败!", e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }

        log.debug("create thumbnail file...qtime:" + (System.currentTimeMillis() - start));
        return true;
    }

    /**
     * 根据原始图片文件创建指定尺寸缩略图
     *
     * @param source - 原始图片路径(绝对路径)
     * @param target - 目标缩略图路径(绝对路径)
     * @param width  - 宽
     * @param height - 高
     * @param suffix - 文件后缀
     * @return 是否创建成功
     */
    private static boolean storeThumbnailFile(String source, String target, int width, int height, String suffix) {
        long start = System.currentTimeMillis();
        if (suffix.startsWith(".")) {
            suffix = suffix.substring(1);
        }
        if (suffix.length() == 0) {
            suffix = "jpg";
        }
        InputStream is = null;
        OutputStream out = null;
        try {
            is = FileStorage.getInputStream(source);
            out = FileStorage.getOutputStream(target);
        } catch (IOException e) {
            log.error(e);
        }

        try {
            BufferedImage originalImage = ImageIO.read(is);
            BufferedImage thumbnail = Thumbnails.of(originalImage).size(width, height).crop(Positions.CENTER)
                    .asBufferedImage();
            ImageIO.write(thumbnail, suffix, out);
        } catch (IOException e) {
            log.error("创建缩略图失败!", e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }

        log.debug("create thumbnail file...qtime:" + (System.currentTimeMillis() - start));
        return true;
    }

    /**
     * 创建头像图片Bean
     *
     * @param source - 源文件bean
     * @return 头像图片bean
     */
    private static Bean buildIconImgBean(Bean source) {
        return buildImgBeanByPrefix(source, IMAGE_ICON_PREFIX);
    }

    /**
     * 创建图片缩略图Bean
     *
     * @param source
     *            - 源文件bean
     * @return 缩略图bean
     */
    // private static Bean buildThumbnailImgBean(Bean source) {
    // return buildImgBeanByPrefix(source, IMAGE_THUMBNAIL);
    // }

    /**
     * 根据前缀创建图片 bean
     *
     * @param source - 原始文件 bean
     * @param prefix - 文件前缀
     * @return - 图片 bean
     */
    private static Bean buildImgBeanByPrefix(Bean source, String prefix) {
        String surfix = "";
        String fileId = source.getId();
        if (0 < fileId.lastIndexOf(".")) {
            surfix = fileId.substring(fileId.lastIndexOf(".") + 1);
        }
        String targetId = prefix + "." + surfix;
        Bean result = source.copyOf();
        result.set("FILE_ID", targetId);
        result.setId(targetId);
        String sourceExpre = source.getStr("FILE_PATH");
        String targetExpre = buildRelatedPathExpress(sourceExpre, targetId);
        result.set("FILE_PATH", targetExpre);
        return result;
    }

    /**
     * @param servSrcId 数据对应父服务ID
     * @param dataId    数据ID
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
     * get file info
     *
     * @param fileId file id
     * @return <CODE>Bean</CODE>
     */
    public static Bean getFile(String fileId) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.warn("url decode error" + e1);
        }
        if (fileId == null || fileId.length() == 0) {
            throw new TipException(Context.getSyMsg("SY_DOWNLOAD_FILE_NOT_FOUND") + ",file id:" + fileId);
        }
        if (fileId.startsWith("/file/")) { // 支持URL类型数据获取
            fileId = fileId.substring(6);
        }

        // if is history file
        Bean histFile = null;
        if (fileId.startsWith(HISTFILE_ID_PREFIX)) {
            histFile = ServDao.find(HISTFILE_SERVICE, fileId);
            fileId = histFile.getStr("FILE_ID");
        } else if (fileId.startsWith(OA_HISTFILE_ID_PREFIX)) {
            Bean result = ServDao.find(OA_HISTFILE_SERVICE, fileId);
            return result;
        }

        Bean result = ServDao.find(CURRENT_SERVICE, fileId);

        // if (null == result) {
        // throw new
        // TipException(Context.getSyMsg("SY_DOWNLOAD_FILE_NOT_FOUND"));
        // }

        // replace file bean
        if (null != result && null != histFile) {
            result.set("FILE_ID", histFile.getId());
            result.set("FILE_PATH", histFile.getStr("HISTFILE_PATH"));
            result.set("FILE_SIZE", histFile.getStr("HISTFILE_SIZE"));
            result.set("FILE_MTYPE", histFile.getStr("HISTFILE_MTYPE"));
            result.set("HISTFILE_VERSION", histFile.getStr("HISTFILE_VERSION"));
            result.set("S_USER", histFile.getStr("S_USER"));
            result.set("S_UNAME", histFile.getStr("S_UNAME"));
            result.set("S_DEPT", histFile.getStr("S_DEPT"));
            result.set("S_CMPY", histFile.getStr("S_CMPY"));
            result.set("S_DNAME", histFile.getStr("S_DNAME"));
            result.set("S_MTIME", histFile.getStr("S_MTIME"));
            result.set("SRC_FILE", histFile.getStr("FILE_ID"));
        }

        return result;
    }

    /**
     * 创建一个新文件，新文件为指定文件的链接文件。新文件没有实际的物理文件，实际物理文件路径为老文件的地址，达到节省磁盘空间的目的。
     *
     * @param fileBean 指定文件
     * @param param    新文件的参数Bean。
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
     * 复制指定的文件到当前路径
     *
     * @param fileBean 文件
     * @param param    参数
     * @return 返回
     */
    public static Bean copyFile(Bean fileBean, Bean param) {

        // 生成新文件UUID
        String fileUUID = Lang.getUUID();

        String pathExpr = buildPathExpr(param.getStr("SERV_ID"), fileUUID);

        String absolutePath = getAbsolutePath(pathExpr);

        // 复制文件
        // File file = new File(getAbsolutePath(fileBean.getStr("FILE_PATH")));
        try {
            InputStream is = download(fileBean);
            FileStorage.saveFile(is, absolutePath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // 构造新的File Bean
        Bean newFileBean = fileBean.copyOf();

        // 获取文件后缀名
        String fileName = newFileBean.getStr("FILE_NAME");
        String surfix = "";
        if (0 < fileName.lastIndexOf(".")) {
            surfix = fileName.substring(fileName.lastIndexOf("."));
        }

        // 设置新的文件ID和新的文件路径
        newFileBean.set("FILE_ID", fileUUID + surfix);
        newFileBean.set("FILE_PATH", pathExpr);

        // 合并属性值
        extendBean(newFileBean, param);

        return ServDao.create("SY_COMM_FILE", newFileBean.remove("S_MTIME"));
    }

    /**
     * 把参数Bean中的Value覆盖到newFileBean中
     *
     * @param newFileBean 文件Bean
     * @param param       参数Bean
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
     * @param file <CODE>Bean</CODE>
     */
    public static void updateFile(Bean file) {
        ServDao.update(CURRENT_SERVICE, file);
    }

    /**
     * delete file from file system
     *
     * @param files files
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
     * delete file by id
     *
     * @param fileId - file id
     * @return - deleted?
     */
    public static boolean deleteFile(String fileId) {
        Bean file = getFile(fileId);
        return deleteFile(file);
    }

    /**
     * @param file 被删除文件Bean
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
     * @param file - file bean
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
     * @param suffix file name suffix
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
     * @param name file name
     * @return mtype
     */
    public static String getMTypeByName(String name) {
        String suffix = getSuffix(name);
        return getMTypeBySuffix(suffix);

    }

    /**
     * 生成相关文件的路径表达式
     *
     * @param fileExpre   - 源文件路径表达式
     * @param relatedFile - 相关文件名
     * @return relatedPathExpress
     */
    private static String buildRelatedPathExpress(String fileExpre, String relatedFile) {
        String targetExpre = fileExpre + "_file/" + relatedFile;
        return targetExpre;
    }

    /**
     * 对文件图片进行存储
     *
     * @param source     - 原文件路径
     * @param target     - 生成文件路径
     * @param sizePatten - 尺寸信息 example: 80x80
     * @return 是否存储成功
     */
    private static boolean storeImgFile(String source, String target, String sizePatten) {
        boolean result = false;
        String[] indexArray = sizePatten.split("x");
        if (2 != indexArray.length) {
            log.warn(" invalid image size patten:" + sizePatten);
            return result;
        }
        boolean locked = false;
        TaskLock lock = null;
        InputStream is = null;
        OutputStream out = null;
        // 创建图片
        try {
            lock = new TaskLock("ImageZoom", FilenameUtils.getName(source));
            locked = lock.lock();
            if (locked) {
                int width = Integer.valueOf(indexArray[0]);
                int height = Integer.valueOf(indexArray[1]);
                is = FileStorage.getInputStream(source);
                out = FileStorage.getOutputStream(target);

                ImageZoom imgZoom = new ImageZoom(width, height);
                imgZoom.setQuality(0.8f);
                imgZoom.resize(is, out);
                result = true;
            }
        } catch (Exception e) {
            log.error("image resize error, we will delete it:" + target);
            // 删除已创建的图片
            try {
                FileStorage.deleteFile(target);
            } catch (IOException e1) {
                // ignore
                log.warn(" delete error file:" + target, e1);
            }
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
            if (locked) {
                lock.release();
            }
        }
        return result;
    }

    /**
     * 截取文件后缀
     *
     * @param fileId - file id
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
     * create file bean
     *
     * @param servId      service id
     * @param dataId      data id
     * @param fileId      file id (option)
     * @param category    file category (option)
     * @param fileName    fileName (option)
     * @param mType       file mime type
     * @param sizeInBytes file size
     * @param checksum    - file checksum
     * @return Bean
     */
    private static Bean createFileBean(String servId, String dataId, String fileId, String category, String fileName,
                                       String mType, long sizeInBytes, String checksum) {
        Bean itemParam = new Bean();
        String uuid = Lang.getUUID();
        String surfix = "";
        if (0 < fileName.lastIndexOf(".")) {
            surfix = fileName.substring(fileName.lastIndexOf("."));
        }
        String pathExpr = buildPathExpr(servId, uuid + surfix);
        // String absolutePath = getAbsolutePath(relativePath);
        if (null == fileId || 0 == fileId.length()) {
            itemParam.set("FILE_ID", uuid + surfix);
        } else {
            itemParam.set("FILE_ID", fileId);
        }

        if (null == mType || mType.length() == 0) {
            mType = getMTypeByName(fileName);
        }

        itemParam.set("FILE_SIZE", sizeInBytes);
        itemParam.set("FILE_PATH", pathExpr);
        itemParam.set("FILE_NAME", fileName);
        itemParam.set("FILE_MTYPE", mType);
        itemParam.set("SERV_ID", servId);
        itemParam.set("DATA_ID", dataId);
        itemParam.set("FILE_CAT", category);
        itemParam.set("FILE_CHECKSUM", checksum);
        return itemParam;
    }

    /**
     * 通过 mtype 返回后缀
     *
     * @param mtype - mtype
     * @return 后缀
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
     * @param paramBean 参数
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
     * @param servId  服务ID
     * @param newName 新文件名
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
     * 获取文件绝对路径
     *
     * @param expresstion 文件路径表达式
     * @return 绝对路径
     */
    public static String getAbsolutePath(String expresstion) {
        // 系统文件root路径
        if (expresstion.startsWith(SY_COMM_FILE_PATH_EXPR)) {
            return expresstion.replace(SY_COMM_FILE_PATH_EXPR, getRootPath());
            // 系统home路径
        } //这个是新添加的自己定义的一个路径
        else if (expresstion.startsWith(HIS_COMM_FILE_PATH_EXPR)) {
            //	return expresstion.replace(HIS_COMM_FILE_PATH_EXPR,"D:"+File.separator+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            return expresstion.replace(HIS_COMM_FILE_PATH_EXPR, getTransFilePath());
        } else if (expresstion.startsWith(CURRENT_SYSTEM_HOME_PATH_EXPR)) {
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
     * @param servId 服务ID
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
     * 下载文件
     *
     * @param pathExpre 文件路径表达式
     * @return InputStream file inputstream
     * @throws IOException file not found
     */
    private static InputStream downloadFromExpre(String pathExpre) throws IOException {
        String absolutePath = getAbsolutePath(pathExpre);
        return FileStorage.getInputStream(absolutePath);
    }

    /**
     * @param fileBean 文件Bean
     * @return 文件显示名
     */
    public static String getFileDisName(Bean fileBean) {
        if (fileBean.isEmpty("DIS_NAME")) {
            return fileBean.getStr("FILE_NAME");
        }

        String disName = Strings.escapeFilenameSepcChar(fileBean.getStr("DIS_NAME"));

        String fileExt = FilenameUtils.getExtension(fileBean.getStr("FILE_NAME"));

        if (fileExt == null || fileExt.length() <= 0) {
            fileExt = FilenameUtils.getExtension(fileBean.getStr("FILE_ID"));
        }

        return disName + "." + fileExt;
    }

    /**
     * 将文件复制到本地路径，便于对文件进行其它处理。如解压、替换文件内容等
     *
     * @param fileId    文件ID
     * @param localPath 本地路径
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

    /**
     * 根据图片文件ID获取对应Base64编码，采用缺省尺寸为60x60
     *
     * @param img 图片地址信息
     * @return 图片文件Base64编码
     */
    public static String getBase64IconByImg(String img) {
        return getBase64IconByImg(img, "60x60");
    }

    /**
     * 根据图片文件ID获取对应Base64编码 支持缩略图(THUM_${imgFileId}),图标(ICON_${imgFileId})等
     *
     * @param fileId 图片ID
     * @param size   尺寸
     * @return 图片文件Base64编码
     */
    public static String getBase64ByImg(String fileId, String size) {
        // 提取文件ID
        int pos = fileId.indexOf(",");
        if (pos > 0) {
            fileId = fileId.substring(0, pos);
        }

        String result = "";
        InputStream in = null;
        Base64InputStream is = null;
        try {
            Bean file = getImgFile(fileId, size);
            in = download(file);
            if (in != null) {
                is = new Base64InputStream(in, true, -1, null);
                result = IOUtils.toString(is);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(is);
        }

        return result;
    }

    /**
     * 根据图片文件ID获取对应Base64编码
     *
     * @param img  图片地址信息
     * @param size 尺寸
     * @return 图片文件Base64编码
     */
    public static String getBase64IconByImg(String img, String size) {
        String result = "";
        InputStream in = null;
        Base64InputStream is = null;
        try {
            in = getIconInputStreamByImg(img, size);
            if (in != null) {
                is = new Base64InputStream(in, true, -1, null);
                // result = new String(Base64.encode(IOUtils.toByteArray(in)),
                // Constant.ENCODING);
                result = IOUtils.toString(is);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(is);
        }

        return result;
    }

    /**
     * 根据图片文件获取对应Base64编码
     *
     * @param img  图片地址信息
     * @param size 尺寸
     * @return 图片文件的输入流
     */
    public static InputStream getIconInputStreamByImg(String img, String size) {
        InputStream in = null;
        try {
            Bean fileBean = getIconFileBeanByImg(img, size);
            if (fileBean != null) {
                in = FileMgr.download(fileBean);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return in;
    }

    /**
     * 根据图片文件获取对应Base64编码
     *
     * @param img  图片地址信息。格式为图片文件ID，或者“图片文件ID” + “，” + “文件名”
     * @param size 尺寸
     * @return 图片文件的Bean对象
     * @throws IOException IO异常
     */
    public static Bean getIconFileBeanByImg(String img, String size) throws IOException {
        if (img.isEmpty()) {
            return null;
        }
        int pos = img.indexOf(",");
        if (pos > 0) {
            img = img.substring(0, pos);
        }
        img = "ICON_" + img;
        return FileMgr.getImgFile(img, size);
    }

    // public static void main (String [] args){
    // int height = 99;
    // int width = 449;
    // int defaultSize = 100;
    // int hScale = height / defaultSize;
    // int wScale = width / defaultSize;
    // int minScale = hScale;
    // if (wScale < hScale) {
    // minScale = wScale;
    // }
    // if (0 == minScale) {
    // minScale = 1;
    // }
    //
    // System.out.println("w:" + width/minScale + " h:" + height/minScale );
    // }

    /**
     * 文件复制
     *
     * @param oldPath
     * @param newPath
     */
    public static void copyFile(String oldPath, String newPath) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            // 旧文件
            File oldFile = new File(oldPath);
            // 新文件
            File newFile = new File(newPath);
            // 如果没有父目录则创建
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }

            // 创建输入 输出流
            in = new FileInputStream(oldFile);
            out = new FileOutputStream(newFile);
            // 定义每次读取的大小
            byte[] buffer = new byte[1024 * 8];
            int readByte = 0;
            while ((readByte = in.read(buffer)) != -1) {
                out.write(buffer, 0, readByte);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("121212");
            System.out.println(
                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + "在这个时间这个文件" + oldPath + "没有复制成功");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void hongTouFileSave(String fileId) {
        Bean fileBean = ServDao.find("SY_COMM_FILE", fileId);
        String niid = fileBean.getStr("WF_NI_ID");
        //在这里 讲历史 版本 插入到数据库中
        List<Bean> hisList = ServDao.finds("OA_GW_COMM_FILE_HIS", "AND SERV_ID ='" + fileBean.getStr("SERV_ID") + "'AND DATA_ID='" + fileBean.getStr("DATA_ID") + "'");
		//定义计数器用来判断是否是是否要覆盖文件
		int count=0;
		for(Bean hisBean:hisList){
			//如果有这个文件则替换
			if("REDHEAD".equals(hisBean.getStr("HISTFILE_QINGGAO_TYPE"))&&niid.equals(hisBean.getStr("WF_NI_ID"))){
				// 如果有相同的值则 size的Num - 1;  
				//复制文件
				String path=fileBean.getStr("FILE_PATH");
				//将文件路径转换成为绝对路径
				String zhengWenFilePath = FileMgr.getAbsolutePath(path);
				//将这个文件复制到一个指定的新路径下
				String newHisFilePath="";
				//将文件名进行替换  成hisFileId
				String[] filePaths = zhengWenFilePath.split("/");
				for(int i=0;i<filePaths.length;i++){
					if(i==filePaths.length-1){
						newHisFilePath+=hisBean.get("HISFILE_ID");
					}else{
						newHisFilePath+=filePaths[i]+"/";
					}
				}
				FileMgr.copyFile(zhengWenFilePath, newHisFilePath);
				int  histVers = ServDao.count("OA_GW_COMM_FILE_HIS", new Bean().set("SERV_ID", fileBean.getStr("SERV_ID")).set("DATA_ID", fileBean.getStr("DATA_ID"))) + 1;//
			    hisBean.set("HISFILE_VERSION", histVers);
				hisBean.set("FILE_SIZE", fileBean.getStr("FILE_SIZE"));      
				hisBean.set("DIS_NAME", fileBean.getStr("DIS_NAME"));   
				hisBean.set("FILE_MTYPE", fileBean.getStr("FILE_MTYPE"));   
				hisBean.set("FILE_NAME", fileBean.getStr("FILE_NAME"));   
				hisBean.set("FILE_CAT", fileBean.getStr("FILE_CAT"));   
				hisBean.set("DATA_TYPE", fileBean.getStr("DATA_TYPE"));   
				hisBean.set("ITEM_CODE", fileBean.getStr("ITEM_CODE"));   
				hisBean.set("FILE_CHECKSUM", fileBean.getStr("FILE_CHECKSUM"));   
				hisBean.set("FILE_HIST_COUNT", fileBean.getInt("FILE_HIST_COUNT")+1);   
				  //获得当前节点 讲节点放入到数据中
				hisBean.set("WF_NI_ID", niid);
				hisBean.set("S_MTIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
				//在这里进行向数据库经插入清稿文档
				hisBean.set("HISTFILE_QINGGAO_TYPE", "REDHEAD");
				ServDao.save("OA_GW_COMM_FILE_HIS", hisBean);
				count++;
				log.debug("覆盖了一个清稿的历史版本，服务编码为：" + hisBean.getStr("SERV_ID") + ",数据主键为：" + hisBean.getStr("DATA_ID"));
		}
		}
		//本节点中第一个清稿文件
		if(count==0){
			// 如果没有相同的值   则创建这个清稿文件
		    fileBean.setId("");
			//获得版本记录   将版本记录+1 返回 版本号
		    int histVers =0;
		   /* if(0==hisList.size()){
		     histVers = 1;
		    } 
		    else{*/
			 histVers = ServDao.count("OA_GW_COMM_FILE_HIS", new Bean().set("SERV_ID", fileBean.getStr("SERV_ID")).set("DATA_ID", fileBean.getStr("DATA_ID"))) + 1;//
		    //}
			 fileBean.set("HISFILE_VERSION", histVers);
			//历史文件的id
			String hisFileId="OAHIST_" + Lang.getUUID()+"."+FileMgr.getSuffix(fileBean.getStr("FILE_ID"));
			fileBean.set("HISFILE_ID", hisFileId);
			//在这里获得数据库中存储文件的路径
			String path=fileBean.getStr("FILE_PATH");
			//将文件路径转换成为绝对路径
			String zhengWenFilePath = FileMgr.getAbsolutePath(path);
			//将这个文件复制到一个指定的新路径下
			String newHisFilePath="";
			//将文件名进行替换  成hisFileId
			String[] filePaths = zhengWenFilePath.split("/");
			for(int i=0;i<filePaths.length;i++){
				if(i==filePaths.length-1){
					newHisFilePath+=hisFileId;
				}else{
					newHisFilePath+=filePaths[i]+"/";
				}
			}
			FileMgr.copyFile(zhengWenFilePath, newHisFilePath);
			//将新路径转换成数据库中存储的路径
			 newHisFilePath="";
			String[] sqlPath = path.split("/");
			for(int i=0;i<sqlPath.length;i++){
				if(i==sqlPath.length-1){
					newHisFilePath+=hisFileId;
				}else{
					newHisFilePath+=sqlPath[i]+"/";
				}
			}
		    //将路径上传到数据库
			fileBean.set("FILE_PATH", newHisFilePath);
			//在这里进行向数据库经插入清稿文档
			fileBean.set("HISTFILE_QINGGAO_TYPE", "REDHEAD");
			  //获得当前节点 讲节点放入到数据中
			fileBean.set("WF_NI_ID", niid);
			fileBean.set("S_MTIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
			ServDao.save("OA_GW_COMM_FILE_HIS", fileBean);
			log.debug("新增一个正文的历史版本，服务编码为：" + fileBean.getStr("SERV_ID") + ",数据主键为：" + fileBean.getStr("DATA_ID"));
		}
    }
     	/**
	 * 将一个流写到指定的文件中
	 * @param is
	 * @param 绝对路径
	 */
	public static void inputStreamToFile(InputStream is, String newPath) {
		FileOutputStream out = null;
		try {
			// 新文件
			File newFile = new File(newPath);
			// 如果没有父目录则创建
			if (!newFile.getParentFile().exists()) {
				newFile.getParentFile().mkdirs();
			}

			// 创建输入 输出流
			out = new FileOutputStream(newFile);
			// 定义每次读取的大小
			byte[] buffer = new byte[1024 * 8];
			int readByte = 0;
			while ((readByte = is.read(buffer)) != -1) {
				out.write(buffer, 0, readByte);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(
					new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + "在这个时间这个文件" + newPath + "没有复制成功");
		}
	}
}
