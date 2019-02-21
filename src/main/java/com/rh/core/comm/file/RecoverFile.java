package com.rh.core.comm.file;

import java.io.IOException;
import java.io.InputStream;

import com.rh.core.base.Bean;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.util.Constant;

/**
 * 
 * @author wanglong
 * 
 */
public class RecoverFile extends CommonServ {

    /**
     * 
     * @param paramBean bean
     * @return Bean
     */
    public Bean recoverHisFile(Bean paramBean) {

        // 获取SY_COMM_FILE_HIS表中的数据主键，根据主键将历史记录表中的该条数据信息插入到SY_COMM_FILE表中
        String histFileID = paramBean.getStr("HISTFILE_ID");

        // 获取文件ID，根据该ID将SY_COMM_FILE中的数据插入到SY_COMM_FILE_HIS中
//        String fileID = paramBean.getStr("FILE_ID");
//
//        // 创建一个Bean，用于接收查询到的内容。
//        Bean fileContent = new Bean();
//
//        // 根据fileID到SY_COMM_FILE表中查询数据，该数据处理后插入到SY_COMM_FILE_HIS表中。
//        fileContent = ServDao.find(ServMgr.SY_COMM_FILE, fileID);
//
//        // 获取用户信息
//        UserBean currentUser = Context.getUserBean();
//
//        String sUser = currentUser.getsUser();
//        String sUname = currentUser.getName();
//        String sDept = currentUser.getDeptName();
//        String sDName = currentUser.getDeptName();
//        String sCMPY = currentUser.getCmpyName();
//
//        // 处理数据
//        Bean insertFileHis = new Bean();
//
//        // 将SY_COMM_FILE中的数据处理后，存储到insertFileHis中
//        insertFileHis.set("FILE_ID", fileID).set("HISTFILE_PATH", fileContent.getStr("FILE_PATH"))
//                .set("HISTFILE_SIZE", fileContent.getStr("FILE_SIZE"))
//                .set("HISTFILE_MTYPE", fileContent.getStr("FILE_MTYPE"))
//                .set("HISTFILE_MEMO", fileContent.getStr("FILE_MEMO")).set("S_FLAG", 1).set("S_USER", sUser)
//                .set("S_UNAME", sUname).set("S_DEPT", sDept).set("S_DNAME", sDName).set("S_CMPY", sCMPY)
//                .set("FILE_CHECKSUM", fileContent.getStr("FILE_CHECKSUM"));
//
//        // 将insertFileHis的值插入到SY_COMM_FILE_HIS表中。
//        ServDao.create(ServMgr.SY_COMM_FILE_HIS, insertFileHis);
//
//        // 删除SY_COMM_FILE_HIS中的历史文件，然后将该条记录插入到SY_COMM_FILE表中。
//        // ServDao.destroy(ServMgr.SY_COMM_FILE_HIS, paramBean);
//
//        // 更新SY_COMM_FILE表中的记录
//        Bean updateFile = new Bean();
//
//        // 将获取的值保存到updateFile对象中，并将updateFile对象中的数据插入到表SY_COMM_FILE中。
//        updateFile.set("FILE_ID", paramBean.getStr("FILE_ID")).set("FILE_PATH", paramBean.getStr("HISTFILE_PATH"))
//                .set("FILE_SIZE", paramBean.getStr("HISTFILE_SIZE"))
//                .set("FILE_MTYPE", paramBean.getStr("HISTFILE_MTYPE"))
//                .set("FILE_MEMO", paramBean.getStr("HISTFILE_MEMO")).set("S_FLAG", 1).set("S_USER", sUser)
//                .set("S_UNAME", sUname).set("S_DEPT", sDept).set("S_DNAME", sDName).set("S_CMPY", sCMPY)
//                .set("FILE_CHECKSUM", paramBean.getStr("FILE_CHECKSUM")).setId(paramBean.getStr("FILE_ID"));
//
//        // 将移除HISTFILE_ID后的数据更新到SY_COMM_FILE表中。
//        ServDao.update(ServMgr.SY_COMM_FILE, updateFile);

        // revert file
        String hisFile = histFileID;
        Bean targetFile = FileMgr.getFile(hisFile);
        try {
            InputStream is = FileMgr.download(targetFile);

            FileMgr.update(targetFile.getStr("SRC_FILE"), is, targetFile.getStr("HISFILE_MTYPE"));

        } catch (IOException e) {
            throw new RuntimeException(e);
          }

        return new Bean().set(Constant.RTN_MSG, Constant.RTN_MSG_OK);

    }

}
