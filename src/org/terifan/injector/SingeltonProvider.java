package org.terifan.injector;


class SingeltonProvider<T> extends Provider<T>
{
	private Class<T> mType;
	private T mInstance;


	public SingeltonProvider(Injector aInjector, Class aType)
	{
		super(aInjector);

		mType = aType;
	}


	public SingeltonProvider(Injector aInjector, T aInstance)
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
