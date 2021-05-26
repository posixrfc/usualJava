package wcy.usual.db;

public interface NoSQL
{
public KvsLink getNoSQLLink();
public void closeNoSQLLink(KvsLink link);

public void initNoSQL();
public void purgeNoSQL();

public static final String dbhost="192.168.190.130";
public static final int dbport=6379;
public static final int dbidx=9;
public static final String dbpass="quTQeii0VUhTILxsfb9CIzUVPgqD19Uhc06YeAbVYsxbHNL35oPU5sWs8fBbM2Pttzgi";
}