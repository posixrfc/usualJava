package wcy.usual.func;

import java.util.Random;
import java.util.UUID;

import wcy.usual.crypto.EncryptDigest;

public interface Strings
{
public static String newUUID()
{
	return UUID.randomUUID().toString().replace("-","");
}
public static String md2(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_MD2);
}
public static String md5(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_MD5);
}
public static String sha1(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_SHA1);
}
public static String sha224(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_SHA224);
}
public static String sha256(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_SHA256);
}
public static String sha384(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_SHA384);
}
public static String sha512(CharSequence src){
	return EncryptDigest.digest(src,EncryptDigest.ENC_SHA512);
}
public static String newRandStr(int length,CharSequence source)
{
	if(1>length){
		return null;
	}
	if(null==source){
		source=letterNormal;
	}
	if(source.length()==0){
		source=letterSpecial;
	}
	final int strlen = source.length();
	char[] ret=new char[length];
	Random rm = new Random();
	for(int i=0;i!=length;i++){
		ret[i]=source.charAt(rm.nextInt(strlen));
	}
	return new String(ret);
}
public static String newNumStrMills(int len)
{
	String r=String.valueOf(System.currentTimeMillis());
	while(r.length()<len){
		r+=newNumStrSeed();
	}
	return r.substring(0,len);
}
public static String newNumStrRand(int len)
{
	String r=newNumStrSeed();
	while(r.length()<len){
		r+=newNumStrSeed();
	}
	return r.substring(0,len);
}
private static String newNumStrSeed()
{
	String r=String.valueOf(Math.random());
	while(r.length()<3){
		r=String.valueOf(Math.random());
	}
	return r.substring(2);
}
public static String randChars(CharSequence src)
{
	StringBuilder sb = new StringBuilder(src);
	final int sl = sb.length();
	char[] cs = new char[sl];
	Random rm = new Random();
	for (int i = 0; i != sl; i++)
	{
		int idx = rm.nextInt(sl - i);
		cs[i] = sb.charAt(idx);
		sb.deleteCharAt(idx);
	}
	return new String(cs);
}
public static final String letterNormal="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
public static final String letterSpecial="`~!@#$%^&*()_-=+[]{}\\|:;'\"<>,.?/abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
}