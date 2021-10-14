package wcy.usual.db;

public abstract interface Resource
{
public abstract boolean init() throws Exception;
public abstract boolean kill() throws Exception;
}
