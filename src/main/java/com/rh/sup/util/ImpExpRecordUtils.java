package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.DateUtils;

import java.util.Date;
import java.util.List;

/*
SUP_IMP_EXP_RECORD 唯一 （RECORD_ID）
                       （RECORD_TYPE/SERV_ID/S_USER/S_ATIME）

RECORD_ID         主键
NAME            名称
RECORD_TYPE         功能类型 1:导入 2:导出
SERV_ID         服务名称
XM_ID           项目ID
FILE_ID         结果文件ID
STATE           状态 ing end 18%
RESULT_JSON
S_USER
S_TDEPT
S_ODEPT
S_MTIME
S_DEPT
S_CMPY
S_ATIME
*/
public class ImpExpRecordUtils {

    /** 导入导出记录表 */
    public static final String SERV_IMP_EXP_RECORD = "SUP_IMP_EXP_RECORD";

    public enum RecordType {

        IMP_TYPE("1"),
        EXP_TYPE("2");

        private String type;

        // 构造方法
        private RecordType(String type) {
            this.type = type;
        }

        // get set 方法
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static void save(String name, ImpExpRecordUtils.RecordType recordType, String servId, String xmId, String fileId, String state, String resultJson, Long aTimeL) {
        Bean saveBean = new Bean();

        String aTimeStr = DateUtils.getStringFromDate(new Date(aTimeL), DateUtils.FORMAT_TIMESTAMP);

        SqlBean sqlBean = new SqlBean();
        sqlBean.and("S_ATIME", aTimeStr);
        String currentUserCode = Context.getUserBean().getCode();
        sqlBean.and("S_USER", currentUserCode);
        List<Bean> beanList = ServDao.finds(SERV_IMP_EXP_RECORD, sqlBean);
        if (beanList!=null && !beanList.isEmpty()) {
            saveBean = beanList.get(0);
        }
        saveBean.set("RECORD_NAME", name);
        saveBean.set("RECORD_TYPE", recordType.getType());
        saveBean.set("SERV_ID", servId);
        saveBean.set("XM_ID", xmId);
        saveBean.set("FILE_ID", fileId);
        saveBean.set("STATE", state);
        saveBean.set("RESULT_JSON", resultJson);
        saveBean.set("S_ATIME", aTimeStr);

        ServDao.save(SERV_IMP_EXP_RECORD, saveBean);
        Transaction.commit();
    }

    /*public static void save(String servId, String dataId, String str1, String text1) {
        Bean saveBean = new Bean();

        SqlBean sqlBean = new SqlBean();
        sqlBean.and("SERV_ID", servId);
        sqlBean.and("DATA_ID", dataId);
//        sqlBean.and("TEXT_1", str1);
        String currentUserCode = Context.getUserBean().getCode();
        sqlBean.and("S_USER", currentUserCode);

        List<Bean> beanList = ServDao.finds(TsConstant.SERV_OBJECT, sqlBean);
        if (null == beanList || beanList.isEmpty()) {
            saveBean.set("SERV_ID", servId);
            saveBean.set("DATA_ID", dataId);
            saveBean.set("STR1", str1);
            saveBean.set("TEXT_1", text1);
        } else {
            saveBean = beanList.get(0);
            saveBean.set("SERV_ID", servId);
            saveBean.set("DATA_ID", dataId);
            saveBean.set("STR1", str1);
            saveBean.set("TEXT_1", text1);
        }

        ServDao.save(TsConstant.SERV_OBJECT, saveBean);
        Transaction.commit();
    }*/

//    public static void delete(String servId, String dataId) {
//        SqlBean sqlBean = new SqlBean();
//        sqlBean.and("SERV_ID", servId);
//        sqlBean.and("DATA_ID", dataId);
//        String currentUserCode = Context.getUserBean().getCode();
//        sqlBean.and("S_USER", currentUserCode);
//
//        ServDao.destroy(TsConstant.SERV_OBJECT, sqlBean);
//        Transaction.commit();
//    }


    public static String getResultJson(Long aTimeL) {
        String result = null;
        Bean bean = find(aTimeL);
        if (bean != null) {
            result = bean.getStr("RESULT_JSON");
        }
        return result;
    }


    /*
        根据条件获取单条记录
        唯一 （RECORD_ID）
              （RECORD_TYPE/SERV_ID/S_USER/S_ATIME）
     */
    public static Bean find(String recordId) {
        return ServDao.find(SERV_IMP_EXP_RECORD, recordId);
    }

    /*
     根据条件获取单条记录
     唯一 （RECORD_ID）
           （RECORD_TYPE/SERV_ID/S_USER/S_ATIME）
    */
    public static Bean find(Long dateTime) {
        Bean result = null;
        String aTimeStr = DateUtils.getStringFromDate(new Date(dateTime), DateUtils.FORMAT_TIMESTAMP);

        SqlBean sqlBean = new SqlBean();
        sqlBean.and("S_ATIME", aTimeStr);

        List<Bean> beanList = ServDao.finds(SERV_IMP_EXP_RECORD, sqlBean);
        if (beanList!=null && !beanList.isEmpty()) {
            result = beanList.get(0);
        }

        return result;
    }

    /*
       根据条件 RECORD_TYPE/SERV_ID/S_USER 记录
       唯一 （RECORD_ID）
             （RECORD_TYPE/SERV_ID/S_USER/S_ATIME）
    */
    public static List<Bean> finds(String servId, String userCode, String recordType) {
        SqlBean sqlBean = new SqlBean();
        sqlBean.and("SERV_ID", servId);
        sqlBean.and("S_USER", userCode);
        sqlBean.and("RECORD_TYPE", recordType);
        String currentUserCode = Context.getUserBean().getCode();
        sqlBean.and("S_USER", currentUserCode);
        sqlBean.orders(" S_ATIME desc");

        return ServDao.finds(SERV_IMP_EXP_RECORD, sqlBean);
    }
}
