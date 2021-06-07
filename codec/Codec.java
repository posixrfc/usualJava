package wcy.usual.codec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wcy.usual.codec.json.JsonSerializer;
import wcy.usual.codec.xml.XmlSerializer;

public final class Codec
{
public static Map<CharSequence,Object> uri2obj(CharSequence queryString)
{
	Map<CharSequence,Object> res=new HashMap<CharSequence,Object>();
	String[] kvs=queryString.toString().split("&");
	try{
		for(String kv:kvs){
			String[] pair=kv.split("=");
			putMultiVal(res,URLDecoder.decode(pair[0],"UTF-8"),URLDecoder.decode(pair[1],"UTF-8"));
		}
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
	}
	return res;
}
public static CharSequence obj2uri(Map<CharSequence,Object> parameter)
{
	StringBuilder queryString=new StringBuilder();
	try{
		for(Map.Entry<CharSequence,Object> e:parameter.entrySet()){
			String enck=URLEncoder.encode(e.getKey().toString(),"UTF-8"),encv;
			Object value=e.getValue();
			if(value.getClass().isArray()){
				final int len=Array.getLength(value);
				for(int i=0;i!=len;i++){
					encv=URLEncoder.encode(Array.get(value,i).toString(),"UTF-8");
					queryString.append('&').append(enck).append('=').append(encv);
				}
			}else if(value instanceof Iterable){
				for(Object val:(Iterable<?>)value){
					encv=URLEncoder.encode(val.toString(),"UTF-8");
					queryString.append('&').append(enck).append('=').append(encv);
				}
			}else{
				encv=URLEncoder.encode(value.toString(),"UTF-8");
				queryString.append('&').append(enck).append('=').append(encv);
			}
		}
		queryString.deleteCharAt(0);
	}catch(UnsupportedEncodingException e){
		e.printStackTrace(System.err);
	}
	return queryString;
}
public static CharSequence obj2xml(Object any,CharSequence tag)
{
	return XmlSerializer.serialize(any,tag,true,true);
}
public static CharSequence obj2xml(Object any,CharSequence tag,boolean instance,boolean asvalue)
{
	return XmlSerializer.serialize(any,tag,instance,asvalue);
}
public static Object xml2obj(CharSequence xml)
{
	return XmlSerializer.parseXml(xml);
}

public static CharSequence obj2json(Object any)
{
	return JsonSerializer.serialize(any,true,true);
}
public static CharSequence obj2json(Object any,boolean instance,boolean asvalue)
{
	return JsonSerializer.serialize(any,instance,asvalue);
}
public static Object json2obj(CharSequence json)
{
	return JsonSerializer.parseJson(json);
}
public static CharSequence readText(Reader input)
{
	if(null==input){
		return null;
	}
	BufferedReader br=new BufferedReader(input);
	StringBuilder buf=new StringBuilder();
	String line=null;
	try{
		while((line=br.readLine())!=null){
			if(line.length()!=0){
				buf.append(line).append('\n');
			}
		}
	}catch(IOException e){
		buf=null;
		e.printStackTrace(System.err);
	}finally{
		try{
			input.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
		try{
			br.close();
		}catch(IOException e){
			e.printStackTrace(System.err);
		}
	}
	return null==buf || buf.length()==0 ? null : buf;
}
public static CharSequence readText(CharSequence path)
{
	if(null==path || path.length()==0){
		return null;
	}
	Reader is=null;
	try{
		is=new FileReader(path.toString());
	}catch(FileNotFoundException e){
		e.printStackTrace(System.err);
	}
	return null==is ? null : readText(is);
}
@SuppressWarnings("unchecked")
public static void putMultiVal(Map<CharSequence,Object> container,CharSequence key,Object val)
{
	Object tmp=container.get(key);
	if(null==tmp){
		container.put(key,val);
		return;
	}
	if(tmp instanceof List){
		((List<Object>)tmp).add(val);
		return;
	}
	List<Object> list=new ArrayList<Object>();
	list.add(tmp);
	list.add(val);
	container.put(key,list);
}
public static String calcAttrName(Method func)
{
	String mnm=func.getName();
	char char1,char2;
	if(mnm.startsWith("get"))
	{
		int mlen=mnm.length();
		if(3==mlen){
			return mnm;
		}
		char1=mnm.charAt(3);
		if(4==mlen){
			if(64<char1&&char1<91){
				char1=(char)(char1+32);
			}
			return String.valueOf((char)(char1));
		}
		char2=mnm.charAt(4);
		if(64<char2&&char2<91){
			return mnm.substring(3);
		}
		if(64<char1&&char1<91){
			char1=(char)(char1+32);
		}
		return char1+mnm.substring(4);
	}
	if(mnm.startsWith("is"))
	{
		int mlen=mnm.length();
		if(2==mlen){
			return mnm;
		}
		char1=mnm.charAt(2);
		if(3==mlen){
			if(64<char1&&char1<91){
				char1=(char)(char1+32);
			}
			return String.valueOf((char)(char1));
		}
		char2=mnm.charAt(3);
		if(64<char2&&char2<91){
			return mnm.substring(2);
		}
		if(64<char1&&char1<91){
			char1=(char)(char1+32);
		}
		return char1+mnm.substring(3);
	}
	return mnm;
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