package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IFileApiServ {

    public ApiOutBean getFileListByDataId(Bean reqData);

}
