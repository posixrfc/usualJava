package wcy.usual.json;

public class Entity<T, P> extends Object
{
	public T tvalue;
	public P pvalue;
	public T getTvalue() {
		return tvalue;
	}
	public void setTvalue(T tvalue) {
		this.tvalue = tvalue;
	}
	public P getPvalue() {
		return pvalue;
	}
	public void setPvalue(P pvalue) {
		this.pvalue = pvalue;
	}
}
