package wcy.usual;

import java.util.Random;
import java.util.UUID;

public final class Tool
{
public static String newUUID()
{
	return UUID.randomUUID().toString().replace("-","");
}
public static String newRandNumStr(int len)
{
	String r=String.valueOf(System.currentTimeMillis());
	while(r.length()<len){
		r+=String.valueOf(Math.random()).substring(2);
	}
	return r.substring(0,len);
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
public static String getRandStr(String source, final int length)
{
	if (1 > length){
		return null;
	}
	if(null == source || source.length() == 0){
		source = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";//62
	}
	final int strlen = source.length();
	char[] ret=new char[length];
	Random rm = new Random();
	for (int i = 0; i != length; i++){
		ret[i]=source.charAt(rm.nextInt(strlen));
	}
	return new String(ret);
}
public static <T> String arrJoin(T[] arr,String join)
{
	if(null==arr || 0==arr.length){
		return null;
	}
	StringBuilder sb=new StringBuilder();
	if(null==join || join.length()==0){
		for(int i=0;i!=arr.length;i++){
			if(null==arr[i]){
				continue;
			}
			if((arr[i] instanceof CharSequence) && ((CharSequence)arr[i]).length()==0) {
				continue;
			}
			sb.append(arr[i].toString());
		}
	}else{
		for(int i=0;i!=arr.length;i++){
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
}