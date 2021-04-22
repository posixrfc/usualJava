package wcy.usual.ognl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ognls
{
@SuppressWarnings("unchecked")
public static Object getAnyObj(Object obj,CharSequence ognl)
{
	List<OgnlNode> nodes=parseOgnl(ognl);
	if(null==nodes){
		return null;
	}
	loopnode:for(int i=0,len=nodes.size();i!=len;i++)
	{
		if(null==obj){
			return null;
		}
		OgnlNode node=nodes.get(i);
		if(int.class==node.cls)
		{
			if(obj instanceof List){
				try{
					obj=((List<?>)obj).get(node.idx);
				}catch(IndexOutOfBoundsException iex){
					System.err.println("IndexOutOfBoundsException=>"+node);
					return null;
				}
				continue;
			}else if(obj.getClass().isArray()){
				try{
					obj=Array.get(obj,node.idx);
				}catch(ArrayIndexOutOfBoundsException aex){
					System.err.println("ArrayIndexOutOfBoundsException=>"+node);
					return null;
				}
				continue;
			}
			return null;
		}
		else if(char.class==node.cls)
		{
			if(null==node.str){
				return null;
			}
			if(obj instanceof Map){
				try{
					obj=((Map<String,?>)obj).get(node.str);
				}catch(RuntimeException rex){
					System.err.println("RuntimeException=>"+node);
					return null;
				}
			}else{
				Field pfd=null;
				try{
					pfd=obj.getClass().getField(node.str);
				}catch(NoSuchFieldException|SecurityException e){
					e.printStackTrace(System.err);
				}
				if(null!=pfd){
					try{
						obj=pfd.get(obj);
					}catch(IllegalArgumentException|IllegalAccessException e){
						e.printStackTrace(System.err);
						return null;
					}
					continue loopnode;
				}
				BeanInfo info=null;
				try{
					info=Introspector.getBeanInfo(obj.getClass(),Object.class);
				}catch(IntrospectionException iex){
					iex.printStackTrace(System.err);
					return null;
				}
				PropertyDescriptor[] pds=info.getPropertyDescriptors();
				if(null==pds || pds.length==0){
					System.err.println("PropertyDescriptor.zero=>"+node);
					return null;
				}
				for(PropertyDescriptor pd:pds)
				{
					if(!pd.getName().equals(node.str)){
						continue;
					}
					Method rmd=pd.getReadMethod();
					if(null==rmd){
						System.err.println("getReadMethod.null=>"+node);
						return null;
					}
					try{
						obj=rmd.invoke(obj);
					}catch(IllegalAccessException|IllegalArgumentException|InvocationTargetException ex){
						ex.printStackTrace(System.err);
						return null;
					}
					continue loopnode;
				}
				System.err.println("PropertyDescriptor.nosearch=>"+node);
				return null;
			}
		}
		else
		{
			System.err.println("class=>"+node);
			return null;
		}
	}
	return obj;
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
	}//System.out.println(ognl);
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
			try{
				idxbrackets=Integer.parseInt(src.substring(1+idxbrackets,i));
			}catch(NumberFormatException nex){
				System.err.println("NumberFormatException=>"+src.substring(1+idxbrackets,i));
				return null;
			}
			rdat.add(new OgnlNode(idxbrackets,int.class));
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