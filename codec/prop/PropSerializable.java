package wcy.usual.codec.prop;

public interface PropSerializable
{
public default CharSequence toPropVal()
{
	return toString();
}
public default CharSequence toPropKey()
{
	return toString();
}
}
