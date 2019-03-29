package org.terifan.injector.samples;

import java.util.Date;
import org.terifan.injector.Injector;


public class AllPaths
{
	public static void main(String... args)
	{
		try
		{
			Injector injector = new Injector();

			injector.bind(Date.class).asSingleton();
			injector.bind(Date.class).to(Date.class);
			injector.bind(Date.class).to(Date.class).asSingleton();
			injector.bind(Date.class).toInstance(new Date());
			injector.bind(Date.class).toProvider(() -> new Date());
			injector.bind(Date.class).in(Date.class);
			injector.bind(Date.class).in(Date.class).asSingleton();
			injector.bind(Date.class).in(Date.class).to(Date.class);
			injector.bind(Date.class).in(Date.class).to(Date.class).asSingleton();
			injector.bind(Date.class).in(Date.class).toInstance(new Date());
			injector.bind(Date.class).in(Date.class).toProvider(() -> new Date());
			injector.bind(Date.class).named("name");
			injector.bind(Date.class).named("name").asSingleton();
			injector.bind(Date.class).named("name").to(Date.class);
			injector.bind(Date.class).named("name").to(Date.class).asSingleton();
			injector.bind(Date.class).named("name").toInstance(new Date());
			injector.bind(Date.class).named("name").toProvider(() -> new Date());
			injector.bind(Date.class).named("name").in(Date.class);
			injector.bind(Date.class).named("name").in(Date.class).asSingleton();
			injector.bind(Date.class).named("name").in(Date.class).to(Date.class);
			injector.bind(Date.class).named("name").in(Date.class).to(Date.class).asSingleton();
			injector.bind(Date.class).named("name").in(Date.class).toInstance(new Date());
			injector.bind(Date.class).named("name").in(Date.class).toProvider(() -> new Date());

			injector.getInstance(Date.class);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
