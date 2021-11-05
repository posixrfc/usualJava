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
public HttpRsps(HttpRqst servletRequest,HttpServletResponse servletResponse){
	this.sreq=Objects.requireNonNull(servletRequest);
	this.srsp=Objects.requireNonNull(servletResponse);
	try{
		WebCore.setCommonHeader(sreq,srsp);
	}catch(IOException e){
		e.printStackTrace(System.err);
	}
}
@Override
public void flushBuffer() throws IOException{	
	srsp.flushBuffer();
}
@Override
public int getBufferSize(){
	return srsp.getBufferSize();
}
@Override
public String getCharacterEncoding(){
	return srsp.getCharacterEncoding();
}
@Override
public String getContentType(){
	return srsp.getContentType();
}
@Override
public Locale getLocale(){
	return srsp.getLocale();
}
@Override
public ServletOutputStream getOutputStream() throws IOException{
	return srsp.getOutputStream();
}
@Override
public PrintWriter getWriter() throws IOException{
	return srsp.getWriter();
}
@Override
public boolean isCommitted(){
	return srsp.isCommitted();
}
@Override
public void reset(){
	srsp.reset();
}
@Override
public void resetBuffer(){
	srsp.resetBuffer();
}
@Override
public void setBufferSize(int arg0){
	srsp.setBufferSize(arg0);
}
@Override
public void setCharacterEncoding(String arg0){
	srsp.setCharacterEncoding(arg0);
}
@Override
public void setContentLength(int arg0){
	srsp.setContentLength(arg0);
}
@Override
public void setContentLengthLong(long arg0){
	srsp.setContentLengthLong(arg0);
}
@Override
public void setContentType(String arg0){
	srsp.setContentType(arg0);
}
@Override
public void setLocale(Locale arg0){
	srsp.setLocale(arg0);
}
@Override
public void addCookie(Cookie arg0){
	arg0.setHttpOnly(true);
	arg0.setSecure(true);
	if(arg0.getComment()==null){
		arg0.setComment("wcy");
	}
	srsp.addCookie(arg0);
	Collection<String> ckold=srsp.getHeaders("Set-Cookie");
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
	srsp.setHeader("Set-Cookie",sessid);
	for(int i=0,l=cknew.size();i!=l;i++){
		srsp.addHeader("Set-Cookie",cknew.get(i));
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
	srsp.addDateHeader(arg0, arg1);
}
@Override
public void addHeader(String arg0, String arg1){
	srsp.addHeader(arg0, arg1);
}
@Override
public void addIntHeader(String arg0, int arg1){
	srsp.addIntHeader(arg0, arg1);
}
@Override
public boolean containsHeader(String arg0){
	return srsp.containsHeader(arg0);
}
@Override
public String encodeRedirectURL(String arg0){
	return srsp.encodeRedirectURL(arg0);
}
@Deprecated
@Override
public String encodeRedirectUrl(String arg0){
	return srsp.encodeRedirectUrl(arg0);
}
@Override
public String encodeURL(String arg0){
	return srsp.encodeURL(arg0);
}
@Deprecated
@Override
public String encodeUrl(String arg0){
	return srsp.encodeUrl(arg0);
}
@Override
public String getHeader(String arg0){
	return srsp.getHeader(arg0);
}
@Override
public Collection<String> getHeaderNames(){
	return srsp.getHeaderNames();
}
@Override
public Collection<String> getHeaders(String arg0){
	return srsp.getHeaders(arg0);
}
@Override
public int getStatus(){
	return srsp.getStatus();
}
@Override
public void sendError(int arg0) throws IOException{
	srsp.sendError(arg0);
}
@Override
public void sendError(int arg0, String arg1) throws IOException{
	srsp.sendError(arg0,arg1);
}
@Override
public void sendRedirect(String arg0) throws IOException{
	srsp.sendRedirect(arg0);
}
@Override
public void setDateHeader(String arg0, long arg1){
	srsp.setDateHeader(arg0, arg1);
}
@Override
public void setHeader(String arg0, String arg1){
	srsp.setHeader(arg0,arg1);
}
@Override
public void setIntHeader(String arg0, int arg1){
	srsp.setIntHeader(arg0,arg1);
}
@Override
public void setStatus(int arg0){
	srsp.setStatus(arg0);
}
@Deprecated
@Override
public void setStatus(int arg0, String arg1){
	srsp.setStatus(arg0,arg1);
}
public final HttpRqst sreq;
public final HttpServletResponse srsp;
}