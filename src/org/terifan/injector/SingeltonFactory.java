package org.terifan.injector;


class SingeltonFactory<T> extends Factory<T>
{
	private Class<T> mType;
	private T mInstance;


	public SingeltonFactory(Injector aInjector, Class aType)
	{
		super(aInjector);

		mType = aType;
	}


	public SingeltonFactory(Injector aInjector, T aInstance)
	{
		super(aInjector);

		mInstance = aInstance;
	}


	@Override
	public T get(Context aContext)
	{
		try
		{
			if (mInstance == null)
			{
				mInstance = mInjector.createInstance(aContext, mType);
			}

			return mInstance;
		}
		catch (Exception | Error e)
		{
			throw new InjectionException(e);
		}
	}
}
