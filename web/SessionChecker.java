package wcy.usual.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract interface SessionChecker{
public default boolean hasError(HttpServletRequest hreq,String key){
	HttpSession sess=hreq.getSession(false);
	if(null==sess){
		return true;
	}
	return sess.getAttribute(key)==null;
}
}