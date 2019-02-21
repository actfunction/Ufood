package com.rh.sup.util;

import java.util.List;

import com.rh.core.base.Bean;

/**
 * 督查工具类
 * @author admin
 *
 */
public class SUUtils {
	/**
	 * 根据用户Bean列表取得UserIds
	 */
	public static String getUserIds(List<? extends Bean> userList) {
		StringBuilder sb = new StringBuilder();
		
		if (userList.size() > 0) {
			sb.append(userList.get(0).getStr("USER_CODE"));
			for (int i=1;i<userList.size();i++) {
				sb.append(",")
					.append(userList.get(i).getStr("USER_CODE"));
			}
		}
		
		return sb.toString();
	}
	/**
	 * 根据用户Bean列表取得UserIds(督查用)
	 */
	public static String getUserIdsDc(List<? extends Bean> userList) {
		StringBuilder sb = new StringBuilder();
		
		if (userList.size() > 0) {
			sb.append(userList.get(0).getStr("C_USER_CODE"));
			for (int i=1;i<userList.size();i++) {
				sb.append(",")
					.append(userList.get(i).getStr("C_USER_CODE"));
			}
		}
		
		return sb.toString();
	}
}
