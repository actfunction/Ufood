package com.rh.gw.util;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.comm.ConfMgr;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.suwell.ofd.custom.agent.ConvertException;
import com.suwell.ofd.custom.agent.HTTPAgent;
import com.suwell.ofd.custom.wrapper.PackException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.text.DecimalFormat;

import static com.rh.core.comm.FileMgr.upload;

/**
 * doc文件转ofd
 *
 * @author kfzx-yangjw
 */
public class GwOfdUtil {
    //    记录日志
    private static Log log = LogFactory.getLog(GwOfdUtil.class);

    /**
     * 获取ofd转换服务的地址
     */
    private static final String OFD_SERVER_URL_DEF = "http://127.0.0.1:8088/convert-issuer/";
    private static final String OFD_SERVER_URL = Context.getSyConf("OFD_SERVER_URL", OFD_SERVER_URL_DEF);


    /**
     * 参数处理
     *
     * @param paramBean 请求参数（正文doc文件的相关参数）
     * @return 返回值
     */
    public static OutBean cov(ParamBean paramBean) {

//        得到服务ID
//        判断是否为行政发文或者业务发文
        String servId = paramBean.getServId();
        if (servId == null) {//如果没有服务ID
            log.debug("缺少服务ID，文件转换失败，本次请求参数为：" + paramBean);
            return new OutBean().setError("文件转换错误，缺失服务ID");
        }
//        if (servId.equals("OA_GW_GONGWEN_ICBC_XZFW") || servId.equals("OA_GW_GONGWEN_ICBC_YWFW")) {//如果属于行政发文或业务发文
//                获得正文bean
        Bean zhengwen = paramBean.getBean("zhengwen");
//            获得正文ID
        String fileId = zhengwen.getStr("fileId");
//            获取文件真实路径
        String filePath = getFilePath(fileId);
//            转换文件类型
        int convertInfo = convert(filePath);
//            log.error("");
//            System.err.println(zhengwen);
//            上传ofd文件
        Bean upload = null;
        if (convertInfo > 0) {
            try {
                Bean itemParam = new Bean();
                itemParam.set("FILE_NAME", modifiedFileName(zhengwen.getStr("fileName")));
                itemParam.set("DIS_NAME", "红章正文(无章)");
                itemParam.set("FILE_SORT", 2);
                itemParam.set("SERV_ID", servId);
                itemParam.set("DATA_ID", zhengwen.getStr("dataId"));
                itemParam.set("FILE_CAT", "OFD");
                itemParam.set("ITEM_CODE", "OFD_NOZ");
                upload = upload(itemParam, new FileInputStream(modifiedFileName(filePath)));
//                System.err.println(upload);
                log.debug("文件转换OFD完成：" + upload);

                return new OutBean().setOk().setData(upload);
            } catch (FileNotFoundException e) {
                log.error("文件转换失败,该文件不存在" + e);
                ;
            }
        }
        log.error("文件转换失败-covert方法报错");
        return new OutBean().setError("转换失败！");

//            删除旧文件，暂时保留，不删除
//            deleteOriginalFile(filePath);
//            返回正确的文件参数信息
//            Bean newfile = FileMgr.getFile(modifiedFileName(fileId));

//        } else {
//            log.error("不是行政发文或者业务发文调用");
//            return new OutBean().setError("转换失败！");
//        }


    }


    /**
     * 获取系统当前文件的绝对路径
     *
     * @param fileId 文件ID
     * @return 系统文件保存位置 例如：d:\\upload_files\ruaho\OA_GW_GONGWEN_ICBC_XZFW\2018\2018-11-06\17XTgJOcV9CVGNHnLj7bgx4.ofd
     */
    private static String getFilePath(String fileId) {
        Bean file = FileMgr.getFile(fileId);
        String SYS_FILE_PATH = ConfMgr.getConf("SY_COMM_FILE_ROOT_PATH", "");
        String FILE_PATH = file.getStr("FILE_PATH");
        String FilePath = SYS_FILE_PATH + FILE_PATH.substring(15);
        File docfile = new File(FilePath);
        if (docfile.exists() && docfile.isFile()) {
            long fileS = docfile.length();
            DecimalFormat df = new DecimalFormat("#.00");
            String size;
            if (fileS < 1024) {
                size = df.format((double) fileS) + "BT";
            } else if (fileS < 1048576) {
                size = df.format((double) fileS / 1024) + "KB";
            } else if (fileS < 1073741824) {
                size = df.format((double) fileS / 1048576) + "MB";
            } else {
                size = df.format((double) fileS / 1073741824) + "GB";
            }
            log.debug("原doc文件大小为：" + size);
        } else {
            log.error("传入文件路径有误，错误路径为：" + FilePath);
            return FilePath;
        }
        return FilePath;
    }

    /**
     * 修改后缀名为.ofd--用于更新数据库信息
     *
     * @param str 名称
     * @return 修改后的字符串
     */
    private static String modifiedFileName(String str) {
        int i = str.lastIndexOf(".");
        String substring = str.substring(0, i) + ".ofd";
//        System.out.println(substring);
        return substring;
    }

    /**
     * 将目标文件转换为ofd文件，储存到指定路径中
     *
     * @param filePath 文件路径
     * @return 是否转换成功
     */
    private static int convert(String filePath) {
        log.info("convert : " + filePath);
        HTTPAgent ha = null;
        FileOutputStream targetFile = null;
        int convertInfo = 1;
        try {
            ha = new HTTPAgent(OFD_SERVER_URL);
            log.info("convert ha success: " + ha);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("无法连接OFD服务器！");
            return -1;
        }
        try {
            targetFile = new FileOutputStream(modifiedFileName(filePath));
            ha.officeToOFD(new File(filePath), targetFile);
        } catch (PackException e) {
            e.printStackTrace();
            log.error("con pack error！" + e.getMessage());
            convertInfo = -1;
        } catch (IOException e) {
            e.printStackTrace();
            convertInfo = -1;
        } catch (ConvertException e) {
            e.printStackTrace();
            log.error("转换失败1！" + e.getMessage());
            convertInfo = -1;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("转换失败2！" + e.getMessage());
            convertInfo = -1;
        } finally {
            try {
                ha.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("转换失败3！" + e.getMessage());
                convertInfo = -1;
            }
            try {
                targetFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("转换失败4！" + e.getMessage());
                convertInfo = -1;
            }
        }
        return convertInfo;
//        return 1;
    }


    /**
     * 更新数据库文件信息
     *
     * @param fileId 文件ID
     * @return 数据库表是否更新成功
     */
    private static int UpdateDataBase(String fileId) {
        String newFileId = modifiedFileName(fileId);
        Bean file = FileMgr.getFile(fileId);
        String fileName = file.getStr("FILE_NAME");
        fileName = modifiedFileName(fileName);
        String file_path = modifiedFileName(file.getStr("FILE_PATH"));
        SqlExecutor se = Context.getExecutor();//获取SqlExecutor对象，用于执行sql语句
        String sql = "UPDATE PLATFORM.SY_COMM_FILE t " +
                "SET t.FILE_ID='" + newFileId +
                "' ,t.FILE_NAME='" + fileName +
                "' ,t.FILE_PATH='" + file_path +
                "' ,t.ITEM_CODE='OFD'" +
                " WHERE t.FILE_ID LIKE '" + fileId + "' ESCAPE '#'";

        int execute = se.execute(sql);
        return execute;
    }


    /**
     * 将原doc文件删除
     *
     * @param OriginalFile 源文件路径
     * @return 是否删除成功
     */
    private static boolean deleteOriginalFile(String OriginalFile) {
        File file = new File(OriginalFile);
        boolean delete = false;
        try {
            delete = file.delete();
            if (!delete) {
                throw new Exception("文件删除失败！" + OriginalFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return delete;
    }

}
