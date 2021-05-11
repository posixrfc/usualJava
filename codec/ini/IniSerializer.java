package wcy.usual.codec.ini;

import java.util.ArrayList;
import java.util.List;

public final class IniSerializer
{
public static Object parseIni(CharSequence ini)
{
	if(null==ini || ini.length()==0){
		return null;
	}
	final int len=ini.length();
	List<Object> parts=new ArrayList<Object>();
	int j=0;
	for(int i=0;len!=i;i++)
	{
		char chr=ini.charAt(i);
		if(';'==chr){
			
		}
		if('\n'!=chr){
			continue;
		}
		char pch=ini.charAt(i-1);
	}
	/*String[] line=ini.toString().split("\\r?\\n");
	List<Object> parts=new ArrayList<Object>();
	for(int i=0;line.length!=i;i++)
	{
		if(null==line[i] || line[i].length()<2){
			continue;
		}
		if(line[i].charAt(0)==';'){
			continue;
		}
		
	}*/
	return null;
}
public static CharSequence serialize(Object pobj,boolean asInstance,boolean asValue)
{
	return null;
}
public static CharSequence serialize(Object pobj)
{
	return serialize(pobj,true,true);
}
}