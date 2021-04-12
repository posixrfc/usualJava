package wcy.usual.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exec extends Object implements Cloneable
{
@SuppressWarnings("unused")
public static void main(String[] args)
{
	List<List<String>> data=new ArrayList<>();
	data.add(Arrays.asList("一个3","早3上","3你好"));
	data.add(Arrays.asList("一个","早0上","你好"));
	data.add(Arrays.asList("一个4","早4上","4你好"));
	Map<String,Object> mdata=new HashMap<String,Object>();
	mdata.put(null, "not");
	mdata.put("not", null);
	mdata.put(null, null);
	mdata.put("all", data);
	mdata.put("ar1", new float[]{3.3f,0.99835f,3.14159265f});
	mdata.put("nickName", "Tomcat");
	Object o=Array.newInstance(Object.class,4);
	Array.set(o, 0, "zero");
	Array.set(o, 1, 2);
	Array.set(o, 2, Arrays.asList("的\"离开","大\\打开","欧\t克的"));
	Map<Integer,SexType> m1=new HashMap<Integer,SexType>();
	m1.put(3, SexType.FEMALE);
	m1.put(99, SexType.NONE);
	Array.set(o, 3, m1);
	mdata.put("z-index", o);
	System.out.println(JsonSerializer.serialize(mdata));
	//System.out.println("Exec.class.getDeclaredAnnotations(00)");System.out.println(Exec.class.getDeclaredAnnotations().length);
}
public static void main4(String[] args) throws Exception
{
	Entity<List<String>,Map<Character,Boolean>> entity=new Entity<List<String>,Map<Character,Boolean>>(){};
	Type type = entity.getClass().getGenericSuperclass();
	//System.err.println(type);
	ParameterizedType ptype = (ParameterizedType) type;
	Type[] types = ptype.getActualTypeArguments();
	for (Type type0 : types) {
		//System.err.println(type0);
		if (type0 instanceof ParameterizedType) {
			ptype = (ParameterizedType) type0;
			Type[] types0 = ptype.getActualTypeArguments();
			for (Type type2 : types0) {
				//System.err.println(type2);
			}
		}
	}
	Class<?> clazz = entity.getClass().getSuperclass();
	//System.err.println(clazz);
	Field[] fields = clazz.getDeclaredFields();
	System.err.println("-------------------");
	for (Field field : fields) {
		System.err.println(field.getName());
		//System.err.println(field.getType());
		//System.err.println(field.getGenericType());
	}
}
}