package org.terifan.injector;


class Scope
{
	private String mNamed;
	private Class mType;
	private boolean mOptional;


	public Scope()
	{
	}


	public Scope(String aName, Class aType, boolean aOptional)
	{
		setNamed(aName);
		mType = aType;
		mOptional = aOptional;
	}


	public String getNamed()
	{
		return mNamed;
	}


	public void setNamed(String aNamed)
	{
		mNamed = aNamed == null || aNamed.isEmpty() ? null : aNamed;
	}


	public Class getType()
	{
		return mType;
	}


	public void setType(Class aType)
	{
		mType = aType;
	}


	public boolean isOptional()
	{
		return mOptional;
	}


	public void setOptional(boolean aOptional)
	{
		mOptional = aOptional;
	}


	@Override
	public String toString()
	{
		return "{" + "mNamed=" + mNamed + ", mType=" + mType + ", mOptional=" + mOptional + '}';
	}
}
