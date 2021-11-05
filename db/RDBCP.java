package wcy.usual.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
public int exec(CharSequence sql)
{
	int cnt=-1;
	Connection link=initRdbConnect();
	if(null==link){
		return cnt;
	}
	PreparedStatement stmt=null;
	try{
		stmt=link.prepareStatement(sql.toString());
		cnt=stmt.executeUpdate();
	}catch(SQLException e){
		e.printStackTrace(System.err);
		return cnt;
	}finally{
		killResource(stmt,null);
		killRdbConnect(link);
	}
	return cnt;
}
public String getScalar(CharSequence sql)
{
	Connection link=initRdbConnect();
	if(null==link){
		return null;
	}
	PreparedStatement stmt=null;
	ResultSet rset=null;
	String str=null;
	try{
		stmt=link.prepareStatement(sql.toString());
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
public List<String> getCol(CharSequence sql)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	List<String> res=new ArrayList<String>();
	try{
		stmt=link.prepareStatement(sql.toString());
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
public List<String> getRow(CharSequence sql)
{
	Connection link=initRdbConnect();
	if(null==link){
		return null;
	}
	PreparedStatement stmt=null;
	ResultSet rset=null;
	List<String> row=null;
	try{
		stmt=link.prepareStatement(sql.toString());
		rset=stmt.executeQuery();
		if(rset.getFetchSize()==1){
			rset.next();
			int cols=rset.getMetaData().getColumnCount();
			row=new ArrayList<String>(cols);
			for(int i=1,l=cols+1;l!=i;i++){
				row.add(rset.getString(i));
			}
		}
	}catch(SQLException e){
		e.printStackTrace(System.err);
	}finally{
		killResource(stmt,rset);
		killRdbConnect(link);
	}
	return row;
}
public List<List<String>> getTab(CharSequence sql)
{
	Connection link=initRdbConnect();
	if(null==link){
		return null;
	}
	PreparedStatement stmt=null;
	ResultSet rset=null;
	List<List<String>> res=null;
	try{
		stmt=link.prepareStatement(sql.toString());
		rset=stmt.executeQuery();
		int size=rset.getFetchSize();
		if(0!=size){
			res=new ArrayList<List<String>>(size);
			int ccnt=rset.getMetaData().getColumnCount();
			while(rset.next()){
				List<String> row=new ArrayList<String>(ccnt);
				for(int i=1,l=ccnt+1;l!=i;i++){
					row.add(rset.getString(i));
				}
				res.add(row);
			}
		}
	}catch(SQLException e){
		e.printStackTrace(System.err);
	}finally{
		killResource(stmt,rset);
		killRdbConnect(link);
	}
	return res;
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
		Connection dc=pool.get(i);
		if(stat.get(i)==byte.class){
			try{
				if(dc.isClosed()){
					pool.remove(i);
					stat.remove(i);
				}
			}catch(SQLException e){
				pool.remove(i);
				stat.remove(i);
				e.printStackTrace(System.out);
			}
			continue;
		}
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