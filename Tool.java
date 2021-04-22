package wcy.usual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public static List<OgnlNode> parseOgnl(CharSequence ognl)
{//[1].persons[0].6001[3][2].author.teacher.name.0
	if(null==ognl || ognl.length()==0){
		System.err.println("idx=>null");
		return null;
	}
	if(ognl.charAt(0)=='.'){
		System.err.println("idx=>0");
		return null;
	}
	System.out.println(ognl);
	List<OgnlNode> rdat=new ArrayList<OgnlNode>();
	String src=ognl.toString();
	final int initval=src.length();
	int idxbrackets=initval,idxpoint=initval;
	boolean first=true;
	loop:for(int i=0;i!=initval;i++)
	{
		char chr=src.charAt(i);
		switch(chr)
		{
		case '[':
			if(initval!=idxbrackets){
				System.err.println("idx=>"+i);
				return null;
			}
			if(initval==idxpoint){
				if(0!=i && first){
					rdat.add(new OgnlNode(src.substring(0,i),char.class));
				}
			}else{
				if(i==1+idxpoint){
					System.err.println("idx=>"+i);
					return null;
				}
				rdat.add(new OgnlNode(src.substring(1+idxpoint,i),char.class));
				idxpoint=initval;
			}
			first=false;
			idxbrackets=i;
			continue loop;
		case ']':
			if(initval==idxbrackets){
				System.err.println("idx=>"+i);
				return null;
			}
			if(i==1+idxbrackets){
				System.err.println("idx=>"+i);
				return null;
			}
			rdat.add(new OgnlNode(src.substring(1+idxbrackets,i),int.class));
			idxbrackets=initval;
			continue loop;
		case '.':
			if(initval!=idxbrackets){
				System.err.println("idx=>"+i);
				return null;
			}
			if(initval==idxpoint){
				if(first){
					rdat.add(new OgnlNode(src.substring(0,i),char.class));
					first=false;
				}
				idxpoint=i;
				continue loop;
			}
			if(i==1+idxpoint){
				System.err.println("idx=>"+i);
				return null;
			}
			first=false;
			rdat.add(new OgnlNode(src.substring(1+idxpoint,i),char.class));
			idxpoint=i;
			continue loop;
		default:
			continue loop;
		}
	}//[1].persons[0].6001[3][2].author.teacher.name.0
	if(initval!=idxbrackets){
		System.err.println("idx=>len");
		return null;
	}
	if(initval!=idxpoint){
		if(1+idxpoint==initval){
			System.err.println("idx=>len");
			return null;
		}
		rdat.add(new OgnlNode(src.substring(1+idxpoint,initval),char.class));
	}
	return rdat;
}
}