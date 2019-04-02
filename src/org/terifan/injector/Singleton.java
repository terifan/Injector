package org.terifan.injector;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * A class instance created with this annotation will become a singleton.
 *
 * This code without the annotation <code>injector.bind(SomeClassWithoutAnnotation.class).asSingleton();</code> is equivalent to this with
 * the annotation <code>injector.bind(SomeClassWithAnnotation.class);</code>.
 */
@Retention(RUNTIME)
@Target(
	{
		TYPE
	})
public @interface Singleton
{
}
