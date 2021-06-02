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

public interface RDBCP
{
public default void initDBCP(){};
public default void killDBCP(){};

public abstract Connection initRdbConnect();
public default void killRdbConnect(Connection link){};

public default void killResource(Statement stmt,ResultSet rst)
{
	if(null != stmt){
		try{
			stmt.close();
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}
	if(null != rst){
		try{
			rst.close();
		}catch (Exception e){
			e.printStackTrace(System.err);
		}
	}
}

public default String getScalar(String sql)
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

public default List<String> getCol(String sql)
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
public default Map<String,String> getRow(String sql,String...alias)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	Map<String,String> row=null;
	try {
		stmt=link.prepareStatement(sql);
		rset=stmt.executeQuery();
		if(rset.next()){
			row=new HashMap<String,String>();
			String tmp;
			for(int i=0;alias.length!=i;i++){
				if((tmp=rset.getString(alias[i]))!=null) {
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
public default List<Map<String,String>> getTab(String sql,String...alias)
{
	Connection link=initRdbConnect();
	PreparedStatement stmt=null;
	ResultSet rset=null;
	List<Map<String,String>> res=new ArrayList<Map<String,String>>();
	try{
		stmt = link.prepareStatement(sql);
		rset = stmt.executeQuery();
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
}
/*
jdbc:sqlserver://127.0.0.1:1433;databaseName=tdb;user=sa;password=w3c
com.microsoft.sqlserver.jdbc.SQLServerDriver

jdbc:postgresql://127.0.0.1:5432/tdb?useUnicode=true&characterEncoding=utf8
org.postgresql.Driver

jdbc:mariadb://127.0.0.1:3306/tdb?useUnicode=true&characterEncoding=utf8
org.mariadb.jdbc.Driver
*/