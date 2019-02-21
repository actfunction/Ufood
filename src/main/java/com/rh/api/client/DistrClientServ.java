package com.rh.api.client;

import com.rh.api.entity.SendParamEntity;
import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.*;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/***
 *
 * @author Weitl
 * @version 1.0
 *
 */
public class DistrClientServ extends CommonServ {
    /*** 记录历史 */
    private static Log log = LogFactory.getLog(DistrClientServ.class);

    /**
     * MESSAGE:返回信息
     */
    public String MESSAGE = "分发成功！";

    public void setMessage(String message) {
        this.MESSAGE = message;
    }

    public String getMessage() {
        return this.MESSAGE;
    }
    /**
     * 返回的集合
     */
    public List<Bean> rtnList = null;
    /**
     * 文件集合
     */
    public List<Bean> fileList = null;
    /**
     * 批量保存的复制待办集合
     */
    public List<Bean> copyNodeList = null;
    /**
     * 当前登录人
     */
    private UserBean sendUser = null;
    /**
     * 发文的数据bean，减少查询次数
     */
    private Bean fwDataBean = null;
    /**
     * 分发的服务编码
     */
    private static String SEND_SERV = "SY_COMM_SEND_DETAIL";
    /**
     * 标志
     */
    private static final String S_FLAG = "S_FLAG";
    /**
     * 有效部门编码
     */
    private static final String TDEPT_CODE = "TDEPT_CODE";
    /**
     * 用户编码
     */
    private static final String USER_CODE = "USER_CODE";
    /**
     * 角色编码
     */
    private static final String ROLE_CODE = "ROLE_CODE";

    private static final String ODEPT_CODE = "ODEPT_CODE";

    private static final String DEPT_CODE = "DEPT_CODE";
    /**
     * 用户角色表
     */
    private static final String SY_ORG_ROLE_USER = "SY_ORG_ROLE_USER";


    public DistrClientServ(SendParamEntity sendEntity, List<Bean> rtnList, List<Bean> copyNodeList) {
    	this.rtnList = rtnList;
        this.copyNodeList = copyNodeList;
    	this.fileList = sendEntity.getFileList();
        this.sendUser = sendEntity.getSendUser();
        this.fwDataBean  = sendEntity.getFwDataBean();
	}

	public List<Bean> send(Bean detail) {
        ParamBean sendDetail = new ParamBean();
        sendDetail.set("SEND_TIME", DateUtils.getDatetime());
        sendDetail.set("SEND_FORR", 1);
        sendDetail.set("SEND_STATUS", "2");
        sendDetail.set("DATA_ID", detail.getStr("dataId"));
        sendDetail.set("SERV_ID", detail.getStr("servId"));
        sendDetail.set("SEND_NUM", detail.getInt("sendNum"));
        sendDetail.set("SEND_TYPE", detail.isNotEmpty("sendType") ? detail.getInt("sendType") : 1);
        sendDetail.set("RECV_TYPE", "inside");
        
        if (detail.getStr("type").equals("extUnit")) { // 如果是外部单位则走公文转换
            sendDetail.set("RECV_USER", detail.getStr("code"));
            sendDetail.set("RECV_UNAME", detail.getStr("name"));
            sendDetail.set("RECV_DEPT", detail.getStr("code"));
            sendDetail.set("RECV_DNAME", detail.getStr("name"));
            sendDetail.set("RECV_TYPE", "outside");
            OutBean saveBean = ServMgr.act(SEND_SERV, ServMgr.ACT_SAVE, sendDetail);
            this.rtnList.add(saveBean);
        } else if (detail.getStr("type").equals("dept")) { // 如果是内部单位则走公文分发
            // 针对不同层级的机构
            String deptPk = detail.getStr("code");
            // 默认是审计署的收文
            int fwToSwType = 3;
            // 默认是审计署的收文角色
            String recvRole = "R_GW_SWRY";
            // 默认是审计署的收文服务编码
            String swServ = "OA_GW_GONGWEN_ICBCSW";
            Bean dpcode = ServDao.find("SY_ORG_DEPT_FENFA", deptPk); // 当前服务针对分发按钮
            String dpType = dpcode.getStr("DEPT_TYPE");
            String dpGrade = dpcode.getStr("DEPT_GRADE");
            String dpSign = dpcode.getStr("DEPT_SIGN");

            if ("OT10".equals(dpSign) && "10".equals(dpGrade)) {
            	// 如果是审计署或下面的司局
            	if ("2".equals(dpType)) {
                	fwToSwType = 3;
                    recvRole = "R_GW_SWRY";
                    swServ = "OA_GW_GONGWEN_ICBCSW";
            	} else if ("1".equals(dpType)) {
                	fwToSwType = 4;
                    recvRole = "R_GW_SJWS";
                    swServ = "OA_GW_GONGWEN_ICBCSW";
            	}
            } else if ("OT10".equals(dpSign) && "50".equals(dpGrade)) {
            	// 如果是特派办 
            	fwToSwType = 5;
                recvRole = "R_GW_TPB_SWRY";
                swServ = "OA_GW_GONGWEN_TPBSW";
//            } else if ("OT10".equals(dpSign) && "20".equals(dpGrade)) {
//            	// 如果是省分 
//            	fwToSwType = 5;
//                recvRole = "R_GW_TPB_SWRY";
//                swServ = "OA_GW_GONGWEN_TPBSW";
            }

            List<Bean> userList = getUserListByDeptAndRoles(recvRole, deptPk);
            log.error(dpcode.getStr("DEPT_NAME") + "下查询到用户数量为：" + userList.size());
            this.commonSend(userList, sendDetail, detail, fwToSwType, swServ);
        }
        return this.rtnList;
    }


    /**
     * 公共分发
     *
     * @param list 机构/部门下符合角色的所有用户list
     * @param sendDetail 分发日志信息
     * @param fwToSwType 发文转收文类别
     * @param swServ 
     */
    public void commonSend(List<Bean> list, ParamBean sendDetail, Bean detail, int fwToSwType, String swServ) {
        int x = 0; // 这个用来标识 什么时候调用发文转收文方法
        String pkCode = Lang.getUUID(); // 公文主键
        String userCode = null; // 用户编码
        Bean nodeBean = null;
        Bean todoBean = null;

        OutBean out = null;
        for (Bean user : list) {
            UserBean recUser = UserMgr.getUser(user.getId());
            sendDetail.set("RECV_USER", recUser.getId());
            sendDetail.set("RECV_UNAME", recUser.getName());
            sendDetail.set("RECV_DEPT", recUser.getDeptCode());
            sendDetail.set("RECV_DNAME", recUser.getDeptName());
            sendDetail.set("RECV_ODEPT", recUser.getODeptCode());
            sendDetail.set("RECV_TDEPT", recUser.getTDeptCode());
            sendDetail.set("RECV_TNAME", recUser.getTDeptName());
            Context.setOnlineUser(sendUser);
//			OutBean saveBean = ServMgr.act(SEND_SERV, ServMgr.ACT_SAVE, sendDetail);
            this.rtnList.add(sendDetail);
            if (x == 0) {
                Context.cleanThreadData();
                Context.setOnlineUser(recUser);
                // 针对的是相关角色下第一个用户调用发文转收文的方法
                out = this.sendToRece(recUser, detail, pkCode, fwToSwType, swServ);
                userCode = recUser.getId(); // 第一个人的用户编码

                // 发文转成收文之后 这个out代表的是收文的相关数据
                String todo = "AND TODO_OBJECT_ID1 = '" + out.getId() + "' AND OWNER_CODE = '" + userCode + "'"; // 在代办表中第一个人所对应的代办数据(通过用户编码和公文主键)
                todoBean = ServDao.find("SY_COMM_TODO_GW_OA", new Bean().set("_WHERE_", todo));
                if (null == todoBean) {
                    this.setMessage("收文没有获取到符合条件的流程信息。机构名称：" + out.getStr("S_TNAME"));
                    log.error(this.getMessage());
                }
                String sWfInst = todoBean.getStr("TODO_OBJECT_ID2"); // 得到节点的和代办的对应表

                nodeBean = ServDao.find(ServMgr.SY_WFE_NODE_INST, sWfInst);
                x++;
            } else {
                Bean newNode = new Bean();
                // 用一个新的复制bean，防止add到list里时数据重复
                BeanUtils.trans(nodeBean, newNode);
                newNode.setId("");
                // 针对节点表中的 唯一id
                String nodePk = Lang.getUUID();
                newNode.set("NI_ID", nodePk);
                // 节点表中插入新的id
                newNode.set("TO_USER_ID", recUser.getId());
                // 指定新的接收人
                newNode.set("TO_USER_NAME", recUser.getName());
                // 向节点表中加入一条数据;通过list批量添加不再单独添加。
                this.copyNodeList.add(newNode);
//				ServDao.save(ServMgr.SY_WFE_NODE_INST, nodeBean);

                this.sendMultipleTodo(todoBean, recUser, nodePk);
            }
        }
    }


    /**
     * 新增一条待办
     *
     * @param recUser 被分发人，也就是待办所有人
     * @param todoBean 待办bean
     * @param nodePk 节点主键，与待办的TODO_OBJECT_ID2对应
     */
    public void sendMultipleTodo(Bean todoBean, UserBean recUser, String nodePk) {
        String todoPk = Lang.getUUID();

        TodoBean tbBean = new TodoBean();
        BeanUtils.trans(todoBean, tbBean);
        tbBean.setId("");
        tbBean.set("TODO_ID", todoPk);
        tbBean.set("OWNER_CODE", recUser.getId()); // 针对当前代办的拥有人
        tbBean.set("SEND_USER_CODE", recUser.getId()); // 针对发送代办的人员
        tbBean.set("TODO_OBJECT_ID2", nodePk);
        TodoUtils.insert(tbBean); // 插入一条待办；因为待办的特殊性，故不使用批量添加。
    }


    /***
     * 发文转收文
     *
     * @param recUser 收件人信息
     * @param param 参数
     * @param pkCode 收文的主键
     * @param fwToSwType 发文转收文类别
     * @param swServ 
     */
    private OutBean sendToRece(UserBean recUser, Bean param, String pkCode, int fwToSwType, String swServ) {
        OutBean out = new OutBean();

        // 生成收文的数据信息
        ParamBean swBean = new ParamBean(this.fwDataBean);
        BeanUtils.trans(this.fwDataBean, swBean);
        swBean.setId("");
        swBean.set("GW_ID", pkCode);
        swBean.set("GW_COPIES", "");
        swBean.set("GW_MAIN_HANDLE", "");
        swBean.set("GW_COPY_HANDLE", "");
        swBean.set("GROUP_ID", this.fwDataBean.getId());
        swBean.set("TMPL_TYPE_CODE", "OA_GW_GONGWEN_SW");
        swBean.set("TMPL_CODE", swServ);
        swBean.set("GW_GONGWEN_SWSJ", DateUtils.getDate());
        swBean.set("GW_SW_CNAME", sendUser.getODeptFullName());
        swBean.set("S_WF_INST", ""); // 是否修改
        swBean.set("S_WF_USER", "");
        swBean.set("S_WF_STATE", "");
        swBean.set("S_WF_USER_STATE", "");
        swBean.set("S_CMPY", recUser.getCmpyCode());
        swBean.set("S_TDEPT", recUser.getTDeptCode());
        swBean.set("S_DEPT", recUser.getDeptCode());
        swBean.set("S_DNAME", recUser.getDeptName());
        swBean.set("S_USER", recUser.getCode());
        swBean.set("S_FLAG", 1);
        String dateTime = DateUtils.getDatetime();
        swBean.set("S_ATIME", dateTime);
        swBean.set("S_MTIME", dateTime);
        swBean.set("S_TNAME", recUser.getTDeptName());
        swBean.set("S_UNAME", recUser.getName());
        swBean.set("TO_USERS", recUser.getCode());
        // 是否是发文转的收文，在最开始的时候就已经判断好机构了
        swBean.set("IS_FW_TO_SW", fwToSwType);
        //针对的是发文转收文方法
        swBean.set("GW_GONGWEN_SFSZZWJ", "否");
        // 将发文转成收文的相关数据 返回
        out = ServMgr.act(swServ, ServMgr.ACT_SAVE, swBean);

        getFileList(pkCode, swServ);

        return out;
    }


    /***
     * 复制文件
     * @param pkCode 收文的数据主键
     * @param swServ 收文的服务编码
     */
    private void getFileList(String pkCode, String swServ) {

        /*
         * 在数据库中查询出@SYS_FILE_PATH@ 的值
         */
        for (Bean fileBean : this.fileList) {
            String fileId = fileBean.getId();
            fileBean.setId("");
            fileBean.set("SERV_ID", swServ);
            fileBean.set("DATA_ID", pkCode);

            // 获得文件的路径
            String filePath = fileBean.getStr("FILE_PATH");
            // 将路径替换成一个新的路径
            String newFileId = Lang.getUUID();
            String suffix = FileMgr.getSuffix(fileId);
            if (suffix.length() > 0) {
                newFileId += "." + suffix;
            } 
            fileBean.set("FILE_ID", newFileId);
            String newFilePathHis = FileMgr.buildPathExpr(swServ, newFileId);
            // 设置文件的新路径
            fileBean.set("FILE_PATH", newFilePathHis);

            // 复制文件
            FileMgr.copyFile(FileMgr.getAbsolutePath(filePath), FileMgr.getAbsolutePath(newFilePathHis));

            ServDao.save("SY_COMM_FILE", fileBean);
        }
    }


    /**
     * 取得角色用户列表
     *
     * @param deptCode  部门编码
     * @param roleCodes 角色Code串
     * @return 用户Bean列表
     */
    public static List<Bean> getUserListByDeptAndRoles(String roleCodes, String deptCode) {
        if (roleCodes.indexOf(Constant.SEPARATOR) > 0) {
            roleCodes = roleCodes.replaceAll(Constant.SEPARATOR, "'" + Constant.SEPARATOR + "'");
        }

        StringBuilder condition = new StringBuilder(" and " + S_FLAG + " = 1");
        condition.append(" and( " + TDEPT_CODE + " = '").append(deptCode).append("'");
        condition.append(" or " + ODEPT_CODE + " = '").append(deptCode).append("'");
        condition.append(" or " + DEPT_CODE + " = '").append(deptCode).append("'");
        condition.append(" )and " + USER_CODE + " in (select distinct " + USER_CODE + " from ");
        condition.append(SY_ORG_ROLE_USER + " where " + ROLE_CODE + " in ('" + roleCodes + "')");
        condition.append(" and " + S_FLAG + "=1)");
        return ServDao.finds(ServMgr.SY_ORG_USER, condition.toString());
    }
}
