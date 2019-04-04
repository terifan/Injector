package org.terifan.injector;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class ConstantBindingNamedTo extends Binding
{
	private Object mValue;


	ConstantBindingNamedTo(Injector aInjector, String aNamed, Class aEnclosingType)
	{
		super(aInjector);

		mNamed = aNamed;
		mEnclosingType = aEnclosingType;
	}


	public void to(boolean aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(byte aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(short aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(char aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(int aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(long aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(float aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(double aValue)
	{
		mValue = aValue;
		bind();
	}


	public void to(String aValue)
	{
		mValue = aValue;
		bind();
	}


	@Override
	Object getInstance(Context aContext)
	{
		return mValue;
	}


	@Override
	void populate(Context aContext, Object aInstance, Field aField)
	{
		try
		{
			aField.set(aInstance, mValue);
		}
		catch (Exception | Error e)
		{
			throw new InjectionException(e);
		}
	}


	@Override
	String describe()
	{
		return mNamed + " = (float)" + mValue;
	}


	private void bind()
	{
		mInjector.mBindings.computeIfAbsent(ConstantBinding.class, e -> new ArrayList<>()).add(this);
	}
}
