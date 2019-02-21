package com.rh.core.wfe;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流  参数
 * @author wanglong
 *
 */
public class WfParam extends Bean {
    

	/**
	 * 
	 */
	private static final long serialVersionUID = -3788769525529136356L;

	/**
     * 节点送交类型：1 送部门+角色
     */
    public static final int TYPE_TO_DEPT_ROLE = 1;
    
    /**
     * 节点送交类型：2 送角色
     */
    public static final int TYPE_TO_ROLE = 2;
    
    /**
     * 节点送交类型：3 送用户
     */
    public static final int TYPE_TO_USER = 3;
    
    /**
     * 是否是代理办理
     */
    public static final String IS_AGENT = "_IS_AGENT_USER_";
    
    //办理人
    private UserBean doneUser;
    
    //送交的人,多个以逗号分隔
    private String toUser = "";
    
    //送交的部门,多个以逗号分隔
    private String toDept = "";
    
    //送交的角色
    private String toRole = "";
    
	//送交类型
    private int typeTo = TYPE_TO_USER;
    
    //办理类型：3收回、2终止、1正常结束
    private int doneType = WfeConstant.NODE_DONE_TYPE_END;
    
    //办理类型说明
	private String doneDesc = WfeConstant.NODE_DONE_TYPE_END_DESC;
	
	/**
	 * 
	 * @return 送 交部门
	 */
    public String getToDept() {
		return toDept;
	}

    /**
     * 
     * @param aToDept 送 交部门
     */
	public void setToDept(String aToDept) {
		this.toDept = aToDept;
	}

	/**
	 * 
	 * @return 送交角色
	 */
	public String getToRole() {
		return toRole;
	}

	/**
	 * 
	 * @param aToRole 送交角色
	 */
	public void setToRole(String aToRole) {
		this.toRole = aToRole;
	}
	
    /**
     * 
     * @return 办理类型
     */
    public int getDoneType() {
		return doneType;
	}

    /**
     * 
     * @param aDoneType 办理类型
     */
	public void setDoneType(int aDoneType) {
		this.doneType = aDoneType;
	}

	/**
	 * 
	 * @return 办理说明
	 */
	public String getDoneDesc() {
		return doneDesc;
	}

	/**
	 * 
	 * @param aDoneDesc 办理说明
	 */
	public void setDoneDesc(String aDoneDesc) {
		this.doneDesc = aDoneDesc;
	}

    /**
     * 送交人
     * @return 送交人
     */
    public String getToUser() {
        return toUser;
    }
    
    /**
     * 
     * @param user 送交人 
     */
    public void setToUser(String user) {
        toUser = user;
    }
    
    /**
     * 
     * @param type 送交类型
     */
    public void setTypeTo(int type) {
        typeTo = type;
    }
    
    /**
     * 
     * @return 是否送交用户
     */
    public boolean isToUser() {
        if (typeTo == TYPE_TO_USER) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @return 是否送交部门+角色
     */
    public boolean isToDeptRole() {
        if (typeTo == TYPE_TO_DEPT_ROLE) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @return 是否送交角色
     */
    public boolean isToRole() {
        if (typeTo == TYPE_TO_ROLE) {
            return true;
        } else if (typeTo == TYPE_TO_DEPT_ROLE) {
        	return true;
        }
        return false;
    }

    /**
     * 
     * @return 送交 类型
     */
    public int getTypeTo() {
        return typeTo;
    }

    /**
     * 
     * @return 办理人
     */
    public UserBean getDoneUser() {
        return doneUser;
    }

    /**
     * 
     * @param aDoneUser 办理人
     */
    public void setDoneUser(UserBean aDoneUser) {
        this.doneUser = aDoneUser;
    }
    
    /**
     * 
     * @return 是否是委托状态
     */
    public boolean isAgent() {
        return this.getBoolean(IS_AGENT);
    }

    /**
     * 
     * @param isAgent 是否委托
     */
    public void setIsAgent(boolean isAgent) {
        this.set(IS_AGENT, isAgent);
    }

}
