package org.terifan.injector;

import java.util.function.Supplier;



public class Binding
{
	private final Injector mInjector;
	private final Context mContext;
	private final Scope mScope;
	private final Class mFromType;
	private Class mToType;
	private Provider mProvider;


	Binding(Injector aInjector, Context aContext, Class aFromType, Scope aScope)
	{
		mInjector = aInjector;
		mContext = aContext;
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
		mProvider = new SingeltonProvider(mInjector, mToType != null ? mToType : mFromType);
	}


	public void toInstance(Object aInstance)
	{
		mProvider = new SingeltonProvider(mInjector, aInstance);
	}


	public void toProvider(Supplier aSupplier)
	{
		mProvider = new Provider(mInjector)
		{
			@Override
			public Object get(Context aContext)
			{
				return mInjector.injectMembers(aContext, aSupplier.get());
			}
		};
	}


	public Binding to(Class aToType)
	{
		if (aToType.getAnnotation(Singleton.class) != null)
		{
			mProvider = new SingeltonProvider(mInjector, aToType);
		}
		else
		{
			mToType = aToType;
		}

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


	Object getInstance(Context aContext)
	{
		if (mProvider != null)
		{
			return mProvider.get(aContext);
		}

		return mInjector.createInstance(aContext, mToType != null ? mToType : mFromType);
	}


	@Override
	public String toString()
	{
		return "Binding{" + "mScope=" + mScope + ", mToType=" + mToType + ", mSupplier=" + mProvider + '}';
	}
}
