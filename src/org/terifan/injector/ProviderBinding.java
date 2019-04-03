package org.terifan.injector;

import java.util.function.Supplier;


public class ProviderBinding extends Binding
{
	private final Class mFromType;
	private Class mToType;
	private Factory mProvider;


	ProviderBinding(Injector aInjector, Class aFromType)
	{
		super(aInjector);

		mFromType = aFromType;
	}


	public void asSingleton()
	{
		mProvider = new SingeltonFactory(mInjector, mToType != null ? mToType : mFromType);
	}


	public void toInstance(Object aInstance)
	{
		mProvider = new SingeltonFactory(mInjector, aInstance);
	}


	public void toProvider(Supplier aSupplier)
	{
		mProvider = new Factory(mInjector)
		{
			@Override
			public Object get(Context aContext)
			{
				return mInjector.injectMembers(aContext, aSupplier.get());
			}
		};
	}


	public ProviderBinding to(Class aToType)
	{
		if (aToType.getAnnotation(Singleton.class) != null)
		{
			mProvider = new SingeltonFactory(mInjector, aToType);
		}
		else
		{
			mToType = aToType;
		}

		return this;
	}


	public ProviderBinding named(String aName)
	{
		mNamed = aName;
		return this;
	}


	public ProviderBinding in(Class aEnclosingType)
	{
		mEnclosingType = aEnclosingType;
		return this;
	}


	@Override
	Object getInstance(Context aContext)
	{
		if (mProvider != null)
		{
			return mProvider.get(aContext);
		}

		return mInjector.createInstance(aContext, mToType != null ? mToType : mFromType);
	}



	@Override
	String describe()
	{
		if (mToType == null)
		{
			return "<self>";
		}
		if (mEnclosingType == null && mNamed == null)
		{
			return mToType.toString();
		}

		return mToType + " in scope " + mEnclosingType + ", " + mNamed;
	}


	@Override
	public String toString()
	{
		return "Binding{" + "mFromType=" + mFromType + ", mToType=" + mToType + ", mProvider=" + mProvider + ", mEnclosingType=" + mEnclosingType + ", mName=" + mNamed + '}';
	}
}
