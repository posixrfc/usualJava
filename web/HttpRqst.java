package wcy.usual.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
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

import wcy.usual.codec.json.JsonSerializer;
import wcy.usual.ognl.Ognls;
import wcy.usual.codec.xml.XmlSerializer;

public final class HttpRqst extends Object implements HttpServletRequest
{
public HttpRqst(HttpServletRequest servletRequest) throws ServletException, IOException{
	this.servletRequest=Objects.requireNonNull(servletRequest);
	enctype=servletRequest.getContentType();
	switch(servletRequest.getMethod())
	{
	case "GET":
		break;
	case "POST":
	case "DELETE":
	case "PUT":
	case "HEAD":
	case "OPTIONS":
	case "TRACE":
		if(null==enctype && enctype.length()==0){
			break;
		}
		switch(enctype)
		{
		case "application/x-www-form-urlencoded":
			break;
		case "application/json":
			rdat=JsonSerializer.parseJson(readText());
			break;
		case "text/plain":
			rdat=readText();
			break;
		case "text/xml":
		case "application/xml":
			rdat=XmlSerializer.parseXml(readText());
			break;
		case "multipart/form-data":
			break;
		case "application/octet-stream":
			break;
		default:;
		}
	}
	String client=servletRequest.getHeader("User-Agent");
	mobile=(null!=client&&client.matches("^.+[^a-zA-Z0-9]((iPhone)|(iPad)|(Android))[^a-zA-Z0-9].+$"));
}
public final boolean mobile;
public final String enctype;
public Cookie getCookie(String name)
{
	Cookie[] cks=servletRequest.getCookies();
	if(null==cks || 0==cks.length){
		return null;
	}
	for(Cookie ck:cks){
		if(ck.getName().equals(name)){
			return ck;
		}
	}
	return null;
}//servletRequest.getSession().getAttribute(null);servletRequest.getSession().setAttribute(null, null);
public Object getSharedSessionValue(String userid,String arg0)
{
	return null;
}
public boolean setSharedSessionValue(String arg0, String arg1)
{
	return true;
}
public String getFullContextPath()
{
	String fullPath=servletRequest.getScheme()+"://"+servletRequest.getServerName();
	if(servletRequest.getScheme().equals("http")){
		if(servletRequest.getServerPort()!=80){
			fullPath=fullPath+':'+servletRequest.getServerPort();
		}
	}else{//https
		if(servletRequest.getServerPort()!=443){
			fullPath=fullPath+':'+servletRequest.getServerPort();
		}
	}
	return fullPath+servletRequest.getContextPath();//http://localhost:8080/esb
}
@Override
public AsyncContext getAsyncContext() {
	return servletRequest.getAsyncContext();
}
@Override
public Object getAttribute(String arg0) {
	return servletRequest.getAttribute(arg0);
}
@Override
public Enumeration<String> getAttributeNames() {
	return servletRequest.getAttributeNames();
}
@Override
public String getCharacterEncoding() {
	return servletRequest.getCharacterEncoding();
}
@Override
public int getContentLength() {
	return servletRequest.getContentLength();
}
@Override
public long getContentLengthLong() {
	return servletRequest.getContentLengthLong();
}
@Override
public String getContentType() {
	return servletRequest.getContentType();
}
@Override
public DispatcherType getDispatcherType() {
	return servletRequest.getDispatcherType();
}
@Override
public ServletInputStream getInputStream() throws IOException {
	return servletRequest.getInputStream();
}
@Override
public String getLocalAddr() {
	return servletRequest.getLocalAddr();
}
@Override
public String getLocalName() {
	return servletRequest.getLocalName();
}
@Override
public int getLocalPort() {
	return servletRequest.getLocalPort();
}
@Override
public Locale getLocale() {
	return servletRequest.getLocale();
}
@Override
public Enumeration<Locale> getLocales() {
	return servletRequest.getLocales();
}
@Override
public String getParameter(String arg0){
	return servletRequest.getParameter(arg0);
}
@Override
public Map<String,String[]> getParameterMap() {
	return servletRequest.getParameterMap();
}
@Override
public Enumeration<String> getParameterNames() {
	return servletRequest.getParameterNames();
}
@Override
public String[] getParameterValues(String arg0) {
	return servletRequest.getParameterValues(arg0);
}
@Override
public String getProtocol() {
	return servletRequest.getProtocol();
}
@Override
public BufferedReader getReader() throws IOException {
	return servletRequest.getReader();
}
@Deprecated
@Override
public String getRealPath(String arg0) {
	return servletRequest.getRealPath(arg0);
}
@Override
public String getRemoteAddr() {
	return servletRequest.getRemoteAddr();
}
@Override
public String getRemoteHost() {
	return servletRequest.getRemoteHost();
}
@Override
public int getRemotePort() {
	return servletRequest.getRemotePort();
}
@Override
public RequestDispatcher getRequestDispatcher(String arg0) {
	return servletRequest.getRequestDispatcher(arg0);
}
@Override
public String getScheme() {
	return servletRequest.getScheme();
}
@Override
public String getServerName() {
	return servletRequest.getServerName();
}
@Override
public int getServerPort() {
	return servletRequest.getServerPort();
}
@Override
public ServletContext getServletContext() {
	return servletRequest.getServletContext();
}
@Override
public boolean isAsyncStarted() {
	return servletRequest.isAsyncStarted();
}
@Override
public boolean isAsyncSupported() {
	return servletRequest.isAsyncSupported();
}
@Override
public boolean isSecure() {
	return servletRequest.isSecure();
}
@Override
public void removeAttribute(String arg0) {
	servletRequest.removeAttribute(arg0);
}
@Override
public void setAttribute(String arg0, Object arg1) {
	servletRequest.setAttribute(arg0, arg1);
}
@Override
public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
	servletRequest.setCharacterEncoding(arg0);
}
@Override
public AsyncContext startAsync() throws IllegalStateException {
	return servletRequest.startAsync();
}
@Override
public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
	return servletRequest.startAsync(arg0, arg1);
}
@Override
public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
	return servletRequest.authenticate(arg0);
}
@Override
public String changeSessionId() {
	return servletRequest.changeSessionId();
}
@Override
public String getAuthType() {
	return servletRequest.getAuthType();
}
@Override
public String getContextPath() {
	return servletRequest.getContextPath();
}
@Override
public Cookie[] getCookies() {
	return servletRequest.getCookies();
}
@Override
public long getDateHeader(String arg0) {
	return servletRequest.getDateHeader(arg0);
}
@Override
public String getHeader(String arg0) {
	return servletRequest.getHeader(arg0);
}
@Override
public Enumeration<String> getHeaderNames() {
	return servletRequest.getHeaderNames();
}
@Override
public Enumeration<String> getHeaders(String arg0) {
	return servletRequest.getHeaders(arg0);
}
@Override
public int getIntHeader(String arg0) {
	return servletRequest.getIntHeader(arg0);
}
@Override
public String getMethod() {
	return servletRequest.getMethod();
}
@Override
public Part getPart(String arg0) throws IOException, ServletException {
	return servletRequest.getPart(arg0);
}
@Override
public Collection<Part> getParts() throws IOException, ServletException {
	return servletRequest.getParts();
}
@Override
public String getPathInfo() {
	return servletRequest.getPathInfo();
}
@Override
public String getPathTranslated() {
	return servletRequest.getPathTranslated();
}
@Override
public String getQueryString() {
	return servletRequest.getQueryString();
}
@Override
public String getRemoteUser() {
	return servletRequest.getRemoteUser();
}
@Override
public String getRequestURI() {
	return servletRequest.getRequestURI();
}
@Override
public StringBuffer getRequestURL() {
	return servletRequest.getRequestURL();
}
@Override
public String getRequestedSessionId() {
	return servletRequest.getRequestedSessionId();
}
@Override
public String getServletPath() {
	return servletRequest.getServletPath();
}
@Override
public HttpSession getSession() {
	return servletRequest.getSession();
}
@Override
public HttpSession getSession(boolean arg0) {
	return servletRequest.getSession(arg0);
}
@Override
public Principal getUserPrincipal() {
	return servletRequest.getUserPrincipal();
}
@Override
public boolean isRequestedSessionIdFromCookie() {
	return servletRequest.isRequestedSessionIdFromCookie();
}
@Override
public boolean isRequestedSessionIdFromURL() {
	return servletRequest.isRequestedSessionIdFromURL();
}
@Deprecated
@Override
public boolean isRequestedSessionIdFromUrl() {
	return servletRequest.isRequestedSessionIdFromUrl();
}
@Override
public boolean isRequestedSessionIdValid(){
	return servletRequest.isRequestedSessionIdValid();
}
@Override
public boolean isUserInRole(String arg0){
	return servletRequest.isUserInRole(arg0);
}
@Override
public void login(String arg0, String arg1) throws ServletException{
	servletRequest.login(arg0, arg1);
}
@Override
public void logout() throws ServletException {
	servletRequest.logout();
}
@Override
public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException{
	return servletRequest.upgrade(arg0);
}
public Object getReqObj(CharSequence ognl)
{
	if(null==ognl || ognl.length()==0){
		if("text/plain".equals(enctype)){
			return rstr;
		}
		return rdat;
	}
	String qsval=servletRequest.getParameter(ognl.toString());
	if(null==rstr || null==rdat){
		return qsval;
	}
	switch(servletRequest.getMethod())
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
public String getReqTxt(){
	return rstr;
}
protected String readText() throws IOException, ServletException{
	servletRequest.setCharacterEncoding("UTF-8");
	BufferedReader br=servletRequest.getReader();
	StringBuilder buf=new StringBuilder();
	String str;
	while((str=br.readLine())!=null){
		buf.append(str);
	}//System.out.println(buf);
	return rstr=buf.length()==0 ? null : buf.toString();
}
protected String rstr;
protected Object rdat;
private final HttpServletRequest servletRequest;
}