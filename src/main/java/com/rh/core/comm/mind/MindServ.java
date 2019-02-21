/*
 * Copyright (c) 2012 Ruaho All rights reserved.
 */
package com.rh.core.comm.mind;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.FileMgr;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.wfe.WfAct;

/**
 * mind service extends <CODE>CommonServ</CODE>
 * @author wanglong
 * 
 */
public class MindServ extends CommonServ {

    /** mind service id **/
    private static final String MIND_SERV_ID = "SY_COMM_MIND";

    /** 意见显示规则 部门 1 */
    public static final int MIND_RULE_DEPT = 1;

    /** 意见显示规则 机构内 2 */
    public static final int MIND_RULE_ORG_INNER = 2;

    /** 意见显示规则 机构外 3 */
    public static final int MIND_RULE_ORG_OUTTER = 3;

    /** 意见排序规则 按时间 */
    public static final String MIND_SORT_TIME = "TIME";

    /** 意见排序规则 按意见类型 */
    public static final String MIND_SORT_TYPE = "TYPE";

    /** 意见 类型 固定意见 */
    public static final String MIND_TYPE_REGULAR = "REGULAR";

    /** 意见 类型 最终意见 */
    public static final String MIND_TYPE_TERMINAL = "TERMINAL";

    /** 意见 类型 一般意见 */
    public static final String MIND_TYPE_GENERAL = "GENERAL";

    /**
     * 获取意见 并将结果按mind type进行分组成<CODE>map</CODE>
     * 
     * @param params 参数bean
     * @return 结果bean 返回意见按照mind type进行分组，bean.get("_DATA_") 可获取意见结果
     */
    public OutBean getMindGroupByType(Bean params) {
        String mindSort = Context.getSyConf("SY_COMM_MIND_SORT_TYPE", MIND_SORT_TYPE);

        if (params.isNotEmpty("SORT_TYPE")) { // 指定按照 那种方式 排序
            mindSort = params.getStr("SORT_TYPE");
        }

        UserBean userBean = Context.getUserBean();

        OutBean out = new OutBean();

        if (mindSort.equalsIgnoreCase(MIND_SORT_TYPE)) { // 先按意见类型排序，再按时间排序
            params.set(Constant.PARAM_ORDER, " MIND_CODE, MIND_TIME DESC");
        } else {
            params.set(Constant.PARAM_ORDER, " MIND_TIME DESC");
        }

        StringBuilder mindWhere = new StringBuilder();
        mindWhere.append(" and ((MIND_DIS_RULE =");
        mindWhere.append(MIND_RULE_DEPT);
        mindWhere.append(" and S_TDEPT = '");
        mindWhere.append(userBean.getTDeptCode());
        mindWhere.append("') or MIND_DIS_RULE = ");
        mindWhere.append(MIND_RULE_ORG_OUTTER);
        mindWhere.append(" or (MIND_DIS_RULE =");
        mindWhere.append(MIND_RULE_ORG_INNER);
        mindWhere.append(" and S_ODEPT = '");
        mindWhere.append(userBean.getODeptCode());
        mindWhere.append("')) and SERV_ID = '");
        mindWhere.append(params.getStr("SERV_ID"));
        mindWhere.append("' and DATA_ID = '");
        mindWhere.append(params.getStr("DATA_ID"));
        mindWhere.append("'");

        params.set(Constant.PARAM_WHERE, mindWhere.toString());

        List<Bean> mindList = ServDao.finds(MIND_SERV_ID, params);

        // 意见上关联的附件
        // for (Bean mindBean: mindList) {
        // Bean queryBean = new Bean();
        // queryBean.set("SERV_ID", MIND_SERV_ID);
        // queryBean.set("DATA_ID", mindBean.getId());
        //
        // List<Bean> fileList = ServDao.finds(ServMgr.SY_COMM_FILE, queryBean);
        //
        // mindBean.set("fileList", fileList);
        // }

        List<Bean> codeMindList = new ArrayList<Bean>();

        if (mindSort.equalsIgnoreCase(MIND_SORT_TYPE)) { // 先按意见类型排序，再按时间排序
            if (mindList.size() > 0) {
                Bean mindCodeQuery = new Bean();
                mindCodeQuery.set(Constant.PARAM_SELECT, "distinct MIND_CODE");
                mindCodeQuery.set("SERV_ID", params.getStr("SERV_ID"));
                mindCodeQuery.set("DATA_ID", params.getStr("DATA_ID"));

                List<Bean> codeList = ServDao.finds("SY_COMM_MIND", mindCodeQuery);

                StringBuilder codeStrs = new StringBuilder();
                for (Bean codeBean : codeList) {
                    codeStrs.append("'");
                    codeStrs.append(codeBean.getStr("MIND_CODE"));
                    codeStrs.append("',");
                }
                if (codeStrs.length() > 1) {
                    codeStrs.append("''");
                }

                Bean sortQuery = new Bean();
                String mindCodeWhere = " and CODE_ID in (" + codeStrs.toString() + ")";
                sortQuery.set(Constant.PARAM_WHERE, mindCodeWhere);
                sortQuery.set(Constant.PARAM_ORDER, " CODE_SORT asc");
                codeMindList = ServDao.finds("SY_COMM_MIND_CODE", sortQuery);

                for (Bean codeMindBean : codeMindList) {
                    for (Bean mindBean : mindList) {
                        if (codeMindBean.getStr("CODE_ID").equalsIgnoreCase(mindBean.getStr("MIND_CODE"))) {
                            addMindBean(codeMindBean, mindBean);
                        }
                    }
                }
            }
        } else if (mindSort.equalsIgnoreCase(MIND_SORT_TIME)) { // 只是按照时间排序
            if (mindList.size() > 0) {
                Bean codeMindBean = new Bean();
                codeMindBean.set("mind", mindList);
                codeMindBean.set("CODE_NAME", "意见类型"); // 初始化值，页面有用到
                codeMindBean.set("CODE_ID", "mindType");

                codeMindList.add(codeMindBean);
            }
        }

        return out.setData(codeMindList);
    }

    /**
     * 
     * @param codeMindBean 意见编码Bean
     * @param bean 意见Bean
     */
    @SuppressWarnings("unchecked")
    private void addMindBean(Bean codeMindBean, Bean bean) {
        List<Bean> mindBeanList = null;
        if (codeMindBean.isEmpty("mind")) {
            mindBeanList = new ArrayList<Bean>();
        } else {
            mindBeanList = (List<Bean>) codeMindBean.get("mind");
        }

        mindBeanList.add(bean);
        codeMindBean.set("mind", mindBeanList);
    }

    /**
     * 获取意见编码
     * @param codeId 意见编码主键
     * @return <CODE>Bean</CODE>
     */
    public OutBean getMindCode(String codeId) {
        return new OutBean(ServDao.find("SY_COMM_MIND_CODE", codeId));
    }

    /**
     * @param codeIds 意见编码串
     * @return 意见编码Bean map
     */
    public HashMap<String, Bean> getMindCodeBeanMap(String codeIds) {
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and CODE_ID in ('");
        strWhere.append(codeIds.replaceAll(",", "','"));
        strWhere.append("')");

        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, strWhere);

        List<Bean> mindCodeList = ServDao.finds("SY_COMM_MIND_CODE", queryBean);

        HashMap<String, Bean> mindCodeMap = new HashMap<String, Bean>();
        for (Bean mindCode : mindCodeList) {
            if (!mindCodeMap.containsKey(mindCode.getStr("CODE_ID"))) {
                mindCodeMap.put(mindCode.getStr("CODE_ID"), mindCode);
            }
        }

        return mindCodeMap;
    }

    /**
     * 添加涂鸦板意见
     * @param param 参数Bean
     * @return out Bean
     */
    public OutBean addTuyaMind(Bean param) {
        String imgDataP = param.getStr("img_data");
        imgDataP = imgDataP.substring("data:image/png;base64,".length());
        byte[] data = null;
        try {
            data = Lang.decodeBase64(imgDataP.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        OutBean imageBean = new OutBean(FileMgr.upload("SY_COMM_MIND", "", "", "", "mind.png",
                new ByteArrayInputStream(data),
                "mind.png", "image/png"));
        return imageBean;
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 表单上填写的固定意见的列表
     */
    public OutBean getRegularMind(ParamBean paramBean) {
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and USUAL_ID is not null "); // 固定意见
//        strWhere.append(" and SERV_ID ='");
//        strWhere.append(paramBean.getStr("SERV_ID"));
        strWhere.append(" and DATA_ID = '");
        strWhere.append(paramBean.getStr("DATA_ID"));
        strWhere.append("'");

        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        queryBean.set(Constant.PARAM_ORDER, " MIND_TIME desc"); // 按时间倒排

        List<Bean> mindList = ServDao.finds(ServMgr.SY_COMM_MIND, queryBean);

        return new OutBean().set("rtnList", mindList);
    }

    /**
     * @param paramBean 参数Bean
     * @return 输出意见列表
     */
    public OutBean displayMindList(ParamBean paramBean) {
        OutBean out = new OutBean();
        FormMindView mindView = createMindView(paramBean);
        //是否移动设备
        mindView.setMobile(paramBean.getBoolean("_isMobile"));

        boolean canCopy = paramBean.getBoolean("CAN_COPY");
        boolean userDoInWf = paramBean.getBoolean("userDoInWf");
        

        String odeptCode = paramBean.isEmpty("ODEPT_CODE") ? Context.getUserBean().getODeptCode() : paramBean
                .getStr("ODEPT_CODE"); // 指定机构
        out.set("MIND_LIST", mindView.output(odeptCode, canCopy, userDoInWf, paramBean));
        // 意见类型的大小
        out.set("MIND_TYPE_LIST_SIZE", mindView.getMindTypeListSize());
        // 意见所属机构的大小
        out.set("MIND_ODEPT_SIZE", mindView.getMindOdeptListSize());

        out.set("odeptCode", odeptCode);
        out.set("sortType", paramBean.getStr("SORT_TYPE"));
        return out.setOk();
    }

    /**
     * 根据条件取得意见列表
     * @param paramBean 参数
     * @return FormMindView 对象实例
     */
    private FormMindView createMindView(ParamBean paramBean) {
        UserBean userBean = getDoUserBean(paramBean);
        String servID = paramBean.getStr("SERV_ID");
        String dateID = paramBean.getStr("DATA_ID");

        String sortType = "TYPE";
        if (paramBean.isNotEmpty("SORT_TYPE")) {
            sortType = paramBean.getStr("SORT_TYPE");
        }

        FormMindView mindView = new FormMindView(userBean, servID, dateID, sortType);
        return mindView;
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 显示的意见列表 的 单位名称
     */
    public OutBean displayMindTitle(ParamBean paramBean) {
        OutBean out = new OutBean();
        FormMindView mindView = createMindView(paramBean);
        mindView.setMobile(paramBean.getBoolean("_isMobile"));

        UserBean userBean = getDoUserBean(paramBean);
        
        out.set("MIND_TITLE", mindView.mindListTitle(userBean.getODeptCode(), paramBean));
        // 意见类型的大小
        out.set("MIND_TYPE_LIST_SIZE", mindView.getMindTypeListSize());
        // 意见所属机构的大小
        out.set("MIND_ODEPT_SIZE", mindView.getMindOdeptListSize());

        // 取得本部门未提交的意见
        List<Bean> codeMindList = MindUtils.getDisabledMindInDept(userBean.getTDeptCode()
                , paramBean.getStr("DATA_ID"));
        
        //如果是系统配置上设置了一次展开全部
        boolean expansionAll = paramBean.getStr("EXPANDSION_ALL").equalsIgnoreCase("TRUE");
        
        if (expansionAll) {
            boolean canCopy = paramBean.getBoolean("CAN_COPY");
            boolean userDoInWf = paramBean.getBoolean("userDoInWf");
            
            String sortType = paramBean.getStr("SORT_TYPE");
            int typeSize = mindView.getMindTypeListSize();
            int odeptSize = mindView.getMindOdeptListSize();

            List<Bean> odeptList = mindView.getOdeptList();
            
            List<Bean> allData = new ArrayList<Bean>();
            for (Bean odeptBean: odeptList) {
                String odeptCode = odeptBean.getId();
                if (userBean.getODeptCode().equalsIgnoreCase(odeptCode)) {
                    continue;
                }
                
                //获取所有的机构，除了自己
                Bean odeptMind = new Bean();
                odeptMind.set("MIND_LIST", mindView.output(odeptCode, canCopy, userDoInWf, paramBean));
                // 意见类型的大小
                odeptMind.set("MIND_TYPE_LIST_SIZE", typeSize);
                // 意见所属机构的大小
                odeptMind.set("MIND_ODEPT_SIZE", odeptSize);

                odeptMind.set("odeptCode", odeptCode);
                odeptMind.set("sortType", sortType);
                
                allData.add(odeptMind);
            }
            out.set("allData", allData);
        }

        return out.setData(codeMindList).setOk();
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 显示的意见列表 的 单位名称
     */
    public OutBean showMindInput(ParamBean paramBean) {
        OutBean out = new OutBean();

        InputMindView inputView = new InputMindView(paramBean);
        // 是否移动设备
        inputView.setMobile(paramBean.getBoolean("_isMobile"));
        
        out.set("inputMindStr", inputView.output());

        return out.setOk();
    }

    /**
     * 绑定意见ID和文件的关系，在文件表保存MIND_ID到DATA_ID字段
     * @param paramBean 参数Bean
     * @param outBean 意见Bean
     */
    private void bindMind2File(ParamBean paramBean, OutBean outBean) {
        //参数中包含了MIND_FILE参数
        if (paramBean.isEmpty("MIND_FILE") || paramBean.getStr("MIND_FILE").equals(",")) {
            return;
        }
        UserMind.appendFileID(outBean);
        //MIND_FILE_ID
        List<Bean> fileList = outBean.getList("_MIND_FILE_LIST");
        if (fileList != null) {
            List<String> hasRemovedFileList = new ArrayList<String>(); //是否有些文件不存在，用于兼容异常情况
            for (Bean bean : fileList) {
                Bean file = FileMgr.getFile(bean.getStr("FILE_ID"));
                if (file == null) {
                    hasRemovedFileList.add(bean.getStr("FILE_ID"));
                    continue;
                }
                if (file.isEmpty("DATA_ID")) {
                    file.set("DATA_ID", outBean.getId());
                    FileMgr.updateFile(file);
                }
            }
            //MIND_FILE字段的值与数据库中的文件数量不同步，则同步数据库中的文件，以数据库中的为准。
            if (hasRemovedFileList.size() > 0) { 
                StringBuilder str = new StringBuilder("");
                for (Bean bean : fileList) {
                    String fileId = bean.getStr("FILE_ID");
                    if (hasRemovedFileList.contains(fileId)) {
                        continue;
                    }
                    str.append(";");
                    str.append(bean.getStr("FILE_ID")).append(",");
                    str.append(bean.getStr("FILE_NAME"));
                }
                
                if (str.length() > 1) {
                    ParamBean mindBean = new ParamBean();
                    mindBean.setId(outBean.getId());
                    mindBean.set("MIND_FILE", str.substring(1));
                    ServDao.save(ServMgr.SY_COMM_MIND, mindBean);
                }
            }
        }
    }

    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        // 如果存在节点实例ID，则指定本节点定义的条件表达式。
        if (outBean.isNotEmpty("WF_NI_ID")) {
            WfAct wfAct = new WfAct(outBean.getStr("WF_NI_ID"), true);
            UserBean userBean = UserMgr.getUser(outBean.getStr("S_USER"));
            wfAct.updateServWhenMindSave(userBean);
        }
        bindMind2File(paramBean, outBean);
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 保存补登意见
     */
    public OutBean saveBuDengMind(ParamBean paramBean) {
        // 处理选择的人为S_USER ， 填写人为补登人
        if (paramBean.isNotEmpty("TARGET_USER")) { // 如果选择了补登人
            UserBean curUser = Context.getUserBean();

            String leaderCode = paramBean.getStr("TARGET_USER");
            UserBean leaderBean = UserMgr.getUser(leaderCode);

            paramBean.set("S_USER", leaderCode);
            paramBean.set("S_UNAME", leaderBean.getName());
            paramBean.set("S_DEPT", leaderBean.getDeptCode());
            paramBean.set("S_DNAME", leaderBean.getDeptName());
            paramBean.set("S_TDEPT", leaderBean.getTDeptCode());
            paramBean.set("S_TNAME", leaderBean.getTDeptName());
            paramBean.set("S_CMPY", leaderBean.getCmpyCode());
            paramBean.set("S_ODEPT", leaderBean.getODeptCode());

            paramBean.set("BD_USER", curUser.getCode());
            paramBean.set("BD_UNAME", curUser.getName());
        }

        if (paramBean.isNotEmpty("MIND_ID")) {
            paramBean.setId(paramBean.getStr("MIND_ID"));
        }
        ServDao.save(ServMgr.SY_COMM_MIND, paramBean);

        OutBean rtnBean = new OutBean();
        rtnBean.setOk("保存补登意见成功");
        return rtnBean;
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @return 补登意见Bean
     */
    public OutBean getBudengMind(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();

        Bean queryBean = new Bean();

        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and WF_NI_ID = '");
        strWhere.append(paramBean.getStr("WF_NI_ID"));
        strWhere.append("' and DATA_ID = '");
        strWhere.append(paramBean.getStr("DATA_ID"));
        strWhere.append("' and BD_USER = '");
        strWhere.append(userBean.getCode());
        strWhere.append("' and BD_USER is not null");

        queryBean.set(Constant.PARAM_WHERE, strWhere);
        
        Bean mindBean = ServDao.find(ServMgr.SY_COMM_MIND, queryBean);
        
        if (null == mindBean) {
            mindBean = new Bean();
        }
        
        OutBean bdMind = new OutBean(mindBean);

        return bdMind;
    }

    /**
     * @param paramBean 前台获取的值
     *            根据SY_COMM_MIND中的MIND_CODE,去SY_COMM_MIND_CODE表中查询出MIND_LEVEL(意见级别,30:公司领导意见,20:部门领导意见,10:处室领导意见,1:普通)，
     *            将SCREEN_RULE保存到SY_COMM_MIND表中
     */
    public void beforeSave(ParamBean paramBean) {
        
        String mindCode = "";
        if (paramBean.isNotEmpty("MIND_CODE")) {
            mindCode = paramBean.getStr("MIND_CODE");
        } else {
            mindCode = paramBean.getSaveOldData().getStr("MIND_CODE");
        }
        Bean mindCodeBean = ServDao.find(ServMgr.SY_COMM_MIND_CODE, mindCode);
        // 判断是否取到 SY_COMM_MIND_CODE 表对应的Bean对象
        if (mindCodeBean.equals("") || mindCodeBean == null) {
            log.warn("意见类型不存在。mindCode=" + mindCode);
            throw new TipException("意见类型不存在。");
        } else { // 如果对象中有值则取MIND_LEVEL
            int mindLevel = mindCodeBean.getInt("MIND_LEVEL");
            paramBean.set("MIND_LEVEL", mindLevel);
            paramBean.set("MIND_DIS_RULE", mindCodeBean.getStr("MIND_DIS_RULE"));
            paramBean.set("MIND_CODE_NAME", mindCodeBean.getStr("CODE_NAME"));
            appendMindLevel(paramBean, mindLevel);
        }
        
        if (paramBean.getAddFlag()) {
            //设置签意见人信息
            UserBean doUser = getDoUserBean(paramBean);
            paramBean.set("S_DEPT", doUser.getDeptCode());
            paramBean.set("S_DNAME", doUser.getDeptName());
            paramBean.set("S_ODEPT", doUser.getODeptCode());
            paramBean.set("S_TDEPT", doUser.getTDeptCode());
            paramBean.set("S_TNAME", doUser.getTDeptName());
            paramBean.set("S_UNAME", doUser.getName());
            paramBean.set("S_USER", doUser.getCode());
            
            if (!Context.isCurrentUser(doUser)) {
                //如果当前用户不是办理用户，则把当前用户作为意见补登人（或委托办理人）。
                UserBean currUser = Context.getUserBean();
                paramBean.set("BD_UNAME", currUser.getName());
                paramBean.set("BD_USER", currUser.getCode());
            }
        }
    }

    /**
     * 
     * @param paramBean 参数Bean
     * @param mindLevel 意见级别
     */
    private void appendMindLevel(Bean paramBean, int mindLevel) {
        // 如果mindLevel这个值==30，则是公司领导意见。则判断当前服务对应的表是否有“S_HAS_PS_MIND(领导批示意见)”字段
        if (mindLevel != 30) {
            return;
        }

        // 根据服务ID，获取服务定义信息
        String servId = paramBean.getStr("SERV_ID");
        ServDefBean psMind = null;
        if (!StringUtils.isEmpty(servId)) {
            psMind = ServUtils.getServDef(servId);
        }

        // 根据服务判断是否有“S_HAS_PS_MIND”字段，如果存在该字段，则根据服务ID、DATA_ID获取“S_HAS_PS_MIND”的值，如果S_has_ps_mind
        // 没值，则直接将当前用户的ODEPT_CODE更新到S_has_ps_mind中，如果有值，则更新数据，将当前的值追加到S_has_ps_mind值后面，用逗号分隔
        if (psMind == null || !psMind.containsItem("S_HAS_PS_MIND")) {
            return;
        }
        // 获取当前用户的ODEPT_CODE(机构编码)
        String userOdept = Context.getUserBean().getODeptCode();

        // 根据servID查询出服务,再获取服务是否存在S_HAS_PS_MIND值
        Bean resuleBean = ServDao.find(servId, paramBean.getStr("DATA_ID"));
        if (resuleBean.isEmpty("S_HAS_PS_MIND")) { // 如果s_has_ps_mind为空，则直接更新值
            resuleBean.set("S_HAS_PS_MIND", userOdept);
        } else {
            // 判断是否存在当前机构的odept_code,不存在则直接追加数据
            if (resuleBean.getStr("S_HAS_PS_MIND").indexOf(userOdept) < 0) {
                // 将当前的机构编码追加到已有数据后面，用逗号分隔
                String allDeptCode = resuleBean.getStr("S_HAS_PS_MIND") + "," + userOdept;
                resuleBean.set("S_HAS_PS_MIND", allDeptCode);
            }
        }
        // 更新获取到的服务中的S_HAS_PS_MIND列值；
        ServDao.update(servId, resuleBean);
    }

    /**
     * @param paramBean 参数Bean
     * @return 意见Bean
     */
    public OutBean leaderMind(ParamBean paramBean) {

        UserBean userInfo = Context.getUserBean();
        String odept = userInfo.getODeptCode();
        String servID = paramBean.getStr("SERV_ID");
        String dataID = paramBean.getStr("DATA_ID");
        String whereStr = " and SERV_ID='" + servID + "' and DATA_ID='" + dataID + "' and MIND_LEVEL=30 and S_ODEPT='"
                + odept + "'";
        List<Bean> dataBean = ServDao.finds(ServMgr.SY_COMM_MIND, whereStr);
        return new OutBean().setData(dataBean);
    }

    /**
     * @param paramBean 参数Bean
     * @return 检查意见是否填写
     */
    public OutBean checkFillMind(ParamBean paramBean) {
        OutBean outBean = new OutBean();

        String dataId = paramBean.getStr("DATA_ID");
        String nid = paramBean.getStr("NI_ID");

        Bean regularMind = JsonUtils.toBean(paramBean.getStr(MindServ.MIND_TYPE_REGULAR));
        Bean terminalMind = JsonUtils.toBean(paramBean.getStr(MindServ.MIND_TYPE_TERMINAL));
        Bean generalMind = JsonUtils.toBean(paramBean.getStr(MindServ.MIND_TYPE_GENERAL));

        SqlBean sql = new SqlBean();
        sql.and("DATA_ID", dataId);
        sql.and("WF_NI_ID", nid);

        List<Bean> minds = ServDao.finds(ServMgr.SY_COMM_MIND, sql);
        Map<String, Bean> mindMap = MindUtils.getMindMap(minds);

        outBean.set("pass", "NO");
        if (regularMind.isNotEmpty("CODE_ID")) { // 有这种意见
            if (regularMind.getInt("MIND_MUST") == Constant.YES_INT) { // 必填
                if (null == mindMap.get(regularMind.getStr("CODE_ID"))) { // 意见表里没有
                    outBean.set("reason", "请填写 " + regularMind.getStr("CODE_NAME"));

                    return outBean;
                }
            }
        }

        if (terminalMind.isNotEmpty("CODE_ID")) { // 有这种意见
            if (terminalMind.getInt("MIND_MUST") == Constant.YES_INT) { // 必填
                if (null == mindMap.get(terminalMind.getStr("CODE_ID"))) { // 意见表里没有
                    outBean.set("reason", "请填写 " + terminalMind.getStr("CODE_NAME"));

                    return outBean;
                }
            }
        }

        if (generalMind.isNotEmpty("CODE_ID")) { // 有这种意见
            if (generalMind.getInt("MIND_MUST") == Constant.YES_INT) { // 必填
                if (null == mindMap.get(generalMind.getStr("CODE_ID"))) { // 意见表里没有
                    outBean.set("reason", "请填写 " + generalMind.getStr("CODE_NAME"));

                    return outBean;
                }
            }
        }

        outBean.set("pass", "YES");
        return outBean;
    }
    
    /**
     * 
     * @param paramBean 参数Bean 
     * @return 最后的那条固定意见
     */
    public OutBean getLastRegularMind(ParamBean paramBean) {
        SqlBean sql = new SqlBean();
        sql.and("DATA_ID", paramBean.getStr("DATA_ID"));
        sql.andNotNull("USUAL_ID");
        sql.desc("S_MTIME");
        
        Bean mindBean = ServDao.find(ServMgr.SY_COMM_MIND, sql);
        //如果没有意见则认为同意
        if (null == mindBean) {
            return new OutBean(new Bean().set("MIND_VALUE", MindUtils.MIND_TONGYI));
        } else {
            return new OutBean(mindBean);
        }
    }
    
    /**
     * 
     * @param paramBean 参数Bean 
     * @return 根据参数获取意见列表
     */
    public OutBean getMindList(ParamBean paramBean) {
        UserBean userBean = Context.getUserBean();
        
        String servId = paramBean.getStr("SERV_ID");
        String dataId = paramBean.getStr("DATA_ID");
        String odeptCode = userBean.getODeptCode();
        
        if (paramBean.isNotEmpty("ODEPT_CODE")) {
            odeptCode = paramBean.getStr("ODEPT_CODE");
        }
        
        String sortType = "TIME";
        if (paramBean.isNotEmpty("sortType")) {
            sortType = paramBean.getStr("sortType");
        }
        
        UserMind userMind = new UserMind(userBean);
        userMind.query(servId, dataId, sortType);
        
        List<Bean> mindList = userMind.getMindList(odeptCode); //获取指定机构的意见列表
        
        if (paramBean.getInt("FETCH_ALL") == 1) { //一次获取所有机构的意见
            mindList = userMind.getMindList();
        }
        
        OutBean outBean = new OutBean();
        if (paramBean.isNotEmpty("ODEPT_LIST")) { //需要获取机构列表
            List<Bean> odeptList = userMind.getOdeptList();
            
            outBean.set("ODEPT_LIST", odeptList);
        }
       
        outBean.set("MIND_LIST", mindList);
        outBean.setOk();
        
        return outBean;
    }
}
