// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Encoder.java

package com.rh.core.util.encoder;

import java.io.IOException;
import java.io.OutputStream;

public interface Encoder
{

	public abstract int encode(byte abyte0[], int i, int j, OutputStream outputstream)
		throws IOException;

	public abstract int decode(byte abyte0[], int i, int j, OutputStream outputstream)
		throws IOException;

	public abstract int decode(String s, OutputStream outputstream)
		throws IOException;
}
