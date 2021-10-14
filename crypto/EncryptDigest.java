package wcy.usual.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import wcy.usual.codec.Codec;

public final class EncryptDigest
{
public static byte[] digest(byte[] bytes, byte enctype)
{
	if(null==bytes || 0==bytes.length){
		return null;
	}
	String algorithms=getEncAlgorithms(enctype);
	if(null==algorithms){
		return null;
	}
	MessageDigest digest=null;
	try{
		digest=MessageDigest.getInstance(algorithms);
	}catch(NoSuchAlgorithmException e){
		e.printStackTrace(System.err);
		return null;
	}
	return digest.digest(bytes);
}
public static void main(String[] args){
	String src="q2a46528IqT4XnVu7027qC0M2NCiq4EKU";
	src="echpiL2KPwTkkN9pCxLc0b8FNKX3JuaBnrE0T9ceAxCCaZAS5nbHX3zpxPfIOSbsSBT5FUJqH6hiOoB86vJFFus5K6T8EyNR3LHAFKriOf1Ei16bmwtpOUh72zqS1joCNL5L5M9CZVMhFK59LTqsxHqbDwG8TnwbqAzAGk5Iwh2u5dmx2VgXTe9qiYHrV08w5yvDjZPNhDOjFymjllwTNxr6uBiyn6yGqV2c7y3ul9l9Cdmxb55EPyV8JkMDvRp";
	System.out.println("src");
	//System.out.println("MD2=>"+digest(src,EncryptDigest.ENC_MD2));
	System.out.println("MD5=>"+digest(src,EncryptDigest.ENC_MD5));
	System.out.println("SHA1=>"+digest(src,EncryptDigest.ENC_SHA1));
	System.out.println("SHA224=>"+digest(src,EncryptDigest.ENC_SHA224));
	System.out.println("SHA256=>"+digest(src,EncryptDigest.ENC_SHA256));
	System.out.println("SHA384=>"+digest(src,EncryptDigest.ENC_SHA384));
	System.out.println("SHA512=>"+digest(src,EncryptDigest.ENC_SHA512));
}
public static String digest(String src,byte enctype)
{
	if(null==src || src.length()==0){
		return null;
	}
	String algorithms=getEncAlgorithms(enctype);
	if(null==algorithms) {
		return null;
	}
	byte[] bytes;
	try{
		bytes=src.getBytes("UTF-8");
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
		return null;
	}
	MessageDigest digest=null;
	try{
		digest=MessageDigest.getInstance(algorithms);
	}catch(NoSuchAlgorithmException e){
		e.printStackTrace(System.err);
		return null;
	}
	bytes=digest.digest(bytes);
	return Codec.bit2hex(bytes);
}
protected static String getEncAlgorithms(byte type)
{
	switch(type)
	{
	case ENC_MD2:
		return "MD2";	
	case ENC_MD5:
		return "MD5";
	case ENC_SHA1:
		return "SHA-1";
	case ENC_SHA224:
		return "SHA-224";
	case ENC_SHA256:
		return "SHA-256";
	case ENC_SHA384:
		return "SHA-384";
	case ENC_SHA512:
		return "SHA-512";
	default:
		return null;
	}
}
private EncryptDigest(){}
public static final byte ENC_MD2=0;
public static final byte ENC_MD5=1;
public static final byte ENC_SHA1=2;
public static final byte ENC_SHA224=3;
public static final byte ENC_SHA256=4;
public static final byte ENC_SHA384=5;
public static final byte ENC_SHA512=6;
}