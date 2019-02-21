package com.rh.food.serv;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.food.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
/**
 * 审计管理分系统行政办公管理子系统
* @author: kfzx-cuiyc
* @date: 2018年11月15日 上午10:40:34
* @version: V1.0
* @description: 综合查询模块关联类
*/
public class OaSvFoodOrderDetViewServ extends CommonServ{
	
	private static Log log = LogFactory.getLog(OaSvFoodOrderDetViewServ.class);
	/**
	 * @title: getBuyNumberSum
	 * @descriptin: 统计食品总数
	 * @param @param paramBean
	 * @param @return
	 * @return OutBean
	 * @throws
	 */
	public OutBean getBuyNumberSum(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		SqlBean sqlBean = new SqlBean();
		//拼接sql语句
		sqlBean.selects("SUM(BUY_NUMBER)");
		if(StringUtils.isNotBlank(paramBean.getStr("USER_NAME"))) {
			sqlBean.andLike("USER_NAME", paramBean.getStr("USER_NAME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("DEPT_NAME"))) {
			sqlBean.andLike("DEPT_NAME", paramBean.getStr("DEPT_NAME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("FOOD_TYPE"))) {
			sqlBean.andLike("FOOD_TYPE", paramBean.getStr("FOOD_TYPE"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("FOOD_NAME"))) {
			sqlBean.andLike("FOOD_NAME", paramBean.getStr("FOOD_NAME"));		
		}
		if(StringUtils.isNotBlank(paramBean.getStr("SERIAL_NUMBER"))) {
			sqlBean.andLike("SERIAL_NUMBER", paramBean.getStr("SERIAL_NUMBER"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("CHECK_CODE"))) {
			sqlBean.andLike("CHECK_CODE", paramBean.getStr("CHECK_CODE"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("BUY_NUMBER"))) {
			sqlBean.andGTE("BUY_NUMBER", paramBean.getStr("BUY_NUMBER"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("BUY_NUMBER2"))) {
			sqlBean.andLTE("BUY_NUMBER", paramBean.getStr("BUY_NUMBER2"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("OBTAIN_START_TIME"))) {
			sqlBean.andGTE("OBTAIN_START_TIME", paramBean.getStr("OBTAIN_START_TIME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("OBTAIN_END_TIME"))) {
			sqlBean.andLTE("OBTAIN_END_TIME", paramBean.getStr("OBTAIN_END_TIME"));
		}
		UserBean bean = Context.getUserBean();
		String roleCodeQuotaStr = bean.getRoleCodeQuotaStr();
		if(roleCodeQuotaStr.indexOf("R_SV_FSPGLY")<0) {
			sqlBean.and("S_USER", bean.getCode());
		}
		log.debug("根据查询条件统计预订食品总数，并将统计值返回");
		//根据拼接的sql，调用服务OA_SV_FOOD_ORDER_DET_V_QUERY，执行查询
		Bean totalNumber=ServDao.find("OA_SV_FOOD_ORDER_DET_V_QUERY", sqlBean);
		outBean.set("result", totalNumber.get("SUM"));
		return outBean;
	}
	
	/**
	 * 重写方法，修改查询条件
	 */
	@Override
	protected void beforeQuery(ParamBean paramBean) {
		//页面打开，首次执行查询时，设置时间查询条件默认值
		if(!paramBean.containsKey("_searchWhere")){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startTime;
			Date endTime=new Date();
			startTime = DateUtil.addDay(endTime, -7);
			String start = formatter.format(startTime);
			String end = formatter.format(endTime);
			String _searchWhere = "and OBTAIN_START_TIME >= '"+start+"' and OBTAIN_END_TIME <= '"+ end +"'";
			paramBean.set("_searchWhere", _searchWhere);
		}else {
			//非页面首次打开执行查询时，处理查询条件
			String search = paramBean.get("_searchWhere").toString();
			search = search.replace("OBTAIN_TIME >=", "OBTAIN_START_TIME >=");
			search = search.replace("OBTAIN_TIME <=", "OBTAIN_END_TIME <=");
			//取出“数量”值，重新拼接where条件
			String searchReady = "";
			String searchReadys = "";
			if(search.contains("BUY_NUMBER") || search.contains("undefined")) {
				String[] wheres = search.split("and");
				int length = wheres.length;
				for(int i=0;i<length;i++) {
					if(wheres[i].contains("BUY_NUMBER")) {
						int a = wheres[i].indexOf("%");
						int b = wheres[i].lastIndexOf("%");
						String numStart = wheres[i].substring(a+1, b);
						searchReady = searchReady + " and BUY_NUMBER >="+ numStart;
					}else if(wheres[i].contains("undefined")) {
						int a = wheres[i].indexOf("%");
						int b = wheres[i].lastIndexOf("%");
						String numEnd = wheres[i].substring(a+1, b);
						searchReady = searchReady + " and BUY_NUMBER <="+ numEnd;
					}else if(StringUtils.isNotBlank(wheres[i])){
						searchReadys =searchReadys+ " and " + wheres[i].toString(); 
					}
					search = searchReadys + searchReady;
				}
			}
			log.debug("在beforeQuery中修改系统默认查询条件");
			paramBean.set("_searchWhere", search);
		}
	}

	/**
	 * @title: getOrderDetailExp
	 * @descriptin: 导出功能
	 * @param @param paramBean
	 * @param @return
	 * @return void
	 * @throws
	 */
	public void getOrderDetailExp(ParamBean paramBean) {
		SqlBean sqlBeanOne = new SqlBean();
		SqlBean sqlBeanTwo = new SqlBean();
		sqlBeanOne.selects("USER_NAME,DEPT_NAME,FOOD_TYPE,FOOD_NAME,BUY_NUMBER,SERIAL_NUMBER,CHECK_CODE,OBTAIN_TIME");
		
		sqlBeanTwo.selects(" FOOD_TYPE,FOOD_NAME,OBTAIN_TIME,SUM(BUY_NUMBER)");
		sqlBeanTwo.groups("FOOD_TYPE,FOOD_NAME,OBTAIN_TIME");
		
		if(StringUtils.isNotBlank(paramBean.getStr("USER_NAME"))) {
			sqlBeanOne.andLike("USER_NAME", paramBean.getStr("USER_NAME"));
			sqlBeanTwo.andLike("USER_NAME", paramBean.getStr("USER_NAME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("DEPT_NAME"))) {
			sqlBeanOne.andLike("DEPT_NAME", paramBean.getStr("DEPT_NAME"));
			sqlBeanTwo.andLike("DEPT_NAME", paramBean.getStr("DEPT_NAME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("FOOD_TYPE"))) {
			sqlBeanOne.andLike("FOOD_TYPE", paramBean.getStr("FOOD_TYPE"));
			sqlBeanTwo.andLike("FOOD_TYPE", paramBean.getStr("FOOD_TYPE"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("FOOD_NAME"))) {
			sqlBeanOne.andLike("FOOD_NAME", paramBean.getStr("FOOD_NAME"));
			sqlBeanTwo.andLike("FOOD_NAME", paramBean.getStr("FOOD_NAME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("SERIAL_NUMBER"))) {
			sqlBeanOne.andLike("SERIAL_NUMBER", paramBean.getStr("SERIAL_NUMBER"));
			sqlBeanTwo.andLike("SERIAL_NUMBER", paramBean.getStr("SERIAL_NUMBER"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("CHECK_CODE"))) {
			sqlBeanOne.andLike("CHECK_CODE", paramBean.getStr("CHECK_CODE"));
			sqlBeanTwo.andLike("CHECK_CODE", paramBean.getStr("CHECK_CODE"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("BUY_NUMBER"))) {
			sqlBeanOne.andGTE("BUY_NUMBER", paramBean.getStr("BUY_NUMBER"));
			sqlBeanTwo.andGTE("BUY_NUMBER", paramBean.getStr("BUY_NUMBER"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("BUY_NUMBER2"))) {
			sqlBeanOne.andLTE("BUY_NUMBER", paramBean.getStr("BUY_NUMBER2"));
			sqlBeanTwo.andLTE("BUY_NUMBER", paramBean.getStr("BUY_NUMBER2"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("OBTAIN_START_TIME"))) {
			sqlBeanOne.andGTE("OBTAIN_START_TIME", paramBean.getStr("OBTAIN_START_TIME"));
			sqlBeanTwo.andGTE("OBTAIN_START_TIME", paramBean.getStr("OBTAIN_START_TIME"));
		}
		if(StringUtils.isNotBlank(paramBean.getStr("OBTAIN_END_TIME"))) {
			sqlBeanOne.andLTE("OBTAIN_END_TIME", paramBean.getStr("OBTAIN_END_TIME"));
			sqlBeanTwo.andLTE("OBTAIN_END_TIME", paramBean.getStr("OBTAIN_END_TIME"));
		}
		UserBean bean = Context.getUserBean();
		String roleCodeQuotaStr = bean.getRoleCodeQuotaStr();
		if(roleCodeQuotaStr.indexOf("R_SV_FSPGLY")<0) {
			sqlBeanOne.and("S_USER", bean.getCode());
			sqlBeanTwo.and("S_USER", bean.getCode());
		}
		
		List<Bean> beanOne = ServDao.finds("OA_SV_FOOD_ORDER_DET_V_QUERY", sqlBeanOne);
		List<Bean> beanTwo = ServDao.finds("OA_SV_FOOD_ORDER_DET_V_QUERY", sqlBeanTwo);
		log.debug("根据查询出的list，生成对应excel");
		exportExcel(beanOne,beanTwo,paramBean);
	}
	
	/**
	 * @title: exportExcel
	 * @descriptin: 流的方式实现导出功能
	 * @param @param beanList
	 * @param @param paramBean
	 * @return void
	 * @throws
	 */
	public static void exportExcel(List<Bean> beanListOne, List<Bean> beanListTwo, ParamBean paramBean) {
       HttpServletResponse response = Context.getResponse();
       HttpServletRequest request = Context.getRequest();
       SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
       String fileName = "副食品综合查询-"+ formatter.format(new Date());
       WritableWorkbook wwb = null;
       try {
           response.resetBuffer();
           response.setContentType("application/x-msdownload");
           response.setCharacterEncoding("UTF-8");
           com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
           OutputStream out = response.getOutputStream();
           wwb = Workbook.createWorkbook(out);

           //设置标题的字体大小和样式
           WritableFont wfc = new WritableFont(WritableFont.createFont("宋体"),13);
           //设置单元格样式
           WritableCellFormat headerFormats = new WritableCellFormat(wfc);
           //水平居中对齐
           headerFormats.setAlignment(Alignment.CENTRE);
           //竖直方向居中对齐
           headerFormats.setVerticalAlignment(VerticalAlignment.CENTRE);

           //设置文字的字体大小和样式
           WritableFont wfcs = new WritableFont(WritableFont.createFont("宋体"),10);
           //设置单元格样式
           WritableCellFormat headerFormat = new WritableCellFormat(wfcs);
           //水平居中对齐
           headerFormat.setAlignment(Alignment.CENTRE);
           //竖直方向居中对齐
           headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
           //自动换行
           headerFormat.setWrap(true);

           //生成第一页工作表
           WritableSheet sheetOne=wwb.createSheet("食品明细",0);
           //设置sheet1表头行高
           sheetOne.setRowView(0, 500);
           //设置sheet1列宽
           sheetOne.setColumnView(0, 11);
           sheetOne.setColumnView(1, 11);
           sheetOne.setColumnView(2, 12);
           sheetOne.setColumnView(3, 12);
           sheetOne.setColumnView(4, 10);
           sheetOne.setColumnView(5, 10);
           sheetOne.setColumnView(6, 10);
           sheetOne.setColumnView(7, 40);

           //sheet1第一列第一行
           Label title=new Label(0,0,"姓名",headerFormats);
           //添加进第一页
           sheetOne.addCell(title);
           
           //sheet1第一列第二行
           Label title1=new Label(1,0,"部门",headerFormats);
           //添加进第一页
           sheetOne.addCell(title1);
           
           //sheet1第一列第三行
           Label title2=new Label(2,0,"食品类别",headerFormats);
           //添加进第一页
           sheetOne.addCell(title2);
           
           //sheet1第一列第四行
           Label title3=new Label(3,0,"食品名称",headerFormats);
           //添加进第一页
           sheetOne.addCell(title3);
           
           //sheet1第一列第五行
           Label title4=new Label(4,0,"数量",headerFormats);
           //添加进第一页
           sheetOne.addCell(title4);
           
           //sheet1第一列第六行
           Label title5=new Label(5,0,"序号",headerFormats);
           //添加进第一页
           sheetOne.addCell(title5);
           
           //sheet1第一列第七行
           Label title6=new Label(6,0,"校验码",headerFormats);
           //添加进第一页
           sheetOne.addCell(title6);
           
           //sheet1第一列第八行
           Label title7=new Label(7,0,"领取时间段",headerFormats);
           //添加进第一页
           sheetOne.addCell(title7);
           
           //动态绑定数据
           for( int i = 0 ; i < beanListOne.size() ; i++ ){
               Bean beanOne = beanListOne.get(i);
               sheetOne.addCell(new Label(0,1+i,beanOne.getStr("USER_NAME"),headerFormat));
               sheetOne.addCell(new Label(1,1+i,beanOne.getStr("DEPT_NAME"),headerFormat));
               sheetOne.addCell(new Label(2,1+i,beanOne.getStr("FOOD_TYPE"),headerFormat));
               sheetOne.addCell(new Label(3,1+i,beanOne.getStr("FOOD_NAME"),headerFormat));
               sheetOne.addCell(new Label(4,1+i,beanOne.getStr("BUY_NUMBER"),headerFormat));
               sheetOne.addCell(new Label(5,1+i,beanOne.getStr("SERIAL_NUMBER"),headerFormat));
               sheetOne.addCell(new Label(6,1+i,beanOne.getStr("CHECK_CODE"),headerFormat));
               sheetOne.addCell(new Label(7,1+i,beanOne.getStr("OBTAIN_TIME"),headerFormat));
           }
           
           //生成第二页工作表
           WritableSheet sheetTwo=wwb.createSheet("食品统计",1);
           
           //设置sheet2表头行高
           sheetTwo.setRowView(0, 500);
           //设置sheet2列宽
           sheetTwo.setColumnView(0, 12);
           sheetTwo.setColumnView(1, 12);
           sheetTwo.setColumnView(2, 40);
           sheetTwo.setColumnView(3, 10);
           //sheet2第一列第一行
           Label label=new Label(0,0,"食品类别",headerFormats);
           //添加进第二页
           sheetTwo.addCell(label);
           
           //sheet2第一列第二行
           Label label1=new Label(1,0,"食品名称",headerFormats);
           //添加进第二页
           sheetTwo.addCell(label1);
           
           //sheet2第一列第三行
           Label label2=new Label(2,0,"领取时间段",headerFormats);
           //添加进第二页
           sheetTwo.addCell(label2);
           
           //sheet2第一列第四行
           Label label3=new Label(3,0,"已订数量",headerFormats);
           //添加进第二页
           sheetTwo.addCell(label3);
           //动态绑定数据
           for( int i = 0 ; i < beanListTwo.size() ; i++ ){
               Bean beanTwo = beanListTwo.get(i);
               sheetTwo.addCell(new Label(0,1+i,beanTwo.getStr("FOOD_TYPE"),headerFormat));
               sheetTwo.addCell(new Label(1,1+i,beanTwo.getStr("FOOD_NAME"),headerFormat));
               sheetTwo.addCell(new Label(2,1+i,beanTwo.getStr("OBTAIN_TIME"),headerFormat));
               sheetTwo.addCell(new Label(3,1+i,beanTwo.getStr("SUM"),headerFormat));
           }
           wwb.write();
           //关闭流
           closeStream(wwb, response);
       } catch (Exception e) {
       	e.printStackTrace();
       	log.error("生成excel异常，异常信息为：" + e.getMessage() + "," + e.getCause().getMessage());
       	throw new TipException("生成excel异常，异常信息为：" + e.getMessage() + "," + e.getCause().getMessage());
       }
   }
	
	/**
	 * @title: closeStream
	 * @descriptin: 关闭流
	 * @param @param wookBook
	 * @param @param response
	 * @return void
	 * @throws
	 */
	private static void closeStream(WritableWorkbook wookBook, HttpServletResponse response){
       if (wookBook != null) {
           try {
               wookBook.close();
           } catch (Exception e) {
           	e.printStackTrace();
           }
       }
       if (response != null && !response.isCommitted()) {
           try {
               response.flushBuffer();
           } catch (Exception e) {
           	e.printStackTrace();
           }
       }
   }
	/**
	 * @title导出维护单数据excel
	 * @param paramBean
	 * @return url
	 */
	public void getDetailExp(ParamBean paramBean) {
		
		//标题
		String title = paramBean.getStr("MAINTAIN_TITLE");
		//维护主键ID
		String maintainId = paramBean.getStr("MAINTAIN_ID");
			
		List<Bean> beanOne = null;
		List<Bean> beanTwo = null;
		if(StringUtils.isNotBlank(maintainId)) {
			
			String sql1 = "select USER_NAME,DEPT_NAME,FOOD_TYPE,FOOD_NAME,BUY_NUMBER,SERIAL_NUMBER,CHECK_CODE,OBTAIN_TIME "
					+ "from OA_SV_FOOD_ORDER_DET_V where MAINTAIN_ID='"+maintainId+"' and rownum<50000";
			
			String sql2 = "select FOOD_TYPE,FOOD_NAME,OBTAIN_TIME,SUM(BUY_NUMBER) " + 
					"from OA_SV_FOOD_ORDER_DET_V where MAINTAIN_ID='"+maintainId+"' and rownum<50000 group by FOOD_TYPE,FOOD_NAME,OBTAIN_TIME";
			
			beanOne = Transaction.getExecutor().query(sql1);
			beanTwo = Transaction.getExecutor().query(sql2);
		}		
	    exportFoodExcel(beanOne,beanTwo,paramBean,title);
	}
	
	/*
	 * 处理list，输出excel
	 */
	public static void exportFoodExcel(List<Bean> beanListOne, List<Bean> beanListTwo, ParamBean paramBean,String excelTitle) {
		 HttpServletResponse response = Context.getResponse();
	     HttpServletRequest request = Context.getRequest();
		 WritableWorkbook wwb  = null;       
        //导出的文件名
        Long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
       // String url = "\\oa\\food\\util\\"+excelTitle+ sdf.format(date) +".xls";
        String fileName =excelTitle+sdf.format(date);
        try {   
        	//io读写文件
        	 response.resetBuffer();
             response.setContentType("application/x-msdownload");
             com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
             OutputStream out = response.getOutputStream();
            //创建Excel
             wwb = Workbook.createWorkbook(out);

             //设置标题的字体大小和样式
             WritableFont wfc = new WritableFont(WritableFont.createFont("宋体"),13);
             //设置单元格样式
             WritableCellFormat headerFormats = new WritableCellFormat(wfc);
             //水平居中对齐
             headerFormats.setAlignment(Alignment.CENTRE);
             //竖直方向居中对齐
             headerFormats.setVerticalAlignment(VerticalAlignment.CENTRE);

             //设置文字的字体大小和样式
             WritableFont wfcs = new WritableFont(WritableFont.createFont("宋体"),10);
             //设置单元格样式
             WritableCellFormat headerFormat = new WritableCellFormat(wfcs);
             //水平居中对齐
             headerFormat.setAlignment(Alignment.CENTRE);
             //竖直方向居中对齐
             headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
             //自动换行
             headerFormat.setWrap(true);

            //生成第一页工作表
            WritableSheet sheetOne=wwb.createSheet("食品明细",0);
            //设置sheet1表头行高
            sheetOne.setRowView(0, 500);
            //设置sheet1列宽
            sheetOne.setColumnView(0, 11);
            sheetOne.setColumnView(1, 20);
            sheetOne.setColumnView(2, 20);
            sheetOne.setColumnView(3, 30);
            sheetOne.setColumnView(4, 40);
            sheetOne.setColumnView(5, 10);
            sheetOne.setColumnView(6, 10);
            sheetOne.setColumnView(7, 40);
            //sheet1第一列第一行
            String[] title_name_one={"序号","姓名","部门","食品类别","食品名称","数量","校验码","领取时间段"};
            for (int i = 0; i <8; i++) {
         	   Label title=new Label(i,0,title_name_one[i],headerFormat);
         	   sheetOne.addCell(title);
		     	}
                     
            //动态绑定数据
            for( int i = 0 ; i < beanListOne.size() ; i++ ){
                Bean beanOne = beanListOne.get(i);  
                sheetOne.addCell(new Label(0,1+i,beanOne.getStr("SERIAL_NUMBER"),headerFormat));
                sheetOne.addCell(new Label(1,1+i,beanOne.getStr("USER_NAME"),headerFormat));
                sheetOne.addCell(new Label(2,1+i,beanOne.getStr("DEPT_NAME"),headerFormat));
                sheetOne.addCell(new Label(3,1+i,beanOne.getStr("FOOD_TYPE"),headerFormat));
                sheetOne.addCell(new Label(4,1+i,beanOne.getStr("FOOD_NAME"),headerFormat));
                sheetOne.addCell(new Label(5,1+i,beanOne.getStr("BUY_NUMBER"),headerFormat));
                sheetOne.addCell(new Label(6,1+i,beanOne.getStr("CHECK_CODE"),headerFormat));
                sheetOne.addCell(new Label(7,1+i,beanOne.getStr("OBTAIN_TIME"),headerFormat));
            }
            
            //生成第二页工作表
            WritableSheet sheetTwo=wwb.createSheet("食品统计",1);
            
            //设置sheet2表头行高
            sheetTwo.setRowView(0, 500);
            //设置sheet2列宽
            sheetTwo.setColumnView(0, 50);
            sheetTwo.setColumnView(1, 50);
            sheetTwo.setColumnView(2, 50);
            sheetTwo.setColumnView(3, 10);
            //sheet2第一列第一行          
            String[] title_name_tow={"食品类别","食品名称","领取时间段","已订数量"};
            for (int i = 0; i <4; i++) {
         	   Label title=new Label(i,0,title_name_tow[i],headerFormat);
         	   sheetTwo.addCell(title);
		     	}
                    
           
            //动态绑定数据
            for( int i = 0 ; i < beanListTwo.size() ; i++ ){
                Bean beanTwo = beanListTwo.get(i);
                sheetTwo.addCell(new Label(0,1+i,beanTwo.getStr("FOOD_TYPE"),headerFormat));
                sheetTwo.addCell(new Label(1,1+i,beanTwo.getStr("FOOD_NAME"),headerFormat));
                sheetTwo.addCell(new Label(2,1+i,beanTwo.getStr("OBTAIN_TIME"),headerFormat));
                sheetTwo.addCell(new Label(3,1+i,beanTwo.getStr("SUM"),headerFormat));
            }
            
            wwb.write();      
        } catch (Exception e) {
        	e.printStackTrace();
           	log.error("生成excel异常，异常信息为：" + e.getMessage() + "," + e.getCause().getMessage());
           	throw new TipException("生成excel异常，异常信息为：" + e.getMessage() + "," + e.getCause().getMessage());
        }finally{
        	 //关闭流
            closeStream(wwb, response);
        }
       
    }
	
	/**
	 * 
	 * @title: getNumOfDetail
	 * @descriptin: 该方法用于查询MAINTAIN_ID下有多少条Detail数据
	 * @param @param paramBean
	 * @param @return
	 * @return OutBean
	 * @throws
	 */
	public OutBean getNumOfDetail(ParamBean paramBean) {
		
		OutBean outBean = new OutBean();
		String maintainId = paramBean.getStr("MAINTAIN_ID");
		if(StringUtils.isNotBlank(maintainId)) {
			String sql = "select count(*) ORDERCOUNT " + 
					"from OA_SV_FOOD_ORDER_DET_V where MAINTAIN_ID='"+maintainId+"'";
			Bean bean = Transaction.getExecutor().queryOne(sql);
			if(bean != null) {
				return outBean.set("result", bean.getStr("ORDERCOUNT"));
			}
		}
		return outBean.set("result", "0");
	}
	

}
