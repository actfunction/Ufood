/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.WfParamBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.relate.RelateMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;

/**
 * 公共表单服务抽象类，为CommonServ提供一些公共方法支持，一般表单服务类不要继承自这个类，要继承自CommonServ.java。
 * 
 * @author wanglong
 */
public abstract class AbstractServ extends BaseServ {

    /**
     * 保存之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     *      可以通过paramBean获取数据库中的原始数据信息：
     *          Bean oldBean = paramBean.getSaveOldData();
     *      可以通过方法paramBean.getFullData()获取数据库原始数据加上修改数据的完整的数据信息：
     *          Bean fullBean = paramBean.getSaveFullData();
     *      可以通过paramBean.getAddFlag()是否为true判断是否为添加模式
     */
    protected void beforeSave(ParamBean paramBean) {
    }
    
    /**
     * 批量保存之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeBatchSave(ParamBean paramBean) {
    }
    
    /**
     * 删除之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     *      可以通过paramBean获取删除标志：true真删除，false假删除
     *          boolean defFlag = paramBean.getDeleteDropFlag();
     *      可以通过paramBean获取所有待删除数据的列表
     *          List<Bean> dataList = paramBean.getDeleteDatas();
     */
    protected void beforeDelete(ParamBean paramBean) {
    }
    
    /**
     * 添加待办之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     * TODO_DATA 待办数据
     * NODE_DEF 节点定义对象
     * SERV_DATA 表单数据
     */
    protected void beforeInsertTodo(ParamBean paramBean) {
    }
    
    /**
     * 查询之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeQuery(ParamBean paramBean) {
    }
    
    /**
     * 查询之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeFinds(ParamBean paramBean) {
    }
    
    /**
     * 单条记录显示之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeByid(ParamBean paramBean) {
    }
    
    /**
     * 用户自定义打印数据
     * @param paramBean 参数信息
     * @param outBean ByID方法的执行结果
     */
    protected void beforePrint(ParamBean paramBean, OutBean outBean) {
    }
    
    /**
     * 启动流程之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param dataBean 数据信息
     */
    protected void beforeStart(ParamBean paramBean, Bean dataBean) {
    }
    
    /**
     * 办结流程之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeFinish(ParamBean paramBean) {
    }
    
    /**
     * 导出excel之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeExp(ParamBean paramBean) {
    }
    
    /**
     * 导入excel之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeImp(ParamBean paramBean) {
    }
    
    /**
     * 导出压缩数据之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeExpZip(ParamBean paramBean) {
    }
    
    /**
     * 导入压缩数据之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeImpZip(ParamBean paramBean) {
    }

    /**
     * 保存之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     *      可以通过paramBean获取数据库中的原始数据信息：
     *          Bean oldBean = paramBean.getSaveOldData();
     *      可以通过方法paramBean.getFullData()获取数据库原始数据加上修改数据的完整的数据信息：
     *          Bean fullBean = paramBean.getSaveFullData();
     *      可以通过paramBean.getAddFlag()是否为true判断是否为添加模式
     * @param outBean 输出信息
     *      可以通过outBean.getSaveIds()获取实际插入的数据主键
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
    }
    
    /**
     * 批量保存之后的拦截方法
     * @param paramBean 参数信息
     *      通过paramBean.getBatchSaveDatas()获取待保存的数据列表
     *      通过paramBean.getBatchSaveDelIds()获取待删除的数据主键列表，多个逗号分隔
     * @param outBean 输出信息
     *      通过outBean.getSaveIds()获取实际插入以及修改的数据主键
     */
    protected void afterBatchSave(ParamBean paramBean, OutBean outBean) {
    }
    
    /**
     * 删除之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     *      通过paramBean.getDeleteDatas()获取待删除的数量列表（含完整数据信息）
     *      通过paramBean.getDeleteDropFlag()获取真删除标志
     * @param outBean 输出信息 
     *      通过outBean.getDeleteIds()获取已删除的数据主键列表，多个逗号分隔
     *      通过outBean.getDataList()获取所有已删除的数据对象信息
     *      List<Bean> dataList = outBean.getOkDatas(); 
     *      for (Bean data : dataList) {
     *      }
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
    }

    /**
     * 页面查询之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
    }
    

    /**
     * 查询之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterFinds(ParamBean paramBean, OutBean outBean) {
    }
    /**
     * 单条记录显示之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterByid(ParamBean paramBean, OutBean outBean) {
    }
    
    /**
     * 启动流程之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterStart(ParamBean paramBean, OutBean outBean) {
    }
    
    /**
     * 办结流程之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void afterFinish(ParamBean paramBean) {
    }
    
    /**
     * 导出excel执行查询之后的拦截方法，由子类重载
     * 在输出Excel流之前触发
     * 可以通过outBean.geDataList() outBean.getCols()获取要导出的数据信息进行加工处理
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterExp(ParamBean paramBean, OutBean outBean) {
    }
    
    /**
     * 执行工作流的启动之前的处理
     * @param wfParam 实例化的流程信息
     * @param dataBean 数据信息
     */
    protected void beforeStartWf(WfParamBean wfParam, Bean dataBean) {
	}
    
    /**
     * 执行工作流的启动处理
     * @param servId 服务编码
     * @param dataBean 数据信息
     * @return 是否启动了工作流
     */
    protected boolean startWf(String servId, Bean dataBean) {
        WfParamBean param = new WfParamBean(WfParamBean.ACT_START);
        beforeStartWf(param, dataBean);
        param.setDataServId(servId).setDataBean(dataBean);
        return ServMgr.act(param).isOk();
    }

	/**
     * 执行工作流的删除处理
     * @param dataBean 数据信息
     * @param falseDel 是否假删除
     */
    protected void deleteWf(Bean dataBean, boolean falseDel) {
        if (dataBean.isNotEmpty("S_WF_INST")) { // 绑定了工作流程
            String procInstId = dataBean.getStr("S_WF_INST"); // 流程实例ID
            int procState = dataBean.getInt("S_WF_STATE"); // 流程状态，是否在流程中
            WfParamBean param = new WfParamBean(WfParamBean.ACT_DELETE);
            param.setProcInstCode(procInstId).setProcState(procState);
            param.set("falseDel", falseDel);
            param.set("S_FLAG", dataBean.getInt("S_FLAG"));
            ServMgr.act(param); //执行流程删除操作
        }
    }

    /**
     * 处理级联修改操作,采用servBean.updates进行批量更新
     * @param servId 服务编码
     * @param oldBean 原主数据信息
     * @param newBean 新主数据信息
     * @return 是否保存成功
     */
    @SuppressWarnings("unchecked")
    protected OutBean linkUpdate(String servId, Bean oldBean, Bean newBean) {
        OutBean outBean = new OutBean();
        StringBuilder msg = new StringBuilder();
        ServDefBean servDef = ServUtils.getServDef(servId);
        // 进行关联保存的判断
        LinkedHashMap<String, Bean> links = servDef.getAllLinks();
        for (Object key : links.keySet()) {
            Bean link = links.get(key);
            if ((link.getInt("LNKE_READONLY") == Constant.YES_INT)
                    || (link.getInt("LINK_UPDATE_FLAG") != Constant.YES_INT)) { // 忽略只读关联以及非关联更新设定
                continue;
            }
            // 进行批量提交数据的判断，执行批量保存
            String linkServ = link.getStr("LINK_SERV_ID"); // 批量保存想数据键值
            String delsKey = linkServ + "__DELS"; // delete项数据数据键值
            if (newBean.contains(linkServ) || newBean.contains(delsKey)) { // 存在级联修改的数据
                ParamBean param = new ParamBean(linkServ, ServMgr.ACT_BATCHSAVE);
                param.setBatchSaveDatas(newBean.getList(linkServ));
                param.setBatchSaveDelIds(newBean.getStr(delsKey));
                param.set("SERV_ID", link.getStr("LINK_SERV_ID"));
                param.set(Constant.IS_LINK_ACT, true); // 设定当前处理运行在级联处理模式下
                param.setLinkFlag(true); // 设定级联处理标志
                OutBean linkOutBean = ServMgr.act(param);
                if (!linkOutBean.isOk()) {
                    msg.append(linkOutBean.getStr(Constant.RTN_MSG)).append(" ");
                }
            } else { // 进行变更项的判断，执行主子保存处理
                Bean setBean = new Bean();
                Bean whereBean = new Bean();
                List<Bean> linkItems = (ArrayList<Bean>) link.get("SY_SERV_LINK_ITEM");
                boolean canUpdate = false;
                for (Bean item : linkItems) {
                    if (item.getInt("LINK_VALUE_FLAG") == Constant.YES_INT) {
                        if (newBean.contains(item.get("ITEM_CODE"))) { // 变更字段涉及传值字段
                            canUpdate = true;
                            setBean.set(item.get("LINK_ITEM_CODE"), newBean.getStr(item.get("ITEM_CODE")));
                        }
                        if (item.getInt("LINK_WHERE_FLAG") == Constant.YES_INT) { // 过滤条件
                            whereBean.set(item.get("LINK_ITEM_CODE"), oldBean.get(item.get("ITEM_CODE")));
                        }
                    } else if (item.getInt("LINK_WHERE_FLAG") == Constant.YES_INT) { // 固定值过滤条件
                        whereBean.set(item.get("LINK_ITEM_CODE"), item.get("ITEM_CODE"));
                    }
                }
                if (whereBean.size() == 0) { // 没有设置where条件，不允许级联更新
                    canUpdate = false;
                }
                if (canUpdate) { // 执行批量更新处理
                    ServDao.updates(link.getStr("LINK_SERV_ID"), setBean, whereBean);
                }
            }
        } //end for
        if (msg.length() > 0) {
            outBean.setMsg(msg.toString());
        } else {
            outBean.setOk();
        }
        return outBean;
    }

    /**
     * 更新文件、相关文件等对应的数据主键
     * @param servDef 服务定义
     * @param oldKey 原主键
     * @param newKey 新主键
     */
    protected void updateRelatePKey(ServDefBean servDef, String oldKey, String newKey) {
        if (StringUtils.isEmpty(oldKey)) {
            return;
        }
        //更新文件主键
        if (servDef.hasFile()) { //包含文件信息
            Bean whereBean, setBean;
            whereBean = new Bean().set("SERV_ID", servDef.getSrcId()).set("DATA_ID", oldKey);
            setBean = new Bean().set("DATA_ID", newKey);
            ServDao.updates(ServMgr.SY_COMM_FILE, setBean, whereBean);
        }
        //更新相关文件
        if (servDef.hasRelate()) {
            RelateMgr.updateRelate(servDef, oldKey, newKey);
        }
    }

    /**
     * 处理级联删除操作
     * @param servId 服务编码
     * @param dataBean 主数据信息
     * @param delFlag 删除标志
     *      true为真删除，无论子服务是否启用了假删除全部真删除；
     *      false为假删除，子服务为真删除时忽略，为假时假删除相关数据
     * @param linkFlag 是否强制级联删除， true:强制级联删除；false:按照关联设定进行删除处理
     */
    protected void linkDelete(String servId, Bean dataBean, Boolean delFlag, Boolean linkFlag) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        // 进行关联删除的判断
        LinkedHashMap<String, Bean> links = servDef.getAllLinks();
        for (Object key : links.keySet()) {
            Bean link = links.get(key);
            if (link.getInt("LINK_READONLY") == Constant.YES_INT) { // 忽略只读关联
                continue;
            }
            if (link.isNotEmpty("LINK_EXPRESSION") && !Lang.isTrueScript(
                    ServUtils.replaceSysAndData(link.getStr("LINK_EXPRESSION"), dataBean))) { // 忽略表达式不符合
                continue;
            }
            ServDefBean linkServDef = ServUtils.getServDef(link.getStr("LINK_SERV_ID"));
            if (!delFlag && !linkServDef.hasFalseDelete()) { //父假删除、子不是假删除的忽略
                continue;
            }
            List<String> datas = ServUtils.getLinkDataIds(servId, link, dataBean, delFlag);
            if (!datas.isEmpty()) {
                if (link.getInt("LINK_DELETE_FLAG") == Constant.YES_INT || linkFlag) { // 关联删除
                    ParamBean param = new ParamBean(link.getStr("LINK_SERV_ID"), ServMgr.ACT_DELETE);
                    param.setId(Strings.toString(datas));
                    param.setLinkFlag(linkFlag); // 传递强制级联处理标志（过载缺省的服务设定）
                    param.set(Constant.IS_LINK_ACT, true); // 设定当前处理运行在级联处理模式下
                    param.setDeleteDropFlag(delFlag); // 传递是否强制删除的标志
                    ServMgr.act(param);
                } else { // 不允许关联删除
                    throw new TipException(Context.getSyMsg("SY_DELETE_LINK_ERROR",
                            link.getStr("LINK_NAME"), String.valueOf(datas.size())));
                }
            }
        }
    }

    /**
     * 为显示列处理相关设定，包括字典名称列的处理
     * @param cols 列显示设定列表
     * @param itemBean 数据项定义
     * @param listFlag 列显示标志
     */
    protected void addCols(LinkedHashMap<String, Bean> cols, Bean itemBean, int listFlag) {
        Bean colBean = itemBean.copyOf(new String[] { "ITEM_CODE", "ITEM_NAME"});
        String code = colBean.getStr("ITEM_CODE");
        if (itemBean.isNotEmpty("DICT_ID")) { // 字典项需要单独生成生成一份名称列，用原有的显示标志，编码隐藏
            Bean nameBean = colBean.copyOf();
            String nameCode = code + "__NAME";
            nameBean.set("ITEM_CODE", nameCode);
            // 使用原字段定义的是否显示来控制字典项
            nameBean.set("ITEM_LIST_FLAG", listFlag);
            cols.put(nameCode, nameBean);
            colBean.set("ITEM_LIST_FLAG", Constant.NO_INT);
        } else { //非字典项采用指定的列表标志
            colBean.set("ITEM_LIST_FLAG", listFlag);
        }
        cols.put(code, colBean);
    }

    /**
     * 批量处理文件关联
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void batchFile(ParamBean paramBean, OutBean outBean) {
        ServDefBean servDef = ServUtils.getServDef(paramBean.getServId());
        if (paramBean.contains("ADDFILE")) {
            ParamBean param = new ParamBean(ServMgr.SY_COMM_FILE, "update", paramBean.getStr("ADDFILE"));
            param.set("SERV_ID", servDef.getSrcId());
            param.set("DATA_ID", outBean.getId());
            ServMgr.act(param);
            // 设置入outBean供索引进行处理
            String fileIds;
            if (outBean.contains(Constant.RTN_FILE_IDS)) {
                fileIds = outBean.getStr(Constant.RTN_FILE_IDS);
            } else {
                fileIds = "";
            }
            outBean.set(Constant.RTN_FILE_IDS, fileIds + paramBean.getStr("ADDFILE"));
        }
        if (paramBean.contains("DELFILE")) {
            ParamBean param = new ParamBean(ServMgr.SY_COMM_FILE, ServMgr.ACT_DELETE, paramBean.getStr("DELFILE"));
            ServMgr.act(param);
        }
    }

    /**
     * 从参数中以及服务定义中获取完整的过滤条件，给各个服务的方法使用。
     * @param paramBean 参数信息
     * @return 完整的过滤条件
     */
    @SuppressWarnings("unchecked")
    protected String getFullWhere(ParamBean paramBean) {
        String servId = paramBean.getServId();
        ServDefBean serv = ServUtils.getServDef(servId);
        StringBuilder where = new StringBuilder(); // 处理WHERE条件
        if (paramBean.isNotEmpty(Constant.PARAM_WHERE)) { // 获取ParamBean里的_WHERE_
            where.append(paramBean.getStr(Constant.PARAM_WHERE));
        }
        if (paramBean.isNotEmpty("_linkWhere")) { // 有linkwhere则服务设定的过滤就不起作用，除非设置了忽略
            where.append(ServUtils.replaceSysVars(paramBean.getStr("_linkWhere")));
            if (paramBean.getInt("_linkServQuery") == Constant.YES_INT) { //关联中启用了服务过滤规则
                where.append(serv.getServDefWhere());
            }
        } else { // 没有linkwhere则以服务设定的过滤为基础
            where.append(serv.getServDefWhere());
        }
        if (paramBean.isNotEmpty("_queryId")) { // 常用查询
            Bean queryBean = (Bean) serv.getAllQueries().get(paramBean.getStr("_queryId"));
            if (queryBean != null) {
                where.append(" ").append(ServUtils.replaceSysVars(queryBean.getStr("QUERY_SQL")));
            }
        }
        where.append(" ").append(ServUtils.replaceSysVars(paramBean.getStr("_extWhere")))
                .append(ServUtils.getSearchWhere(serv, paramBean.getStr("_searchWhere")));
        if (paramBean.contains("_treeWhere") && paramBean.isNotEmpty("_treeWhere")) {
            where.append(ServUtils.getTreeWhere(serv, (List<Bean>) paramBean.get("_treeWhere")));
        }
        return where.toString();
    }
    
    /**
     * 对于外部字典类服务，提前预处理字典相关字段（CODE_PATH和CODE_LEVEL）
     * @param servDef   服务定义
     * @param paramBean 参数信息
     */
    protected void beforeAddTreeDictField(ServDefBean servDef, Bean paramBean) {
        String dictId = Strings.getFirstBySep(servDef.getDictCodes());
        Bean dict = DictMgr.getDict(dictId);
        if (DictMgr.isSingleTree(dict)) { //独立树形字典
            String code = dict.getStr("DICT_F_ID");
            String pCode = dict.getStr("DICT_F_PARENT");
            String codePath = dict.getStr("DICT_F_PATH");
            String level = dict.getStr("DICT_F_LEVEL");
            if (codePath.length() == 0 || level.length() == 0) { //忽略没有设定CODE_PATH和CODE_LEVEL字段的
                return;
            }
            if (paramBean.isEmpty(pCode)) { //没有选父数据，自身缺省为第一级
                paramBean.set(level, 1);
                paramBean.set(codePath, paramBean.getStr(code) + Constant.CODE_PATH_SEPERATOR);
            } else { //选了父数据
                Bean pData = ServDao.find(servDef.getId(), paramBean.getStr(pCode));
                if (pData != null) {
                    paramBean.set(level, pData.getInt(level) + 1);
                    paramBean.set(codePath, pData.getStr(codePath) + paramBean.getStr(code) 
                            + Constant.CODE_PATH_SEPERATOR);
                }
            }
        }
    }
    
    /**
     * 对于外部字典类服务，提前预处理字典相关字段（CODE_PATH和CODE_LEVEL）
     * @param servDef   服务定义
     * @param paramBean 参数信息
     */
    protected void beforeModifyTreeDictField(ServDefBean servDef, ParamBean paramBean) {
        String dictId = Strings.getFirstBySep(servDef.getDictCodes());
        Bean dict = DictMgr.getDict(dictId);
        if (dict != null && DictMgr.isSingleTree(dict)) { //独立树形字典
            String pCode = dict.getStr("DICT_F_PARENT");
            String codePath = dict.getStr("DICT_F_PATH");
            String level = dict.getStr("DICT_F_LEVEL");
            if (codePath.length() == 0 || level.length() == 0) { //忽略没有设定CODE_PATH和CODE_LEVEL字段的
                return;
            }
            if (paramBean.contains(pCode) || paramBean.contains(servDef.getPKey())) { //变更了父编码或者编码
                Bean fullData = paramBean.getSaveFullData();
                String pId = fullData.getStr(pCode);
                String id = fullData.getStr(servDef.getPKey());
                paramBean.set(servDef.getPKey(), id); //为级联处理初始化编码参数
                if (pId.length() == 0) { //没有父，自身为第一级
                    paramBean.set(level, 1);
                    paramBean.set(codePath, id + Constant.CODE_PATH_SEPERATOR);
                } else {
                    Bean pData = ServDao.find(servDef.getId(), pId);
                    if (pData != null) {
                        if (pData.getStr(codePath).indexOf(fullData.getStr(codePath)) >= 0) { //自身的子不能作为父
                            throw new TipException(Context.getSyMsg("SY_SERV_DICT_PCODE_ERROR"));
                        }
                        paramBean.set(level, pData.getInt(level) + 1);
                        paramBean.set(codePath, pData.getStr(codePath) + id + Constant.CODE_PATH_SEPERATOR);
                    } else {
                        paramBean.set(level, 1);
                        paramBean.set(codePath, id + Constant.CODE_PATH_SEPERATOR);
                    }
                }
                recurSubDatas(dict, paramBean);
            }
        }
    }
    
    
    /**
     * 对于外部字典类服务，提前预处理字典相关字段（CODE_PATH和CODE_LEVEL）
     * @param servDef   服务定义
     * @param paramBean 参数信息
     */
    protected void beforeDeleteTreeDict(ServDefBean servDef, Bean paramBean) {
        String servId = servDef.getId();
        String dictId = Strings.getFirstBySep(servDef.getDictCodes());
        Bean dict = DictMgr.getDict(dictId);
        if (DictMgr.isSingleTree(dict)) { //独立树形字典
            String code = dict.getStr("DICT_F_ID");
            String flag = dict.getStr("DICT_F_FLAG");
            Bean param = new Bean();
            param.set(dict.getStr("DICT_F_PARENT"), paramBean.getStr(code));
            if (flag.length() > 0) { //如果启用标志字段，要求过滤所有有效的数据
                param.set(flag, Constant.YES_INT);
            }
            int count = ServDao.count(servId, param);
            if (count > 0) { //存在子数据
                throw new TipException(Context.getSyMsg("SY_DELETE_EXIST_SUB_ERROR", String.valueOf(count)));
            }
        }
    }
    
    /**
     * 递归处理所有子孙的服务字典字段（CODE_PATH和LEVEL）
     * @param dict  字典定义
     * @param data  数据，需要预制好ID和CODE_PATH、CODE_LEVEL
     */
    private void recurSubDatas(Bean dict, Bean data) {
        String servId = data.getStr(Constant.PARAM_SERV_ID);
        String code = dict.getStr("DICT_F_ID");
        String pCode = dict.getStr("DICT_F_PARENT");
        String codePath = dict.getStr("DICT_F_PATH");
        String level = dict.getStr("DICT_F_LEVEL");
        if (data.getId().length() == 0) { //要求必须有主键数据才进行递归处理
            return;
        }
        List<Bean> subList = ServDao.finds(servId, 
                new Bean().set(pCode, data.getId()).set(Constant.PARAM_SELECT, code));
        for (Bean sub : subList) {
            sub.set(level, data.getInt(level) + 1);
            sub.set(codePath, data.getStr(codePath) + sub.getStr(code) + Constant.CODE_PATH_SEPERATOR);
            sub.set(Constant.PARAM_SERV_ID, servId);
            sub.set(pCode, data.getStr(code));
            recurSubDatas(dict, sub);
            ServDao.update(servId, sub);
        }
    }
    
    /**
     * 获取实际的办理用户
     * @param paramBean 参数信息
     * @return 取得流程的办理用户 ， 如果参数中包含_AGENT_USER_， 则返回这个用户的UserBean表示当前用户代替该用户办理,否则取当前用户UserBean
     */
    public UserBean getDoUserBean(ParamBean paramBean) {
        // 获取 委托人 的 UserBean
        if (!paramBean.isEmpty(OrgConstant.AGENT_USER)) {
            String realUserId = paramBean.getStr(OrgConstant.AGENT_USER);
            UserBean doUserBean = UserMgr.getUser(realUserId);
            return doUserBean;
        }

        return Context.getUserBean();
    }
}
