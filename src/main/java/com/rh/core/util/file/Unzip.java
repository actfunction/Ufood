package com.rh.core.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 解压文件
 * @author wanglong
 * 
 */
public class Unzip {
    
    /**
     * 把一个zip文件解压到一个指定的目录中
     * 
     * @param zipfilename zip文件抽象地址
     * @param outputdir 目录绝对地址
     * @exception IOException IO错误
     * @return 解压后的所有文件的列表
     */
    public ArrayList<File> unzip(String zipfilename, String outputdir) throws IOException {
        File zipfile = new File(zipfilename);
        return unzip(zipfile, outputdir);
    }    

    /**
     * 解压文件到指定目录
     * @param zipFile 压缩文件
     * @param outputdir 保存解压后文件的目录
     * @exception IOException IO错误
     * @return 解压后的所有文件的列表
     */
    public ArrayList<File> unzip(File zipFile, String outputdir) throws IOException {
        ArrayList<File> fileList = new ArrayList<File>();

        if (!zipFile.exists()) {
            throw new FileNotFoundException(zipFile.getAbsolutePath());
        }

        outputdir = outputdir + File.separator;

        ZipFile zf = null;
        try {
            FileUtils.forceMkdir(new File(outputdir));

            zf = new ZipFile(zipFile, "GBK");
            Enumeration<ZipArchiveEntry> zipArchiveEntrys = zf.getEntries();
            while (zipArchiveEntrys.hasMoreElements()) {
                ZipArchiveEntry entry = zipArchiveEntrys
                        .nextElement();
                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(new File(outputdir + entry.getName()
                            + File.separator));
                } else {
                    File outFile = new File(outputdir + entry.getName());
                    unzipOneFile(zf, outFile, entry);
                    fileList.add(outFile);
                }
            }

            zf.close();
            zf = null;
        } finally {
            if (zf != null) {
                zf.close();
                zf = null;
            }
        }

        return fileList;

    }

    /**
     * @param zf 压缩文件
     * @param outFile 目标文件
     * @param entry zip归档实例
     * @throws IOException IO错误
     */
    private void unzipOneFile(ZipFile zf, File outFile, ZipArchiveEntry entry) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {

            FileUtils.forceMkdir(outFile.getParentFile());

            in = zf.getInputStream(entry);

            out = FileUtils.openOutputStream(outFile);

            IOUtils.copy(in, out);

            in.close();
            in = null;

            out.close();
            out = null;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 如果ZIP中只有一个文件，那么把这个文件解压到目标位置
     * 
     * @param zipFile zip文件
     * @param targetFile 目标文件
     * @exception IOException IO错误
     */
    public void decompressSingleFile(File zipFile, File targetFile) throws IOException {
        if (!zipFile.exists()) {
            throw new FileNotFoundException(zipFile.getAbsolutePath());
        }

        ZipFile zf = null;
        try {
            FileUtils.forceMkdir(targetFile.getParentFile());

            zf = new ZipFile(zipFile, "GBK");
            Enumeration<ZipArchiveEntry> zipArchiveEntrys = zf.getEntries();
            while (zipArchiveEntrys.hasMoreElements()) {
                ZipArchiveEntry entry = zipArchiveEntrys
                        .nextElement();
                if (!entry.isDirectory()) {
                    unzipOneFile(zf, targetFile, entry);
                    break;
                }
            }

            zf.close();
            zf = null;
        } finally {
            if (zf != null) {
                zf.close();
                zf = null;
            }
        }
    }




}
