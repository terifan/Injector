package org.terifan.injector;


public class Provider<T>
{
	private Class<T> mType;
	private T mInstance;
	private Injector mInjector;
	private boolean mSingleton;


	Provider(Injector aInjector, Class<T> aType)
	{
		mInjector = aInjector;
		mType = aType;
		mSingleton = mType.getAnnotation(Singleton.class) != null;
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
}
