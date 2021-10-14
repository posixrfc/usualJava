package wcy.usual.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import redis.clients.jedis.Jedis;

public class NoSqlRedis extends TimerTask implements Resource
{
public Jedis initNoSqlConnect()
{
	Jedis jc=null;
	int exec=0;
	rslk.lock();
	for(int i=0,l=pool.size();i!=l;i++)
	{
		if(stat.get(i)==void.class)
		{
			stat.set(i,byte.class);
			jc=pool.get(i);
			exec=i;
			break;
		}
	}
	if(null==jc)
	{
		jc=new Jedis(host,port);
		pool.add(jc);
		stat.add(byte.class);
		exec=pool.size()-1;
	}
	if(!jc.isConnected()){
		jc.connect();
	}
	if(null!=pass)
	{
		if(!jc.auth(pass).contentEquals("OK"))
		{
			stat.set(exec,void.class);
			rslk.unlock();
			return null;
		}
	}
	if(!jc.ping().contentEquals("PONG"))
	{
		stat.set(exec,void.class);
		rslk.unlock();
		return null;
	}
	if(!jc.select(idx).contentEquals("OK"))
	{
		stat.set(exec,void.class);
		rslk.unlock();
		return null;
	}
	rslk.unlock();
	return jc;
}
public void killNoSqlConnect(Jedis jc)
{
	if(null==jc){
		return;
	}
	rslk.lock();
	for(int i=0,l=pool.size();i!=l;i++)
	{
		if(pool.get(i)==jc){
			stat.set(i,void.class);
			break;
		}
	}
	rslk.unlock();
}
@Override
public boolean init() throws Exception
{
	pool=new ArrayList<Jedis>(3);
	stat=new ArrayList<Class<?>>(3);
	rslk=new ReentrantLock();
	(task=new Timer(getClass().getName()+'-'+hashCode(),false)).schedule(this,99999L,99999L);
	return true;
}
@Override
public boolean kill() throws Exception
{
	rslk.lock();
	task.cancel();
	for(Jedis jc:pool){
		if(jc.isConnected()){
			jc.close();
		}
	}
	pool.clear();
	stat.clear();
	rslk.unlock();
	return true;
}
@Override
public void run()
{
	rslk.lock();
	for(int i=0,l=pool.size();i!=l;i++)
	{
		if(stat.get(i)==byte.class){
			continue;
		}
		Jedis jc=pool.get(i);
		if(jc.isConnected()){
			jc.ping();
			continue;
		}
		jc.connect();
		if(null!=pass){
			if(!jc.auth(pass).contentEquals("OK")){
				continue;
			}
		}
		jc.select(idx);
	}
	rslk.unlock();
}
protected transient List<Jedis> pool;
protected transient List<Class<?>> stat;
protected transient Timer task;
protected transient Lock rslk;

public NoSqlRedis(String host,int port,String pass,int idx)
{
	this.host=host;
	this.port=port;
	this.pass=pass;
	this.idx=idx;
}
protected String host;
protected int port;
protected String pass;
protected int idx;
}