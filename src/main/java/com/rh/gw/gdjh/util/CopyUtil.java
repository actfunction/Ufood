package com.rh.gw.gdjh.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.tongtech.protocol.util.ByteArrayOutputStream;

public class CopyUtil {
	public static <T> List<T> deepCopyList(List<T> src) throws IOException, ClassNotFoundException {
		List<T> dest = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(src);
		ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
		ObjectInputStream in = new ObjectInputStream(byteIn);
		dest = (List<T>) in.readObject();
		return dest;
	}
}
