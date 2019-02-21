package com.rh.core.util.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;


/**
 * 使用Gzip压缩的Servlet输出流
 * @author wanglong
 * 
 */
public class GZipServletOutputStream extends ServletOutputStream {

    private GZIPOutputStream gzipOutputStream = null;

    /**
     * 
     * @param output 输出流
     * @throws IOException IO异常
     */
    public GZipServletOutputStream(OutputStream output)
            throws IOException {
        super();
        this.gzipOutputStream = new GZIPOutputStream(output);
    }

    @Override
    public void close() throws IOException {
        this.gzipOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        this.gzipOutputStream.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.gzipOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.gzipOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.gzipOutputStream.write(b);
    }

}
