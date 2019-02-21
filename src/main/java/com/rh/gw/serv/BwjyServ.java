package com.rh.gw.serv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.JsonUtils;
import com.rh.gw.util.GwPageObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * 公文系统办文依据扩展类
 *
 * @author kfzz-yxb
 */
public class BwjyServ extends GwExtServ {
    /**
     * 将对象转JSON
     */
    private static ObjectMapper mapper = new ObjectMapper();
    /**
     * 审计提供的url地址,公文系统调用获取项目名
     */
    private static final String SJ_INTERFACE_URL_QS = "http://122.119.93.99:30098/item/findProJect";
    private static final String SJ_INTERFACE_URL = Context.getSyConf("SJ_INTERFACE_URL",SJ_INTERFACE_URL_QS);
    /**
     * 调用OSS系统的地址，暂时没有
     */
    private static final String OSS_URL_QS = "http://localhost/file/oss.do";
    private static final String OSS_URL = Context.getSyConf("OSS_URL", OSS_URL_QS);
    /**
     * 公文系统提供给审计的业务发文URL
     */
    private static final String GW_URL = "/oa/view/page/gwForm.html?sjFlag=2&servId=OA_GW_GONGWEN_ICBC_YWFW";

    /**
     * 公文系统提供给审计的流程图查看的URL
     */
    private static final String GW_FLOW_URL_QS = "/oa/view/page/gwForm.html?lcFlag=2&servId=OA_GW_GONGWEN_ICBC_YWFW&dataId=";
    private static final String GW_FLOW_URL = Context.getSyConf("GW_FLOW_URL", GW_FLOW_URL_QS);

    /**
     * 向审计系统传递流程图URL的接口
     */
    private static final String FLOW_URL_TO_SJ_QS = "http://122.119.93.99:30098/rest/apmservice/officialDocument/officialFlow";
    private static final String FLOW_URL_TO_SJ = Context.getSyConf("FLOW_URL_TO_SJ", FLOW_URL_TO_SJ_QS);
    /**
     * 办结后将文件信息传递给审计系统
     */
    private static final String FILE_TO_SJ_QS = "http://122.119.93.99:30098/rest/apmservice/officialDocument/officialUploadWenshu";
    private static final String FILE_TO_SJ = Context.getSyConf("FILE_TO_SJ", FILE_TO_SJ_QS);

    /**
     * 日志记录
     */
    private static Logger log = Logger.getLogger(BwjyServ.class);

    /**
     * 点击公文办文依据按钮，将前台参数传递后台处理
     *
     * @param paramBean 参数
     * @return
     */
    public OutBean doGetGongWen(ParamBean paramBean) {
        OutBean out = new OutBean();
        String resultJson = null;
        GwPageObject<Bean> pageResult = new GwPageObject<>();
        try {
            //获取servID
            String servId = paramBean.getStr("servId");
            //获取查询的文号、标题、开始/截止时间
            String wenHao = paramBean.getStr("wenHao");
            String title = paramBean.getStr("title");
            String startTime = paramBean.getStr("startTime");
            String endTime = paramBean.getStr("endTime");

            //前台传回的一页显示总条数，默认10
            Integer pageSize = paramBean.getInt("limit");
            //当前用户ID
            String userId = paramBean.getStr("uid");
            //当前页数
            Integer pageCurrent = paramBean.getInt("page");
            if (pageCurrent == null || pageCurrent < 1) {
                throw new IllegalArgumentException("当前页码值无效");
            }
            //查询总记录数;
            String sql = "";
            if (StringUtils.isEmpty(startTime) && StringUtils.isEmpty(endTime)) {
                sql = "select * from PLATFORM.OA_GW_GONGWEN where S_FLAG = '1' AND " +
                        "S_USER = '" + userId + "' and TMPL_CODE = '" + servId + "' " +
                        "and GW_TITLE like '%" + title + "%' " +
                        "and GW_YEAR_NUMBER like '%" + wenHao + "%' order by GW_END_TIME";
            } else if (StringUtils.isEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                sql = "select * from PLATFORM.OA_GW_GONGWEN where S_FLAG = '1' AND " +
                        "S_USER = '" + userId + "' and TMPL_CODE = '" + servId + "' " +
                        "and GW_TITLE like '%" + title + "%' " +
                        "and GW_YEAR_NUMBER like '%" + wenHao + "%' " +
                        "and GW_END_TIME < '" + endTime + "' order by GW_END_TIME";
            } else if (StringUtils.isNotEmpty(startTime) && StringUtils.isEmpty(endTime)) {
                sql = "select * from PLATFORM.OA_GW_GONGWEN where S_FLAG = '1' AND " +
                        "S_USER = '" + userId + "' and TMPL_CODE = '" + servId + "' " +
                        "and GW_TITLE like '%" + title + "%' " +
                        "and GW_YEAR_NUMBER like '%" + wenHao + "%' " +
                        "and GW_END_TIME > '" + startTime + "' order by GW_END_TIME";
            } else if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                sql = "select * from PLATFORM.OA_GW_GONGWEN where S_FLAG = '1' AND " +
                        "S_USER = '" + userId + "' and TMPL_CODE = '" + servId + "' " +
                        "and GW_TITLE like '%" + title + "%' " +
                        "and GW_YEAR_NUMBER like '%" + wenHao + "%' " +
                        "and GW_END_TIME between '" + startTime + "' and '" + endTime + "' order by GW_END_TIME";
            }

            SqlExecutor executor = Transaction.getExecutor();
            int rowCount = executor.count(sql);
            //假如总记录数为0,则抛出异常
            if (rowCount == 0) {
                throw new IllegalArgumentException("系统中没有找到对应数据");
            }

            //判断没错就真正执行查询操作
            //获取开始页数
            int startIndex = (pageCurrent - 1) * pageSize;
            //拿到当前页显示的条数
            List<Bean> result = executor.query(sql);
            int endSize = startIndex + pageSize;
            if (endSize >= result.size()) endSize = result.size();
            result = result.subList(startIndex, endSize);
            //在分页工具类中添加分页信息
            pageResult.setPageCurrent(pageCurrent);
            pageResult.setList(result);
            pageResult.setPageSize(pageSize);
            pageResult.setRowCount(rowCount);
            //数据回显
            resultJson = mapper.writeValueAsString(pageResult);
        } catch (IllegalArgumentException e) {
            log.error(e);
            out.set("code", 0);
            out.set("error", e.getMessage());
            return out;
        } catch (Exception e) {
            log.error(e);
            out.set("code", 0);
            out.set("error", e.getMessage());
            return out;
        }
        out.set("code", 200);
        return out.setData(resultJson);
    }

    /**
     * 保存公文办文依据进数据库
     *
     * @param paramBean
     * @return
     */
    public OutBean doSaveGwBwyj(ParamBean paramBean) {
        OutBean out = new OutBean();
        //在办文依据中获取当前用户已经存在的办文依据
        String sql = "SELECT GW_ID FROM PLATFORM.OA_GW_GONGWEN_BWJY WHERE DATA_ID = '" + paramBean.getStr("DATA_ID") + "' AND S_USER = '" + paramBean.getStr("uid") + "'";
        SqlExecutor executor = Transaction.getExecutor();
        List<Bean> gwBean = executor.query(sql);
        List<String> gwIds = new ArrayList<>();
        for (Bean gwId : gwBean) {
            gwIds.add(gwId.getStr("GW_ID"));
        }
        //获取前台选中的所有办文依据
        List<Bean> params = paramBean.get("params", new ArrayList<Bean>());
        for (Bean fileBwan : params) {
            if (!gwIds.contains(fileBwan.getStr("GW_ID"))) {
                //保存到数据库
                ServDao.save("OA_GW_GONGWEN_BWYJ", fileBwan);
            }
        }
        return out;
    }


    public OutBean doSaveSj(ParamBean paramBean) {
        OutBean out = new OutBean();
        //在办文依据中获取当前用户已经存在的办文依据
        String sql = "SELECT PROJECT_ID FROM PLATFORM.OA_GW_GONGWEN_BWJY WHERE DATA_ID = '" + paramBean.getStr("DATA_ID") + "'";
        SqlExecutor executor = Transaction.getExecutor();
        List<Bean> gwBean = executor.query(sql);
        List<String> gwIds = new ArrayList<>();
        for (Bean gwId : gwBean) {
            gwIds.add(gwId.getStr("PROJECT_ID"));
        }
        //获取前台选中的所有审计办文依据
        List<Bean> params = paramBean.get("params", new ArrayList<Bean>());
        for (Bean fileBwan : params) {
            if (!gwIds.contains(fileBwan.getStr("PROJECT_ID"))) {
                //保存到数据库
                ServDao.save("OA_GW_GONGWEN_BWYJ", fileBwan);
            }
        }
        return out;
    }

    /**
     * 审计系统的业务发文在保存后向审计系统发送可以查看公文流转信息的URL
     *
     * @param paramBean
     * @return
     */
    public OutBean doSendFlowToSj(ParamBean paramBean) {
        OutBean out = new OutBean();
        //当前流程实例ID
        String dataId = paramBean.getStr("DATA_ID");
        //审计系统业务发文相关信息保存的主键ID
        String yjId = paramBean.getStr("YJ_ID");
        //审计系统的标识
        String sjFlag = paramBean.getStr("SJ_FLAG");
        //更新文件标题
        String title = paramBean.getStr("GW_TITLE");
        //根据办文依据的主键YJ_ID更新data_id
        try {
            Transaction.begin();
            SqlExecutor executor = Transaction.getExecutor();
            String flowSql = "SELECT FLOW_ID FROM PLATFORM.OA_GW_GONGWEN_BWJY WHERE YJ_ID = '" + yjId + "'";
            String flowId = executor.queryOne(flowSql).getStr("FLOW_ID");
            String sql = "UPDATE PLATFORM.OA_GW_GONGWEN_BWJY SET GW_ID = '" + dataId + "', DATA_ID = '" + dataId + "', SJ_FALG = '" + sjFlag + "', GW_TITLE = '"+ title +"' WHERE YJ_ID = '" + yjId + "'";
            int count = executor.execute(sql);
            if (count == 1) {
                Transaction.commit();
                String url = GW_FLOW_URL + dataId;
                //将正确的参数传给审计系统
                Map<String, String> param = new HashMap<>();
                param.put("flowUrl", url);
                param.put("orgId", paramBean.getStr("ORG_ID"));
                param.put("userId", paramBean.getStr("USER_ID"));
                param.put("flowId", flowId);
                doSendMsgToOss(FLOW_URL_TO_SJ, param, "UTF-8");
                out.set("code", 200);
                return out;
            } else {
                Transaction.rollback();
                throw new RuntimeException("该公文对应信息不存在");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            out.set("code", 500);
            return out;
        } finally {
            Transaction.end();
        }
    }


    /**
     * 办结后上传公文资料
     *
     * @param paramBean
     * @return
     */
    public OutBean doSendOss(ParamBean paramBean) {
        OutBean out = new OutBean();
        //办结状态
        String status = paramBean.getStr("STATUS");
        //当前用户ID
        String userId = paramBean.getStr("S_USER");
        //执行数据库查询的方法
        SqlExecutor executor = Transaction.getExecutor();
        String dataId = paramBean.getStr("DATA_ID");
        //保存向审计接口发送的数据
        Map<String, String> sjBean = new HashMap<>();
        try {
            //根据DATA_ID查出当前流程对应的审计相关的所有办文依据
            String bwSql = "SELECT * FROM PLATFORM.OA_GW_GONGWEN_BWJY WHERE DATA_ID = '" + dataId + "' and SJ_FALG = '2'";
            List<Bean> bwResult = executor.query(bwSql);
            //遍历当前流程对应的所有的审计项目的办文依据
            for (Bean bwBean : bwResult) {
                String sql = "SELECT * FROM PLATFORM.SY_COMM_FILE WHERE DATA_ID = '" + dataId + "' and S_USER = '" + userId + "'";
                //返回的所有相关文件信息
                List<Bean> fileResult = executor.query(sql);
                Boolean flag = false;
                //遍历DATA_ID对应的所有文件，找出定稿
                for (Bean oper : fileResult) {
                    if ("WENGAO".equals(oper.getStr("ITEM_CODE"))) {
                        flag = true;
                        Map<String, String> ossParam = new HashMap<>();
                        ossParam.put("fileId", oper.getStr("FILE_ID"));

                        //保存定稿信息的Bean
                        Bean docMsg = new Bean();
                        if ("2".equals(status)) {
                            //调用OSS的接口,返回链接地址
                            String fileUrl = doSendMsgToOss(OSS_URL, ossParam, "UTF-8");
                            //fileUrl不为空则保存到数据库中，后续还需要做正则校验，确定url格式正确之后再保存-------------------
                            if (StringUtils.isNotEmpty(fileUrl)) {
                                String updateSql = "UPDATE PLATFORM.OA_GW_GONGWEN_BWJY SET SJ_LINK = '" + fileUrl + "' WHERE YJ_ID = '" + bwBean.getStr("YJ_ID") + "'";
                                executor.execute(updateSql);
                            } else {
                                throw new RuntimeException("返回的请求不存在！");
                            }

                            docMsg.set("docId", oper.getStr("FILE_ID"));
                            docMsg.set("docType", oper.getStr("FILE_NAME").substring(oper.getStr("FILE_NAME").lastIndexOf("."), oper.getStr("FILE_NAME").length()));
                            docMsg.set("docSize", oper.getStr("FILE_SIZE"));
                            //拟稿人参数来源待确认
                            docMsg.set("writer", "");
                            docMsg.set("downloadUrl", fileUrl);
                        }
                        //调用审计接口将相关文件资料传给审计系统
                        sjBean.put("orgId", bwBean.getStr("ORG_ID"));
                        sjBean.put("userId", bwBean.getStr("USER_ID"));
                        sjBean.put("projectId", bwBean.getStr("PROJECT_ID"));
                        sjBean.put("wenshuId", bwBean.getStr("WENSHU_ID"));
                        sjBean.put("flowId", bwBean.getStr("FLOW_ID"));
                        sjBean.put("docs", mapper.writeValueAsString(docMsg));
                        sjBean.put("status", status);
                        //  sjBean.set("flowId",dataId);
                        sjBean.put("flowUrl", GW_FLOW_URL + dataId);
                        //调用审计接口返回信息
                        String result = doSendMsgToOss(FILE_TO_SJ, sjBean, "UTF-8");
                        //记录返回参数
                        Bean resultBean = JsonUtils.toBean(result);
                        if (resultBean.getStr("resultCode") != "200") {
                            throw new RuntimeException("参数返回值异常");
                        }
                    }
                }
                if(!flag) {
                    throw new RuntimeException("未上传定稿");
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage());
            out.set("code", 500);
            out.setError(e.getMessage() + ",上传OSS失败");
            return out;
        }
        out.setOk("上传OSS成功");

        return out;
    }

    /**
     * 抽取查询用户的公共方法
     *
     * @param userId 用户Id
     * @return
     */
    public boolean doGetRightUser(String userId) throws RuntimeException {
        Boolean flag = false;
        String sql = "SELECT * FROM PLATFORM.SY_ORG_USER WHERE USER_CODE = '" + userId + "'";
        SqlExecutor executor = Transaction.getExecutor();
        int rowCount = executor.count(sql);
        if (rowCount == 1) {
            flag = true;
        } else {
            throw new RuntimeException("用户数据异常");

        }
        return flag;
    }

    /**
     * 抽取调用注册中心的公共类方法
     *
     * @param url     注册中心地址
     * @param params  传入参数，键值对的String类型
     * @param charset 字符集编码
     * @return
     */
    public String doSendMsgToOss(String url, Map<String, String> params, String charset) {
        String result = null;
        //判断字符集编码
        if (org.springframework.util.StringUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }

        try {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            //参数不为空的时候，发送参数给指定的URL地址
            if (params != null) {
                List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                //遍历参数集合，将参数封装为固定格式的键值类型
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                    parameters.add(pair);
                }
                try {
                    //设置请求编码，POST传递参数
                    UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
                    post.setEntity(formEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e);
                }
            }
            //执行发送请求
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                // 请求正确,获取响应数据
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            //捕捉异常，写入日志
            log.error(e);
            throw new RuntimeException("get message has error", e);
        }
        return result;
    }


    public String doPost(String url, Map parameterMap) throws Exception {
        StringBuffer parameterBuffer = new StringBuffer();
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) != null) {
                    value = (String) parameterMap.get(key);
                } else {
                    value = "";
                }
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        URL localUrl = new URL(url);
        URLConnection connection = localUrl.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(parameterBuffer.toString());
            outputStreamWriter.flush();
            //相应失败
            if (httpURLConnection.getResponseCode() != 200) {
                throw new Exception("Response Code is" + httpURLConnection.getResponseCode());
            }

            //接收相应
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            outputStream.close();
            outputStreamWriter.close();
            inputStream.close();
            inputStreamReader.close();
            reader.close();
        }
        return resultBuffer.toString();
    }

}
