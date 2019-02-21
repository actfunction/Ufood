package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfServCorrespond;
import com.rh.core.wfe.resource.GroupBean;

/**
 * 子流程数据准备接口的抽象类，提供公共的方法。
 * @author wanglong
 * 
 */
public abstract class AbstractSubProcessPreparer implements SubProcessPreparer {

    /** 父任务实例act，即主流程的流程节点任务实例 */
    protected WfAct parentAct = null;

    /**
     * 根据子流程数据，确定要使用的子流程定义。可以根据自己的业务需要进行扩展。
     * @param servId 子流程服务
     * @param dataBean 数据信息
     * @return 子流程定义
     */
    public WfProcDef getProcDef(String servId, Bean dataBean) {
        return WfServCorrespond.getProcDef(servId, dataBean);
    }

    @Override
    public List<Bean> prepareData(List<GroupBean> startUsers) {
        WfNodeDef parentNodeDef = parentAct.getNodeDef();
        WfProcess parentProcess = parentAct.getProcess();

        if (parentNodeDef.getInt("CREATE_DATA_FLAG") == Constant.YES_INT) {
            return createDataFromLinkConfig(parentProcess.getServId(), parentProcess.getServInstBean(),
                    parentNodeDef.getStr("SUB_SERVICE_ID"), startUsers.size());
        } else {
            return getDataFromLinkConfig(parentProcess.getServId(), parentProcess.getServInstBean(),
                    parentNodeDef.getStr("SUB_SERVICE_ID"));
        }
    }

    /**
     * 根据关联定义取服务targetServId的数据
     * @param sourceServId 源数据服务Id
     * @param sourceData 源数据
     * @param targetServId 要生成的数据的服务Id
     * @return 获取的数据列表
     */
    protected List<Bean> getDataFromLinkConfig(String sourceServId, Bean sourceData, String targetServId) {
        Bean link = getLink(sourceServId, targetServId);
        return ServUtils.getLinkDataList(sourceServId, link, sourceData, 1);
    }

    /**
     * 根据关联定义创造服务targetServId的数据。多份数据间没有差别。
     * @param sourceServId 源数据服务Id
     * @param sourceData 源数据
     * @param targetServId 要生成的数据的服务Id
     * @param amount 生成的数据的数量
     * @return 生成的数据列表
     */
    protected List<Bean> createDataFromLinkConfig(String sourceServId, Bean sourceData, 
            String targetServId, int amount) {
        Bean link = getLink(sourceServId, targetServId);
        List<Bean> linkItems = link.getList("SY_SERV_LINK_ITEM");
        Bean dataBean = new Bean();
        for (Bean item : linkItems) {
            if (item.getInt("LINK_WHERE_FLAG") == Constant.YES_INT) { // 过滤条件
                if (item.getInt("LINK_VALUE_FLAG") == Constant.YES_INT) { // 子单数据项值
                    dataBean.set(item.get("LINK_ITEM_CODE"), sourceData.get(item.get("ITEM_CODE")));
                }
            }
        }
        List<Bean> dataBeanList = new ArrayList<Bean>();
        for (int i = 0; i < amount; i++) {
            Bean targetDataBean = ServMgr.act(targetServId, ServMgr.ACT_BYID, new ParamBean());
            targetDataBean.copyFrom(dataBean);
            dataBeanList.add(targetDataBean);
        }

        return dataBeanList;
    }

    /**
     * 获取两服务的关联信息
     * @param parentServId 主服务
     * @param childServId 子服务
     * @return 服务的关联信息
     */
    protected Bean getLink(String parentServId, String childServId) {
        ServDefBean parentServDef = ServUtils.getServDef(parentServId);
        Map<String, Bean> links = parentServDef.getAllLinks();
        return links.get(childServId);

    }

    @Override
    public WfAct getParentAct() {
        return parentAct;
    }

    @Override
    public void setParentAct(WfAct parentAct) {
        this.parentAct = parentAct;
    }

}
