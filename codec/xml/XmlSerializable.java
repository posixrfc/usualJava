package wcy.usual.codec.xml;

public interface XmlSerializable
{
public default CharSequence toXmlVal()
{
	return '"'+XmlSerializer.txt2xml(toString()).toString()+'"';
}
public default CharSequence toXmlKey()
{
	return '"'+XmlSerializer.txt2xml(toString()).toString()+'"';
}
}
