package wcy.usual.xml;

public interface XmlSerializable
{
public default CharSequence toXmlValue()
{
	return '"'+XmlSerializer.toXmlStandard(toString()).toString()+'"';
}
public default CharSequence toXmlKey()
{
	return '"'+XmlSerializer.toXmlStandard(toString()).toString()+'"';
}
}
