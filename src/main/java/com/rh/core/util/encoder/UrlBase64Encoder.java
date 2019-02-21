// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   UrlBase64Encoder.java

package com.rh.core.util.encoder;


// Referenced classes of package com.rh.core.util.encoder:
//			Base64Encoder

public class UrlBase64Encoder extends Base64Encoder
{

	public UrlBase64Encoder()
	{
		encodingTable[encodingTable.length - 2] = 45;
		encodingTable[encodingTable.length - 1] = 95;
		padding = 46;
		initialiseDecodingTable();
	}
}
