package org.terifan.injector;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;


public class Injector
{
	final HashMap<Class, ArrayList<Binding>> mBindings;
	private boolean mStrict;
	private PrintStream mLog;


	public Injector()
	{
		mBindings = new HashMap<>();
	}


	public Injector setLog(PrintStream aLog)
	{
		mLog = aLog;
		return this;
	}


	public boolean isStrict()
	{
		return mStrict;
	}


	/**
	 * Will throw an exception if an unbound instance is created from an injection. Default is false, which allow unbound objects to be
	 * created by the injector.
	 */
	public Injector setStrict(boolean aStrict)
	{
		mStrict = aStrict;
		return this;
	}


	public synchronized ProviderBinding bind(Class aType)
	{
		ProviderBinding binding = new ProviderBinding(this, aType);

		mBindings.computeIfAbsent(aType, e -> new ArrayList<>()).add(binding);

		return binding;
	}


	public synchronized ConstantBinding bindConstant()
	{
		ConstantBinding binding = new ConstantBinding(this);

		return binding;
	}


	public <T> T getInstance(Class<T> aType)
	{
		return (T)findBinding(aType, null, null, false).getInstance(new Context());
	}


	private <T> Binding findBinding(Class<T> aType, String aNamed, Class aEnclosingType, boolean aOptional)
	{
		ArrayList<Binding> list = mBindings.get(aType);

		if (list != null)
		{
			Binding result = null;
			for (Binding binding : list)
			{
				if (binding.matches(aNamed, aEnclosingType))
				{
					if (result == null || binding.isMoreSpecific(result))
					{
						result = binding;
					}
				}
			}
			if (result != null)
			{
				return result;
			}
		}

		list = mBindings.get(ConstantBinding.class);

		if (list != null)
		{
			Binding result = null;
			for (Binding binding : list)
			{
				if (binding.matches(aNamed, aEnclosingType))
				{
					if (result == null || binding.isMoreSpecific(result))
					{
						result = binding;
					}
				}
			}
			if (result != null)
			{
				return result;
			}
		}

		if (!aOptional && (aNamed != null && !aNamed.isEmpty()))
		{
			throw new InjectionException("Type not bound: " + aType + (aNamed == null || aNamed.isEmpty() ? "" : " '" + aNamed + "'") + (aEnclosingType == null ? "" : " in " + aEnclosingType));
		}

		if (aOptional || (aNamed != null && !aNamed.isEmpty()))
		{
			return null;
		}

		if (mStrict)
		{
			throw new InjectionException("Type not bound: " + aType + (aNamed == null || aNamed.isEmpty() ? "" : " '" + aNamed + "'") + (aEnclosingType == null ? "" : " in " + aEnclosingType));
		}

		return new ProviderBinding(this, aType);
	}


	/**
	 * Injects dependencies into the fields and methods of instance.
	 */
	public Object injectMembers(Object aInstance)
	{
		return injectMembers(new Context(), aInstance);
	}


	Object injectMembers(Context aContext, Object aInstance)
	{
		visit(aContext, aInstance, mInjectVisitor);

		return aInstance;
	}


	private void visit(Context aContext, Object aInstance, Visitor aVisitor)
	{
		try
		{
			Class<?> type = aInstance.getClass();

			for (;;)
			{
				if (type == Object.class)
				{
					return;
				}

				aVisitor.visitClass(aContext, aInstance, type);

				for (Field field : type.getDeclaredFields())
				{
					aVisitor.visitField(aContext, aInstance, type, field);
				}

				for (Method method : type.getDeclaredMethods())
				{
					aVisitor.visitMethod(aContext, aInstance, type, method);
				}

				type = type.getSuperclass();
			}
		}
		catch (InjectionException e)
		{
			throw e;
		}
		catch (Exception | Error e)
		{
			throw new InjectionException(e);
		}
	}


	<T> T createInstance(Context aContext, Class<T> aType)
	{
		T instance = null;

		if (aType.isInterface())
		{
			throw new InjectionException("Cannot constructor instance of interface: " + aType);
		}

		for (Constructor constructor : aType.getConstructors())
		{
			Inject injectAnnotation = (Inject)constructor.getAnnotation(Inject.class);

			if (injectAnnotation != null)
			{
				if (mLog != null)
				{
					StringBuilder sb = new StringBuilder();
					for (Class cls : constructor.getParameterTypes())
					{
						if (sb.length() > 0)
						{
							sb.append(", ");
						}
						sb.append(cls.getSimpleName());
					}
					mLog.printf("Creating instance of [%s] using constructor [%s]%n", aType.getSimpleName(), sb);
				}

				try
				{
					Object[] mappedValues = createMappedValues(aContext, aType, constructor);

					constructor.setAccessible(true);
					instance = (T)constructor.newInstance(mappedValues);
				}
				catch (InjectionException e)
				{
					throw e;
				}
				catch (Exception | Error e)
				{
					throw new InjectionException(e);
				}

				break;
			}
		}

		if (instance == null)
		{
			try
			{
				try
				{
					Constructor constructor = aType.getDeclaredConstructor();
					constructor.setAccessible(true);

					instance = (T)constructor.newInstance();
				}
				catch (NoSuchMethodException e)
				{
					// TODO: detect inner classes properly

					// pass the enclosing object instance to the constructor.
					Constructor constructor = aType.getDeclaredConstructors()[0];
					constructor.setAccessible(true);

					instance = (T)constructor.newInstance(aContext.getEnclosingInstance());
				}
			}
			catch (InjectionException e)
			{
				throw e;
			}
			catch (Exception | Error e)
			{
				throw new InjectionException(e);
			}
		}

		if (instance != null)
		{
			injectMembers(aContext, instance);

			visit(aContext, instance, mPostConstructVisitor);
		}

		return instance;
	}


	Object[] createMappedValues(Context aContext, Class aEnclosingType, Executable aExecutable)
	{
		Named namedAnnotation = aExecutable.getAnnotation(Named.class);
		Class<?>[] paramTypes = aExecutable.getParameterTypes();
		Annotation[][] annotations = aExecutable.getParameterAnnotations();

		Object[] values = new Object[paramTypes.length];

		for (int i = 0; i < paramTypes.length; i++)
		{
			Class paramType = paramTypes[i];

			String named = namedAnnotation == null ? null : namedAnnotation.value();
			boolean optional = aExecutable.getAnnotation(Inject.class).optional();

			for (Annotation annotation : annotations[i])
			{
				if (annotation instanceof Named)
				{
					named = ((Named)annotation).value();
				}
			}

			values[i] = findBinding(paramType, named, aEnclosingType, optional).getInstance(aContext);
		}

		return values;
	}


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Injector{\n");
		sb.append("\tmBindings={\n");
		for (Entry<Class, ArrayList<Binding>> entry : mBindings.entrySet())
		{
			sb.append("\t\t" + entry.getKey() + "\n");
			for (Binding b : entry.getValue())
			{
				sb.append("\t\t\t" + b.describe() + "\n");
			}
		}
		sb.append("\t},\n");
		sb.append("\tmStrict=" + mStrict + "\n}");
		return sb.toString();
	}

	private final Visitor mPostConstructVisitor = new Visitor()
	{
		@Override
		public void visitMethod(Context aContext, Object aInstance, Class aEnclosingType, Method aMethod) throws Exception
		{
			PostConstruct annotation = aMethod.getAnnotation(PostConstruct.class);

			if (annotation != null)
			{
				if (mLog != null)
				{
					mLog.printf("Invoking PostConstruct method [%s] in instance of [%s]%n", aMethod.getName(), aEnclosingType.getSimpleName());
				}

				aMethod.setAccessible(true);
				aMethod.invoke(aInstance);
			}
		}
	};

	private final Visitor mInjectVisitor = new Visitor()
	{
		@Override
		public void visitField(Context aContext, Object aInstance, Class aEnclosingType, Field aField) throws IllegalAccessException, SecurityException
		{
			Inject injectAnnotation = aField.getAnnotation(Inject.class);

			if (injectAnnotation != null)
			{
				Named namedAnnotation = aField.getAnnotation(Named.class);
				String named = namedAnnotation == null ? "" : namedAnnotation.value();

				aField.setAccessible(true);

				if (aField.getType() == Provider.class)
				{
					Object fieldValue = new Provider<>(Injector.this, (Class)((ParameterizedType)aField.getGenericType()).getActualTypeArguments()[0], false);
					aField.set(aInstance, fieldValue);
				}
				else
				{
					Binding binding = findBinding(aField.getType(), named, aEnclosingType, injectAnnotation.optional());

					if (binding != null)
					{
						binding.populate(new Context(aContext, aInstance), aInstance, aField);
					}
					else if (!injectAnnotation.optional())
					{
						aField.set(aInstance, null);
					}
				}

				if (mLog != null)
				{
					if (named.isEmpty())
					{
						mLog.printf("Injecting [%s] instance into [%s] instance field [%s]%n", aField.get(aInstance) == null ? "null" : aField.get(aInstance).getClass().getSimpleName(), aInstance.getClass().getSimpleName(), aField.getName());
					}
					else
					{
						mLog.printf("Injecting [%s] instance named [%s] into [%s] instance field [%s]%n", aField.get(aInstance) == null ? "null" : aField.get(aInstance).getClass().getSimpleName(), named, aInstance.getClass().getSimpleName(), aField.getName());
					}
				}
			}
		}


		@Override
		public void visitMethod(Context aContext, Object aInstance, Class aEnclosingType, Method aMethod) throws IllegalAccessException, InvocationTargetException
		{
			Inject injectAnnotation = aMethod.getAnnotation(Inject.class);

			if (injectAnnotation != null)
			{
				Object[] mappedValues = createMappedValues(new Context(aContext, aInstance), aEnclosingType, aMethod);

				aMethod.setAccessible(true);
				aMethod.invoke(aInstance, mappedValues);
			}
		}
	};


	public void load(Class aClass, String aPropertiesFile)
	{
		new PropertiesReader().parse(this, aClass, aPropertiesFile);
	}
}
