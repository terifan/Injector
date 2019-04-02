package org.terifan.injector;


abstract class Provider<T>
{
	protected final Injector mInjector;


	Provider(Injector aInjector)
	{
		mInjector = aInjector;
	}


	public abstract T get(Context aContext);
}
