package wcy.usual.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class HttpRsps implements HttpServletResponse
{
public HttpRsps(HttpServletResponse servletResponse){
	this.servletResponse=Objects.requireNonNull(servletResponse);
}
@Override
public void flushBuffer() throws IOException{	
	servletResponse.flushBuffer();
}
@Override
public int getBufferSize(){
	return servletResponse.getBufferSize();
}
@Override
public String getCharacterEncoding(){
	return servletResponse.getCharacterEncoding();
}
@Override
public String getContentType(){
	return servletResponse.getContentType();
}
@Override
public Locale getLocale(){
	return servletResponse.getLocale();
}
@Override
public ServletOutputStream getOutputStream() throws IOException{
	return servletResponse.getOutputStream();
}
@Override
public PrintWriter getWriter() throws IOException{
	return servletResponse.getWriter();
}
@Override
public boolean isCommitted(){
	return servletResponse.isCommitted();
}
@Override
public void reset(){
	servletResponse.reset();
}
@Override
public void resetBuffer(){
	servletResponse.resetBuffer();
}
@Override
public void setBufferSize(int arg0){
	servletResponse.setBufferSize(arg0);
}
@Override
public void setCharacterEncoding(String arg0){
	servletResponse.setCharacterEncoding(arg0);
}
@Override
public void setContentLength(int arg0){
	servletResponse.setContentLength(arg0);
}
@Override
public void setContentLengthLong(long arg0){
	servletResponse.setContentLengthLong(arg0);
}
@Override
public void setContentType(String arg0){
	servletResponse.setContentType(arg0);
}
@Override
public void setLocale(Locale arg0){
	servletResponse.setLocale(arg0);
}
@Override
public void addCookie(Cookie arg0){
	arg0.setHttpOnly(true);
	arg0.setSecure(true);
	if(arg0.getComment()==null){
		arg0.setComment("wcy");
	}
	servletResponse.addCookie(arg0);
	Collection<String> ckold=servletResponse.getHeaders("Set-Cookie");
	List<String> cknew=new ArrayList<String>(ckold.size()+1);
	String ckpre=arg0.getName()+'='+arg0.getValue()+';';
	for(String ckval:ckold){
		if(ckval.startsWith(ckpre)){
			ckval=ckval.substring(0,ckpre.length())+" SameSite=None;"+ckval.substring(ckpre.length());
		}
		cknew.add(ckval);
	}
	String sessid=null;
	for(int i=0,l=cknew.size();i!=l;){
		if(cknew.get(i).startsWith("JSESSIONID=")){
			if(null==sessid){
				sessid=cknew.remove(i);
			}else{
				cknew.remove(i);
			}
			l--;
		}else{
			i++;
		}
	}//SameSite=None,Lax,Strict
	if(!sessid.contains("; Secure; ")){
		int idx=sessid.indexOf("; HttpOnly")+1;
		sessid=sessid.substring(0,idx)+" Secure;"+sessid.substring(idx);
	}
	if(!sessid.contains("; SameSite=None; ")){
		int idx=sessid.indexOf(';')+1;
		sessid=sessid.substring(0,idx)+" SameSite=None;"+sessid.substring(idx);
	}
	servletResponse.setHeader("Set-Cookie",sessid);
	for(int i=0,l=cknew.size();i!=l;i++){
		servletResponse.addHeader("Set-Cookie",cknew.get(i));
	}
/*
JSESSIONID=C10EEA846561D4731EA3C93259E0E3DC; SameSite=None; Path=/cater; Secure; HttpOnly
Set-Cookie: JSESSIONID=29FE88855D58210A67D5DA84392F902C; Path=/cater; HttpOnly
Set-Cookie: cknm=ckval; Max-Age=86400; Expires=Fri, 03-Sep-2021 02:24:30 GMT; Path=/cater; HttpOnly
Set-Cookie: cknm1=ckval1; Max-Age=86400; Expires=Fri, 03-Sep-2021 02:24:30 GMT; Path=/cater; HttpOnly
Set-Cookie: cknm2=ckval2; Max-Age=86400; Expires=Fri, 03-Sep-2021 02:24:30 GMT; Path=/cater; HttpOnly
*/
}
@Override
public void addDateHeader(String arg0, long arg1){
	servletResponse.addDateHeader(arg0, arg1);
}
@Override
public void addHeader(String arg0, String arg1){
	servletResponse.addHeader(arg0, arg1);
}
@Override
public void addIntHeader(String arg0, int arg1){
	servletResponse.addIntHeader(arg0, arg1);
}
@Override
public boolean containsHeader(String arg0){
	return servletResponse.containsHeader(arg0);
}
@Override
public String encodeRedirectURL(String arg0){
	return servletResponse.encodeRedirectURL(arg0);
}
@Deprecated
@Override
public String encodeRedirectUrl(String arg0){
	return servletResponse.encodeRedirectUrl(arg0);
}
@Override
public String encodeURL(String arg0){
	return servletResponse.encodeURL(arg0);
}
@Deprecated
@Override
public String encodeUrl(String arg0){
	return servletResponse.encodeUrl(arg0);
}
@Override
public String getHeader(String arg0){
	return servletResponse.getHeader(arg0);
}
@Override
public Collection<String> getHeaderNames(){
	return servletResponse.getHeaderNames();
}
@Override
public Collection<String> getHeaders(String arg0){
	return servletResponse.getHeaders(arg0);
}
@Override
public int getStatus(){
	return servletResponse.getStatus();
}
@Override
public void sendError(int arg0) throws IOException{
	servletResponse.sendError(arg0);
}
@Override
public void sendError(int arg0, String arg1) throws IOException{
	servletResponse.sendError(arg0,arg1);
}
@Override
public void sendRedirect(String arg0) throws IOException{
	servletResponse.sendRedirect(arg0);
}
@Override
public void setDateHeader(String arg0, long arg1){
	servletResponse.setDateHeader(arg0, arg1);
}
@Override
public void setHeader(String arg0, String arg1){
	servletResponse.setHeader(arg0,arg1);
}
@Override
public void setIntHeader(String arg0, int arg1){
	servletResponse.setIntHeader(arg0,arg1);
}
@Override
public void setStatus(int arg0){
	servletResponse.setStatus(arg0);
}
@Deprecated
@Override
public void setStatus(int arg0, String arg1){
	servletResponse.setStatus(arg0,arg1);
}
protected final HttpServletResponse servletResponse;
}