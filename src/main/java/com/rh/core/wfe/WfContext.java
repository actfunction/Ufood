package com.rh.core.wfe;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;

/**
 * 
 * @author wanglong
 * 
 */
public class WfContext {
    /** 办理用户 */
    private static final String DO_USER = "DO_USER";

    /** 应用数据 */
    public static final String APP_DATA = "_APP_DATA";

    private static final String NODE_INST_LIST = "NODE_INST_LIST";

    /** 线程级变量 */
    private static ThreadLocal<Bean> thread = new ThreadLocal<Bean>() {
        public Bean initialValue() {
            return new Bean();
        }
    };

    /**
     * 清理ThreadLocal中的数据
     */
    public static void cleanThreadData() {
        thread.remove();
    }

    /**
     * 
     * @return 线程参数bean
     */
    private static Bean getBean() {
        return thread.get();
    }

    /**
     * 
     * @return 办理用户
     */
    public static UserBean getDoUser() {
        return (UserBean) thread.get().getBean(DO_USER);
    }

    /**
     * 
     * @param userBean 办理用户Bean
     */
    public static void setDoUser(UserBean userBean) {
        thread.get().set(DO_USER, userBean);
    }

    /**
     * 
     * @return 取得应用原始数据
     */
    public static Bean getAppData() {
        return getBean().getBean(APP_DATA);
    }

    /**
     * 
     * @param appBean 设置原始的AppData
     */
    public static void setAppData(Bean appBean) {
        getBean().set(APP_DATA, appBean);
    }

    /**
     * @param piId 流程实例ID
     * @return 取得列表bean
     */
    public static List<Bean> getNodeInstList(String piId) {
        String key = NODE_INST_LIST + "@" + piId;
        return getBean().getList(key);
    }

    /**
     * @param piId 流程实例ID
     * @param list 节点实例列表
     */
    public static void setNodeInstList(String piId, List<Bean> list) {
        String key = NODE_INST_LIST + "@" + piId;
        getBean().set(key, list);
    }

    /**
     * 新增节点
     * @param niBean 节点实例
     */
    public static void addNodeInstBean(Bean niBean) {
        String piId = niBean.getStr("PI_ID");
        List<Bean> list = getNodeInstList(piId);
        list.add(niBean);
    }

    /**
     * 修改节点实例
     * @param niBean 节点实例
     */
    public static void modifyNodeInstBean(Bean niBean) {
        String piId = niBean.getStr("PI_ID");
        List<Bean> list = getNodeInstList(piId);
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            Bean bean = list.get(i);
            if (bean.getId().equals(niBean.getId())) {
                list.remove(i);
                list.add(i, niBean);
            }
        }
    }

}
