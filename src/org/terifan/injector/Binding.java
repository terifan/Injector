package org.terifan.injector;

import java.util.function.Supplier;


public class Binding
{
	private final Injector mInjector;
	private final Scope mScope;
	private final Class mFromType;
	private Class mToType;
	private Supplier mSupplier;


	Binding(Injector aInjector, Class aFromType, Scope aScope)
	{
		mInjector = aInjector;
		mFromType = aFromType;
		mScope = aScope;
	}


	Scope getScope()
	{
		return mScope;
	}


	Class getToType()
	{
		return mToType;
	}


	public void asSingleton()
	{
		mSupplier = new SingeltonSupplier(mInjector, mToType != null ? mToType : mFromType, mScope);
	}


	public void toInstance(Object aInstance)
	{
		mSupplier = new SingeltonSupplier(aInstance);
	}


	public void toProvider(Supplier aSupplier)
	{
		mSupplier = aSupplier;
	}


	public Binding to(Class aToType)
	{
		mToType = aToType;
		return this;
	}


	public Binding named(String aName)
	{
		mScope.setName(aName);
		return this;
	}


	public Binding in(Class aScope)
	{
		mScope.setType(aScope);
		return this;
	}


	Object getInstance()
	{
		if (mSupplier != null)
		{
			return mInjector.injectMembers(mSupplier.get());
		}

		return mInjector.createInstance(mToType != null ? mToType : mFromType);
	}


	@Override
	public String toString()
	{
		return "Binding{" + "mScope=" + mScope + ", mToType=" + mToType + ", mSupplier=" + mSupplier + '}';
	}
}
