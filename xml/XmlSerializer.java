package wcy.usual.xml;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	XmlNotFinal nofinal=null;
	do{
		getMembers(allValidMds,allIgnoreMds,allValidFds,allIgnoreFds,lclass,!isclass);
		nofinal=lclass.getDeclaredAnnotation(XmlNotFinal.class);
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
			mnms.add(txt2xml(require.toXml()).toString());
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
			fnms.add(txt2xml(require.toXml()).toString());
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
			return wrapTag(txt2xml(String.valueOf((char)pobj)),tag);
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
			return txt2xml(String.valueOf((char)pobj));
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
		String str=txt2xml((CharSequence)pobj).toString();
		if(xmlValue){
			return wrapTag(str,tag);
		}
		return str;
	}
	if(pobj instanceof XmlSerializable){
		if(xmlValue){
			return ((XmlSerializable)pobj).toXmlValue();
		}
		return txt2xml(((XmlSerializable)pobj).toXmlKey());
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
public static CharSequence txt2xml(CharSequence chars)
{
	if(null==chars || chars.length()==0){
		return null;
	}
	final int len=chars.length();
	StringBuilder varstr=new StringBuilder(len);
	scan:for(int i=0;len!=i;i++)//<![CDATA[<xml>]]>
	{
		char chr=chars.charAt(i);
		switch(chr)
		{//'->&apos;
		case '<':
			varstr.append("&lt;");
			continue scan;
		case '>':
			varstr.append("&gt;");
			continue scan;
		case '"':
			varstr.append("&quot;");
			continue scan;
		case ' ':
			varstr.append("&#32;");
			continue scan;
		default:
			varstr.append(chr);
			continue scan;
		}//&->&amp;
	}
	return varstr;
}
public static CharSequence xml2txt(CharSequence chars)
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
	scan:for(int i=0;len!=i;i++)//<![CDATA[<xml>]]>
	{
		char chr=chars.charAt(i);
		switch(chr)
		{//'->&apos;
		case '<':
			varstr.append("&lt;");
			continue scan;
		case '>':
			varstr.append("&gt;");
			continue scan;
		case '"':
			varstr.append("&quot;");
			continue scan;
		case ' ':
			varstr.append("&#32;");
			continue scan;
		default:
			varstr.append(chr);
			continue scan;
		}//&->&amp;
	}
	return varstr;
}
protected static CharSequence wrapTag(CharSequence val,CharSequence tag)
{
	String key=tag.toString();
	return '<'+key+'>'+val+"</"+key+'>';
}
public static Object parseXml(CharSequence xml)
{
	System.out.println(xml);
	if(null==xml || xml.length()==0){
		return null;
	}
	String tmpstr=xml.toString();
	StringBuilder varstr=new StringBuilder(xml.length());
	int xi=0;
	for(int dati=0,datj;(dati=tmpstr.indexOf("<!--",dati))!=-1;)
	{
		if((datj=tmpstr.indexOf("-->",dati+4))==-1){
			System.out.println("<!--=>not match");
			return null;
		}
		if(xi!=dati){
			varstr.append(tmpstr.substring(xi,dati));
		}
		xi=dati=datj+3;
	}
	if(0!=xi){
		if(tmpstr.length()!=xi){
			varstr.append(tmpstr.substring(xi));
		}
		xml=varstr;
	}
	System.out.println(xml);
	tmpstr=xml.toString();
	varstr=new StringBuilder(xml.length());
	xi=0;
	for(int dati=3,datj;(dati=tmpstr.indexOf("<![CDATA[",dati))!=-1;)
	{
		if((datj=tmpstr.indexOf("]]>",dati+9))==-1){
			System.out.println("CDATA=>not match");
			return null;
		}
		if(xi!=dati){
			varstr.append(tmpstr.substring(xi,dati));
		}
		if(dati+9!=datj){
			varstr.append(txt2xml(tmpstr.substring(dati+9,datj)));
		}
		xi=dati=datj+3;
	}
	if(0!=xi){
		if(tmpstr.length()!=xi){
			varstr.append(tmpstr.substring(xi));
		}
		xml=varstr;
	}
	System.out.println(xml);
	int[] idx=new int[3];
	idx[0]=xml.length();
	idx[1]=0;
	idx[2]=idx[0]-1;
	Map<String,Object> obj=new HashMap<String,Object>();
	if(parseXml(obj,xml.toString(),idx)){
		return obj.size()==0 ? null : obj;
	}
	return null;
}
protected static boolean parseXml(Map<String,Object> parent,String xml,int[] idx)
{
	int vari=idx[2]+1;
	for(int i=idx[1];i!=vari;i++)
	{
		if(Character.isWhitespace(xml.charAt(i))){
			idx[1]+=1;
			continue;
		}
		break;
	}
	if(idx[1]==vari){
		idx[0]=0;
		return false;
	}
	vari=idx[1]-1;
	for(int i=idx[2];i!=vari;i--)
	{
		if(Character.isWhitespace(xml.charAt(i))){
			idx[2]-=1;
			continue;
		}
		break;
	}
	if(idx[2]==vari){
		idx[0]=0;
		return false;
	}
	if(xml.charAt(idx[1])!='<' || xml.charAt(idx[2])!='>'){
		idx[0]=0;
		return false;
	}
	vari=xml.indexOf('>',idx[1]+1);
	if(idx[1]+1==vari || -1==vari || idx[2]<vari){
		idx[0]=0;
		return false;
	}
	int[] tidx={idx[0],vari+1,idx[2]};
	String notag=null;
	Map<String,Object> curent=new HashMap<String,Object>();
	if(xml.charAt(vari-1)=='/')
	{
		if(xml.charAt(vari-2)==' '){
			notag=xml.substring(idx[1]+1,vari-2);
		}else{
			notag=xml.substring(idx[1]+1,vari-1);
		}
		if(notag.indexOf(' ')==-1){
			return vari==idx[2] ? true : parseXml(parent,xml,tidx);
		}
		String[] kvs=notag.split(" ");
		putMultiVal(parent,xml2txt(kvs[0]).toString(),curent);
		for(int i=1;kvs.length!=i;i++)
		{
			String[] kv=kvs[i].split("=");
			putMultiVal(curent,xml2txt(kv[0]).toString(),xml2txt(kv[1].substring(1,kv[1].length()-1)));
		}
		return vari==idx[2] ? true : parseXml(parent,xml,tidx);
	}
	if(xml.charAt(vari-1)==' '){
		notag=xml.substring(idx[1]+1,vari-1);
	}else{
		notag=xml.substring(idx[1]+1,vari);
	}
	String tag=notag;
	if(notag.indexOf(' ')!=-1)
	{
		String[] kvs=notag.split(" ");
		tag=kvs[0];
		for(int i=1;kvs.length!=i;i++)
		{
			String[] kv=kvs[i].split("=");
			putMultiVal(curent,xml2txt(kv[0]).toString(),xml2txt(kv[1].substring(1,kv[1].length()-1)));
		}
		putMultiVal(parent,xml2txt(tag).toString(),curent);
	}
	if(idx[2]==vari+tag.length()+3){
		return xml.substring(vari+1,idx[2]+1).equals("</"+tag+'>');
	}
	if(idx[2]<vari+tag.length()+3){
		return false;
	}
	idx[1]=vari+1;
	vari=xml.indexOf("</"+tag+'>',idx[1]);
	if(-1==vari || idx[2]<vari+tag.length()+2){
		return false;
	}
	if(idx[1]==vari){
		tidx[1]=vari+tag.length()+3;
		return idx[2]==vari+tag.length()+2 ? true : parseXml(parent,xml,tidx);
	}
	int cnti=0;
	for(int tmp=vari-tag.length()-2,loci;((tmp=xml.lastIndexOf('<'+tag,tmp))!=-1) && idx[1]<=tmp;)
	{
		vari=loci=vari+tag.length()+3;
		if(idx[2]<vari+tag.length()+2){
			return false;
		}
		vari=xml.indexOf("</"+tag+'>',vari);
		if(-1==vari || idx[2]<vari+tag.length()+2){
			return false;
		}
		while(((loci=xml.indexOf('<'+tag,loci))!=-1) && loci<vari){
			cnti+=1;
			loci=loci+tag.length()+2;
		}
		if(idx[2]==vari+tag.length()+2){
			if(0==cnti){
				break;
			}
			return false;
		}
		tmp=tmp-tag.length()-2;
	}
	for(int loci;0!=cnti;)
	{
		vari=loci=vari+tag.length()+3;
		if(idx[2]<vari+tag.length()+2){
			return false;
		}
		vari=xml.indexOf("</"+tag+'>',vari);
		if(-1==vari || idx[2]<vari+tag.length()+2){
			return false;
		}
		while(((loci=xml.indexOf('<'+tag,loci))!=-1) && loci<vari){
			cnti+=1;
			loci=loci+tag.length()+2;
		}
		if(idx[2]==vari+tag.length()+2){
			if(1==cnti){
				break;
			}
			return false;
		}
		cnti-=1;
	}
	String cnt=xml.substring(idx[1],vari);
	if(cnt.indexOf('<')==-1){
		putMultiVal(parent,xml2txt(tag).toString(),xml2txt(cnt));
	}else{
		tidx[1]=idx[1];
		tidx[2]=vari-1;
		int size=curent.size();
		if(!parseXml(curent,xml,tidx)){
			return false;
		}
		if(0==size && curent.size()!=0){
			putMultiVal(parent,xml2txt(tag).toString(),curent);
		}
	}
	if(vari+tag.length()+2==idx[2]){
		return true;
	}
	idx[1]=vari+tag.length()+3;
	return parseXml(parent,xml,idx);
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
protected XmlSerializer(){}
}