package com.rh.resource;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.util.JsonUtils;

/**
 * 系统资源装载类，用于装载服务、字典定义等。
 * 
 * @author wanglong
 *
 */
public class Resource {
	/** log */
	private static Log log = LogFactory.getLog(Resource.class);
	/** 服务定义列表 */
	private static LinkedHashMap<String, Bean> servMap = null;
	/** 字典定义列表 */
	private static LinkedHashMap<String, Bean> dictMap = null;

	/**
	 * 从资源定义中获取服务定义信息
	 * 
	 * @param servId
	 *            服务编码
	 * @return 服务定义
	 */
	public static Bean getServ(String servId) {
		return getServMap().get(servId);
	}

	/**
	 * 从资源定义中获取字典定义信息
	 * 
	 * @param dictId
	 *            字典编码
	 * @return 字典定义
	 */
	public static Bean getDict(String dictId) {
		return getDictMap().get(dictId);
	}

	/**
	 * 得到服务定义列表
	 * 
	 * @return 服务定义列表
	 */
	public static LinkedHashMap<String, Bean> getServMap() {
		if (servMap == null) {
			servMap = new LinkedHashMap<String, Bean>();
			initMap("SERV", servMap);
		}
		return servMap;
	}

	/**
	 * 得到字典定义列表
	 * 
	 * @return 字典定义列表
	 */
	public static LinkedHashMap<String, Bean> getDictMap() {
		if (dictMap == null) {
			dictMap = new LinkedHashMap<String, Bean>();
			initMap("SERV_DICT", dictMap);
		}
		return dictMap;
	}

	/**
	 * 根据类型初始化资源中的定义
	 * 
	 * @param mapType
	 *            资源类型
	 * @param map
	 *            资源载体
	 */
	private static synchronized void initMap(String mapType, LinkedHashMap<String, Bean> map) {
		String path = "com/rh/resource/";
		String[] proCats = Context.app("SY_PRO_CAT", "sy").split(","); // 根据系统配置设定的目录装载定义信息
		if (proCats.length == 1 && proCats[0].isEmpty()) {
			proCats[0] = "sy";
		} else if (ArrayUtils.indexOf(proCats, "sy") < 0) { // 缺省必须有core的定义对应的SY
			proCats = (String[]) ArrayUtils.add(proCats, "sy");
		}
		for (String cat : proCats) {
			StringBuilder name = new StringBuilder(cat).append("_").append(mapType).append(".zip");
			try {
				ZipInputStream zipIn = null;
				InputStream in = null;
				try {
					zipIn = new ZipInputStream(Resource.class.getClassLoader().getResourceAsStream(path + name));
					ZipEntry ze = zipIn.getNextEntry();
					while (ze != null) {
						in = new BufferedInputStream(zipIn);
						String key = ze.getName();
						int pos = key.indexOf(".");
						key = key.substring(0, pos);
						map.put(key, JsonUtils.toBean(IOUtils.toString(in, "UTF-8")));
						ze = zipIn.getNextEntry();
					}
					zipIn.closeEntry();
					zipIn.close();
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				} finally {
					if (zipIn != null) {
						IOUtils.closeQuietly(zipIn);
					}
					if (in != null) {
						IOUtils.closeQuietly(in);
					}
				}
				log.info("load " + mapType + " file:" + name + " ok!");
			} catch (Exception e) {
			}
		} // end for
		log.info("load " + mapType + " count: " + map.size() + " !");
	}
}
