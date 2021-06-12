package wcy.usual;

import java.util.Iterator;
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
public static <T> String joinList(Iterable<T> its,CharSequence join)
{
	Iterator<T> itr=its.iterator();
	StringBuilder sb=new StringBuilder();
	if(null==join || join.length()==0){
		while(itr.hasNext()){
			T o=itr.next();
			if(null==o){
				continue;
			}
			if((o instanceof CharSequence) && ((CharSequence)o).length()==0){
				continue;
			}
			sb.append(o.toString());
		}
	}else{
		while(itr.hasNext()){
			T o=itr.next();
			if(null==o){
				continue;
			}
			if((o instanceof CharSequence) && ((CharSequence)o).length()==0){
				continue;
			}
			sb.append(join).append(o.toString());
		}
		if(sb.length()!=0){
			sb.delete(0,join.length());
		}
	}
	return sb.length()==0?null:sb.toString();
}
}