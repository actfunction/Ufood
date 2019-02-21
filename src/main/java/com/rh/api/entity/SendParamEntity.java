package com.rh.api.entity;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;

/**
 * 分发的参数Entity
 * @author Fe
 * @version 1.0
 *
 */
public class SendParamEntity {

	/**
	 * 节点ID
	 */
	private String nid;
	/**
	 * 发文数据bean
	 */
	private Bean fwDataBean;
	/**
	 * 分发用户
	 */
	private UserBean sendUser;
	/**
	 * 文件list
	 */
	private List<Bean> fileList;
	
	
	public SendParamEntity(String nid, Bean fwDataBean, UserBean sendUser, List<Bean> fileList) {
		setNid(nid);
		setSendUser(sendUser);
		setFileList(fileList);
		setFwDataBean(fwDataBean);
	}
	/**
	 * @return the nid
	 */
	public String getNid() {
		return nid;
	}
	/**
	 * @param nid the nid to set
	 */
	public void setNid(String nid) {
		this.nid = nid;
	}
	/**
	 * @return the fwDataBean
	 */
	public Bean getFwDataBean() {
		return fwDataBean;
	}
	/**
	 * @param fwDataBean the fwDataBean to set
	 */
	public void setFwDataBean(Bean fwDataBean) {
		this.fwDataBean = fwDataBean;
	}
	/**
	 * @return the sendUser
	 */
	public UserBean getSendUser() {
		return sendUser;
	}
	/**
	 * @param sendUser the sendUser to set
	 */
	public void setSendUser(UserBean sendUser) {
		this.sendUser = sendUser;
	}
	/**
	 * @return the fileList
	 */
	public List<Bean> getFileList() {
		return fileList;
	}
	/**
	 * @param fileList the fileList to set
	 */
	public void setFileList(List<Bean> fileList) {
		this.fileList = fileList;
	}

}
