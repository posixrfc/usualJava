package wcy.usual.crypto;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public final class EncryptBase64
{
public byte[] encode(byte[] bytes)
{
	if (null == bytes || 0 == bytes.length){
		return null;
	}
	Encoder encoder=Base64.getEncoder();		
	bytes = encoder.encode(bytes);
	return bytes;
}
public String encode(String src)
{
	if(null==src || 0==src.length()){
		return null;
	}
	byte[] bytes=null;
	try {
		bytes=src.getBytes("UTF-8");
	} catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
		return null;
	}		
	Encoder encoder=Base64.getEncoder();
	bytes=encoder.encode(bytes);
	String ret=null;
	try{
		ret=new String(bytes,"ASCII");
	}catch (UnsupportedEncodingException e){
		e.printStackTrace(System.err);
	}
	return ret;
}
public byte[] decode(byte[] bytes)
{
	if (null==bytes || 0==bytes.length){
		return null;
	}
	Decoder decoder=Base64.getDecoder();
	bytes=decoder.decode(bytes);
	return bytes;
}
public String decode(String src)
{
	if (null==src || 0==src.length()){
		return null;
	}
	byte[] bytes=null;
	try{
		bytes=src.getBytes("ASCII");
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
		return null;
	}
	Decoder decoder=Base64.getDecoder();
	bytes=decoder.decode(bytes);
	String ret=null;
	try{
		ret=new String(bytes, "UTF-8");
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
		return null;
	}
	return ret;
}
public String transformEncode(String src)
{
	if(null==src){
		return null;
	}
	if(needEscape)
	{
		src=src.replace('+',signPlus);
		src=src.replace('/',signSlash);
		src=src.replace('=',signEqual);
	}
	return src;
}
public String transformDecode(String src)
{
	if(null==src){
		return null;
	}
	if(needEscape)
	{
		src=src.replace(signPlus,'+');
		src=src.replace(signSlash,'/');
		src=src.replace(signEqual,'=');
	}
	return src;
}
	
public EncryptBase64(char plusSign,char slashSign,char equalSign)
{
	signPlus=plusSign;
	signSlash=slashSign;
	signEqual=equalSign;
	needEscape=true;
}
public EncryptBase64(){
	needEscape=false;
}
protected char signPlus,signSlash,signEqual;
protected final boolean needEscape;
}