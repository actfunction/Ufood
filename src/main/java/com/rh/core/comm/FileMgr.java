//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.rh.core.comm;

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
import com.rh.core.util.DateUtils;
import com.rh.core.util.ImageUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;
import com.rh.core.util.TaskLock;
import com.rh.core.util.file.ImageZoom;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

public class FileMgr {
    private static Log log = LogFactory.getLog(FileMgr.class);
    public static final String CURRENT_SERVICE = "SY_COMM_FILE";
    public static final String HISTFILE_SERVICE = "SY_COMM_FILE_HIS";
    public static final String OA_HISTFILE_SERVICE = "OA_GW_COMM_FILE_HIS";
    private static final String HISTFILE_ID_PREFIX = "HIST_";
    private static final String OA_HISTFILE_ID_PREFIX = "OAHIST_";
    private static final String OA_TRAN_FILE_ROOT_PATH = "OA_TRAN_FILE_ROOT_PATH";
    private static final String IMAGE_ICON_PREFIX = "ICON_";
    private static final String IMAGE_USER_PREFIX = "USER_";
    private static final String IMAGE_GROUP_PREFIX = "GROUP_";
    private static final String IMAGE_THUMBNAIL = "THUM_";
    private static final String DEFAULT_FILE_ROOT_PATH = System.getProperty("java.io.tmpdir");
    private static final String SY_COMM_FILE_PATH_EXPR = "@SYS_FILE_PATH@";
    private static final String HIS_COMM_FILE_PATH_EXPR = "@OA_FILE_PATH@";
    private static final String CURRENT_SYSTEM_HOME_PATH_EXPR;
    private static final String CURRENT_WEBINF_PATH_EXPR;
    private static final String CURRENT_WEBINF_DOC_PATH_EXPR;
    private static final String CURRENT_WEBINF_DOC_CMPY_PATH_EXPR;
    private static final String SY_COMM_FILE_ROOT_PATH = "SY_COMM_FILE_ROOT_PATH";
    private static final String DEFAULT_FILE_PATH_EXPR = "@SYS_FILE_PATH@/@CMPY_CODE@/@SERV_ID@/@DATE_YEAR@/@DATE@/";

    static {
        CURRENT_SYSTEM_HOME_PATH_EXPR = "@" + APP.SYSPATH + "@";
        CURRENT_WEBINF_PATH_EXPR = "@" + APP.WEBINF + "@";
        CURRENT_WEBINF_DOC_PATH_EXPR = "@" + APP.WEBINF_DOC + "@";
        CURRENT_WEBINF_DOC_CMPY_PATH_EXPR = "@" + APP.WEBINF_DOC_CMPY + "@";
    }

    private FileMgr() {
    }

    public static Bean update(String fileId, InputStream input) {
        return update(fileId, input, "");
    }

    public static Bean update(String fileId, InputStream input, String mType) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException var26) {
            log.warn("url decode error", var26);
        }

        Bean result = null;
        Bean fileSrc = getFile(fileId);
        String fileServId = fileSrc.getStr("SERV_ID");
        String uuid = Lang.getUUID();
        String suffix = getSuffix(fileId);
        if (suffix.length() > 0) {
            uuid = uuid + "." + suffix;
        }

        String currentPathExpr = buildPathExpr(fileServId, uuid);
        String currentFilePath = getAbsolutePath(currentPathExpr);
        String checksum = "";
        long bytesInSize = -1L;
        TempFile tmp = null;

        try {
            long start = System.currentTimeMillis();
            tmp = new TempFile(Storage.SMART, input);
            tmp.read();
            IOUtils.closeQuietly(input);
            log.debug(" read inputstream to temp storage qtime:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            InputStream is1 = tmp.openNewInputStream();
            checksum = Lang.getMd5checksum(is1);
            IOUtils.closeQuietly(is1);
            InputStream is2 = tmp.openNewInputStream();
            bytesInSize = FileStorage.saveFile(is2, currentFilePath);
            IOUtils.closeQuietly(is2);
        } catch (NoSuchAlgorithmException var23) {
            log.error(" get file checksum error.", var23);
            throw new RuntimeException("get file checksum error.", var23);
        } catch (IOException var24) {
            throw new RuntimeException(var24);
        } finally {
            tmp.destroy();
        }

        int histVers = ServDao.count("SY_COMM_FILE_HIS", (new Bean()).set("FILE_ID", fileSrc.getId())) + 1;
        Bean histFile = new Bean();
        String surfix = "";
        if (fileSrc.getId().lastIndexOf(".") > 0) {
            surfix = fileSrc.getId().substring(fileSrc.getId().lastIndexOf("."));
        }

        histFile.set("HISTFILE_ID", "HIST_" + uuid + surfix);
        histFile.set("FILE_ID", fileSrc.getId());
        histFile.set("HISTFILE_PATH", fileSrc.getStr("FILE_PATH"));
        histFile.set("HISTFILE_SIZE", fileSrc.getStr("FILE_SIZE"));
        histFile.set("HISTFILE_MTYPE", fileSrc.get("FILE_MTYPE"));
        histFile.set("HISTFILE_VERSION", histVers);
        histFile.set("FILE_CHECKSUM", fileSrc.get("FILE_CHECKSUM"));
        ServDao.save("SY_COMM_FILE_HIS", histFile);
        fileSrc.set("FILE_PATH", currentPathExpr);
        fileSrc.set("FILE_SIZE", bytesInSize);
        fileSrc.set("FILE_MTYPE", mType);
        fileSrc.set("FILE_CHECKSUM", checksum);
        fileSrc.set("FILE_HIST_COUNT", histVers);
        fileSrc.remove("S_MTYPE");
        result = ServDao.update("SY_COMM_FILE", fileSrc);
        return result;
    }

    public static Bean overWrite(String fileId, InputStream input, String fileName, boolean keepMetaData) {
        return overWrite(fileId, input, fileName, (String)null, keepMetaData);
    }

    public static Bean overWrite(String fileId, InputStream input, String fileName, String disName, boolean keepMetaData) {
        return overWrite(fileId, input, fileName, (String)null, keepMetaData, new Bean());
    }

    public static String getTransFilePath() {
        String result = "";
        result = Context.getSyConf("OA_TRAN_FILE_ROOT_PATH", getRootPath());
        if (!result.endsWith("/") && !result.endsWith(File.separator)) {
            result = result + "/";
        }

        result = result + DateUtils.getDate();
        return result;
    }

    public static Bean overWrite(String fileId, InputStream input, String fileName, String disName, boolean keepMetaData, Bean paramBean) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException var29) {
            log.warn("url decode error" + var29);
        }

        String mType = getMTypeByName(fileName);
        Bean result = null;
        Bean fileParam = getFile(fileId);
        String pathExpre = fileParam.getStr("FILE_PATH");
        String absolutePath = getAbsolutePath(pathExpre);

        try {
            FileStorage.deleteFile(absolutePath);
        } catch (IOException var28) {
            log.warn(var28);
        }

        long byteInSize = -1L;
        String checksum = "";
        TempFile tmp = null;

        try {
            long start = System.currentTimeMillis();
            tmp = new TempFile(Storage.SMART, input);
            tmp.read();
            IOUtils.closeQuietly(input);
            log.debug(" read inputstream to temp storage qtime:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            InputStream is1 = tmp.openNewInputStream();
            checksum = Lang.getMd5checksum(is1);
            IOUtils.closeQuietly(is1);
            InputStream is2 = tmp.openNewInputStream();
            byteInSize = FileStorage.saveFile(is2, absolutePath);
            IOUtils.closeQuietly(is2);
        } catch (NoSuchAlgorithmException var25) {
            log.error(" get file checksum error.", var25);
            throw new RuntimeException("get file checksum error.", var25);
        } catch (IOException var26) {
            log.error("save file error.", var26);
            throw new RuntimeException("save file failed. path:" + absolutePath, var26);
        } finally {
            tmp.destroy();
        }

        fileParam.set("FILE_SIZE", byteInSize);
        if (!keepMetaData) {
            fileName = FilenameUtils.getName(fileName);
            if (fileName != null && fileName.length() > 0) {
                fileParam.set("FILE_NAME", fileName);
                if (StringUtils.isNotEmpty(disName)) {
                    fileParam.set("DIS_NAME", disName);
                } else {
                    fileParam.set("DIS_NAME", FilenameUtils.getBaseName(fileName));
                }
            }

            if (mType != null && mType.length() > 0) {
                fileParam.set("FILE_MTYPE", mType);
            } else {
                fileParam.remove("S_MTYPE");
            }

            if (paramBean.containsKey("updateWfNiId") && paramBean.isNotEmpty("WF_NI_ID")) {
                fileParam.set("WF_NI_ID", paramBean.getStr("WF_NI_ID"));
            }
        }

        fileParam.set("FILE_CHECKSUM", checksum);
        result = ServDao.update("SY_COMM_FILE", fileParam);
        return result;
    }

    public static Bean upload(String servId, String dataId, String category, InputStream is, String name) {
        return upload(servId, dataId, "", category, name, is, name, "");
    }

    public static Bean upload(String servId, String dataId, String fileId, String category, String name, InputStream is, String disName, String mtype) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException var9) {
            log.warn("decode error", var9);
        }

        if (mtype == null || mtype.length() == 0) {
            mtype = getMTypeByName(name);
        }

        Bean fileParam = createFileBean(servId, dataId, fileId, category, name, mtype, -1L, "");
        fileParam.set("DIS_NAME", disName);
        return upload(fileParam, is);
    }

    public static Bean upload(Bean paramBean, InputStream input) {
        Bean result = null;
        if (!"OA_GW_GONGWEN_ICBCSW".equals(paramBean.getStr("SERV_ID")) && !"OA_GW_GONGWEN_TPBSW".equals(paramBean.getStr("SERV_ID"))) {
            if (paramBean.getStr("ITEM_CODE").equals("ZHENGWEN")) {
                paramBean.set("DIS_NAME", "正文");
                paramBean.set("FILE_NAME", "正文.docx");
            }

            if (paramBean.getStr("ITEM_CODE").equals("OFD") && paramBean.getStr("UP_OFD").equals("UPLODER")) {
                paramBean.set("DIS_NAME", "红章正文");
                paramBean.set("FILE_NAME", "红章正文.ofd");
            }

            if (paramBean.getStr("RED_FILE").equals("RED")) {
                paramBean.set("DIS_NAME", "定稿");
                paramBean.set("FILE_NAME", "定稿.docx");
            }
        }
        Bean fileParam = createFileBean(paramBean);
        String pathExpre = fileParam.getStr("FILE_PATH");
        String absolutePath = getAbsolutePath(pathExpre);


        String fileName;
//        if ("ZHENGWEN".equals(paramBean.getStr("FILE_CAT")) && "RED".equals(paramBean.getStr("RED_FILE"))) {
//            List<Bean> fileBean = ServDao.finds("SY_COMM_FILE", "and DATA_ID = '" + paramBean.getStr("DATA_ID") + "' and SERV_ID='" + paramBean.getStr("SERV_ID") + "'");
//            int count = 0;
//            fileName = "";
//            Iterator var10 = fileBean.iterator();
//
//            Bean oldFileBean;
//            String itemCode;
//            while(var10.hasNext()) {
//                oldFileBean = (Bean)var10.next();
//                itemCode = oldFileBean.getStr("ITEM_CODE");
//                if ("WENGAO".equals(itemCode)) {
//                    ++count;
//                }
//
//                if ("ZHENGWEN".equals(itemCode)) {
//                    fileName = oldFileBean.getStr("FILE_ID");
//                }
//            }
//
//            if (count == 0 && !fileBean.isEmpty()) {
//                oldFileBean = ServDao.find("SY_COMM_FILE", fileName);
//                oldFileBean.set("ITEM_CODE", "WENGAO");
//                oldFileBean.set("DIS_NAME", "定稿");
//                ServDao.save("SY_COMM_FILE", oldFileBean);
//                oldFileBean.set("HISTFILE_QINGGAO_TYPE", "WENGAO");
//                String hisFileId = "OAHIST_" + Lang.getUUID() + "." + getSuffix(oldFileBean.getStr("FILE_ID"));
//                oldFileBean.setId("").set("HISFILE_ID", hisFileId);
//                itemCode = Context.getSyConf("HIS_FILE_SAVE_TYPE", "");
//                String[] split = itemCode.split(",");
//                System.out.println(split.toString());
//
//                for(int i = 0; i < split.length; ++i) {
//                    if ("WENGAO".equals(split[i])) {
//                        int histVers = ServDao.count("OA_GW_COMM_FILE_HIS", (new Bean()).set("SERV_ID", oldFileBean.getStr("SERV_ID")).set("DATA_ID", oldFileBean.getStr("DATA_ID"))) + 1;
//                        oldFileBean.set("HISFILE_VERSION", histVers);
//                        ServDao.save("OA_GW_COMM_FILE_HIS", oldFileBean);
//                    }
//                }
//            }
//
//            fileParam.set("ITEM_CODE", "ZHENGWEN");
//        }

        String checksum = "";
        TempFile tmp = null;

        try {
            long start = System.currentTimeMillis();
            tmp = new TempFile(Storage.SMART, input);
            tmp.read();
            IOUtils.closeQuietly(input);
            log.debug(" read inputstream to temp storage qtime:" + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            InputStream is1 = tmp.openNewInputStream();
            checksum = Lang.getMd5checksum(is1);
            IOUtils.closeQuietly(is1);
            start = System.currentTimeMillis();
            InputStream is2 = tmp.openNewInputStream();
            long size = FileStorage.saveFile(is2, absolutePath);
            IOUtils.closeQuietly(is2);
            log.debug(" save file to storage qtime:" + (System.currentTimeMillis() - start));
            fileParam.set("FILE_SIZE", size);
        } catch (NoSuchAlgorithmException var18) {
            log.error(" get file checksum error.", var18);
            throw new RuntimeException("get file checksum error.", var18);
        } catch (IOException var19) {
            log.error(" file upload error.", var19);
            throw new RuntimeException("file upload error.", var19);
        } finally {
            tmp.destroy();
        }

        fileParam.set("DIS_NAME", paramBean.getStr("DIS_NAME"));
        if (fileParam.isEmpty("DIS_NAME")) {
            fileName = paramBean.getStr("FILE_NAME");
            fileParam.set("DIS_NAME", FilenameUtils.getBaseName(fileName));
        }

        fileParam.set("FILE_CHECKSUM", checksum);
        result = ServDao.create("SY_COMM_FILE", fileParam);
        return result;
    }

    public static InputStream download(Bean fileBean) throws IOException {
        String relativePath = fileBean.getStr("FILE_PATH");
        if (relativePath != null && relativePath.length() != 0) {
            return downloadFromExpre(relativePath);
        } else {
            throw new RuntimeException("FILE_PATH can not be null");
        }
    }

    public static InputStream openInputStream(Bean fileBean) throws IOException {
        return download(fileBean);
    }

    public static OutputStream openOutputStream(Bean fileBean) throws IOException {
        String pathExpre = fileBean.getStr("FILE_PATH");
        if (pathExpre != null && pathExpre.length() != 0) {
            String absolutePath = getAbsolutePath(pathExpre);
            return FileStorage.getOutputStream(absolutePath);
        } else {
            throw new RuntimeException("FILE_PATH can not be null");
        }
    }

    public static Bean getImgFile(String fileId, String sizePatten) throws FileNotFoundException {
        if (-1 < fileId.lastIndexOf(",")) {
            fileId = fileId.substring(0, fileId.lastIndexOf(","));
        }

        Bean file = null;
        boolean isIcon = fileId.startsWith("ICON_");
        if (isIcon) {
            file = getIconFile(fileId);
        } else if (fileId.startsWith("USER_")) {
            file = getUserIconFile(fileId, sizePatten);
        } else if (fileId.startsWith("GROUP_")) {
            file = getGroupIconFile(fileId, sizePatten);
        } else if (fileId.startsWith("THUM_")) {
            if (sizePatten == null || sizePatten.length() == 0) {
                sizePatten = "100";
            }

            file = getThumFile(fileId, sizePatten);
        } else {
            file = getFile(fileId);
            if (sizePatten != null && sizePatten.length() > 0) {
                file = getTargetSizeFile(file, sizePatten);
            }
        }

        return file;
    }

    public static Bean getThumFile(String fileId, String sizePatten) throws FileNotFoundException {
        fileId = fileId.substring("ICON_".length());
        Bean file = getFile(fileId);
        if (file == null) {
            throw new FileNotFoundException("fileId:" + fileId);
        } else {
            return getThumFile(file, sizePatten);
        }
    }

    public static Bean getThumFile(Bean file, String sizePatten) {
        String suffix = getSuffix(file.getId());
        String targetName;
        if (suffix == null || suffix.length() == 0) {
            targetName = file.getStr("FILE_MTYPE");
            suffix = getSuffixByMtype(targetName);
        }

        targetName = sizePatten + "." + suffix;
        String sourceExpre = file.getStr("FILE_PATH");
        String targetExpre = buildRelatedPathExpress(sourceExpre, targetName);
        String source = getAbsolutePath(sourceExpre);
        String target = getAbsolutePath(targetExpre);
        boolean exits = false;

        try {
            exits = FileStorage.exists(target);
        } catch (IOException var10) {
            log.error(var10);
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

    private static Bean getUserIconFile(String userCode, String sizePatten) throws FileNotFoundException {
        String userId = userCode.replace("USER_", "");
        if (-1 < userId.lastIndexOf(".")) {
            userId = userId.substring(0, userId.lastIndexOf("."));
        }

        UserBean user = UserMgr.getUser(userId);
        String imgId = user.getImg();
        if (imgId != null && imgId.length() != 0) {
            if (imgId.startsWith("/file/")) {
                imgId = imgId.replace("/file/", "");
            }

            if (imgId.lastIndexOf("?") > 0) {
                imgId = imgId.substring(0, imgId.lastIndexOf("?"));
            }

            if (sizePatten == null || sizePatten.length() == 0) {
                sizePatten = "100";
            }

            return getImgFile(imgId, sizePatten);
        } else {
            return null;
        }
    }

    private static Bean getGroupIconFile(String groupCode, String sizePatten) throws FileNotFoundException {
        String groupId = groupCode.replace("GROUP_", "");
        if (-1 < groupId.lastIndexOf(".")) {
            groupId = groupId.substring(0, groupId.lastIndexOf("."));
        }

        ParamBean paramBean = new ParamBean();
        paramBean.setId(groupId);
        OutBean outBean = ServMgr.act("CC_ORG_GROUP", "byid", paramBean);
        if (outBean == null) {
            return null;
        } else {
            String imgId = outBean.getStr("GROUP_IMG");
            if (imgId != null && imgId.length() != 0) {
                if (imgId.startsWith("/file/")) {
                    imgId = imgId.replace("/file/", "");
                }

                if (imgId.lastIndexOf("?") > 0) {
                    imgId = imgId.substring(0, imgId.lastIndexOf("?"));
                }

                if (sizePatten == null || sizePatten.length() == 0) {
                    sizePatten = "100";
                }

                return getImgFile(imgId, sizePatten);
            } else {
                return null;
            }
        }
    }

    private static Bean getIconFile(String fileId) throws FileNotFoundException {
        fileId = fileId.substring("ICON_".length());
        Bean file = getFile(fileId);
        if (file == null) {
            throw new FileNotFoundException("fileId:" + fileId);
        } else {
            String sourceExpre = file.getStr("FILE_PATH");
            String source = getAbsolutePath(sourceExpre);
            file = buildIconImgBean(file);
            String targetExpre = file.getStr("FILE_PATH");
            String target = getAbsolutePath(targetExpre);
            boolean exits = false;

            try {
                exits = FileStorage.exists(target);
            } catch (IOException var8) {
                log.error(var8);
            }

            if (!exits) {
                String size = Context.getSyConf("SY_ICON_FILE_SIZE", "80x80");
                log.debug("icon_size:" + size);
                storeImgFile(source, target, size);
            }

            return file;
        }
    }

    private static Bean getTargetSizeFile(Bean file, String sizePatten) {
        String sourceExpre = file.getStr("FILE_PATH");
        String suffix = getSuffix(sourceExpre);
        String targetName = sizePatten + "." + suffix;
        String targetExpre = buildRelatedPathExpress(sourceExpre, targetName);
        String source = getAbsolutePath(sourceExpre);
        String target = getAbsolutePath(targetExpre);
        boolean exits = false;

        try {
            exits = FileStorage.exists(target);
        } catch (IOException var10) {
            log.error(var10);
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

    public static void createIconImg(String fileId, int x, int y, int width, int height) {
        String surfix = "";
        if (fileId.lastIndexOf(".") > 0) {
            surfix = fileId.substring(fileId.lastIndexOf(".") + 1);
        }

        Bean src = getFile(fileId);
        Bean target = buildIconImgBean(src);
        InputStream is = null;
        OutputStream out = null;

        try {
            is = download(src);
            out = openOutputStream(target);
        } catch (IOException var20) {
            log.error(var20);
        }

        try {
            String relatedPath = buildRelatedPathExpress(target.getStr("FILE_PATH"), "");
            relatedPath = getAbsolutePath(relatedPath);
            FileStorage.deleteDirectory(relatedPath);
            ImageUtils.cutting(is, out, surfix.toLowerCase(), x, y, width, height);
        } catch (IOException var18) {
            log.error("image cutting error, we will delete it:" + target);

            try {
                FileStorage.deleteFile(target.getStr("FILE_PATH"));
            } catch (IOException var17) {
                log.warn(" delete error file:" + target, var17);
            }
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }

    }

    private static boolean storeThumbnailFile(String source, String target, String sizePatten, String suffix) {
        String[] indexArray = sizePatten.split("x");
        int width;
        if (1 == indexArray.length) {
            width = Integer.valueOf(sizePatten);
            return storeThumbnailFile(source, target, width, suffix);
        } else if (2 == indexArray.length) {
            width = Integer.valueOf(indexArray[0]);
            int height = Integer.valueOf(indexArray[1]);
            return storeThumbnailFile(source, target, width, height, suffix);
        } else {
            log.error("error sizePatten:" + sizePatten);
            return false;
        }
    }

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
        } catch (IOException var16) {
            log.error(var16);
        }

        try {
            BufferedImage originalImage = ImageIO.read(is);
            BufferedImage thumbnail = Thumbnails.of(new BufferedImage[]{originalImage}).size(maxThumSize, maxThumSize).asBufferedImage();
            ImageIO.write(thumbnail, suffix, out);
        } catch (IOException var14) {
            log.error("创建缩略图失败!", var14);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }

        log.debug("create thumbnail file...qtime:" + (System.currentTimeMillis() - start));
        return true;
    }

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
        } catch (IOException var17) {
            log.error(var17);
        }

        try {
            BufferedImage originalImage = ImageIO.read(is);
            BufferedImage thumbnail = Thumbnails.of(new BufferedImage[]{originalImage}).size(width, height).crop(Positions.CENTER).asBufferedImage();
            ImageIO.write(thumbnail, suffix, out);
        } catch (IOException var15) {
            log.error("创建缩略图失败!", var15);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(out);
        }

        log.debug("create thumbnail file...qtime:" + (System.currentTimeMillis() - start));
        return true;
    }

    private static Bean buildIconImgBean(Bean source) {
        return buildImgBeanByPrefix(source, "ICON_");
    }

    private static Bean buildImgBeanByPrefix(Bean source, String prefix) {
        String surfix = "";
        String fileId = source.getId();
        if (fileId.lastIndexOf(".") > 0) {
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

    public static List<Bean> getFileListBean(String servSrcId, String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList();
        } else {
            ParamBean sql = new ParamBean();
            sql.setQueryNoPageFlag(true);
            sql.set("DATA_ID", dataId);
            sql.set("_ORDER_", " FILE_SORT asc, S_MTIME asc");
            return ServDao.finds("SY_COMM_FILE", sql);
        }
    }

    public static Bean getFile(String fileId) {
        try {
            fileId = URLDecoder.decode(fileId, "UTF-8");
        } catch (UnsupportedEncodingException var3) {
            log.warn("url decode error" + var3);
        }

        if (fileId != null && fileId.length() != 0) {
            if (fileId.startsWith("/file/")) {
                fileId = fileId.substring(6);
            }

            Bean histFile = null;
            Bean result;
            if (fileId.startsWith("HIST_")) {
                histFile = ServDao.find("SY_COMM_FILE_HIS", fileId);
                fileId = histFile.getStr("FILE_ID");
            } else if (fileId.startsWith("OAHIST_")) {
                result = ServDao.find("OA_GW_COMM_FILE_HIS", fileId);
                return result;
            }

            result = ServDao.find("SY_COMM_FILE", fileId);
            if (result != null && histFile != null) {
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
        } else {
            throw new TipException(Context.getSyMsg("SY_DOWNLOAD_FILE_NOT_FOUND", new Object[0]) + ",file id:" + fileId);
        }
    }

    public static Bean createLinkFile(Bean fileBean, Bean param) {
        String fileUUID = Lang.getUUID() + "." + FilenameUtils.getExtension(fileBean.getId());
        Bean newFile = fileBean.copyOf();
        newFile.set("FILE_ID", fileUUID);
        newFile.remove("S_MTIME").remove("S_FLAG");
        newFile.remove("S_USER").remove("S_UNAME");
        newFile.remove("S_DEPT").remove("S_DNAME");
        newFile.remove("S_CMPY").remove("FILE_HIST_COUNT").remove("WF_NI_ID");
        if (newFile.isEmpty("ORIG_FILE_ID")) {
            newFile.set("ORIG_FILE_ID", fileBean.getId());
        }

        extendBean(newFile, param);
        return ServDao.create("SY_COMM_FILE", newFile);
    }

    public static Bean copyFile(Bean fileBean, Bean param) {
        String fileUUID = Lang.getUUID();
        String pathExpr = buildPathExpr(param.getStr("SERV_ID"), fileUUID);
        String absolutePath = getAbsolutePath(pathExpr);

        try {
            InputStream is = download(fileBean);
            FileStorage.saveFile(is, absolutePath);
        } catch (Exception var8) {
            throw new RuntimeException(var8.getMessage(), var8);
        }

        Bean newFileBean = fileBean.copyOf();
        String fileName = newFileBean.getStr("FILE_NAME");
        String surfix = "";
        if (fileName.lastIndexOf(".") > 0) {
            surfix = fileName.substring(fileName.lastIndexOf("."));
        }

        newFileBean.set("FILE_ID", fileUUID + surfix);
        newFileBean.set("FILE_PATH", pathExpr);
        extendBean(newFileBean, param);
        return ServDao.create("SY_COMM_FILE", newFileBean.remove("S_MTIME"));
    }

    private static void extendBean(Bean newFileBean, Bean param) {
        Iterator it = param.keySet().iterator();

        while(it.hasNext()) {
            String key = (String)it.next();
            newFileBean.set(key, param.getStr(key));
        }

    }

    public static String getRootPath() {
        String result = "";
        result = Context.getSyConf("SY_COMM_FILE_ROOT_PATH", DEFAULT_FILE_ROOT_PATH);
        if (!result.endsWith("/") && !result.endsWith(File.separator)) {
            result = result + "/";
        }

        return result;
    }

    public static void updateFile(Bean file) {
        ServDao.update("SY_COMM_FILE", file);
    }

    public static int deleteFile(List<Bean> files) {
        int count = 0;
        Iterator var3 = files.iterator();

        while(var3.hasNext()) {
            Bean file = (Bean)var3.next();

            try {
                if (deleteFile(file)) {
                    ++count;
                }
            } catch (Exception var5) {
                log.warn("delete file failed from disk.", var5);
            }
        }

        return count;
    }

    public static boolean deleteFile(String fileId) {
        Bean file = getFile(fileId);
        return deleteFile(file);
    }

    private static boolean isOccupied(Bean file) {
        Bean paramBean = new Bean();
        paramBean.set("FILE_PATH", file.getStr("FILE_PATH"));
        int count = ServDao.count("SY_COMM_FILE", paramBean);
        return count > 1;
    }

    public static boolean deleteFile(Bean file) {
        boolean result = false;
        ServDao.delete("SY_COMM_FILE", file);
        if (file.getStr("FILE_PATH") != null && file.getStr("FILE_PATH").length() != 0) {
            if (isOccupied(file)) {
                return true;
            } else {
                try {
                    String absolutePath = getAbsolutePath(file.getStr("FILE_PATH"));
                    FileStorage.deleteFile(absolutePath);
                    String relatedPath = buildRelatedPathExpress(file.getStr("FILE_PATH"), "");
                    relatedPath = getAbsolutePath(relatedPath);
                    boolean exits = FileStorage.exists(relatedPath);
                    if (exits) {
                        FileStorage.deleteDirectory(relatedPath);
                    }

                    result = true;
                } catch (IOException var5) {
                    log.warn("delete file failed from disk.", var5);
                    result = false;
                }

                return result;
            }
        } else {
            return true;
        }
    }

    public static String getMTypeBySuffix(String suffix) {
        String contentType = "application/octet-stream";
        if (suffix != null && suffix.length() > 0) {
            Bean result = DictMgr.getItem("SY_COMM_FILE_MTYPE", suffix);
            if (result != null && result.contains("ITEM_NAME")) {
                contentType = result.getStr("ITEM_NAME");
            }
        }

        return contentType;
    }

    public static String getMTypeByName(String name) {
        String suffix = getSuffix(name);
        return getMTypeBySuffix(suffix);
    }

    private static String buildRelatedPathExpress(String fileExpre, String relatedFile) {
        String targetExpre = fileExpre + "_file/" + relatedFile;
        return targetExpre;
    }

    private static boolean storeImgFile(String source, String target, String sizePatten) {
        boolean result = false;
        String[] indexArray = sizePatten.split("x");
        if (2 != indexArray.length) {
            log.warn(" invalid image size patten:" + sizePatten);
            return result;
        } else {
            boolean locked = false;
            TaskLock lock = null;
            InputStream is = null;
            OutputStream out = null;

            try {
                lock = new TaskLock("ImageZoom", FilenameUtils.getName(source));
                locked = lock.lock();
                if (locked) {
                    int width = Integer.valueOf(indexArray[0]);
                    int height = Integer.valueOf(indexArray[1]);
                    is = FileStorage.getInputStream(source);
                    out = FileStorage.getOutputStream(target);
                    ImageZoom imgZoom = new ImageZoom(width, height);
                    imgZoom.setQuality(0.8F);
                    imgZoom.resize(is, out);
                    result = true;
                }
            } catch (Exception var17) {
                log.error("image resize error, we will delete it:" + target);

                try {
                    FileStorage.deleteFile(target);
                } catch (IOException var16) {
                    log.warn(" delete error file:" + target, var16);
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
    }

    public static String getSuffix(String fileId) {
        String suffix = "";
        if (fileId.lastIndexOf(".") > 0) {
            suffix = fileId.substring(fileId.lastIndexOf(".") + 1);
        }

        return suffix;
    }

    private static Bean createFileBean(String servId, String dataId, String fileId, String category, String fileName, String mType, long sizeInBytes, String checksum) {
        Bean itemParam = new Bean();
        String uuid = Lang.getUUID();
        String surfix = "";
        if (fileName.lastIndexOf(".") > 0) {
            surfix = fileName.substring(fileName.lastIndexOf("."));
        }

        String pathExpr = buildPathExpr(servId, uuid + surfix);
        if (fileId != null && fileId.length() != 0) {
            itemParam.set("FILE_ID", fileId);
        } else {
            itemParam.set("FILE_ID", uuid + surfix);
        }

        if (mType == null || mType.length() == 0) {
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

    public static String getSuffixByMtype(String mtype) {
        if (mtype.startsWith("image/jpeg")) {
            return ".jpg";
        } else if (mtype.startsWith("image/bmp")) {
            return ".bmp";
        } else if (mtype.startsWith("image/gif")) {
            return ".gif";
        } else {
            return mtype.startsWith("image/png") ? ".png" : "";
        }
    }

    private static Bean createFileBean(Bean paramBean) {
        Bean itemParam = new Bean();
        String fileName = paramBean.getStr("FILE_NAME");
        String uuid = Lang.getUUID();
        String suffix = "";
        if (fileName.lastIndexOf(".") > 0) {
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }

        if (suffix == null || suffix.length() == 0) {
            suffix = getSuffixByMtype(paramBean.getStr("FILE_MTYPE"));
        }

        String servId = paramBean.getStr("SERV_ID");
        String pathExpr = buildPathExpr(servId, uuid + suffix);
        String fileId = paramBean.getStr("FILE_ID");
        if (fileId != null && fileId.length() != 0) {
            itemParam.set("FILE_ID", fileId);
        } else {
            itemParam.set("FILE_ID", uuid + suffix);
        }

        String mType = paramBean.getStr("FILE_MTYPE");
        if (mType == null || mType.length() == 0) {
            mType = getMTypeByName(fileName);
        }

        long sizeInBytes = -1L;
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
        return itemParam;
    }

    public static String buildPathExpr(String servId, String newName) {
        String expresstion = getFilePathExpr(servId);
        if (servId == null || servId.length() == 0) {
            servId = "UNKNOW";
        }

        String value = ServUtils.replaceSysVars(expresstion);
        value = value.replace("@SERV_ID@", servId);
        if (!value.endsWith("/")) {
            value = value + "/";
        }

        return value + newName;
    }

    public static String getAbsolutePath(String expresstion) {
        if (expresstion.startsWith("@SYS_FILE_PATH@")) {
            return expresstion.replace("@SYS_FILE_PATH@", getRootPath());
        } else if (expresstion.startsWith("@OA_FILE_PATH@")) {
            return expresstion.replace("@OA_FILE_PATH@", getTransFilePath());
        } else if (expresstion.startsWith(CURRENT_SYSTEM_HOME_PATH_EXPR)) {
            return expresstion.replace(CURRENT_SYSTEM_HOME_PATH_EXPR, Context.app(APP.SYSPATH).toString());
        } else if (expresstion.startsWith(CURRENT_WEBINF_PATH_EXPR)) {
            return expresstion.replace(CURRENT_WEBINF_PATH_EXPR, Context.app(APP.WEBINF).toString());
        } else if (expresstion.startsWith(CURRENT_WEBINF_DOC_PATH_EXPR)) {
            return expresstion.replace(CURRENT_WEBINF_DOC_PATH_EXPR, Context.app(APP.WEBINF_DOC).toString());
        } else {
            return expresstion.startsWith(CURRENT_WEBINF_DOC_CMPY_PATH_EXPR) ? expresstion.replace(CURRENT_WEBINF_DOC_CMPY_PATH_EXPR, Context.app(APP.WEBINF_DOC_CMPY).toString()) : getRootPath() + expresstion;
        }
    }

    private static String getFilePathExpr(String servId) {
        if (servId != null && servId.length() != 0) {
            ServDefBean servBean = null;

            try {
                servBean = ServUtils.getServDef(servId);
            } catch (Exception var3) {
                log.warn("the service not found, servId:" + servId, var3);
            }

            if (servBean != null && servBean.isNotEmpty("SERV_FILE_PATH")) {
                String expr = servBean.getStr("SERV_FILE_PATH");
                return expr.length() == 0 ? "@SYS_FILE_PATH@/@CMPY_CODE@/@SERV_ID@/@DATE_YEAR@/@DATE@/" : expr;
            } else {
                return "@SYS_FILE_PATH@/@CMPY_CODE@/@SERV_ID@/@DATE_YEAR@/@DATE@/";
            }
        } else {
            return "@SYS_FILE_PATH@/@CMPY_CODE@/@SERV_ID@/@DATE_YEAR@/@DATE@/";
        }
    }

    private static InputStream downloadFromExpre(String pathExpre) throws IOException {
        String absolutePath = getAbsolutePath(pathExpre);
        return FileStorage.getInputStream(absolutePath);
    }

    public static String getFileDisName(Bean fileBean) {
        if (fileBean.isEmpty("DIS_NAME")) {
            return fileBean.getStr("FILE_NAME");
        } else {
            String disName = Strings.escapeFilenameSepcChar(fileBean.getStr("DIS_NAME"));
            String fileExt = FilenameUtils.getExtension(fileBean.getStr("FILE_NAME"));
            if (fileExt == null || fileExt.length() <= 0) {
                fileExt = FilenameUtils.getExtension(fileBean.getStr("FILE_ID"));
            }

            return disName + "." + fileExt;
        }
    }

    public static void copyToLocal(String fileId, String localPath) {
        Bean fileBean = getFile(fileId);
        String filePath = getAbsolutePath(fileBean.getStr("FILE_PATH"));
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            is = FileStorage.getInputStream(filePath);
            fos = new FileOutputStream(localPath);
            IOUtils.copyLarge(is, fos);
        } catch (Exception var10) {
            throw new RuntimeException(var10.getMessage(), var10);
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }

            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }

        }

    }

    public static String getBase64IconByImg(String img) {
        return getBase64IconByImg(img, "60x60");
    }

    public static String getBase64ByImg(String fileId, String size) {
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
                is = new Base64InputStream(in, true, -1, (byte[])null);
                result = IOUtils.toString(is);
            }
        } catch (Exception var10) {
            log.error(var10.getMessage(), var10);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(is);
        }

        return result;
    }

    public static String getBase64IconByImg(String img, String size) {
        String result = "";
        InputStream in = null;
        Base64InputStream is = null;

        try {
            in = getIconInputStreamByImg(img, size);
            if (in != null) {
                is = new Base64InputStream(in, true, -1, (byte[])null);
                result = IOUtils.toString(is);
            }
        } catch (Exception var9) {
            log.error(var9.getMessage(), var9);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(is);
        }

        return result;
    }

    public static InputStream getIconInputStreamByImg(String img, String size) {
        InputStream in = null;

        try {
            Bean fileBean = getIconFileBeanByImg(img, size);
            if (fileBean != null) {
                in = download(fileBean);
            }
        } catch (Exception var4) {
            log.error(var4.getMessage(), var4);
        }

        return in;
    }

    public static Bean getIconFileBeanByImg(String img, String size) throws IOException {
        if (img.isEmpty()) {
            return null;
        } else {
            int pos = img.indexOf(",");
            if (pos > 0) {
                img = img.substring(0, pos);
            }

            img = "ICON_" + img;
            return getImgFile(img, size);
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            File oldFile = new File(oldPath);
            File newFile = new File(newPath);
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }

            in = new FileInputStream(oldFile);
            out = new FileOutputStream(newFile);
            byte[] buffer = new byte[8192];
            boolean var7 = false;

            int readByte;
            while((readByte = in.read(buffer)) != -1) {
                out.write(buffer, 0, readByte);
            }
        } catch (Exception var16) {
            var16.printStackTrace();
            System.out.println((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date()) + "在这个时间这个文件" + oldPath + "没有复制成功");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException var15) {
                var15.printStackTrace();
            }

        }

    }

    public static void hongTouFileSave(String fileId) {
        String hisFileSaveType = Context.getSyConf("HIS_FILE_SAVE_TYPE", "");
        String[] split = hisFileSaveType.split(",");
        System.out.println(split.toString());

        label78:
        for(int k = 0; k < split.length; ++k) {
            if ("REDHEAD".equals(split[k])) {
                Bean fileBean = ServDao.find("SY_COMM_FILE", fileId);
                String niid = fileBean.getStr("WF_NI_ID");
                List<Bean> hisList = ServDao.finds("OA_GW_COMM_FILE_HIS", "AND SERV_ID ='" + fileBean.getStr("SERV_ID") + "'AND DATA_ID='" + fileBean.getStr("DATA_ID") + "'");
                int count = 0;
                Iterator var9 = hisList.iterator();

                while(true) {
                    Bean hisBean;
                    String path;
                    String zhengWenFilePath;
                    String newHisFilePath;
                    String[] filePaths;
                    int i;
                    do {
                        do {
                            if (!var9.hasNext()) {
                                if (count != 0) {
                                    continue label78;
                                }

                                fileBean.setId("");
                                int histVers = ServDao.count("OA_GW_COMM_FILE_HIS", (new Bean()).set("SERV_ID", fileBean.getStr("SERV_ID")).set("DATA_ID", fileBean.getStr("DATA_ID"))) + 1;
                                fileBean.set("HISFILE_VERSION", histVers);
                                String hisFileId = "OAHIST_" + Lang.getUUID() + "." + getSuffix(fileBean.getStr("FILE_ID"));
                                fileBean.set("HISFILE_ID", hisFileId);
                                path = fileBean.getStr("FILE_PATH");
                                zhengWenFilePath = getAbsolutePath(path);
                                newHisFilePath = "";
                                filePaths = zhengWenFilePath.split("/");

                                for(i = 0; i < filePaths.length; ++i) {
                                    if (i == filePaths.length - 1) {
                                        newHisFilePath = newHisFilePath + hisFileId;
                                    } else {
                                        newHisFilePath = newHisFilePath + filePaths[i] + "/";
                                    }
                                }

                                copyFile(zhengWenFilePath, newHisFilePath);
                                newHisFilePath = "";
                                String[] sqlPath = path.split("/");

                                for(int i1 = 0; i1 < sqlPath.length; ++i1) {
                                    if (i1 == sqlPath.length - 1) {
                                        newHisFilePath = newHisFilePath + hisFileId;
                                    } else {
                                        newHisFilePath = newHisFilePath + sqlPath[i1] + "/";
                                    }
                                }

                                fileBean.set("FILE_PATH", newHisFilePath);
                                fileBean.set("HISTFILE_QINGGAO_TYPE", "REDHEAD");
                                fileBean.set("WF_NI_ID", niid);
                                fileBean.set("S_MTIME", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")).format(new Date()));
                                ServDao.save("OA_GW_COMM_FILE_HIS", fileBean);
                                log.debug("新增一个正文的历史版本，服务编码为：" + fileBean.getStr("SERV_ID") + ",数据主键为：" + fileBean.getStr("DATA_ID"));
                                continue label78;
                            }

                            hisBean = (Bean)var9.next();
                        } while(!"REDHEAD".equals(hisBean.getStr("HISTFILE_QINGGAO_TYPE")));
                    } while(!niid.equals(hisBean.getStr("WF_NI_ID")));

                    path = fileBean.getStr("FILE_PATH");
                    zhengWenFilePath = getAbsolutePath(path);
                    newHisFilePath = "";
                    filePaths = zhengWenFilePath.split("/");

                    for(i = 0; i < filePaths.length; ++i) {
                        if (i == filePaths.length - 1) {
                            newHisFilePath = newHisFilePath + hisBean.get("HISFILE_ID");
                        } else {
                            newHisFilePath = newHisFilePath + filePaths[i] + "/";
                        }
                    }

                    copyFile(zhengWenFilePath, newHisFilePath);
                    i = ServDao.count("OA_GW_COMM_FILE_HIS", (new Bean()).set("SERV_ID", fileBean.getStr("SERV_ID")).set("DATA_ID", fileBean.getStr("DATA_ID")));
                    hisBean.set("HISFILE_VERSION", i);
                    hisBean.set("FILE_SIZE", fileBean.getStr("FILE_SIZE"));
                    hisBean.set("DIS_NAME", fileBean.getStr("DIS_NAME"));
                    hisBean.set("FILE_MTYPE", fileBean.getStr("FILE_MTYPE"));
                    hisBean.set("FILE_NAME", fileBean.getStr("FILE_NAME"));
                    hisBean.set("FILE_CAT", fileBean.getStr("FILE_CAT"));
                    hisBean.set("DATA_TYPE", fileBean.getStr("DATA_TYPE"));
                    hisBean.set("ITEM_CODE", fileBean.getStr("ITEM_CODE"));
                    hisBean.set("FILE_CHECKSUM", fileBean.getStr("FILE_CHECKSUM"));
                    hisBean.set("FILE_HIST_COUNT", fileBean.getInt("FILE_HIST_COUNT") + 1);
                    hisBean.set("WF_NI_ID", niid);
                    hisBean.set("S_MTIME", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")).format(new Date()));
                    hisBean.set("HISTFILE_QINGGAO_TYPE", "REDHEAD");
                    ServDao.save("OA_GW_COMM_FILE_HIS", hisBean);
                    ++count;
                    log.debug("覆盖了一个清稿的历史版本，服务编码为：" + hisBean.getStr("SERV_ID") + ",数据主键为：" + hisBean.getStr("DATA_ID"));
                }
            }
        }

    }

    public static void inputStreamToFile(InputStream is, String newPath) {
        FileOutputStream out = null;

        try {
            File newFile = new File(newPath);
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }

            out = new FileOutputStream(newFile);
            byte[] buffer = new byte[8192];
            boolean var5 = false;

            int readByte;
            while((readByte = is.read(buffer)) != -1) {
                out.write(buffer, 0, readByte);
            }

            out.flush();
        } catch (Exception var6) {
            var6.printStackTrace();
            System.out.println((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date()) + "在这个时间这个文件" + newPath + "没有复制成功");
        }

    }
}
