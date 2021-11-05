package wcy.usual.net;

public abstract class SmsAgent
{
public boolean sendCodeNew(String content,String receiver)
{
	if(null==content || content.trim().length()==0){
		return false;
	}
	content=sign+"你正在注册为新用户，验证码为："+content+"，2分钟内有效";
	return sendSmsAny(content,receiver);
}
public boolean sendCodeDel(String content,String receiver)
{
	content=sign+content;
	return sendSmsAny(content,receiver);
}
public boolean sendCodeReset(String content,String receiver)
{
	content=sign+content;
	return sendSmsAny(content,receiver);
}
public boolean sendCodeTel(String content,String receiver)
{
	content=sign+content;
	return sendSmsAny(content,receiver);
}
public SmsAgent(String sign){
	this.sign=sign;
}
public abstract boolean sendSmsAny(String content,String... receiver);
public final String sign;
}