package com.rh.sup.util;

import com.rh.core.util.Lang;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Temporary inputStream data storage
 * 
 * @author liwei
 * 
 */
public class TempFile {

	/** storage type */
	public enum Storage {
		/** file system storage */
		FILE,
		/**
		 * if the file small than 1MB we save into memory,<BR>
		 * else we save as file on disk.
		 * */
		SMART
	}

	/** 1MB */
	private static final int DEFAULT_MAX_MEMORY_FILE_SIZE = 1024 * 1024;
	/** default io buffer */
//	private static final int DEFAULT_IO_BUFF = 4096;;
	/** storage type */
	private Storage storage = null;
	/** output file path on the file system */
	private String outputPath = "";
	/** memoryData, its size will not exceed 1MB */
	private byte[] memoryData = null;
	/** data size */
    private long dataSize = 0;
	/** input stream */
	private InputStream input = null;
	/** open stream list */
	private List<InputStream> openStreamList = new ArrayList<InputStream>();

	/**
	 * can not new instance,with no param
	 */
	@SuppressWarnings("unused")
	private TempFile() {
	}

	/**
	 * new Temp Storage Object
	 * 
	 * @param storageType
	 *            - type
	 * @param in
	 *            - base inputStream data
	 */
	public TempFile(Storage storageType, InputStream in) {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		if (!tmpFolder.endsWith("/")) {
			tmpFolder += "/";
		}
		this.storage = storageType;
		this.input = in;
		this.outputPath = tmpFolder + Lang.getUUID();
	}
	
	public TempFile(Storage storageType) {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		if (!tmpFolder.endsWith("/")) {
			tmpFolder += "/";
		}
		this.storage = storageType;
		this.outputPath = tmpFolder + Lang.getUUID();
	}
	
	public OutputStream getOutputStream() throws FileNotFoundException {
		OutputStream out = new FileOutputStream(outputPath);

		return out;
	}
	

	/**
	 * read inputStream
	 * 
	 * @throws IOException
	 *             - ioexception
	 */
	public void read() throws IOException {
		if (this.storage == Storage.FILE) {
			OutputStream out = new FileOutputStream(outputPath);
			// Write the remainder of the data
			long totalRead = IOUtils.copy(input, out);
			dataSize += totalRead;
			IOUtils.closeQuietly(out);

		} else if (this.storage == Storage.SMART) {
			// read data to the memory
			memoryData = toBytesArray(input, DEFAULT_MAX_MEMORY_FILE_SIZE);
			dataSize = memoryData.length;
			if (memoryData.length > DEFAULT_MAX_MEMORY_FILE_SIZE) {
				OutputStream out = new FileOutputStream(outputPath);
				// copy memory data to output, but do not close stream
				copyBytes(memoryData, out, false);
				dataSize += memoryData.length;
				// clear memoryData
				memoryData = null;
				// Write the remainder of the data
				long totalRead = IOUtils.copy(input, out);
				dataSize += totalRead;
				IOUtils.closeQuietly(out);
			}
		}

		// close stream
		IOUtils.closeQuietly(input);
	}

	/**
	 * open and return new inputStream
	 * 
	 * @return new InputStream
	 * @throws IOException
	 *             - io exception
	 */
	public InputStream openNewInputStream() throws IOException {
		InputStream newIs = null;
		if (null == memoryData) {
			newIs = new FileInputStream(outputPath);
		} else {
			newIs = new ByteArrayInputStream(memoryData);
		}
		openStreamList.add(newIs);
		return newIs;
	}
	
	/**
	 * get date size
	 * @return bytes in size
	 */
	public long getSize() {
	    return dataSize;
	}

	/**
	 * destoroy all open resource <br>
	 * clean memory data and delete temp file
	 * 
	 */
	public void destroy() {
		// close all open stream
		for (InputStream is : openStreamList) {
		    IOUtils.closeQuietly(is);
			is = null;
		}
		openStreamList.clear();

		// clear memory data
		memoryData = null;
		dataSize = 0;

		// delete cache file
		File tmp = new File(outputPath);
		if (tmp.exists()) {
			tmp.delete();
		}
	}
	
	
	   /**
     * Copies bytes between byte array and Stream
     * 
     * @param in
     *            byte[] array
     * @param out
     *            OutputStream
     * @param close
     *            if true, the Streams will be closed
     * @throws IOException
     *             io exception
     */
    private  void copyBytes(byte[] in, OutputStream out, boolean close)
            throws IOException {
        try {
            out.write(in);
            out.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (close) {
                out.close();
            }
        }
    }
    

    /**
     * Reads the data from the input stream, and returns the bytes read.<br>
     * 
     * @param in
     *            - InputStream
     * @param maxSize
     *            - read data max size
     * @return read data byte array
     * @throws IOException
     *             - io exception
     */
    private byte[] toBytesArray(InputStream in, long maxSize)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long totalRead = 0;
        byte[] buffer = new byte[4096];
        int read = 0;
        while (read != -1) {
            read = in.read(buffer);
            if (read > 0) {
                baos.write(buffer, 0, read);
                totalRead += read;
            }
            if (totalRead > maxSize) {
                return baos.toByteArray();
            }
        }
        return baos.toByteArray();
    }



	/**
	 * test code
	 * 
	 * @param args
	 *            - null
	 * @throws IOException
	 *             - io exception
	 */
	public static void main(String[] args) throws IOException {
		String path = "/Users/liwei/Desktop/testdoc/test.ppt";
		InputStream fileIs = new FileInputStream(path);
		TempFile test = new TempFile(Storage.FILE, fileIs);
		test.read();

		OutputStream out = new FileOutputStream(path.replace("testdoc", "testresult"));
		IOUtils.copy(test.openNewInputStream(), out);
		System.out.println("-----end---");

		test.destroy();
	}

}
