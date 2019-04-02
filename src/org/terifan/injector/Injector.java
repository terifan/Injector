package org.terifan.injector;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;


public class Injector
{
	private final HashMap<Class, ArrayList<Binding>> mBindings;
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


	public Binding bind(Class aType)
	{
		Binding binding = new Binding(this, new Context(), aType, new Scope());

		mBindings.computeIfAbsent(aType, e -> new ArrayList<>()).add(binding);

		return binding;
	}


	public <T> T getInstance(Class<T> aType)
	{
		return getInstance(new Context(), aType, new Scope());
	}


	<T> T getInstance(Context aContext, Class<T> aType, Scope aScope)
	{
		ArrayList<Binding> list = mBindings.get(aType);

		if (list != null)
		{
			for (Binding binding : list)
			{
				Scope bindingScope = binding.getScope();

				if ((bindingScope.getType() == null || bindingScope.getType() == aScope.getType()) && Objects.equals(aScope.getName(), bindingScope.getName()))
				{
					return (T)binding.getInstance(aContext);
				}
			}
		}

		if (!aScope.isOptional() && aScope.getName() != null)
		{
			throw new InjectionException("Named type not bound: " + aType + ", " + aScope);
		}

		if (aScope.isOptional() || aScope.getName() != null)
		{
			return null;
		}

		if (mStrict)
		{
			throw new InjectionException("Type not bound: " + aType + ", " + aScope);
		}

		return createInstance(aContext, aType);
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
		catch (RuntimeException e)
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

		for (Constructor constructor : aType.getConstructors())
		{
			Inject annotation = (Inject)constructor.getAnnotation(Inject.class);

			if (annotation != null)
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
					instance = (T)constructor.newInstance(createMappedValues(aContext, aType, annotation, constructor.getParameterTypes(), constructor.getParameterAnnotations()));
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

					instance = (T)constructor.newInstance(aContext.mEnclosingInstance);
				}
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


	Object[] createMappedValues(Context aContext, Class aScopeType, Inject aInjectAnnotation, Class[] aParamTypes, Annotation[][] aAnnotations)
	{
		Object[] values = new Object[aParamTypes.length];

		for (int i = 0; i < aParamTypes.length; i++)
		{
			Class paramType = aParamTypes[i];

			String scopeName = aInjectAnnotation.value();
			boolean optional = aInjectAnnotation.optional();

			for (Annotation annotation : aAnnotations[i])
			{
				if (annotation instanceof Named)
				{
					scopeName = ((Named)annotation).value();
				}
			}

			values[i] = getInstance(aContext, paramType, new Scope(scopeName, aScopeType, optional));
		}

		return values;
	}


	private String getName(Inject aAnnotation)
	{
		return aAnnotation.name().isEmpty() ? aAnnotation.value() : aAnnotation.name();
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
				if (b.getScope().getType() == null && b.getScope().getName() == null)
				{
					sb.append("\t\t\t" + b.getToType() + "\n");
				}
				else
				{
					sb.append("\t\t\t" + b.getToType() + " in scope " + b.getScope() + "\n");
				}
			}
		}
		sb.append("\t},\n");
		sb.append("\tmStrict=" + mStrict + "\n}");
		return sb.toString();
	}

	private final Visitor mPostConstructVisitor = new Visitor()
	{
		@Override
		public void visitMethod(Context aContext, Object aInstance, Class aType, Method aMethod) throws Exception
		{
			PostConstruct annotation = aMethod.getAnnotation(PostConstruct.class);

			if (annotation != null)
			{
				if (mLog != null)
				{
					mLog.printf("Invoking PostConstruct method [%s] in instance of [%s]%n", aMethod.getName(), aType.getSimpleName());
				}

				aMethod.setAccessible(true);
				aMethod.invoke(aInstance);
			}
		}
	};

	private final Visitor mInjectVisitor = new Visitor()
	{
		@Override
		public void visitField(Context aContext, Object aInstance, Class aType, Field aField) throws IllegalAccessException, SecurityException
		{
			Inject annotation = aField.getAnnotation(Inject.class);

			if (annotation != null)
			{
				Object fieldValue = getInstance(new Context(aContext, aInstance), aField.getType(), new Scope(getName(annotation), aType, annotation.optional()));

				if (mLog != null)
				{
					if (getName(annotation).isEmpty())
					{
						mLog.printf("Injecting [%s] instance into [%s] instance field [%s]%n", fieldValue == null ? "null" : fieldValue.getClass().getSimpleName(), aInstance.getClass().getSimpleName(), aField.getName());
					}
					else
					{
						mLog.printf("Injecting [%s] instance named [%s] into [%s] instance field [%s]%n", fieldValue == null ? "null" : fieldValue.getClass().getSimpleName(), getName(annotation), aInstance.getClass().getSimpleName(), aField.getName());
					}
				}

				aField.setAccessible(true);
				aField.set(aInstance, fieldValue);
			}
		}


		@Override
		public void visitMethod(Context aContext, Object aInstance, Class aType, Method aMethod) throws IllegalAccessException, InvocationTargetException
		{
			Inject annotation = aMethod.getAnnotation(Inject.class);

			if (annotation != null)
			{
				aMethod.setAccessible(true);
				aMethod.invoke(aInstance, createMappedValues(new Context(aContext, aInstance), aType, annotation, aMethod.getParameterTypes(), aMethod.getParameterAnnotations()));
			}
		}
	};
}
