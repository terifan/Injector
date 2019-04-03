package org.terifan.injector;


abstract class Factory<T>
{
	protected final Injector mInjector;


	Factory(Injector aInjector)
	{
		mInjector = aInjector;
	}


	public abstract T get(Context aContext);
}
