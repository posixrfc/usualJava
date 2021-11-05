package wcy.usual.func;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

public interface Times
{//LocalDateTime|LocalDate|LocalTime|Instant|Date|Calendar|timestamp
public static long getDiffValue(byte field,Serializable arg0,Serializable arg1)
{
	LocalDateTime dt0=null,dt1=null;
	LocalDate ld0=null,ld1=null;
	LocalTime lt0=null,lt1=null;
	Instant in0=null,in1=null;
	long ms0=0L,ms1=0L;
	if(YEAR==field || MONTH==field || DATE==field){
		if(arg0 instanceof Integer){
			ms0=(Integer)arg0;
			ld0=LocalDate.from(Instant.ofEpochMilli(ms0));
		}else if(arg0 instanceof Long){
			ms0=(Long)arg0;
			ld0=LocalDate.from(Instant.ofEpochMilli(ms0));
		}else if(arg0 instanceof Date){
			ms0=((Date)arg0).getTime();
			ld0=LocalDate.from(Instant.ofEpochMilli(ms0));
		}else if(arg0 instanceof Calendar){
			ms0=((Calendar)arg0).getTimeInMillis();
			ld0=LocalDate.from(Instant.ofEpochMilli(ms0));
		}else if(arg0 instanceof LocalDateTime){
			ms0=((LocalDateTime)arg0).toInstant(ZoneOffset.UTC).toEpochMilli();
			ld0=((LocalDateTime)arg0).toLocalDate();
		}else if(arg0 instanceof LocalDate){
			ms0=((LocalDate)arg0).toEpochSecond(LocalTime.NOON,ZoneOffset.UTC)*1000L;
			ld0=(LocalDate)arg0;
		}else if(arg0 instanceof Instant){
			ms0=((Instant)arg0).toEpochMilli();
			ld0=LocalDate.from((Instant)arg0);
		}else{
			return Long.MIN_VALUE;
		}
		if(arg1 instanceof Integer){
			ms1=(Integer)arg1;
			ld1=LocalDate.from(Instant.ofEpochMilli(ms1));
		}else if(arg1 instanceof Long){
			ms1=(Long)arg1;
			ld1=LocalDate.from(Instant.ofEpochMilli(ms1));
		}else if(arg1 instanceof Date){
			ms1=((Date)arg1).getTime();
			ld1=LocalDate.from(Instant.ofEpochMilli(ms1));
		}else if(arg1 instanceof Calendar){
			ms1=((Calendar)arg1).getTimeInMillis();
			ld1=LocalDate.from(Instant.ofEpochMilli(ms1));
		}else if(arg1 instanceof LocalDateTime){
			ms1=((LocalDateTime)arg1).toInstant(ZoneOffset.UTC).toEpochMilli();
			ld1=((LocalDateTime)arg1).toLocalDate();
		}else if(arg1 instanceof LocalDate){
			ms1=((LocalDate)arg1).toEpochSecond(LocalTime.NOON,ZoneOffset.UTC)*1000L;
			ld1=(LocalDate)arg1;
		}else if(arg1 instanceof Instant){
			ms1=((Instant)arg1).toEpochMilli();
			ld1=LocalDate.from((Instant)arg1);
		}else{
			return Long.MIN_VALUE;
		}
		int y=ld1.getYear()-ld0.getYear();
		if(YEAR==field){
			return y;
		}
		if(MONTH==field){
			return y*12+ld1.getMonthValue()-ld0.getMonthValue();
		}
		
		return y*12+ld1.getMonthValue()-ld0.getMonthValue();
	}
	if(MONTH==field){
		return 0;
	}
	if(DATE==field){
		return 0;
	}
	if(HOUR==field){
		return 0;
	}
	if(MINUTE==field){
		return 0;
	}
	if(SECOND==field){
		return 0;
	}
	if(MILLILS==field){
		return 0;
	}
	return Long.MIN_VALUE;
}
public static long getTimeStamp(Serializable arg)
{
	if(arg instanceof Integer){
		return (Integer)arg;
	}else if(arg instanceof Long){
		return (Long)arg;
	}else if(arg instanceof Date){
		return ((Date)arg).getTime();
	}else if(arg instanceof Calendar){
		return ((Calendar)arg).getTimeInMillis();
	}else if(arg instanceof LocalDateTime){
		return ((LocalDateTime)arg).toInstant(ZoneOffset.UTC).toEpochMilli();
	}else if(arg instanceof LocalDate){
		return ((LocalDate)arg).toEpochSecond(LocalTime.NOON,ZoneOffset.UTC)*1000L;
	}else if(arg instanceof Instant){
		return ((Instant)arg).toEpochMilli();
	}else{
		return Long.MIN_VALUE;
	}
}
public static long getDiffValue(byte field,Calendar arg0,Calendar arg1)
{
	int y=arg1.get(Calendar.YEAR)-arg0.get(Calendar.YEAR);
	if(YEAR==field){
		return y;
	}
	int m=arg1.get(Calendar.MONTH)-arg0.get(Calendar.MONTH);
	if(MONTH==field){
		return y*12+m;
	}
	long s=arg1.getTimeInMillis()-arg0.getTimeInMillis();
	if(DATE==field){
		return s/86400000L;
	}
	if(HOUR==field){
		return s/3600000L;
	}
	if(MINUTE==field){
		return s/60000L;
	}
	if(SECOND==field){
		return s/1000L;
	}
	if(MILLILS==field){
		return s;
	}
	return Long.MIN_VALUE;
}
public static final byte YEAR=32;
public static final byte MONTH=16;
public static final byte DATE=8;
public static final byte HOUR=4;
public static final byte MINUTE=2;
public static final byte SECOND=1;
public static final byte MILLILS=64;
}