// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Base64.java

package com.rh.core.util.encoder;

import java.io.*;

// Referenced classes of package com.rh.core.util.encoder:
//			Base64Encoder, Encoder

public class Base64
{

	private static final Encoder encoder = new Base64Encoder();

	public Base64()
	{
	}

	public static byte[] encode(byte data[])
	{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try
		{
			encoder.encode(data, 0, data.length, bOut);
		}
		catch (IOException e)
		{
			throw new RuntimeException((new StringBuilder()).append("exception encoding base64 string: ").append(e).toString());
		}
		return bOut.toByteArray();
	}

	public static int encode(byte data[], OutputStream out)
		throws IOException
	{
		return encoder.encode(data, 0, data.length, out);
	}

	public static int encode(byte data[], int off, int length, OutputStream out)
		throws IOException
	{
		return encoder.encode(data, off, length, out);
	}

	public static byte[] decode(byte data[])
	{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try
		{
			encoder.decode(data, 0, data.length, bOut);
		}
		catch (IOException e)
		{
			throw new RuntimeException((new StringBuilder()).append("exception decoding base64 string: ").append(e).toString());
		}
		return bOut.toByteArray();
	}

	public static byte[] decode(String data)
	{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try
		{
			encoder.decode(data, bOut);
		}
		catch (IOException e)
		{
			throw new RuntimeException((new StringBuilder()).append("exception decoding base64 string: ").append(e).toString());
		}
		return bOut.toByteArray();
	}

	public static int decode(String data, OutputStream out)
		throws IOException
	{
		return encoder.decode(data, out);
	}

}
