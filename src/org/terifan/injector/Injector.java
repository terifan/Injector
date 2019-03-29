package org.terifan.injector;

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


	public Injector()
	{
		mBindings = new HashMap<>();
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
		Binding binding = new Binding(this, aType, new Scope());
		mBindings.computeIfAbsent(aType, e->new ArrayList<>()).add(binding);
		return binding;
	}


	public <T> T getInstance(Class<T> aType)
	{
		return getInstance(aType, new Scope());
	}


	<T> T getInstance(Class<T> aType, Scope aScope)
	{
		ArrayList<Binding> list = mBindings.get(aType);

		if (list != null)
		{
			for (Binding binding : list)
			{
				Scope bindingScope = binding.getScope();

				if ((bindingScope.getType() == null || bindingScope.getType() == aScope.getType()) && Objects.equals(aScope.getName(), bindingScope.getName()))
				{
					return (T)binding.getInstance();
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

		return createInstance(aType);
	}


	/**
	 * Injects dependencies into the fields and methods of instance.
	 */
	public Object injectMembers(Object aInstance)
	{
		visit(aInstance, mInjectVisitor);

		return aInstance;
	}


	private final Visitor mPostConstructVisitor = new Visitor()
	{
		@Override
		public void visitMethod(Object aInstance, Class aType, Method aMethod) throws Exception
		{
			if (aMethod.getAnnotation(PostConstruct.class) != null)
			{
				logPostConstruct(aType, aMethod);

				aMethod.setAccessible(true);
				aMethod.invoke(aInstance);
			}
		}
	};


	private final Visitor mInjectVisitor = new Visitor()
	{
		@Override
		public void visitField(Object aInstance, Class aType, Field aField) throws IllegalAccessException, SecurityException
		{
			if (aField.getAnnotation(Inject.class) != null)
			{
				Inject annotation = aField.getAnnotation(Inject.class);

				Object fieldValue = getInstance(aField.getType(), new Scope(getName(annotation), aType, annotation.optional()));

				logInjection(aInstance, aField, fieldValue, annotation);

				aField.setAccessible(true);
				aField.set(aInstance, fieldValue);
			}
		}


		@Override
		public void visitMethod(Object aInstance, Class aType, Method aMethod) throws IllegalAccessException, InvocationTargetException
		{
			Inject annotation = aMethod.getAnnotation(Inject.class);

			if (annotation != null)
			{
				aMethod.setAccessible(true);
				aMethod.invoke(aInstance, createMappedValues(aType, annotation, aMethod.getParameterTypes(), aMethod.getParameterAnnotations()));
			}
		}
	};


	private void visit(Object aInstance, Visitor aVisitor)
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

				aVisitor.visitClass(aInstance, type);

				for (Field field : type.getDeclaredFields())
				{
					aVisitor.visitField(aInstance, type, field);
				}

				for (Method method : type.getDeclaredMethods())
				{
					aVisitor.visitMethod(aInstance, type, method);
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
			throw new IllegalArgumentException(e);
		}
	}


	<T> T createInstance(Class<T> aType)
	{
		T instance = null;

		System.out.println("#"+aType.getConstructors().length);

		for (Constructor constructor : aType.getConstructors())
		{
			Inject annotation = (Inject)constructor.getAnnotation(Inject.class);

			if (annotation != null)
			{
				logCreation(aType, constructor);

				try
				{
					instance = (T)constructor.newInstance(createMappedValues(aType, annotation, constructor.getParameterTypes(), constructor.getParameterAnnotations()));
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
				Constructor<T> constructor = aType.getDeclaredConstructor();
				constructor.setAccessible(true);
				instance = constructor.newInstance();
			}
			catch (Exception | Error e)
			{
				// ignore
				e.printStackTrace(System.out);
			}
		}

		if (instance != null)
		{
			injectMembers(instance);

			visit(instance, mPostConstructVisitor);
		}

		return instance;
	}


	Object[] createMappedValues(Class aScopeType, Inject aInjectAnnotation, Class[] aParamTypes, Annotation[][] aAnnotations)
	{
		Object[] values = new Object[aParamTypes.length];

		for (int i = 0; i < aParamTypes.length; i++)
		{
			Class paramType = aParamTypes[i];

			String scopeName = aInjectAnnotation.value();
			boolean optional = aInjectAnnotation.optional();

			for (Annotation ann : aAnnotations[i])
			{
				if (ann instanceof Named)
				{
					scopeName = ((Named)ann).value();
				}
			}

			values[i] = getInstance(paramType, new Scope(scopeName, aScopeType, optional));
		}

		return values;
	}


	private void logInjection(Object aInstance, Field aField, Object aMappedType, Inject aAnnotation)
	{
		if (getName(aAnnotation).isEmpty())
		{
			System.out.printf("Injecting [%s] instance into [%s] instance field [%s]%n", aMappedType==null?"null":aMappedType.getClass().getSimpleName(), aInstance.getClass().getSimpleName(), aField.getName());
		}
		else
		{
			System.out.printf("Injecting [%s] instance named [%s] into [%s] instance field [%s]%n", aMappedType==null?"null":aMappedType.getClass().getSimpleName(), getName(aAnnotation), aInstance.getClass().getSimpleName(), aField.getName());
		}
	}


	private void logCreation(Class aType, Constructor aConstructor)
	{
		StringBuilder sb = new StringBuilder();
		for (Class cls : aConstructor.getParameterTypes())
		{
			if (sb.length() > 0)
			{
				sb.append(", ");
			}
			sb.append(cls.getSimpleName());
		}
		System.out.printf("Creating instance of [%s] using constructor [%s]%n", aType.getSimpleName(), sb);
	}


	private void logPostConstruct(Class aType, Method aMethod)
	{
		System.out.printf("Invoking PostConstruct method [%s] in instance of [%s]%n", aMethod.getName(), aType.getSimpleName());
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
}
