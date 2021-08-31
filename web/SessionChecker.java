package wcy.usual.web;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public abstract interface SessionChecker
{
public abstract boolean hasError(HttpServletRequest hreq,String key);
}