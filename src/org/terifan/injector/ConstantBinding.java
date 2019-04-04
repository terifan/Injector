package org.terifan.injector;

import java.lang.reflect.Field;


public class ConstantBinding extends Binding
{
	private Object mValue;


	ConstantBinding(Injector aInjector)
	{
		super(aInjector);
	}


	public void to(boolean aValue)
	{
		mValue = aValue;
	}


	public void to(byte aValue)
	{
		mValue = aValue;
	}


	public void to(short aValue)
	{
		mValue = aValue;
	}


	public void to(char aValue)
	{
		mValue = aValue;
	}


	public void to(int aValue)
	{
		mValue = aValue;
	}


	public void to(long aValue)
	{
		mValue = aValue;
	}


	public void to(float aValue)
	{
		mValue = aValue;
	}


	public void to(double aValue)
	{
		mValue = aValue;
	}


	public void to(String aValue)
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
}
