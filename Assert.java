package wcy.usual;

import wcy.usual.crypto.EncryptDigest;

public class Assert
{
public static boolean hasAuth(String cert,String seed)
{
	if(!cert.matches("^[0-9a-f]{128}$")) {
		return false;
	}
	long time=System.currentTimeMillis()/1000L;
	int diff=(int)(time%100);
	if(diff<50) {
		time/=100;
	}else{
		time=time/100+1;
	}
	if(EncryptDigest.digest(seed+time, EncryptDigest.ENC_SHA512).equalsIgnoreCase(cert)) {
		return true;
	}
	if(diff<50){
		time++;
	}else{
		time--;
	}
	if(EncryptDigest.digest(seed+time, EncryptDigest.ENC_SHA512).equalsIgnoreCase(cert)) {
		return true;
	}
	return false;
}
}