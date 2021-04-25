package wcy.usual.xml;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import wcy.usual.NotFinal;
import wcy.usual.Tool;
import wcy.usual.json.JsonRequire;

public final class XmlSerializer
{
protected static CharSequence serializeArray(Object parr,CharSequence tag)
{
	final int arrlen=Array.getLength(parr);
	if(0==arrlen){
		return null;
	}
	StringBuilder varstr=new StringBuilder();
	for(int i=0;arrlen!=i;i++)
	{
		Object idxval=Array.get(parr,i);
		if(null==idxval){
			continue;
		}
		CharSequence tmpValue=serialize(idxval,tag,true,true);
		if(null==tmpValue){
			continue;
		}
		if(idxval.getClass().isArray() || idxval instanceof Iterable){
			varstr.append('<'+tag.toString()+'>'+tmpValue+"</"+tag+'>');
		}else{
			varstr.append(tmpValue);
		}
	}
	if(varstr.length()==0){
		return null;
	}
	return varstr;
}
protected static CharSequence serializeAnnotation(Object pclass,CharSequence tag)
{
	final StringBuilder jsonBuilder=new StringBuilder();
	return null;
}
protected static CharSequence serializeMap(Map<?,?> pmap,CharSequence tag)
{
	Set<?> eset = pmap.entrySet();
	if(eset.size()==0){
		return null;
	}
	Iterator<?> itor=eset.iterator();
	Entry<?,?> etr=null;
	CharSequence kstr,vstr;
	Object tmpValue=null;
	StringBuilder varstr=new StringBuilder('<'+tag.toString()+'>');
	do{
		etr=(Entry<?,?>)itor.next();
		tmpValue=etr.getKey();
		if(null==tmpValue){
			continue;
		}
		kstr=serialize(tmpValue,null,true,false);
		if(null==kstr){
			continue;
		}
		tmpValue=etr.getValue();
		if(null==tmpValue){
			continue;
		}
		vstr=serialize(tmpValue,kstr,true,true);
		if(null==vstr){
			continue;
		}
		varstr.append(vstr);
	}while(itor.hasNext());
	if(varstr.length()==tag.length()+2){
		return null;
	}
	return varstr.append("</"+tag+'>');
}
protected static CharSequence serializeIterable(Iterable<?> piterator,CharSequence tag)
{
	Iterator<?> iterator=piterator.iterator();
	if(!iterator.hasNext()){
		return null;
	}
	StringBuilder varstr=new StringBuilder();
	do{
		Object idxval=iterator.next();
		if(null==idxval){
			continue;
		}
		CharSequence tmpValue=serialize(idxval,tag,true,true);
		if(null==tmpValue){
			continue;
		}
		if(idxval.getClass().isArray() || idxval instanceof Iterable){
			varstr.append('<'+tag.toString()+'>'+tmpValue+"</"+tag+'>');
		}else{
			varstr.append(tmpValue);
		}
	}while(iterator.hasNext());
	if(varstr.length()==0){
		return null;
	}
	return varstr;
}
protected static void getMembers(List<Method> validMds,List<Method> elideMds,List<Field> validFds,List<Field> elideFds,Class<?> pclass,boolean asInstance)
{
	Field[] lfds=pclass.getDeclaredFields();
	if(null!=lfds && 0!=lfds.length)
	{
		loopfds:for(Field ltfd:lfds)
		{
			int sign=ltfd.getModifiers();
			if(Modifier.isStatic(sign)==asInstance){
				continue;
			}
			final String fname=ltfd.getName();
			for(Field lifd:elideFds){
				if(lifd.getName().equals(fname)){//已经被忽略,不重复忽略
					continue loopfds;
				}
			}
			for(Field lifd:validFds){
				if(lifd.getName().equals(fname)){//已经被添加,不重复添加
					continue loopfds;
				}
			}
			for(Method limd:elideMds){
				if(Tool.calcAttrName(limd).equals(fname)){
					continue loopfds;
				}
			}
			for(Method limd:validMds){
				if(Tool.calcAttrName(limd).equals(fname)){
					continue loopfds;
				}
			}
			XmlRequire require=ltfd.getAnnotation(XmlRequire.class);
			if(null==require){//没成员注解
				if(!Modifier.isPublic(sign)){
					continue loopfds;
				}
				validFds.add(ltfd);
			}else{//有成员注解
				@SuppressWarnings("unused")
				boolean b=require.value() ? validFds.add(ltfd) : elideFds.add(ltfd);
			}//有成员注解
		}//for field
	}
	Method[] lmds=pclass.getDeclaredMethods();
	if(null==lmds || 0==lmds.length){
		return;
	}
	loopmd:for(Method ltmd:lmds)
	{
		int sign=ltmd.getModifiers();
		if((Modifier.isStatic(sign)==asInstance)){
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
		final String mname=ltmd.getName(),fname=Tool.calcAttrName(ltmd);
		for(Field lifd:elideFds){
			if(lifd.getName().equals(fname)){//已经被忽略,不重复忽略
				continue loopmd;
			}
		}
		for(Field lifd:validFds){
			if(lifd.getName().equals(fname)){//已经被添加,不重复添加
				continue loopmd;
			}
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
		XmlRequire require=ltmd.getAnnotation(XmlRequire.class);
		if(null==require){
			if(!Modifier.isPublic(sign)){
				continue loopmd;
			}
			validMds.add(ltmd);
		}else{//有成员注解
			@SuppressWarnings("unused")
			boolean b=require.value() ? validMds.add(ltmd) : elideMds.add(ltmd);
		}//有成员注解
	}//for method
}
protected static CharSequence serializeObject(Object pobj,CharSequence tag)
{
	boolean isclass=pobj instanceof Class;
	List<Method> allValidMds=new ArrayList<>(),allIgnoreMds=new ArrayList<>();
	List<Field> allValidFds=new ArrayList<>(),allIgnoreFds=new ArrayList<>();
	Class<?> lclass=null;
	if(isclass){
		lclass=(Class<?>)pobj;
	}else{
		lclass=pobj.getClass();
	}
	NotFinal nofinal=null;
	do{
		getMembers(allValidMds,allIgnoreMds,allValidFds,allIgnoreFds,lclass,!isclass);
		nofinal=lclass.getDeclaredAnnotation(NotFinal.class);
		lclass=lclass.getSuperclass();
	}while(null!=lclass && (Object.class!=lclass || null!=nofinal));
	List<String> mnms=new ArrayList<String>(allValidMds.size()),fnms=new ArrayList<String>(allValidFds.size());
	for(int i=0,len=allValidMds.size();i!=len;i++)
	{
		Method md=allValidMds.get(i);
		XmlRequire require=md.getAnnotation(XmlRequire.class);
		if(null==require){
			mnms.add(Tool.calcAttrName(md));
		}else if(require.toXml().length()==0){
			mnms.add(Tool.calcAttrName(md));
		}else{
			mnms.add(toXmlStandard(require.toXml()).toString());
		}
	}
	for(int i=0,len=allValidFds.size();i!=len;i++)
	{
		Field fd=allValidFds.get(i);
		XmlRequire require=fd.getAnnotation(XmlRequire.class);
		if(null==require){
			fnms.add(fd.getName());
		}else if(require.toXml().length()==0){
			fnms.add(fd.getName());
		}else{
			fnms.add(toXmlStandard(require.toXml()).toString());
		}
	}
	/*for(int j=0,jlen=allValidFds.size();j!=jlen;j++)
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
	}*/
	StringBuilder varstr=new StringBuilder(),attrstr=new StringBuilder();
	Object returnValue=null;
	for(int i=0,len=allValidMds.size();i!=len;i++)
	{
		Method md=allValidMds.get(i);
		md.setAccessible(true);
		try{
			returnValue=md.invoke(pobj);
		}catch(IllegalAccessException|IllegalArgumentException|InvocationTargetException e){
			e.printStackTrace(System.err);
			continue;
		}
		if(null==returnValue){
			continue;
		}
		String inrkey=mnms.get(i);
		CharSequence chars=serialize(returnValue,inrkey);
		if(null==chars){
			continue;
		}
		XmlRequire require=md.getAnnotation(XmlRequire.class);
		if(null==require || !require.attr()){
			varstr.append(chars);
			continue;
		}
		int idx=chars.toString().indexOf('<',inrkey.length()+3);
		if(chars.length()==inrkey.length()+3+idx){
			attrstr.append(' ').append(inrkey).append("=\"").append(chars.subSequence(inrkey.length()+2,idx)).append('"');
			continue;
		}
		varstr.append(chars);
	}
	for(int i=0,len=allValidFds.size();i!=len;i++)
	{
		Field fd=allValidFds.get(i);
		fd.setAccessible(true);
		try{
			returnValue=fd.get(pobj);
		}catch(IllegalArgumentException|IllegalAccessException e){
			e.printStackTrace(System.err);
			continue;
		}
		if(null==returnValue){
			continue;
		}
		String inrkey=fnms.get(i);
		CharSequence chars=serialize(returnValue,inrkey);
		if(null==chars){
			continue;
		}
		XmlRequire require=fd.getAnnotation(XmlRequire.class);
		if(null==require || !require.attr()){
			varstr.append(chars);
			continue;
		}
		int idx=chars.toString().indexOf('<',inrkey.length()+3);
		if(chars.length()==inrkey.length()+3+idx){
			attrstr.append(' ').append(inrkey).append("=\"").append(chars.subSequence(inrkey.length()+2,idx)).append('"');
			continue;
		}
		varstr.append(chars);
	}
	if(varstr.length()==0){
		if(attrstr.length()==0){
			return null;
		}else{
			return '<'+tag.toString()+attrstr+"/>";
		}
	}else{
		if(attrstr.length()==0){
			return '<'+tag.toString()+'>'+varstr+"</"+tag+'>';
		}else{
			return  '<'+tag.toString()+attrstr+'>'+varstr+"</"+tag+'>';
		}
	}
}
protected static CharSequence serializeClass(Class<?> pclass,CharSequence tag)
{
	return wrapTag(pclass.getName(),tag);
}
protected static CharSequence serializeEnum(Class<?> penum,CharSequence tag)
{
	Object[] enums=penum.getEnumConstants();
	if(null==enums||0==enums.length){
		return null;
	}
	final StringBuilder jsonBuilder=new StringBuilder();
	for(int i=0;enums.length!=i;i++){
		jsonBuilder.append(wrapTag(enums[i].toString(),tag));
	}
	return jsonBuilder;
}
public static CharSequence serialize(Object pobj,CharSequence tag,boolean instance,boolean xmlValue)
{
	Class<?> clazz=null;
	if(pobj instanceof Class){
		clazz=(Class<?>)pobj;
		if(instance){
			if(xmlValue){
				return serializeClass(clazz,tag).toString();
			}
			return clazz.getName();
		}
		if(xmlValue){
			if(clazz.isEnum()){
				return serializeEnum(clazz,tag);
			}
			return serializeObject(clazz,tag);
		}
		return clazz.getName();
	}
	clazz=pobj.getClass();
	if(xmlValue){
		if(Character.class==clazz){
			return wrapTag(toXmlStandard(String.valueOf((char)pobj)),tag);
		}
		if(Boolean.class==clazz){
			return wrapTag(String.valueOf((boolean)pobj),tag);
		}
		if(Byte.class==clazz){
			return wrapTag(String.valueOf((byte)pobj),tag);
		}
		if(Short.class==clazz){
			return wrapTag(String.valueOf((short)pobj),tag);
		}
		if(Integer.class==clazz){
			return wrapTag(String.valueOf((int)pobj),tag);
		}
		if(Long.class==clazz){
			return wrapTag(String.valueOf((long)pobj),tag);
		}
		if(Float.class==clazz){
			return wrapTag(String.valueOf((float)pobj),tag);
		}
		if(Double.class==clazz){
			return wrapTag(String.valueOf((double)pobj),tag);
		}
	}else{
		if(Character.class==clazz){
			return toXmlStandard(String.valueOf((char)pobj));
		}
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
	}
	if(pobj instanceof CharSequence){
		String str=toXmlStandard((CharSequence)pobj).toString();
		if(xmlValue){
			return wrapTag(str,tag);
		}
		return str;
	}
	if(pobj instanceof XmlSerializable){
		if(xmlValue){
			return ((XmlSerializable)pobj).toXmlValue();
		}
		return toXmlStandard(((XmlSerializable)pobj).toXmlKey());
	}
	if(clazz.isAnnotation()){
		if(xmlValue){
			return serializeAnnotation(pobj,tag);
		}
		return clazz.getName();
	}
	if(clazz.isArray()){
		if(xmlValue){
			return serializeArray(pobj,tag);
		}
		return clazz.getName();
	}
	if(clazz.isEnum()){
		if(xmlValue){
			return wrapTag(pobj.toString(),tag);
		}
		return pobj.toString();
	}
	if(pobj instanceof Iterable<?>){
		if(xmlValue){
			return serializeIterable((Iterable<?>)pobj,tag);
		}
		return clazz.getName();
	}
	if(pobj instanceof Map<?,?>){
		if(xmlValue){
			return serializeMap((Map<?,?>)pobj,tag);
		}
		return clazz.getName();
	}
	return serializeObject(pobj,tag);//if(clazz.isInterface())
}
public static CharSequence serialize(Object pobj,CharSequence tag)
{
	return serialize(pobj,tag,true,true);
}
public static CharSequence toXmlStandard(CharSequence chars)
{
	if(null==chars || chars.length()==0){
		return null;
	}
	String[][] entity={{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""},
			{"","",""},{"","",""},{"","",""}};
	final int len=chars.length();
	StringBuilder varstr=new StringBuilder(len);
	scan:for(int i=0;len!=i;i++)
	{
		char chr=chars.charAt(i);
		switch(chr)
		{
		case '<':
			varstr.append("&lt;");
			continue scan;
		case '>':
			varstr.append("&gt;");
			continue scan;
		case '"':
			varstr.append("&quot;");
			continue scan;
		default:
			varstr.append(chr);
			continue scan;
		}
	}
	return varstr;
}
protected static CharSequence wrapTag(CharSequence val,CharSequence tag)
{
	String key=tag.toString();
	return '<'+key+'>'+val+"</"+key+'>';
}
@SuppressWarnings("unchecked")
public static void putMultiVal(Map<String,Object> container,String key,Object val)
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
public static Object parseXml(CharSequence xml)
{
	return null;
}
protected XmlSerializer(){}
}