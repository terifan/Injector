package org.terifan.injector;

import java.util.Objects;
import java.util.function.Supplier;


public class Binding
{
	private final Injector mInjector;
	private final Class mFromType;
	private Class mToType;
	private Provider mProvider;
	private Class mEnclosingType;
	private String mNamed;


	Binding(Injector aInjector, Class aFromType)
	{
		mInjector = aInjector;
		mFromType = aFromType;
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
		mNamed = aName;
		return this;
	}


	public Binding in(Class aEnclosingType)
	{
		mEnclosingType = aEnclosingType;
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
		return "Binding{" + "mInjector=" + mInjector + ", mFromType=" + mFromType + ", mToType=" + mToType + ", mProvider=" + mProvider + ", mEnclosingType=" + mEnclosingType + ", mName=" + mNamed + '}';
	}


	boolean matches(String aNamed, Class aEnclosingType)
	{
		return (mEnclosingType == null || mEnclosingType == aEnclosingType) && (((aNamed == null || aNamed.isEmpty()) && (mNamed == null || mNamed.isEmpty())) || (aNamed != null && aNamed.equals(mNamed)));
	}


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
}
