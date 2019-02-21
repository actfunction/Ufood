package com.rh.gw.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Context;
import com.rh.core.comm.FileStorage;
import com.rh.core.util.DateUtils;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.util.scheduler.RhLocalJob;


/**
 * 密网导入数据
 * 
 * @author yxb
 * 
 */
public class MFileMove extends RhLocalJob {

	/** 第一次执行标志 */
	private static boolean isFirst = true;

	private static Logger log = Logger.getLogger(MFileMove.class);

	/**
	 * 构造函数
	 */
	public MFileMove() {
	}
	
	/**
	 * KETTLE配置文件
	 */
	private static Properties pro = new Properties();
	
	static{
		try {
			pro.load(new FileInputStream(new File(BaseContext.appStr(APP.WEBINF)+"/kettleCon.properties")));
		}  catch (Exception e) {
			log.error("---------------------------------"+BaseContext.appStr(APP.WEBINF)+"/kettleCon.properties");
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * KETTLE脚本执行代码
	 * @param jobname  转换作业的路径
	 */
	public static void runJob(String jobname) {
		try {
			KettleEnvironment.init();
			// jobname 是Job脚本的路径及名称
			JobMeta jobMeta = new JobMeta(jobname, null);
			Job job = new Job(null, jobMeta);
			// 向Job 脚本传递参数，脚本中获取参数值：${参数名}
			// job.setVariable(paraname, paravalue);
			job.start();
			job.waitUntilFinished();
			if (job.getErrors() > 0) {
				System.out.println("decompress fail!");
			}
		} catch (KettleException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	public static void runTrans(String filename) {
		try {
			KettleEnvironment.init();
			TransMeta transMeta = new TransMeta(filename);
			Trans trans = new Trans(transMeta);
			trans.prepareExecution(null);
			trans.startThreads();
			trans.waitUntilFinished();
			if (trans.getErrors() != 0) {
				System.out.println("Error");
			}
		} catch (KettleXMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KettleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 实现Job方法，进行定义调度处理
	 * 
	 * @param context
	 *            调度上下文信息
	 * 
	 */
	@Override
	public void executeJob(RhJobContext context) {
		try{
			//将临时目录下的临时文件删除
			deleteDir(new File(pro.getProperty("fileMwTem")));
			//获取日期
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			//把密网的数据从指定目录读取到临时目录
			String zwPath = pro.getProperty("fileMwName")+date ;
			//密网将数据文件从指定的路径复制到本地临时目录
			moveFiles(zwPath,pro.getProperty("fileMwTem"));
			runJob(pro.getProperty("jobIn"));
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		
		String wzFileUrl=Context.getSyConf("OA_TRAN_FILE_ROOT_PATH", System.getProperty("java.io.tmpdir"));
        if (!wzFileUrl.endsWith("/") && !wzFileUrl.endsWith(File.separator)) {
        	wzFileUrl += "/";
        }
        wzFileUrl += DateUtils.getDate();
        // 获得要转移到的路径
		String mfilesUrl = Context.getSyConf("SY_COMM_FILE_ROOT_PATH", System.getProperty("java.io.tmpdir"));
		System.out.println(wzFileUrl);
		System.out.println(mfilesUrl);
		gwFilesMove(wzFileUrl, mfilesUrl);
		deleteDir(new File(mfilesUrl));
	}
	// 专网转密网数据迁移
	public void gwFilesMove(String oldPath, String newPath) {
		String[] filePaths = new File(oldPath).list();
		if(null!=filePaths){
			for (int i = 0; i < filePaths.length; i++) {
				if (new File(oldPath + File.separator + filePaths[i]).isDirectory()) {
					gwFilesMove(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i]);
				} else if (new File(oldPath + File.separator + filePaths[i]).isFile()) {
					FileInputStream in=null;
					FileInputStream ins=null;
					if (new File(newPath + File.separator + filePaths[i]).exists()) {
						try {
						 in = new FileInputStream(oldPath + File.separator + filePaths[i]);
						 ins = new FileInputStream(newPath + File.separator + filePaths[i]);
							if (DigestUtils.md5Hex(in).equals(
									DigestUtils.md5Hex(ins))) {
				
							} else {
							/*	FileInputStream in = null;
								FileOutputStream out = null;
								// 如果文件存在并且不一样 直接覆盖
								copyFile(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i],
										in, out);*/
								try {
									FileStorage.saveFile(FileStorage.getInputStream(oldPath + File.separator + filePaths[i]), newPath + File.separator + filePaths[i]);
								} catch (IOException e) {
									log.error("在"+DateUtils.getDate()+"这个文件"+oldPath + File.separator + filePaths[i]+"没有复制成功");
									e.printStackTrace();
								}
							}
							new File(oldPath + File.separator + filePaths[i]).delete();
						} catch (IOException e) {
							log.error("md5比较错误");
							e.printStackTrace();
						}finally{
								if(in!=null){
									try {
										in.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								if(ins!=null){
									try {
										ins.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
						}
					} else {
						/*FileInputStream in = null;
						FileOutputStream out = null;
						copyFile(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i], in, out);*/
						try {
							FileStorage.saveFile(FileStorage.getInputStream(oldPath + File.separator + filePaths[i]), newPath + File.separator + filePaths[i]);
						} catch (IOException e) {
							log.error("在"+DateUtils.getDate()+"这个文件"+oldPath + File.separator + filePaths[i]+"没有复制成功");
							e.printStackTrace();
						}
						new File(oldPath + File.separator + filePaths[i]).delete();
					}
				}
			}
		}else{
			log.error("在"+DateUtils.getDate()+"传入的路径有错误 在MFileMove.java中 系统没有找到路径");
		}
	}


	@Override
	public void interrupt() {

	}
	
	//专网转密网数据迁移
	public void moveFiles(String oldPath, String newPath){
		String[] filePaths = new File(oldPath).list();
		if (filePaths.length > 0){
			if (!new File(newPath).exists()){
				new File(newPath).mkdirs();
			}

			for (int i=0; i<filePaths.length; i++){
				if (new File(oldPath + File.separator + filePaths[i]).isDirectory()){
					moveFiles(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i]);
				}else if (new File(oldPath + File.separator + filePaths[i]).isFile()){
					//复制文件到另一个目录
					try{
						copyFiles(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i]);
					} catch (Exception e) {
						log.error(e.getMessage());
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
	
    private static boolean deleteDir(File dir) {
        if (!dir.exists()) return false;
        if (dir.isDirectory()) {
            String[] childrens = dir.list();
            // 递归删除目录中的子目录下
            for (String child : childrens) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
        }
        return true;
    }
	
	
	public void copyFiles(String oldPath, String newPath) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		try{
			File oldFile = new File(oldPath);
			File file = new File(newPath);
			in = new FileInputStream(oldFile);
			out = new FileOutputStream(file);
			int data = -1 ;
			while ((data = in.read()) != -1) {
				out.write(data);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		} finally {
			in.close();
			out.close();
		}
	}
}
