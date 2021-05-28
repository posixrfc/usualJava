package wcy.usual.db;

import redis.clients.jedis.Jedis;

public interface NoSQL
{
public Jedis initNoSQLLink();
public void killNoSQLLink(Jedis link);

public void initNoSQL();
public void killNoSQL();
}