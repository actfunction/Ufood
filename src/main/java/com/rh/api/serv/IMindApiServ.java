package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IMindApiServ {
    public ApiOutBean getMindListByDataId(Bean reqData);
    
    public ApiOutBean inputMind(Bean reqData);

    public ApiOutBean getOftenUseMindList(Bean reqData);

    public ApiOutBean addOftenMind(Bean reqData);
    
    public ApiOutBean getDiscuss(Bean reqData);
    
    public ApiOutBean disToMind(Bean reqData);

    public ApiOutBean getMindListByDataIdForRule(Bean reqData);
}
