package com.rh.core.comm;

import java.util.List;

import com.rh.core.base.Bean;

/**
 * 菜单过滤类
 * @author wanglong
 *
 */
public interface MenuFilter {
    /**
     * 
     * @param menuTree 菜单数据 
     * @return 过滤之后的菜单数据
     */
    List<Bean> filterMenu(List<Bean> menuTree);
}
