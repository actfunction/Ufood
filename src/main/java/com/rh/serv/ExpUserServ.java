package com.rh.serv;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
/*
 * @auther kfzx-xuyj01
 */
public class ExpUserServ extends CommonServ{

	 /**
     * 提供基于列表的查询服务
     * @param paramBean    参数Bean
     * @return 查询结果
     */
   private String SSO_URL = Context.getSyConf("SSO_URL", "http://localhost:9006/");
   public OutBean imp(ParamBean paramBean) {
	   OutBean outBean = new OutBean();
	   beforeImp(paramBean); //执行监听方法
       String servId = paramBean.getServId();
       ServDefBean servDef = ServUtils.getServDef(servId);
       LinkedHashMap<String, Bean> titleMap = BeanUtils.toLinkedMap(servDef.getTableItems(), "ITEM_NAME");
       String fileId = paramBean.getStr("fileId");
       Bean fileBean = FileMgr.getFile(fileId);
       if (fileBean != null && fileBean.getStr("FILE_MTYPE").equals("application/vnd.ms-excel")) { //ֻ֧��excel����
           Workbook book = null;
           InputStream in = null;
           try {
               in = FileMgr.download(fileBean);
             //打开文件
               try {
                   book = Workbook.getWorkbook(in) ;
               } catch(Exception e) {
                   log.error(e.getMessage(), e);
                   throw new RuntimeException("Wrong file format, only suport 2003 and lower version," 
                           + "pls use export excel file as the template!");
               }
             //取得第一个sheet  
               Sheet sheet = book.getSheet(0);  
             //取得行数  
               int rows = sheet.getRows();
               List<Bean> dataList = new ArrayList<Bean>(rows);
               Cell[] titleCell = sheet.getRow(0);
               int cols = titleCell.length;
               Bean[] itemMaps = new Bean[cols];
               for (int j = 0; j < cols; j++) { //第一行标题列，进行标题与字段的自动匹配，优先匹配中文名称，其次配置编码
                   String title = sheet.getCell(j, 0).getContents();
                   Bean itemMap = null;
                   if (titleMap.containsKey(title)) {
                       itemMap = titleMap.get(title);
                   } else {
                       itemMap = servDef.getItem(title);
                   }
                   if (itemMap != null) {
                       itemMaps[j] = itemMap;
                   }
               }
               for(int i = 1; i < rows; i++) {
                   Cell [] cell = sheet.getRow(i);
                   Bean data = new Bean();
                   for(int j = 0; j < cell.length; j++) {
                       if (itemMaps[j] != null) {
                           String value = sheet.getCell(j, i).getContents();
                           if (itemMaps[j].isNotEmpty("DICT_ID")) { //字典处理名称和值的转换
                               String dictVal = DictMgr.getItemCodeByName(itemMaps[j].getStr("DICT_ID"), value);
                               if (dictVal != null) {
                                   value = dictVal;
                               }
                           }
                           data.set(itemMaps[j].getStr("ITEM_CODE"), value);
                       }
                   }
                   dataList.add(data);
               }  
             //关闭文件  
               book.close();
               book = null;
               if (dataList.size() > 0) {
                   ParamBean param = new ParamBean(servId, ServMgr.ACT_BATCHSAVE);
                   param.setBatchSaveDatas(dataList);
                   outBean = ServMgr.act(param);
               } else {
                   outBean.setWarn("");
               }
           } catch (Exception e) {  
               throw new RuntimeException(e.getMessage(), e);  
           } finally {
               if (book != null) {
                   book.close();
               }
               IOUtils.closeQuietly(in);
           }
       } else {//错误的文件内容或格式
           outBean.setWarn("");
       }
       FileMgr.deleteFile(fileBean); //最后删除临时上传的文件
	 
       //todo 自动生产角色
	   return outBean;
   }
   
   public OutBean getUserFileUrl(ParamBean paramBean) {
	   OutBean outBean = new OutBean();
	   String fileUrl  = Context.getSyConf("SSO_URL", "excel/export/user");
	   outBean.set("FILE_URL", SSO_URL+fileUrl);
	   return outBean;
	   
   }
   public OutBean getDeptFileUrl(ParamBean paramBean) {
	   OutBean outBean = new OutBean();
	   String fileUrl  = Context.getSyConf("SSO_URL", "excel/export/org");
	   outBean.set("FILE_URL", SSO_URL+fileUrl);
	   return outBean;
	   
   }
   public OutBean getDeptUserFileUrl(ParamBean paramBean) {
	   OutBean outBean = new OutBean();
	   String fileUrl  = Context.getSyConf("SSO_URL", "excel/export/org_user");
	   outBean.set("FILE_URL", SSO_URL+fileUrl);
	   return outBean;
	   
   }
}
