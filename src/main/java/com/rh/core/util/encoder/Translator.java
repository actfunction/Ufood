// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   Translator.java

package com.rh.core.util.encoder;


public interface Translator
{

	public abstract int getEncodedBlockSize();

	public abstract int encode(byte abyte0[], int i, int j, byte abyte1[], int k);

	public abstract int getDecodedBlockSize();

	public abstract int decode(byte abyte0[], int i, int j, byte abyte1[], int k);
}
