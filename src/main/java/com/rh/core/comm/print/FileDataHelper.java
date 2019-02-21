package com.rh.core.comm.print;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.comm.FileMgr;

/**
 * 
 * @author wanglong
 * 
 */
public class FileDataHelper {
    private List<Bean> fileList = null;

    /**
     * 
     * @param servSrcId 服务ID
     * @param dataId 关联公文的数据ID
     */
    public FileDataHelper(String servSrcId, String dataId) {
        fileList = FileMgr.getFileListBean(servSrcId, dataId);
    }

    /**
     * 获取文件名称
     * @param fileCat 文件类型编码
     * @return 多个文件名使用都好分割
     */
    public String getFileNames(String fileCat) {
        List<Bean> list = getFileList(fileCat);
        if (list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        for (Bean bean : list) {
            sb.append(",");
            sb.append(bean.get("DIS_NAME"));
        }

        return sb.substring(1);
    }

    /**
     * 获取文件列表
     * @param fileCat 文件类型
     * @return 指定类型的文件列表
     */
    public List<Bean> getFileList(String fileCat) {
        List<Bean> result = new ArrayList<Bean>();
        for (Bean bean : fileList) {
            if (bean.getStr("FILE_CAT").equals(fileCat)) {
                result.add(bean);
            }
        }

        return result;
    }
}
