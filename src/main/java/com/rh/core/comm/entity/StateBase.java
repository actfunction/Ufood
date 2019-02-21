package com.rh.core.comm.entity;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 查询 在办 /已办 / 主办的公用类
 * @author wanglong
 *
 */
public class StateBase extends CommonServ {
    /** 按个人查询 */
    public static final int QUERY_PERSONAL = 1;
    
    /** 按部门查询 */
    public static final int QUERY_DEPT = 2;
    
    /** like字符标识 相对于%符合 */
    public static final String SYMBOL_LIKE = "^";
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 菜单上设置的服务参数
     */
    protected String getServId(ParamBean paramBean) {
        Bean urlBean = getUrlParam(paramBean);
        if (null != urlBean && !urlBean.isEmpty("SERV_ID")) {
            return urlBean.getStr("SERV_ID");
        }
        
        return "";    
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 菜单上设置的服务参数
     */
    protected String getTodoCode(ParamBean paramBean) {
        Bean urlBean = getUrlParam(paramBean);
        if (null != urlBean && !urlBean.isEmpty("TODO_CODE")) {
            return urlBean.getStr("TODO_CODE");
        }
        
        return "";    
    }
    
    /**
     * 
     * @param servId 获取到的服务串
     * @return 是否包含like字符串
     */
    protected boolean containsLikeSymbol(String servId) {
        return servId.contains(SYMBOL_LIKE);
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 有关服务的过滤条件
     */
    protected String getServIdWhere(ParamBean paramBean) {
        String fieldCode = "SERV_ID";
        String servStr = getServId(paramBean).trim();
        if (servStr.length() == 0) {
            return "";
        }
        
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and ");
        if (servStr.contains(",")) { //是否包含逗号，有逗号表示是多个ServId
            if (servStr.contains(SYMBOL_LIKE)) { // 包含like标识符，则使用or
                String[] servIds = servStr.split(",");
                strWhere.append("(");
                for (int i = 0; i < servIds.length; i++) { // 处理多个ServId
                    String servId = servIds[i].trim();
                    if (i > 0) {
                        strWhere.append(" or ");
                    }
                    strWhere.append(appendServIdWhere(servId, fieldCode));
                }
                strWhere.append(")");
            } else { //多个完全匹配的ServId，使用in语句。
                strWhere.append(" " + fieldCode + " in ('");
                strWhere.append(servStr.replaceAll(",", "','"));
                strWhere.append("')");
            }
        } else {
            String servId = servStr.trim();
            strWhere.append(appendServIdWhere(servId, fieldCode));
        }
        
        return strWhere.toString();
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 有关待办类型的过滤条件
     */
    protected String getTodoCodeWhere(ParamBean paramBean) {
        String fieldCode = "TODO_CODE";
        String todoStr = getTodoCode(paramBean).trim();
        if (todoStr.length() == 0) {
            return "";
        }
        
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and ");
        if (todoStr.contains(",")) { //是否包含逗号，有逗号表示是多个todoCode
            if (todoStr.contains(SYMBOL_LIKE)) { // 包含like标识符，则使用or
                String[] todoCodes = todoStr.split(",");
                strWhere.append("(");
                for (int i = 0; i < todoCodes.length; i++) { // 处理多个todoCode
                    String todoCode = todoCodes[i].trim();
                    if (i > 0) {
                        strWhere.append(" or ");
                    }
                    strWhere.append(appendServIdWhere(todoCode, fieldCode));
                }
                strWhere.append(")");
            } else { //多个完全匹配的todoCode，使用in语句。
                strWhere.append(" " + fieldCode + " in ('");
                strWhere.append(todoStr.replaceAll(",", "','"));
                strWhere.append("')");
            }
        } else {
            String todoCode = todoStr.trim();
            strWhere.append(appendServIdWhere(todoCode, fieldCode));
        }
        
        return strWhere.toString();
    }
    
    /**
     * 
     * @param servId 服务ID
     * @param fieldCode 字段值
     * @return 对服务字段的查询语句。如果有Like标识符，则用like语句，否则用等于语句
     */
    private String appendServIdWhere(String servId, String fieldCode) {
        StringBuilder strWhere = new StringBuilder();
        if (containsLikeSymbol(servId)) {
            String newStr = servId.replaceAll("\\" + SYMBOL_LIKE, "%");

            strWhere.append(" " + fieldCode + " like '");
            strWhere.append(newStr);
            strWhere.append("'");
        } else {
            strWhere.append(" " + fieldCode + " = '");
            strWhere.append(servId);
            strWhere.append("'");
        }

        return strWhere.toString();
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 查询个人/查询本部门
     */
    protected int getQueryType(Bean paramBean) {
        Bean urlBean = getUrlParam(paramBean);
        if (null != urlBean && !urlBean.isEmpty("QUERY_TYPE")) {
            int queryType = urlBean.getInt("QUERY_TYPE");
            
            if (queryType == QUERY_PERSONAL) {
                return QUERY_PERSONAL;    
            } else {
                return QUERY_DEPT;
            }
        }
        
        return QUERY_PERSONAL;    
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return 已办结/未办结
     */
    protected int getStatusType(Bean paramBean) {
        Bean urlBean = getUrlParam(paramBean);
        if (null != urlBean && !urlBean.isEmpty("STATUS_TYPE")) {
            int statusType = urlBean.getInt("STATUS_TYPE");
            
            if (statusType == WfeConstant.WFE_PROC_INST_IS_RUNNING) {
                return WfeConstant.WFE_PROC_INST_IS_RUNNING;    
            } else {
                return WfeConstant.WFE_PROC_INST_NOT_RUNNING;
            }
        }
        
        return WfeConstant.WFE_PROC_INST_IS_RUNNING;    
    }
    
    /**
     * 
     * @param paramBean 参数Bean
     * @return Url中所含的参数
     */
    private Bean getUrlParam(Bean paramBean) {
        if (!paramBean.isEmpty("_extWhere")) {
            String extWhereStr = paramBean.getStr("_extWhere");
            Bean param = JsonUtils.toBean(extWhereStr);
            
            return param;
        }
        return null;
    }
}
