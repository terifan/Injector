package org.terifan.injector;

import java.lang.reflect.Field;



public abstract class Binding
{
	protected final Injector mInjector;
	protected Class mEnclosingType;
	protected String mNamed;


	public Binding(Injector aInjector)
	{
		this.mInjector = aInjector;
	}


	abstract void populate(Context aContext, Object aInstance, Field aField);


	abstract Object getInstance(Context aContext);


	abstract String describe();


	boolean matches(String aNamed, Class aEnclosingType)
	{
		return (mEnclosingType == null || mEnclosingType == aEnclosingType) && (((aNamed == null || aNamed.isEmpty()) && (mNamed == null || mNamed.isEmpty())) || (aNamed != null && aNamed.equals(mNamed)));
	}
}
