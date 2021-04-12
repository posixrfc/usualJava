package wcy.usual.xml;

public interface XmlSerializable {
	public default CharSequence toJsonValue(){
		return '"'+XmlSerializer.toJsonStandard(toString()).toString()+'"';
	}
	public default CharSequence toJsonKey(){
		return '"'+XmlSerializer.toJsonStandard(toString()).toString()+'"';
	}
}
