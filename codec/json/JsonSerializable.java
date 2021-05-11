package wcy.usual.codec.json;

public interface JsonSerializable
{
public default CharSequence toJsonVal()
{
	return JsonSerializer.serialize(this);
}
public default CharSequence toJsonKey()
{
	return '"'+JsonSerializer.txt2json(toString()).toString()+'"';
}
}