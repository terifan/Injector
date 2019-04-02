package org.terifan.injector;

import java.lang.annotation.ElementType;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target(
	{
		PARAMETER,
		FIELD
	})
public @interface Named
{
	String value();
}
