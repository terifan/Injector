package org.terifan.injector;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target(
	{
		METHOD, FIELD, CONSTRUCTOR
	})
public @interface Inject
{
	String value() default "";
	String name() default "";
	boolean optional() default false;
}
