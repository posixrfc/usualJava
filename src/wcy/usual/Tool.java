package wcy.usual;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public final class Tool
{
public static String randomCharSecquence(CharSequence src)
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
public static String getRandomString(String source, final int length)
{
	if (1 > length) {
		return null;
	}
	if (null == source || source.length() == 0){
		source = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";//62
	}
	final int strlen = source.length();
	char[] ret=new char[length];
	Random rm = new Random();
	for (int i = 0; i != length; i++) {
		ret[i]=source.charAt(rm.nextInt(strlen));
	}
	return new String(ret);
}
public static <T> String arrJoin(T[] arr,String join)
{
	if(null==arr || 0==arr.length) {
		return null;
	}
	StringBuilder sb=new StringBuilder();
	if(null==join || join.length()==0) {
		for(int i=0;i!=arr.length;i++) {
			if(null==arr[i]) {
				continue;
			}
			if((arr[i] instanceof CharSequence) && ((CharSequence)arr[i]).length()==0) {
				continue;
			}
			sb.append(arr[i].toString());
		}
	}else{
		for(int i=0;i!=arr.length;i++) {
			if(null==arr[i]) {
				continue;
			}
			if((arr[i] instanceof CharSequence) && ((CharSequence)arr[i]).length()==0) {
				continue;
			}
			sb.append(join).append(arr[i].toString());
		}
		sb.delete(0, join.length());
	}
	return sb.length()==0?null:sb.toString();
}
public static byte[] hex2bit(String hex)
{
	int len=hex.length();
	byte bytes[]=new byte[len/2];
	for(int i=0;len!=i;i+=2){
		short twobit=(short)Short.parseShort(hex.substring(i, i+2), 16);
		if(127<twobit) {
			twobit-=256;
		}
		bytes[i/2]=(byte)twobit;
	}
	return bytes;
}
public static String bit2hex(byte[] bytes)
{
	StringBuilder sb=new StringBuilder(bytes.length<<1);
	String hex;
	for(int idx=0;idx!=bytes.length;idx++) {
		hex=Integer.toHexString(bytes[idx] & 0xff);
		sb.append(hex.length()==1?('0'+hex):hex);
	}
	return sb.toString();
}
}