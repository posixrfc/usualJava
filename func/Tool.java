package wcy.usual.func;

import java.util.Iterator;

public interface Tool
{
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
public static byte[] hex2bit(String hex)
{
	int len=hex.length();
	byte bytes[]=new byte[len/2];
	for(int i=0;len!=i;i+=2){
		short twobit=(short)Short.parseShort(hex.substring(i,i+2),16);
		if(127<twobit){
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
	for(int idx=0;idx!=bytes.length;idx++){
		hex=Integer.toHexString(bytes[idx] & 0xff);
		sb.append(hex.length()==1?('0'+hex):hex);
	}
	return sb.toString();
}
}