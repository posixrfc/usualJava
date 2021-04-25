package wcy.usual.xml;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlRequire
{
abstract boolean value() default true;
public boolean attr() default false;
public String toXml() default "";
public String toJava() default "";
}
