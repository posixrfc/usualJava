package wcy.usual.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class RdbConnect implements Connection
{
@Override
public <T> T unwrap(Class<T> iface) throws SQLException{
	if(!using){
		return null;
	}
	return conection.unwrap(iface);
}

@Override
public boolean isWrapperFor(Class<?> iface) throws SQLException{
	if(!using){
		return false;
	}
	return conection.isWrapperFor(iface);
}

@Override
public Statement createStatement() throws SQLException{
	if(!using){
		return null;
	}
	return conection.createStatement();
}

@Override
public PreparedStatement prepareStatement(String sql) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareStatement(sql);
}

@Override
public CallableStatement prepareCall(String sql) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareCall(sql);
}

@Override
public String nativeSQL(String sql) throws SQLException{
	if(!using){
		return null;
	}
	return conection.nativeSQL(sql);
}

@Override
public void setAutoCommit(boolean autoCommit) throws SQLException{
	if(!using){
		return;
	}
	conection.setAutoCommit(autoCommit);
}

@Override
public boolean getAutoCommit() throws SQLException{
	if(!using){
		return false;
	}
	return conection.getAutoCommit();
}

@Override
public void commit() throws SQLException{
	if(!using){
		return;
	}
	conection.commit();
}

@Override
public void rollback() throws SQLException{
	if(!using){
		return;
	}
	conection.rollback();
}

@Override
public void close() throws SQLException{
	if(!using){
		return;
	}
	conection.close();
}

@Override
public boolean isClosed() throws SQLException{
	if(!using){
		return false;
	}
	return conection.isClosed();
}

@Override
public DatabaseMetaData getMetaData() throws SQLException{
	if(!using){
		return null;
	}
	return conection.getMetaData();
}

@Override
public void setReadOnly(boolean readOnly) throws SQLException{
	if(!using){
		return;
	}
	conection.setReadOnly(readOnly);
}

@Override
public boolean isReadOnly() throws SQLException{
	if(!using){
		return false;
	}
	return conection.isReadOnly();
}

@Override
public void setCatalog(String catalog) throws SQLException{
	if(!using){
		return;
	}
	conection.setCatalog(catalog);
}

@Override
public String getCatalog() throws SQLException{
	if(!using){
		return null;
	}
	return conection.getCatalog();
}

@Override
public void setTransactionIsolation(int level) throws SQLException{
	if(!using){
		return;
	}
	conection.setTransactionIsolation(level);
}

@Override
public int getTransactionIsolation() throws SQLException{
	if(!using){
		return 0;
	}
	return conection.getTransactionIsolation();
}

@Override
public SQLWarning getWarnings() throws SQLException{
	if(!using){
		return null;
	}
	return conection.getWarnings();
}

@Override
public void clearWarnings() throws SQLException{
	if(!using){
		return;
	}
	conection.clearWarnings();
}

@Override
public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException{
	if(!using){
		return null;
	}
	return conection.createStatement(resultSetType,resultSetConcurrency);
}

@Override
public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareStatement(sql,resultSetType,resultSetConcurrency);
}

@Override
public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareCall(sql,resultSetType,resultSetConcurrency);
}

@Override
public Map<String, Class<?>> getTypeMap() throws SQLException{
	if(!using){
		return null;
	}
	return conection.getTypeMap();
}

@Override
public void setTypeMap(Map<String, Class<?>> map) throws SQLException{
	if(!using){
		return;
	}
	conection.setTypeMap(map);
}

@Override
public void setHoldability(int holdability) throws SQLException{
	if(!using){
		return;
	}
	conection.setHoldability(holdability);
}

@Override
public int getHoldability() throws SQLException{
	if(!using){
		return 0;
	}
	return conection.getHoldability();
}

@Override
public Savepoint setSavepoint() throws SQLException{
	if(!using){
		return null;
	}
	return conection.setSavepoint();
}

@Override
public Savepoint setSavepoint(String name) throws SQLException{
	if(!using){
		return null;
	}
	return conection.setSavepoint(name);
}

@Override
public void rollback(Savepoint savepoint) throws SQLException{
	if(!using){
		return;
	}
	conection.rollback(savepoint);
}

@Override
public void releaseSavepoint(Savepoint savepoint) throws SQLException{
	if(!using){
		return;
	}
	conection.releaseSavepoint(savepoint);
}

@Override
public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)throws SQLException{
	if(!using){
		return null;
	}
	return conection.createStatement(resultSetType,resultSetConcurrency,resultSetHoldability);
}

@Override
public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,int resultSetHoldability) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareStatement(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
}

@Override
public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,int resultSetHoldability) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareCall(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
}

@Override
public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareStatement(sql,autoGeneratedKeys);
}

@Override
public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareStatement(sql,columnIndexes);
}

@Override
public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException{
	if(!using){
		return null;
	}
	return conection.prepareStatement(sql,columnNames);
}

@Override
public Clob createClob() throws SQLException{
	if(!using){
		return null;
	}
	return conection.createClob();
}

@Override
public Blob createBlob() throws SQLException{
	if(!using){
		return null;
	}
	return conection.createBlob();
}

@Override
public NClob createNClob() throws SQLException{
	if(!using){
		return null;
	}
	return conection.createNClob();
}

@Override
public SQLXML createSQLXML() throws SQLException{
	if(!using){
		return null;
	}
	return conection.createSQLXML();
}

@Override
public boolean isValid(int timeout) throws SQLException{
	if(!using){
		return false;
	}
	return conection.isValid(timeout);
}

@Override
public void setClientInfo(String name, String value) throws SQLClientInfoException{
	if(!using){
		return;
	}
	conection.setClientInfo(name,value);
}

@Override
public void setClientInfo(Properties properties) throws SQLClientInfoException{
	if(!using){
		return;
	}
	conection.setClientInfo(properties);
}

@Override
public String getClientInfo(String name) throws SQLException{
	if(!using){
		return null;
	}
	return conection.getClientInfo(name);
}

@Override
public Properties getClientInfo() throws SQLException{
	if(!using){
		return null;
	}
	return conection.getClientInfo();
}

@Override
public Array createArrayOf(String typeName, Object[] elements) throws SQLException{
	if(!using){
		return null;
	}
	return conection.createArrayOf(typeName, elements);
}

@Override
public Struct createStruct(String typeName, Object[] attributes) throws SQLException{
	if(!using){
		return null;
	}
	return conection.createStruct(typeName, attributes);
}

@Override
public void setSchema(String schema) throws SQLException{
	if(!using){
		return;
	}
	conection.setSchema(schema);
}

@Override
public String getSchema() throws SQLException{
	if(!using){
		return null;
	}
	return conection.getSchema();
}

@Override
public void abort(Executor executor) throws SQLException{
	if(!using){
		return;
	}
	conection.abort(executor);
}

@Override
public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException{
	if(!using){
		return;
	}
	conection.setNetworkTimeout(executor, milliseconds);
}

@Override
public int getNetworkTimeout() throws SQLException{
	if(!using){
		return 0;
	}
	return conection.getNetworkTimeout();
}
public volatile Connection conection;
public volatile boolean using=false;
}