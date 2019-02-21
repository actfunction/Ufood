package com.rh.core.base.start;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.comm.FileStorage;
import com.rh.core.util.Constant;
import com.rh.core.util.file.FileHelper;


/**
 * 升级管理的初始化处理，解压JSP，执行SQL等
 * 
 * @author wanglong
 */
public class UpgradeMgr {
    /** log 日志 */
    private static Log log = LogFactory.getLog(UpgradeMgr.class);
	/**
	 * 初始化升级处理。
	 */
	public void start() {
	    String pro = BaseContext.appStr(APP.SYSPATH);
	    String webinfoDoc = BaseContext.appStr(APP.WEBINF_DOC);
	    String todoUpgrade = webinfoDoc + "UPGRADE_TODO";
	    String doneUpgrade = webinfoDoc + "UPGRADE_DONE";
	    int fileCount = 0;
	    int sqlCount = 0;
	    int sqlError = 0;
	    StringBuilder errorSql = new StringBuilder();
	    String dbType = BaseContext.getDSBean().getStr(DS.DB_TYPE).toLowerCase();
	    try {
	        if (FileStorage.exists(todoUpgrade)) {
    	        String[] todos = FileStorage.list(todoUpgrade);
    	        Arrays.sort(todos);
    	        File donePath = new File(doneUpgrade);
    	        for (String todo : todos) {
    	            File todoFile = new File(todoUpgrade, todo);
    	            if (todo.endsWith(".zip")) { //解压JSP到指定目录
    	                fileCount = FileHelper.unzipFile(pro, todoFile);
    	            } else if (todo.endsWith(".sql")) { //执行SQL
    	                if (todo.startsWith("db") && todo.indexOf(dbType) < 0) { //忽略建库的非本数据库类型语句
    	                    todoFile.delete();
    	                    continue;
    	                }
    	                String sqlText = FileUtils.readFileToString(todoFile, Constant.ENCODING);
    	                String[] sqls = sqlText.split(";[\\n\\r]"); //;加上换行符作为分隔符
    	                for (String sql : sqls) {
                            if (sql.matches("\\s*(--|commit;).*")) { //忽略无效语句
                                continue;
                            }
    	                    try {
    
    	                        BaseContext.getExecutor().execute(sql);
    	                        sqlCount++;
    	                    } catch (Exception se) {
    	                        errorSql.append(sql).append(Constant.STR_ENTER);
    	                        sqlError++;
    	                    }
    	                }
    	            }
    	            //升级成功后将文件移动到已升级目录
    	            FileUtils.copyFileToDirectory(todoFile, donePath);
    	            FileUtils.deleteQuietly(todoFile);
    	            System.out.println("Upgrade is OK!.......................");
    	        } //end for
	        }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	    if (fileCount > 0 || sqlCount > 0) {
	        log.info("upgrade ok，files: " + fileCount + " sqls:" + sqlCount);
	    }
	    if (sqlError > 0) {
	        log.error("upgrade " + sqlError + " sql error!  error list:" + Constant.STR_ENTER + errorSql);
	    }
	}
	   
    /**
     * 
     */
    public void stop() {
    }
}