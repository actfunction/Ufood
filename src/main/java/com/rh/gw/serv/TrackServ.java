package com.rh.gw.serv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfParam;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfNodeInstHisDao;
import com.rh.core.wfe.db.WfNodeUserDao;
import com.rh.core.wfe.db.WfNodeUserHisDao;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 处理  流程跟踪服务
 *
 */
public class TrackServ extends CommonServ {
	/**
	 * 查询前添加查询条件
	 * 
	 * @param paramBean 
	 */
    public void beforeQuery(ParamBean paramBean) {
    	String pid = paramBean.getStr("PI_ID");
    	String servId = paramBean.getStr("SERV_ID");
    	String dataId = paramBean.getStr("DATA_ID");
    	
    	if (null != servId && null != dataId) {
        	Bean dataBean = ServDao.find(servId, dataId);
    		paramBean.set("INST_IF_RUNNING", dataBean.getStr("S_WF_STATE"));
    	}
    	
        String procRunning = paramBean.getStr("INST_IF_RUNNING");
        
        if (procInstIsRunning(paramBean)) { //流程未办结
			paramBean.set("serv", WfNodeInstDao.SY_WFE_NODE_INST_SERV);
		} else { //流程已经办结
			paramBean.set("serv", WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV);
		}
		
		StringBuilder strWhere = new StringBuilder(" and PI_ID = '"); 
		strWhere.append(pid);
    	strWhere.append("'");
    	
    	paramBean.set("PI_ID", pid);
    	paramBean.set("procRunning", procRunning);
    	
		
    	paramBean.set("act", ServMgr.ACT_QUERY);
    	paramBean.set("_extWhere", strWhere);
    }
    
    @Override
    public void afterQuery(ParamBean paramBean , OutBean dataBean) {
        //是否是移动平台
        boolean isMobile = paramBean.getBoolean("_IS_MOBILE");
        
        //修改流程跟踪中送交人、送交部门的值，使列表流程的可读性更强。
        List<Bean> dataList = dataBean.getDataList();
        
        if (dataList.size() == 0) {
            return;
        }
        
        addWfManagerParam(paramBean, dataBean);
        
        List<Bean> newList = new ArrayList<Bean>();
        HashMap<String, Bean> map = new HashMap<String, Bean>();
        for (Bean nodeInstBean : dataList) {
            map.put(nodeInstBean.getId(), nodeInstBean);
        }
        
        List<Bean> wfNodeUsers = getNodeUsers(paramBean);
        
        Map<String, List<Bean>> nodeUsersMap = getNodeUserListMap(wfNodeUsers);
        
        for (Bean niBean : dataList) {
            String nid = niBean.getId();
            
            List<Bean> resList= ServDao.finds("SY_WFE_TRACK_V", "AND NI_ID = '" + nid + "'");
            
            if(resList.size() > 0){
            	niBean.set("MIND_CONTENT", resList.get(0).getStr("MIND_CONTENT"));
            }else{
            	niBean.set("MIND_CONTENT", "");
            }
            
            Bean preNode = map.get(niBean.getStr("PRE_NI_ID"));
            
            if (niBean.getStr("PRE_LINE_CODE").startsWith("R")) {
                niBean.set("PRE_LINE_CODE", niBean.getStr("PRE_LINE_CODE").replace("R", ""));
            }
            
            List<Bean> nodeUsers = nodeUsersMap.get(nid);
            if (niBean.isNotEmpty("TO_USER_ID")) { //如果接收人不为NULL，则将接收人的部门作为办理部门。
                String deptName = nodeUsers.get(0).getStr("TO_DEPT_NAME");
                niBean.set("DONE_DEPT_NAMES", deptName);
            }
            
            if (niBean.isEmpty("TO_USER_ID")) { //还没设置上，这个是肯定多人的
                StringBuilder strUser = new StringBuilder();
                
                for (Bean nodeUser: nodeUsers) {
                    String toUserName = createUserLink(nodeUser, "TO_USER_NAME", "TO_USER_ID", isMobile);
                    strUser.append(toUserName).append(",");
                }
                
                if (strUser.length() > 0) {
                    strUser.setLength(strUser.length() - 1);
                }
                niBean.set("DONE_USER_NAME", strUser);
            } else if (niBean.isEmpty("DONE_USER_NAME")) {
                //如果实际办理人为空则把当前接收人的值作为实际办理人。解决正在办理节点没有实际办理人的问题。
                niBean.set("DONE_USER_NAME", createUserLink(niBean, "TO_USER_NAME", "TO_USER_ID", isMobile));
            } else {
                String toUserName = createUserLink(niBean, "TO_USER_NAME", "TO_USER_ID", isMobile);
                String doneUser = createUserLink(niBean, "DONE_USER_NAME", "DONE_USER_ID", isMobile);
                
                if (!toUserName.equals(doneUser)) { //如果接收人和办理人不是同一个人则显示2人的姓名。
                    doneUser = toUserName + " / " + doneUser;
                }
                
                if (niBean.isNotEmpty("SUB_USER_NAME")) {
                    doneUser += "[" + niBean.getStr("SUB_USER_NAME") + " 代办]";
                }
                
                final int doneType = niBean.getInt("DONE_TYPE");
                if (doneType == WfeConstant.NODE_DONE_TYPE_WITHDRAW) {
                    // 收回
                    doneUser += "(被" + WfeConstant.NODE_DONE_TYPE_WITHDRAW_DESC + ")";
                } else if (doneType == WfeConstant.NODE_DONE_TYPE_STOP) {
                    // 中止
                    doneUser += "(" + WfeConstant.NODE_DONE_TYPE_STOP_DESC + ")";
                } else if (doneType == WfeConstant.NODE_DONE_TYPE_FINISH) {
                    // 办结
                    doneUser += "(" + WfeConstant.NODE_DONE_TYPE_FINISH_DESC + ")";
                } else if (doneType == WfeConstant.NODE_DONE_TYPE_CONVERGE) {
                    // 合并
                    doneUser += "(" + WfeConstant.NODE_DONE_TYPE_CONVERGE_DESC + ")";
                }
                

                
                niBean.set("DONE_USER_NAME", doneUser);
            }
            
            if (preNode != null) {
                // 如果前一个节点不为空，则把上一个节点办理人作为本节点的送交人。
                niBean.set("TO_DEPT_NAME", preNode.getStr("DONE_DEPT_NAMES"));
                if (preNode.isEmpty("DONE_USER_NAME")) { // 如果前一个节点未结束
                    niBean.set("TO_USER_NAME", createUserLink(preNode, "TO_USER_NAME", "TO_USER_ID", isMobile));
                } else {
                    niBean.set("TO_USER_NAME", createUserLink(preNode, "DONE_USER_NAME", "DONE_USER_ID", isMobile));
                }
            } else {
                // 如果前一个节点不空，则把上一个节点办理人设置为NULL
                niBean.set("TO_DEPT_NAME", "");
                niBean.set("TO_USER_NAME", "");
            }
            
            newList.add(niBean);
        }
        
        
        dataBean.set(Constant.RTN_DATA, newList);
    }

    /**
     * 
     * @param wfNodeUsers 节点用户
     * @return map<节点实例id, 办理人list>
     */
    private Map<String, List<Bean>> getNodeUserListMap(List<Bean> wfNodeUsers) {
        Map<String, List<Bean>> nodeUsers = new HashMap<String, List<Bean>>();
        
        for (Bean nodeUser: wfNodeUsers) {
            
            String nid = nodeUser.getStr("NI_ID");
            if (nodeUsers.containsKey(nid)) {
                List<Bean> users = nodeUsers.get(nid);
                
                users.add(nodeUser);
            } else {
                List<Bean> users = new ArrayList<Bean>();
                users.add(nodeUser);
                nodeUsers.put(nid, users);
            }
        }
        
        return nodeUsers;
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 流程上的节点用户
     */
    public List<Bean> getNodeUsers(ParamBean paramBean) {
        String userServId = WfNodeUserDao.SY_WFE_NODE_USERS;
        String pid = paramBean.getStr("PI_ID");
        
        if (!procInstIsRunning(paramBean)) { //流程已经办结
            userServId = WfNodeUserHisDao.SY_WFE_NODE_USERS_HIS;
        }
        
        SqlBean sqlUser = new SqlBean();
        sqlUser.and("PI_ID", pid);
        sqlUser.asc("NI_ID");
        
        return ServDao.finds(userServId, sqlUser);
    }
    
    /**
     * 
     * @param niBean 流程实例记录
     * @param nameItem 用户姓名字段的字段名
     * @param userItem 用户ID字段的字段名
     * @param isMobile 是否手机平台
     * @return 返回对应用户的HTML内容。前台根据HTML标签的内容加上显示鼠标放在本用户名称上显示用户信息的效果。
     */
    private String createUserLink(Bean niBean, String nameItem, String userItem, boolean isMobile) {
        if (isMobile) {
            return niBean.getStr(nameItem);
        }
        
        StringBuilder str = new StringBuilder();

        str.append("<a href='#' class='usernameLink' USER_CODE='");
        str.append(niBean.getStr(userItem)).append("'>");
        str.append(niBean.getStr(nameItem));
        str.append("</a>");

        return str.toString();
    }
    
    /**
     * @param paramBean   流程ID
     * @param dataBean  是否活动流程
     */
    private void addWfManagerParam(ParamBean paramBean , Bean dataBean) {
        String pid = paramBean.getStr("PI_ID");
//        String procRunning = paramBean.getStr("INST_IF_RUNNING");
        
        WfProcess wfProc = new WfProcess(pid, procInstIsRunning(paramBean));
        if (wfProc.isProcManage()) {
            dataBean.set("_IS_WF_MANAGER", "1");
        }
        dataBean.set("DOC_ID", wfProc.getDocId());
        dataBean.set("SERV_ID", wfProc.getServId());
        
        
        //设置标识， 标识当前这个流程跟踪对应的流程定义是否存在
        dataBean.set("_EXIST_PROC_DEF_", Constant.YES_INT);
        if (null == wfProc.getProcDef() || wfProc.getProcDef().isEmpty()) {
            dataBean.set("_EXIST_PROC_DEF_", Constant.NO_INT);
        }
    }
    
    /**
     * @param paramBean 参数Bean
     * @return 指定流程的所有节点定义实例列表
     */
    public OutBean reteieveNodeDefList(ParamBean paramBean) {
        String nid = paramBean.getStr("NI_ID");
        WfAct wfAct = new WfAct(nid, true);
        WfProcDef procDef = wfAct.getProcess().getProcDef();
        List<Bean> nodeDefList = procDef.getAllNodeDef();
        
        //按照节点名称排序
        Collections.sort(nodeDefList, new Comparator<Bean>() {
            public int compare(Bean map1 , Bean map2) {
                // 取出操作时间
                int ret = 0;
                try {
                    String map1Node = map1.getStr("NODE_NAME");
                    String map2Node = map2.getStr("NODE_NAME");
                    ret = map1Node.compareTo(map2Node);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                return ret;
            }
            
        }); 
        
        return new OutBean().setData(nodeDefList);
    }
    
    /**
     * 将指定流程节点，送交给随意的节点和用户。流程管理员有此权限。
     * @param paramBean 参数Bean
     * @return 操作结果
     */
    public OutBean wfToNext(ParamBean paramBean) {
        String niId = paramBean.getStr("NI_ID");
        String nodeCode = paramBean.getStr("NODE_CODE");
        String doneUserId = paramBean.getStr("DONE_USER_ID");
        
        WfAct wfAct = new WfAct(niId, true);
        
        if (!wfAct.isRunning()) {
            return new OutBean().setError("流程实例已经结束，不能再次送交。");
        }
        
        WfNodeDef nodeDef = wfAct.getProcess().getProcDef().findNode(nodeCode);
        
        UserBean currUserBean = Context.getUserBean();
        WfParam wfParam = new WfParam();
        
        wfParam.setTypeTo(WfParam.TYPE_TO_USER);
        wfParam.setDoneUser(currUserBean);
        wfParam.setToUser(doneUserId);
        wfParam.setDoneType(WfeConstant.NODE_DONE_TYPE_STOP);
        wfParam.setDoneDesc(WfeConstant.NODE_DONE_TYPE_STOP_DESC);
        
        wfAct.toNextAndEndMe(nodeDef.getStr("NODE_CODE"), wfParam);

        return new OutBean().setOk();
        
    }
    
    /**
     * @param paramBean 中止流程指定节点
     * @return outBean
     */
    public OutBean stopNodeInst(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String niId = paramBean.getStr("NI_ID");
        WfAct wfAct = new WfAct(niId, true);
        int isRunning = wfAct.getNodeInstBean().getInt("NODE_IF_RUNNING");
        if (isRunning == Constant.NO_INT) {
            outBean.setWarn("节点已结束，不能中止。");
            return outBean;
        }
        int count = wfAct.getProcess().getRunningNodeInstCount();
        if (count == 1) {
            outBean.setWarn("只有一个活动实例，不允许结束。");
            return outBean;
        }
        
        wfAct.stop(getDoUserBean(paramBean));
        
        return outBean.setOk();
    }
    
	/**
	 * @param paramBean 从页面传来的  INST_IF_RUNNING 字符串类型的 流程是否运行
	 * @return 流程是否运行
	 */
    private boolean procInstIsRunning(ParamBean paramBean) {

        String procRunning = paramBean.getStr("INST_IF_RUNNING");
        int sFlag = paramBean.getInt("S_FLAG");
        boolean procIsRunning = true;
        if (sFlag > 0 && sFlag == Constant.NO_INT) {
            procIsRunning = false;
        } else if (procRunning.equals(String.valueOf(WfeConstant.WFE_PROC_INST_NOT_RUNNING))) {
            procIsRunning = false;
        }

        return procIsRunning;
    }
	
}
