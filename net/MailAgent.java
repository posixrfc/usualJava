package wcy.usual.net;

import java.util.Date;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public abstract class MailAgent
{
protected MailAgent(String mail,String nick,String pass,String host,String port)
{
	this.mail=mail;
	this.nick=nick;
	this.pass=pass;
	this.host=host;
	this.port=port;
}
public void sendMail(String[][] to,String[][] cc,String[][] bc,String tip,String cnt) throws Exception
{
	Properties props = new Properties();
	props.setProperty("mail.transport.protocol","smtp");
	//props.setProperty("mail.smtp.host","smtp.qq.com");//props.setProperty("mail.smtp.host","smtp.sina.com");
	props.setProperty("mail.smtp.host",host);
	props.setProperty("mail.smtp.auth","true");
	props.setProperty("mail.smtp.port",port);//props.setProperty("mail.smtp.port","465");
	props.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
	props.setProperty("mail.smtp.socketFactory.fallback","false");
	//props.setProperty("mail.smtp.socketFactory.port","465");
	props.setProperty("mail.smtp.socketFactory.port",port);
	
	Session session=Session.getDefaultInstance(props);
	MimeMessage message=new MimeMessage(session);
	session.setDebug(false);
	final String charset="UTF-8";
	message.setFrom(new InternetAddress(mail,nick,charset));
	if(null!=to && to.length!=0){
		for(String[] t:to){
			message.addRecipient(RecipientType.TO,new InternetAddress(t[0],t[1],charset));
		}
	}
	if(null!=cc && cc.length!=0){
		for(String[] c:cc){
			message.addRecipient(RecipientType.CC, new InternetAddress(c[0],c[1],charset));
		}
	}
	if(null!=bc && bc.length!=0){
		for(String[] b:bc){
			message.addRecipient(RecipientType.BCC, new InternetAddress(b[0],b[1],charset));
		}
	}
	//message.setContent("<span style=\"color:black;\">[图像文字识别][张三][1048576][内购]充值100元已成功，</span><a href=\"https://www.baidu.com/\">查看本月报表</a>", "text/html;charset=UTF-8");
	message.setContent(cnt,"text/html;charset=UTF-8");
//	DateFormat df=new SimpleDateFormat("yyyyMMdd");
//	df.setCalendar(Calendar.getInstance());
//	message.setSentDate(df.parse("19891004"));
	message.setSentDate(new Date());
	Transport transport=session.getTransport();
	transport.connect(mail,pass);
	message.setSubject(tip,charset);
	message.saveChanges();
	transport.sendMessage(message,message.getAllRecipients());
	transport.close();
}
public final String host,port;
public final String mail,nick,pass;
}