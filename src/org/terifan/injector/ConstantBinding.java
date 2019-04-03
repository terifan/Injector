package org.terifan.injector;

import java.lang.reflect.Field;


public class ConstantBinding extends Binding
{
	private float mValue;


	ConstantBinding(Injector aInjector)
	{
		super(aInjector);
	}


	public void to(float aValue)
	{
		mValue = aValue;
	}


	public ConstantBinding named(String aName)
	{
		mNamed = aName;
		return this;
	}


	public ConstantBinding in(Class aEnclosingType)
	{
		mEnclosingType = aEnclosingType;
		return this;
	}


	@Override
	Object getInstance(Context aContext)
	{
		return mValue;
	}


	void populate(Context aContext, Object aInstance, Field aField)
	{
		try
		{
			aField.setFloat(aInstance, mValue);
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
}
