package org.terifan.injector;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Properties;


public class PropertiesReader
{
	public void parse(Injector aInjector, Class aClass, String aFile)
	{
		try
		{
			Properties p = new Properties();
			try (InputStream in = aClass.getResourceAsStream(aFile))
			{
				p.load(in);
			}
			for (Map.Entry entry : p.entrySet())
			{
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				if (value.isBlank())
				{
					aInjector.bindConstant().named(key).to("");
				}
				else if (value.matches("[0-9]+"))
				{
					aInjector.bindConstant().named(key).to(Integer.parseInt(value));
				}
				else if (value.matches("[0-9]+L"))
				{
					aInjector.bindConstant().named(key).to(Long.parseLong(value));
				}
				else if (value.matches("[0-9]*\\.[0-9]+"))
				{
					aInjector.bindConstant().named(key).to(Double.parseDouble(value));
				}
				else if (value.matches("[0-9]*\\.[0-9]+[Ff]|[0-9]*\\.[0-9]*[Ff]|[0-9]*[Ff]"))
				{
					aInjector.bindConstant().named(key).to(Float.parseFloat(value));
				}
				else if (value.matches("\".*\""))
				{
					aInjector.bindConstant().named(key).to(value.substring(1, value.length() - 1));
				}
				else if (value.matches("java:.*\\(.*\\)"))
				{
					value = value.substring(5);

					String t = value.substring(value.indexOf('(') + 1, value.indexOf(')'));
					Object[] raw = t.split(",");
					Object[] parameters = new Object[raw.length];
					Class[] parameterTypes = new Class[raw.length];
					for (int i = 0; i < parameters.length; i++)
					{
						parameters[i] = Integer.parseInt(raw[i].toString());
						parameterTypes[i] = Integer.TYPE;
					}
					Class<?> type = Class.forName(value.substring(0, value.indexOf('(')));
					Constructor<?> constructor = type.getConstructor(parameterTypes);
					Object instance = constructor.newInstance(parameters);
					aInjector.bind(type).named(key).toInstance(instance);
				}
				else
				{
					System.out.println("Failed to parse property file: key: " + key + ", value: " + value);
				}
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
}
