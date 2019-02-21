package com.rh.core.comm.mind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;

/**
 * 用于获取指定用户可以查看的审批单的意见
 * 
 * @author wanglong
 */
public class UserMind {

    /**
     * 部门内可见
     */
    public static final int DISPLAY_RULE_DEPT = 1;

    /**
     * 机构内可见
     */
    public static final int DISPLAY_RULE_ORG = 2;

    /**
     * 是否可以完全公开
     */
    public static final int DISPLAY_RULE_ALL = 3;

    /**
     * 本机构及以上机构可见
     */
    public static final int DISPLAY_RULE_PARENT = 4;

    /**
     * 意见类型数据字典
     */
    public static final String DICT_MIND_TYPE = "SY_COMM_MIND_TYPE";

    /**
     * 完整地意见列表
     */
    private List<Bean> mindList = null;

    private UserBean viewUser = null;

    /**
     * @param userBean 意见查看人
     */
    public UserMind(UserBean userBean) {
        this.viewUser = userBean;
    }

    /**
     * 查询符合条件的意见
     * 
     * @param servId 服务ID
     * @param dataId 审批单ID
     */
    public void query(String servId, String dataId) {
        mindList = MindUtils.getMindList(servId, dataId, "");
    }

    /**
     * 查询符合条件的意见
     * 
     * @param servId 服务ID
     * @param dataId 审批单ID
     * @param sortType 排序类型
     */
    public void query(String servId, String dataId, String sortType) {
        mindList = MindUtils.getMindList(servId, dataId, sortType);
    }

    /**
     * 取得指定意见类型的意见
     * 
     * @param type 意见编码
     * @return 符合条件的意见列表
     */
    public List<Bean> getMindListByType(String type) {
        List<Bean> rtnList = new ArrayList<Bean>();
        type = type + "-";
        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);
            String mindCode = mindBean.getStr("MIND_CODE");
            if (mindCode.startsWith(type) && canView(mindBean)) {
                appendFileID(mindBean);
                rtnList.add(mindBean);
            }
        }

        return rtnList;
    }

    /**
     * 取得指定意见类型的意见
     * 
     * @param type 意见编码
     * @param odeptCode 意见编码
     * @return 符合条件的意见列表
     */
    public List<Bean> getMindListByType(String type, String odeptCode) {
        List<Bean> rtnList = new ArrayList<Bean>();
        type = type + "-";
        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);
            if (odeptCode.equals(mindBean.getStr("S_ODEPT"))) {
                String mindCode = mindBean.getStr("MIND_CODE");
                if (mindCode.startsWith(type) && canView(mindBean)) {
                    appendFileID(mindBean);
                    rtnList.add(mindBean);
                }
            }
        }

        return rtnList;
    }

    /**
     * 
     * @param mindCode 意见编码
     * @return 根据意见编码 获取意见的列表
     */
    public List<Bean> getMindListByMindCode(String mindCode) {
        List<Bean> rtnList = new ArrayList<Bean>();
        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);
            if (mindCode.equals(mindBean.getStr("MIND_CODE")) && canView(mindBean)) {
                appendFileID(mindBean);
                rtnList.add(mindBean);
            }
        }

        return rtnList;
    }

    /**
     * 根据意见编码和组织编码获取意见列表
     * 
     * @param mindCode 意见编码
     * @param odeptCode 机构编码
     * @return 意见Bean的List列表
     */
    public List<Bean> getMindListByMindCode(String mindCode, String odeptCode) {
        List<Bean> rtnList = new ArrayList<Bean>();
        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);
            if (odeptCode.equals(mindBean.getStr("S_ODEPT"))) {
                if (mindCode.equals(mindBean.getStr("MIND_CODE")) && canView(mindBean)) {
                    appendFileID(mindBean);
                    rtnList.add(mindBean);
                }
            }
        }

        return rtnList;
    }

    /**
     * 按照意见编码顺序显示意见
     * @param sCmpy 公司编码
     * @return 取得所有意见编码
     */
    public List<Bean> getMindCodeList(String sCmpy) {
        Bean queryBean = new Bean();

        if (sCmpy.length() > 0) {
            String strWhere = " and S_CMPY = '" + sCmpy + "'";
            queryBean.set(Constant.PARAM_WHERE, strWhere);
        }

        queryBean.set(Constant.PARAM_ORDER, "CODE_SORT ASC");

        return ServDao.finds("SY_COMM_MIND_CODE", queryBean);
    }

    /**
     * 解析MIND_FILE字段，根据逗号分隔成MIND_FILE_ID和MIND_FILE_NAME属性。
     * @param mindBean 意见Bean
     */
    public static void appendFileID(Bean mindBean) {
        if (!mindBean.isEmpty("MIND_FILE")) {
            String mindFile = mindBean.getStr("MIND_FILE");
            mindBean.put("_MIND_FILE_LIST", parseFileInfo(mindFile));
        }
    }

    /**
     * 
     * @param fileInfo 文件信息字符串
     * @return 解析文件信息结果
     */
    private static List<Bean> parseFileInfo(String fileInfo) {
        List<Bean> list = new ArrayList<Bean>();
        String[] files = fileInfo.split(";");
        for (String strFile : files) {
            String[] file = strFile.split(",");
            if (file.length == 2 && StringUtils.isNotEmpty(file[0])
                    && StringUtils.isNotEmpty(file[1])) {
                Bean bean = new Bean();
                bean.put("FILE_ID", file[0]);
                bean.put("FILE_NAME", file[1]);
                list.add(bean);
            }
        }

        return list;
    }

    /**
     * MIND_DIS_RULE:意见显示规则：1,部门内可见,2,机构内可见,3,机构外可见
     * 
     * @param mindBean 意见记录Bean
     * @return 是否能查看此条意见
     */
    private boolean canView(Bean mindBean) {
        int disRule = mindBean.getInt("MIND_DIS_RULE");
        if (disRule == DISPLAY_RULE_DEPT) {
            String tDeptCode = mindBean.getStr("S_TDEPT");
            if (tDeptCode.equals(viewUser.getTDeptCode())) {
                return true;
            }
        } else if (disRule == DISPLAY_RULE_ORG) {
            String sDeptCode = mindBean.getStr("S_ODEPT");
            if (sDeptCode.equals(viewUser.getODeptCode())) {
                return true;
            }
        } else if (disRule == DISPLAY_RULE_PARENT) {
            String sDeptCode = mindBean.getStr("S_ODEPT");
            // 意见的机构与意见查看人是同一个机构
            if (sDeptCode.equals(viewUser.getODeptCode())) {
                return true;
            }

            DeptBean deptBean = OrgMgr.getDept(sDeptCode);
            String mindOdeptPath = "";
            if (deptBean != null) {
                mindOdeptPath = deptBean.getCodePath();
            }
            String userOdeptPath = viewUser.getODeptCodePath();

            /** 如果意见的机构Path包含当前用户的机构Path，则表示当前用户为上级机构 **/
            if (mindOdeptPath.startsWith(userOdeptPath)) {
                return true;
            }

        } else if (disRule == DISPLAY_RULE_ALL) {
            return true;
        }

        return false;
    }

    /**
     * @return 取得所有能包含的意见类型
     */
    public List<Bean> getMindTypeList() {
        return getMindTypeList("");
    }

    /**
     * @param odeptCode 机构编码
     * @return 取得所有能包含的意见类型
     */
    public List<Bean> getMindTypeList(String odeptCode) {
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < mindList.size(); i++) {
            Bean mind = mindList.get(i);

            /** 如果有机构值就过滤 给定机构值的意见列表 */
            if (odeptCode.equals(mind.getStr("S_ODEPT")) || odeptCode.length() == 0) {
                String mindCode = mind.getStr("MIND_CODE");
                int pos = mindCode.indexOf("-");
                if (pos > 0) {
                    String type = mindCode.substring(0, pos);
                    if (!types.contains(type)) {
                        types.add(type);
                    }
                }
            }
        }

        List<Bean> list = DictMgr.getItemList(DICT_MIND_TYPE);
        List<Bean> result = new ArrayList<Bean>();

        for (Bean bean : list) {
            String itemCode = bean.getStr("ID");
            if (types.contains(itemCode)) {
                result.add(bean);
            }
        }

        return result;
    }

    /**
     * @return 获取机构的列表 , 按照 dept_sort 顺序 排列
     */
    public List<Bean> getOdeptList() {
        List<Bean> deptList = new ArrayList<Bean>();

        HashMap<String, Bean> odepts = new HashMap<String, Bean>();
        for (int i = 0; i < mindList.size(); i++) {
            Bean mind = mindList.get(i);

            if (canView(mind)) {
                String odeptCode = mind.getStr("S_ODEPT");

                DeptBean deptBean = OrgMgr.getDept(odeptCode);

                if (!odepts.containsKey(odeptCode)) {
                    odepts.put(odeptCode, deptBean);
                    deptList.add(deptBean);
                }
            }
        }

        if (deptList.size() <= 1) { // 小于一个机构， 不用排序了
            return deptList;
        }

        BeanUtils.sort(deptList, "DEPT_LEVEL");

        return deptList;
    }

    /**
     * 
     * @return 返回意见条目的数量
     */
    public int getMindCount() {
        return this.mindList.size();
    }

    /**
     * 
     * @return 意见列表
     */
    public List<Bean> getMindList() {
        List<Bean> rtnList = new ArrayList<Bean>();
        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);
            String deptFullName = DictMgr.getFullNames("SY_ORG_DEPT_SUB", mindBean.getStr("S_DEPT"));
            mindBean.set("DEPT_FULL_NAME", deptFullName);
            if (canView(mindBean)) {
                appendFileID(mindBean);
                rtnList.add(mindBean);
            }
        }

        return rtnList;
    }

    /**
     * @param odeptCode 机构编码
     * @return 意见列表
     */
    public List<Bean> getMindList(String odeptCode) {
        List<Bean> rtnList = new ArrayList<Bean>();
        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);
            if (mindBean.getStr("S_ODEPT").equals(odeptCode)) {
                if (canView(mindBean)) {
                    appendFileID(mindBean);
                    rtnList.add(mindBean);
                }
            }
        }

        return rtnList;
    }

    /**
     * 
     * @return 所有的意见编码
     */
    public HashSet<String> getMindCodeList() {
        HashSet<String> mindCodeset = new HashSet<String>();

        for (int i = 0; i < mindList.size(); i++) {
            Bean mindBean = mindList.get(i);

            String mindCode = mindBean.getStr("MIND_CODE");
            if (!mindCodeset.contains(mindCode)) {
                mindCodeset.add(mindCode);
            }
        }

        return mindCodeset;
    }

    /**
     * 
     * @return 意见查看用户对象
     */
    public UserBean getViewUser() {
        return viewUser;
    }

    /**
     * 获取所有意见编码下的意见
     * @param code 意见编码
     * @return 意见字符串多个意见用逗号分割
     */
    public String getPrintData(String code) {
        StringBuilder sb = new StringBuilder();
        List<Bean> minds = this.getMindListByMindCode(code);
        int size = minds.size();
        String comm = "<div>";
        String endcomm = "</div>";
        String commbr = "</br>";
        sb.append(comm);
        for (Bean bean : minds) {
            String strbegin = "<p style='line-height:18px;'>";
            String strend = "</p>"; // 两条以上需要添加换行
            sb.append(strbegin).append(bean.getStr("MIND_CONTENT"))
                    .append(",");
            if (bean.isNotEmpty("BD_UNAME")) {
                sb.append(bean.getStr("BD_UNAME"));
                sb.append("(").append(bean.getStr("S_UNAME"));
                sb.append("授权)");
            } else {
                sb.append(bean.get("S_UNAME"));
            }
            sb.append("(").append(bean.get("MIND_TIME"))
                    .append(")").append(strend);
            if (size > 1) {
                sb.append(commbr);
            }
            size--;
        }
        sb.append(endcomm);
        return sb.toString();
    }
   /**
    * 获取不同机构的的意见
    * @param code 意见编码
    * @param odeptLevel 机构级别
    * @return 意见列表
    */
    public List<Bean> getMindListByOdeptLevel(String code, int odeptLevel) {
        List<Bean> result = new ArrayList<Bean>();
        List<Bean> minds = this.getMindListByMindCode(code);
        for (Bean mind : minds) {
            String odeptCode = mind.getStr("S_ODEPT");
            DeptBean deptBean = OrgMgr.getDept(odeptCode);
            if (deptBean.getLevel() == odeptLevel) {
                result.add(mind);
            }
        }

        return result;
    }
   /**
    *  获取意见不同级别机构的意见
    * @param code 意见编码
    * @param odeptLevel 机构级别
    * @return 意见字符串多个以逗号分割
    */
    public String getPrintData(String code, int odeptLevel) {
        StringBuilder sb = new StringBuilder();
        List<Bean> minds = this.getMindListByOdeptLevel(code, odeptLevel);
        int size = minds.size();
        String comm = "<div>";
        String endcomm = "</div>";
        String commbr = "</br>";
        sb.append(comm);
        for (Bean bean : minds) {
            String strbegin = "<p style='line-height:18px;'>";
            String strend = "</p>"; // 两条以上需要添加换行
            sb.append(strbegin).append(bean.getStr("MIND_CONTENT"))
                    .append(",").append(bean.get("S_UNAME"))
                    .append("(").append(bean.get("MIND_TIME"))
                    .append(")").append(strend);
            if (size > 1) {
                sb.append(commbr);
            }
            size--;
        }
        sb.append(endcomm);
        return sb.toString();
    }
}
