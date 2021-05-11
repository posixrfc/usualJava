package wcy.usual.codec.json;

import java.lang.annotation.Annotation;
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
import java.util.Map.Entry;

import wcy.usual.codec.Codec;

import java.util.Set;

public final class JsonSerializer
{
protected static CharSequence serializeArray(Object parr)
{
	final int arrlen=Array.getLength(parr);
	if(0==arrlen){
		return null;
	}
	StringBuilder varstr=new StringBuilder("[");
	for(int i=0;arrlen!=i;i++)
	{
		Object idxval=Array.get(parr,i);
		if(null==idxval){
			continue;
		}
		CharSequence tmpValue=serialize(idxval,true,true);
		if(null==tmpValue){
			continue;
		}
		varstr.append(tmpValue).append(',');
	}
	if(varstr.length()==1){
		return null;
	}
	return varstr.deleteCharAt(varstr.length()-1).append(']');
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
	Iterator<?> itor=eset.iterator();
	Entry<?,?> etr=null;
	CharSequence kstr,vstr;
	Object tmpValue=null;
	StringBuilder varstr=new StringBuilder("{");
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
		varstr.append(kstr).append(':').append(vstr).append(',');
	}while(itor.hasNext());
	if(varstr.length()==1){
		return null;
	}
	return varstr.deleteCharAt(varstr.length()-1).append('}');
}
protected static CharSequence serializeIterable(Iterable<?> piterator)
{
	Iterator<?> iterator=piterator.iterator();
	if(!iterator.hasNext()){
		return null;
	}
	StringBuilder varstr=new StringBuilder("[");
	do{
		Object idxval=iterator.next();
		if(null==idxval){
			continue;
		}
		CharSequence tmpValue=serialize(idxval,true,true);
		if(null!=tmpValue){
			varstr.append(tmpValue).append(',');
		}
	}while(iterator.hasNext());
	if(varstr.length()==1){
		return null;
	}
	return varstr.deleteCharAt(varstr.length()-1).append(']');
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
				if(Codec.calcAttrName(limd).equals(fname)){
					continue loopfds;
				}
			}
			for(Method limd:validMds){
				if(Codec.calcAttrName(limd).equals(fname)){
					continue loopfds;
				}
			}
			JsonRequire require=ltfd.getAnnotation(JsonRequire.class);
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
		final String mname=ltmd.getName(),fname=Codec.calcAttrName(ltmd);
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
		JsonRequire require=ltmd.getAnnotation(JsonRequire.class);
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
protected static CharSequence serializeObject(Object pobj)
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
	JsonNotFinal nofinal=null;
	do{
		getMembers(allValidMds,allIgnoreMds,allValidFds,allIgnoreFds,lclass,!isclass);
		nofinal=lclass.getDeclaredAnnotation(JsonNotFinal.class);
		lclass=lclass.getSuperclass();
	}while(null!=lclass && (Object.class!=lclass || null!=nofinal));
	List<String> mnms=new ArrayList<String>(allValidMds.size()),fnms=new ArrayList<String>(allValidFds.size());
	for(int i=0,len=allValidMds.size();i!=len;i++)
	{
		Method md=allValidMds.get(i);
		JsonRequire require=md.getAnnotation(JsonRequire.class);
		if(null==require){
			mnms.add(Codec.calcAttrName(md));
		}else if(require.toJson().length()==0){
			mnms.add(Codec.calcAttrName(md));
		}else{
			mnms.add(txt2json(require.toJson()).toString());
		}
	}
	for(int i=0,len=allValidFds.size();i!=len;i++)
	{
		Field fd=allValidFds.get(i);
		JsonRequire require=fd.getAnnotation(JsonRequire.class);
		if(null==require){
			fnms.add(fd.getName());
		}else if(require.toJson().length()==0){
			fnms.add(fd.getName());
		}else{
			fnms.add(txt2json(require.toJson()).toString());
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
	StringBuilder varstr=new StringBuilder("{");
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
		CharSequence chars=serialize(returnValue);
		if(null==chars){
			continue;
		}
		varstr.append('"').append(txt2json(mnms.get(i))).append("\":").append(chars).append(',');
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
		CharSequence chars=serialize(returnValue);
		if(null==chars){
			continue;
		}
		varstr.append('"').append(txt2json(fnms.get(i))).append("\":").append(chars).append(',');
	}
	if(varstr.length()==1){
		return null;
	}
	return  varstr.deleteCharAt(varstr.length()-1).append('}');
}
protected static CharSequence serializeClass(Class<?> pclass)
{
	return '"'+pclass.getName()+'"';
}
protected static CharSequence serializeEnum(Class<?> penum)
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
	if(asJsonValue){
		if(Character.class==clazz){
			return "\""+ txt2json(String.valueOf((char)pobj))+'"';
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
	}else{
		if(Character.class==clazz){
			return "\""+ txt2json(String.valueOf((char)pobj))+'"';
		}
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
		String str=txt2json(pobj.toString()).toString();
		if(asJsonValue){
			return '"'+str+'"';
		}
		return '"'+str+'"';
	}
	if(pobj instanceof JsonSerializable){
		if(asJsonValue){
			return ((JsonSerializable)pobj).toJsonVal();
		}
		return '"'+txt2json(((JsonSerializable)pobj).toJsonKey()).toString()+'"';
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
public static CharSequence txt2json(CharSequence chars)
{
	StringBuilder hexBuilder=new StringBuilder();
	String hexString=null;
	charsfor:for(int i=0,len=chars.length();len!=i;i++)
	{
		char char1=chars.charAt(i);
		if((96<char1&&char1<123)||(64<char1&&char1<91)||(47<char1&&char1<58)){
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
		case '[':
		case ']':
		case ',':
		case '>':
		case '<':
		case '{':
		case '}':
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
@SuppressWarnings("unchecked")
public static Object parseJson(CharSequence json)
{
	if(null==json || json.length()==0){
		return null;
	}
	int[] stat=new int[]{json.length(),0};
	CharSequence key=null;
	Object val=null,obj=null;
	char chr;
	runloop:for(int i=0;i!=stat[0];)
	{
		chr=json.charAt(i);
		if(Character.isWhitespace(chr)){
			i+=1;
			continue;
		}
		stat[1]=i;
		switch(chr)
		{
		case '{':
			if(null==obj){
				obj=new HashMap<String,Object>();
				i+=1;
				continue runloop;
			}
			val=parseMap(json,stat);
			if(null==val){
				if(0==stat[0]){
					System.err.println("idx=>"+stat[1]);
					return null;
				}else{
					key=null;
					i=stat[1];
					continue runloop;
				}
			}else{
				if(obj instanceof Iterable){
					((List<Object>)obj).add(val);
				}else{
					if(null!=key){
						((Map<String,Object>)obj).put(key.toString(),val);
					}
					key=null;
				}
				i=stat[1];
				continue runloop;
			}
		case '[':
			if(null==obj){
				obj=new ArrayList<Object>();
				i+=1;
				continue runloop;
			}
			val=parseList(json,stat);
			if(null==val){
				if(0==stat[0]){
					System.err.println("idx=>"+stat[1]);
					return null;
				}else{
					key=null;
					i=stat[1];
					continue runloop;
				}
			}else{
				if(obj instanceof Iterable){
					((List<Object>)obj).add(val);
				}else{
					if(null!=key){
						((Map<String,Object>)obj).put(key.toString(),val);
					}
					key=null;
				}
				i=stat[1];
				continue runloop;
			}
		case '"':
			val=parseStr(json,stat);
			if(null==val){
				if(0==stat[0]){
					System.err.println("idx=>"+stat[1]);
					return null;
				}else{
					key=null;
					i=stat[1];
					continue runloop;
				}
			}else{
				if(obj instanceof Iterable){
					((List<Object>)obj).add(val);
				}else{
					if(null==key){
						key=(CharSequence)val;
					}else{
						((Map<String,Object>)obj).put(key.toString(),val);
						key=null;
					}
				}
				i=stat[1];
				continue runloop;
			}
		case 'n':
			val=parseNull(json,stat);
			if(null==val){
				System.err.println("idx=>"+stat[1]);
				return null;
			}else{
				key=null;
				i=stat[1];
				continue runloop;
			}
		case 't':
		case 'f':
			val=parseBool(json,stat);
			if(null==val){
				System.err.println("idx=>"+stat[1]);
				return null;
			}else{
				if(obj instanceof Iterable){
					((List<Object>)obj).add(val);
				}else{
					if(null!=key){
						((Map<String,Object>)obj).put(key.toString(),val);
					}
					key=null;
				}
				i=stat[1];
				continue runloop;
			}
		case '+':
		case '-':
		case '.':
		case 'e':
		case 'E':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			val=parseNum(json,stat);
			if(null==val){
				System.err.println("idx=>"+stat[1]);
				return null;
			}else{
				if(obj instanceof Iterable){
					((List<Object>)obj).add(val);
				}else{
					if(null!=key){
						((Map<String,Object>)obj).put(key.toString(),val);
					}
					key=null;
				}
				i=stat[1];
				continue runloop;
			}
		case ',':
		case ':':
			i+=1;
			continue runloop;
		case '}':
		case ']':
			return obj;
		default:
			System.err.println("idx=>"+i);
			return null;
		}
	}
	return obj;
}
protected static Map<String,?> parseMap(CharSequence src,int[] idx)
{
	Map<String,Object> obj=new HashMap<String,Object>();
	CharSequence key=null;
	Object val=null;
	charsfor:for(int i=idx[1]+1;idx[0]!=i;)
	{
		char chr=src.charAt(i);
		if(Character.isWhitespace(chr)){
			continue;
		}
		switch(chr)
		{
		case '}':
			idx[1]=i+1;
			return obj.size()==0 ? null : obj;
		case '"':
			idx[1]=i;
			if(null==key){
				key=parseStr(src,idx);
				if(0==idx[0]){
					return null;
				}
				if(null==key){
					idx[0]=0;
					return null;
				}else{
					i=idx[1];
					continue charsfor;
				}
			}else{
				val=parseStr(src,idx);
				if(0==idx[0]){
					return null;
				}
				i=idx[1];
				if(null!=val){
					obj.put(key.toString(),val);
				}
				key=null;
				continue charsfor;
			}
		case '{':
			idx[1]=i;
			if(null==key){
				idx[0]=0;
				return null;
			}
			val=parseMap(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				obj.put(key.toString(),val);
			}
			key=null;
			continue charsfor;
		case '[':
			idx[1]=i;
			if(null==key){
				idx[0]=0;
				return null;
			}
			val=parseStr(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				obj.put(key.toString(),val);
			}
			key=null;
			continue charsfor;
		case 'n':
			idx[1]=i;
			if(null==key){
				idx[0]=0;
				return null;
			}
			val=parseNull(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			key=null;
			continue charsfor;
		case 't':
		case 'f':
			idx[1]=i;
			if(null==key){
				idx[0]=0;
				return null;
			}
			val=parseBool(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				obj.put(key.toString(),val);
			}
			key=null;
			continue charsfor;
		case '+':
		case '-':
		case '.':
		case 'e':
		case 'E':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			idx[1]=i;
			if(null==key){
				idx[0]=0;
				return null;
			}
			val=parseNum(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				obj.put(key.toString(),val);
			}
			key=null;
			continue charsfor;
		case ':':
		case ',':
			i+=1;
			continue charsfor;
		default:
			return null;
		}
	}
	return null;
}
protected static List<?> parseList(CharSequence src,int[] idx)
{
	List<Object> lst=new ArrayList<Object>();
	Object val=null;
	charsfor:for(int i=idx[1]+1;idx[0]!=i;)
	{
		char chr=src.charAt(i);
		if(Character.isWhitespace(chr)){
			continue;
		}
		switch(chr)
		{
		case ']':
			idx[1]=i+1;
			return lst.size()==0 ? null : lst;
		case '"':
			idx[1]=i;
			val=parseStr(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				lst.add(val);
			}
			continue charsfor;
		case '{':
			idx[1]=i;
			val=parseMap(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				lst.add(val);
			}
			continue charsfor;
		case '[':
			idx[1]=i;
			val=parseList(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				lst.add(val);
			}
			continue charsfor;
		case 'n':
			idx[1]=i;
			val=parseNull(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			continue charsfor;
		case 't':
		case 'f':
			idx[1]=i;
			val=parseBool(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				lst.add(val);
			}
			continue charsfor;
		case '+':
		case '-':
		case '.':
		case 'e':
		case 'E':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			idx[1]=i;
			val=parseNum(src,idx);
			if(0==idx[0]){
				return null;
			}
			i=idx[1];
			if(null!=val){
				lst.add(val);
			}
			continue charsfor;
		case ',':
			i+=1;
			continue charsfor;
		default:
			return null;
		}
	}
	return null;
}
protected static StringBuilder parseStr(CharSequence src,int[] idx)
{
	StringBuilder sb=new StringBuilder();
	char chr;
	charsfor:for(int i=idx[1]+1;i!=idx[0];i++)
	{
		chr=src.charAt(i);
		if('"'==chr){
			idx[1]=i+1;
			break;
		}
		if((96<chr&&chr<123)||(64<chr&&chr<91)||(47<chr&&chr<58)){
			sb.append(chr);
			continue;
		}
		switch(chr)
		{
		case '\\':
			i+=1;
			chr=src.charAt(i);
			switch(chr)
			{
			case 'b':
				sb.append('\b');
				break;
			case 'f':
				sb.append('\f');
				break;
			case 'n':
				sb.append('\n');
				break;
			case 'r':
				sb.append('\r');
				break;
			case 't':
				sb.append('\t');
				break;
			case '\\':
				sb.append('\\');
				break;
			case '/':
				sb.append('/');
				break;
			case '"':
				sb.append('"');
				break;
			case 'u':
				try{
					chr=(char)Integer.parseInt(src.subSequence(i+1,i+5).toString(),16);
				}catch(NumberFormatException e){
					idx[0]=0;
					idx[1]=i+1;
					return null;
				}
				sb.append(chr);
				i+=4;
				break;
			default:
				idx[0]=0;
				idx[1]=i;
				return null;
			}
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
		case '[':
		case ']':
		case ',':
		case '>':
		case '<':
		case '{':
		case '}':
		case ':':
		case ';':
		case '_':
		case '|':
		case '\'':
		case '?':
		case '/':
			sb.append(chr);
			continue charsfor;
		default:
			sb.append(chr);
			continue charsfor;
		}
	}
	return sb.length()==0 ? null : sb;
}
protected static StringBuilder parseNum(CharSequence src,int[] idx)//-1.33e-3
{
	StringBuilder sb=new StringBuilder();
	char chr;
	int positive=0,negative=0,upper=0,lower=0,point=0;
	charsfor:for(int i=idx[1];i!=idx[0];i++)
	{
		chr=src.charAt(i);
		switch(chr)
		{
		case '+':
			positive+=1;
			if(i==idx[1]){
				sb.append(chr);
				continue charsfor;
			}
			chr=sb.charAt(i-idx[1]-1);
			if('E'==chr || 'e'==chr){
				sb.append(src.charAt(i));
				continue charsfor;
			}
			idx[0]=0;
			idx[1]=i;
			return null;
		case '-':
			negative+=1;
			if(i==idx[1]){
				sb.append(chr);
				continue charsfor;
			}
			chr=sb.charAt(i-idx[1]-1);
			if('E'==chr || 'e'==chr){
				sb.append(src.charAt(i));
				continue charsfor;
			}
			idx[0]=0;
			idx[1]=i;
			return null;
		case '.':
			point+=1;
			if(i==idx[1]){
				idx[0]=0;
				return null;
			}
			chr=sb.charAt(i-idx[1]-1);
			if('9'<chr || '0'>chr){
				idx[0]=0;
				return null;
			}
			sb.append(src.charAt(i));
			continue charsfor;
		case 'e':
			lower+=1;
			if(i==idx[1]){
				idx[0]=0;
				return null;
			}
			chr=sb.charAt(i-idx[1]-1);
			if('9'<chr || '0'>chr){
				idx[0]=0;
				return null;
			}
			sb.append(src.charAt(i));
			continue charsfor;
		case 'E':
			upper+=1;
			if(i==idx[1]){
				idx[0]=0;
				return null;
			}
			chr=sb.charAt(i-idx[1]-1);
			if('9'<chr || '0'>chr){
				idx[0]=0;
				return null;
			}
			sb.append(src.charAt(i));
			continue charsfor;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			sb.append(chr);
			continue charsfor;
		default:
			break charsfor;
		}
	}
	if(sb.length()==0){
		return null;
	}
	chr=sb.charAt(sb.length()-1);
	if('+'==chr && '-'==chr || '.'==chr || 'e'==chr ||'E'==chr){
		idx[0]=0;
		return null;
	}
	if(positive+negative>2 || point>1 || upper+lower>1){
		idx[0]=0;
		return null;
	}
	idx[1]+=sb.length();
	return sb;
}
protected static String parseBool(CharSequence src,int[] idx)
{
	if(src.charAt(idx[1]+1)=='r'){
		if(src.charAt(idx[1]+2)!='u'){
			idx[0]=0;
			idx[1]+=2;
			return null;
		}
		if(src.charAt(idx[1]+3)!='e'){
			idx[0]=0;
			idx[1]+=3;
			return null;
		}
		idx[1]+=4;
		return "true";
	}
	if(src.charAt(idx[1]+1)=='a'){
		if(src.charAt(idx[1]+2)!='l'){
			idx[0]=0;
			idx[1]+=2;
			return null;
		}
		if(src.charAt(idx[1]+3)!='s'){
			idx[0]=0;
			idx[1]+=3;
			return null;
		}
		if(src.charAt(idx[1]+4)!='e'){
			idx[0]=0;
			idx[1]+=4;
			return null;
		}
		idx[1]+=5;
		return "false";
	}
	idx[0]=0;
	return null;
}
protected static String parseNull(CharSequence src,int[] idx)
{
	if(src.charAt(idx[1]+1)!='u'){
		idx[0]=0;
		idx[1]+=1;
		return null;
	}
	if(src.charAt(idx[1]+2)!='l'){
		idx[0]=0;
		idx[1]+=2;
		return null;
	}
	if(src.charAt(idx[1]+3)!='l'){
		idx[0]=0;
		idx[1]+=3;
		return null;
	}
	idx[1]+=4;
	return "null";
}
}