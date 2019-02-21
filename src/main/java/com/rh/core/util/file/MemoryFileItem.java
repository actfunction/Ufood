package com.rh.core.util.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.io.IOUtils;

/**
 * 内存文件项
 * @author wanglong
 * @deprecated - 当上传文件过大时会导致内存溢出. <br>
 * 推荐使用 <code>RhFileItem</code>
 */
public class MemoryFileItem implements FileItem {
    private static final long serialVersionUID = 1L;
    private byte[] bytes;
    /**
     * 内存文件项
     * @param in 输入流
     * @throws Exception 例外
     * 
     */
    public MemoryFileItem(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        out.flush();
        out.close();
        this.bytes = out.toByteArray();
    }
    /**
     * 内存文件项
     * @param bytes 字节
     */
    public MemoryFileItem(byte[] bytes) {
        this.bytes = bytes;
    }
    @Override
    public void delete() {
    }

    @Override
    public byte[] get() {
        return this.bytes;
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
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override
    public long getSize() {
        return this.bytes.length;
    }

    @Override
    public String getString() {
        return new String(this.bytes);
    }

    @Override
    public String getString(String arg0) throws UnsupportedEncodingException {
        return new String(this.bytes, arg0);
    }

    @Override
    public boolean isFormField() {
        return false;
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public void setFieldName(String arg0) {
    }

    @Override
    public void setFormField(boolean arg0) {
    }

    @Override
    public void write(File arg0) throws Exception {
        FileOutputStream out = new FileOutputStream(arg0);
        IOUtils.copy(this.getInputStream(), out);
        out.flush();
        out.close();
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
