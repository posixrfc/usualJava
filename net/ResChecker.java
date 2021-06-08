package wcy.usual.net;

import java.net.HttpURLConnection;

@FunctionalInterface
public abstract interface ResChecker
{
public abstract byte[] check(HttpURLConnection hcon,byte[] body);
}
