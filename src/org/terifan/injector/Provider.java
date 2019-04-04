package org.terifan.injector;


public class Provider<T>
{
	private final Injector mInjector;
	private Class<T> mType;
	private T mInstance;
	private boolean mSingleton;


	public Provider(Injector aInjector, Class aType, boolean aSingleton)
	{
		mInjector = aInjector;
		mType = aType;
		mSingleton = aSingleton || mType.getAnnotation(Singleton.class) != null;
	}


	public Provider(Injector aInjector, T aInstance, boolean aSingleton)
	{
		mInjector = aInjector;
		mInstance = aInstance;
		mSingleton = true;
	}


	public T get()
	{
		try
		{
			if (mSingleton)
			{
				if (mInstance == null)
				{
					mInstance = mInjector.getInstance(mType);
				}

				return mInstance;
			}

			return mInjector.getInstance(mType);
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


	T get(Context aContext)
	{
		try
		{
			if (mSingleton)
			{
				if (mInstance == null)
				{
					mInstance = mInjector.createInstance(aContext, mType);
				}

				return mInstance;
			}

			return mInjector.createInstance(aContext, mType);
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
}
