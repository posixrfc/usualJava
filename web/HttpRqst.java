package wcy.usual.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import wcy.usual.codec.Codec;
import wcy.usual.codec.json.JsonSerializer;
import wcy.usual.ognl.Ognls;
import wcy.usual.codec.xml.XmlSerializer;

public class HttpRqst implements HttpServletRequest
{
public HttpRqst(HttpServletRequest servletRequest) throws ServletException,IOException
{
	this.sreq=Objects.requireNonNull(servletRequest);
	enctype=sreq.getContentType();
	switch(sreq.getMethod())
	{
	case "GET":
		break;
	case "POST":
	case "DELETE":
	case "PUT":
	case "HEAD":
	case "OPTIONS":
	case "TRACE":
		if(null==enctype || enctype.length()==0){
			break;
		}
		switch(enctype)
		{
		case "application/x-www-form-urlencoded":
			if(sreq.getMethod().contentEquals("POST")){
				break;
			}//user=root&pass=123456&vcode=DCFP
			readText();
			if(null==rstr||rstr.length()==0){
				break;
			}
			Map<CharSequence,Object> values=new HashMap<CharSequence,Object>();
			String[] strval=rstr.split("&");
			for(String kvpair:strval){
				if(null==kvpair||kvpair.length()==0){
					continue;
				}
				String[] once=kvpair.split("=");
				if(null==once||2!=once.length){
					continue;
				}
				if(null==once[0]||once[0].length()==0||null==once[1]||once[1].length()==0){
					continue;
				}
				try{
					once[0]=URLDecoder.decode(once[0],"UTF-8");
					once[1]=URLDecoder.decode(once[1],"UTF-8");
				}catch(Exception ie){
					ie.printStackTrace(System.out);
					continue;
				}
				Codec.putMultiVal(values,once[0],once[1]);
			}
			if(values.size()!=0){
				rdat=values;
			}
			break;
		case "application/json":
			rdat=JsonSerializer.parseJson(readText());
			if(null==rdat){
				enctype=null;
			}
			break;
		case "text/plain":
			rdat=readText();
			break;
		case "text/xml":
		case "application/xml":
			rdat=XmlSerializer.parseXml(readText());
			if(null==rdat){
				enctype=null;
			}
			break;
		case "multipart/form-data":
			break;
		case "application/octet-stream":
			break;
		default:;
		}
	}
	String client=sreq.getHeader("User-Agent");
	mobile=(null!=client&&client.matches("^.+[^a-zA-Z0-9]((iPhone)|(iPad)|(Android))[^a-zA-Z0-9].+$"));
}
public final boolean mobile;
protected String enctype;
public String getMimeType(){
	return enctype;
}
public Cookie getCookie(String name)
{//Cookie: JSESSIONID=29FE88855D58210A67D5DA84392F902C; cknm=ckval; cknm1=ckval1; cknm2=ckval2
	Cookie[] cks=sreq.getCookies();
	if(null==cks || 0==cks.length){
		return null;
	}
	for(Cookie ck:cks){
		if(ck.getName().equals(name)){
			return ck;
		}
	}
	return null;
}
public Object getSharedSessionValue(String userid,String arg0){
	return null;
}
public boolean setSharedSessionValue(String arg0, String arg1){
	return true;
}
public String getFullContextPath(){
	String fullPath=sreq.getScheme()+"://"+sreq.getServerName();
	if(sreq.getScheme().equals("http")){
		if(sreq.getServerPort()!=80){
			fullPath=fullPath+':'+sreq.getServerPort();
		}
	}else{//https
		if(sreq.getServerPort()!=443){
			fullPath=fullPath+':'+sreq.getServerPort();
		}
	}
	return fullPath+sreq.getContextPath();//http://localhost:8080/esb
}
@Override
public AsyncContext getAsyncContext(){
	return sreq.getAsyncContext();
}
@Override
public Object getAttribute(String arg0){
	return sreq.getAttribute(arg0);
}
@Override
public Enumeration<String> getAttributeNames(){
	return sreq.getAttributeNames();
}
@Override
public String getCharacterEncoding(){
	return sreq.getCharacterEncoding();
}
@Override
public int getContentLength(){
	return sreq.getContentLength();
}
@Override
public long getContentLengthLong(){
	return sreq.getContentLengthLong();
}
@Override
public String getContentType(){
	return sreq.getContentType();
}
@Override
public DispatcherType getDispatcherType(){
	return sreq.getDispatcherType();
}
@Override
public ServletInputStream getInputStream() throws IOException{
	return sreq.getInputStream();
}
@Override
public String getLocalAddr(){
	return sreq.getLocalAddr();
}
@Override
public String getLocalName(){
	return sreq.getLocalName();
}
@Override
public int getLocalPort(){
	return sreq.getLocalPort();
}
@Override
public Locale getLocale(){
	return sreq.getLocale();
}
@Override
public Enumeration<Locale> getLocales(){
	return sreq.getLocales();
}
@Override
public String getParameter(String arg0)
{
	if(null==arg0||arg0.length()==0){
		return null;
	}
	if(sreq.getMethod().contentEquals("GET")){
		return sreq.getParameter(arg0);
	}
	if(null==enctype||!enctype.contentEquals("application/x-www-form-urlencoded")){
		return null;
	}
	if(sreq.getMethod().contentEquals("POST")){
		return sreq.getParameter(arg0);
	}
	if(!(rdat instanceof Map)){
		return null;
	}
	if(null==rdat){
		return null;
	}
	@SuppressWarnings("unchecked")
	Object any=((Map<CharSequence,Object>)rdat).get(arg0);
	if(any instanceof CharSequence){
		return any.toString();
	}
	if(any instanceof List){
		return ((List<?>)any).get(0).toString();
	}
	return null;
}
@SuppressWarnings("unchecked")
@Override
public Map<String,String[]> getParameterMap()
{
	if(sreq.getMethod().contentEquals("GET")){
		return sreq.getParameterMap();
	}
	if(null==enctype||!enctype.contentEquals("application/x-www-form-urlencoded")){
		return null;
	}
	if(sreq.getMethod().contentEquals("POST")){
		return sreq.getParameterMap();
	}
	if(!(rdat instanceof Map)){
		return null;
	}
	Map<String,String[]> rst=new HashMap<String,String[]>();
	Map<CharSequence,Object> values=(Map<CharSequence,Object>)rdat;
	for(CharSequence key:values.keySet()){
		Object any=values.get(key);
		if(any instanceof CharSequence){
			rst.put(key.toString(),new String[]{any.toString()});
			continue;
		}
		List<CharSequence> arr=(List<CharSequence>)any;
		String[] val=new String[arr.size()];
		for(int i=0;i!=val.length;i++){
			val[i]=arr.get(i).toString();
		}
		rst.put(key.toString(),val);
	}
	return rst;
}
@Override
public Enumeration<String> getParameterNames()
{
	if(sreq.getMethod().contentEquals("GET")){
		return sreq.getParameterNames();
	}
	if(null==enctype||!enctype.contentEquals("application/x-www-form-urlencoded")){
		return null;
	}
	if(sreq.getMethod().contentEquals("POST")){
		return sreq.getParameterNames();
	}
	if(!(rdat instanceof Map)){
		return null;
	}
	@SuppressWarnings("unchecked")
	Map<CharSequence,Object> values=((Map<CharSequence,Object>)rdat);
	List<String> keys=new ArrayList<String>(values.size());
	for(CharSequence key:values.keySet()){
		keys.add(key.toString());
	}
	return Collections.enumeration(keys);
}
@SuppressWarnings("unchecked")
@Override
public String[] getParameterValues(String arg0)
{
	if(null==arg0||arg0.length()==0){
		return null;
	}
	if(sreq.getMethod().contentEquals("GET")){
		return sreq.getParameterValues(arg0);
	}
	if(null==enctype||!enctype.contentEquals("application/x-www-form-urlencoded")){
		return null;
	}
	if(sreq.getMethod().contentEquals("POST")){
		return sreq.getParameterValues(arg0);
	}
	if(!(rdat instanceof Map<?,?>)){
		return null;
	}
	Object any=((Map<CharSequence,Object>)rdat).get(arg0);
	if(null==any){
		return null;
	}
	if(any instanceof CharSequence){
		return new String[]{any.toString()};
	}
	List<CharSequence> arr=(List<CharSequence>)any;
	String[] rst=new String[arr.size()];
	for(int i=0;i!=rst.length;i++){
		rst[i]=arr.get(i).toString();
	}
	return rst;
}
@Override
public String getProtocol(){
	return sreq.getProtocol();
}
@Override
public BufferedReader getReader() throws IOException{
	return sreq.getReader();
}
@Deprecated
@Override
public String getRealPath(String arg0){
	return sreq.getRealPath(arg0);
}
@Override
public String getRemoteAddr(){
	return sreq.getRemoteAddr();
}
@Override
public String getRemoteHost(){
	return sreq.getRemoteHost();
}
@Override
public int getRemotePort(){
	return sreq.getRemotePort();
}
@Override
public RequestDispatcher getRequestDispatcher(String arg0){
	return sreq.getRequestDispatcher(arg0);
}
@Override
public String getScheme(){
	return sreq.getScheme();
}
@Override
public String getServerName(){
	return sreq.getServerName();
}
@Override
public int getServerPort(){
	return sreq.getServerPort();
}
@Override
public ServletContext getServletContext(){
	return sreq.getServletContext();
}
@Override
public boolean isAsyncStarted(){
	return sreq.isAsyncStarted();
}
@Override
public boolean isAsyncSupported(){
	return sreq.isAsyncSupported();
}
@Override
public boolean isSecure(){
	return sreq.isSecure();
}
@Override
public void removeAttribute(String arg0){
	sreq.removeAttribute(arg0);
}
@Override
public void setAttribute(String arg0, Object arg1){
	sreq.setAttribute(arg0, arg1);
}
@Override
public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException{
	sreq.setCharacterEncoding(arg0);
}
@Override
public AsyncContext startAsync() throws IllegalStateException{
	return sreq.startAsync();
}
@Override
public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException{
	return sreq.startAsync(arg0, arg1);
}
@Override
public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException{
	return sreq.authenticate(arg0);
}
@Override
public String changeSessionId(){
	return sreq.changeSessionId();
}
@Override
public String getAuthType(){
	return sreq.getAuthType();
}
@Override
public String getContextPath(){
	return sreq.getContextPath();
}
@Override
public Cookie[] getCookies(){
	return sreq.getCookies();
}
@Override
public long getDateHeader(String arg0){
	return sreq.getDateHeader(arg0);
}
@Override
public String getHeader(String arg0){
	return sreq.getHeader(arg0);
}
@Override
public Enumeration<String> getHeaderNames(){
	return sreq.getHeaderNames();
}
@Override
public Enumeration<String> getHeaders(String arg0){
	return sreq.getHeaders(arg0);
}
@Override
public int getIntHeader(String arg0){
	return sreq.getIntHeader(arg0);
}
@Override
public String getMethod(){
	return sreq.getMethod();
}
@Override
public Part getPart(String arg0) throws IOException, ServletException{
	return sreq.getPart(arg0);
}
@Override
public Collection<Part> getParts() throws IOException, ServletException{
	return sreq.getParts();
}
@Override
public String getPathInfo(){
	return sreq.getPathInfo();
}
@Override
public String getPathTranslated(){
	return sreq.getPathTranslated();
}
@Override
public String getQueryString(){
	return sreq.getQueryString();
}
@Override
public String getRemoteUser(){
	return sreq.getRemoteUser();
}
@Override
public String getRequestURI(){
	return sreq.getRequestURI();
}
@Override
public StringBuffer getRequestURL(){
	return sreq.getRequestURL();
}
@Override
public String getRequestedSessionId(){
	return sreq.getRequestedSessionId();
}
@Override
public String getServletPath(){
	return sreq.getServletPath();
}
@Override
public HttpSession getSession(){
	return sreq.getSession();
}
@Override
public HttpSession getSession(boolean arg0){
	return sreq.getSession(arg0);
}
@Override
public Principal getUserPrincipal(){
	return sreq.getUserPrincipal();
}
@Override
public boolean isRequestedSessionIdFromCookie(){
	return sreq.isRequestedSessionIdFromCookie();
}
@Override
public boolean isRequestedSessionIdFromURL(){
	return sreq.isRequestedSessionIdFromURL();
}
@Deprecated
@Override
public boolean isRequestedSessionIdFromUrl(){
	return sreq.isRequestedSessionIdFromUrl();
}
@Override
public boolean isRequestedSessionIdValid(){
	return sreq.isRequestedSessionIdValid();
}
@Override
public boolean isUserInRole(String arg0){
	return sreq.isUserInRole(arg0);
}
@Override
public void login(String arg0, String arg1) throws ServletException{
	sreq.login(arg0, arg1);
}
@Override
public void logout() throws ServletException{
	sreq.logout();
}
@Override
public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException{
	return sreq.upgrade(arg0);
}
public Object getReqObj(CharSequence ognl)
{
	if(null==ognl || ognl.length()==0){
		if("text/plain".equals(enctype)){
			return rstr;
		}
		return rdat;
	}
	String qsval=sreq.getParameter(ognl.toString());
	if(null==rstr || null==rdat){
		return qsval;
	}
	switch(sreq.getMethod())
	{
	case "GET":
		return qsval;
	case "POST":
	case "DELETE":
	case "PUT":
	case "HEAD":
	case "OPTIONS":
	case "TRACE":
		if(null==enctype && enctype.length()==0){
			return qsval;
		}
		switch(enctype)
		{
		case "application/x-www-form-urlencoded":
			return qsval;
		case "text/plain":
			return qsval;
		case "application/json":
		case "text/xml"://[1].persons[0].boos[3][2].author.teacher.name.0
		case "application/xml":
			return Ognls.getAnyObj(rdat,ognl);
		case "multipart/form-data":
			return qsval;
		case "application/octet-stream":
			return qsval;
		default:
			return qsval;
		}
	}
	return qsval;
}
public String getReqTxt()
{
	return rstr;
}
protected String readText() throws IOException, ServletException
{
	String mime=sreq.getContentType(),enc="UTF-8";
	if(null==mime){
		mime="text/plain";
	}else{
		int idx=mime.lastIndexOf('=');
		if(-1!=idx){
			enc=mime.substring(idx+1);
			idx=mime.indexOf(';');
			mime=mime.substring(0,idx);
		}
	}
	sreq.setCharacterEncoding(enc);
	byte[] bytes=new byte[sreq.getContentLength()];
	sreq.getInputStream().read(bytes);//byte[] bytes=sreq.getInputStream().readAllBytes();
	if(null==bytes || 0==bytes.length){
		return null;
	}
	rstr=new String(bytes,enc);
	return rstr;
}
protected String rstr;
protected Object rdat;
protected final HttpServletRequest sreq;
}