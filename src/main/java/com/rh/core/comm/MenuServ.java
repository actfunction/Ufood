package com.rh.core.comm;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.auth.acl.mgr.AclMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.RequestUtils;
import com.rh.core.util.file.FileHelper;

/**
 * 菜单服务类
 * 
 * @author wanglong
 * 
 */
public class MenuServ extends CommonServ {
    /** 菜单字典编码 */
    public static final String DICT_SERV_MENU = "SY_COMM_MENU";
    /** 菜单用户字典编码 */
    public static final String DICT_SERV_MENU_USER = "SY_COMM_MENU_USER";

    /** 左菜单 */
    public static final String LEFTMENU = "LEFTMENU";
    /** 上菜单 */
    public static final String TOPMENU = "TOPMENU";

    /** 范围 */
    private static final String MENU_SCOPE_DEF = "MENU_SCOPE_DEF";
    
    /** 菜单文件路径 */
    private static final String MENU_DIR = "SY_COMM_MENU/";
    /** Log */
    private static Log log = LogFactory.getLog(MenuServ.class);
    
    /** 菜单文件存储路径配置：单服务模式不用设置，集群模式必须设置，单机模式的默认存储路径为pro/WEB-INF/doc **/
    private static final String CONF_MENU_OBJ_PATH = "MENU_OBJ_PATH";

    @Override
    protected void beforeSave(ParamBean paramBean) {
        if (paramBean.contains("MENU_ID")) { //有menu ID，判断是否修改了menu ID
            String newId = paramBean.getStr("MENU_ID");
            String oldId = paramBean.getId();
            if (oldId.length() > 0 && newId.length() > 0 && !newId.equals(oldId)) { //修改了自动判断是否有公司编码作为后缀
                String ends = "__" + Context.getCmpy();
                if (!newId.endsWith(ends)) {
                    paramBean.set("MENU_ID", newId + ends);
                }
            }
        }
        
        if (paramBean.isNotEmpty(MENU_SCOPE_DEF)) { // 根据前台数据，合并角色显示范围
            String scopeDef = paramBean.getStr(MENU_SCOPE_DEF);
            String[] scopes = scopeDef.split(",");

            int scopeVal = 0;

            for (String scope : scopes) {
                scopeVal += Integer.parseInt(scope);
            }

            if (scopeVal > 511) {
                scopeVal = 511;
            }

            paramBean.set("MENU_SCOPE", scopeVal);
        }
    }
    
    @Override
    protected void beforeQuery(ParamBean paramBean) {
        super.beforeQuery(paramBean);

        UserBean user = Context.getUserBean();

        int level = user.getODeptLevel();
        if (level >= 1) {
            level = level - 1;
        }
        
        //增加按照级别过滤角色的代码，暂时只支持ORACLE
        //2018年10月17日  修改后已支持神通数据库
        StringBuilder where = new StringBuilder();
        where.append(" and ")
            .append(Transaction.getBuilder().bitand("MENU_SCOPE", String.valueOf((int) Math.pow(2, level))))
            .append(" > 0");

        paramBean.setQueryExtWhere(where.toString());
    }

    @Override
    protected void afterByid(ParamBean paramBean, OutBean outBean) {
        super.afterByid(paramBean, outBean);

        int scope = outBean.getInt("MENU_SCOPE");
        if (scope > 0) { // 根据合并的数据拆分出可供多选框反选的值。
            String scopeDef = "0";

            for (int i = 0; i < 10; i++) {
                int pos = (int) Math.pow(2, i);
                if ((scope & pos) > 0) {
                    scopeDef += "," + pos;
                }
            }

            outBean.set(MENU_SCOPE_DEF, scopeDef);
        }
    }
    
    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk()) {
            Bean fullBean = paramBean.getSaveFullData();
            UserMgr.clearMenuByCmpy(fullBean.getStr("S_CMPY"));  //保存菜单后清除用户菜单时间
        }
    }

    @Override
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        List<Bean> okList = outBean.getDataList();
        if (okList.size() > 0) { // 清除用户菜单时间
            Bean menu = okList.get(0);
            UserMgr.clearMenuByCmpy(menu.getStr("S_CMPY"));  //删除菜单后清除缓存的操作
        }
    }

    /**
     * 将菜单对象持久化，并放置到缓存中
     * 
     * @param userCode 用户编码
     * @return 菜单列表
     */
    public static List<Bean> menuToFile(String userCode) {
        List<Bean> menuTree;
        
        //从系统配置中获取 实现类  eg,PT_SY_COMM_MENU_USER,com.rh.pt.util.PtMenuFilter
        //PT_SY_COMM_MENU_USER是用户菜单字典，com.rh.pt.util.PtMenuFilter是菜单的过滤实现类
        String extClass = Context.getSyConf("SY_MENU_FLITER", "");
        
        if (StringUtils.isNotEmpty(extClass)) { //配置了扩展类  字典,扩展类
            String[] configArr = extClass.split(",");
            
            menuTree = DictMgr.getTreeList(configArr[0], 0);
            
            MenuFilter menuFilter = null;
            try {
                menuFilter = (MenuFilter) Lang.loadClass(configArr[1]).newInstance();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            menuTree = menuFilter.filterMenu(menuTree);
        } else {
            menuTree = DictMgr.getTreeList(DICT_SERV_MENU_USER, 0);
        }

        FileHelper.ser(menuTree, getMenuFullPath(userCode));
        UserMgr.setCacheMenuList(userCode, menuTree);        
        return menuTree;
    }

    /**
     * 将菜单对象从文件反持久化，如果不存在则返回null
     * 
     * @param userCode 用户编码
     * @return 菜单列表
     */
    @SuppressWarnings("unchecked")
    public static List<Bean> menuFromFile(String userCode) {
        try {
            String path = getMenuFullPath(userCode);
            if (FileStorage.exists(path)) {
                return (List<Bean>) FileHelper.dser(path);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将菜单定义导出<br>
     * 导出的格式为：zip包，里面为一个包含各个菜单定义的json文件<br>
     * @param paramBean 参数，用menuIds表示要导出的菜单的menu_id
     * @return Bean
     */
    public OutBean expMenuDef(ParamBean paramBean) {
        String menuDefIds = paramBean.getStr("menuIds");   //要导出的菜单定义ID
        boolean expAcls = paramBean.getBoolean("expAcl");  //是否导出菜单权限
        
        if (menuDefIds.indexOf(Constant.SEPARATOR) > -1) {  //导出多个菜单定义
            menuDefIds = menuDefIds.replaceAll(Constant.SEPARATOR, "'" + Constant.SEPARATOR + "'");
        }
        Bean tempBean = new Bean();
        tempBean.set(Constant.PARAM_WHERE, "AND MENU_ID IN ('" + menuDefIds + "')");
        List<Bean> tempList = ServDao.finds(ServMgr.SY_COMM_MENU, tempBean);  //查询要导出的菜单定义
        Bean queryBean = new Bean();
        
        List<Bean> allDataList = new ArrayList<Bean>();
        for (Bean bean : tempList) {  //遍历要导出的菜单定义，查询其本身及其子菜单
            queryBean.set(Constant.PARAM_WHERE, " AND CODE_PATH LIKE '" + bean.getStr("CODE_PATH") + "%'");
            queryBean.set(Constant.PARAM_ORDER, "MENU_LEVEL, MENU_ORDER ASC");
            List<Bean> menuBeanList = ServDao.finds(ServMgr.SY_COMM_MENU, queryBean);
            
            allDataList.addAll(menuBeanList);
        }
        
        //查询所有菜单的权限
        List<Bean> menuAclList = new ArrayList<Bean>();
        if (expAcls) {
            for (Bean menuBean: allDataList) {
                SqlBean sql = new SqlBean();
                sql.and("SERV_ID", menuBean.getStr("MENU_ID"));
                sql.selects("SERV_ID,ACT_CODE,ACL_TYPE,ACL_OWNER,ACL_OTYPE");
                
                List<Bean> aclList = ServDao.finds(ServMgr.SY_ORG_ACL, sql);
                menuAclList.addAll(aclList);
            }
        }
        
        HttpServletRequest request = Context.getRequest();
        HttpServletResponse response = Context.getResponse();
        response.setContentType("application/x-download");
        RequestUtils.setDownFileName(request, response, ServMgr.SY_COMM_MENU + ".zip");  //指定导出格式及名字
        
        ZipOutputStream zipOut = null; //输出流
        InputStream is = null;  //输入流
        InputStream aclIs = null;
        try {
            zipOut = new ZipOutputStream(response.getOutputStream());
            is = IOUtils.toInputStream(JsonUtils.toJson(allDataList, false), Constant.ENCODING); //将菜单定义转为输入流
            zipOut.putNextEntry(new ZipEntry("SY_MENU_DATA.json"));  //指定输出文件名称
            IOUtils.copyLarge(is, zipOut);
            if (expAcls) {
                aclIs = IOUtils.toInputStream(JsonUtils.toJson(menuAclList, false), Constant.ENCODING); 
                zipOut.putNextEntry(new ZipEntry("SY_MENU_ACL.json"));  //指定输出文件名称
                IOUtils.copyLarge(aclIs, zipOut);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);  //关闭输入流
            IOUtils.closeQuietly(aclIs);
            IOUtils.closeQuietly(zipOut); //关闭输出流
            try {
                response.flushBuffer();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return new OutBean();
    }

    /**
     * 导入菜单定义 如果系统中已经有此菜单定义，则不导入
     * @param paramBean 要导入的文件的fileId
     * @return Bean 
     */
    public OutBean impMenu(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String fileId = paramBean.getStr("fileId");
        Bean fileBean = FileMgr.getFile(fileId);
        int count = 0;
        if (fileBean != null && fileBean.getStr("FILE_MTYPE").equals("application/zip")) {
            ZipInputStream zipIn = null;
            InputStream in = null;
            List<Bean> menuAclList = null;
            
            try {
                zipIn = new ZipInputStream(FileMgr.download(fileBean));
                while (true) {
                    ZipEntry entry = zipIn.getNextEntry();
                    if (null == entry) {
                        break;
                    }

                    if (entry.getName().equals("SY_MENU_ACL.json")) {
                        in = new BufferedInputStream(zipIn);
                        menuAclList = FileHelper.listFromJsonFile(in);
                        break;
                    }
                }                
                
                
                //处理菜单定义信息
                zipIn = new ZipInputStream(FileMgr.download(fileBean));
                
                while (true) {
                    
                    ZipEntry entry = zipIn.getNextEntry();
                    
                    if (null == entry) {
                        break;
                    }

                    if (entry.getName().equals("SY_MENU_DATA.json")) {
                        in = new BufferedInputStream(zipIn);
                        List<Bean> menuDataList = FileHelper.listFromJsonFile(in);
                        
                        count = MenuServ.importMenu(menuDataList, menuAclList, 
                                paramBean.getServId(), paramBean.getStr("MENU_PID"));
                        
                        break;
                    }
                }
                
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                if (zipIn != null) {
                    IOUtils.closeQuietly(zipIn);
                }
            }
        }
        if (count > 0) {
            outBean.setOk(count + "个定义导入成功！");
            FileMgr.deleteFile(fileBean);
        } else {
            outBean.setError();
        }
        return outBean;
    }

    /**
     * 将文件流的内容导入为菜单定义并保存
     * @param menuList 菜单列表
     * @param menuAclList 菜单权限列表
     * @param servId 服务编码
     * @param pId 父菜单编码
     * @return Bean 
     */
    public static int importMenu(List<Bean> menuList, List<Bean> menuAclList
            , String servId, String pId) {
        boolean pubFlag = servId.indexOf("PUBLIC") > 0 ? true : false; //公共服务标志为true 
        int count = 0;
        boolean impAcl = true;
        if (null == menuAclList) {
            impAcl = false;
        }
        
        HashMap<String, List<Bean>> aclMap = new HashMap<String, List<Bean>>();
        if (impAcl) {
            aclMap = getAclMap(menuAclList);    
        }
        
        Bean pBean = new Bean();
        List<Bean> rootList = new ArrayList<Bean>();
        for (Bean menuBean : menuList) { // 遍历需导入的菜单
            String oldId = menuBean.getId();
            String newId = Lang.getUUID();
            pBean.set(menuBean.getId(), newId);
            menuBean.set("MENU_ID", newId);
            if (menuBean.isNotEmpty("MENU_PID")) { //存在父ID
                menuBean.set("MENU_PID", pBean.getStr(menuBean.getStr("MENU_PID"))); //采用新的父ID
            }
            if (pubFlag) { //公共菜单服务导入的设定公共标志为1
                menuBean.set("S_PUBLIC", Constant.YES_INT);
            } else {
                menuBean.set("S_PUBLIC", Constant.NO_INT);
            }
            menuBean.set("S_CMPY", Context.getCmpy()).set("S_MTIME", DateUtils.getDatetimeTS());
            if (menuBean.isEmpty("MENU_PID")) {
                rootList.add(menuBean);
                menuBean.set("MENU_PID", "tempMenuPID");
            }
            
            List<Bean> acls = aclMap.get(oldId);
            if (null != acls && acls.size() > 0) { //menuId 即 sy_org_acl中的serv_id
                for (Bean aclBean: acls) {
                    
                    aclBean.set("SERV_ID", newId); //菜单ID
                    
                    aclBean.set("S_CMPY", Context.getCmpy()); //导入到的新的公司的ID
                    aclBean.setId("");
                    aclBean.set("ACL_ID", "");
                    
                    ServDao.create(ServMgr.SY_ORG_ACL, aclBean);                    
                    
                    //清除涉及用户的缓存
                    clearMenuTime(aclBean);
                }
            }
        }
        count = ServDao.creates(ServMgr.SY_COMM_MENU, menuList); //先批量插入数据库
        for (Bean menuBean : rootList) { ////第一级菜单修改PID确保LEVEL和CODE_PATH计算正确
            ParamBean data = new ParamBean(servId, ServMgr.ACT_SAVE);
            data.setId(menuBean.getStr("MENU_ID")).set("MENU_PID", pId);
            ServMgr.act(data);
        }
        return count;
    }
    

    /**
     * 
     * @param aclBean 菜单权限Bean
     */
    private static void clearMenuTime(Bean aclBean) {
        int otype = aclBean.get("ACL_OTYPE", AclMgr.ACL_OTYPE_ROLE);
        String owner = aclBean.getStr("ACL_OWNER");
        if (otype == AclMgr.ACL_OTYPE_ROLE) {
            String publicRole = Context.getSyConf("SY_ORG_ROLE_PUBLIC", "RPUB");
            if (owner.startsWith(publicRole)) { // 系统设定的公共角色，清除全部用户的菜单时间
                UserMgr.clearMenuByCmpy(Context.getCmpy());
            } else {
                UserMgr.clearMenuByRole(owner, Context.getCmpy());
            }
        } else if (otype == AclMgr.ACL_OTYPE_DEPT) {
            UserMgr.clearMenuByDept(owner);
        } else if (otype == AclMgr.ACL_OTYPE_USER) {
            UserMgr.clearMenuByUsers(owner);
        }
    }
    
    /**
     * 
     * @param aclList 菜单权限列表
     * @return 菜单权限Map
     */
    private static HashMap<String, List<Bean>> getAclMap(List<Bean> aclList) {
        
        HashMap<String, List<Bean>> aclMap = new HashMap<String, List<Bean>>();
        for (Bean aclBean: aclList) {
            if (null == aclMap.get(aclBean.getStr("SERV_ID"))) {
                List<Bean> acls = new ArrayList<Bean>();
                acls.add(aclBean);
                
                aclMap.put(aclBean.getStr("SERV_ID"), acls);
            } else {
                aclMap.get(aclBean.getStr("SERV_ID")).add(aclBean);
            }
        }
        
        return aclMap;
    }
    
    
    
    /**
     * 根据菜单编码获取菜单
     * @param paramBean 参数
     * @return tree
     */
    public OutBean getMenu(ParamBean paramBean) {
        InfoServ info = new InfoServ();
        OutBean menuBean = info.menu(paramBean);
        if (paramBean.isNotEmpty("defaultMenuId")) {
            menuBean.set("defaultMenuId", paramBean.getStr("defaultMenuId"));
        }
        return menuBean;
    }
    
    /**
     * 获取用户的菜单存储路径，会自动根据系统配置，按照用户编码分目录分层级存放
     * @param userCode 用户编码
     * @return 路径
     */
    public static String getMenuFullPath(String userCode) {
        String menuParentPath = getMenuSaveRootPath();
        
        StringBuilder sb = new StringBuilder(menuParentPath + MENU_DIR);
        String menu = Context.getSyConf("SY_COMM_MENU_PATH_LEVEL", "");
        int userLen = userCode.length();
        if (menu.length() > 0) {
            String[] menuPath = menu.split(Constant.SEPARATOR);
            int len = menuPath.length;
            int last = 0;
            for (int i = 0; i < len; i++) {
                int pos = Integer.parseInt(menuPath[i]) + last;
                if (pos >= userLen) {
                    sb.append(userCode).append("/");
                    break;
                } else {
                sb.append(userCode.substring(last, pos)).append("/");
                last = pos;
            }
        }
        }
        return sb.append(userCode).append(".obj").toString();
    }
    
    /**
     * 
     * @return 取得菜单文件文件存储的根路径
     */
    private static String getMenuSaveRootPath() {
        final String webDoc = FileHelper.getJsonPath();
        String menuParentPath = Context.getSyConf(CONF_MENU_OBJ_PATH, webDoc);
        //保证返回的路径最后一个字符为路径分隔符
        if (!menuParentPath.endsWith("\\") && !menuParentPath.endsWith("/")) {
            menuParentPath = menuParentPath + "/";
        }
        return menuParentPath;
    }
}
