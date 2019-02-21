package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.ConfMgr;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import jxl.*;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.write.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 导入工具类
 * Created by shenh on 2017/12/20.
 */
public class ImpUtils {

    private static Log log = LogFactory.getLog(ImpUtils.class);

    public final static String COL_NAME = "col";

    public final static String ERROR_NAME = "error";

    public final static String PUBLIC_LIST = "publicList";

    public final static String COL_TITLE_LIST = "colTitleList";

    public final static String DATA_LIST = "datalist";

    public final static String ALL_LIST = "alllist";

    public final static String SUCCESS_LIST = "successlist";

    //服务id标识符
    public final static String SERV_ID = "SERV_ID";

    //服务id对应类方法名称
    public final static String SERV_METHOD_NAME = "SERVMETHOD";

    public final static String OK_NUM = "oknum";
    public final static String FAIL_NUM = "failernum";
    public final static String FILE_ID = "fileid";

    // 要点类导入对应的服务ID
    private final static String POINT_ID = "OA_SUP_APPRO_POINT";

    // 署发 导入对应的服务ID
    private final static String OFFICE_ID = "OA_SUP_APPRO_OFFICE";


    //单个文件导入最大条数显示 (已经移入方法内)
//    public final static int TS_IMP_EXCEL_MAX_NUM = ConfMgr.getConf("TS_IMP_EXCEL_MAX_NUM",20000);

    /**
     * 在excel中设置失败信息，返回fileId
     *
     * @param rowBeanList 通过getDataFromXls返回的rowBeanList
     * @return errorFileId
     * @throws WriteException 写入失败
     */
    public static void saveErrorAndReturnErrorFile(int i, WritableSheet wSheet, Sheet sheet, List<Bean> rowBeanList, String titleError) throws WriteException {
        int cols = sheet.getColumns();

        WritableFont font1 = new WritableFont(WritableFont.COURIER);
        font1.setColour(Colour.GREEN);
        WritableCellFormat format1 = new WritableCellFormat(font1);
        format1.setAlignment(Alignment.CENTRE);

        if(StringUtils.isEmpty(titleError)){
            // 正确
            Label label1 = new Label(cols, 1, "信息正确", format1);
            wSheet.addCell(label1);
            Label label2 = new Label(cols + 1, 1,titleError, format1);
            wSheet.addCell(label2);
        } else {
            // 错误
            font1.setColour(Colour.RED);
            Label label = new Label(cols, 1, "信息错误", format1);
            wSheet.addCell(label);
            Label label2 = new Label(cols + 1, 1,titleError, format1);
            wSheet.addCell(label2);
        }

        for (int j = 0; j < rowBeanList.size(); j++) {
            Bean bean = rowBeanList.get(j);
            // 设置写入格式
            WritableFont font = new WritableFont(WritableFont.COURIER);
            font.setColour(Colour.GREEN);
            WritableCellFormat format = new WritableCellFormat(font);
            format.setAlignment(Alignment.CENTRE);

            if (StringUtils.isEmpty(bean.getStr(ERROR_NAME))){ // 导入成功的信息
                Label label = new Label(cols, i + j - rowBeanList.size() + 1, "成功", format);
                wSheet.addCell(label);
                if(!bean.getStr("successinfo").equals("")){
                    Label label2 = new Label(cols + 1, i + j - rowBeanList.size() + 1, bean.getStr("successinfo"), format);
                    wSheet.addCell(label2);
                }
            } else {  //  导入失败的信息
                font.setColour(Colour.RED);
                Label label = new Label(cols, i + j - rowBeanList.size() + 1, "失败", format);
                wSheet.addCell(label);
                Label label2 = new Label(cols + 1, i + j - rowBeanList.size() + 1, bean.getStr(ERROR_NAME), format);
                wSheet.addCell(label2);
            }
        }

    }

    /**
     *    将导入后的结果信息 写入到 Excel里面
     * @param i
     * @param wSheet
     * @param sheet
     * @param rowBeanList
     * @throws WriteException
     */
    public static void saveErrorAndReturnErrorOfficeFile(int i, WritableSheet wSheet, Sheet sheet, List<Bean> rowBeanList) throws WriteException {
        int cols = sheet.getColumns();

        for (int j = 0; j < rowBeanList.size(); j++) {
            Bean bean = rowBeanList.get(j);
            // 设置写入格式
            WritableFont font = new WritableFont(WritableFont.COURIER);
            font.setColour(Colour.GREEN);
            WritableCellFormat format = new WritableCellFormat(font);
            format.setAlignment(Alignment.CENTRE);

            if (StringUtils.isEmpty(bean.getStr(ERROR_NAME))){ // 导入成功的信息
                Label label = new Label(cols, i + j - rowBeanList.size() + 1, "成功", format);
                wSheet.addCell(label);
                if(!bean.getStr("successinfo").equals("")){
                    Label label2 = new Label(cols + 1, i + j - rowBeanList.size() + 1, bean.getStr("successinfo"), format);
                    wSheet.addCell(label2);
                }
            } else {  //  导入失败的信息
                font.setColour(Colour.RED);
                Label label = new Label(cols, i + j - rowBeanList.size() + 1, "失败", format);
                wSheet.addCell(label);
                Label label2 = new Label(cols + 1, i + j - rowBeanList.size() + 1, bean.getStr(ERROR_NAME), format);
                wSheet.addCell(label2);
            }
        }

    }

    /**
     * @param oldFileBean
     * @param tempFile
     * @return
     */
    public static Bean saveTempFile(Bean oldFileBean, TempFile tempFile) {
        try {
            Bean param = new Bean();
            param.set("SERV_ID", oldFileBean.getStr("SERV_ID"));
            param.set("FILE_CAT", "TEMP_EXCEL_IMP");
            param.set("DATA_ID", oldFileBean.getStr("DATA_ID"));
            param.set("FILE_NAME", oldFileBean.getStr("FILE_NAME"));
            param.set("FILE_MTYPE", oldFileBean.getStr("FILE_MTYPE"));
            param.set("S_FLAG", 2);
            return FileMgr.upload(param, tempFile.openNewInputStream());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            tempFile.destroy();
        }
        return null;
    }


    /**
     * 通过fileId获取第一个sheet的内容
     *
     * @param fileId 文件id
     * @return outBean  rowBeans(List<Bean>)
     * @throws TipException 文件不存在，请重试 / Excel文件解析错误，请校验！
     */
    public static OutBean getDataFromXls(String fileId, ParamBean paramBean) {
        String xmId = paramBean.getStr("XM_ID");
        //是否存在未完成的导入操作（同一个项目）
        if(impInSameTime(paramBean)) {
           return new OutBean().setError("存在未完成的导入操作,请稍后再次尝试！");
        }
        // 创建返回bean
        OutBean resultOutBean = new OutBean();

        int successnum = 0;
        int failernum = 0;
        Boolean flag = true;

        String servId = paramBean.getStr(SERV_ID);
        if (StringUtils.isBlank(servId)) {
            servId = paramBean.getServId();//服务名
        }

        String timestamp = paramBean.getStr("TIMESTAMP");
        ServDefBean serv = ServUtils.getServDef(servId);
        LinkedHashMap<String, Bean> titleMap = BeanUtils.toLinkedMap(serv.getTableItems(), "ITEM_NAME");

        long aTimeL = Long.parseLong(timestamp);

        if (StringUtils.isBlank(timestamp)) {
            aTimeL = System.currentTimeMillis();
        }

        //导入前做标记 - 导入开始
        ImpExpRecordUtils.save(serv.getName(), ImpExpRecordUtils.RecordType.IMP_TYPE, servId, xmId, "", "ing", "", aTimeL);
//        TsObjectUtils.save(paramBean.getServId(), paramBean.getStr("TIMESTAMP"), TsObjectServ.ING,"");

        try {
            String method = paramBean.getStr(SERV_METHOD_NAME);//方法名
            InputStream in = null;
            OutputStream os = null;
            TempFile tempFile = null;
            Bean fileBean = null;
            Bean newFileBean = null;
            try {
                fileBean = FileMgr.getFile(fileId);
                in = FileMgr.download(fileBean);
            } catch (Exception e) {
                throw new TipException("文件不存在，请重试");
            }
            Workbook workbook = null;
            WritableWorkbook wbook = null;
            try {
                Sheet sheet;
                WritableSheet wSheet;
                try {
                    WorkbookSettings workbookSetting = new WorkbookSettings();
                    workbookSetting.setCellValidationDisabled(true);
                    workbook = Workbook.getWorkbook(in, workbookSetting);
                    sheet = workbook.getSheet(0);

                    tempFile = new TempFile(TempFile.Storage.SMART);
                    os = tempFile.getOutputStream();

                    log.debug("imp---->open file 1");
                    WorkbookSettings settings = new WorkbookSettings();
                    settings.setWriteAccess(null);
                    wbook = Workbook.createWorkbook(os, workbook,settings);
                    wSheet = wbook.getSheet(0);
                } catch (Exception e) {
                    throw new TipException("Excel文件解析错误，请校验！");
                }

                int rows = getRightRows(sheet);//去除空行的行数
                int tsImpExcelMaxNum = ConfMgr.getConf("TS_IMP_EXCEL_MAX_NUM", 20000);
                //若导入的数据大于阈值，禁止导入
                if (rows > tsImpExcelMaxNum) {
                    //单次导入数据最大限制
                    resultOutBean.setError("当前导入条数：" + rows + "条，单次导入数据最大限制 " + tsImpExcelMaxNum + "条，请调整文件后重新导入！");
                } else {
                    // 要点类模板 解析
                    if(serv.getId().equals(POINT_ID)){
                        OutBean analyOut =analysisPointExcel(sheet, paramBean,  titleMap, serv,rows, servId,method,resultOutBean,wSheet);
                        successnum = analyOut.getInt("successnum");
                        failernum = analyOut.getInt("failernum");
                        flag = analyOut.getBoolean("flag");
                    }else if(serv.getId().equals(OFFICE_ID)){  // 署发模板 解析
                        OutBean analyOut =analysisOfficeExcel(sheet, paramBean,  titleMap, serv,rows, servId,method,wSheet);
                        successnum = analyOut.getInt("successnum");
                        failernum = analyOut.getInt("failernum");
                        flag = analyOut.getBoolean("flag");
                    }
                }

                wbook.write();
                wbook.close();

                fileBean.set("FILE_NAME", fileBean.getStr("DIS_NAME") + "-导入结果.xls");
                newFileBean = saveTempFile(fileBean, tempFile);

            } catch (WriteException e) {
                throw new TipException("导入结果填写错误");
            } catch (IOException e) {
                throw new TipException("导入结果填写错误");
            } finally {
                //关闭文件
                if (workbook != null) {
                    workbook.close();
                }
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(os);
                FileMgr.deleteFile(fileId);
                if (tempFile != null) {
                    tempFile.destroy();
                }
            }

            //导入完变更ts_object记录
            OutBean saveObjectOutBean;
            if (resultOutBean.getMsg().contains(Constant.RTN_MSG_ERROR)) {
                saveObjectOutBean = resultOutBean;
            } else {
                resultOutBean.set(OK_NUM, successnum).set(FAIL_NUM, failernum).set(FILE_ID, newFileBean.getId()).set("flag",flag);
                if(failernum > 0 || !"".equals(resultOutBean.get("titleError"))){
                    saveObjectOutBean = new OutBean().set("FILE_ID", newFileBean.getId()).setMsg("正确数据：" + successnum + "条；错误数据：" + (Integer.valueOf(failernum)+1) + "条；请修改后重新导入！");
                } else {
                    saveObjectOutBean = new OutBean().set("FILE_ID", newFileBean.getId()).setMsg("正确数据：" + successnum + "条；错误数据：" + failernum + "条；导入成功！");
                }
            }

            ImpExpRecordUtils.save(fileBean.getStr("FILE_NAME"), ImpExpRecordUtils.RecordType.IMP_TYPE, servId, xmId, newFileBean.getId(), "end", JsonUtils.toJson(saveObjectOutBean), aTimeL);

         } catch (TipException tipE) {
            resultOutBean.setError(tipE.getMessage());

            //导入异常 修改 ImpExpRecordUtils值
            ImpExpRecordUtils.save(serv.getName(), ImpExpRecordUtils.RecordType.IMP_TYPE, servId, xmId, "", "end", JsonUtils.toJson(resultOutBean), aTimeL);
            throw tipE;
        }catch (Exception e) {

            resultOutBean.setError("导入程序错误");

            //导入异常 修改 ImpExpRecordUtils值
            ImpExpRecordUtils.save(serv.getName(), ImpExpRecordUtils.RecordType.IMP_TYPE, servId, xmId, "", "end", JsonUtils.toJson(resultOutBean), aTimeL);

            e.printStackTrace();

            throw new TipException("导入文件不正确，导入失败！");
        }

//        TsObjectUtils.save(paramBean.getServId(), paramBean.getStr("TIMESTAMP"),"end", JsonUtils.toJson(saveObjectOutBean));

        return resultOutBean;
    }


    /**
     *      通过不同的模板 解析Excel方式不同
     * @param sheet    模板sheet
     * @param paramBean   界面参数
     * @param titleMap    模板列表头
     * @param serv       服务
     * @param rows      有效行数
     * @param servId    服务ID
     * @param method    对应保存方法
     * @param resultOutBean   返回结果
     * @param wSheet     导入结果返回Excel的sheet
     * @return
     * @throws WriteException
     */
    public static OutBean analysisPointExcel(Sheet sheet, ParamBean paramBean, LinkedHashMap<String, Bean> titleMap, ServDefBean serv,
                              int rows, String servId,String method,OutBean resultOutBean,WritableSheet wSheet) throws WriteException {
        OutBean outBean = new OutBean();
        int successnum = 0;
        int failernum = 0;
        Boolean flag = true;

        //未超出限制 开始解析表格
        List<Bean> result = new ArrayList<>();

        //列标题
        Cell[] titleCells = sheet.getRow(2);
        //列数
        int cols = titleCells.length;
        Bean[] itemMaps = new Bean[cols];

        // 获取excel表格title
        String excelTitle = sheet.getCell(0,0).getContents();

        // excel第一行公共信息
        String unit = sheet.getCell(1,1).getContents(); // 单位
        String lxUser = sheet.getCell(4,1).getContents(); // 联系人
        String workTel = sheet.getCell(7,1).getContents(); // 工作电话
        String phone = sheet.getCell(10,1).getContents(); // 手机
        List<String> publicList = new ArrayList<>();
        publicList.add(lxUser);
        publicList.add(workTel);
        publicList.add(phone);
        publicList.add(excelTitle);
        publicList.add(unit);
        paramBean.set(PUBLIC_LIST, publicList);

        //表头字段
        for (int i = 0; i < cols; ++i) {
            String title = sheet.getCell(i, 2).getContents();
            if (StringUtils.isNotEmpty(title)) {
                if (title.contains("*")) {
                    //替换掉模板中的必输标志*
                    title = StringUtils.replaceOnce(title, "*", "");
                }
            }
            Bean data = null;
            if (titleMap.containsKey(title)) {
                data = (Bean) titleMap.get(title);
            } else {
                data = serv.getItem(title);
            }
            if (data != null) {
                itemMaps[i] = data;
            }
        }
        paramBean.set(COL_TITLE_LIST, itemMaps);

        //获取数据 --- 从第三行开始
        for (int i = 3; i < rows; i++) {
            Cell[] cells = sheet.getRow(i);
            Bean rowBean = new Bean();
            // 遍历每一列 获取每一列的数据
            for (int j = 1; j < cells.length; j++) {
                Cell cell = cells[j];
                String value;
                if (cell.getType() == CellType.DATE) {
                    DateCell dc = (DateCell) cell;
                    Date date = dc.getDate();
                    value = DateUtils.getStringFromDate(date, DateUtils.FORMAT_DATE);
                } else {
                    value = cell.getContents();
                }
                rowBean.set(COL_NAME + "" + (j + 1), value);
            }
            result.add(rowBean);

            //500 分批次
            // 判断是否循环到最后一条记录 或 result记录树超过500
            if (i == rows - 1 || result.size() >= 500) {
                paramBean.set(DATA_LIST, result);
                // 通过反射调用savedata方法
                Bean resultres = ServMgr.act(servId, method, paramBean);
                List<Bean> rowBeanList = resultres.getList(ALL_LIST);
                List<Object> successlist = resultres.getList(SUCCESS_LIST);
                successnum = successlist.size();
                failernum = rowBeanList.size() - successlist.size();
                flag = resultres.getBoolean("flag");
                // 设置返回信息
                if(!"".equals(resultres.get("titleError").toString().trim())){
                    resultOutBean.set("titleError", resultres.get("titleError").toString());
                }
                saveErrorAndReturnErrorFile(i, wSheet, sheet, rowBeanList, resultres.get("titleError").toString());
                result = new ArrayList<>();
            }
        }
        outBean.set("successnum",successnum);
        outBean.set("failernum",failernum);
        outBean.set("flag",flag);
        return outBean;
    }


    public static OutBean analysisOfficeExcel(Sheet sheet, ParamBean paramBean, LinkedHashMap<String, Bean> titleMap, ServDefBean serv,
                                             int rows, String servId,String method,WritableSheet wSheet) throws WriteException {
        OutBean outBean = new OutBean();
        int successnum = 0;
        int failernum = 0;
        Boolean flag = true;

        //未超出限制 开始解析表格
        List<Bean> result = new ArrayList<>();

        //列标题
        Cell[] titleCells = sheet.getRow(1);
        //列数
        int cols = titleCells.length;
        Bean[] itemMaps = new Bean[cols];

        //表头字段
        for (int i = 0; i < cols; ++i) {
            String title = sheet.getCell(i, 1).getContents();
            if (StringUtils.isNotEmpty(title)) {
                if (title.contains("*")) {
                    //替换掉模板中的必输标志*
                    title = StringUtils.replaceOnce(title, "*", "");
                }
            }
            Bean data = null;
            if (titleMap.containsKey(title)) {
                data = (Bean) titleMap.get(title);
            } else {
                data = serv.getItem(title);
            }
            if (data != null) {
                itemMaps[i] = data;
            }
        }
        paramBean.set(COL_TITLE_LIST, itemMaps);

        //获取数据 --- 从第三行开始
        for (int i = 2; i < rows; i++) {
            Cell[] cells = sheet.getRow(i);
            Bean rowBean = new Bean();
            // 遍历每一列 获取每一列的数据
            for (int j = 1; j < cells.length; j++) {
                Cell cell = cells[j];
                String value;
                if (cell.getType() == CellType.DATE) {
                    DateCell dc = (DateCell) cell;
                    Date date = dc.getDate();
                    value = DateUtils.getStringFromDate(date, DateUtils.FORMAT_DATE);
                } else {
                    value = cell.getContents();
                }
                rowBean.set(COL_NAME + "" + (j + 1), value);
            }
            result.add(rowBean);

            //500 分批次
            // 判断是否循环到最后一条记录 或 result记录树超过500
            if (i == rows - 1 || result.size() >= 500) {
                paramBean.set(DATA_LIST, result);
                // 通过反射调用savedata方法
                Bean resultres = ServMgr.act(servId, method, paramBean);
                List<Bean> rowBeanList = resultres.getList(ALL_LIST);
                List<Object> successlist = resultres.getList(SUCCESS_LIST);
                successnum = successlist.size();
                failernum = rowBeanList.size() - successlist.size();
                flag = resultres.getBoolean("flag");
                saveErrorAndReturnErrorOfficeFile(i, wSheet, sheet, rowBeanList);
                result = new ArrayList<>();
            }
        }
        outBean.set("successnum",successnum);
        outBean.set("failernum",failernum);
        outBean.set("flag",flag);
        return outBean;
    }



    private static int getRightRows(Sheet sheet) {
        int rsCols = sheet.getColumns(); //列数
        int rsRows = sheet.getRows(); //行数
        int nullCellNum;
        int afterRows = rsRows;
        for (int i = rsRows - 1; i >= 0; i--) { //统计行中为空的单元格数
            nullCellNum = 0;
            for (int j = 0; j < rsCols; j++) {
                String val = sheet.getCell(j, i).getContents();
                val = StringUtils.trimToEmpty(val);
                if (StringUtils.isBlank(val)) {
                    nullCellNum++;
                }
            }
            if (nullCellNum >= rsCols) { //如果nullCellNum大于或等于总的列数
                afterRows--;          //行数减一
            } else {
                break;
            }
        }
        return afterRows;
    }

    /*
    * 是否有未完成的导入
    *
    * */
    private static Boolean impInSameTime(ParamBean paramBean){
        String servId=paramBean.getServId();
        String xmId=paramBean.getStr("XM_ID");
        if(StringUtils.isNotBlank(xmId)) {
            String countSql = "select * from sup_imp_exp_record where 1=1 and SERV_ID='" + servId + "' and XM_ID='"
                    + xmId + "' and STATE='ing'";
            int count = Transaction.getExecutor().count(countSql);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }
}