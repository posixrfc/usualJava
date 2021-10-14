package wcy.usual.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

public abstract class RDBCP extends TimerTask implements Resource
{
public void killResource(Statement stmt,ResultSet rst)
{
	if(null!=stmt){
		try{
			stmt.close();
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}
	if(null!=rst){
		try{
			rst.close();
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}
}
public String getScalar(String sql)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	String str=null;
	try{
		stmt=link.prepareStatement(sql);
		rset=stmt.executeQuery();
		rset.next();
		str=rset.getString(1);
	}catch(SQLException e){
		e.printStackTrace(System.err);
	}finally{
		killResource(stmt,rset);
		killRdbConnect(link);
	}
	return str;
}
public List<String> getCol(String sql)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	List<String> res=new ArrayList<String>();
	try{
		stmt=link.prepareStatement(sql);
		rset=stmt.executeQuery();
		while(rset.next()){
			res.add(rset.getString(1));
		}
	}catch(SQLException e){
		e.printStackTrace(System.err);
	}finally{
		killResource(stmt,rset);
		killRdbConnect(link);
	}
	return res.size()==0 ? null : res;
}
public Map<String,String> getRow(String sql,String...alias)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	Map<String,String> row=null;
	try{
		stmt=link.prepareStatement(sql);
		rset=stmt.executeQuery();
		if(rset.next()){
			row=new HashMap<String,String>();
			String tmp;
			for(int i=0;alias.length!=i;i++){
				if((tmp=rset.getString(alias[i]))!=null){
					row.put(alias[i],tmp);
				}
			}
		}
	}catch(SQLException e){
		e.printStackTrace(System.err);
	}finally{
		killResource(stmt,rset);
		killRdbConnect(link);
	}
	return row.size()==0 ? null : row;
}
public List<Map<String,String>> getTab(String sql,String...alias)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	List<Map<String,String>> res=new ArrayList<Map<String,String>>();
	try{
		stmt=link.prepareStatement(sql);
		rset=stmt.executeQuery();
		while(rset.next()){
			Map<String,String> row=new HashMap<String, String>();
			String tmp;
			for(int i=0;alias.length!=i;i++){
				if((tmp=rset.getString(alias[i]))!=null){
					row.put(alias[i],tmp);
				}
			}
			res.add(row.size()==0 ? null : row);
		}
	}catch(SQLException e){
		e.printStackTrace(System.err);
	}finally{
		killResource(stmt,rset);
		killRdbConnect(link);
	}
	return res.size()==0 ? null : res;
}
@Override
public boolean init() throws Exception
{
	pool=new ArrayList<Connection>(3);
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
	for(Connection dc:pool){
		try{
			if(!dc.isClosed()){
				dc.close();
			}
		}catch(SQLException se){
			se.printStackTrace(System.out);
		}
	}
	pool.clear();
	stat.clear();
	rslk.unlock();
	return true;
}
public abstract Connection initRdbConnect();
public abstract void killRdbConnect(Connection link);
@Override
public void run()
{
	rslk.lock();
	for(int i=0;pool.size()!=i;)
	{
		if(stat.get(i)==byte.class){
			continue;
		}
		Connection dc=pool.get(i);
		try{
			if(dc.isClosed()){
				pool.remove(i);
				stat.remove(i);
			}else{
				dc.getSchema();
			}
		}catch(SQLException e){
			pool.remove(i);
			stat.remove(i);
			e.printStackTrace(System.out);
		}
	}
	rslk.unlock();
}
public RDBCP(String host,int port,String user,String pass,String schema)
{
	this.host=host;
	this.port=port;
	this.user=user;
	this.pass=pass;
	this.schema=schema;
}
protected transient DataSource dbs;
protected transient List<Connection> pool;
protected transient List<Class<?>> stat;
protected transient Timer task;
protected transient Lock rslk;

protected String host;
protected int port;
protected String user;
protected String pass;
protected String schema;
}
/*
jdbc:sqlserver://127.0.0.1:1433;databaseName=tdb;user=sa;password=w3c
com.microsoft.sqlserver.jdbc.SQLServerDriver

jdbc:postgresql://127.0.0.1:5432/tdb?useUnicode=true&characterEncoding=utf8
org.postgresql.Driver

jdbc:mariadb://127.0.0.1:3306/tdb?useUnicode=true&characterEncoding=utf8
org.mariadb.jdbc.Driver
*/