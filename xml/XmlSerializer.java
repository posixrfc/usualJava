package wcy.usual.xml;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class XmlSerializer
{

	public static Object parseXml(CharSequence xml) {
		return null;
	}/*
	private static CharSequence serializeArray(final Object parr){
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
	private static CharSequence serializeAnnotation(final Object pclass){
		final StringBuilder jsonBuilder=new StringBuilder();
		return null;
	}
	private static CharSequence serializeMap(final Map<?,?> pmap){
		Set<?> eset = pmap.entrySet();
		if(eset.size()==0){
			return null;
		}
		Iterator<?> itor = eset.iterator();
		Entry<?,?> etr=null;
		CharSequence kstr,vstr;
		Object tmpValue=null;
		final StringBuilder jsonBuilder=new StringBuilder("{");
		do {
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
		} while (itor.hasNext());
		if(jsonBuilder.length()==1){
			return null;
		}
		return jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append('}');
	}
	private static CharSequence serializeIterable(final Iterable<?> piterator){
		Iterator<?> iterator = piterator.iterator();
		if(!iterator.hasNext()){
			return null;
		}
		final StringBuilder jsonBuilder=new StringBuilder("[");
		CharSequence tmpValue=null;
		do {
			tmpValue=serialize(iterator.next(),true,true);
			if(null!=tmpValue){
				jsonBuilder.append(tmpValue).append(',');
			}
		} while (iterator.hasNext());
		if(jsonBuilder.length()==1){
			return null;
		}
		return jsonBuilder.deleteCharAt(jsonBuilder.length()-1).append(']');
	}
	private static void obtainRegularMembers(List<Method> validMds,List<Method> elideMds,List<Field> validFds,List<Field> elideFds,Class<?> pclass,boolean asInstance){
		Method[] lmds=pclass.getDeclaredMethods();
		if(null==lmds||lmds.length==0){
			return;
		}
		RequireTypeDef[] needType,elideType;
		boolean onlyGetter=true,onlyMethod=false,methodPriority=true;
		RequireType requiret=pclass.getAnnotation(RequireType.class);
		if(null==requiret){
			requiret=pclass.getPackage().getAnnotation(RequireType.class);
		}
		if(null==requiret){
			needType=new RequireTypeDef[]{RequireTypeDef.PUBLIC};
			elideType=new RequireTypeDef[]{RequireTypeDef.IGNORE_clone,RequireTypeDef.IGNORE_getClass,RequireTypeDef.IGNORE_hashCode,RequireTypeDef.IGNORE_toString};
		}else{
			onlyGetter=requiret.onlyGetter();
			onlyMethod=requiret.onlyMethod();
			methodPriority=requiret.methodPriority();
			needType=requiret.needType();
			elideType=requiret.elideType();
		}
		int sign;
		loopmd:for(Method ltmd:lmds){
			sign=ltmd.getModifiers();
			if((Modifier.isStatic(sign)&&asInstance)||(!Modifier.isStatic(sign)&&!asInstance)){
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
			RequireValue requirev=ltmd.getAnnotation(RequireValue.class);
			final String mname=ltmd.getName();
			if(null==requirev){//没成员注解,默认要添加
				boolean needmaybe=false;
				loopty:for(int i=0;needType.length!=i;i++){
					switch(needType[i]){
					case PUBLIC:
						if(Modifier.isPublic(sign)){
							needmaybe=true;
							break loopty;
						}
						break;
					case PROTECTED:
						if(Modifier.isProtected(sign)){
							ltmd.setAccessible(true);
							needmaybe=true;
							break loopty;
						}
						break;
					case PRIVATE:
						if(Modifier.isPrivate(sign)){
							ltmd.setAccessible(true);
							needmaybe=true;
							break loopty;
						}
						break;
					case PACKAGE:
						if(Modifier.isPrivate(sign)||Modifier.isProtected(sign)||Modifier.isPublic(sign)){
							break;
						}else{
							ltmd.setAccessible(true);
							needmaybe=true;
							break loopty;
						}
					default:
						break;
					}
				}
				if(!needmaybe){//访问权限不匹配
					continue loopmd;
				}
				for(int i=0;elideType.length!=i;i++){
					switch(elideType[i]){
					case IGNORE_toString:
						if(mname.equals("toString")){
							continue loopmd;
						}
						break;
					case IGNORE_hashCode:
						if(mname.equals("hashCode")){
							continue loopmd;
						}
						break;
					case IGNORE_getClass:
						if(mname.equals("getClass")){
							continue loopmd;
						}
						break;
					case IGNORE_clone:
						if(mname.equals("clone")){
							continue loopmd;
						}
						break;
					default:
						break;
					}
				}
				if(onlyGetter&&!mname.startsWith("get")){
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
				switch(requirev.needType()){
				case PROHIBIT://注解指定要忽略此方法
					for(Method limd:validMds){
						if(limd.getName().equals(mname)){//已经被添加,忽略无效
							continue loopmd;
						}
					}
					for(Method limd:elideMds){
						if(limd.getName().equals(mname)){//已经被忽略,不重复忽略
							continue loopmd;
						}
					}
					elideMds.add(ltmd);//成员注解优先级高于类注解优先级不用考虑类注解的情况
					break;
				case ESSENTIAL://注解指定必须要此方法
					for(Method limd:elideMds){//应该添加此方法
						if(limd.getName().equals(mname)){//已经被忽略,添加无效
							continue loopmd;
						}
					}
					for(Method limd:validMds){
						if(limd.getName().equals(mname)){//已经被添加,不重复添加
							continue loopmd;
						}
					}
					validMds.add(ltmd);
					break;//成员注解优先级高于类注解优先级不用考虑类注解的情况
				case NATURAL://注解指定视返回值而定,就该添加此方法
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
					validMds.add(ltmd);//成员注解优先级高于类注解优先级不用考虑类注解的情况
				}
			}//有成员注解
		}//for method
		if(onlyMethod){
			return;
		}
		Field[] lfds=pclass.getDeclaredFields();
		if(null==lfds||lfds.length==0){
			return;
		}
		loopfds:for(Field ltfd:lfds){
			sign=ltfd.getModifiers();
			if((Modifier.isStatic(sign)&&asInstance)||(!Modifier.isStatic(sign)&&!asInstance)){
				continue;
			}
			RequireValue requirev=ltfd.getAnnotation(RequireValue.class);
			final String fname=ltfd.getName();
			if(null==requirev){//没成员注解,默认要添加
				boolean needmaybe=false;
				loopty:for(int i=0;needType.length!=i;i++){
					switch(needType[i]){
					case PUBLIC:
						if(Modifier.isPublic(sign)){
							needmaybe=true;
							break loopty;
						}
						break;
					case PROTECTED:
						if(Modifier.isProtected(sign)){
							ltfd.setAccessible(true);
							needmaybe=true;
							break loopty;
						}
						break;
					case PRIVATE:
						if(Modifier.isPrivate(sign)){
							ltfd.setAccessible(true);
							needmaybe=true;
							break loopty;
						}
						break;
					case PACKAGE:
						if(Modifier.isPrivate(sign)||Modifier.isProtected(sign)||Modifier.isPublic(sign)){
							break;
						}else{
							ltfd.setAccessible(true);
							needmaybe=true;
							break loopty;
						}
					default:
						break;
					}
				}
				if(!needmaybe){//访问权限不匹配
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
				switch(requirev.needType()){
				case PROHIBIT://注解指定要忽略
					for(Field lifd:validFds){
						if(lifd.getName().equals(fname)){//已经被添加,忽略无效
							continue loopfds;
						}
					}
					for(Field lifd:elideFds){
						if(lifd.getName().equals(fname)){//已经被忽略,不重复忽略
							continue loopfds;
						}
					}
					elideFds.add(ltfd);//成员注解优先级高于类注解优先级不用考虑类注解的情况
					break;
				case ESSENTIAL://注解指定必须要
					for(Field lifd:elideFds){//必须添加
						if(lifd.getName().equals(fname)){//已经被忽略,添加无效
							continue loopfds;
						}
					}
					for(Field lifd:validFds){
						if(lifd.getName().equals(fname)){//已经被添加,不重复添加
							continue loopfds;
						}
					}
					validFds.add(ltfd);
					break;//成员注解优先级高于类注解优先级不用考虑类注解的情况
				case NATURAL://注解指定视返回值而定,就该添加此方法
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
					validFds.add(ltfd);//成员注解优先级高于类注解优先级不用考虑类注解的情况
				}
			}//有成员注解
		}//for field
	}
	private static CharSequence serializeObject(final Object pobj){
		boolean asInstance=pobj instanceof Class;
		List<Method> allValidMds=new ArrayList<>(),allIgnoreMds=new ArrayList<>();
		List<Field> allValidFds=new ArrayList<>(),allIgnoreFds=new ArrayList<>();
		Class<?> lclass=null;
		if(asInstance){
			lclass=pobj.getClass();
		}else{
			lclass=(Class<?>) pobj;
		}
		obtainRegularMembers(allValidMds,allIgnoreMds,allValidFds,allIgnoreFds,lclass,asInstance);
		Object returnValue=null;
		final StringBuilder jsonBuilder=new StringBuilder("{");
		return null;
	}
	private static CharSequence serializeClass(final Class<?> pclass){
		final StringBuilder jsonBuilder=new StringBuilder();
		return null;
	}
	private static CharSequence serializeEnum(final Class<?> penum){
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
	private static CharSequence serialize(final Object pobj,final boolean asInstance,final boolean asJsonValue){
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
		if(pobj instanceof XmlSerializable){
			if(asJsonValue) {
				return ((XmlSerializable)pobj).toJsonValue();
			}
			return ((XmlSerializable)pobj).toJsonKey();
		}
		if(clazz.isAnnotation()){
			if(asJsonValue){
				return serializeAnnotation(pobj);
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
	public static String serialize(final Object pobj,final boolean asInstance){
		if(null==pobj){
			return null;
		}
		CharSequence chars=serialize(pobj,asInstance,true);
		if(null==chars){
			return null;
		}
		return chars.toString();
	}
	public static CharSequence toJsonStandard(CharSequence chars){
		StringBuilder hexBuilder=new StringBuilder();
		String hexString=null;
		charsfor:for(int i=0,len=chars.length();len!=i;i++){
			char char1=chars.charAt(i);
			if((47<char1&&char1<58)||(64<char1&&char1<91)||(96<char1&&char1<123)){
				hexBuilder.append(char1);
				continue;
			}
			switch(char1){
			case '\b':
			case '\f':
			case '\n':
			case '\r':
			case '\t':
				hexBuilder.append(char1);
				continue charsfor;
			case '/':
			case '\\':
			case '"':
				hexBuilder.append("\\").append(char1);
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
private XmlSerializer(){}*/
}
/*util.obj2xml=(key,val)=>{
	if("number"===typeof val){
		return '<'+key+'>'+val+'</'+key+'>';
	}
	if("boolean"===typeof val){
		return '<'+key+'>'+val+'</'+key+'>';
	}
	if(!val){
		return null;
	}
	if("string"===typeof val){
		return '<'+key+'>'+val+'</'+key+'>';
	}
	let rs='';
	if(Array.isArray(val)){
		for(let i=0,tmp;i!==val.length;i++){
			tmp=util.obj2xml(key,val[i]);
			if(tmp){
				rs+=tmp;
			}
		}
		return 0===rs.length?null:rs;
	}
	let mismatch=true;
	rs='<'+key+'>';
	for(let k in val){
		tmp=util.obj2xml(k,val[k]);
		if(tmp){
			mismatch=false;
			rs+=tmp;
		}
	}
	if(mismatch){
		return null;
	}
	return rs+'</'+key+'>';
};
util.xml2obj=(obj,val)=>{
	val=val.trim();
	if(0===val.length){
		return false;
	}
	if(val.charAt(0)!=='<'){
		return false;
	}
	let idx=val.indexOf('>',2);
	if(-1===idx){
		return false;
	}
	let tag=val.substring(1,idx);
	if(idx+idx+3>=val.length){
		return false;
	}
	val=val.substring(idx+1).trim();
	idx=val.indexOf('</'+tag+'>');
	if(-1===idx){
		return false;
	}
	let tmp=idx-tag.length-2,noTag=true;
	while(tmp>=0 && ((tmp=val.lastIndexOf('<'+tag+'>',tmp))!==-1)){
		noTag=false;
		idx=idx+tag.length+3;
		if(idx+tag.length+3>=val.length){
			return false;
		}
		idx=val.indexOf('</'+tag+'>',idx);
		if(-1===idx){
			return false;
		}
		if(idx+tag.length+3===val.length){
			break;
		}
		tmp=tmp-tag.length-2;
	}
	let cnt=val.substring(0,idx).trim();
	val=val.substring(idx+tag.length+3);
	if(noTag && cnt.indexOf('<')!==-1){
		noTag=false;
	}
	if(noTag){
		if(obj[tag]){
			if(Array.isArray(obj[tag])){
				obj[tag].push(cnt);
			}else{
				obj[tag]=[obj[tag]];
				obj[tag].push(cnt);
			}
		}else{
			obj[tag]=cnt;
		}
	}else{
		if(obj[tag]){
			tmp={};
			if(Array.isArray(obj[tag])){
				obj[tag].push(tmp);
			}else{
				obj[tag]=[obj[tag]];
				obj[tag].push(tmp);
			}
		}else{
			tmp=obj[tag]={};
		}
		if(!util.xml2obj(tmp,cnt)){
			delete obj[tag];
			return false;
		}
	}
	return val ? util.xml2obj(obj,val) : true;
};*/