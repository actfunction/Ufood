package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ExportExcel;
import com.rh.core.serv.util.ServUtils;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class FromDbToExcel extends CommonServ {

    private static String[] str = {"按时\n办结", "逾期\n办结", "小计", "未到完\n成时限", "逾期\n未办结", "无明确\n完成时限", "小计"};
    private static String[] str1 = {"以前年\n度结转", "本年\n新增", "小计"};
    private static String[] str2 = {"序号", "任务来源", "发文字号", "工作任务", "主办单位", "协办单位", "完成时限", "办理情况"};
    private static String[] str3 = {"序号", "任务来源", "工作任务", "主办单位", "成果体现/\n具体工作举措"};
    private static String[] str4 = {"后续工作", "建议完\n成时限", "责任人"};
    private static String[] str5 = {"序号", "单位\\要点\\措施数"};
    private static String[] str6 = {"序号", "重点工作", "主要内容", "指标3", "指标4", "*具体落实措施", "*进度安排", "*完成时限", "*责任处室（市局）", "*责任人", "归口管理司局"};
    private static String[] str7 = {"序号", "重点工作", "主要内容", "具体落实措施", "进度安排", "完成时间", "责任省厅", "责任处室和责任人", "归口管理司局"};
    private static String[] month = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
    private static String zbc = "制表：办公厅督查处";
    private static String[] str8 = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十"};
    private static Label label1;
    private static Label label2;
    private static Label label3;
    private static Label label4;
    private static Label label5;
    private static Label label6;
    private static Label label7;
    private static Label label8;
    private static Label label9;
    private static Label label10;
    private static Label label11;
    private static Label label12;
    private static Label label13;
    private static Label label14;
    private static Label label15;

    /**
     * 重要事项进展情况汇总表（按主办单位）
     *
     * @param beanList
     */
    public static void exportExcelOne(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "重要事项进展情况汇总表（按主办单位）";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 500);
            sheetOne.setRowView(2, 700);
            sheetOne.setRowView(3, 900);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 10);
            sheetOne.setColumnView(1, 25);
            sheetOne.setColumnView(2, 15);
            sheetOne.setColumnView(3, 15);
            sheetOne.setColumnView(4, 15);
            sheetOne.setColumnView(5, 15);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 15);
            sheetOne.setColumnView(8, 20);
            sheetOne.setColumnView(9, 20);

            //第一列第一行——第十列第一行
            Label title1 = new Label(0, 0, "重要事项进展情况汇总表（按主办单位）", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第十列第一行
            sheetOne.mergeCells(0, 0, 9, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第五列第二行——第六列第二行
            Label title9 = new Label(4, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);
            //合并第五列第二行到第六列第二行
            sheetOne.mergeCells(4, 1, 5, 1);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第九列第二行——第十列第二行
            Label title8 = new Label(8, 1, "查询时间：" + begintime + "-" + endtime , headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第九列第二行到第十列第二行
            sheetOne.mergeCells(8, 1, 9, 1);

            //第一列第三行——第一列第三行
            Label title3 = new Label(0, 2, "序号", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title3);
            //合并第一列第三行到第一列第四行
            sheetOne.mergeCells(0, 2, 0, 3);

            //第二列第三行——第二列第四行
            Label title4 = new Label(1, 2, "主办单位", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title4);
            //合并第二列第三行到第二列第四行
            sheetOne.mergeCells(1, 2, 1, 3);

            //第三列第三行——第五列第三行
            Label title5 = new Label(2, 2, "已办结", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title5);
            //合并第三列第三行到第五列第三行
            sheetOne.mergeCells(2, 2, 4, 2);

            //第六列第三行——第九列第三行
            Label title6 = new Label(5, 2, "办理中", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title6);
            //合并第六列第三行到第九列第三行
            sheetOne.mergeCells(5, 2, 8, 2);

            //第十列第三行——第十列第四行
            Label title7 = new Label(9, 2, "合计", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title7);
            //合并第十列第三行到第十列第四行
            sheetOne.mergeCells(9, 2, 9, 3);
            //动态绑定相同的表头
            for (int i = 2; i <= 8; i++) {
                label1 = new Label(i, 3, str[i - 2], headerFormat1);
                sheetOne.addCell(label1);
            }
            //动态绑定数据
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer one = Integer.parseInt(bean.getStr("ON_TIME_FINISHED")) + Integer.parseInt(bean.getStr("OVER_TIME_FINISHED"));
                Integer two = Integer.parseInt(bean.getStr("NOT_ACCOMPLISH_TIME")) + Integer.parseInt(bean.getStr("OVERDUE_NOT_STTLEMENT")) + Integer.parseInt(bean.getStr("NOT_UNEQUIVOCAL_DATETIME"));
                Integer xh = i + 1;
                Integer zj = one + two;
                label1 = new Label(0, 4 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 4 + i, bean.getStr("DEPT_NAME"), headerFormat1);
                label3 = new Label(2, 4 + i, bean.getStr("ON_TIME_FINISHED"), headerFormat1);
                label4 = new Label(3, 4 + i, bean.getStr("OVER_TIME_FINISHED"), headerFormat1);
                label5 = new Label(4, 4 + i, "" + one + "", headerFormat1);
                label6 = new Label(5, 4 + i, bean.getStr("NOT_ACCOMPLISH_TIME"), headerFormat1);
                label7 = new Label(6, 4 + i, bean.getStr("OVERDUE_NOT_STTLEMENT"), headerFormat1);
                label8 = new Label(7, 4 + i, bean.getStr("NOT_UNEQUIVOCAL_DATETIME"), headerFormat1);
                label9 = new Label(8, 4 + i, "" + two + "", headerFormat1);
                label10 = new Label(9, 4 + i, "" + zj + "", headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.addCell(label9);
                sheetOne.addCell(label10);
                sheetOne.setRowView(4 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 重要事项进展情况汇总表（按成果体现）
     *
     * @param beanList
     */
    public static void exportExcelTwo(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "重要事项进展情况汇总表（按成果体现）";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 400);
            sheetOne.setRowView(2, 700);
            sheetOne.setRowView(3, 900);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 10);
            sheetOne.setColumnView(1, 25);
            sheetOne.setColumnView(2, 30);
            sheetOne.setColumnView(3, 13);
            sheetOne.setColumnView(4, 13);
            sheetOne.setColumnView(5, 13);
            sheetOne.setColumnView(6, 13);
            sheetOne.setColumnView(7, 13);
            sheetOne.setColumnView(8, 13);
            sheetOne.setColumnView(9, 13);
            sheetOne.setColumnView(10, 13);
            sheetOne.setColumnView(11, 13);
            sheetOne.setColumnView(12, 16);
            sheetOne.setColumnView(13, 20);
            sheetOne.setColumnView(14, 20);

            //第一列第一行——第十五列第一行
            Label title1 = new Label(0, 0, "重要事项进展情况汇总表（按成果体现）", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第十五列第一行
            sheetOne.mergeCells(0, 0, 14, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第七列第二行——第八列第二行
            Label title9 = new Label(6, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);
            //合并第七列第二行到第八列第二行
            sheetOne.mergeCells(6, 1, 7, 1);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第十四列第二行——第十五列第二行
            Label title8 = new Label(13, 1, "查询时间：" + begintime + "-" + endtime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第十四列第二行到第十五列第二行
            sheetOne.mergeCells(13, 1, 14, 1);

            //第一列第三行——第一列第四行
            Label title3 = new Label(0, 2, "序号", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title3);
            //合并第一列第三行到第一列第四行
            sheetOne.mergeCells(0, 2, 0, 3);

            //第二列第三行——第二列第四行
            Label title4 = new Label(1, 2, "主办单位", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title4);
            //合并第二列第三行到第二列第四行
            sheetOne.mergeCells(1, 2, 1, 3);

            //第三列第三行——第三列第四行
            Label title5 = new Label(2, 2, "成果体现/具体工作举措", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title5);
            //合并第三列第四行到第三列第五行
            sheetOne.mergeCells(2, 2, 2, 3);

            //第四列第三行——第六列第三行
            Label title6 = new Label(3, 2, "上月结转", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title6);
            //合并第四列第三行到第六列第三行
            sheetOne.mergeCells(3, 2, 5, 2);

            //第七列第三行——第七列第四行
            Label title7 = new Label(6, 2, "新增", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title7);
            //合并第七列第三行到第七列第四行
            sheetOne.mergeCells(6, 2, 6, 3);

            //第八列第三行——第十列第三行
            Label title11 = new Label(7, 2, "已办结", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title11);
            //合并第八列第三行到第十列第三行
            sheetOne.mergeCells(7, 2, 9, 2);

            //第十一列第三行——第十四列第三行
            Label title12 = new Label(10, 2, "办理中", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title12);
            //合并第十一列第三行到第十四列第三行
            sheetOne.mergeCells(10, 2, 13, 2);

            //第十五列第三行——第十五列第四行
            Label title10 = new Label(14, 2, "合计", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title10);
            //合并第十五列第三行到第十五列第四行
            sheetOne.mergeCells(14, 2, 14, 3);

            //动态绑定相同的表头
            for (int i = 3; i <= 5; i++) {
                label1 = new Label(i, 3, str1[i - 3], headerFormat1);
                sheetOne.addCell(label1);
            }
            for (int i = 7; i <= 13; i++) {
                label1 = new Label(i, 3, str[i - 7], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer one = Integer.parseInt(bean.getStr("ON_TIME_FINISHED")) + Integer.parseInt(bean.getStr("OVER_TIME_FINISHED"));
                Integer two = Integer.parseInt(bean.getStr("NOT_ACCOMPLISH_TIME")) + Integer.parseInt(bean.getStr("OVERDUE_NOT_STTLEMENT")) + Integer.parseInt(bean.getStr("NOT_UNEQUIVOCAL_DATETIME"));
                Integer three = Integer.parseInt(bean.getStr("YQJZ")) + Integer.parseInt(bean.getStr("BNXZ"));
                Integer xh = i + 1;
                Integer zj = one + two + three + Integer.parseInt(bean.getStr("XZ"));
                label1 = new Label(0, 4 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 4 + i, bean.getStr("DEPT_NAME"), headerFormat1);
                label15 = new Label(2,4+i, bean.getStr("DICT_NAME"), headerFormat1);
                label3 = new Label(3, 4 + i, bean.getStr("YQJZ"), headerFormat1);
                label4 = new Label(4, 4 + i, bean.getStr("BNXZ"), headerFormat1);
                label5 = new Label(5, 4 + i, "" + three + "", headerFormat1);
                label6 = new Label(6, 4 + i, bean.getStr("XZ"), headerFormat1);
                label7 = new Label(7, 4 + i, bean.getStr("ON_TIME_FINISHED"), headerFormat1);
                label8 = new Label(8, 4 + i, bean.getStr("OVER_TIME_FINISHED"), headerFormat1);
                label9 = new Label(9, 4 + i, "" + one + "", headerFormat1);
                label10 = new Label(10, 4 + i, bean.getStr("NOT_ACCOMPLISH_TIME"), headerFormat1);
                label11 = new Label(11, 4 + i, bean.getStr("OVERDUE_NOT_STTLEMENT"), headerFormat1);
                label12 = new Label(12, 4 + i, bean.getStr("NOT_UNEQUIVOCAL_DATETIME"), headerFormat1);
                label13 = new Label(13, 4 + i, "" + two + "", headerFormat1);
                label14 = new Label(14, 4 + i, "" + zj + "", headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.addCell(label9);
                sheetOne.addCell(label10);
                sheetOne.addCell(label11);
                sheetOne.addCell(label12);
                sheetOne.addCell(label13);
                sheetOne.addCell(label14);
                sheetOne.addCell(label15);
                sheetOne.setRowView(4 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 重要事项办理情况汇总表（按事项来源）
     *
     * @param beanList
     */
    public static void exportExcelThree(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "重要事项办理情况汇总表（按事项来源）";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 400);
            sheetOne.setRowView(2, 700);
            sheetOne.setRowView(3, 900);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 10);
            sheetOne.setColumnView(1, 45);
            sheetOne.setColumnView(2, 13);
            sheetOne.setColumnView(3, 13);
            sheetOne.setColumnView(4, 18);
            sheetOne.setColumnView(5, 13);
            sheetOne.setColumnView(6, 13);
            sheetOne.setColumnView(7, 13);
            sheetOne.setColumnView(8, 18);
            sheetOne.setColumnView(9, 20);
            sheetOne.setColumnView(10, 13);
            sheetOne.setColumnView(11, 13);
            sheetOne.setColumnView(12, 20);
            sheetOne.setColumnView(13, 20);

            //第一列第一行——第十四列第一行
            Label title1 = new Label(0, 0, "重要事项办理情况汇总表（按事项来源）", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第十四列第一行
            sheetOne.mergeCells(0, 0, 13, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第五列第二行——第六列第二行
            Label title9 = new Label(4, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);
            //合并第五列第二行到第六列第二行
            sheetOne.mergeCells(4, 1, 5, 1);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第十三列第二行——第十四列第二行
            Label title8 = new Label(12, 1, "查询时间：" + begintime + "-" + endtime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第十三列第二行到第十四列第二行
            sheetOne.mergeCells(12, 1, 13, 1);

            //第一列第三行——第一列第四行
            Label title3 = new Label(0, 2, "序号", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title3);
            //合并第一列第三行到第一列第四行
            sheetOne.mergeCells(0, 2, 0, 3);

            //第二列第三行——第二列第四行
            Label title4 = new Label(1, 2, "任务来源", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title4);
            //合并第二列第三行到第二列第四行
            sheetOne.mergeCells(1, 2, 1, 3);

            //第三列第三行——第五列第三行
            Label title5 = new Label(2, 2, "上月结转", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title5);
            //合并第三列第三行到第五列第三行
            sheetOne.mergeCells(2, 2, 4, 2);

            //第六列第三行——第六列第四行
            Label title6 = new Label(5, 2, "本月\n新增", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title6);
            //合并第六列第三行到第六列第四行
            sheetOne.mergeCells(5, 2, 5, 3);

            //第七列第三行——第九列第三行
            Label title7 = new Label(6, 2, "已办结", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title7);
            //合并第七列第三行到第九列第三行
            sheetOne.mergeCells(6, 2, 8, 2);

            //第十列第三行——第十三列第三行
            Label title10 = new Label(9, 2, "办理中", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title10);
            //合并第十列第三行到第十三列第三行
            sheetOne.mergeCells(9, 2, 12, 2);

            //第十四列第三行——第十四列第四行
            Label title11 = new Label(13, 2, "合计", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title11);
            //合并第十四列第三行到第十四列第四行
            sheetOne.mergeCells(13, 2, 13, 3);

            //动态绑定相同的表头
            for (int i = 2; i <= 4; i++) {
                label1 = new Label(i, 3, str1[i - 2], headerFormat1);
                sheetOne.addCell(label1);
            }
            for (int i = 6; i <= 12; i++) {
                label1 = new Label(i, 3, str[i - 6], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer one = Integer.parseInt(bean.getStr("ON_TIME_FINISHED")) + Integer.parseInt(bean.getStr("OVER_TIME_FINISHED"));
                Integer two = Integer.parseInt(bean.getStr("NOT_ACCOMPLISH_TIME")) + Integer.parseInt(bean.getStr("OVERDUE_NOT_STTLEMENT")) + Integer.parseInt(bean.getStr("NOT_UNEQUIVOCAL_DATETIME"));
                Integer three = Integer.parseInt(bean.getStr("YQJZ")) + Integer.parseInt(bean.getStr("BNXZ"));
                Integer xh = i + 1;
                Integer zj = one + two + three + Integer.parseInt(bean.getStr("XZ"));
                label1 = new Label(0, 4 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 4 + i, bean.getStr("DICT_NAME"), headerFormat1);
                label3 = new Label(2, 4 + i, bean.getStr("YQJZ"), headerFormat1);
                label4 = new Label(3, 4 + i, bean.getStr("BNXZ"), headerFormat1);
                label5 = new Label(4, 4 + i, "" + three + "", headerFormat1);
                label6 = new Label(5, 4 + i, bean.getStr("XZ"), headerFormat1);
                label7 = new Label(6, 4 + i, bean.getStr("ON_TIME_FINISHED"), headerFormat1);
                label8 = new Label(7, 4 + i, bean.getStr("OVER_TIME_FINISHED"), headerFormat1);
                label9 = new Label(8, 4 + i, "" + one + "", headerFormat1);
                label10 = new Label(9, 4 + i, bean.getStr("NOT_ACCOMPLISH_TIME"), headerFormat1);
                label11 = new Label(10, 4 + i, bean.getStr("OVERDUE_NOT_STTLEMENT"), headerFormat1);
                label12 = new Label(11, 4 + i, bean.getStr("NOT_UNEQUIVOCAL_DATETIME"), headerFormat1);
                label13 = new Label(12, 4 + i, "" + two + "", headerFormat1);
                label14 = new Label(13, 4 + i, "" + zj + "", headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.addCell(label9);
                sheetOne.addCell(label10);
                sheetOne.addCell(label11);
                sheetOne.addCell(label12);
                sheetOne.addCell(label13);
                sheetOne.addCell(label14);
                sheetOne.setRowView(4 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 重要事项办理情况汇总表（按成果体现）
     *
     * @param beanList
     */
    public static void exportExcelFourth(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "重要事项办理情况汇总表（按成果体现）";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 400);
            sheetOne.setRowView(2, 700);
            sheetOne.setRowView(3, 900);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 10);
            sheetOne.setColumnView(1, 30);
            sheetOne.setColumnView(2, 30);
            sheetOne.setColumnView(3, 13);
            sheetOne.setColumnView(4, 13);
            sheetOne.setColumnView(5, 13);
            sheetOne.setColumnView(6, 13);
            sheetOne.setColumnView(7, 13);
            sheetOne.setColumnView(8, 13);
            sheetOne.setColumnView(9, 13);
            sheetOne.setColumnView(10, 13);
            sheetOne.setColumnView(11, 13);
            sheetOne.setColumnView(12, 13);
            sheetOne.setColumnView(13, 20);
            sheetOne.setColumnView(14, 20);

            //第一列第一行——第十五列第一行
            Label title1 = new Label(0, 0, "重要事项办理情况汇总表（按成果体现）", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第十五列第一行
            sheetOne.mergeCells(0, 0, 14, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第五列第二行——第六列第二行
            Label title9 = new Label(4, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);
            //合并第五列第二行到第六列第二行
            sheetOne.mergeCells(4, 1, 5, 1);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第十三列第二行——第十五列第二行
            Label title8 = new Label(12, 1, "查询时间：" + begintime + "-" + endtime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第十三列第二行到第十五列第二行
            sheetOne.mergeCells(12, 1, 14, 1);

            //第一列第三行——第一列第四行
            Label title3 = new Label(0, 2, "序号", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title3);
            //合并第一列第三行到第一列第四行
            sheetOne.mergeCells(0, 2, 0, 3);

            //第二列第三行——第二列第四行
            Label title4 = new Label(1, 2, "任务来源", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title4);
            //合并第二列第三行到第二列第四行
            sheetOne.mergeCells(1, 2, 1, 3);

            //第三列第三行——第三列第四行
            Label title5 = new Label(2, 2, "成果体现/具体工作举措", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title5);
            //合并第三列第三行到第三列第四行
            sheetOne.mergeCells(2, 2, 2, 3);

            //第四列第三行——第六列第三行
            Label title6 = new Label(3, 2, "上月结转", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title6);
            //合并第四列第三行到第六列第三行
            sheetOne.mergeCells(3, 2, 5, 2);

            //第七列第三行——第七列第四行
            Label title7 = new Label(6, 2, "本月\n新增", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title7);
            //合并第七列第三行到第七列第四行
            sheetOne.mergeCells(6, 2, 6, 3);

            //第八列第三行——第十列第三行
            Label title = new Label(7, 2, "已办结", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title);
            //合并第八列第三行到第十列第三行
            sheetOne.mergeCells(7, 2, 9, 2);

            //第十一列第三行——第十四列第三行
            Label title11 = new Label(10, 2, "办理中", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title11);
            //合并第十一列第三行到第十四列第三行
            sheetOne.mergeCells(10, 2, 13, 2);

            //第十五列第三行——第十五列第四行
            Label title10 = new Label(14, 2, "合计", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title10);
            //合并第十五列第三行到第十五列第四行
            sheetOne.mergeCells(14, 2, 14, 3);

            //动态绑定相同的表头
            for (int i = 3; i <= 5; i++) {
                label1 = new Label(i, 3, str1[i - 3], headerFormat1);
                sheetOne.addCell(label1);
            }
            for (int i = 7; i <= 13; i++) {
                label1 = new Label(i, 3, str[i - 7], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer one = Integer.parseInt(bean.getStr("ON_TIME_FINISHED")) + Integer.parseInt(bean.getStr("OVER_TIME_FINISHED"));
                Integer two = Integer.parseInt(bean.getStr("NOT_ACCOMPLISH_TIME")) + Integer.parseInt(bean.getStr("OVERDUE_NOT_STTLEMENT")) + Integer.parseInt(bean.getStr("NOT_UNEQUIVOCAL_DATETIME"));
                Integer three = Integer.parseInt(bean.getStr("YQJZ")) + Integer.parseInt(bean.getStr("BNXZ"));
                Integer xh = i + 1;
                Integer zj = one + two + three + Integer.parseInt(bean.getStr("XZ"));
                label1 = new Label(0, 4 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 4 + i, bean.getStr("SUPERV_ITEM"), headerFormat1);
                label3 = new Label(2, 4 + i, bean.getStr("DICT_NAME"), headerFormat1);
                label4 = new Label(3, 4 + i, bean.getStr("YQJZ"), headerFormat1);
                label5 = new Label(4, 4 + i, bean.getStr("BNXZ"), headerFormat1);
                label6 = new Label(5, 4 + i, "" + three + "", headerFormat1);
                label7 = new Label(6, 4 + i, bean.getStr("XZ"), headerFormat1);
                label8 = new Label(7, 4 + i, bean.getStr("ON_TIME_FINISHED"), headerFormat1);
                label9 = new Label(8, 4 + i, bean.getStr("OVER_TIME_FINISHED"), headerFormat1);
                label10 = new Label(9, 4 + i, "" + one + "", headerFormat1);
                label11 = new Label(10, 4 + i, bean.getStr("NOT_ACCOMPLISH_TIME"), headerFormat1);
                label12 = new Label(11, 4 + i, bean.getStr("OVERDUE_NOT_STTLEMENT"), headerFormat1);
                label13 = new Label(12, 4 + i, bean.getStr("NOT_UNEQUIVOCAL_DATETIME"), headerFormat1);
                label14 = new Label(13, 4 + i, "" + two + "", headerFormat1);
                label15 = new Label(14, 4 + i, "" + zj + "", headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.addCell(label9);
                sheetOne.addCell(label10);
                sheetOne.addCell(label11);
                sheetOne.addCell(label12);
                sheetOne.addCell(label13);
                sheetOne.addCell(label14);
                sheetOne.addCell(label15);
                sheetOne.setRowView(4 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 已办结重要事项明细表
     *
     * @param beanList
     */
    public static void exportExcelFive(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "已办结重要事项明细表";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 600);
            sheetOne.setRowView(2, 800);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 35);
            sheetOne.setColumnView(2, 30);
            sheetOne.setColumnView(3, 50);
            sheetOne.setColumnView(4, 15);
            sheetOne.setColumnView(5, 15);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 60);

            //第一列第一行——第八列第一行
            Label title1 = new Label(0, 0, "已办结重要事项明细表", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第八列第一行
            sheetOne.mergeCells(0, 0, 7, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第四列第二行
            Label title9 = new Label(3, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第七列第二行——第八列第二行
            Label title8 = new Label(6, 1, "查询时间：" + begintime + "-" + endtime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第七列第二行到第八列第二行
            sheetOne.mergeCells(6, 1, 7, 1);

            //动态绑定相同的表头
            for (int i = 0; i <= 7; i++) {
                label1 = new Label(i, 2, str2[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            HashMap<Object, Object> hashMap1 = null;
            HashMap<Object, Object> hashMap2 = null;
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer xh = i + 1;
                label1 = new Label(0, 3 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 3 + i, bean.getStr("ITEM_SOURCE"), headerFormat1);
                label3 = new Label(2, 3 + i, bean.getStr("ISSUE_CODE"), headerFormat1);
                label4 = new Label(3, 3 + i, bean.getStr("SUPERV_ITEM"), headerFormat1);
                label5 = new Label(4, 3 + i, bean.getStr("HOST_UNIT"), headerFormat1);
                label6 = new Label(5, 3 + i, bean.getStr("CO_ORGANIZER"), headerFormat1);
                if(bean.getStr("LIMIT_DATE").length()>0){
                    label7 = new Label(6, 3 + i, bean.getStr("LIMIT_DATE"), headerFormat1);
                }else{
                    label7 = new Label(6, 3 + i, bean.getStr("NOT_LIMIT_TIME_REASON"), headerFormat1);
                }
                label8 = new Label(7, 3 + i, bean.getStr("GAIN_TEXT"), headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.setRowView(3 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 正在办理重要事项明细表
     *
     * @param beanList
     */
    public static void exportExcelSix(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "正在办理重要事项明细表";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 600);
            sheetOne.setRowView(2, 800);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 35);
            sheetOne.setColumnView(2, 30);
            sheetOne.setColumnView(3, 50);
            sheetOne.setColumnView(4, 15);
            sheetOne.setColumnView(5, 15);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 60);

            //第一列第一行——第八列第一行
            Label title1 = new Label(0, 0, "正在办理重要事项明细表", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第二行到第八列第二行
            sheetOne.mergeCells(0, 0, 7, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第四列第二行
            Label title9 = new Label(3, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第七列第二行——第八列第二行
            Label title8 = new Label(6, 1, "查询时间：" + begintime + "-" + endtime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第七列第二行到第八列第二行
            sheetOne.mergeCells(6, 1, 7, 1);

            //动态绑定相同的表头
            for (int i = 0; i <= 7; i++) {
                label1 = new Label(i, 2, str2[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            HashMap<Object, Object> hashMap1 = null;
            HashMap<Object, Object> hashMap2 = null;
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer xh = i + 1;
                label1 = new Label(0, 3 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 3 + i, bean.getStr("ITEM_SOURCE"), headerFormat1);
                label3 = new Label(2, 3 + i, bean.getStr("ISSUE_CODE"), headerFormat1);
                label4 = new Label(3, 3 + i, bean.getStr("SUPERV_ITEM"), headerFormat1);
                label5 = new Label(4, 3 + i, bean.getStr("HOST_UNIT"), headerFormat1);
                label6 = new Label(5, 3 + i, bean.getStr("CO_ORGANIZER"), headerFormat1);
                if(bean.getStr("LIMIT_DATE").length()>0){
                    label7 = new Label(6, 3 + i, bean.getStr("LIMIT_DATE"), headerFormat1);
                }else{
                    label7 = new Label(6, 3 + i, bean.getStr("NOT_LIMIT_TIME_REASON"), headerFormat1);
                }
                label8 = new Label(7, 3 + i, bean.getStr("GAIN_TEXT"), headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.setRowView(3 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 未按期办结重要事项明细表
     *
     * @param beanList
     */
    public static void exportExcelSeven(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "未按期办结重要事项明细表";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 600);
            sheetOne.setRowView(2, 800);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 35);
            sheetOne.setColumnView(2, 30);
            sheetOne.setColumnView(3, 50);
            sheetOne.setColumnView(4, 15);
            sheetOne.setColumnView(5, 15);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 60);

            //第一列第一行——第八列第一行
            Label title1 = new Label(0, 0, "未按期办结重要事项明细表", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第八列第一行
            sheetOne.mergeCells(0, 0, 7, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第四列第二行
            Label title9 = new Label(3, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);

            String begintime = paramBean.getStr("begintime").substring(0,4).toString()+"年"+paramBean.getStr("begintime").substring(5,7).toString()+"月";
            String endtime = paramBean.getStr("endtime").substring(0,4).toString()+"年"+paramBean.getStr("endtime").substring(5,7).toString()+"月";

            //第七列第二行——第八列第二行
            Label title8 = new Label(6, 1, "查询时间：" + begintime + "-" + endtime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第七列第二行到第八列第二行
            sheetOne.mergeCells(6, 1, 7, 1);

            //动态绑定相同的表头
            for (int i = 0; i <= 7; i++) {
                label1 = new Label(i, 2, str2[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            HashMap<Object, Object> hashMap1 = null;
            HashMap<Object, Object> hashMap2 = null;
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer xh = i + 1;
                label1 = new Label(0, 3 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 3 + i, bean.getStr("ITEM_SOURCE"), headerFormat1);
                label3 = new Label(2, 3 + i, bean.getStr("ISSUE_CODE"), headerFormat1);
                label4 = new Label(3, 3 + i, bean.getStr("SUPERV_ITEM"), headerFormat1);
                label5 = new Label(4, 3 + i, bean.getStr("HOST_UNIT"), headerFormat1);
                label6 = new Label(5, 3 + i, bean.getStr("CO_ORGANIZER"), headerFormat1);
                if(bean.getStr("LIMIT_DATE").length()>0){
                    label7 = new Label(6, 3 + i, bean.getStr("LIMIT_DATE"), headerFormat1);
                }else{
                    label7 = new Label(6, 3 + i, bean.getStr("NOT_LIMIT_TIME_REASON"), headerFormat1);
                }
                label8 = new Label(7, 3 + i, bean.getStr("GAIN_TEXT"), headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.setRowView(3 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 提出分月推荐计划重要事项明细表
     *
     * @param beanList
     */
    public static void exportExcelEight(List<Bean> beanList, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "提出分月推荐计划重要事项明细表";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 600);
            sheetOne.setRowView(2, 800);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 35);
            sheetOne.setColumnView(2, 40);
            sheetOne.setColumnView(3, 30);
            sheetOne.setColumnView(4, 50);
            sheetOne.setColumnView(5, 15);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 15);
            sheetOne.setColumnView(8, 15);
            sheetOne.setColumnView(9, 15);
            sheetOne.setColumnView(10, 15);
            sheetOne.setColumnView(11, 15);
            sheetOne.setColumnView(12, 15);
            sheetOne.setColumnView(13, 15);
            sheetOne.setColumnView(14, 15);
            sheetOne.setColumnView(15, 15);
            sheetOne.setColumnView(16, 15);
            sheetOne.setColumnView(17, 15);
            sheetOne.setColumnView(18, 20);
            sheetOne.setColumnView(19, 20);
            sheetOne.setColumnView(20, 20);

            //第一列第一行——第十五列第一行
            Label title1 = new Label(0, 0, "提出分月推荐计划重要事项明细表", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第十五列第一行
            sheetOne.mergeCells(0, 0, 14, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            //第十列第二行——第十一列第二行
            Label title9 = new Label(9, 1, "查询机构：" + paramBean.getStr("jg1"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title9);
            //合并第十列第二行——第十一列第二行
            sheetOne.mergeCells(9, 1, 10, 1);

            String selecttime = paramBean.getStr("selecttime").substring(0,4).toString()+"年"+paramBean.getStr("selecttime").substring(5,7).toString()+"月";

            //第十九列第二行——第二十列第二行
            Label title8 = new Label(18, 1, "查询时间："  + selecttime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);
            //合并第十九列第二行到第二十列第二行
            sheetOne.mergeCells(18, 1, 19, 1);

            //动态绑定相同的表头
            for (int i = 0; i <= 4; i++) {
                label1 = new Label(i, 2, str3[i], headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.mergeCells(i, 2,i , 3);
            }

            //第六列第三行——第十七列第三行
            Label title3 = new Label(5, 2, paramBean.getStr("selecttime").substring(0,4)+"年", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title3);
            //合并第六列第三行到第十七列第三行
            sheetOne.mergeCells(5, 2, 16, 2);

            //动态绑定相同的表头
            for (int i = 5; i <= 16; i++) {
                label1 = new Label(i, 3, month[i - 5], headerFormat1);
                sheetOne.addCell(label1);
            }
            for (int i = 17; i <= 19; i++) {
                label1 = new Label(i, 2, str4[i - 17], headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.mergeCells(i, 2, i, 3);
            }

            //动态绑定数据
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                label1 = new Label(0, 4 + i, "" + (i + 1) + "", headerFormat1);
                label2 = new Label(1, 4 + i, bean.getStr("ITEM_SOURCE"), headerFormat1);
                label3 = new Label(2, 4 + i, bean.getStr("SUPERV_ITEM"), headerFormat1);
                label4 = new Label(3, 4 + i, bean.getStr("DEPT_NAME"), headerFormat1);
                label5 = new Label(4, 4 + i, bean.getStr("DICT_NAME"), headerFormat1);
                List<Bean> list = (List<Bean>) bean.get("AA");
                for (int j = 0 ; j < list.size() ; j++){
                    Bean bean1 = list.get(j);
                    Integer BEIGN_DATE =  Integer.parseInt(bean1.get("BEIGN_DATE").toString());
                    Integer END_DATE = Integer.parseInt(bean1.get("END_DATE").toString());
                    if(BEIGN_DATE == END_DATE){
                        Label label16 = new Label( BEIGN_DATE + 4 ,4 + i,bean1.getStr("DETAIL_TEXT"),headerFormat1);
                        sheetOne.addCell(label16);
                    }else{
                        for (int x = BEIGN_DATE ; x <= END_DATE ; x ++ ){
                            Label label16 = new Label( x + 4 ,4 + i,bean1.getStr("DETAIL_TEXT"),headerFormat1);
                            sheetOne.addCell(label16);
                        }
                    }
                }
                if(bean.getStr("LIMIT_DATE").length()>0){
                    label7 = new Label(18, 4 + i, bean.getStr("LIMIT_DATE"), headerFormat1);
                }else{
                    label7 = new Label(18, 4 + i, bean.getStr("NOT_LIMIT_TIME_REASON"), headerFormat1);
                }
                label8 = new Label(19, 4 + i, bean.getStr("CHARGE_NAME"), headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.setRowView(4 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * XX同志分管单位X月底前需完成的重要事项明细表（共X项）
     *
     * @param beanList
     */
    public static void exportExcelNine(List<Bean> beanList, ParamBean paramBean,String LEADER_NAME) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = LEADER_NAME + "同志分管单位" + paramBean.getStr("selecttime").substring(5) + "月底前需完成的重要事项明细表（共" + beanList.size() + "项）";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //设置制表样式
            //设置表头样式
            WritableCellFormat headerFormat3 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat3.setAlignment(Alignment.LEFT);
            //竖直方向居中对齐
            headerFormat3.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置查询时间段样式
            //设置表头样式
            WritableCellFormat headerFormat4 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat4.setAlignment(Alignment.RIGHT);
            //竖直方向居中对齐
            headerFormat4.setVerticalAlignment(VerticalAlignment.CENTRE);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 900);
            sheetOne.setRowView(1, 600);
            sheetOne.setRowView(2, 800);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 35);
            sheetOne.setColumnView(2, 30);
            sheetOne.setColumnView(3, 50);
            sheetOne.setColumnView(4, 15);
            sheetOne.setColumnView(5, 15);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 60);


            //第一列第一行——第八列第一行
            Label title1 = new Label(0, 0, LEADER_NAME + "同志分管单位" + paramBean.getStr("selecttime").substring(5) + "月底前需完成的重要事项明细表（共" + beanList.size() + "项）", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第一行到第八列第一行
            sheetOne.mergeCells(0, 0, 7, 0);

            //第一列第二行——第二列第二行
            Label title2 = new Label(0, 1, zbc, headerFormat3);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第二列第二行
            sheetOne.mergeCells(0, 1, 1, 1);

            String selecttime = paramBean.getStr("selecttime").substring(0,4).toString()+"年"+paramBean.getStr("selecttime").substring(5,7).toString()+"月";

            //第八列第二行
            Label title8 = new Label(7, 1, "查询时间："  + selecttime, headerFormat4);
            //添加进第一页
            sheetOne.addCell(title8);

            //动态绑定相同的表头
            for (int i = 0; i <= 7; i++) {
                label1 = new Label(i, 2, str2[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            HashMap<Object, Object> hashMap1 = null;
            HashMap<Object, Object> hashMap2 = null;
            for (int i = 0; i < beanList.size(); i++) {
                Bean bean = beanList.get(i);
                Integer xh = i + 1;
                label1 = new Label(0, 3 + i, "" + xh + "", headerFormat1);
                label2 = new Label(1, 3 + i, bean.getStr("DICT_NAME"), headerFormat1);
                label3 = new Label(2, 3 + i, bean.getStr("ISSUE_CODE"), headerFormat1);
                label4 = new Label(3, 3 + i, bean.getStr("SUPERV_ITEM"), headerFormat1);
                label5 = new Label(4, 3 + i, bean.getStr("HOST_UNIT"), headerFormat1);
                label6 = new Label(5, 3 + i, bean.getStr("CO_ORGANIZER"), headerFormat1);
                if(bean.getStr("LIMIT_DATE").length()>0){
                    label7 = new Label(6, 3 + i, bean.getStr("LIMIT_DATE"), headerFormat1);
                }else{
                    label7 = new Label(6, 3 + i, bean.getStr("NOT_LIMIT_TIME_REASON"), headerFormat1);
                }
                label8 = new Label(7, 3 + i, bean.getStr("GAIN_TEXT"), headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.setRowView(3 + i, 600);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 各省级审计机关落实《全国审计机关20XX年度工作要点》具体措施统计表
     *
     * @param hashMap
     * @param paramBean
     */
    public static void exportExcelTen(HashMap<String, Object> hashMap, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        String date = paramBean.getStr("selecttime").substring(0, 4);
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = "各省级审计机关落实《全国审计机关" + date + "年度工作要点》具体措施统计表";
        WritableWorkbook wwb = null;
        List<Object> objectList = (List<Object>) hashMap.get("MAJOR_WORK");
        List<Object> objectList1 = (List<Object>) hashMap.get("data");
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 800);
            sheetOne.setRowView(1, 900);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 25);


            //第一列第一行——第十一列第一行
            Label title1 = new Label(0, 0, "各省级审计机关落实《全国审计机关" + date + "年度工作要点》具体措施统计表", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第二行到第八列第二行
            sheetOne.mergeCells(0, 0, (2 + objectList.size()), 0);

            //动态绑定相同的表头
            for (int i = 0; i < str5.length; i++) {
                label1 = new Label(i, 1, str5[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            //动态绑定数据
            for (int i = 0; i < objectList.size() + 1; i++) {
                if (i == objectList.size()) {
                    label2 = new Label((2 + i), 1, "合计", headerFormat1);
                    sheetOne.addCell(label2);
                    sheetOne.setColumnView((2 + i), 35);
                    break;
                }
                label1 = new Label(2 + i, 1, "" + str8[i] + "、" + objectList.get(i) + "", headerFormat1);
                sheetOne.addCell(label1);
                sheetOne.setColumnView((2 + i), 35);
            }

            for (int i = 0; i < objectList1.size(); i++) {
                HashMap<Object, Object> hashMap1 = (HashMap<Object, Object>) objectList1.get(i);
                label1 = new Label(0, (i + 2), "" + (i + 1) + "", headerFormat1);
                sheetOne.addCell(label1);
                label2 = new Label(1, (i + 2), "" + hashMap1.get("PROVIN_NAME") + "", headerFormat1);
                sheetOne.addCell(label2);
                List<Object> objectList2 = (List<Object>) hashMap1.get("data");
                Integer count = 0;
                for (int j = 0; j < objectList2.size() + 1; j++) {
                    if (j == objectList2.size()) {
                        label3 = new Label((j + 2), (i + 2), "" + count + "", headerFormat1);
                        sheetOne.addCell(label3);
                        break;
                    }
                    label3 = new Label((j + 2), (i + 2), "" + objectList2.get(j) + "", headerFormat1);
                    sheetOne.addCell(label3);
                    count += Integer.parseInt(objectList2.get(j).toString());
                }
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 落实《全国审计机关2018年度工作要点》具体措施清单
     *
     * @param hashMap
     * @param paramBean
     */
    public static void exportExcelEleven(HashMap<String, Object> hashMap, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String date = paramBean.getStr("selecttime").substring(0, 4);
        String fileName = "落实《全国审计机关" + date + "年度工作要点》具体措施清单";
        WritableWorkbook wwb = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置Excel表头的行宽
            sheetOne.setRowView(0, 800);
            sheetOne.setRowView(1, 700);
            sheetOne.setRowView(2, 700);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 15);
            sheetOne.setColumnView(2, 15);
            sheetOne.setColumnView(3, 35);
            sheetOne.setColumnView(4, 35);
            sheetOne.setColumnView(5, 35);
            sheetOne.setColumnView(6, 35);
            sheetOne.setColumnView(7, 35);
            sheetOne.setColumnView(8, 25);
            sheetOne.setColumnView(9, 15);
            sheetOne.setColumnView(10, 20);

            //第一列第一行——第十一列第一行
            Label title1 = new Label(0, 0, "落实《全国审计机关" + date + "年度工作要点》具体措施清单", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第二行到第八列第二行
            sheetOne.mergeCells(0, 0, 10, 0);

            //第一列第二行——第三列第二行
            Label title2 = new Label(0, 1, "单位：" + paramBean.getStr("st1") + "（局）", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title2);
            //合并第一列第二行到第三列第二行
            sheetOne.mergeCells(0, 1, 2, 1);

            //第四列第二行
            Label title3 = new Label(3, 1, "联系人：" + hashMap.get("USER_CODE") + "（省厅督查员）", headerFormat1);
            //添加进第一页
            sheetOne.addCell(title3);

            //第七列第二行
            Label title4 = new Label(6, 1, "工作电话：" + hashMap.get("USER_TEL"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title4);

            //第八列第二行
            Label title5 = new Label(7, 1, "手机号码：" + hashMap.get("USER_PHONE"), headerFormat1);
            //添加进第一页
            sheetOne.addCell(title5);

            //动态绑定相同的表头
            for (int i = 0; i <= 10; i++) {
                label1 = new Label(i, 2, str6[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            List<Object> objectList = (List<Object>) hashMap.get("data");
            List<Object> objectList1 = null;
            List<Object> objectList2 = null;
            HashMap<Object, Object> hashMap1 = null;
            HashMap<Object, Object> hashMap2 = null;
            HashMap<Object, Object> hashMap3 = null;
            int row = 3;
            int row1 = 3;
            int row2 = 3;
            int a = 1;
            int count;
            //动态绑定数据
            for (int i = 0; i < objectList.size(); i++) {
                count = 0;
                //主要工作
                hashMap1 = (HashMap<Object, Object>) objectList.get(i);
                //内容集合
                objectList1 = (List<Object>) hashMap1.get("data");
                label1 = new Label(1, row, hashMap1.get("MAJOR_WORK").toString(), headerFormat1);
                sheetOne.addCell(label1);
                for (int j = 0; j < objectList1.size(); j++) {
                    //主要内容
                    hashMap2 = (HashMap<Object, Object>) objectList1.get(j);
                    //数据集合
                    objectList2 = (List<Object>) hashMap2.get("data");
                    label2 = new Label(0, row1, "" + a + "", headerFormat1);
                    sheetOne.addCell(label2);
                    label3 = new Label(2, row1, hashMap2.get("MAJOR_CONTENT").toString(), headerFormat1);
                    sheetOne.addCell(label3);
                    for (int n = 0; n < objectList2.size(); n++) {
                        //数据map
                        hashMap3 = (HashMap<Object, Object>) objectList2.get(n);
                        label4 = new Label(3, row2, hashMap3.get("TARGET3").toString(), headerFormat1);
                        label5 = new Label(4, row2, hashMap3.get("TARGET4").toString(), headerFormat1);
                        label6 = new Label(5, row2, hashMap3.get("FACT_OPERATE").toString(), headerFormat1);
                        label7 = new Label(6, row2, hashMap3.get("SCHEDULE_PLAN").toString(), headerFormat1);
                        label8 = new Label(7, row2, hashMap3.get("LIMIT_DATE").toString(), headerFormat1);
                        label9 = new Label(8, row2, hashMap3.get("LIABLE_OFFICE").toString(), headerFormat1);
                        label10 = new Label(9, row2, hashMap3.get("LIABLE_USER").toString(), headerFormat1);
                        label11 = new Label(10, row2, hashMap3.get("CENTRALIED_MGR_BUREAU").toString(), headerFormat1);
                        sheetOne.addCell(label4);
                        sheetOne.addCell(label5);
                        sheetOne.addCell(label6);
                        sheetOne.addCell(label7);
                        sheetOne.addCell(label8);
                        sheetOne.addCell(label9);
                        sheetOne.addCell(label10);
                        sheetOne.addCell(label11);
                        sheetOne.setRowView(row2, 600);
                        row2++;
                        count++;
                    }
                    sheetOne.mergeCells(0, row1, 0, row2 - 1);
                    sheetOne.mergeCells(2, row1, 2, row2 - 1);
                    row1 = row2;
                    a++;
                }
                sheetOne.mergeCells(1, row, 1, row + count - 1);
                row += count;
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 落实《全国审计机关2018年度工作要点》具体措施清单（按内容汇总）
     *
     * @param hashMap
     * @param paramBean
     */
    public static void exportExcelTwelve(HashMap<String, Object> hashMap, ParamBean paramBean) {
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String date = paramBean.getStr("selecttime").substring(0, 4);
        String fileName = "落实《全国审计机关" + date + "年度工作要点》具体措施清单（按内容汇总）";
        WritableWorkbook wwb = null;
        OutputStream out = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            //设置Excel表头样式
            //设置标题的字体大小和样式
            WritableFont writableFont = new WritableFont(WritableFont.createFont("宋体"), 20);
            //设置标题样式
            WritableCellFormat headerFormat = new WritableCellFormat(writableFont);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置Excel单元格表头样式
            //设置单元格的字体大小和样式
            WritableFont writableFont1 = new WritableFont(WritableFont.createFont("宋体"), 13);
            //设置表头样式
            WritableCellFormat headerFormat1 = new WritableCellFormat(writableFont1);
            //水平居中对齐
            headerFormat1.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat1.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat1.setWrap(true);

            //生成第一页工作表
            WritableSheet sheetOne = wwb.createSheet("第一页", 0);

            //设置表头每一列的列宽
            sheetOne.setColumnView(0, 15);
            sheetOne.setColumnView(1, 15);
            sheetOne.setColumnView(2, 15);
            sheetOne.setColumnView(3, 35);
            sheetOne.setColumnView(4, 30);
            sheetOne.setColumnView(5, 30);
            sheetOne.setColumnView(6, 15);
            sheetOne.setColumnView(7, 25);
            sheetOne.setColumnView(8, 20);

            //第一列第一行——第十一列第一行
            Label title1 = new Label(0, 0, "落实《全国审计机关" + date + "年度工作要点》具体措施清单", headerFormat);
            //添加进第一页
            sheetOne.addCell(title1);
            //合并第一列第二行到第八列第二行
            sheetOne.mergeCells(0, 0, 10, 0);
            //设置第一列的单元格列宽
            sheetOne.setColumnView(0, 10);

            int rows = 1;
            if(!paramBean.getStr("st1").equals("全部")){
                //设置Excel表头的行宽
                sheetOne.setRowView(0, 800);
                sheetOne.setRowView(1, 700);
                sheetOne.setRowView(2, 700);

                //第一列第二行——第三列第二行
                Label title2 = new Label(0, rows, "单位：" + paramBean.getStr("st1") + "（局）", headerFormat1);
                //添加进第一页
                sheetOne.addCell(title2);
                //合并第一列第二行到第三列第二行
                sheetOne.mergeCells(0, 1, 2, rows);

                //第四列第二行
                Label title3 = new Label(3, rows, "联系人：" + hashMap.get("USER_CODE") + "（省厅督查员）", headerFormat1);
                //添加进第一页
                sheetOne.addCell(title3);

                //第五列第二行
                Label title4 = new Label(4, rows, "工作电话：" + hashMap.get("USER_TEL"), headerFormat1);
                //添加进第一页
                sheetOne.addCell(title4);

                //第六列第二行
                Label title5 = new Label(5, rows, "手机号码：" + hashMap.get("USER_PHONE"), headerFormat1);
                //添加进第一页
                sheetOne.addCell(title5);
                rows += 1 ;
            }else{
                sheetOne.setRowView(0, 800);
                sheetOne.setRowView(1, 700);
            }

            //动态绑定相同的表头
            for (int i = 0; i <= 8; i++) {
                label1 = new Label(i, rows, str7[i], headerFormat1);
                sheetOne.addCell(label1);
            }

            List<Object> objectList = (List<Object>) hashMap.get("data");
            List<Object> objectList1 = null;
            List<Object> objectList2 = null;
            HashMap<Object, Object> hashMap1 = null;
            HashMap<Object, Object> hashMap2 = null;
            HashMap<Object, Object> hashMap3 = null;
            int row = rows+1;
            int row1 = rows+1;
            int row2 = rows+1;
            int a = 1;
            int count;
            //动态绑定数据
            for (int i = 0; i < objectList.size(); i++) {
                count = 0;
                //主要工作
                hashMap1 = (HashMap<Object, Object>) objectList.get(i);
                //内容集合
                objectList1 = (List<Object>) hashMap1.get("data");
                label1 = new Label(1, row, hashMap1.get("MAJOR_WORK").toString(), headerFormat1);
                sheetOne.addCell(label1);
                for (int j = 0; j < objectList1.size(); j++) {
                    //主要内容
                    hashMap2 = (HashMap<Object, Object>) objectList1.get(j);
                    //数据集合
                    objectList2 = (List<Object>) hashMap2.get("data");
                    label2 = new Label(0, row1, "" + a + "", headerFormat1);
                    sheetOne.addCell(label2);
                    label3 = new Label(2, row1, hashMap2.get("MAJOR_CONTENT").toString(), headerFormat1);
                    sheetOne.addCell(label3);
                    for (int n = 0; n < objectList2.size(); n++) {
                        //数据map
                        hashMap3 = (HashMap<Object, Object>) objectList2.get(n);
                        label4 = new Label(3, row2, hashMap3.get("FACT_OPERATE").toString(), headerFormat1);
                        label5 = new Label(4, row2, hashMap3.get("SCHEDULE_PLAN").toString(), headerFormat1);
                        label6 = new Label(5, row2, hashMap3.get("LIMIT_DATE").toString(), headerFormat1);
                        label7 = new Label(6, row2, hashMap3.get("PROVIN_NAME").toString(), headerFormat1);
                        label8 = new Label(7, row2, hashMap3.get("LIABLE_OFFICE").toString() + "(" + hashMap3.get("LIABLE_USER").toString() + ")", headerFormat1);
                        label9 = new Label(8, row2, hashMap3.get("CENTRALIED_MGR_BUREAU").toString(), headerFormat1);
                        sheetOne.addCell(label4);
                        sheetOne.addCell(label5);
                        sheetOne.addCell(label6);
                        sheetOne.addCell(label7);
                        sheetOne.addCell(label8);
                        sheetOne.addCell(label9);
                        sheetOne.setRowView(row2, 600);
                        row2++;
                        count++;
                    }
                    sheetOne.mergeCells(0, row1, 0, row2 - 1);
                    sheetOne.mergeCells(2, row1, 2, row2 - 1);
                    row1 = row2;
                    a++;
                }
                sheetOne.mergeCells(1, row, 1, row + count - 1);
                row += count;
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        } finally {
        }
    }

    /**
     * 关闭流
     * @param response
     */
    private static void closeStream(WritableWorkbook wookBook, HttpServletResponse response) {
        try {
            if (wookBook != null) {
                wookBook.close();
            }
            if (response != null) {
                response.flushBuffer();
            }
        } catch (Exception e) {
            /*log.error(e.getMessage(), e);*/
        }
    }

    /**
     * 封装继承CommSev的导出Excel方法
     * @param paramBean
     * @return
     */
    public OutBean QueryStatementExcel(ParamBean paramBean,OutBean outBeans){
        //获取指定系统参数
        String servId = paramBean.getServId();
        ServDefBean serv = ServUtils.getServDef(servId);
        paramBean.setQueryPageShowNum(Context.getSyConf("SY_EXP_NUM", 5000));
        this.beforeExp(paramBean);
        //将系统参数传递指定方法获取下载数据
        OutBean outBean = outBeans;
        this.afterExp(paramBean, outBean);
        ExportExcel expExcel = new ExportExcel();
        LinkedHashMap<String, Bean> cols = outBean.getCols();
        LinkedHashMap<String, Bean> expPols = new LinkedHashMap(cols.size());
        if (paramBean.getSelect().equals("*")) {
            boolean allCols = paramBean.getBoolean("_ALL_COLS_");
            Iterator var10 = cols.keySet().iterator();
            while(var10.hasNext()) {
                String key = (String)var10.next();
                Bean item = serv.getItem(key);
                if (item != null) {
                    int listFlag = 1;
                    if (!allCols && item.getInt("ITEM_HIDDEN") == 1) {
                        listFlag = 2;
                    }
                    this.addCols(expPols, item, listFlag);
                }
            }
            cols = expPols;
        }
        return expExcel.exp(serv, outBean.getDataList(), cols);
    }
}
