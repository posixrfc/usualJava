package wcy.usual.xml;

public interface XmlSerializable
{
public default CharSequence toXmlValue()
{
	return '"'+XmlSerializer.txt2xml(toString()).toString()+'"';
}
public default CharSequence toXmlKey()
{
	return '"'+XmlSerializer.txt2xml(toString()).toString()+'"';
}
}
