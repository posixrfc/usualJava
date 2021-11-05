package wcy.usual.db;

public abstract interface Serializable extends java.io.Serializable
{
	public abstract boolean load();
	public abstract boolean save();
	
	public default boolean cache(){return false;};
	public default String getCacheKey(){return null;};
}
