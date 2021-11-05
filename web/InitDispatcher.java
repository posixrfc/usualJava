package wcy.usual.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class InitDispatcher extends Object implements Filter
{
@Override
public void doFilter(ServletRequest sreq,ServletResponse srsp,FilterChain chain) throws IOException,ServletException
{
	HttpServletRequest oreq=(HttpServletRequest)sreq;
	HttpServletResponse orsp=(HttpServletResponse)srsp;
	switch(sreq.getDispatcherType()){
	case REQUEST:
		break;
	case ASYNC:
	case INCLUDE:
	case FORWARD:
		doNextFilter(oreq,orsp,chain);
		return;
	case ERROR:
		WebCore.noEligibleRoutine(oreq,orsp);
		return;
	}
	if(!oreq.getMethod().equals("GET")){
		oreq.setCharacterEncoding("UTF-8");//hrsp.setContentType("application/json");
	}
	String urireq=oreq.getRequestURI();///esb/pages/index.html
	String cpx=oreq.getContextPath();///esb
	if(null!=cpx && cpx.length()!=0){
		urireq=urireq.substring(cpx.length());///pages/index.html
	}
	if(null!=WebCore.publicuri && 0!=WebCore.publicuri.length){
		for(int i=0;i!=WebCore.publicuri.length;i++){
			if(urireq.startsWith(WebCore.publicuri[i])){
				doNextFilter(oreq,orsp,chain);
				return;
			}
		}
	}
	if(null!=WebCore.signinuri && 0!=WebCore.signinuri.length){
		for(int i=0;i!=WebCore.signinuri.length;i+=2){
			if(!urireq.startsWith(WebCore.signinuri[i])){
				continue;
			}
			int idx=i+1;
			if(idx==WebCore.signinuri.length){
				WebCore.noEligibleAuthorized(oreq,orsp);
				return;
			}
			if(null==WebCore.checker){
				HttpSession sess=oreq.getSession(false);
				if(null==sess){
					WebCore.noEligibleAuthorized(oreq,orsp);
					return;
				}
				if(sess.getAttribute(WebCore.signinuri[idx])==null){
					WebCore.noEligibleAuthorized(oreq,orsp);
					return;
				}
			}else{
				if(WebCore.checker.hasError(oreq,WebCore.signinuri[idx])){
					WebCore.noEligibleAuthorized(oreq,orsp);
					return;
				}
			}
			doNextFilter(oreq,orsp,chain);
			return;
		}
	}
	Object target=null;
	Method action=null;
	WebHandler handle=null;
	List<Package> pkgall=new ArrayList<Package>(9);
	uriloop:for(Object serv:(List<?>)WebCore.webhandler){
		Class<?> clazz=serv.getClass();
		Package opkg=clazz.getPackage();
		Deque<Package> qpkg=new LinkedList<Package>();
		while(null!=opkg){
			qpkg.addFirst(opkg);
			opkg=WebCore.getParentPackage(opkg);
		}
		pkgall.clear();
		while((opkg=qpkg.pollFirst())!=null){
			pkgall.add(opkg);
		}//System.out.println(pkgall);
		String urirgx="";
		pkgloop:for(int i=0,l=pkgall.size();i!=l;i++){
			opkg=pkgall.get(i);
			handle=opkg.getDeclaredAnnotation(WebHandler.class);
			if(null==handle){
				handle=opkg.getAnnotation(WebHandler.class);
			}
			if(null==handle || handle.pattern()==null || handle.pattern().length==0){
				continue;
			}
			for(String uripkg:handle.pattern()){
				if(urireq.matches(urirgx+uripkg+".*")){
					urirgx+=uripkg;
					continue pkgloop;
				}
			}
			if(urirgx.length()==0){
				continue uriloop;
			}else{
				WebCore.noEligibleRegular(oreq,orsp);
				return;
			}
		}
		handle=clazz.getDeclaredAnnotation(WebHandler.class);
		if(null==handle){
			handle=clazz.getAnnotation(WebHandler.class);
		}
		if(null!=handle && handle.pattern()!=null && handle.pattern().length!=0){
			boolean notmatch=true;
			for(String uricls:handle.pattern()){
				if(urireq.matches(urirgx+uricls+".*")){
					urirgx+=uricls;
					notmatch=false;
					break;
				}
			}
			if(notmatch){
				continue uriloop;
			}
		}
		for(Method func:clazz.getDeclaredMethods()){
			handle=func.getDeclaredAnnotation(WebHandler.class);
			if(null==handle){
				handle=func.getAnnotation(WebHandler.class);
			}
			if(null==handle){
				continue;
			}
			if(func.getParameterCount()!=2){
				continue;
			}
			Class<?>[] ptp=func.getParameterTypes();
			if(!HttpServletRequest.class.isAssignableFrom(ptp[0])){
				continue;
			}
			if(!HttpServletResponse.class.isAssignableFrom(ptp[1])){
				continue;
			}
			if(handle.pattern()==null || handle.pattern().length==0){
				if(null==target){
					target=serv;
					action=func;
				}
			}else{
				for(String uriact:handle.pattern()){
					if(urireq.matches(urirgx+uriact)){
						urirgx+=uriact;
						target=serv;
						action=func;
						break uriloop;
					}
				}
			}
		}
		if(null==target){
			if(urirgx.length()==0){
				continue uriloop;
			}else{
				WebCore.noEligibleRegular(oreq,orsp);
				return;
			}
		}else{
			break uriloop;
		}
	}
	if(null==target){
		WebCore.noEligibleForbidden(oreq,orsp);
		return;
	}
	byte reqlx=handle.method();
	if(WebHandler.NONE==reqlx){
		handle=target.getClass().getDeclaredAnnotation(WebHandler.class);
		if(null==handle){
			handle=target.getClass().getAnnotation(WebHandler.class);
		}
		if(null!=handle){
			reqlx=handle.method();
		}
	}
	if(WebHandler.NONE==reqlx){
		for(int i=pkgall.size()-1,l=-1;i!=l;i--){
			Package opkg=pkgall.get(i);
			handle=opkg.getDeclaredAnnotation(WebHandler.class);
			if(null==handle){
				handle=opkg.getAnnotation(WebHandler.class);
			}
			if(null==handle){
				continue;
			}
			if(handle.method()!=WebHandler.NONE){
				reqlx=handle.method();
				break;
			}
		}
	}
	if(WebHandler.NONE==reqlx){
		WebCore.noEligibleMethod(oreq,orsp);
		return;
	}
	switch(oreq.getMethod())
	{
	case "GET":
		if((reqlx&WebHandler.GET)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	case "POST":
		if((reqlx&WebHandler.POST)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	case "PUT":
		if((reqlx&WebHandler.PUT)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	case "DELETE":
		if((reqlx&WebHandler.DELETE)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	case "HEAD":
		if((reqlx&WebHandler.HEAD)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	case "TRACE":
		if((reqlx&WebHandler.TRACE)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	case "OPTIONS":
		if((reqlx&WebHandler.OPTIONS)==0){
			WebCore.noEligibleMethod(oreq,orsp);
			return;
		}
		break;
	default:
		WebCore.noEligibleMethod(oreq,orsp);
		return;
	}
	WebCore.setCommonHeader(oreq,orsp);
	if(null!=WebCore.preater){
		WebCore.preater.setDefaultHeader(oreq,orsp);
	}
	if(oreq.getMethod().contentEquals("OPTIONS")){
		executeHandler(oreq,orsp,target,action);
		return;
	}
	handle=action.getDeclaredAnnotation(WebHandler.class);
	if(null==handle){
		handle=action.getAnnotation(WebHandler.class);
	}
	String[] sign=handle.session();
	if(null==sign || sign.length==0){
		handle=target.getClass().getDeclaredAnnotation(WebHandler.class);
		if(null==handle){
			handle=target.getClass().getAnnotation(WebHandler.class);
		}
		if(null!=handle){
			sign=handle.session();
		}
	}
	if(null==sign || sign.length==0){
		for(int i=pkgall.size()-1,l=-1;i!=l;i--){
			Package opkg=pkgall.get(i);
			handle=opkg.getDeclaredAnnotation(WebHandler.class);
			if(null==handle){
				handle=opkg.getAnnotation(WebHandler.class);
			}
			if(null==handle){
				continue;
			}
			sign=handle.session();
			if(null!=sign && sign.length!=0){
				break;
			}
		}
	}
	if(null==sign || sign.length==0){
		executeHandler(oreq,orsp,target,action);
		return;
	}
	if(1==sign.length && sign[0].length()==0){
		executeHandler(oreq,orsp,target,action);
		return;
	}
	boolean suitable=true;
	if(null==WebCore.checker){
		HttpSession sess=oreq.getSession(false);
		if(null==sess){
			suitable=false;
		}else{
			for(String s:sign){
				if(sess.getAttribute(s)==null){
					suitable=false;
					break;
				}
			}
		}
	}else{
		for(String s:sign){
			if(WebCore.checker.hasError(oreq,s)){
				suitable=false;
				break;
			}
		}
	}
	if(suitable){
		executeHandler(oreq,orsp,target,action);
		return;
	}
	WebCore.noEligibleAuthorized(oreq,orsp);
}
protected void executeHandler(HttpServletRequest hreq,HttpServletResponse hrsp,Object target,Method action) throws IOException
{
	HttpRqst nreq=null;
	if(!(hreq instanceof HttpRqst)){
		try{
			nreq=new HttpRqst(hreq);
		}catch(ServletException|IOException e){
			e.printStackTrace(System.out);
			WebCore.noEligibleRoutine(hreq,hrsp);
			return;
		}
	}
	HttpRsps nrsp=new HttpRsps(nreq,hrsp);
	if(nreq.getReqTxt()!=null && nreq.getReqTxt().length()!=0 && nreq.getMimeType()==null){
		WebCore.noEligibleFormat(hreq,nrsp);
		return;
	}
	int flag=action.getModifiers();
	if(!Modifier.isPublic(flag)){
		action.setAccessible(true);
	}
	try{
		if(Modifier.isStatic(flag)){
			action.invoke(null,nreq,nrsp);
		}else{
			action.invoke(target,nreq,nrsp);
		}
		return;
	}catch(Exception e){
		e.printStackTrace(System.out);
	}
	WebCore.noEligibleRoutine(nreq,nrsp);
}
protected void doNextFilter(HttpServletRequest hreq,HttpServletResponse hrsp,FilterChain chain) throws IOException,ServletException
{
	try{
		HttpRqst nreq=new HttpRqst(hreq);
		HttpRsps nrsp=new HttpRsps(nreq,hrsp);
		chain.doFilter(nreq,nrsp);
	}catch(Exception e){
		e.printStackTrace(System.out);
		WebCore.noEligibleRoutine(hreq,hrsp);
		return;
	}
	switch(hrsp.getStatus())
	{
	case 200:
		return;
	case 404:
		WebCore.noEligibleResource(hreq,hrsp);
		break;
	case 500:
		WebCore.noEligibleRoutine(hreq,hrsp);
	}
}
@Override
public void init(FilterConfig fConfig)throws ServletException{}
@Override
public void destroy(){}
public InitDispatcher(){super();}
}