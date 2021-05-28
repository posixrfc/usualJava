package wcy.usual.db;

import redis.clients.jedis.Jedis;

public interface NoSQL
{
public Jedis openNoSQLLink();
public void killNoSQLLink(Jedis link);

public void initNoSQL();
public void purgeNoSQL();
}