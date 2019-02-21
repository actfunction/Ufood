
package com.rh.gw.job;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.BaseContext;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.comm.FileStorage;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.util.scheduler.RhLocalJob;


/**
 * 专网数据导出
 *
 * @author yxb
 */
public class ZFileOutJob extends RhLocalJob {

    /**
     * 第一次执行标志
     */
    private static boolean isFirst = true;

    private static Logger log = Logger.getLogger(ZFileOutJob.class);

    /**
     * 构造函数
     */
    public ZFileOutJob() {
    }

    /**
     * KETTLE配置文件
     */
    private static Properties pro = new Properties();

    static {
        try {
            pro.load(new FileInputStream(new File(BaseContext.appStr(APP.WEBINF)+"/kettleCon.properties")));
        } catch (Exception e) {
            log.error("---------------------------------"+BaseContext.appStr(APP.WEBINF)+"/kettleCon.properties");
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * KETTLE脚本执行代码
     *
     * @param jobname 转换作业的路径
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
            System.out.println(e);
            log.error(e.getMessage());
        }
    }

    /**
     * KETTLE脚本执行代码
     *
     * @param filename
     */
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
     * @param context 调度上下文信息
     */
    @Override
    public void executeJob(RhJobContext context) {

        copySevretFile("SY_COMM_FILE");
        copySevretFile("OA_GW_COMM_FILE_HIS");

        try {
            runJob(pro.getProperty("jobOut"));
            // 获取今天日期
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            //专网数据从临时目录导入指定目录
            String newPath = pro.getProperty("fileNewName") + date ;
            File file = new File(newPath);
            if ( file.isDirectory()) {
               deleteDir(file);
            }
            file.mkdirs();
            moveFiles(pro.getProperty("fileOldName"), newPath);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }

    /**
     * 传入服务名查询 其中的加密字段 根据加密字段中的数据查询出文件,复制到新文件夹下
     */
    public void copySevretFile(String servName) {
        List<String> list = new ArrayList<String>();
        List<Bean> listGwBean = ServDao.finds("OA_GW_GONGWEN", " and S_FLAG = 3");
        //FileInputStream in = null;
        //FileOutputStream out = null;
        for (Bean bean : listGwBean) {
            String gw_Id = bean.getStr("GW_ID");
            List<Bean> fileBean = ServDao.finds(servName, "and DATA_ID ='" + gw_Id + "'");
            for (Bean file : fileBean) {
                // 获得路径中的filePath
                String filePath = file.getStr("FILE_PATH");
                list.add(filePath);
                // 生成新的路径
                String newAbsolutePath = filePath.replace("@SYS_FILE_PATH@", Context.getSyConf("OA_TRAN_FILE_ROOT_PATH", System.getProperty("java.io.tmpdir")) + File.separator + DateUtils.getDate());
                String oldAbsolutePath = FileMgr.getAbsolutePath(filePath);
                //copyFile(oldAbsolutePath, newAbsolutePath, in, out);
            	try {
					FileStorage.saveFile(FileStorage.getInputStream(oldAbsolutePath), newAbsolutePath);
				} catch (IOException e) {
					log.error("在"+DateUtils.getDate()+"这个文件"+oldAbsolutePath+"没有复制成功");
					e.printStackTrace();
				}
            }
        }
    }

    @Override
    public void interrupt() {

    }

    //专网转密网数据迁移
    public void moveFiles(String oldPath, String newPath) {
        String[] filePaths = new File(oldPath).list();
        if (filePaths.length > 0) {
            if (!new File(newPath).exists()) {
                new File(newPath).mkdirs();
            }

            for (int i = 0; i < filePaths.length; i++) {
                if (new File(oldPath + File.separator + filePaths[i]).isDirectory()) {
                    moveFiles(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i]);
                } else if (new File(oldPath + File.separator + filePaths[i]).isFile()) {
                    //复制文件到另一个目录
                    try {
                        copyFiles(oldPath + File.separator + filePaths[i], newPath + File.separator + filePaths[i]);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException("file not found", e);
                    }
                }
            }
        }
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
        } finally {
            in.close();
            out.close();
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
        // 目录此时为空，可以删除
        return dir.delete();
    }

}
