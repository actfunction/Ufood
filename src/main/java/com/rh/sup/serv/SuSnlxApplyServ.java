package com.rh.sup.serv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
/**
 * 署内立项扩展类
 * @author admin
 *
 */
public class SuSnlxApplyServ extends CommonServ{
	//司局长补登意见后删除待办，增加已办
	public OutBean deleteTodoAndSaveTodoHis(ParamBean paramBean){
		SqlBean sqlBean = new SqlBean();
		SqlBean whereBean = new SqlBean();
		sqlBean.and("TODO_ID", paramBean.getStr("todoId"));
		SimpleDateFormat sdf = new SimpleDateFormat("YY-MM-dd hh:mm:ss");
		Bean todoBean = ServDao.find("SY_COMM_TODO", sqlBean);
		todoBean.set("TODO_FINISH_TIME", sdf.format(new Date()));
		whereBean.and("TODO_ID", todoBean.getStr("TODO_ID"));
		try {
			ServDao.create("SY_COMM_TODO_HIS", todoBean);
			ServDao.delete("SY_COMM_TODO", whereBean);
		} catch (Exception e) {
			return new OutBean().set("MSG", "ERROR");
		}
		return new OutBean().set("MSG", "OK");
	}
	//获取领导和督查员列表
	public OutBean getSupLeaderListByDeptAndRole(ParamBean paramBean) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects("USER_CODE,USER_NAME,DEPT_CODE,ROLE_CODE");
		sqlBean.and("TDEPT_CODE", paramBean.getStr("deptCode"));
		sqlBean.and("ROLE_CODE", paramBean.getStr("leaderRoleCode"));
		List<Bean> leaderList = ServDao.finds("SY_ORG_ROLE_USER", sqlBean);
		sqlBean = new SqlBean();
		sqlBean.selects("USER_CODE,USER_NAME,DEPT_CODE,ROLE_CODE");
		sqlBean.and("TDEPT_CODE", paramBean.getStr("deptCode"));
		sqlBean.and("ROLE_CODE", paramBean.getStr("userRoleCode"));
		List<Bean> userList = ServDao.finds("SY_ORG_ROLE_USER", sqlBean);
		OutBean outBean = new OutBean();
		outBean.set("leaderList", leaderList);
		outBean.set("userList", userList);
		return outBean;
	}
	public void exportWord(){
		Configuration configuration = new Configuration();
		try {
			configuration.setClassForTemplateLoading(SuSnlxApplyServ.class, "/com/rh/oa/sup/serv");
			configuration.setObjectWrapper(new DefaultObjectWrapper());
			configuration.setDefaultEncoding("UTF-8");
			Template template = configuration.getTemplate("static.html");
			SqlBean sql = new SqlBean();
			sql.selects("APPLY_CODE,MIND_TYPE,DO_TYPE,DC_SX");
			sql.and("ID", "2TvhQTvWsINeEV70gQjlR6Ir");
			Map<Object, Object> paramMap = ServDao.find("SU_SNLX_APPLY", sql);
			Map<Object, Object> pMap = new HashMap<>();
			pMap.put("paramMap", paramMap);
			String outFilePath = "D:\\dclx.doc";
			File docFile = new File(outFilePath);
			FileOutputStream fos = new FileOutputStream(docFile);
			Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"), 10240);
			template.process(pMap, writer);
			if(writer != null){
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
