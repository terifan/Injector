package org.terifan.injector;


class Scope
{
	private String mName;
	private Class mType;
	private boolean mOptional;


	public Scope()
	{
	}


	public Scope(String aName, Class aType, boolean aOptional)
	{
		setName(aName);
		mType = aType;
		mOptional = aOptional;
	}


	public String getName()
	{
		return mName;
	}


	public void setName(String aName)
	{
		mName = aName == null || aName.isEmpty() ? null : aName;
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
		return "{" + "mName=" + mName + ", mType=" + mType + ", mOptional=" + mOptional + '}';
	}
}
