package wcy.usual.ognl;

public class OgnlNode
{
public OgnlNode(){}
public OgnlNode(String str,Class<?> cls)
{
	this.str=str;
	this.cls=cls;
}
public OgnlNode(int idx,Class<?> cls)
{
	this.idx=idx;
	this.cls=cls;
}
@Override
public String toString()
{
	if(int.class==cls){
		return idx+"--"+cls.getName();
	}
	return str+"--"+cls.getName();
}
public int idx;
public String str;
public Class<?> cls;//char.class|int.class
}
