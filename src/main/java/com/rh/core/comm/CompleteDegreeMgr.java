package com.rh.core.comm;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
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
import com.rh.core.util.Strings;

/**
 * 通用完整度管理类
 * @author wanglong
 */
public class CompleteDegreeMgr extends CommonServ {
    /** 完整度缓存类型名 */
    private static final String COMPLETESETTING = "SY_COMM_COMPLETE_SETTINGS";
    /** 用户完整度主服务 */
    private static final String USERINFOSRCSERV = "USERINFO";
    /** 等价服务的分隔符 */
    private static final String SEPARATOR_EQUAL_SERV = "#";
    
    static {
        //初始化
        initCompleteDegSettings();
    }
    
    /**
     * 清除缓存
     * @param paramBean paramBean
     * @return OutBean
     */
    public OutBean clearCache(ParamBean paramBean) {
        //CacheMgr.getInstance().clearCache(COMPLETESETTING);
        CacheMgr.getInstance().remove("servScrServHash", COMPLETESETTING);
        CacheMgr.getInstance().remove("itemWeightHash", COMPLETESETTING);
        CacheMgr.getInstance().remove("totalWeightHash", COMPLETESETTING);
        CacheMgr.getInstance().remove("equalServHash", COMPLETESETTING);
        initCompleteDegSettings();
        return new OutBean().setOk();
    }
    
    /**
     * 应用启动时读取完整度配置到缓存中
     * 
     */
    public static void initCompleteDegSettings() {
        // [servcode,srcservcode1:srcservcode2:srcservcoden]映射
        Map<String, HashSet<String>> servScrServHash = new HashMap<String, HashSet<String>>();
        // [srcservcode+servcode+itemcode,weight]映射
        Map<String, Integer> itemWeightHash = new HashMap<String, Integer>();
        // [srcservcode,totalWeight]映射
        Map<String, Integer> totalWeightHash = new HashMap<String, Integer>();
        // [servcode,servcode1#servcode2#...#servcodeN]等价服务映射
        Map<String, String[]> equalServHash = new HashMap<String, String[]>();
        
        //从数据库获取所有配置数据
        ParamBean param = new ParamBean()
            .setServId("SY_COMM_COMPLETE_SETTINGS")
            .setAct(ServMgr.ACT_FINDS)
            .set("S_FLAG", Constant.YES_INT)
            .setOrder("SRC_SERV_CODE, SERV_CODE");
        List<Bean> dataList = ServMgr.act(param).getDataList();
        for (Bean data : dataList) {
            String srcServCode = data.getStr("SRC_SERV_CODE");
            String servCode = data.getStr("SERV_CODE");
            String itemCode = data.getStr("ITEM_CODE");
            int weight = data.getInt("ITEM_WEIGHT");
            
            if (!itemCode.startsWith(SEPARATOR_EQUAL_SERV)) {
                //填充servScrServHash 格式（一条记录：服务-多个主服务）
                if (servScrServHash.containsKey(servCode)) {
                    HashSet<String> set = (HashSet<String>) servScrServHash.get(servCode);
                    set.add(srcServCode);
                } else {
                    HashSet<String> set = new HashSet<String>();
                    set.add(srcServCode);
                    servScrServHash.put(servCode, set);
                }
                
                //填充itemWeightHash 格式（一条记录：主服务+服务+字段名-权重）
                if (!itemWeightHash.containsKey(srcServCode + servCode + itemCode)) {
                    itemWeightHash.put(srcServCode + servCode + itemCode, weight);
                }
                
                //填充totalWeightHash 格式（一条记录：主服务-总权重）
                if (totalWeightHash.containsKey(srcServCode)) {
                    if (weight != 99) { //忽略特殊的99
                        int w = totalWeightHash.get(srcServCode) + weight;
                        totalWeightHash.put(srcServCode, w);
                    }
                } else {
                    totalWeightHash.put(srcServCode, weight);
                }
            //处理等价服务配置
            } else {
                String equalServs = itemCode.substring(1);
                String[] equalServArr = equalServs.split(SEPARATOR_EQUAL_SERV);
                equalServHash.put(servCode, equalServArr);
            }
        }
        
        //放入缓存中
        CacheMgr.getInstance().set("servScrServHash", servScrServHash, COMPLETESETTING);
        CacheMgr.getInstance().set("itemWeightHash", itemWeightHash, COMPLETESETTING);
        CacheMgr.getInstance().set("totalWeightHash", totalWeightHash, COMPLETESETTING);
        CacheMgr.getInstance().set("equalServHash", equalServHash, COMPLETESETTING);
    }
    
    /**
     * (主服务是USERINFO的需要改造成存储在userState中，其他的存储在其ENTITY记录或主记录中)
     * 计算其[服务--dataId]的完整度并置回表中（内存变量会有集群同步的问题暂时不支持）
     * @param servCode 服务ID
     * @param dataCode 数据ID
     * @param act 方法
     * @param dataBean 源数据
     */
    @SuppressWarnings("unchecked")
    public static void computeCompleteDegree(String servCode, String dataCode, Bean dataBean, String act) {
        //从缓存获取配置
        Map<String, HashSet<String>> servScrServHash = (Map<String, HashSet<String>>) CacheMgr.getInstance().get(
                "servScrServHash", COMPLETESETTING);
        Map<String, Integer> itemWeightHash = (Map<String, Integer>) CacheMgr.getInstance().get("itemWeightHash",
                COMPLETESETTING);
        
        if (servScrServHash != null) {
            HashSet<String> srcServCodes = servScrServHash.get(servCode);
            if (srcServCodes != null) {
                for (String srcServCode : srcServCodes) {
                    int deg = 0;
                    boolean multiFlag = false;
                    String foreignKey = "";
                    for (Map.Entry<String, Integer> m : itemWeightHash.entrySet()) {
                        if (m.getKey().indexOf(srcServCode + servCode) < 0) {
                            continue;
                        }
                        Object obj = dataBean.get(Strings.replace(m.getKey(), srcServCode + servCode, ""));
                        boolean exist = false;
                        if (obj != null) {
                            if (obj instanceof String) {
                                exist = !((String) obj).equals("");
                            } else if (obj instanceof Integer) {
                                exist = true;
                            }
                            if (exist) {
                                if (m.getValue() == 99) { //是多条的话记录外键并且不计分
                                    multiFlag = true;
                                    foreignKey = Strings.replace(m.getKey(), srcServCode + servCode, "");
                                    continue;
                                }
                                deg += m.getValue();
                            }
                        }
                    }
                    String dataId = multiFlag ? dataBean.getStr(foreignKey) : dataBean.getId();
                    if (dataId.equals("")) {
                        continue;
                    }
                    //处理批量删除的情况
                    if (act.equals("delete")) {
                        ParamBean query = new ParamBean();
                        ServDefBean serv = ServUtils.getServDef(servCode);
                        query.setServId(servCode).setAct("count").setWhere(serv.getServDefWhere());
                        int leaveSize = ServMgr.act(query).getInt("_OKCOUNT_");
                        if (leaveSize == 0) { //置零
                            Bean setBean = new Bean().set("CMLE_DEG", 0);
                            String where = " and SERV_CODE = '" + servCode + "' and DATA_ID = '" + dataId + "'";
                            Bean whereBean = new Bean().set(Constant.PARAM_WHERE, where);
                            ServDao.updates("SY_COMM_COMPLETE_DATA", setBean, whereBean);
                            if (srcServCode.equals(USERINFOSRCSERV)) {
                                setUserInfoDeg(dataId);
                            }
                            continue;
                        } else {
                            break;
                        }
                    }
                    
                    //存储
                    ParamBean param = new ParamBean();
                    param.set("SERV_CODE", servCode);
                    param.set("DATA_ID", dataId);
                    if (ServDao.count("SY_COMM_COMPLETE_DATA", param) > 0) { //修改
                        Bean setBean = new Bean().set("CMLE_DEG", deg);
                        String where = " and SERV_CODE = '" + servCode + "' and DATA_ID = '" + dataId + "'";
                        Bean whereBean = new Bean().set(Constant.PARAM_WHERE, where);
                        ServDao.updates("SY_COMM_COMPLETE_DATA", setBean, whereBean);
                    } else { //添加
                        param.set("SRC_SERV_CODE", srcServCode);
                        param.set("CMLE_DEG", deg);
                        //param.set("ID", Lang.getUUID());
                        ServDao.save("SY_COMM_COMPLETE_DATA", param);
                    }
                    if (srcServCode.equals(USERINFOSRCSERV)) {
                        setUserInfoDeg(dataId);
                    }
                }
            }
        }
    }
    
    /**
     * 计算用户完整度并同步userstate中的数据
     * @param dataId 数据主键
     * 
     */
    @SuppressWarnings("unchecked")
    private static void setUserInfoDeg(String dataId) {
        Map<String, Integer> totalWeightHash = (Map<String, Integer>) CacheMgr.getInstance().get("totalWeightHash",
                COMPLETESETTING);
        SqlBean query = new SqlBean();
        query.selects("sum(CMLE_DEG) as DEG").and("SRC_SERV_CODE", USERINFOSRCSERV).and("DATA_ID", dataId);    
        Bean res = ServDao.find("SY_COMM_COMPLETE_DATA", query);
        int upDeg = res.getInt("DEG");
        int downDeg = totalWeightHash.get(USERINFOSRCSERV);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(0);
        df.setMinimumFractionDigits(0);
        int deg = (int) (upDeg * 100.00 / downDeg);
        UserBean userState = UserMgr.getUser(dataId);
        userState.set("USER_CMLE_DEG", deg);
        UserMgr.saveUserState(userState);
    }
    
    /**
     * 获取完成度
     * @param paramBean 入参
     * @return OutBean 出参
     */
    @SuppressWarnings("unchecked")
    public OutBean getDeg(ParamBean paramBean) {
        Map<String, Integer> totalWeightHash = (Map<String, Integer>) CacheMgr.getInstance().get("totalWeightHash",
                COMPLETESETTING);
        if (totalWeightHash == null) {
            totalWeightHash = new HashMap<String, Integer>();
            CacheMgr.getInstance().set("totalWeightHash", totalWeightHash, COMPLETESETTING);
        }
        String srcServCode = paramBean.getStr("SRC_SERV_CODE");
        String dataId = paramBean.getStr("DATA_ID");
        int deg;
        if (totalWeightHash.containsKey(srcServCode)) {
            SqlBean query = new SqlBean();
            query.selects("sum(CMLE_DEG) as DEG").and("SRC_SERV_CODE", srcServCode).and("DATA_ID", dataId);    
            Bean res = ServDao.find("SY_COMM_COMPLETE_DATA", query);
            int upDeg = res.getInt("DEG");
            int downDeg = totalWeightHash.get(srcServCode);
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(0);
            df.setMinimumFractionDigits(0);
            deg = (int) (upDeg * 100.00 / downDeg);
        } else {
            deg = 100;
        }
        return new OutBean().set("deg", deg);
    }
}


