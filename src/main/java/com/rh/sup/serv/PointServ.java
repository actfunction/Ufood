package com.rh.sup.serv;

import com.rh.core.base.*;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.sup.util.ImpUtils;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述: SUO_APPRO_POINT 服务扩展类
 * @author: guochenghao
 * @date: 2018/11/27 14:08
 */
public class PointServ extends CommonServ {


    private static Log log = org.apache.commons.logging.LogFactory.getLog(PointServ.class);

    /**
     * 功能描述: 数据字典表名称
     * @date: 2018/11/27 14:06
     */
    private final String SUP_SERV_DICT = "SUP_SERV_DICT";

    /**
     * @description: 要点类 表名
     * @author: kfzx-guoch
     * @date: 2018/12/11 15:32
     */
    private final String SUP_APPRO_POINT = "SUP_APPRO_POINT";

    /**
     * 功能描述: 提醒规则类型 DICT_KINDS
     * @author: guochenghao
     * @date: 2018/11/21 9:22
     */
    public static final String NOTIFY_RULE_TYPE = "001";

    /**
     * 功能描述: 办理类型 DICT_KINDS
     * @author: guochenghao
     * @date: 2018/11/21 9:22
     */
    public static final String HANDLE_TYPE = "002";

    /**
     * 功能描述: 重点工作DICT_KINDS
     * @author: guochenghao
     * @date: 2018/11/21 9:22
     */
    public static final String MAJOR_WORK_TYPE = "006";

    /**
     * 功能描述: 主要内容DICT_KINDS
     * @author: guochenghao
     * @date: 2018/11/21 9:22
     */
    public static final String MAJOR_CONTENT_TYPE = "007";

    /**
     * 功能描述: 指标3DICT_KINDS
     * @author: guochenghao
     * @date: 2018/11/21 9:22
     */
    public static final String TARGET3_TYPE = "008";

    /**
     * 功能描述: 指标4DICT_KINDS
     * @author: guochenghao
     * @date: 2018/11/21 9:22
     */
    public static final String TARGET4_TYPE = "009";

    /**
     * @description: 立项单状态值
     * @author: kfzx-guoch
     * @date: 2018/12/11 15:36
     */
    public static final String APPLY_STATE_1 = "1";
    public static final String APPLY_STATE_2 = "2";
    public static final String APPLY_STATE_3 = "3";
    public static final String APPLY_STATE_4 = "4";
    public static final String APPLY_STATE_5 = "5";
    public static final String APPLY_STATE_6 = "6";
    public static final String APPLY_STATE_7 = "7";
    public static final String APPLY_STATE_8 = "8";

    //办理状态 办理中 1
    private final String GAIN_ING_STATE = "1";
    //办理状态 待审核 2
    private final String GAIN_CHECK_STATE ="2";
    //办理状态 已审核 3
    private final String GAIN_DONE_STATE ="3";


    /**
     * @description: 要点类导入模板文件位置
     * @author: kfzx-guoch
     * @date: 2018/12/14 10:12
     */
    public static final String TEMPLATE_PATH = "/oa/imp_template/Template_落实《全国审计机关XXXX年度工作要点》具体措施清单.xls";
    public static final String TEMP_PATH = "/oa/imp_template/落实《全国审计机关XXXX年度工作要点》具体措施清单.xls";

    /**
     * @description: 复制模板的备份文件，防止模板被毁坏
     * @author: guochenghao
     * @date: 2018/12/3 13:31
     */
    public static void copyFileTemplate(HttpServletRequest request){
        FileChannel input = null;
        FileChannel output = null;
        try {
            input = new FileInputStream(request.getRealPath(TEMPLATE_PATH)).getChannel();
            output = new FileOutputStream(request.getRealPath(TEMP_PATH)).getChannel();
            output.transferFrom(input, 0, input.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(input != null){
                    input.close();
                }
                if(output != null){
                    output.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 功能描述: 更新导出Excel文件内容
     * @author: guochenghao
     * @date: 2018/11/16 13:31
     */
    public OutBean updateExcelTemplate() {
        HttpServletRequest request = Context.getRequest();
        copyFileTemplate(request);
        String msgError = "";
        WritableWorkbook book = null;
        try {
            //获得文件
            Workbook wb = Workbook.getWorkbook(new File (request.getRealPath(TEMP_PATH)));
            //打开一个文件的副本，并且指定数据写回到原文件
            book = Workbook.createWorkbook(new File (request.getRealPath(TEMP_PATH)), wb);

            // 获取第二个sheet的数据
            WritableSheet sheet = book.getSheet(1);

            // 准备数据
            UserBean userBean = Context.getUserBean();
            String odept_code = userBean.getStr("ODEPT_CODE");
            OutBean resultBean = getDictEnumListByS_ODEPT(odept_code);

            // 获取所有重点工作类型
            List<Bean> majorWorkEnumList = (List<Bean>)resultBean.get("majorWorkEnumList");
            if(majorWorkEnumList.size() > 0) {
                for(int i = 0; i < majorWorkEnumList.size(); i++) {
                    sheet.addCell(new Label(0, i+1, majorWorkEnumList.get(i).getStr("DICT_NAME")));
                }
            }

            // 获取所有主要内容类型
            List<Bean> majorContentEnumList = (List<Bean>)resultBean.get("majorContentEnumList");
            if(majorContentEnumList.size() > 0) {
                for (int i = 0; i < majorContentEnumList.size(); i++) {
                    sheet.addCell(new Label(1, i + 1, majorContentEnumList.get(i).getStr("DICT_NAME")));
                }
            }

            // 获取所有指标3类型
            List<Bean> target3EnumList = (List<Bean>)resultBean.get("target3EnumList");
            if(target3EnumList.size() > 0) {
                for (int i = 0; i < target3EnumList.size(); i++) {
                    sheet.addCell(new Label(2, i + 1, target3EnumList.get(i).getStr("DICT_NAME")));
                }
            }

            // 获取所有指标4类型
            List<Bean> target4EnumList = (List<Bean>)resultBean.get("target4EnumList");
            if(target4EnumList.size() > 0) {
                for (int i = 0; i < target4EnumList.size(); i++) {
                    sheet.addCell(new Label(3, i + 1, target4EnumList.get(i).getStr("DICT_NAME")));
                }
            }
            if(book!=null) {
                book.write();
                book.close();
            }
        } catch (IOException e) {
            msgError = "读取文件失败，请稍后重试！";
        } catch (RowsExceededException e) {
            msgError = "对应列数加载失败，请稍后重试！";
        } catch (WriteException e) {
            msgError = "更新文件失败，请稍后重试！";
        } catch (BiffException e) {
            msgError = "文件加载失败，请稍后重试！";
        } finally {
            OutBean out = new OutBean();
            out.set(ImpUtils.ERROR_NAME, msgError);
            return out;
        }
    }

    /**
     * 导入方法开始的入口
     */
    public OutBean saveFromExcel(ParamBean paramBean) {
        String fileId = paramBean.getStr("FILE_ID");
        //保存方法入口
        paramBean.set("SERVMETHOD", "savedata");
        Transaction.begin();
        OutBean out = ImpUtils.getDataFromXls(fileId, paramBean);
        Transaction.end();
        String failnum = out.getStr("failernum");
        String successnum = out.getStr("oknum");
        String errorMsg = out.getStr("_MSG_");
        String titleError = out.getStr("titleError");
        if(errorMsg.contains(Constant.RTN_MSG_ERROR)){
            return new OutBean().setError(errorMsg);
        }
        //返回导入结果
        if(Integer.valueOf(failnum) > 0 || !"".equals(titleError.trim())){
            return new OutBean().set("FILE_ID",out.getStr("fileid")).set("_MSG_", "ERROR!!!正确数据："+successnum+"条；错误数据："+(Integer.valueOf(failnum)+1)+"条；请修改后重新导入。");
        } else {
            return new OutBean().set("FILE_ID",out.getStr("fileid")).set("_MSG_", "正确数据："+successnum+"条；错误数据："+failnum+"条；导入成功。请前往我的工作-待办工作中查看待办事项。");
        }
    }

    /**
     * 导入保存方法
     *
     * @param paramBean
     * @return
     */
    public OutBean savedata(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        StringBuffer titleError = new StringBuffer();
        // 获取前端传递参数
        String apprDate  = paramBean.getStr("APPR_DATE"); // 立项时间
        String cueType  = paramBean.getStr("CUE_TYPE"); // 提醒规则类型
        String handleType  = paramBean.getStr("HANDLE_TYPE"); // 办理类型

        // 获取excel公共内容
        List<String> publicList = paramBean.getList("publicList");
        // 联系人姓名
        String lxUser = publicList.get(0);
        if(!lxUser.equals(userBean.getName())){
            titleError.append("联系人不正确!");
        }
        // 工作电话
        String workTel = publicList.get(1);
        // 手机号码
        String phone = publicList.get(2);
        // 校验手机号码格式
        ParamBean isPhoneNumberBean = isPhoneNumber(phone);
        if(!(boolean)isPhoneNumberBean.get("isPhoneNumber")) {
            titleError.append(isPhoneNumberBean.get(ImpUtils.ERROR_NAME));
        }
        String excelTitle = publicList.get(3);// 获取excel表格title

        // 所属省级机关
        String odept_code = userBean.getStr("ODEPT_CODE");
        String odeptName = DictMgr.getName("SY_ORG_DEPT", odept_code);

        // 校验单位
        String unit = publicList.get(4); // 单位
        if(!unit.equals(odeptName)){
            titleError.append("单位不正确！");
        }

        OutBean resultBean = getDictEnumListByS_ODEPT(odept_code);
        // 获取所有重点工作类型
        List<Bean> majorWorkEnumList = (List<Bean>)resultBean.get("majorWorkEnumList");

        // 获取所有主要内容类型
        List<Bean> majorContentEnumList = (List<Bean>)resultBean.get("majorContentEnumList");

        // 获取所有指标3类型
        List<Bean> target3EnumList = (List<Bean>)resultBean.get("target3EnumList");

        // 获取所有指标4类型
        List<Bean> target4EnumList = (List<Bean>)resultBean.get("target4EnumList");

        // 获取所有归口管理司局
        String gkEnumBuf = "SELECT DEPT_CODE,DEPT_NAME FROM SY_ORG_DEPT WHERE DEPT_PCODE = (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE DEPT_NAME = '审计署机关')";
        List<Bean> gkEnumList = Transaction.getExecutor().query(gkEnumBuf);

        // 获取今日导入批次
        String appro_ID = getApproID();

        // 获取立项编号
        Integer item_Num = getItem_Num();

        // 获取扩展字段
        String extend2 = getExtend2(handleType);

        // 获取年份，用于填写立项编号
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        String year = df.format(date);

        // 获取文件内容
        List<Bean> rowBeanList = paramBean.getList("datalist");

        // 创建set用于筛选 重点工作，主要类型，指标3，指标4 的重复项
        HashMap<String, String> map = new HashMap<>();

        // 校验数据 数据全部正确 提交事务
        boolean flag = true;
        StringBuffer errorMsg = null;
        List<ParamBean> returnList  = new ArrayList<>();
        for (int i=0; i < rowBeanList.size(); i++){
            if(!"".equals(titleError.toString().trim())){
                flag = false;
            }

            StringBuffer sb = new StringBuffer();

            Bean rowbean = rowBeanList.get(i);
            errorMsg = new StringBuffer();
            // 重点工作
            String majorWork = rowbean.getStr(ImpUtils.COL_NAME + "2");
            boolean majorWorkFlag = false;
            for(Bean bean : majorWorkEnumList){
                if(bean.getStr("DICT_NAME").equals(majorWork)){
                    majorWorkFlag = true;
                    majorWork = bean.getStr("ID");
                    sb.append(majorWork);
                }
            }
            if (!majorWorkFlag){
                errorMsg.append("重点工作类型不存在！");
                flag = false;
            }
            // 主要内容
            String majorContent = rowbean.getStr(ImpUtils.COL_NAME + "3");
            boolean majorContentFlag = false;
            for(Bean bean : majorContentEnumList){
                if(bean.getStr("DICT_NAME").equals(majorContent)){
                    majorContentFlag = true;
                    majorContent = bean.getStr("ID");
                    sb.append(majorContent);
                }
            }
            if (!majorContentFlag){
                errorMsg.append("主要内容类型不存在！");
                flag = false;
            }
            // 指标3
            String target3 = rowbean.getStr(ImpUtils.COL_NAME + "4");
            if(!StringUtils.isBlank(target3)) {
                boolean target3Flag = false;
                for(Bean bean : target3EnumList){
                    if(bean.getStr("DICT_NAME").equals(target3)){
                        target3Flag = true;
                        target3 = bean.getStr("ID");
                        sb.append(target3);
                    }
                }
                if (!target3Flag){
                    errorMsg.append("指标3类型不存在！");
                    flag = false;
                }
            }
            // 指标4
            String target4 = rowbean.getStr(ImpUtils.COL_NAME + "5");
            if(!StringUtils.isBlank(target4)) {
                boolean target4Flag = false;
                for (Bean bean : target4EnumList) {
                    if (bean.getStr("DICT_NAME").equals(target4)) {
                        target4Flag = true;
                        target4 = bean.getStr("ID");
                        sb.append(target4);
                    }
                }
                if (!target4Flag) {
                    errorMsg.append("指标4类型不存在！");
                    flag = false;
                }
            }

            if(map.containsKey(sb.toString())){
                map.put(sb.toString(), map.get(sb.toString()) + "," + (i+1));
            } else {
                map.put(sb.toString(), (i+1)+"");
            }
            String factOperate = rowbean.getStr(ImpUtils.COL_NAME + "6"); // 具体落实措施
            String schedulePlan = rowbean.getStr(ImpUtils.COL_NAME + "7"); // 进度安排
            // 完成时限 校验时间是否已经过期
            String limitDate = rowbean.getStr(ImpUtils.COL_NAME + "8");
            boolean limitDateFlag = checkDate(limitDate);
            if (!limitDateFlag) {
                errorMsg.append("完成时限必须大于当前工作日！");
                flag = false;
            }
            String liableOffice = rowbean.getStr(ImpUtils.COL_NAME + "9"); // 责任处室
            String liableUser = rowbean.getStr(ImpUtils.COL_NAME + "10"); // 负责人
            String gkName = rowbean.getStr(ImpUtils.COL_NAME + "11"); // 归口管理司局
            OutBean checkBean = checkLiable(new ParamBean()
                    .set("ODEPT_CODE", odept_code)
                    .set("LIABLE_OFFICE", liableOffice)
                    .set("LIABLE_USER", liableUser)
                    .set("GK_NAME", gkName));
            if("true".equals(checkBean.getStr("ERROR"))){
                errorMsg.append(checkBean.get("_MSG_"));
                flag = false;
            }

            if(!"".equals(errorMsg.toString().trim())) {
                rowbean.set(ImpUtils.ERROR_NAME, errorMsg.toString().trim());
            } else {
                ParamBean bean = new ParamBean();
                bean.set("APPR_DATE", apprDate);
                bean.set("CUE_TYPE", cueType);
                bean.set("HANDLE_TYPE", handleType);
                bean.set("USER_NAME", lxUser);
                bean.set("USER_TEL", workTel);
                bean.set("USER_PHONE", phone);
                bean.set("MAJOR_WORK", majorWork);
                bean.set("MAJOR_CONTENT", majorContent);
                bean.set("TARGET3", target3);
                bean.set("TARGET4", target4);
                bean.set("FACT_OPERATE", factOperate);
                bean.set("SCHEDULE_PLAN", schedulePlan);
                bean.set("LIMIT_DATE", limitDate);
                bean.set("LIABLE_OFFICE", liableOffice);
                bean.set("LIABLE_USER", liableUser);
                bean.set("PROVIN_NAME", odeptName);
                bean.set("DEPT_CODE", odept_code);
                bean.set("CENTRALIED_MGR_BUREAU", gkName);
                bean.set("APPRO_ID", appro_ID);
                bean.set("TITLE", excelTitle);
                bean.set("ITEM_NUM", "省立 "+ year +"（" + (item_Num + i) + "）号");
                // 获取 系统编码
                String SCode = getS_code((item_Num + i));
                bean.set("S_CODE", SCode);
                bean.set("APPLY_STATE", APPLY_STATE_1);
                // 设置 扩展字段
                bean.set("NEXT_GAIN_DATE", extend2);
                bean.set("S_TNAME", odeptName);
                bean.set("S_UNAME", lxUser);
                returnList.add(bean);
            }
        }

        // 修改逻辑 校验 这四项指标在数据库中是否已存在相同的记录
        if(map.size() < rowBeanList.size()) {
            for (String str : map.keySet()) {
                titleError.append(map.get(str) + "行数据重复！");
            }
            flag = false;
        }

        // 批量插入数据
        // flag 为 true 表示 数据全部正确，false表示有错误数据存在，不进行批量插入
        if (flag) {
            // 批量保存
            for (ParamBean bean : returnList) {
                bean.setServId("OA_SUP_APPRO_POINT");
                save(bean);
            }
        }
        OutBean outBean = new OutBean();
        outBean.set("alllist", rowBeanList).set("successlist", returnList).set("flag", flag);
        outBean.set("titleError", titleError.toString());
        return outBean;
    }

    /**
     * 功能描述: 校验日期是否过期（完成时限在今日之前即为过期）
     * @author: guochenghao
     * @date: 2018/11/21 14:03
     */
    private static boolean checkDate(String limitDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(limitDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date.getTime() > System.currentTimeMillis()){
            return true;
        }
        return false;
    }

    /**
     * 功能描述: 校验手机号
     * @author: guochenghao
     * @date: 2018/11/21 9:14
     */
    public static ParamBean isPhoneNumber(String phone) {
        ParamBean bean = new ParamBean();
        if (phone.length() != 11) {
            bean.set(ImpUtils.ERROR_NAME, "手机号码应为11位数");
            bean.set("isPhoneNumber", false);
            return bean;
        } else {
            String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            if (!isMatch) {
                bean.set(ImpUtils.ERROR_NAME, "请填入正确的手机号码");
                bean.set("isPhoneNumber", false);
                return bean;
            }
        }
        bean.set("isPhoneNumber", true);
        return bean;
    }

    /**
     * 功能描述: 获取要点类今日批次编号
     * @author: guochenghao
     * @date: 2018/11/20 17:03
     */
    private String getApproID() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String today = df.format(date);
        String importCountSql = "SELECT APPRO_ID as todayCount FROM SUP_APPRO_POINT where S_ATIME > '" + today + "' GROUP BY APPRO_ID";
        List<Bean> importCountList = Transaction.getExecutor().query(importCountSql);
        String pre = today.replace("-", "");
        if(importCountList.size() == 0){
            pre = pre + "01";
        } else if (0 < importCountList.size() && importCountList.size() < 10) {
            pre = pre + "0" + (importCountList.size()+1);
        } else {
            pre = pre + (importCountList.size()+1);
        }
        return pre;
    }

    /**
     * 功能描述: 获取立项编号
     * @author: guochenghao
     * @date: 2018/11/29 15:22
     */
    public Integer getItem_Num() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        String year = df.format(date);
        String importCountSql = "SELECT count(1) as todayCount FROM SUP_APPRO_POINT where S_ATIME > '" + year + "'";
        List<Bean> importCountList = Transaction.getExecutor().query(importCountSql);
        int item_Num = 0;
        if(importCountList.get(0).get("TODAYCOUNT") != null){
            item_Num = (int)importCountList.get(0).get("TODAYCOUNT") + 1;
        }
        return item_Num;
    }

    /**
     * 功能描述: 获取系统编号
     * @author: guochenghao
     * @date: 2018/12/10 15:18
     */
    public String getS_code(Integer item_Num) {
        String currentDate = DateUtils.getDate();//系统当前时间
        // 生成系统编号
        String time = DateUtils.getTime();
        String timeCode = time.substring(0, 5);
        String sCode = "SUP" + currentDate.replace("-", "") + timeCode.replace(":", "") + "000" + item_Num;
        return sCode;
    }

    /**
     * @description: 在保存信息前批量保存title
     * @author: guochenghao
     * @date: 2018/12/1 15:56
     */
    @Override
    protected void beforeSave(ParamBean paramBean) {
        // 获取title
        String title = paramBean.getStr("TITLE");
        if (paramBean.get("_OLDBEAN_") != null) {
            if (StringUtils.isNotBlank(title)) {
                HashMap<String, String> map = (HashMap<String, String>) paramBean.get("_OLDBEAN_");
                // 获取批次号
                String appro_id = map.get("APPRO_ID");
                // 同步更新同批次号的所有数据的title
                String updatePointSql = "UPDATE SUP_APPRO_POINT SET TITLE = '" + title + "' WHERE APPRO_ID = '" + appro_id + "' ";
                Transaction.getExecutor().execute(updatePointSql);
                // 同步更新 todo表中对应的 所有数据的title
                String pointSql = "SELECT ID FROM SUP_APPRO_POINT WHERE APPRO_ID = '" + appro_id + "'";
                List<Bean> list = Transaction.getExecutor().query(pointSql);
                for(Bean b : list){
                    String updateTodoSql = "UPDATE SY_COMM_TODO SET TODO_TITLE = '要点类督查_" +title + "' WHERE TODO_OBJECT_ID1 = '" + b.get("ID") + "'";
                    Transaction.getExecutor().execute(updateTodoSql);
                }
            }
        }
    }

    /**
     * @description: 校验责任处室，责任人，归口管理司局
     * @author: guochenghao
     * @date: 2018/12/3 15:56
     */
    public OutBean checkLiable(ParamBean paramBean) {
        OutBean returnBean = new OutBean();
        String oDeptCode = paramBean.getStr("ODEPT_CODE"); // 省级机关 机构id
        String liable_office = paramBean.getStr("LIABLE_OFFICE"); // 责任处室
        String liable_user = paramBean.getStr("LIABLE_USER"); // 责任人
        String gkName = paramBean.getStr("GK_NAME"); // 归口管理司局
        // 校验责任处室
        OutBean officeResult = checkLiableOffice(oDeptCode, liable_office);
        if(officeResult .size() == 0){
            returnBean.set("ERROR", true);
            returnBean.set("_MSG_", "责任处室不存在！");
        } else {
            // 校验责任人
            boolean flag = checkLiableUser(liable_user, officeResult.getStr("dept_code"));
            if(flag){
                returnBean.set("ERROR", true);
                returnBean.set("_MSG_", "责任人不存在！");
            } else {
                // 获取所有归口管理司局
                String gkEnumBuf = "SELECT DEPT_CODE,DEPT_NAME FROM SY_ORG_DEPT WHERE DEPT_PCODE = (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE DEPT_NAME = '审计署机关')";
                List<Bean> gkEnumList = Transaction.getExecutor().query(gkEnumBuf);
                if(!StringUtils.isBlank(gkName)) {
                    boolean gkNameFlag = false;
                    for (Bean bean : gkEnumList) {
                        if (bean.getStr("DEPT_NAME").equals(gkName)) {
                            gkNameFlag = true;
                        }
                    }
                    if (gkNameFlag == false) {
                        returnBean.set("ERROR", true);
                        returnBean.set("_MSG_", "归口管理司局不存在！");
                    }
                }
            }
        }
        return returnBean;
    }

    /**
     * @description: 校验责任处室
     * @author: guochenghao
     * @date: 2018/12/4 14:32
     */
    public OutBean checkLiableOffice(String oDeptCode, String deptName){
        String odeptName = DictMgr.getName("SY_ORG_DEPT", oDeptCode); // 填报省厅
        Map<String, String> deptNameMap = getDeptNamesMap(oDeptCode, odeptName);
        OutBean outBean = new OutBean();
        for(String str : deptNameMap.keySet()){
            if(deptNameMap.get(str).equals(deptName)){
                outBean.set("dept_code", str);
            }
        }
        return outBean;
    }

    /**
     * @description: 校验责任人
     * @author: guochenghao
     * @date: 2018/12/4 14:32
     */
    public boolean checkLiableUser(String liableUser, String deptCode){
        // 创建list保存部门名称
        Map<String, String> deptNameMap = new HashMap<>();
        // 递归获取机构树
        deptNameMap.put(deptCode, "");
        deptNameMap = recursiveDeptCode(deptCode, deptNameMap);

        // 根据deptCode获取其下所有的用户
        List<String> userList = new ArrayList<>();
        for(String str : deptNameMap.keySet()){
            String sql = "SELECT USER_NAME FROM SY_ORG_USER WHERE DEPT_CODE = '" + str + "'";
            List<Bean> userTempList = Transaction.getExecutor().query(sql);
            for(Bean bean : userTempList){
                userList.add(bean.getStr("USER_NAME"));
            }
        }
        if(userList.contains(liableUser)){
            return false;
        }
        return true;
    }

    /**
     * @description: 获取当前用户所在的机构的所有部门
     * @author: guochenghao
     * @date: 2018/12/4 14:17
     */
    public Map<String, String> getDeptNamesMap(String  odept_code,String odeptName){
        // 创建list保存部门名称
        Map<String, String> deptNameMap = new HashMap<>();
        // 添加顶级机构
        deptNameMap.put(odept_code, odeptName);
        // 递归获取机构树
        deptNameMap = recursiveDeptCode(odept_code, deptNameMap);
        return deptNameMap;
    }

    /**
     * @description: 递归获取机构树
     * @author: guochenghao
     * @date: 2018/12/4 14:17
     */
    public Map<String, String> recursiveDeptCode(String odept_code, Map<String, String> deptNameMap){
        String sql = "SELECT DEPT_CODE,DEPT_NAME FROM SY_ORG_DEPT WHERE DEPT_PCODE = '" + odept_code + "'";
        List<Bean> deptList = Transaction.getExecutor().query(sql);
        if(deptList.size() == 0){
            return deptNameMap;
        } else {
            for(Bean bean : deptList){
                deptNameMap.put(bean.getStr("DEPT_CODE"), bean.getStr("DEPT_NAME"));
                deptNameMap = recursiveDeptCode(bean.getStr("DEPT_CODE"), deptNameMap);
            }
            return deptNameMap;
        }
    }

    /**
     * @description: afterSendToNode
     * 批量操作 调用此接口
     * @author: kfzx-guoch
     * @date: 2018/12/19 19:13
     */
    public void afterSendToNode(ParamBean paramBean){
        updateApplyState(paramBean);
    }


    /**
     * @description: 更新立项单状态
     * @author: kfzx-guoch
     * @date: 2018/12/11 15:29
     */
    public void updateApplyState(ParamBean paramBean){
        String currentNodeCode = paramBean.getStr("currNodeCode");
        String nodeCode = paramBean.getStr("nextNodeCode");
        String ID = paramBean.getStr("approId");
        String flag = currentNodeCode + nodeCode;
        String state = "";
        switch (flag) {
            case "N1N2": {
                state = APPLY_STATE_2;
                break;
            }
            case "N23N3": {
                state = APPLY_STATE_5;
                break;
            }
            case "N3N32":{
                // 填写完成时间，逾期天数，TODO 主办单位办理情况更新时间（等吴滔对接）
                addFinishTimeAndOverdueDay(ID);
                break;
            }
            case "N3N24":{
                // 如果督察员直接进行督查办理，则需要补填写完成时间，逾期天数, TODO 主办单位办理情况更新时间（等吴滔对接）
                updateFinishTimeAndOverdueDay(ID);
                break;
            }
            case "N3N23": {
                state = APPLY_STATE_2;
                break;
            }
            case "N3N34": {
                state = APPLY_STATE_6;
                // 填写办结时间
                updateDealtTime(ID);
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                // 修改办理情况的状态
                updateGainState(ID, GAIN_ING_STATE, GAIN_CHECK_STATE);
                break;
            }
            case "N34N3": {
                state = APPLY_STATE_5;
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
            case "N3N25":{
                state = APPLY_STATE_6;
                // 填写办结时间
                updateDealtTime(ID);
                break;
            }
            case "N27N28": {
                state = APPLY_STATE_7;
                break;
            }
            case "N35N3": {
                state = APPLY_STATE_5;
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
            case "N35N34": {
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
            case "N28N3": {
                state = APPLY_STATE_6;
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
            case "N25N3": {
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
            case "N26N3": {
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
            case "N27N3": {
                // 修改待办状态为 督查办结
                updateToDoOperation(ID);
                break;
            }
        }
        if(StringUtils.isNotEmpty(state)){
            Transaction.getExecutor().execute("UPDATE " + SUP_APPRO_POINT + " SET APPLY_STATE = '" + state + "' WHERE ID = '" + ID + "' ");
        }
    }

    /**
     * @description: 更新办理情况的状态
     * @author: kfzx-guoch
     * @date: 2018/12/25 18:57
     */
    private void updateGainState(String approId, String curState, String upState) {
        ParamBean paramBean = new ParamBean();
        paramBean.set("approId", approId);
        paramBean.set("curState", curState);
        paramBean.set("upState", upState);
        new SupApproGain().updateWfState(paramBean);
    }

    /**
     * @description: 更新待办表的 当前操作环节
     * @author: kfzx-guoch
     * @date: 2018/12/22 13:54
     */
    public void updateToDoOperation(String id) {
        Transaction.getExecutor().execute("UPDATE SY_COMM_TODO SET TODO_OPERATION = '督查办结' WHERE TODO_OBJECT_ID1 = '" + id + "' AND TODO_CODE_NAME != '分发'");
    }

    /**
     * @description: 督查办理，填写完成时间，逾期天数
     * @author: kfzx-guoch
     * @date: 2018/12/19 19:24
     */
    public void addFinishTimeAndOverdueDay(String ID){
        String sql = "SELECT * FROM SUP_APPRO_POINT WHERE ID = '" + ID + "'";
        Bean point = Transaction.getExecutor().query(sql).get(0);
        point.set("FINISH_TIME", LocalDate.now());
        String limitDate = point.getStr("LIMIT_DATE");
        SupApperUrge supApperUrge = new SupApperUrge();
        String day = supApperUrge.getWorkDay(supApperUrge.getYDM(new Date()), limitDate);
        if(Integer.valueOf(day) <= 0){
            point.set("OVERDUE_DAY", 0);
        } else {
            point.set("OVERDUE_DAY", day);
        }
        ServDao.update("OA_SUP_APPRO_POINT", point);
    }

    /**
     * @description: 如果督察员直接进行督查办理，则需要补填写完成时间，逾期天数
     * @author: kfzx-guoch
     * @date: 2018/12/24 11:12
     */
    public void updateFinishTimeAndOverdueDay(String ID){
        String sql = "SELECT * FROM SUP_APPRO_POINT WHERE ID = '" + ID + "'";
        Bean point = Transaction.getExecutor().query(sql).get(0);
        if(point.getStr("FINISH_TIME") == null || "".equals(point.getStr("FINISH_TIME"))){
            point.set("FINISH_TIME", LocalDate.now());
        }
        String limitDate = point.getStr("LIMIT_DATE");
        SupApperUrge supApperUrge = new SupApperUrge();
        String day = supApperUrge.getWorkDay(supApperUrge.getYDM(new Date()), limitDate);
        if(Integer.valueOf(day) <= 0){
            point.set("OVERDUE_DAY", 0);
        } else {
            point.set("OVERDUE_DAY", day);
        }
        ServDao.update("OA_SUP_APPRO_POINT", point);
    }

    /**
     * @description: 督查办结，填写办结时间
     * @author: kfzx-guoch
     * @date: 2018/12/19 19:24
     */
    public void updateDealtTime(String ID){
        String sql = "SELECT * FROM SUP_APPRO_POINT WHERE ID = '" + ID + "'";
        Bean point = Transaction.getExecutor().query(sql).get(0);
        point.set("DEALT_TIME", LocalDate.now());
        ServDao.update("OA_SUP_APPRO_POINT", point);
    }

    /**
     * @description: 获取字典项值
     * 重新给字典项赋值，解决跨机构字典项值不同的问题
     * @author: kfzx-guoch
     * @date: 2018/12/13 11:14
     */
    public OutBean getDictEnumList(ParamBean paramBean) {
        String ID = paramBean.getStr( "ID");
        String sql = "SELECT * FROM " + SUP_APPRO_POINT + " WHERE ID = '" + ID + "'";
        Bean pointBean = Transaction.getExecutor().query(sql).get(0);
        String sOdept = pointBean.getStr("S_ODEPT");
        return getDictEnumListByS_ODEPT(sOdept);
    }

    /**
     * @description: 根据 sOdept 获取字典项列表
     * @author: kfzx-guoch
     * @date: 2018/12/19 19:38
     */
    public OutBean getDictEnumListByS_ODEPT(String sOdept){
        SupServDict supServDict = new SupServDict();
        // 获取提醒规则类型
        List<Bean> cueTypeEnumList = (List<Bean>)supServDict.queryDicts(new ParamBean().set("ODEPT_CODE", sOdept).set("DICT_KINDS", NOTIFY_RULE_TYPE)).get("list");
        // 获取办理规则类型
        List<Bean> handleTypeEnumList = (List<Bean>)supServDict.queryDicts(new ParamBean().set("ODEPT_CODE", sOdept).set("DICT_KINDS", HANDLE_TYPE)).get("list");
        // 获取重点工作类型
        List<Bean> majorWorkEnumList = (List<Bean>)supServDict.queryDicts(new ParamBean().set("ODEPT_CODE", sOdept).set("DICT_KINDS", MAJOR_WORK_TYPE)).get("list");
        // 获取主要内容类型
        List<Bean> majorContentEnumList = (List<Bean>)supServDict.queryDicts(new ParamBean().set("ODEPT_CODE", sOdept).set("DICT_KINDS", MAJOR_CONTENT_TYPE)).get("list");
        // 获取指标3类型
        List<Bean> target3EnumList = (List<Bean>)supServDict.queryDicts(new ParamBean().set("ODEPT_CODE", sOdept).set("DICT_KINDS", TARGET3_TYPE)).get("list");
        // 获取指标4类型
        List<Bean> target4EnumList = (List<Bean>)supServDict.queryDicts(new ParamBean().set("ODEPT_CODE", sOdept).set("DICT_KINDS", TARGET4_TYPE)).get("list");
        OutBean outBean = new OutBean();
        outBean.set("cueTypeEnumList", cueTypeEnumList);
        outBean.set("handleTypeEnumList", handleTypeEnumList);
        outBean.set("majorWorkEnumList", majorWorkEnumList);
        outBean.set("majorContentEnumList", majorContentEnumList);
        outBean.set("target3EnumList", target3EnumList);
        outBean.set("target4EnumList", target4EnumList);
        return outBean;
    }

    /**
     * @description: 获取扩展字段
     * @author: kfzx-guoch
     * @date: 2018/12/13 18:42
     */
    public String getExtend2(String handleTypeID) {
        String sql = "SELECT * FROM " + SUP_SERV_DICT + " WHERE ID = '" + handleTypeID + "'";
        Bean dictBean = Transaction.getExecutor().query(sql).get(0);
        return dictBean.getStr("EXTEND2");
    }

    /**
     * @description: 获取当前主单 是督查办理还是督查办结
     * @author: kfzx-guoch
     * @date: 2018/12/22 15:15
     */
    public OutBean getTodoOperation(ParamBean paramBean) {
        String ID = paramBean.getStr("ID");
        String sql = "SELECT * FROM SY_COMM_TODO WHERE TODO_OBJECT_ID1 = '" + ID + "' AND TODO_CODE_NAME != '分发'";
        List<Bean> result = Transaction.getExecutor().query(sql);
        String todoOperation = "";
        if(result != null && result.size() > 0){
            todoOperation = result.get(0).getStr("TODO_OPERATION");
        }
        return new OutBean().set("todoOperation", todoOperation);
    }

}
