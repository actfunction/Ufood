package com.rh.core.serv.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PkgUtil {

	private Vector<String> exFolder;

	private ArrayList<String> listPath;

	public PkgUtil() {
		exFolder = new Vector<String>();
		listPath = new ArrayList<String>();
	}

	public ArrayList<File> searchFile(File file, String last, String stop, String fileExt[]) {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = new Date(System.currentTimeMillis());
		Date stopDate = new Date(System.currentTimeMillis());
		try {
			if (last != null && last.indexOf("-") != -1)
				startDate = df.parse(last + " 00:00:00");
			if (stop != null && stop.indexOf("-") != -1)
				stopDate = df.parse(stop + " 23:59:59");
		} catch (ParseException e) {
		}

		return searchFile(file, startDate, stopDate, fileExt);
	}

	public ArrayList<File> searchFile(File file, Date last, Date stop, String fileExt[]) {
		return searchFile(file, last.getTime(), stop.getTime(), fileExt);
	}

	public ArrayList<File> searchFile(File file, long start, long stop, String fileExt[]) {
		ArrayList<File> resultList = new ArrayList<File>();
		startSearch(file, start, stop, fileExt, resultList);
		return resultList;
	}

	public void addExFolder(String strFolder) {
		exFolder.add(strFolder);
	}

	public void clearExFolder() {
		exFolder.clear();
	}

	private void startSearch(File file, long start, long stop, String fileExt[], ArrayList<File> resultList) {
		if (stop == 0L)
			stop = System.currentTimeMillis();
		if (file.canRead())
			if (file.isDirectory()) {
				if (!isExFolder(file.getAbsolutePath())) {
					File files[] = file.listFiles();
					for (File f : files) {
						startSearch(f, start, stop, fileExt, resultList);
					}
				}
			} else if (file.lastModified() > start && file.lastModified() <= stop) {
				for (String fext : fileExt) {
					if (file.getName().endsWith(fext)) {
						resultList.add(file);
						continue;
					}
					if (fext.equals(".*"))
						resultList.add(file);
				}
			}
	}

	private boolean isExFolder(String strFolder) {
		for (String exFolder : exFolder) {
			if (strFolder.startsWith(exFolder))
				return true;
		}
		return false;
	}

	public static String checkFile(File file) throws IOException {
		if (!file.exists())
			file.getParentFile().mkdirs();
		return file.getAbsolutePath();
	}

	public ArrayList<String> getAllFilelist(String rootPath) {
		listPath = new ArrayList<String>();
		File file = new File(rootPath);
		eachFilelist(file);
		return listPath;
	}

	private void eachFilelist(File files) {
		for (File file : files.listFiles()) {
			if (file.isDirectory()) {
				eachFilelist(file);
			} else if (file.exists()) {
				listPath.add(file.getAbsolutePath());
			}
		}
	}
}
