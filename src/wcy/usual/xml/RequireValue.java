package wcy.usual.xml;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited()
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD})
public @interface RequireValue {
RequireValueDef needType() default RequireValueDef.NATURAL;
String toJson() default "";
String toJava() default "";
}
