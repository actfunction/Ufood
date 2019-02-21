package com.rh.core.serv.util;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.RequestUtils;

/**
 * 将列表页面的数据导出到Excel文件中，并将文件流返回客户端
 * @author wanglong
 * 
 */
public class ExportExcel {
    /** 已办结状态 **/
    private static final String S_WF_STATE_YB = "2";

    /**
     * 记录日志信息的函数
     */
    protected Log log = LogFactory.getLog(this.getClass());

    private WritableSheet sheet = null;
    private WritableCellFormat format = null;
    private HashMap<Integer, Integer> columnWidthMap = new HashMap<Integer, Integer>();
    private LinkedHashMap<String, Bean> colList = null;

    /**
     * 
     * @param dataList 数据
     * @param colList 列表显示定义
     */
    private void initSpecItem(List<Bean> dataList, LinkedHashMap<String, Bean> colList) {
        boolean hasWfState = false;
        boolean hasEmergency = false;
        if (colList.containsKey("S_WF_USER_STATE")) {
            Bean col = colList.get("S_WF_USER_STATE");
            if (col.getInt("ITEM_LIST_FLAG") == Constant.YES_INT) {
                hasWfState = true;
            }
        }

        if (colList.containsKey("S_EMERGENCY__NAME")) {
            Bean col = colList.get("S_EMERGENCY__NAME");
            if (col.getInt("ITEM_LIST_FLAG") == Constant.YES_INT) {
                hasEmergency = true;
            }
        }

        // 没有办理环节和缓急字段则不处理特殊字段数据
        if (!hasWfState && !hasEmergency) {
            return;
        }

        for (Bean bean : dataList) {
            if (hasWfState) {
                if (bean.isNotEmpty("S_WF_STATE") && bean.getStr("S_WF_STATE").equals(S_WF_STATE_YB)) {
                    bean.set("S_WF_USER_STATE", "已办结");
                } else {
                    bean.set("S_WF_USER_STATE", modifyWfState(bean.getStr("S_WF_USER_STATE")));
                }
            }

            if (hasEmergency) {
                String val = bean.getStr("S_EMERGENCY__NAME");
                bean.set("S_EMERGENCY__NAME", getEmergencyName(val));
            }
        }
    }

    /**
     * 
     * @param val 紧急程度的字典值
     * @return 去掉不在字典中的数据
     */
    private String getEmergencyName(String val) {
        if (NumberUtils.isNumber(val)) {
            return "";
        }

        return val;
    }

    /**
     * 修改[办理环节]excel导出数据
     * @param oldStr 原数据
     * @return newStr 修改后的数据
     */
    private String modifyWfState(String oldStr) {
        StringBuffer newStr = new StringBuffer();
        List<Bean> wfStateBeanList = JsonUtils.toBeanList(oldStr);
        // 单个用户
        if (wfStateBeanList.size() == 1) {
            Bean wfStrtrBean = wfStateBeanList.get(0);
            newStr.append(wfStrtrBean.getStr("D")).append("(").append(wfStrtrBean.getStr("N")).append(")");
        } else {
            // 多个并发用户
            newStr.append("并发：");
            for (int i = 0; i < wfStateBeanList.size(); i++) {
                StringBuffer thisNodeStr = new StringBuffer();
                // 如果不为最后一个，则追加逗号
                thisNodeStr.append(wfStateBeanList.get(i).getStr("D")).append("(");
                thisNodeStr.append(wfStateBeanList.get(i).getStr("N")).append(")");
                if ((i + 1) < wfStateBeanList.size()) {
                    newStr.append(thisNodeStr.toString()).append(",");
                } else {
                    // 如果是最后一个，则不追加逗号
                    newStr.append(thisNodeStr);
                }
            }
        }
        return newStr.toString();
    }

    /**
     * 
     * @param serv 服务定义
     * @param dataList 数据列表
     * @param colList 列数据Key
     * @return 执行结果
     */
    public OutBean exp(ServDefBean serv, List<Bean> dataList, LinkedHashMap<String, Bean> colList) {
        this.colList = colList;
        initSpecItem(dataList, colList);
        HttpServletResponse res = Context.getResponse();
        HttpServletRequest req = Context.getRequest();
        if (res != null) {
            res.setContentType("application/x-msdownload");
            RequestUtils.setDownFileName(req, res, serv.getName() + ".xls");
            try {
                OutputStream out = res.getOutputStream();
                WritableWorkbook wwInst = Workbook.createWorkbook(out);
                sheet = wwInst.createSheet(serv.getName(), 0);

                // 增加列表标题
                addGridHeader();

                format = new WritableCellFormat(
                        new WritableFont(WritableFont.createFont("微软简仿宋"), 12)); // 设置字体
                format.setWrap(true); // 自动换行
                format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

                int x = 0;
                // 添加数据体
                for (Bean data : dataList) {
                    x++;
                    addRow(x, data);
                }

                wwInst.write();
                wwInst.close();
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                try {
                    res.flushBuffer();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        return new OutBean();
    }
    
    /**
     *  增加列表标题
     * @throws WriteException Excel操作错误
     */
    private void addGridHeader() throws WriteException {
        WritableFont titleWf = new WritableFont(WritableFont.createFont("微软简仿宋"), 12,
                WritableFont.BOLD);
        WritableCellFormat formatTitle = new WritableCellFormat(titleWf);
        formatTitle.setWrap(true);
        formatTitle.setAlignment(jxl.format.Alignment.CENTRE);
        formatTitle.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        int x = 0;
        int y = 0;
        for (String key : colList.keySet()) {
            Bean col = colList.get(key);
            if (col.getInt("ITEM_LIST_FLAG") == Constant.YES_INT) { // 进行展示
                Label tmpLabel = new Label(y++, x, col.getStr("ITEM_NAME"), formatTitle);
                sheet.addCell(tmpLabel);
            }
        }
    }

    /**
     * 
     * @param rowNum 行号
     * @param rowData 行数据
     * @throws WriteException Excel操作错误
     */
    private void addRow(int rowNum,
            Bean rowData) throws WriteException {
        int y = 0;
        for (String key : colList.keySet()) {
            Bean col = colList.get(key);
            if (col.getInt("ITEM_LIST_FLAG") == Constant.YES_INT) {
                String cellData = rowData.getStr(col.getStr("ITEM_CODE"));
                addCell(rowNum, y, cellData);
                y++;
            }
        }
    }

    /**
     * 增加单元格数据
     * @param rowNum 行号
     * @param colNum 列好
     * @param cellData 单元格数据
     * @throws WriteException Excel 操作 Exception
     */
    private void addCell(int rowNum,
            int colNum, String cellData) throws WriteException {

        Label tmpLabel = new Label(colNum, rowNum, cellData, format);
        sheet.addCell(tmpLabel);
        if (columnWidthMap.containsKey(colNum)) {
            if (columnWidthMap.get(colNum) < cellData.length()) {
                columnWidthMap.put(colNum, cellData.length());
            }
        } else {
            columnWidthMap.put(colNum, cellData.length());
        }

        int colWidth = columnWidthMap.get(colNum);

        if (colWidth >= 40) {
            sheet.setColumnView(colNum, 80);
        } else if (colWidth >= 30) {
            sheet.setColumnView(colNum, 60);
        } else if (colWidth >= 20) {
            sheet.setColumnView(colNum, 45);
        } else {
            sheet.setColumnView(colNum, 25);
        }
    }
}
