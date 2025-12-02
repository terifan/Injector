package org.terifan.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Implementation of the Factory pattern.
 */
public class Factory
{
	private final HashMap<Class, Supplier> mSuppliers = new HashMap<>();
	private final HashMap<Class, Class> mBoundTypes = new HashMap<>();
	private final HashMap<Class, Object> mSingletons = new HashMap<>();
	private final HashSet<Class> mFutureSingletons = new HashSet<>();
	private final HashMap<Class, HashMap<Object, Function>> mProducers = new HashMap<>();

	private final HashMap<String, Supplier> mNamedSuppliers = new HashMap<>();
	private final HashMap<String, Class> mNamedTypes = new HashMap<>();
	private final HashMap<String, Object> mNamedSingletons = new HashMap<>();
	private final HashSet<String> mNamedFutureSingletons = new HashSet<>();


	public Factory()
	{
		init();
	}


	/**
	 * This method is called by the constructor, override it to initialize this instance.
	 */
	protected void init()
	{
	}


	public <T> FactoryBind<T> bind(Class<T> aFrom)
	{
		mBoundTypes.put(aFrom, aFrom);
		return new FactoryBind<>(aFrom);
	}


	public FactoryNamedBind bindNamed(String aName)
	{
		return new FactoryNamedBind(aName);
	}


	public Producer with(Object aParameter)
	{
		if (aParameter == null)
		{
			throw new IllegalArgumentException("Provided parameter is null.");
		}

		HashMap<Object, Function> map = mProducers.get(aParameter.getClass());

		if (map == null)
		{
			throw new IllegalArgumentException("Parameter type is not bound: " + aParameter.getClass());
		}

		return new Producer(map, aParameter);
	}


	public <T> T named(String aName)
	{
		T instance = (T)mNamedSingletons.get(aName);

		if (instance == null)
		{
			instance = (T)mNamedSuppliers.getOrDefault(aName, () ->
			{
				try
				{
					Class type = mNamedTypes.get(aName);

					if (type == null)
					{
						throw new IllegalArgumentException("Named type not bound: " + aName);
					}

					return (T)type.newInstance();
				}
				catch (IllegalAccessException | InstantiationException e)
				{
					throw new IllegalStateException(e);
				}
			}).get();

			if (mNamedFutureSingletons.remove(aName))
			{
				mNamedSingletons.put(aName, instance);
			}
		}

		return instance;
	}


	public <T> T get(Class<T> aType)
	{
		T instance = (T)mSingletons.get(aType);

		if (instance == null)
		{
			instance = (T)mSuppliers.getOrDefault(aType, () ->
			{
				try
				{
					return (T)mBoundTypes.getOrDefault(aType, aType).newInstance();
				}
				catch (IllegalAccessException | InstantiationException e)
				{
					throw new IllegalStateException(e);
				}
			}).get();

			if (mFutureSingletons.remove(aType))
			{
				mSingletons.put(aType, instance);
			}
		}

		return instance;
	}


	public class FactoryBind<R>
	{
		private Class<R> mFromType;


		FactoryBind(Class<R> aType)
		{
			mFromType = aType;
		}


		public <T> FactoryBindTo to(Class aTo)
		{
			mBoundTypes.put(mFromType, aTo);
			return new FactoryBindTo<>(mFromType);
		}


		public FactoryBindTo<R> toSupplier(Supplier aSupplier)
		{
			mSuppliers.put(mFromType, aSupplier);
			return new FactoryBindTo<>(mFromType);
		}


		public void toInstance(R aInstance)
		{
			mSingletons.put(mFromType, aInstance);
		}


		public void asSingleton()
		{
			mFutureSingletons.add(mFromType);
		}


		public <U> FactoryProducer<U, R> with(Class<U> aParameterType)
		{
			return new FactoryProducer<>(mFromType, aParameterType);
		}
	}


	public class FactoryNamedBind
	{
		private String mName;


		FactoryNamedBind(String aName)
		{
			mName = aName;
		}


		public FactoryNamedBindTo to(Class aTo)
		{
			mNamedTypes.put(mName, aTo);
			return new FactoryNamedBindTo(mName);
		}


		public FactoryNamedBindTo toSupplier(Supplier aSupplier)
		{
			mNamedSuppliers.put(mName, aSupplier);
			return new FactoryNamedBindTo(mName);
		}


		public void toInstance(Object aInstance)
		{
			mNamedSingletons.put(mName, aInstance);
		}


		public <T> FactoryProducer<T, Object> with(Class<T> aParameterType)
		{
			return new FactoryProducer<T, Object>(mName, aParameterType);
		}
	}


	public class FactoryProducer<T,R>
	{
		private Object mKey;
		private Class<T> mParameterType;


		FactoryProducer(Object aKey, Class<T> aParameterType)
		{
			mKey = aKey;
			mParameterType = aParameterType;
		}


		public void toProducer(Function<T, R> aFunction)
		{
			HashMap<Object, Function> map = mProducers.computeIfAbsent(mParameterType, k -> new HashMap<>());
			map.put(mKey, aFunction);
		}
	}


	public class Producer
	{
		private HashMap<Object, Function> mMap;
		private Object mParameter;


		Producer(HashMap<Object, Function> aMap, Object aParameter)
		{
			mMap = aMap;
			mParameter = aParameter;
		}


		public <T> T get(Class<T> aType)
		{
			Function fn = mMap.get(aType);

			if (fn == null)
			{
				throw new IllegalArgumentException("Type is not bound to a producer: " + aType);
			}

			T instance = (T)fn.apply(mParameter);

			return instance;
		}


		public <T> T get(String aName)
		{
			Function fn = mMap.get(aName);

			if (fn == null)
			{
				throw new IllegalArgumentException("Named type is not bound to a producer: " + mParameter.getClass());
			}

			T instance = (T)fn.apply(mParameter);

			return instance;
		}
	}


	public class FactoryBindTo<T>
	{
		private Class<T> mFromType;


		FactoryBindTo(Class<T> aType)
		{
			mFromType = aType;
		}


		public void asSingleton()
		{
			mFutureSingletons.add(mFromType);
		}
	}


	public class FactoryNamedBindTo
	{
		private String mName;


		FactoryNamedBindTo(String aName)
		{
			mName = aName;
		}


		public void asSingleton()
		{
			mNamedFutureSingletons.add(mName);
		}
	}
}
