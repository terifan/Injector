package org.terifan.injector;

import java.lang.reflect.Field;
import java.util.function.Supplier;


public class ProviderBinding extends Binding
{
	private final Class mFromType;
	private Class mToType;
	private Provider mProvider;


	ProviderBinding(Injector aInjector, Class aFromType)
	{
		super(aInjector);

		mFromType = aFromType;
		mToType = aFromType;
		mProvider = new Provider(mInjector, mFromType, false);
	}


	public void asSingleton()
	{
		mProvider = new Provider(mInjector, mToType, true);
	}


	public void toInstance(Object aInstance)
	{
		mProvider = new Provider(mInjector, aInstance, false);
	}


	public void toProvider(Supplier aSupplier)
	{
		mProvider = new Provider(mInjector, null, true)
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
		mToType = aToType;
		mProvider = new Provider(mInjector, aToType, false);

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
	void populate(Context aContext, Object aInstance, Field aField)
	{
		try
		{
			aField.set(aInstance, getInstance(aContext));
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


	@Override
	Object getInstance(Context aContext)
	{
		return mProvider.get(aContext);
	}


	@Override
	String describe()
	{
		if (mFromType == mToType)
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
		return "ProviderBinding{" + "mFromType=" + mFromType + ", mToType=" + mToType + ", mEnclosingType=" + mEnclosingType + ", mNamed=" + mNamed + '}';
	}
}
