package wcy.usual.json;

public interface JsonSerializable {
	public default CharSequence toJsonValue(){
		return JsonSerializer.serialize(this);
	}
	public default CharSequence toJsonKey(){
		return '"'+JsonSerializer.toJsonStandard(toString()).toString()+'"';
	}
}
