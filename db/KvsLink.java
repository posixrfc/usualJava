package wcy.usual.db;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;

public class KvsLink
{
public Map<String,String> hgetAll(String id)
{
	return redis.hgetAll(id);
}
public List<String> lrange(String id,long go,long to)
{
	return redis.lrange(id, go, to);
}
public String hget(String id,String key)
{
	return redis.hget(id, key);
}
public boolean hset(String id,String key,String val)
{
	if(!using){
		return false;
	}
	redis.hset(id, key, val);
	return true;
}
public boolean hdel(String id, String...key)
{
	if(!using){
		return false;
	}
	redis.hdel(id,key);
	return true;
}
public volatile Jedis redis;
public volatile boolean using;
}