package wcy.usual.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public abstract interface ResponsePretreater
{
public abstract void setDefaultHeader(HttpServletRequest hreq,HttpServletResponse hrsp) throws IOException;
}