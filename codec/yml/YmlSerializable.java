package wcy.usual.codec.yml;

public interface YmlSerializable
{
public default CharSequence toYmlVal()
{
	return toString();
}
public default CharSequence toYmlKey()
{
	return toString();
}
}
