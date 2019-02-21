/**
 * 
 */
package com.rh.core.wfe.resource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.rh.core.base.Bean;
import com.rh.core.org.mgr.UserMgr;

/**
 * 任务分配中的组。每个GroupBean实例作为一个任务实例的抢占式处理人。每个GroupBean中有1到多个用户。
 * @author wanglong
 *
 */
public class GroupBean extends Bean {

    /**  */
    private static final long serialVersionUID = 1L;
    
    /** 组名，可以使用角色、部门编码等作为组名。可以为空。 */
    private String name = null;
    
    /** 组中的成员 */
    private Set<String> userIds = new HashSet<String>();

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the userIds
     */
    public Set<String> getUserIds() {
        return userIds;
    }    
    
    
    /**
     * @return 逗号分隔的用户Name字符串
     */
    public String getUserNames() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = userIds.iterator();
        while (iter.hasNext()) {
            sb.append(UserMgr.getUser(iter.next()).getName()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }   
    
    /**
     * @return 逗号分隔的用户Id字符串
     */
    public String getUserIdStr() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = userIds.iterator();
        while (iter.hasNext()) {
            sb.append(iter.next()).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
    

    /**
     * @param userIds the userIds to set
     */
    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }
    
    
    /**
     * 向组中加入用户
     * @param userId 用户id
     */
    public void addUser(String userId) {
        this.userIds.add(userId);
    }
    
    
    /**
     * 向组中加入用户
     * @param userIds 用户id数组
     */
    public void addUsers(String[] userIds) {
        if (userIds != null && userIds.length > 0) {
            for (int i = 0; i < userIds.length; i++) {
                this.userIds.add(userIds[i]);
            }
        }
    }
    
    
    /**
     * 向组中加入用户
     * @param userIds 用户id列表
     */
    public void addUsers(List<String> userIds) {
        if (userIds != null && userIds.size() > 0) {
            this.userIds.addAll(userIds);
        }
    }
    
    
    /**
     * 从组中移除用户
     * @param userId 用户id
     */
    public void removeUser(String userId) {
        this.userIds.remove(userId);
    }
    
}
