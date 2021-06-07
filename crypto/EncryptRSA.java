package wcy.usual.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import wcy.usual.codec.Codec;

public final class EncryptRSA
{
public final boolean hasError;
	
public final RSAPrivateKey priKey;
public final RSAPublicKey pubKey;
	
public byte[] encrypt(byte[] srcBytes)
{
	if(null==srcBytes || 0==srcBytes.length){
		return null;
	}
	Cipher cipher;
	try{
		cipher=Cipher.getInstance("RSA");
	}catch(Exception e){
		e.printStackTrace(System.err);
		return null;
	}
	try{
		cipher.init(Cipher.ENCRYPT_MODE,pubKey);
	}catch(InvalidKeyException e){
		e.printStackTrace(System.err);
		return null;
	}
	byte[] resultBytes;
	try {
		resultBytes=cipher.doFinal(srcBytes);
	} catch (Exception e) {
		e.printStackTrace(System.err);
		return null;
	}
	return resultBytes;
}
public String encrypt(String srcString)
{
	if(null==srcString || srcString.length()==0){
		return null;
	}
	byte[] bytes;
	try{
		bytes=srcString.getBytes("UTF-8");
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
		return null;
	}
	bytes=encrypt(bytes);//加密RSA
	return null==bytes ? null : Codec.bit2hex(bytes);
}
	
public byte[] decrypt(byte[] srcBytes)
{
	if (null==srcBytes || 0==srcBytes.length){
		return null;
	}
	Cipher cipher;
	try{
		cipher=Cipher.getInstance("RSA");
	}catch(Exception e){
		e.printStackTrace(System.err);
		return null;
	}
	try{
		cipher.init(Cipher.DECRYPT_MODE,priKey);
	}catch (InvalidKeyException e){
		e.printStackTrace(System.err);
		return null;
	}
	byte[] resultBytes;
	try {
		resultBytes=cipher.doFinal(srcBytes);
	}catch(Exception e){
		e.printStackTrace(System.err);
		return null;
	}
	return resultBytes;
}
public String decrypt(String srcString)
{
	if (null==srcString || srcString.length()==0) {
		return null;
	}
	byte[] bytes=Codec.hex2bit(srcString);
	bytes=decrypt(bytes);//解密RSA
	if(null==bytes){
		return null;
	}
	try{
		return new String(bytes,"UTF-8");
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
	}
	return null;
}

public EncryptRSA(CharSequence publicFile,CharSequence privateFile)
{
	File pubKeyFile=new File(publicFile.toString());
	File priKeyFile=new File(privateFile.toString());
	if(pubKeyFile.exists() && priKeyFile.exists())
	{
		pubKey=(RSAPublicKey)loadKey(pubKeyFile);
        if(null==pubKey)
        {
        	hasError=true;
        	priKey=null;
    		return;
		}
        priKey=(RSAPrivateKey)loadKey(priKeyFile);
        if (null==priKey)
        {
        	hasError=true;
        	return;
		}
        hasError=false;
	}
	else
	{
        if(pubKeyFile.exists())
        {
        	if(!pubKeyFile.delete())
        	{
        		hasError=true;
        		pubKey=null;
        		priKey=null;
        		return;
			}
    	}
        if(priKeyFile.exists())
        {
			if(!priKeyFile.delete())
			{
				hasError=true;
				pubKey=null;
				priKey=null;
        		return;
			}
		}
        KeyPair keyPair=null;
    	try
    	{
    		KeyPairGenerator keyPairGen=KeyPairGenerator.getInstance("RSA");
    		keyPairGen.initialize(1024);
    		keyPair=keyPairGen.generateKeyPair();
    	}
    	catch(NoSuchAlgorithmException e)
    	{
    		e.printStackTrace(System.err);
    		hasError=true;
    		pubKey=null;
    		priKey=null;
    		return;
    	}
    	priKey=(RSAPrivateKey)keyPair.getPrivate();
    	pubKey=(RSAPublicKey)keyPair.getPublic();
        if(!storageKey(pubKey,pubKeyFile))
        {
        	hasError=true;
        	return;
		}
        if (!storageKey(priKey,priKeyFile))
        {
            hasError=true;
        	return;
		}
        hasError = false;
	}
}
	
protected Serializable loadKey(File keyFile)
{
	InputStream in;
	try{
		in=new FileInputStream(keyFile);
	}catch(FileNotFoundException e){
		e.printStackTrace(System.err);
		return null;
	}
	ObjectInputStream ois;
	try{
		ois=new ObjectInputStream(in);
	}catch(IOException e){
		try{
			in.close();
		}catch (IOException e2){
			e2.printStackTrace(System.err);
		}
		e.printStackTrace(System.err);
		return null;
	}
	Serializable key=null;
	try{
		key=(Serializable)ois.readObject();
	}catch(Exception e){
		e.printStackTrace(System.err);
	}finally{
		try{
			in.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
		try{
			ois.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
	}
	return key;
}
protected boolean storageKey(Serializable key, File keyFile)
{   
	try{
		keyFile.createNewFile();
	}catch(IOException e){
		e.printStackTrace(System.err);
		return false;
	}
	OutputStream out;
	try {
		out=new FileOutputStream(keyFile);
	} catch(FileNotFoundException e){
		e.printStackTrace(System.err);
		keyFile.delete();
		return false;
	}
	ObjectOutputStream oos;
	try{
		oos=new ObjectOutputStream(out);
	}catch(IOException e){
		try{
			out.close();
		}catch (Exception e2){
			e2.printStackTrace(System.err);
		}
		keyFile.delete();
		e.printStackTrace(System.err);
		return false;
	}
	boolean ret=true;
	try{
		oos.writeObject(key);
		oos.flush();
	}catch(IOException e){
		keyFile.delete();
		e.printStackTrace(System.err);
		ret=false;
	}finally{
		try{
			out.close();
		}catch(IOException e2){
			ret=false;
			e2.printStackTrace(System.err);
		}
		try{
			oos.close();
		}catch (IOException e2){
			ret=false;
			e2.printStackTrace(System.err);
		}
	}
	return ret;
}
}