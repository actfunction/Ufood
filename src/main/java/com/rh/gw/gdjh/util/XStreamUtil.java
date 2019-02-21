package com.rh.gw.gdjh.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class XStreamUtil {
	private static Log log = LogFactory.getLog(XStreamUtil.class);
	private static XStream objectToXml = null;
	
	private static ConcurrentHashMap<String,XStream> xmlToObjectMap=new ConcurrentHashMap<String, XStream>();
	
	private static final List <String> DUPLICATE_AKUAS_LIST=new ArrayList<String>(3);
	
	private static final String SINGLE_ALIAS="singleAlias";
	
	public static String objectToXml(Object obj) {
		
		
		return objectToXml.toXML(obj);
		
	}
	public static Object xmlToObject(String xml,Class<?> clazz) {
		XStream xStream=checkDuplicateAliasClassXStream(clazz);
		xStream.processAnnotations(clazz);
		
		return xStream.fromXML(xml);
		
	}
	public static void stringToXml(String path,String Obj) {
		 SAXReader saxReader = new SAXReader();
			Document document;
			try {
				document = (Document) saxReader.read(new ByteArrayInputStream(Obj.toString().getBytes("UTF-8")));
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(new FileWriter(new File(path)), format);
				writer.write(document);
				writer.close();
			} catch (Exception e) {
				log.error("异常信息："+e.getMessage(), e);
			}
	}
	
	/**
	 * 将xml文件转化为字符串
	 * @title: xmlToString
	 * @descriptin: TODO
	 * @param @param path 文件路径
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String xmlToString(String path) {
		SAXReader saxReader = new SAXReader();
		Document document;
		String xmlString = "";
		try {
			document = (Document) saxReader.read(new File(path));
			xmlString = document.asXML();
		} catch (Exception e) {
			xmlString = "";
			log.error("异常信息："+e.getMessage(), e);
		}
		return xmlString;
	}
	/**
	 * 
	 * @title: xmlFileToString 将xml文件转化为字符串
	 * @descriptin: TODO
	 * @param @param file xml文件 
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String xmlFileToString(File file) {
		SAXReader saxReader = new SAXReader();
		Document document;
		String xmlString = "";
		try {
			document = (Document) saxReader.read(file);
			xmlString = document.asXML();
		} catch (Exception e) {
			e.printStackTrace();
			xmlString = "";
		}
		return xmlString;
	}
	/**
	 * 将xml文件转化为String
	 * @title: xmlToString
	 * @descriptin: TODO
	 * @param @param path 文件url地址
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String xmlToString(URL path) {
		SAXReader saxReader = new SAXReader();
		Document document;
		String xmlString = "";
		try {
			document = (Document) saxReader.read(path);
			xmlString = document.asXML();
		} catch (Exception e) {
			xmlString = "";
			log.error("异常信息："+e.getMessage(), e);
		}
		return xmlString;
	}
	private static XStream checkDuplicateAliasClassXStream(Class<?> clazz) {
		XStream xStream=null;
		XStreamAlias classAlias=(XStreamAlias)clazz.getDeclaredAnnotation(XStreamAlias.class);
		if((classAlias!=null)&&(DUPLICATE_AKUAS_LIST.contains(classAlias.value()))) {
			xStream =(XStream)xmlToObjectMap.get(clazz.getName());
			if(xStream==null) {
				xStream=new ETLXstream();
				XStream  exist=(XStream)xmlToObjectMap.putIfAbsent(clazz.getName(),xStream);
				if(exist!=null) {
					xStream=exist;
				}
 			}
			
		}else {
			xStream=(XStream)xmlToObjectMap.get(SINGLE_ALIAS);
		}
		return xStream;
	}
	static {
	    objectToXml=new XStream(new XppDriver(new XmlFriendlyNameCoder("_","_")));
	    objectToXml.autodetectAnnotations(true);
	    DUPLICATE_AKUAS_LIST.add("record");
	    xmlToObjectMap.put(SINGLE_ALIAS, new ETLXstream());
	}

}
