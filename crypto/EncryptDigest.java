package wcy.usual.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import wcy.usual.func.Tool;

public interface EncryptDigest
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
public static String digest(CharSequence src,byte enctype)
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
		bytes=src.toString().getBytes("UTF-8");
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
	return Tool.bit2hex(bytes);
}
private static String getEncAlgorithms(byte type)
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
public static final byte ENC_MD2=0;
public static final byte ENC_MD5=1;
public static final byte ENC_SHA1=2;
public static final byte ENC_SHA224=3;
public static final byte ENC_SHA256=4;
public static final byte ENC_SHA384=5;
public static final byte ENC_SHA512=6;
}