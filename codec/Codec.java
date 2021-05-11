package wcy.usual.codec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wcy.usual.codec.json.JsonSerializer;
import wcy.usual.codec.xml.XmlSerializer;

public final class Codec
{
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
}