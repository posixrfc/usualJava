package wcy.usual.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import wcy.usual.codec.Codec;

public final class NetAgent
{
public static final Object http(CharSequence type,CharSequence url,CharSequence body,Map<CharSequence,CharSequence> header,ResChecker checker)
{
	Object res=null;
	String mime="application/x-www-form-urlencoded",enc="UTF-8";
	try{
		HttpURLConnection hcon=(HttpURLConnection)new URL(url.toString()).openConnection();
		hcon.setRequestMethod(type.toString());
		hcon.setConnectTimeout(1999);
		hcon.setReadTimeout(99999);
		hcon.setDefaultUseCaches(false);
		hcon.setAllowUserInteraction(false);
		hcon.setUseCaches(false);
		if(null==header || header.size()==0){
			hcon.setRequestProperty("Content-Type",mime);
		}else{
			for(Map.Entry<CharSequence,CharSequence> e:header.entrySet()){
				if("Content-Type".contentEquals(e.getKey())){
					mime=e.getValue().toString();
					int idx=mime.lastIndexOf('=');
					if(-1!=idx){
						enc=mime.substring(1+idx);
						idx=mime.indexOf(';');
						mime=mime.substring(0,idx);
					}
				}
				hcon.setRequestProperty(e.getKey().toString(),e.getValue().toString());
			}
			if(!header.containsKey("Content-Type")){
				hcon.setRequestProperty("Content-Type",mime);
			}
		}
		hcon.setDoInput(true);
		if(!"GET".contentEquals(type) && null!=body){
			hcon.setDoOutput(true);//hcon.setChunkedStreamingMode(0);
		}
		hcon.connect();
		if(!"GET".contentEquals(type) && null!=body){
			OutputStream nos=hcon.getOutputStream();
			nos.write(body.toString().getBytes(enc));
			nos.flush();
			nos.close();
		}
		hcon.getResponseCode();
		mime=hcon.getContentType();
		byte[] bytes=new byte[hcon.getContentLength()];
		if(hcon.getErrorStream()==null){
			hcon.getInputStream().read(bytes);//bytes=hcon.getInputStream().readAllBytes();
		}else{
			hcon.getErrorStream().read(bytes);//bytes=hcon.getErrorStream().readAllBytes();
		}
		if(null!=checker){
			bytes=checker.check(hcon,bytes);
		}
		hcon.disconnect();
		enc="UTF-8";
		if(null==mime){
			mime="text/plain";
		}else{
			int idx=mime.lastIndexOf('=');
			if(-1!=idx){
				enc=mime.substring(1+idx);
				idx=mime.indexOf(';');
				mime=mime.substring(0,idx);
			}
		}
		res=new String(bytes,enc);
	}catch(IOException e){
		e.printStackTrace(System.err);
	}
	System.out.println("raw");
	System.out.println(res);
	switch(mime)
	{
	case "text/plain":
		return res;
	case "text/html":
		return res;
	case "application/json":
		return Codec.json2obj(res.toString());
	case "text/xml":
	case "application/xml":
		return Codec.xml2obj(res.toString());
	case "application/x-www-form-urlencoded":
		return Codec.uri2obj(res.toString());
	case "multipart/form-data":
		return res;
	case "application/octet-stream":
		return res;
	default:
		return res;
	}
}
public static final Object http(CharSequence type,CharSequence url,Map<CharSequence,Object> paramOrBody,Map<CharSequence,CharSequence> header,ResChecker checker)
{
	if(null==paramOrBody || paramOrBody.size()==0){
		return http(type,url,(CharSequence)null,header,checker);
	}
	if("GET".contentEquals(type)){
		return http(type,url.toString()+'?'+Codec.obj2uri(paramOrBody),(CharSequence)null,header,checker);
	}
	@SuppressWarnings("unused")
	String mime="application/x-www-form-urlencoded",enc="UTF-8";
	if(null!=header && header.size()!=0){
		for(Map.Entry<CharSequence,CharSequence> e:header.entrySet()){
			if("Content-Type".contentEquals(e.getKey())){
				mime=e.getValue().toString();
				int idx=mime.lastIndexOf('=');
				if(-1!=idx){
					enc=mime.substring(1+idx);
					idx=mime.indexOf(';');
					mime=mime.substring(0,idx);
				}
			}
		}
	}
	switch(mime)
	{
	case "text/plain":
		return http(type,url,(CharSequence)null,header,checker);
	case "text/html":
		return http(type,url,(CharSequence)null,header,checker);
	case "application/json":
		return http(type,url,Codec.obj2json(paramOrBody),header,checker);
	case "text/xml":
	case "application/xml":
		return http(type,url,Codec.obj2xml(paramOrBody,"xml"),header,checker);
	case "application/x-www-form-urlencoded":
		return http(type,url,Codec.obj2uri(paramOrBody),header,checker);
	case "multipart/form-data":
		return http(type,url,(CharSequence)null,header,checker);
	case "application/octet-stream":
		return http(type,url,(CharSequence)null,header,checker);
	default:
		return http(type,url,(CharSequence)null,header,checker);
	}
}
private NetAgent(){}
}
