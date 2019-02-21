package com.rh.core.serv.dict;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDef;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.resource.Resource;

/**
 * 字典服务类
 * 
 * @author wanglong
 * 
 */
public class DictServ extends CommonServ {

    /**
     * 保存字典前的操作
     * 
     * @param paramBean 传入的参数Bean
     */
    protected void beforeSave(ParamBean paramBean) {
        Bean fullBean = paramBean.getSaveFullData();
        if (paramBean.isNotEmpty("DICT_F_CMPY")) { //公司字段不为空，必为私有字典
            paramBean.set("S_PUBLIC", Constant.NO_INT);
        }
        Bean oldBean = paramBean.getSaveOldData();
        //项目模式下，修改操作，修改内置字典
        if (!paramBean.getAddFlag() && oldBean.getInt("PRO_FLAG") == ServDef.PRO_FLAG_INNER) {
            paramBean.set("PRO_FLAG", ServDef.PRO_FLAG_MIX);  //产品标记为混合服务
        }
        if (fullBean.getInt("DICT_IS_INNER") == Constant.YES_INT) {
            if (fullBean.getInt("S_PUBLIC") != Constant.YES_INT) { //内部字典不允许为私有
                throw new TipException(Context.getSyMsg("SY_SERV_DICT_INNER_PUBLIC") + ":" + fullBean.getId());
            }
            if (fullBean.isNotEmpty("TABLE_WHERE")) { //内部字典不需要过滤条件
                paramBean.set("TABLE_WHERE", "");
            }
        }
    }
    
    /**
     * 保存字典后的操作
     * 
     * @param paramBean 传入的参数Bean
     * @param outBean 返回的Bean
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk()) {
            Bean dict = paramBean.getSaveFullData();
            DictMgr.clearAllCache(dict.getStr("DICT_ID"), paramBean.get("$JSON_FLAG", true));
            //修改模式下，变更了字典编码，则删除历史字典编码
            if (!paramBean.getAddFlag() && paramBean.contains("DICT_ID")) {
                Bean oldBean = paramBean.getSaveOldData();
                if (oldBean.getStr("DICT_ID").length() > 0 
                        && !oldBean.getStr("DICT_ID").equals(paramBean.getStr("DICT_ID"))) {
                    DictMgr.deleteJsonFile(oldBean.getStr("DICT_ID")); //删除字典定义文件
                }
            }
        }
    }

    /**
     * 删除后的操作
     * 
     * @param paramBean 传入的参数Bean
     * @param outBean 返回的Bean
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        String[] ids = outBean.getDeleteIds().split(Constant.SEPARATOR);
        for (String id : ids) {
            DictMgr.clearAllCache(id, false); //清除缓存
            //删除服务定义文件（没有设定不处理json文件）
            if (paramBean.get("$JSON_FLAG", true)) { 
                DictMgr.deleteJsonFile(id); //删除字典定义文件
            }
        }
    }

    
    /**
     * 将服务定义信息生成为json文本文件
     * @param paramBean 参数信息
     * @return 生成结果
     */
    public OutBean toJson(ParamBean paramBean) {
        int count = 0;
        Bean param = new Bean();
        StringBuilder where = new StringBuilder(" and S_FLAG=");
        where.append(Constant.YES);
        if (paramBean.getId().length() > 0) {
            where.append(" and DICT_ID in ('").append(paramBean.getId().replaceAll(",", "','")).append("')");
        }
        param.set(Constant.PARAM_WHERE, where.toString());
        List<Bean> dictList = ServDao.finds(ServMgr.SY_SERV_DICT, param);
        for (Bean dict : dictList) {
            if (DictMgr.toJsonFile(dict.getId())) {
                DictMgr.clearAllCache(dict.getId(), false); //清除缓存
                count++;
            }
        }
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_BATCHSAVE_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_BATCHSAVE_NONE"));
        }
        return outBean;
    }
    
    
    /**
     * 将字典定义从文件导入数据库，缺省导入选定的字典，如果没有选定导入数据库不存在
     * @param paramBean 参数信息
     * @return 导入结果
     */
    public OutBean fromJson(ParamBean paramBean) {
        int count = 0;
        if (paramBean.getId().length() > 0) {
            String[] dictIds = paramBean.getId().split(Constant.SEPARATOR);
            for (String dictId : dictIds) {
                ParamBean param = new ParamBean();
                param.setServId(ServMgr.SY_SERV_DICT).setId(dictId).setLinkFlag(true)
                    .set("$JSON_FLAG", false);
                delete(param);
                param = new ParamBean(DictMgr.getDictDefByFile(dictId)); //从文件获取最新的定义信息
                param.setServId(ServMgr.SY_SERV_DICT).setLinkFlag(true).set("$JSON_FLAG", false);
                if (add(param).isOk()) {
                    count++;
                }
            }
        } else {
            List<Bean> dictList = DictMgr.getDictDefListByFile();
            for (Bean dict : dictList) {
                Bean countBean = new Bean();
                countBean.set("DICT_ID", dict.getStr("DICT_ID"));
                if (ServDao.count(ServMgr.SY_SERV_DICT, countBean) <= 0) { //不存在的再添加
                    ParamBean param = new ParamBean(dict);
                    param.setServId(ServMgr.SY_SERV_DICT).set("$JSON_FLAG", false);
                    if (add(param).isOk()) {
                        count++;
                    }
                }
            }
        }
        OutBean outBean = new OutBean();
        outBean.setOk(Context.getSyMsg("SY_IMPORT_OK", String.valueOf(count)));
        return outBean;
    }
   
    /**
     * 清除缓存信息
     * @param paramBean    参数Bean
     * @return 服务相关设定信息
     */
    public OutBean clearCache(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        if (paramBean.getId().length() > 0) {
            DictMgr.clearAllCache(paramBean.getId(), false); //清除缓存但不更新定义文件
            outBean.setOk();
        } else {
            outBean.setError();
        }
        return outBean;
    }

    /**
     * 重建树形信息，自动设置LEVEL和CODE_PATH
     * @param paramBean    参数Bean
     * @return 服务相关设定信息
     */
    public OutBean rebuildTree(ParamBean paramBean) {
        int count = 0;
        String id = paramBean.getId();
        DictMgr.clearCache(id);
        Bean dictDef = DictMgr.getDictDef(id);
        String level, codePath;
        if (dictDef.getInt("DICT_IS_INNER") == Constant.YES_INT) {
            level = "ITEM_LEVEL";
            codePath = "CODE_PATH";
        } else {
            level = dictDef.getStr("DICT_F_LEVEL");
            codePath = dictDef.getStr("DICT_F_PATH");
        }
        OutBean outBean = new OutBean();
        if (level.length() > 0 && codePath.length() > 0 
                && dictDef.getInt("DICT_TYPE") == DictMgr.DIC_TYPE_TREE) { //树
            List<Bean> treeList = DictMgr.toTree(dictDef, DictMgr.getDictItemList(dictDef, Context.getCmpy()));
            List<Bean> upList = new ArrayList<Bean>();
            for (Bean item : treeList) {
                item.set("LEVEL", 1);
                item.set("CODE_PATH", item.getStr("ID") + Constant.CODE_PATH_SEPERATOR);
                upList.add(item);
                upList.addAll(DictMgr.buildItemLevel(item));
            }
            StringBuilder sql = new StringBuilder("update ");
            sql.append(dictDef.getStr("TABLE_ID")).append(" set ").append(level)
            .append("=#LEVEL#,").append(codePath).append("=#CODE_PATH# where ")
            .append(dictDef.getStr("DICT_F_ID")).append("=#ID#");
            count = Context.getExecutor().executeBatchBean(sql.toString(), upList);
        }
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_BATCHSAVE_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_BATCHSAVE_NONE"));
        }
        return outBean;
    }
    

    /**
     * 导入服务定义 如果系统中已经有此流程定义，则进行覆盖
     * @param paramBean 要导入的文件的fileId
     * @return Bean
     */
    public OutBean uploadJson(ParamBean paramBean) {
        OutBean resultBean = new OutBean();
        int count = 0; // 导入的流程数量
        String fileId = paramBean.getStr("fileId");
        Bean fileBean = FileMgr.getFile(fileId);
        if (fileBean != null) {
            ZipInputStream zipIn = null;
            InputStream in = null;
            try {
                if (fileBean.getStr("FILE_MTYPE").equals("application/zip")) {
                    zipIn = new ZipInputStream(FileMgr.download(fileBean));
                    while (zipIn.getNextEntry() != null) {
                        in = new BufferedInputStream(zipIn);
                        if (impJsonBean(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)))) {
                            count++;
                        }
                        zipIn.closeEntry();
                    }
                } else {
                    in = FileMgr.download(fileBean);
                    if (impJsonBean(JsonUtils.toBean(IOUtils.toString(in, Constant.ENCODING)))) {
                        count++;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                if (zipIn != null) {
                    IOUtils.closeQuietly(zipIn);
                }
                if (in != null) {
                    IOUtils.closeQuietly(in);
                }
            }
        }
        if (count > 0) {
            resultBean.setOk(count + "个定义导入成功！");
            FileMgr.deleteFile(fileBean);
        } else {
            resultBean.setError();
        }
        return resultBean;
    }
    
    /**
     * 重置内部字典定义：从定义文件导入数据库，导入指定的字典。
     * @param paramBean 参数信息
     * @return 导入结果
     */
    public OutBean resetInner(ParamBean paramBean) {
        int count = 0;
        if (paramBean.getId().length() > 0) {
            String[] ids = paramBean.getId().split(Constant.SEPARATOR);
            for (String id : ids) {
                Bean serv = Resource.getDict(id);
                if (serv != null) {
                    ParamBean param = new ParamBean();
                    //删除服务定义，删除文件、级联删除
                    param.setId(id).setServId(ServMgr.SY_SERV_DICT).setLinkFlag(true).set("$JSON_FLAG", true); 
                    delete(param);
                    //插入数据，不生成文件
                    param = new ParamBean(serv).setServId(ServMgr.SY_SERV_DICT).setAddFlag(true)
                            .set("$JSON_FLAG", false).set("PRO_FLAG", ServDef.PRO_FLAG_INNER);
                    if (save(param).isOk()) {
                        count++;
                    }
                }
            }
        }
        OutBean outBean = new OutBean();
        outBean.setOk(Context.getSyMsg("SY_IMPORT_OK", String.valueOf(count)));
        return outBean;
    }
    
    
    /**
     * 导入内部字典定义：从定义文件导入数据库，重新覆盖所有内置字典，不覆盖已经被修改的内置字典
     * @param paramBean 参数信息
     * @return 导入结果
     */
    public OutBean importInner(ParamBean paramBean) {
        int count = 0;
        LinkedHashMap<String, Bean> dictMap = Resource.getDictMap();
        for (String key : dictMap.keySet()) {
            boolean addFlag = true;
            Bean dict = dictMap.get(key);
            Bean data = ServDao.find(ServMgr.SY_SERV_DICT, key);
            ParamBean param = new ParamBean();
            if (data != null) { //不存在的再添加
                if (data.getInt("PRO_FLAG") != ServDef.PRO_FLAG_INNER) { //只允许覆盖INNER的字典
                    addFlag = false;
                } else {
                    //删除服务定义，级联删除，不删除文件
                    param.setId(key).setServId(ServMgr.SY_SERV_DICT).setLinkFlag(true).set("$JSON_FLAG", false); 
                    delete(param);
                }
            }
            if (addFlag) {
                try {
                    //插入数据，不生成文件
                    param = new ParamBean(dict).setServId(ServMgr.SY_SERV_DICT).setAddFlag(true)
                            .set("$JSON_FLAG", false).set("PRO_FLAG", ServDef.PRO_FLAG_INNER);
                    if (save(param).isOk()) {
                        count++;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        OutBean outBean = new OutBean();
        outBean.setOk(Context.getSyMsg("SY_IMPORT_OK", String.valueOf(count)));
        return outBean;
    }
    
    /**
     * 导入JsonBean格式的服务定义，如果已经存在，则直接覆盖当前服务定义（删除后导入）
     * @param jsonBean JsonBean格式的服务定义
     * @return 是否成功导入
     */
    private boolean impJsonBean(Bean jsonBean) {
        ParamBean param = new ParamBean();
        String servId = jsonBean.getStr("DICT_ID");
        param.setId(servId).setServId(ServMgr.SY_SERV_DICT).setLinkFlag(true).set("$JSON_FLAG", false); //不生成文件、指定级联删除
        delete(param);
        param = new ParamBean(jsonBean).setServId(ServMgr.SY_SERV_DICT).setAddFlag(true);
        return save(param).isOk();
    }
}
