package wcy.usual.xml;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited()
@Retention(RetentionPolicy.RUNTIME)
@Target({PACKAGE,TYPE})
public @interface RequireType
{
RequireTypeDef[] needType() default {RequireTypeDef.PUBLIC};
RequireTypeDef[] elideType() default {RequireTypeDef.IGNORE_clone,RequireTypeDef.IGNORE_getClass,RequireTypeDef.IGNORE_hashCode,RequireTypeDef.IGNORE_toString};

boolean onlyGetter() default true;
boolean onlySetter() default true;

boolean onlyMethod() default false;
boolean methodPriority() default true;
}
