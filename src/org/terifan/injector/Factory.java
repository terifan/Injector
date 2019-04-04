package org.terifan.injector;


abstract class Factory<T>
{
	protected final Injector mInjector;


	Factory(Injector aInjector)
	{
		mInjector = aInjector;
	}


	abstract T get(Context aContext);
}
