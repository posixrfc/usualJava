package wcy.usual.codec.xml;

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

import wcy.usual.codec.Codec;

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
				if(Codec.calcAttrName(limd).equals(fname)){
					continue loopfds;
				}
			}
			for(Method limd:validMds){
				if(Codec.calcAttrName(limd).equals(fname)){
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
			mnms.add(Codec.calcAttrName(md));
		}else if(require.toXml().length()==0){
			mnms.add(Codec.calcAttrName(md));
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
			return ((XmlSerializable)pobj).toXmlVal();
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
	scan:for(int i=0;len!=i;i++)
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
public static CharSequence xml2txt(CharSequence secquence)
{
	if(null==secquence){
		return null;
	}
	final int len=secquence.length();
	String tmpstr=secquence.toString();
	StringBuilder varstr=new StringBuilder(len);
	int xi=0;
	for(int i=0,j;len!=i;)
	{
		char chr=tmpstr.charAt(i);
		if('&'!=chr){
			i++;
			continue;
		}
		j=tmpstr.indexOf(';',i+3);
		if(-1==j){
			break;
		}
		if(i+16<j){
			i=j-16;
			continue;
		}
		if(tmpstr.charAt(i+1)=='#'){
			chr=entity2char(tmpstr.substring(i+2,j));
		}else{
			chr=entity2char(tmpstr.substring(i+1,j));
		}
		if('\0'==chr){
			i++;
			continue;
		}
		if(0!=i){
			varstr.append(tmpstr.subSequence(xi,i));
		}
		varstr.append(chr);
		i=xi=j+1;
	}
	if(0==xi){
		return secquence;
	}
	if(len==xi){
		return varstr;
	}
	return varstr.append(tmpstr.subSequence(xi,len));
}
public static char entity2char(String entity)
{
	switch(entity)
	{
	case "32":case "nbsp":return ' ';
	case "33":return '!';
	case "34":case "quot":return '"';
	case "35":return '#';
	case "36":return '$';
	case "37":return '%';
	case "38":case "amp":return '&';
	case "39":case "apos":return '\'';
	case "40":return '(';
	case "41":return ')';
	case "42":return '*';
	case "43":return '+';
	case "44":return ',';
	case "45":return '-';
	case "46":return '.';
	case "47":return '/';
	case "48":return '0';
	case "49":return '1';
	case "50":return '2';
	case "51":return '3';
	case "52":return '4';
	case "53":return '5';
	case "54":return '6';
	case "55":return '7';
	case "56":return '8';
	case "57":return '9';
	case "58":return ':';
	case "59":return ';';
	case "60":case "lt":return '<';
	case "61":return '=';
	case "62":case "gt":return '>';
	case "63":return '?';
	case "64":return '@';
	case "65":return 'A';
	case "66":return 'B';
	case "67":return 'C';
	case "68":return 'D';
	case "69":return 'E';
	case "70":return 'F';
	case "71":return 'G';
	case "72":return 'H';
	case "73":return 'I';
	case "74":return 'J';
	case "75":return 'K';
	case "76":return 'L';
	case "77":return 'M';
	case "78":return 'N';
	case "79":return 'O';
	case "80":return 'P';
	case "81":return 'Q';
	case "82":return 'R';
	case "83":return 'S';
	case "84":return 'T';
	case "85":return 'U';
	case "86":return 'V';
	case "87":return 'W';
	case "88":return 'X';
	case "89":return 'Y';
	case "90":return 'Z';
	case "91":return '[';
	case "92":return '\\';
	case "93":return ']';
	case "94":return '^';
	case "95":return '_';
	case "96":return '`';
	case "97":return 'a';
	case "98":return 'b';
	case "99":return 'c';
	case "100":return 'd';
	case "101":return 'e';
	case "102":return 'f';
	case "103":return 'g';
	case "104":return 'h';
	case "105":return 'i';
	case "106":return 'j';
	case "107":return 'k';
	case "108":return 'l';
	case "109":return 'm';
	case "110":return 'n';
	case "111":return 'o';
	case "112":return 'p';
	case "113":return 'q';
	case "114":return 'r';
	case "115":return 's';
	case "116":return 't';
	case "117":return 'u';
	case "118":return 'v';
	case "119":return 'w';
	case "120":return 'x';
	case "121":return 'y';
	case "122":return 'z';
	case "123":return '{';
	case "124":return '|';
	case "125":return '}';
	case "126":return '~';
	case "128":case "euro":return '€';
	case "130":case "sbquo":return '‚';
	case "131":case "fnof":return 'ƒ';
	case "132":case "bdquo":return '„';
	case "133":case "hellip":return '…';
	case "134":case "dagger":return '†';
	case "135":case "Dagger":return '‡';
	case "136":case "circ":return 'ˆ';
	case "137":case "permil":return '‰';
	case "138":case "Scaron":return 'Š';
	case "139":case "lsaquo":return '‹';
	case "140":case "OElig":return 'Œ';
	case "142":case "Zcaron":return 'Ž';
	case "145":case "lsquo":return '‘';
	case "146":case "rsquo":return '’';
	case "147":case "ldquo":return '“';
	case "148":case "rdquo":return '”';
	case "149":case "bull":return '•';
	case "150":case "ndash":return '–';
	case "151":case "mdash":return '—';
	case "152":case "tilde":return '˜';
	case "153":case "trade":return '™';
	case "154":case "scaron":return 'š';
	case "155":case "rsaquo":return '›';
	case "156":case "oelig":return 'œ';
	case "158":case "zcaron":return 'ž';
	case "159":case "Yuml":return 'Ÿ';
	case "160":/*case "nbsp":*/return ' ';
	case "161":case "iexcl":return '¡';
	case "162":case "cent":return '¢';
	case "163":case "pound":return '£';
	case "164":case "curren":return '¤';
	case "165":case "yen":return '¥';
	case "166":case "brvbar":return '¦';
	case "167":case "sect":return '§';
	case "168":case "uml":return '¨';
	case "169":case "copy":return '©';
	case "170":case "ordf":return 'ª';
	case "171":case "laquo":return '«';
	case "172":case "not":return '¬';
	case "174":case "reg":return '®';
	case "175":case "macr":return '¯';
	case "176":case "deg":return '°';
	case "177":case "plusmn":return '±';
	case "178":case "sup2":return '²';
	case "179":case "sup3":return '³';
	case "180":case "acute":return '´';
	case "181":case "micro":return 'µ';
	case "182":case "para":return '¶';
	case "183":case "middot":return '·';
	case "184":case "cedil":return '¸';
	case "185":case "sup1":return '¹';
	case "186":case "ordm":return 'º';
	case "187":case "raquo":return '»';
	case "188":case "frac14":return '¼';
	case "189":case "frac12":return '½';
	case "190":case "frac34":return '¾';
	case "191":case "iquest":return '¿';
	case "192":case "Agrave":return 'À';
	case "193":case "Aacute":return 'Á';
	case "194":case "Acirc":return 'Â';
	case "195":case "Atilde":return 'Ã';
	case "196":case "Auml":return 'Ä';
	case "197":case "Aring":return 'Å';
	case "198":case "AElig":return 'Æ';
	case "199":case "Ccedil":return 'Ç';
	case "200":case "Egrave":return 'È';
	case "201":case "Eacute":return 'É';
	case "202":case "Ecirc":return 'Ê';
	case "203":case "Euml":return 'Ë';
	case "204":case "Igrave":return 'Ì';
	case "205":case "Iacute":return 'Í';
	case "206":case "Icirc":return 'Î';
	case "207":case "Iuml":return 'Ï';
	case "208":case "ETH":return 'Ð';
	case "209":case "Ntilde":return 'Ñ';
	case "210":case "Ograve":return 'Ò';
	case "211":case "Oacute":return 'Ó';
	case "212":case "Ocirc":return 'Ô';
	case "213":case "Otilde":return 'Õ';
	case "214":case "Ouml":return 'Ö';
	case "215":case "times":return '×';
	case "216":case "Oslash":return 'Ø';
	case "217":case "Ugrave":return 'Ù';
	case "218":case "Uacute":return 'Ú';
	case "219":case "Ucirc":return 'Û';
	case "220":case "Uuml":return 'Ü';
	case "221":case "Yacute":return 'Ý';
	case "222":case "THORN":return 'Þ';
	case "223":case "szlig":return 'ß';
	case "224":case "agrave":return 'à';
	case "225":case "aacute":return 'á';
	case "226":case "acirc":return 'â';
	case "227":case "atilde":return 'ã';
	case "228":case "auml":return 'ä';
	case "229":case "aring":return 'å';
	case "230":case "aelig":return 'æ';
	case "231":case "ccedil":return 'ç';
	case "232":case "egrave":return 'è';
	case "233":case "eacute":return 'é';
	case "234":case "ecirc":return 'ê';
	case "235":case "euml":return 'ë';
	case "236":case "igrave":return 'ì';
	case "237":case "iacute":return 'í';
	case "238":case "icirc":return 'î';
	case "239":case "iuml":return 'ï';
	case "240":case "eth":return 'ð';
	case "241":case "ntilde":return 'ñ';
	case "242":case "ograve":return 'ò';
	case "243":case "oacute":return 'ó';
	case "244":case "ocirc":return 'ô';
	case "245":case "otilde":return 'õ';
	case "246":case "ouml":return 'ö';
	case "247":case "divide":return '÷';
	case "248":case "oslash":return 'ø';
	case "249":case "ugrave":return 'ù';
	case "250":case "uacute":return 'ú';
	case "251":case "ucirc":return 'û';
	case "252":case "uuml":return 'ü';
	case "253":case "yacute":return 'ý';
	case "254":case "thorn":return 'þ';
	case "255":case "yuml":return 'ÿ';
	case "880":return 'Ͱ';
	case "881":return 'ͱ';
	case "882":return 'Ͳ';
	case "883":return 'ͳ';
	case "884":return 'ʹ';
	case "885":return '͵';
	case "886":return 'Ͷ';
	case "887":return 'ͷ';
	case "890":return 'ͺ';
	case "891":return 'ͻ';
	case "892":return 'ͼ';
	case "893":return 'ͽ';
	case "894":return ';';
	case "900":return '΄';
	case "901":return '΅';
	case "902":return 'Ά';
	case "903":return '·';
	case "904":return 'Έ';
	case "905":return 'Ή';
	case "906":return 'Ί';
	case "908":return 'Ό';
	case "910":return 'Ύ';
	case "911":return 'Ώ';
	case "912":return 'ΐ';
	case "913":case "Alpha":return 'Α';
	case "914":case "Beta":return 'Β';
	case "915":case "Gamma":return 'Γ';
	case "916":case "Delta":return 'Δ';
	case "917":case "Epsilon":return 'Ε';
	case "918":case "Zeta":return 'Ζ';
	case "919":case "Eta":return 'Η';
	case "920":case "Theta":return 'Θ';
	case "921":case "Iota":return 'Ι';
	case "922":case "Kappa":return 'Κ';
	case "923":case "Lambda":return 'Λ';
	case "924":case "Mu":return 'Μ';
	case "925":case "Nu":return 'Ν';
	case "926":case "Xi":return 'Ξ';
	case "927":case "Omicron":return 'Ο';
	case "928":case "Pi":return 'Π';
	case "929":case "Rho":return 'Ρ';
	case "931":case "Sigma":return 'Σ';
	case "932":case "Tau":return 'Τ';
	case "933":case "Upsilon":return 'Υ';
	case "934":case "Phi":return 'Φ';
	case "935":case "Chi":return 'Χ';
	case "936":case "Psi":return 'Ψ';
	case "937":case "Omega":return 'Ω';
	case "938":return 'Ϊ';
	case "939":return 'Ϋ';
	case "940":return 'ά';
	case "941":return 'έ';
	case "942":return 'ή';
	case "943":return 'ί';
	case "944":return 'ΰ';
	case "945":case "alpha":return 'α';
	case "946":case "beta":return 'β';
	case "947":case "gamma":return 'γ';
	case "948":case "delta":return 'δ';
	case "949":case "epsilon":return 'ε';
	case "950":case "zeta":return 'ζ';
	case "951":case "eta":return 'η';
	case "952":case "theta":return 'θ';
	case "953":case "iota":return 'ι';
	case "954":case "kappa":return 'κ';
	case "955":case "lambda":return 'λ';
	case "956":case "mu":return 'μ';
	case "957":case "nu":return 'ν';
	case "958":case "xi":return 'ξ';
	case "959":case "omicron":return 'ο';
	case "960":case "pi":return 'π';
	case "961":case "rho":return 'ρ';
	case "962":case "sigmaf":return 'ς';
	case "963":case "sigma":return 'σ';
	case "964":case "tau":return 'τ';
	case "965":case "upsilon":return 'υ';
	case "966":case "phi":return 'φ';
	case "967":case "chi":return 'χ';
	case "968":case "psi":return 'ψ';
	case "969":case "omega":return 'ω';
	case "970":return 'ϊ';
	case "971":return 'ϋ';
	case "972":return 'ό';
	case "973":return 'ύ';
	case "974":return 'ώ';
	case "975":return 'Ϗ';
	case "976":return 'ϐ';
	case "977":case "thetasym":return 'ϑ';
	case "978":case "upsih":return 'ϒ';
	case "979":return 'ϓ';
	case "980":return 'ϔ';
	case "981":case "straightphi":return 'ϕ';
	case "982":case "piv":return 'ϖ';
	case "983":return 'ϗ';
	case "984":return 'Ϙ';
	case "985":return 'ϙ';
	case "986":return 'Ϛ';
	case "987":return 'ϛ';
	case "988":case "Gammad":return 'Ϝ';
	case "989":case "gammad":return 'ϝ';
	case "990":return 'Ϟ';
	case "991":return 'ϟ';
	case "992":return 'Ϡ';
	case "993":return 'ϡ';
	case "994":return 'Ϣ';
	case "995":return 'ϣ';
	case "996":return 'Ϥ';
	case "997":return 'ϥ';
	case "998":return 'Ϧ';
	case "999":return 'ϧ';
	case "1000":return 'Ϩ';
	case "1001":return 'ϩ';
	case "1002":return 'Ϫ';
	case "1003":return 'ϫ';
	case "1004":return 'Ϭ';
	case "1005":return 'ϭ';
	case "1006":return 'Ϯ';
	case "1007":return 'ϯ';
	case "1008":return 'ϰ';
	case "1009":return 'ϱ';
	case "1010":return 'ϲ';
	case "1011":return 'ϳ';
	case "1012":return 'ϴ';
	case "1013":case "straightepsilon":return 'ϵ';
	case "1014":case "backepsilon":return '϶';
	case "1015":return 'Ϸ';
	case "1016":return 'ϸ';
	case "1017":return 'Ϲ';
	case "1018":return 'Ϻ';
	case "1019":return 'ϻ';
	case "1020":return 'ϼ';
	case "1021":return 'Ͻ';
	case "1022":return 'Ͼ';
	case "1023":return 'Ͽ';
	case "8352":return '₠';
	case "8353":return '₡';
	case "8354":return '₢';
	case "8355":return '₣';
	case "8356":return '₤';
	case "8357":return '₥';
	case "8358":return '₦';
	case "8359":return '₧';
	case "8360":return '₨';
	case "8361":return '₩';
	case "8362":return '₪';
	case "8363":return '₫';
	case "8364":/*case "euro":*/return '€';
	case "8365":return '₭';
	case "8366":return '₮';
	case "8367":return '₯';
	case "8368":return '₰';
	case "8369":return '₱';
	case "8370":return '₲';
	case "8371":return '₳';
	case "8372":return '₴';
	case "8373":return '₵';
	case "8374":return '₶';
	case "8375":return '₷';
	case "8376":return '₸';
	case "8377":return '₹';
	case "8378":return '₺';
	case "8379":return '₻';
	case "8380":return '₼';
	case "8381":return '₽';
	case "8382":return '₾';
	case "8383":return '₿';
	case "8592":case "larr":return '←';
	case "8593":case "uarr":return '↑';
	case "8594":case "rarr":return '→';
	case "8595":case "darr":return '↓';
	case "8596":case "harr":return '↔';
	case "8597":return '↕';
	case "8598":return '↖';
	case "8599":return '↗';
	case "8600":return '↘';
	case "8601":return '↙';
	case "8602":return '↚';
	case "8603":return '↛';
	case "8604":return '↜';
	case "8605":return '↝';
	case "8606":return '↞';
	case "8607":return '↟';
	case "8608":return '↠';
	case "8609":return '↡';
	case "8610":return '↢';
	case "8611":return '↣';
	case "8612":return '↤';
	case "8613":return '↥';
	case "8614":return '↦';
	case "8615":return '↧';
	case "8616":return '↨';
	case "8617":return '↩';
	case "8618":return '↪';
	case "8619":return '↫';
	case "8620":return '↬';
	case "8621":return '↭';
	case "8622":return '↮';
	case "8623":return '↯';
	case "8624":return '↰';
	case "8625":return '↱';
	case "8626":return '↲';
	case "8627":return '↳';
	case "8628":return '↴';
	case "8629":case "crarr":return '↵';
	case "8630":return '↶';
	case "8631":return '↷';
	case "8632":return '↸';
	case "8633":return '↹';
	case "8634":return '↺';
	case "8635":return '↻';
	case "8636":return '↼';
	case "8637":return '↽';
	case "8638":return '↾';
	case "8639":return '↿';
	case "8640":return '⇀';
	case "8641":return '⇁';
	case "8642":return '⇂';
	case "8643":return '⇃';
	case "8644":return '⇄';
	case "8645":return '⇅';
	case "8646":return '⇆';
	case "8647":return '⇇';
	case "8648":return '⇈';
	case "8649":return '⇉';
	case "8650":return '⇊';
	case "8651":return '⇋';
	case "8652":return '⇌';
	case "8653":return '⇍';
	case "8654":return '⇎';
	case "8655":return '⇏';
	case "8656":case "lArr":return '⇐';
	case "8657":case "uArr":return '⇑';
	case "8658":case "rArr":return '⇒';
	case "8659":case "dArr":return '⇓';
	case "8660":case "hArr":return '⇔';
	case "8661":return '⇕';
	case "8662":return '⇖';
	case "8663":return '⇗';
	case "8664":return '⇘';
	case "8665":return '⇙';
	case "8666":return '⇚';
	case "8667":return '⇛';
	case "8668":return '⇜';
	case "8669":return '⇝';
	case "8670":return '⇞';
	case "8671":return '⇟';
	case "8672":return '⇠';
	case "8673":return '⇡';
	case "8674":return '⇢';
	case "8675":return '⇣';
	case "8676":return '⇤';
	case "8677":return '⇥';
	case "8678":return '⇦';
	case "8679":return '⇧';
	case "8680":return '⇨';
	case "8681":return '⇩';
	case "8682":return '⇪';
	case "8683":return '⇫';
	case "8684":return '⇬';
	case "8685":return '⇭';
	case "8686":return '⇮';
	case "8687":return '⇯';
	case "8688":return '⇰';
	case "8689":return '⇱';
	case "8690":return '⇲';
	case "8691":return '⇳';
	case "8692":return '⇴';
	case "8693":return '⇵';
	case "8694":return '⇶';
	case "8695":return '⇷';
	case "8696":return '⇸';
	case "8697":return '⇹';
	case "8698":return '⇺';
	case "8699":return '⇻';
	case "8700":return '⇼';
	case "8701":return '⇽';
	case "8702":return '⇾';
	case "8703":return '⇿';
	case "8704":case "forall":return '∀';
	case "8705":return '∁';
	case "8706":case "part":return '∂';
	case "8707":case "exist":return '∃';
	case "8708":return '∄';
	case "8709":case "empty":return '∅';
	case "8710":return '∆';
	case "8711":case "nabla":return '∇';
	case "8712":case "isin":return '∈';
	case "8713":case "notin":return '∉';
	case "8714":return '∊';
	case "8715":case "ni":return '∋';
	case "8716":return '∌';
	case "8717":return '∍';
	case "8718":return '∎';
	case "8719":case "prod":return '∏';
	case "8720":return '∐';
	case "8721":case "&sum;":return '∑';
	case "8722":case "minus":return '−';
	case "8723":return '∓';
	case "8724":return '∔';
	case "8725":return '∕';
	case "8726":return '∖';
	case "8727":case "lowast":return '∗';
	case "8728":return '∘';
	case "8729":return '∙';
	case "8730":return '√';
	case "8731":return '∛';
	case "8732":return '∜';
	case "8733":case "prop":return '∝';
	case "8734":case "infin":return '∞';
	case "8735":return '∟';
	case "8736":case "ang":return '∠';
	case "8737":return '∡';
	case "8738":return '∢';
	case "8739":return '∣';
	case "8740":return '∤';
	case "8741":return '∥';
	case "8742":return '∦';
	case "8743":case "and":return '∧';
	case "8744":case "or":return '∨';
	case "8745":case "cap":return '∩';
	case "8746":case "cup":return '∪';
	case "8747":case "int":return '∫';
	case "8748":return '∬';
	case "8749":return '∭';
	case "8750":return '∮';
	case "8751":return '∯';
	case "8752":return '∰';
	case "8753":return '∱';
	case "8754":return '∲';
	case "8755":return '∳';
	case "8756":return '∴';
	case "8757":return '∵';
	case "8758":return '∶';
	case "8759":return '∷';
	case "8760":return '∸';
	case "8761":return '∹';
	case "8762":return '∺';
	case "8763":return '∻';
	case "8764":case "sim":return '∼';
	case "8765":return '∽';
	case "8766":return '∾';
	case "8767":return '∿';
	case "8768":return '≀';
	case "8769":return '≁';
	case "8770":return '≂';
	case "8771":return '≃';
	case "8772":return '≄';
	case "8773":case "cong":return '≅';
	case "8774":return '≆';
	case "8775":return '≇';
	case "8776":case "asymp":return '≈';
	case "8777":return '≉';
	case "8778":return '≊';
	case "8779":return '≋';
	case "8780":return '≌';
	case "8781":return '≍';
	case "8782":return '≎';
	case "8783":return '≏';
	case "8784":return '≐';
	case "8785":return '≑';
	case "8786":return '≒';
	case "8787":return '≓';
	case "8788":return '≔';
	case "8789":return '≕';
	case "8790":return '≖';
	case "8791":return '≗';
	case "8792":return '≘';
	case "8793":return '≙';
	case "8794":return '≚';
	case "8795":return '≛';
	case "8796":return '≜';
	case "8797":return '≝';
	case "8798":return '≞';
	case "8799":return '≟';
	case "8800":case "ne":return '≠';
	case "8801":case "equiv":return '≡';
	case "8802":return '≢';
	case "8803":return '≣';
	case "8804":case "le":return '≤';
	case "8805":case "ge":return '≥';
	case "8806":return '≦';
	case "8807":return '≧';
	case "8808":return '≨';
	case "8809":return '≩';
	case "8810":return '≪';
	case "8811":return '≫';
	case "8812":return '≬';
	case "8813":return '≭';
	case "8814":return '≮';
	case "8815":return '≯';
	case "8816":return '≰';
	case "8817":return '≱';
	case "8818":return '≲';
	case "8819":return '≳';
	case "8820":return '≴';
	case "8821":return '≵';
	case "8822":return '≶';
	case "8823":return '≷';
	case "8824":return '≸';
	case "8825":return '≹';
	case "8826":return '≺';
	case "8827":return '≻';
	case "8828":return '≼';
	case "8829":return '≽';
	case "8830":return '≾';
	case "8831":return '≿';
	case "8832":return '⊀';
	case "8833":return '⊁';
	case "8834":case "sub":return '⊂';
	case "8835":case "sup":return '⊃';
	case "8836":case "nsub":return '⊄';
	case "8837":return '⊅';
	case "8838":case "sube":return '⊆';
	case "8839":case "supe":return '⊇';
	case "8840":return '⊈';
	case "8841":return '⊉';
	case "8842":return '⊊';
	case "8843":return '⊋';
	case "8844":return '⊌';
	case "8845":return '⊍';
	case "8846":return '⊎';
	case "8847":return '⊏';
	case "8848":return '⊐';
	case "8849":return '⊑';
	case "8850":return '⊒';
	case "8851":return '⊓';
	case "8852":return '⊔';
	case "8853":case "oplus":return '⊕';
	case "8854":return '⊖';
	case "8855":case "otimes":return '⊗';
	case "8856":return '⊘';
	case "8857":return '⊙';
	case "8858":return '⊚';
	case "8859":return '⊛';
	case "8860":return '⊜';
	case "8861":return '⊝';
	case "8862":return '⊞';
	case "8863":return '⊟';
	case "8864":return '⊠';
	case "8865":return '⊡';
	case "8866":return '⊢';
	case "8867":return '⊣';
	case "8868":return '⊤';
	case "8869":case "perp":return '⊥';
	case "8870":return '⊦';
	case "8871":return '⊧';
	case "8872":return '⊨';
	case "8873":return '⊩';
	case "8874":return '⊪';
	case "8875":return '⊫';
	case "8876":return '⊬';
	case "8877":return '⊭';
	case "8878":return '⊮';
	case "8879":return '⊯';
	case "8880":return '⊰';
	case "8881":return '⊱';
	case "8882":return '⊲';
	case "8883":return '⊳';
	case "8884":return '⊴';
	case "8885":return '⊵';
	case "8886":return '⊶';
	case "8887":return '⊷';
	case "8888":return '⊸';
	case "8889":return '⊹';
	case "8890":return '⊺';
	case "8891":return '⊻';
	case "8892":return '⊼';
	case "8893":return '⊽';
	case "8894":return '⊾';
	case "8895":return '⊿';
	case "8896":return '⋀';
	case "8897":return '⋁';
	case "8898":return '⋂';
	case "8899":return '⋃';
	case "8900":return '⋄';
	case "8901":case "sdot":return '⋅';
	case "8902":return '⋆';
	case "8903":return '⋇';
	case "8904":return '⋈';
	case "8905":return '⋉';
	case "8906":return '⋊';
	case "8907":return '⋋';
	case "8908":return '⋌';
	case "8909":return '⋍';
	case "8910":return '⋎';
	case "8911":return '⋏';
	case "8912":return '⋐';
	case "8913":return '⋑';
	case "8914":return '⋒';
	case "8915":return '⋓';
	case "8916":return '⋔';
	case "8917":return '⋕';
	case "8918":return '⋖';
	case "8919":return '⋗';
	case "8920":return '⋘';
	case "8921":return '⋙';
	case "8922":return '⋚';
	case "8923":return '⋛';
	case "8924":return '⋜';
	case "8925":return '⋝';
	case "8926":return '⋞';
	case "8927":return '⋟';
	case "8928":return '⋠';
	case "8929":return '⋡';
	case "8930":return '⋢';
	case "8931":return '⋣';
	case "8932":return '⋤';
	case "8933":return '⋥';
	case "8934":return '⋦';
	case "8935":return '⋧';
	case "8936":return '⋨';
	case "8937":return '⋩';
	case "8938":return '⋪';
	case "8939":return '⋫';
	case "8940":return '⋬';
	case "8941":return '⋭';
	case "8942":return '⋮';
	case "8943":return '⋯';
	case "8944":return '⋰';
	case "8945":return '⋱';
	case "8946":return '⋲';
	case "8947":return '⋳';
	case "8948":return '⋴';
	case "8949":return '⋵';
	case "8950":return '⋶';
	case "8951":return '⋷';
	case "8952":return '⋸';
	case "8953":return '⋹';
	case "8954":return '⋺';
	case "8955":return '⋻';
	case "8956":return '⋼';
	case "8957":return '⋽';
	case "8958":return '⋾';
	case "8959":return '⋿';
	case "9728":return '☀';
	case "9729":return '☁';
	case "9730":return '☂';
	case "9731":return '☃';
	case "9732":return '☄';
	case "9733":return '★';
	case "9734":return '☆';
	case "9735":return '☇';
	case "9736":return '☈';
	case "9737":return '☉';
	case "9738":return '☊';
	case "9739":return '☋';
	case "9740":return '☌';
	case "9741":return '☍';
	case "9742":return '☎';
	case "9743":return '☏';
	case "9744":return '☐';
	case "9745":return '☑';
	case "9746":return '☒';
	case "9747":return '☓';
	case "9748":return '☔';
	case "9749":return '☕';
	case "9750":return '☖';
	case "9751":return '☗';
	case "9752":return '☘';
	case "9753":return '☙';
	case "9754":return '☚';
	case "9755":return '☛';
	case "9756":return '☜';
	case "9757":return '☝';
	case "9758":return '☞';
	case "9759":return '☟';
	case "9760":return '☠';
	case "9761":return '☡';
	case "9762":return '☢';
	case "9763":return '☣';
	case "9764":return '☤';
	case "9765":return '☥';
	case "9766":return '☦';
	case "9767":return '☧';
	case "9768":return '☨';
	case "9769":return '☩';
	case "9770":return '☪';
	case "9771":return '☫';
	case "9772":return '☬';
	case "9773":return '☭';
	case "9774":return '☮';
	case "9775":return '☯';
	case "9776":return '☰';
	case "9777":return '☱';
	case "9778":return '☲';
	case "9779":return '☳';
	case "9780":return '☴';
	case "9781":return '☵';
	case "9782":return '☶';
	case "9783":return '☷';
	case "9784":return '☸';
	case "9785":return '☹';
	case "9786":return '☺';
	case "9787":return '☻';
	case "9788":return '☼';
	case "9789":return '☽';
	case "9790":return '☾';
	case "9791":return '☿';
	case "9792":return '♀';
	case "9793":return '♁';
	case "9794":return '♂';
	case "9795":return '♃';
	case "9796":return '♄';
	case "9797":return '♅';
	case "9798":return '♆';
	case "9799":return '♇';
	case "9800":return '♈';
	case "9801":return '♉';
	case "9802":return '♊';
	case "9803":return '♋';
	case "9804":return '♌';
	case "9805":return '♍';
	case "9806":return '♎';
	case "9807":return '♏';
	case "9808":return '♐';
	case "9809":return '♑';
	case "9810":return '♒';
	case "9811":return '♓';
	case "9812":return '♔';
	case "9813":return '♕';
	case "9814":return '♖';
	case "9815":return '♗';
	case "9816":return '♘';
	case "9817":return '♙';
	case "9818":return '♚';
	case "9819":return '♛';
	case "9820":return '♜';
	case "9821":return '♝';
	case "9822":return '♞';
	case "9823":return '♟';
	case "9824":case "spades":return '♠';
	case "9825":return '♡';
	case "9826":return '♢';
	case "9827":case "clubs":return '♣';
	case "9828":return '♤';
	case "9829":case "hearts":return '♥';
	case "9830":case "diams":return '♦';
	case "9831":return '♧';
	case "9832":return '♨';
	case "9833":return '♩';
	case "9834":return '♪';
	case "9835":return '♫';
	case "9836":return '♬';
	case "9837":return '♭';
	case "9838":return '♮';
	case "9839":return '♯';
	case "9840":return '♰';
	case "9841":return '♱';
	case "9842":return '♲';
	case "9843":return '♳';
	case "9844":return '♴';
	case "9845":return '♵';
	case "9846":return '♶';
	case "9847":return '♷';
	case "9848":return '♸';
	case "9849":return '♹';
	case "9850":return '♺';
	case "9851":return '♻';
	case "9852":return '♼';
	case "9853":return '♽';
	case "9854":return '♾';
	case "9855":return '♿';
	case "9856":return '⚀';
	case "9857":return '⚁';
	case "9858":return '⚂';
	case "9859":return '⚃';
	case "9860":return '⚄';
	case "9861":return '⚅';
	case "9862":return '⚆';
	case "9863":return '⚇';
	case "9864":return '⚈';
	case "9865":return '⚉';
	case "9866":return '⚊';
	case "9867":return '⚋';
	case "9868":return '⚌';
	case "9869":return '⚍';
	case "9870":return '⚎';
	case "9871":return '⚏';
	case "9872":return '⚐';
	case "9873":return '⚑';
	case "9874":return '⚒';
	case "9875":return '⚓';
	case "9876":return '⚔';
	case "9877":return '⚕';
	case "9878":return '⚖';
	case "9879":return '⚗';
	case "9880":return '⚘';
	case "9881":return '⚙';
	case "9882":return '⚚';
	case "9883":return '⚛';
	case "9884":return '⚜';
	case "9885":return '⚝';
	case "9886":return '⚞';
	case "9887":return '⚟';
	case "9888":return '⚠';
	case "9889":return '⚡';
	case "9890":return '⚢';
	case "9891":return '⚣';
	case "9892":return '⚤';
	case "9893":return '⚥';
	case "9894":return '⚦';
	case "9895":return '⚧';
	case "9896":return '⚨';
	case "9897":return '⚩';
	case "9898":return '⚪';
	case "9899":return '⚫';
	case "9900":return '⚬';
	case "9901":return '⚭';
	case "9902":return '⚮';
	case "9903":return '⚯';
	case "9904":return '⚰';
	case "9905":return '⚱';
	case "9906":return '⚲';
	case "9907":return '⚳';
	case "9908":return '⚴';
	case "9909":return '⚵';
	case "9910":return '⚶';
	case "9911":return '⚷';
	case "9912":return '⚸';
	case "9913":return '⚹';
	case "9914":return '⚺';
	case "9915":return 'v';
	case "9916":return '⚼';
	case "9917":return '⚽';
	case "9918":return '⚾';
	case "9919":return '⚿';
	case "9920":return '⛀';
	case "9921":return '⛁';
	case "9922":return '⛂';
	case "9923":return '⛃';
	case "9924":return '⛄';
	case "9925":return '⛅';
	case "9926":return '⛆';
	case "9927":return '⛇';
	case "9928":return '⛈';
	case "9929":return '⛉';
	case "9930":return '⛊';
	case "9931":return '⛋';
	case "9932":return '⛌';
	case "9933":return '⛍';
	case "9934":return '⛎';
	case "9935":return '⛏';
	case "9936":return '⛐';
	case "9937":return '⛑';
	case "9938":return '⛒';
	case "9939":return '⛓';
	case "9940":return '⛔';
	case "9941":return '⛕';
	case "9942":return '⛖';
	case "9943":return '⛗';
	case "9944":return '⛘';
	case "9945":return '⛙';
	case "9946":return '⛚';
	case "9947":return '⛛';
	case "9948":return '⛜';
	case "9949":return '⛝';
	case "9950":return '⛞';
	case "9951":return '⛟';
	case "9952":return '⛠';
	case "9953":return '⛡';
	case "9954":return '⛢';
	case "9955":return '⛣';
	case "9956":return '⛤';
	case "9957":return '⛥';
	case "9958":return '⛦';
	case "9959":return '⛧';
	case "9960":return '⛨';
	case "9961":return '⛩';
	case "9962":return '⛪';
	case "9963":return '⛫';
	case "9964":return '⛬';
	case "9965":return '⛭';
	case "9966":return '⛮';
	case "9967":return '⛯';
	case "9968":return '⛰';
	case "9969":return '⛱';
	case "9970":return '⛲';
	case "9971":return '⛳';
	case "9972":return '⛴';
	case "9973":return '⛵';
	case "9974":return '⛶';
	case "9975":return '⛷';
	case "9976":return '⛸';
	case "9977":return '⛹';
	case "9978":return '⛺';
	case "9979":return '⛻';
	case "9980":return '⛼';
	case "9981":return '⛽';
	case "9982":return '⛾';
	case "9983":return '⛿';
	default:return '\0';
	}
}
protected static CharSequence wrapTag(CharSequence val,CharSequence tag)
{
	String key=tag.toString();
	return '<'+key+'>'+val+"</"+key+'>';
}
public static Object parseXml(CharSequence xml)
{//System.out.println(xml);
	if(null==xml || xml.length()==0){
		return null;
	}
	Map<CharSequence,Object> obj=new HashMap<CharSequence,Object>();
	String tmpstr=xml.toString();
	int xi=tmpstr.indexOf("?>",5);
	if(-1!=xi){
		int i=tmpstr.indexOf("<?xml ");
		if(-1==i || xi<i){
			System.err.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			return null;
		}
		String[] kvs=tmpstr.substring(i+6,xi).trim().split(" ");
		Map<String,String> strpair=new HashMap<String,String>();
		obj.put("xml",strpair);
		for(i=0;kvs.length!=i;i++)
		{
			String[] kv=kvs[i].split("=");
			strpair.put(kv[0],kv[1].substring(1,kv[1].length()-1));
		}
		xml=tmpstr=tmpstr.substring(xi+2);
	}
	StringBuilder varstr=new StringBuilder(xml.length());
	xi=0;
	for(int dati=0,datj;(dati=tmpstr.indexOf("<!--",dati))!=-1;)
	{
		if((datj=tmpstr.indexOf("-->",dati+4))==-1){
			System.err.println("<!--=>not match");
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
	}//System.out.println(xml);
	tmpstr=xml.toString();
	varstr=new StringBuilder(xml.length());
	xi=0;
	for(int dati=3,datj;(dati=tmpstr.indexOf("<![CDATA[",dati))!=-1;)
	{
		if((datj=tmpstr.indexOf("]]>",dati+9))==-1){
			System.err.println("CDATA=>not match");
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
	}//System.out.println(xml);
	int[] idx=new int[3];
	idx[0]=xml.length();
	idx[1]=0;
	idx[2]=idx[0]-1;
	if(parseXml(obj,xml.toString(),idx)){
		return obj.size()==0 ? null : obj;
	}
	return null;
}
protected static boolean parseXml(Map<CharSequence,Object> parent,String xml,int[] idx)
{//System.out.println(xml.substring(idx[1],idx[2]+1));
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
	String tag=null;
	Map<CharSequence,Object> curent=new HashMap<CharSequence,Object>();
	if(xml.charAt(vari-1)=='/')
	{
		tag=parseAttrsVal(curent,xml.substring(idx[1]+1,vari-1)).toString();
		if(null==tag){
			return false;
		}
		if(curent.size()!=0){
			Codec.putMultiVal(parent,xml2txt(tag),curent);
		}
		return vari==idx[2] ? true : parseXml(parent,xml,tidx);
	}
	tag=parseAttrsVal(curent,xml.substring(idx[1]+1,vari)).toString();
	if(null==tag){
		return false;
	}
	if(curent.size()!=0){
		Codec.putMultiVal(parent,xml2txt(tag),curent);
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
		int i=xml.indexOf('>',tmp+tag.length()+1);
		char chr=xml.charAt(tmp+tag.length()+1);
		tmp=tmp-tag.length()-2;
		if(!Character.isWhitespace(chr) && '/'!=chr && '>'!=chr){
			continue;
		}
		if(xml.charAt(i-1)=='/'){
			continue;
		}
		vari=loci=vari+tag.length()+3;
		if(idx[2]<vari+tag.length()+2){
			return false;
		}
		vari=xml.indexOf("</"+tag+'>',vari);
		if(-1==vari || idx[2]<vari+tag.length()+2){
			return false;
		}
		while(((loci=xml.indexOf('<'+tag,loci))!=-1) && loci<vari){
			i=xml.indexOf('>',loci+tag.length()+1);
			chr=xml.charAt(loci+tag.length()+1);
			loci=loci+tag.length()+2;
			if(!Character.isWhitespace(chr) && '/'!=chr && '>'!=chr){
				continue;
			}
			if(xml.charAt(i-1)!='/'){
				cnti+=1;
			}
		}
		if(idx[2]==vari+tag.length()+2){
			if(0==cnti){
				break;
			}
			return false;
		}
	}
	for(int loci,i;0!=cnti;)
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
			i=xml.indexOf('>',loci+tag.length()+1);
			char chr=xml.charAt(loci+tag.length()+1);
			loci=loci+tag.length()+2;
			if(!Character.isWhitespace(chr) && '/'!=chr && '>'!=chr){
				continue;
			}
			if(xml.charAt(i-1)!='/'){
				cnti+=1;
			}
		}
		if(idx[2]==vari+tag.length()+2){
			if(1==cnti){
				break;
			}
			return false;
		}
		cnti--;
	}
	String cnt=xml.substring(idx[1],vari);
	if(cnt.indexOf('<')==-1){
		Codec.putMultiVal(parent,xml2txt(tag).toString(),xml2txt(cnt));
	}else{
		tidx[1]=idx[1];
		tidx[2]=vari-1;
		int size=curent.size();
		if(!parseXml(curent,xml,tidx)){
			return false;
		}
		if(0==size && curent.size()!=0){
			Codec.putMultiVal(parent,xml2txt(tag).toString(),curent);
		}
	}
	if(vari+tag.length()+2==idx[2]){
		return true;
	}
	idx[1]=vari+tag.length()+3;
	return parseXml(parent,xml,idx);
}
public static CharSequence parseAttrsVal(Map<CharSequence,Object> attr,CharSequence src)
{
	CharSequence k=null,tag=null;
	String str=src.toString();
	for(int i=1,n=str.length(),j=0,eqs=0;n!=i;)
	{
		char chr=str.charAt(i);
		if(Character.isWhitespace(chr))
		{
			if(null==tag){
				tag=str.substring(0,i);
			}else if(null==k){
				if(0!=j){
					k=str.substring(j,i);
					j=0;
				}
			}
			i++;
		}
		else if('='==chr)
		{
			if(0!=eqs){
				return null;
			}
			if(null==k){
				k=str.substring(j,i);
			}
			eqs++;
			j=0;
			i++;
		}
		else if('"'==chr)
		{
			if(null==k){
				return null;
			}
			j=str.indexOf('"',i+1);
			if(-1==j){
				return null;
			}
			if(j!=i+1){
				Codec.putMultiVal(attr,xml2txt(k),xml2txt(str.substring(i+1,j)));
			}
			k=null;
			i=j+1;
			j=0;
			eqs=0;
		}
		else
		{
			if(0==j && null!=tag){
				j=i;
			}
			i++;
		}
	}
	return null==tag ? src : tag;
}
protected XmlSerializer(){}
}