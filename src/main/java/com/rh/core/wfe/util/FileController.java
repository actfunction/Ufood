package com.rh.core.wfe.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.util.JsonUtils;

/**
 * 文件类型权限公用类
 * 
 */
public class FileController {

    private List<Bean> filePrivBeanList;

    /**
     * {'VALUE':'3','TYPES':'*.jpg;*.jpeg;*.png;*.gif;*.doc;*.docx;*.wps;*.xls;*.xlsx;*.ppt;*.pptx;*.txt;'
     * ,'UPLOAD_BTN_NAME':'上传附件','FILENUMBER':'13'}
     * 
     * [{'ID':'GESHIHETONG','NAME':'合同列表','VALUE':'15','DESC':'格式合同文本'},
     * {'ID':'FUJIANLIEBIAO','NAME':'附件列表','VALUE':'15','DESC':'格式合同附件'}]
     * 
     * @param fileAuthStr 文件授权值
     */
    @SuppressWarnings("unchecked")
    public FileController(String fileAuthStr) {
        if (fileAuthStr.startsWith("{")) {
            Bean fileDefBean = JsonUtils.toBean(fileAuthStr);

            this.filePrivBeanList = (List<Bean>) fileDefBean.get("CAT");

        } else {
            this.filePrivBeanList = JsonUtils.toBeanList(fileAuthStr);
        }
    }

    /**
     * 
     * @param list 文件权限列表
     */
    public FileController(List<Bean> list) {
        this.filePrivBeanList = list;
    }

    /**
     * 
     * @return 文件类型转成的权限列表
     */
    public List<Bean> getFileControlBeanList() {
        return filePrivBeanList;
    }

    /**
     * 
     * @param fileTypeId 文件类型ID 如ZHENGWEN
     * @param priv 要修改成的权限值
     */
    public void modifyFileControl(String fileTypeId, String priv) {

        for (Bean fileAuthBean : filePrivBeanList) {
            if (fileAuthBean.getStr("ID").equals(fileTypeId)) {
                fileAuthBean.set("VALUE", priv);
            }
        }
    }

    /**
     * 
     * 设置只读权限
     */
    public void setReadOnlyPermit() {
        for (Bean fileAuthBean : filePrivBeanList) {
            fileAuthBean.set("VALUE", 1);
        }
    }

    /**
     * 
     * 设置下载
     */
    public void setReadAndDownload() {
        for (Bean fileAuthBean : filePrivBeanList) {
            fileAuthBean.set("VALUE", 33);
        }
    }

    /**
     * 保留服务上定义的最低权限：读和写
     */
    public void reserveMinPermission() {
        for (Bean fileAuthBean : filePrivBeanList) {
            String value = fileAuthBean.get("VALUE", "1");
            if (StringUtils.isNumeric(value)) {
                final int val = Integer.parseInt(value);
                int result = 1; // 默认读权限
                if ((val & 32) > 0) { // 如果有下载则增加下载权限
                    result += 32;
                }

                fileAuthBean.set("VALUE", result);
            } else {
                value = value.toLowerCase();
                String result = "r";
                if (value.contains("d")) {
                    result += "d";
                }

                fileAuthBean.set("VALUE", result);
            }
        }
    }
}
