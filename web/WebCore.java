package wcy.usual.web;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wcy.usual.db.Resource;

public abstract class WebCore
{
public static void init(ServletContext ctx,String... pkgs)
{
	Dynamic fc=ctx.addFilter("init.dispatcher.filter",InitDispatcher.class);
	fc.setAsyncSupported(false);
	fc.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST,DispatcherType.FORWARD,DispatcherType.INCLUDE,DispatcherType.ERROR,DispatcherType.ASYNC),false,"/*");
	
	List<String> scls=new ArrayList<String>();
	File rootdir=new File(ctx.getRealPath("/WEB-INF/classes"));//System.out.println(rootdir);
	if(null==pkgs || pkgs.length==0){
		List<String> tmp=getClassNames(null,rootdir);
		if(null!=tmp){
			scls.addAll(tmp);
		}
	}else{
		pkgloop:for(String pkg:pkgs){
			String[] comps=pkg.split("\\.");
			File pkgdir=rootdir;
			File[] tfls=null;
			for(int i=0;i!=comps.length;i++){
				final String part=comps[i];
				tfls=pkgdir.listFiles(tfile->tfile.getName().contentEquals(part) && tfile.isDirectory());
				if(null==tfls || tfls.length!=1){
					continue pkgloop;
				}
				pkgdir=tfls[0];
			}
			List<String> tmp=getClassNames(pkg,pkgdir);
			if(null!=tmp){
				scls.addAll(tmp);
			}
		}
	}
	if(scls.size()==0){
		return;
	}
	List<Class<?>> ocls=new ArrayList<Class<?>>(scls.size());
	ClassLoader cldr=WebCore.class.getClassLoader();
	for(int i=0,l=scls.size();i!=l;){
		String sc=scls.get(i);
		try{
			ocls.add(cldr.loadClass(sc));
		}catch(ClassNotFoundException e){
			e.printStackTrace(System.out);
			scls.remove(i);
			l--;
			continue;
		}
		i++;
	}
	if(ocls.size()==0){
		return;
	}
	scls.clear();
	clsloop:for(Class<?> cls:ocls){
		if(cls.isAnnotationPresent(WebHandler.class)){
			createObject(cls,false);
			continue;
		}
		if(cls.isAnnotationPresent(Component.class)){
			createObject(cls,true);
			continue;
		}
		Package opkg=cls.getPackage();
		while(null!=opkg){
			if(opkg.isAnnotationPresent(WebHandler.class)){
				createObject(cls,false);
				continue clsloop;
			}
			if(opkg.isAnnotationPresent(Component.class)){
				createObject(cls,true);
				continue clsloop;
			}
			opkg=getParentPackage(opkg);
		}
		for(Method func:cls.getDeclaredMethods()){
			if(func.isAnnotationPresent(WebHandler.class)){
				createObject(cls,false);
				continue clsloop;
			}
			if(func.isAnnotationPresent(Component.class)){
				createObject(cls,true);
				continue clsloop;
			}
		}
	}
	for(int i=0;resource.size()!=i;){
		try{
			if(resource.get(i).init()){
				i++;
			}else{
				resource.remove(i);
			}
		}catch(Exception e){
			e.printStackTrace(System.out);
			resource.remove(i);
		}
	}
	for(Object web:webhandler){
		Class<?> cls=web.getClass();
		fdlp:for(Field prop:cls.getDeclaredFields()){
			if(!prop.isAnnotationPresent(IocInject.class)){
				continue;
			}
			if(!prop.isAccessible()){
				prop.setAccessible(true);
			}
			Class<?> fcs=prop.getDeclaringClass();
			for(Object pojo:components){
				if(fcs.isInstance(pojo)){
					try{
						prop.set(web,pojo);
					}catch(IllegalArgumentException | IllegalAccessException e){
						e.printStackTrace(System.out);
					}
					continue fdlp;
				}
			}
			for(Object hand:webhandler){
				if(fcs.isInstance(hand)){
					try{
						prop.set(web,hand);
					}catch(IllegalArgumentException | IllegalAccessException e){
						e.printStackTrace(System.out);
					}
					continue fdlp;
				}
			}
		}
	}
	for(Object obj:components){
		Class<?> cls=obj.getClass();
		fdlp:for(Field prop:cls.getDeclaredFields()){
			if(!prop.isAnnotationPresent(IocInject.class)){
				continue;
			}
			if(!prop.isAccessible()){
				prop.setAccessible(true);
			}
			Class<?> fcs=prop.getDeclaringClass();
			for(Object pojo:components){
				if(fcs.isInstance(pojo)){
					try{
						prop.set(obj,pojo);
					}catch(IllegalArgumentException | IllegalAccessException e){
						e.printStackTrace(System.out);
					}
					continue fdlp;
				}
			}
			for(Object hand:webhandler){
				if(fcs.isInstance(hand)){
					try{
						prop.set(obj,hand);
					}catch(IllegalArgumentException | IllegalAccessException e){
						e.printStackTrace(System.out);
					}
					continue fdlp;
				}
			}
		}
	}
	System.out.println("------WebCore------init-------finished------");
}
protected static void createObject(Class<?> cls,boolean pojo)
{
	if(cls.isAnonymousClass()){
		return;
	}
	if(cls.isAnnotation()){
		return;
	}
	if(cls.isArray()){
		return;
	}
	if(cls.isEnum()){
		return;
	}
	if(cls.isInterface()){
		return;
	}
	if(cls.isLocalClass()){
		return;
	}
	if(cls.isMemberClass()){
		return;
	}
	if(cls.isPrimitive()){
		return;
	}
	if(cls.isSynthetic()){
		return;
	}
	if(Modifier.isAbstract(cls.getModifiers())){
		return;
	}
	Constructor<?>[] csr=cls.getDeclaredConstructors();
	if(null==csr || csr.length==0){
		return;
	}
	Constructor<?> c=null;
	for(Constructor<?> t:csr){
		if(t.getParameterCount()==0){
			c=t;
			break;
		}
	}
	if(null==c){//System.out.println("----WebCore------notClass-------"+cls.getName()+"-----");
		return;
	}
	if(!c.isAccessible()){
		c.setAccessible(true);
	}
	try{
		Object o=c.newInstance();
		if(o instanceof Resource){
			resource.add((Resource)o);
		}
		if(pojo){
			components.add(o);//System.out.println("----WebCore------createComponent-------"+cls.getName()+"-----");
		}else{
			webhandler.add(o);//System.out.println("----WebCore------createWebHandler-------"+cls.getName()+"-----");
		}
	}catch(Exception e){
		e.printStackTrace(System.out);
	}
}
public static void kill()
{
	for(int i=0;resource.size()!=i;i++){
		try{
			resource.get(i).kill();
		}catch(Exception e){
			e.printStackTrace(System.out);
		}
	}
	System.out.println("------WebCore------kill-------finished------");
}
protected static List<String> getClassNames(String folder,File pkgdir){
	File[] fs=pkgdir.listFiles();
	if(null==fs || fs.length==0){
		return null;
	}
	List<String> name=new ArrayList<String>();
	for(int i=0;i!=fs.length;i++){
		String fnm=fs[i].getName();
		if(fnm.matches("^(\\w|\\$)+\\.class$") && fs[i].isFile() && fs[i].canRead()){
			
			if(null==folder){
				name.add(fnm.substring(0,fnm.length()-6));
			}else{
				name.add(folder+'.'+fnm.substring(0,fnm.length()-6));
			}
			continue;
		}
		if(fnm.matches("^(\\w|\\$)+$") && fs[i].isDirectory() && fs[i].canExecute()){
			List<String> n=null;
			if(null==folder){
				n=getClassNames(fnm,fs[i]);
			}else{
				n=getClassNames(folder+'.'+fnm,fs[i]);
			}
			if(null!=n){
				name.addAll(n);
			}
		}
	}
	return name.size()==0?null:name;
}
protected static Package getParentPackage(Package pkg){
	String cpnm=pkg.getName();
	if(null==cpnm || cpnm.length()==0){
		return null;
	}
	int idx=cpnm.lastIndexOf('.');
	if(-1==idx){
		return null;
	}
	return Package.getPackage(cpnm.substring(0,idx));
}
protected static List<Package> getChildrenPackages(Package pkg){
	return null;
}
protected static List<Class<?>> getIncludeClasses(Package pkg){
	return null;
}
public static void noErrorFinished(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(200);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("OK");
}
public static void noEligibleReceipt(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(402);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Payment Required");
}
public static void noEligibleParameter(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(498);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Parameter Required");
}
public static void noEligibleMimeType(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(415);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Unsupported Media Type");
}
public static void noEligibleRegular(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(400);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Bad Request");
}
public static void noEligibleResource(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(404);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Not Found");
}
public static void noEligibleFormat(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(499);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Payload Format Error");
}
public static void noEligibleRoutine(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(500);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Internal Server Error");
}
public static void noEligibleAuthorized(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(401);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Unauthorized");
}
public static void noEligibleForbidden(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(403);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Forbidden");
}
public static void noEligibleMethod(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(405);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().write("Method Not Allowed");
}
public static void setCommonHeader(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	hrsp.setHeader("Server","cloudflare");
	hrsp.setContentType("text/plain");
	hrsp.setCharacterEncoding("UTF-8");
	if(null==hreq){
		hrsp.setHeader("Access-Control-Allow-Origin","*");
		hrsp.setHeader("Access-Control-Allow-Headers","*");
		hrsp.setHeader("Access-Control-Allow-Methods","*");
	}else{
		String origin=hreq.getHeader("Origin");
		if(null==origin || origin.length()==0){
			hrsp.setHeader("Access-Control-Allow-Origin","*");
		}else{
			hrsp.setHeader("Access-Control-Allow-Origin",origin);
		}
		String head=hreq.getHeader("Access-Control-Request-Headers");
		if(null==head || head.length()==0){
			hrsp.setHeader("Access-Control-Allow-Headers","*");
		}else{
			hrsp.setHeader("Access-Control-Allow-Headers",head);
		}
		String reqm=hreq.getHeader("Access-Control-Request-Method");
		if(null==reqm || reqm.length()==0){
			hrsp.setHeader("Access-Control-Allow-Methods","*");
		}else{
			hrsp.setHeader("Access-Control-Allow-Methods",reqm);
		}
	}
	hrsp.setIntHeader("Access-Control-Max-Age",999999);
	hrsp.setHeader("Access-Control-Allow-Credentials","true");
	hrsp.setHeader("Access-Control-Expose-Headers","*");//X-My-Custom-Header,X-Another-Custom-Header
}
public static void responseOptions(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException
{
	if(hrsp.isCommitted()){
		return;
	}
	hrsp.reset();
	hrsp.setStatus(200);
	setCommonHeader(hreq,hrsp);
	hrsp.getWriter().close();
}
public static String[] publicuri;
public static String[] signinuri;
public static SessionChecker checker;
public static ResponsePretreater preater;
protected static final List<Object> webhandler=new ArrayList<Object>();
protected static final List<Object> components=new ArrayList<Object>();
protected static final List<Resource> resource=new ArrayList<Resource>();
}