package wcy.usual.func;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Logging
{
public void log(CharSequence chrs)
{
	if(null==chrs){
		chrs="NULL";
	}
	FileWriter fw=null;
	BufferedWriter bw=null;
	Logging.lk.lock();
	String time=LocalDateTime.now().toString().replace('T',' ');
	String clsn=cls.getTypeName();
	Thread t=Thread.currentThread();
	String trdn=t.getThreadGroup().getName()+'-'+t.getName();
	try{
		fw=new FileWriter(Logging.path,true);
		bw=new BufferedWriter(fw);
		bw.write(time+' '+clsn+' '+trdn+' '+chrs);
		bw.newLine();
		bw.close();
		fw.close();
	}catch(IOException ex){
		ex.printStackTrace(System.err);
	}finally{
		Logging.lk.unlock();
	}
}

public void log(Object o)
{
	if(null==o){
		this.log((String)null);
	}else{
		this.log(o.toString());
	}
}
public void log(byte b){
	this.log(String.valueOf(b));
}
public void log(short s){
	this.log(String.valueOf(s));
}
public void log(int i){
	this.log(String.valueOf(i));
}
public void log(long l){
	this.log(String.valueOf(l));
}
public void log(float f){
	this.log(String.valueOf(f));
}
public void log(double d){
	this.log(String.valueOf(d));
}
public void log(char c){
	this.log(String.valueOf(c));
}
public void log(boolean b){
	this.log(String.valueOf(b));
}
public void log(char[] chs){
	this.log(String.valueOf(chs));
}

public Logging(Class<?> cls){
	this.cls=cls;
}
public static void setLogFile(CharSequence path)
{
	Logging.lk.lock();
	Logging.path=path.toString();
	Logging.lk.unlock();
}
protected Class<?> cls;

protected static volatile String path;
protected static Lock lk=new ReentrantLock();
}
