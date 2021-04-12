package wcy.usual.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wcy.usual.NotFinal;

import java.util.Set;

public final class JsonSerializer
{
protected static CharSequence serializeArray(Object parr)
{
	final int arrlen=Array.getLength(parr);
	if(0==arrlen){
		return null;
	}
	final StringBuilder jsonBuilder=new StringBuilder("[");
	CharSequence tmpValue=null;
	for(int i=0;arrlen!=i;i++){
		tmpValue=serialize(Array.get(parr,i),true,true);
		if(null!=tmpValue){
			jsonBuilder.append(tmpValue).append(',');
		}
	}
	if(jsonBuilder.length()==1){
		return null;
	}
	return jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append(']');
}
@Deprecated
protected static CharSequence serializeAnnotation(Annotation pclass)
{
	return null;
}
protected static CharSequence serializeMap(Map<?,?> pmap)
{
	Set<?> eset = pmap.entrySet();
	if(eset.size()==0){
		return null;
	}
	Iterator<?> itor = eset.iterator();
	Entry<?,?> etr=null;
	CharSequence kstr,vstr;
	Object tmpValue=null;
	StringBuilder jsonBuilder=new StringBuilder("{");
	do{
		etr=(Entry<?,?>)itor.next();
		tmpValue=etr.getKey();
		if(null==tmpValue){
			continue;
		}
		kstr=serialize(tmpValue,true,false);
		if(null==kstr){
			continue;
		}
		tmpValue=etr.getValue();
		if(null==tmpValue){
			continue;
		}
		vstr=serialize(tmpValue,true,true);
		if(null==vstr){
			continue;
		}
		jsonBuilder.append(kstr).append(':').append(vstr).append(',');
	}while(itor.hasNext());
	if(jsonBuilder.length()==1){
		return null;
	}
	return jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append('}');
}
protected static CharSequence serializeIterable(Iterable<?> piterator)
{
	Iterator<?> iterator=piterator.iterator();
	if(!iterator.hasNext()){
		return null;
	}
	StringBuilder jsonBuilder=new StringBuilder("[");
	CharSequence tmpValue=null;
	do{
		tmpValue=serialize(iterator.next(),true,true);
		if(null!=tmpValue){
			jsonBuilder.append(tmpValue).append(',');
		}
	}while (iterator.hasNext());
	if(jsonBuilder.length()==1){
		return null;
	}
	return jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append(']');
}
protected static void getMembers(List<Method> validMds,List<Method> elideMds,List<Field> validFds,List<Field> elideFds,Class<?> pclass,boolean asInstance)
{
	Method[] lmds=pclass.getDeclaredMethods();
	if(null!=lmds && 0!=lmds.length)
	{
		loopmd:for(Method ltmd:lmds)
		{
			int sign=ltmd.getModifiers();
			if(Modifier.isStatic(sign)==asInstance){
				continue;
			}
			if(ltmd.getParameterCount()!=0){
				continue;
			}
			if(ltmd.getReturnType()==void.class||ltmd.getReturnType()==Void.class){
				continue;
			}
			if(Modifier.isAbstract(sign)){
				continue;
			}
			Require require=ltmd.getAnnotation(Require.class);
			final String mname=ltmd.getName();
			if(null==require){//没成员注解
				if(!Modifier.isPublic(sign)){
					continue loopmd;
				}
				for(Method limd:elideMds){
					if(limd.getName().equals(mname)){//已经被忽略,不添加
						continue loopmd;
					}
				}
				for(Method limd:validMds){
					if(limd.getName().equals(mname)){//已经被添加,不重复添加
						continue loopmd;
					}
				}
				validMds.add(ltmd);
			}else{//有成员注解
				for(Method limd:elideMds){
					if(limd.getName().equals(mname)){//已经被忽略,不添加
						continue loopmd;
					}
				}
				for(Method limd:validMds){
					if(limd.getName().equals(mname)){//已经被添加,不重复添加
						continue loopmd;
					}
				}
				@SuppressWarnings("unused")
				boolean b=require.value() ? validMds.add(ltmd) : elideMds.add(ltmd);
			}//有成员注解
		}//for method
	}
	Field[] lfds=pclass.getDeclaredFields();
	if(null==lfds || 0==lfds.length){
		return;
	}
	loopfds:for(Field ltfd:lfds)
	{
		int sign=ltfd.getModifiers();
		if(Modifier.isStatic(sign)==asInstance){
			continue;
		}
		Require require=ltfd.getAnnotation(Require.class);
		final String fname=ltfd.getName();
		if(null==require){//没成员注解
			if(!Modifier.isPublic(sign)){
				continue loopfds;
			}
			for(Field lifd:elideFds){
				if(lifd.getName().equals(fname)){//已经被忽略,不添加
					continue loopfds;
				}
			}
			for(Field lifd:validFds){
				if(lifd.getName().equals(fname)){//已经被添加,不重复添加
					continue loopfds;
				}
			}
			validFds.add(ltfd);
		}else{//有成员注解
			for(Field lifd:elideFds){
				if(lifd.getName().equals(fname)){//已经被忽略,不添加
					continue loopfds;
				}
			}
			for(Field lifd:validFds){
				if(lifd.getName().equals(fname)){//已经被添加,不重复添加
					continue loopfds;
				}
			}
			@SuppressWarnings("unused")
			boolean b=require.value() ? validFds.add(ltfd) : elideFds.add(ltfd);
		}//有成员注解
	}//for field
}
private static CharSequence serializeObject(Object pobj)
{
	boolean asInstance=pobj instanceof Class;
	List<Method> allValidMds=new ArrayList<>(),allIgnoreMds=new ArrayList<>();
	List<Field> allValidFds=new ArrayList<>(),allIgnoreFds=new ArrayList<>();
	Class<?> lclass=null;
	if(asInstance){
		lclass=pobj.getClass();
	}else{
		lclass=(Class<?>) pobj;
	}
	NotFinal nofinal=null;
	do{
		getMembers(allValidMds,allIgnoreMds,allValidFds,allIgnoreFds,lclass,asInstance);
		nofinal=lclass.getDeclaredAnnotation(NotFinal.class);
		lclass=lclass.getSuperclass();
	}while(lclass!=null && (Object.class!=lclass || null==nofinal));
	List<String> mnms=new ArrayList<String>(allValidMds.size()),fnms=new ArrayList<String>(allValidFds.size());
	for(int i=0,len=allValidMds.size();i!=len;i++)
	{
		Method md=allValidMds.get(i);
		Require require=md.getAnnotation(Require.class);
		if(null==require){
			mnms.add(calcJsonName(md));
		}else if(require.toJson().length()==0){
			mnms.add(calcJsonName(md));
		}else{
			mnms.add(require.toJson());
		}
	}
	for(int i=0,len=allValidFds.size();i!=len;i++)
	{
		Field fd=allValidFds.get(i);
		Require require=fd.getAnnotation(Require.class);
		if(null==require){
			fnms.add(fd.getName());
		}else if(require.toJson().length()==0){
			fnms.add(fd.getName());
		}else{
			fnms.add(require.toJson());
		}
	}
	for(int j=0,jlen=allValidFds.size();j!=jlen;j++)
	{
		String fnm=fnms.get(j);
		for(int i=0,ilen=allValidMds.size();i!=ilen;)
		{
			if(mnms.get(i).equals(fnm))
			{
				mnms.remove(i);
				allValidMds.remove(i);
				ilen-=1;
			}else{
				i+=1;
			}
		}
	}
	StringBuilder jsonBuilder=new StringBuilder("{");
	Object returnValue=null;
	for(int i=0,len=allValidMds.size();i!=len;i++)
	{
		Method md=allValidMds.get(i);
		md.setAccessible(true);
		try{
			returnValue=md.invoke(pobj);
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			e.printStackTrace(System.err);
		}
		if(null==returnValue){
			continue;
		}
		CharSequence chars=serialize(returnValue);
		if(null==chars){
			continue;
		}
		jsonBuilder.append('"').append(toJsonStandard(mnms.get(i))).append("\":").append(chars).append(',');
	}
	for(int i=0,len=allValidFds.size();i!=len;i++)
	{
		Field fd=allValidFds.get(i);
		fd.setAccessible(true);
		try{
			returnValue=fd.get(pobj);
		}catch(IllegalArgumentException | IllegalAccessException e){
			e.printStackTrace(System.err);
		}
		if(null==returnValue){
			continue;
		}
		CharSequence chars=serialize(returnValue);
		if(null==chars){
			continue;
		}
		jsonBuilder.append('"').append(toJsonStandard(fnms.get(i))).append("\":").append(chars).append(',');
	}
	if(jsonBuilder.length()==1){
		return null;
	}
	return  jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append('}');
}
protected static String calcJsonName(Method func)
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
private static CharSequence serializeClass(Class<?> pclass)
{
	final StringBuilder jsonBuilder=new StringBuilder();
	return null;
}
private static CharSequence serializeEnum(Class<?> penum)
{
	Object[] enums=penum.getEnumConstants();
	if(null==enums||0==enums.length){
		return null;
	}
	final StringBuilder jsonBuilder=new StringBuilder("[");
	for(int i=0;enums.length!=i;i++){
		jsonBuilder.append('"').append(enums[i].toString()).append("\",");
	}
	return jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append(']');
}
public static CharSequence serialize(Object pobj,boolean asInstance,boolean asJsonValue)
{
	Class<?> clazz=null;
	if(pobj instanceof Class){
		clazz=(Class<?>)pobj;
		if(asInstance){
			if(asJsonValue){
				return serializeClass(clazz).toString();
			}
			return '"'+clazz.getName()+'"';
		}
		if(asJsonValue){
			if(clazz.isEnum()){
				return serializeEnum(clazz);
			}
			return serializeObject(clazz);
		}
		return '"'+clazz.getName()+'"';
	}
	clazz=pobj.getClass();
	if(Character.class==clazz){
		return "\""+ toJsonStandard(String.valueOf((char)pobj))+'"';
	}
	if(asJsonValue){
		if(Boolean.class==clazz){
			return String.valueOf((boolean)pobj);
		}
		if(Byte.class==clazz){
			return String.valueOf((byte)pobj);
		}
		if(Short.class==clazz){
			return String.valueOf((short)pobj);
		}
		if(Integer.class==clazz){
			return String.valueOf((int)pobj);
		}
		if(Long.class==clazz){
			return String.valueOf((long)pobj);
		}
		if(Float.class==clazz){
			return String.valueOf((float)pobj);
		}
		if(Double.class==clazz){
			return String.valueOf((double)pobj);
		}
	}else{
		if(Boolean.class==clazz){
			return '"'+String.valueOf((boolean)pobj)+'"';
		}
		if(Byte.class==clazz){
			return '"'+String.valueOf((byte)pobj)+'"';
		}
		if(Short.class==clazz){
			return '"'+String.valueOf((short)pobj)+'"';
		}
		if(Integer.class==clazz){
			return '"'+String.valueOf((int)pobj)+'"';
		}
		if(Long.class==clazz){
			return '"'+String.valueOf((long)pobj)+'"';
		}
		if(Float.class==clazz){
			return '"'+String.valueOf((float)pobj)+'"';
		}
		if(Double.class==clazz){
			return '"'+String.valueOf((double)pobj)+'"';
		}
	}
	if(pobj instanceof CharSequence){
		return '"'+toJsonStandard((CharSequence)pobj).toString()+'"';
	}
	if(pobj instanceof JsonSerializable){
		if(asJsonValue) {
			return ((JsonSerializable)pobj).toJsonValue();
		}
		return ((JsonSerializable)pobj).toJsonKey();
	}
	if(clazz.isAnnotation()){
		if(asJsonValue){
			return serializeAnnotation((Annotation) pobj);
		}
		return '"'+clazz.getName()+'"';
	}
	if(clazz.isArray()){
		if(asJsonValue){
			return serializeArray(pobj);
		}
		return '"'+clazz.getName()+'"';
	}
	if(clazz.isEnum()){
		return '"'+pobj.toString()+'"';
	}
	if(pobj instanceof Iterable<?>){
		if(asJsonValue){
			return serializeIterable((Iterable<?>) pobj);
		}
		return '"'+clazz.getName()+'"';
	}
	if(pobj instanceof Map<?,?>){
		if(asJsonValue){
			return serializeMap((Map<?,?>)pobj);
		}
		return '"'+clazz.getName()+'"';
	}
	return serializeObject(pobj);//if(clazz.isInterface())
}
public static CharSequence serialize(Object pobj)
{
	return serialize(pobj,true,true);
}
public static CharSequence toJsonStandard(CharSequence chars)
{
	StringBuilder hexBuilder=new StringBuilder();
	String hexString=null;
	charsfor:for(int i=0,len=chars.length();len!=i;i++)
	{
		char char1=chars.charAt(i);
		if((47<char1&&char1<58)||(64<char1&&char1<91)||(96<char1&&char1<123)){
			hexBuilder.append(char1);
			continue;
		}
		switch(char1){
		case '\b':
			hexBuilder.append('\\').append('b');
			continue charsfor;
		case '\f':
			hexBuilder.append('\\').append('f');
			continue charsfor;
		case '\n':
			hexBuilder.append('\\').append('n');
			continue charsfor;
		case '\r':
			hexBuilder.append('\\').append('r');
			continue charsfor;
		case '\t':
			hexBuilder.append('\\').append('t');
			continue charsfor;
		case '/':
		case '\\':
		case '"':
			hexBuilder.append('\\').append(char1);
			continue charsfor;
		case ' ':
		case '+':
		case '=':
		case '-':
		case ')':
		case '(':
		case '*':
		case '&':
		case '^':
		case '%':
		case '$':
		case '#':
		case '@':
		case '!':
		case '`':
		case '~':
		case '.':
		case ',':
		case '>':
		case '<':
		case ':':
		case ';':
		case '_':
		case '|':
		case '\'':
		case '?':
			hexBuilder.append(char1);
			continue charsfor;
		}
		hexString=Integer.toHexString(char1);
		while(hexString.length()!=4){
			hexString='0'+hexString;
		}
		hexBuilder.append("\\u"+hexString);
	}
	return hexBuilder;
}
public static Object parseJson(CharSequence src)
{
	return null;
}
}