package wcy.usual;

import java.util.Arrays;
import java.util.List;

public class UtilCalendar {
public static void main(String[] args) {
	List<String> lst = Arrays.asList(getMonthlyDate("19970128",542));
	for(int i=0,len=lst.size();len!=i;i++){
		if(0==i%12){
			System.out.println();
		}
		System.out.print(lst.get(i)+'\t');
	}
}
public static String[] getMonthlyDate(final String date,int len){
	String[] dates=new String[len];
	len+=1;
	int year=Integer.parseUnsignedInt(date.substring(0,4)),month=Integer.parseUnsignedInt(date.substring(5,7)),day=Integer.parseUnsignedInt(date.substring(8,10));
	final int srcDay=day;
	for(int i=0;len!=i;i++){
		if(0!=i){
			dates[i-1]=getYYYY_MM_DDRepaymentDate(year,month,day);
		}
		if(12==month){
			month=1;
			year+=1;
			continue;
		}
		month+=1;//2-11月
		if(srcDay<29){
			continue;
		}
		switch(month){//29,30,31
		case 2:
			if(0==year%400){
				day=29;
				break;
			}
			if((0==year%4)&&(0!=year%100)){
				day=29;
				break;
			}
			day=28;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			if(31==srcDay){
				day=30;
				break;
			}
			day=srcDay;//29,30
			break;
		default://3,5,7,8,10,12
			day=srcDay;
		}
	}
	return dates;
}
public static String getYYYY_MM_DDRepaymentDate(int y,int m,int d){
	String date=String.valueOf(y);
	if(9<m){
		date=date+'-'+m;
	}else{
		date=date+"-0"+m;
	}
	if(9<d){
		date=date+'-'+d;
	}else{
		date=date+"-0"+d;
	}
	return date;
}
}
