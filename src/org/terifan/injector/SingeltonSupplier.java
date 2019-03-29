package org.terifan.injector;

import java.util.function.Supplier;


class SingeltonSupplier<T> implements Supplier<T>
{
	private Injector mInjector;
	private Class<T> mType;
	private Scope mScope;
	private T mInstance;


	public SingeltonSupplier(Injector aInjector, Class aType, Scope aScope)
	{
		mInjector = aInjector;
		mType = aType;
		mScope = aScope;
	}


	public SingeltonSupplier(T aInstance)
	{
		mInstance = aInstance;
	}


	@Override
	public T get()
	{
		try
		{
			if (mInstance == null)
			{
				mInstance = mInjector.getInstance(null, mType, mScope);
			}

			return mInstance;
		}
		catch (Exception | Error e)
		{
			throw new InjectionException(e);
		}
	}
}
