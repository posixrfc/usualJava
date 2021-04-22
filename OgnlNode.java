package wcy.usual;

public class OgnlNode
{
public OgnlNode(){}
public OgnlNode(String str,Class<?> cls)
{
	this.str=str;
	this.cls=cls;
}
@Override
public String toString()
{
	return str+"--"+cls.getName();
}
public String str;
public Class<?> cls;//char.class|int.class
}
