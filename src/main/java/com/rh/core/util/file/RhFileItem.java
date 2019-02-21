package com.rh.core.util.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

import com.rh.core.comm.file.TempFile;
import com.rh.core.comm.file.TempFile.Storage;

/**
 * File item 实现， 当文件较小时，我们保存在内存，当文件较大时我们将其持久化至硬盘
 * 
 * @author wanglong
 * 
 */
public class RhFileItem implements FileItem {
    
    /**
     * 
     */
    private static final long serialVersionUID = -2022262770083196815L;
    //file storage
    private TempFile currentFile = null;

    /**
     * 构造函数
     * @param is - inputstream
     */
    public RhFileItem(InputStream is) {
        currentFile = new TempFile(Storage.SMART, is);
        try {
            currentFile.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete() {
        currentFile.destroy();
    }

    @Override
    public byte[] get() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getFieldName() {
        return "file";
    }

    @Override
    public InputStream getInputStream() throws IOException {
       return currentFile.openNewInputStream();
    }

    @Override
    public String getName() {
       return "file";
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getSize() {
        return currentFile.getSize();
    }

    @Override
    public String getString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(String arg0) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFormField() {
        return false;
    }

    @Override
    public boolean isInMemory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setFieldName(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFormField(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void write(File arg0) throws Exception {
        // TODO Auto-generated method stub

    }

	@Override
	public FileItemHeaders getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHeaders(FileItemHeaders headers) {
		// TODO Auto-generated method stub
		
	}

}
