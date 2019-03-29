package org.terifan.injector;

import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target(
	{
		PARAMETER
	})
public @interface Named
{
	String value();
}
