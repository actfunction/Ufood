package com.rh.gw.wfe;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Lang;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.util.AbstractLineEvent;
/**
 * 正文历史版本事件监听类.
 * @author lizhiyu
 */
public class FileHisLineEvent extends AbstractLineEvent {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(FileHisLineEvent.class);
    /***文件表 */
    private static final String SERV_FILE = "SY_COMM_FILE";
    /**文件版本表.*/
    private static final String OA_FILE_HIS = "OA_GW_COMM_FILE_HIS";
    /**
     * 顺序送下一个节点时触发此事件，判断下一节点绑定的人员类型，并在与会人员列表中存入一条送交信息.
     * @param preWfAct 前一个节点的实例.
     * @param nextWfAct 下一个节点的实例.
     * @param lineDef 线定义Bean.
     */
    /* (non-Javadoc)
     * @see com.rh.core.wfe.util.AbstractLineEvent#forward(com.rh.core.wfe.WfAct, com.rh.core.wfe.WfAct, com.rh.core.base.Bean)
     */
    public void forward(WfAct preWfAct, WfAct nextWfAct, Bean lineDef) {
        // 表单数据bean
        Bean dataBean = nextWfAct.getProcess().getServInstBean();
        // 获取表单主键
        String dataId = dataBean.getId();
        if ("N27".equalsIgnoreCase(preWfAct.getNodeInstBean().getStr("NODE_CODE"))
        		|| "N29".equalsIgnoreCase(preWfAct.getNodeInstBean().getStr("NODE_CODE"))
        		) {
        // 流程实例bean
        Bean wfeBean = nextWfAct.getProcess().getProcInstBean();
        // 服务ID
        String servId = wfeBean.getStr("SERV_ID");
        //向历史表中查询是否有这个数据  如果有则不进行插入了
        StringBuffer hisFileSb = new StringBuffer();
        hisFileSb.append("AND DATA_ID = '").append(dataId)
        .append("' AND SERV_ID = '").append(servId).append("'")
        .append(" AND FILE_CAT = 'SMINDY'");
    	List<Bean> fileCat = ServDao.finds(OA_FILE_HIS, hisFileSb.toString());
    	if(null ==fileCat || fileCat.size()==0){
        //审理司会审意见 上传
        StringBuffer fileSb = new StringBuffer();
        fileSb.append("AND DATA_ID = '").append(dataId)
        .append("' AND SERV_ID = '").append(servId).append("'")
        .append(" AND FILE_CAT = 'SMINDY'");
        List<Bean> fileList = ServDao.finds(SERV_FILE, fileSb.toString());
        addHisFile(fileList);
    	}
		//意见
    	 StringBuffer hisFileSbs = new StringBuffer();
    	 hisFileSbs.append("AND DATA_ID = '").append(dataId)
         .append("' AND SERV_ID = '").append(servId).append("'")
         .append(" AND FILE_CAT = 'SMINDY'");
     	List<Bean> fileCats = ServDao.finds(OA_FILE_HIS, hisFileSbs.toString());
     	if(null ==fileCats  || fileCat.size()==0){
    	StringBuffer fileSbs = new StringBuffer();
    	fileSbs.append("AND DATA_ID = '").append(dataId)
        .append("' AND SERV_ID = '").append(servId).append("'")
        .append(" AND FILE_CAT = 'SMINDS'");
		List<Bean> fileList = ServDao.finds(SERV_FILE, fileSbs.toString());
			addHisFile(fileList);
     	}
        }
    }
    public void addHisFile(List<Bean> fileList){
		for (Bean hisFile : fileList) {
			// 如果没有相同的值   则创建这个清稿文件
			hisFile.setId("");
			//获得版本记录   将版本记录+1 返回 版本号
		    int histVers =0;
			 histVers = ServDao.count("OA_GW_COMM_FILE_HIS", new Bean().set("SERV_ID", hisFile.getStr("SERV_ID")).set("DATA_ID", hisFile.getStr("DATA_ID"))) + 1;//
			 hisFile.set("HISFILE_VERSION", histVers);
			//历史文件的id
			String hisFileId="OAHIST_" + Lang.getUUID()+"."+FileMgr.getSuffix(hisFile.getStr("FILE_ID"));
			hisFile.set("HISFILE_ID", hisFileId);
			//在这里获得数据库中存储文件的路径
			String path=hisFile.getStr("FILE_PATH");
			//将文件路径转换成为绝对路径
			String zhengWenFilePath = FileMgr.getAbsolutePath(path);
			//将这个文件复制到一个指定的新路径下
			String newHisFilePath="";
			//将文件名进行替换  成hisFileId
			String[] filePaths = zhengWenFilePath.split("/");
			for(int i=0;i<filePaths.length;i++){
				if(i==filePaths.length-1){
					newHisFilePath+=hisFileId;
				}else{
					newHisFilePath+=filePaths[i]+"/";
				}
			}
			FileMgr.copyFile(zhengWenFilePath, newHisFilePath);
			//将新路径转换成数据库中存储的路径
			 newHisFilePath="";
			String[] sqlPath = path.split("/");
			for(int i=0;i<sqlPath.length;i++){
				if(i==sqlPath.length-1){
					newHisFilePath+=hisFileId;
				}else{
					newHisFilePath+=sqlPath[i]+"/";
				}
			}
		    //将路径上传到数据库
			hisFile.set("FILE_PATH", newHisFilePath);
			//在这里进行向数据库经插入清稿文档
			hisFile.set("HISTFILE_QINGGAO_TYPE", "HUIQIAN");
			  //获得当前节点 讲节点放入到数据中
			hisFile.set("S_MTIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
			ServDao.save("OA_GW_COMM_FILE_HIS", hisFile);
			log.debug("新增一个正文的历史版本，服务编码为：" + hisFile.getStr("SERV_ID") + ",数据主键为：" + hisFile.getStr("DATA_ID"));
		}
    }
    /**
     * 返回操作送下一个节点时触发此事件.
     * @param preWfAct 前一个节点的实例.
     * @param nextWfAct 下一个节点的实例.
     * @param lineDef 线定义Bean.
     */
    public void backward(WfAct preWfAct, WfAct nextWfAct, Bean lineDef) {
    }
}
