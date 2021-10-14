package wcy.usual.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGSimpleDataSource;

public class PostgresSQL extends RDBCP
{
@Override
public Connection initRdbConnect()
{
	Connection dc=null;
	rslk.lock();
	for(int i=0;pool.size()!=i;i++)
	{
		if(stat.get(i)==void.class)
		{
			stat.set(i,byte.class);
			dc=pool.get(i);
			break;
		}
	}
	if(null==dc)
	{
		try{
			dc=super.dbs.getConnection();
		}catch(SQLException e){
			e.printStackTrace(System.out);
			rslk.unlock();
			return null;
		}
		pool.add(dc);
		stat.add(byte.class);
	}
	rslk.unlock();
	return dc;
}
@Override
public void killRdbConnect(Connection link)
{
	if(null==link){
		return;
	}
	int exec=-1;
	rslk.lock();
	for(int i=0;pool.size()!=i;i++)
	{
		if(pool.get(i)==link){
			stat.set(i,void.class);
			exec=i;
			break;
		}
	}
	if(-1==exec){
		rslk.unlock();
		return;
	}
	try{
		if(link.isClosed()){
			pool.remove(exec);
			stat.remove(exec);
		}
	}catch(SQLException e){
		pool.remove(exec);
		stat.remove(exec);
		e.printStackTrace(System.out);
	}
	rslk.unlock();
}
@Override
public boolean init() throws Exception
{
	PGSimpleDataSource sds=new PGSimpleDataSource();
	super.dbs=sds;
	sds.setServerNames(new String[]{super.host});
	sds.setPortNumbers(new int[]{super.port});
	sds.setUser(super.user);
	sds.setPassword(super.pass);
	sds.setDatabaseName(super.schema);
	return super.init();
}
public PostgresSQL(String host,int port,String user,String pass,String schema){
	super(host,port,user,pass,schema);
}
}