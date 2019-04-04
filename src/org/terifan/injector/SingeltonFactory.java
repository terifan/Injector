package org.terifan.injector;


class SingeltonFactory<T> extends Factory<T>
{
	private Class<T> mType;
	private T mInstance;
	private boolean mSingleton;


	public SingeltonFactory(Injector aInjector, Class aType, boolean aSingleton)
	{
		super(aInjector);

		mType = aType;
		mSingleton = aSingleton || mType.getAnnotation(Singleton.class) != null;
	}


	public SingeltonFactory(Injector aInjector, T aInstance, boolean aSingleton)
	{
		super(aInjector);

		mInstance = aInstance;
		mSingleton = true;
	}


//	public T get()
//	{
//		try
//		{
//			if (mSingleton)
//			{
//				if (mInstance == null)
//				{
//					mInstance = mInjector.getInstance(mType);
//				}
//
//				return mInstance;
//			}
//
//			return mInjector.getInstance(mType);
//		}
//		catch (InjectionException e)
//		{
//			throw e;
//		}
//		catch (Exception | Error e)
//		{
//			throw new InjectionException(e);
//		}
//	}


	@Override
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
