package wcy.usual.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.PACKAGE,ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebHandler
{
public abstract byte method() default 127;

public abstract String[] pattern() default {};
public abstract String[] session() default {};

public static final byte GET=1;
public static final byte POST=2;
public static final byte PUT=4;
public static final byte DELETE=8;
public static final byte HEAD=16;
public static final byte TRACE=32;
public static final byte OPTIONS=64;
}