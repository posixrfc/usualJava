package wcy.usual.codec.ini;

public interface IniSerializable
{
public default CharSequence toIniVal()
{
	return toString();
}
public default CharSequence toIniKey()
{
	return toString();
}
}
