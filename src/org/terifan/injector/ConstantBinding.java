package org.terifan.injector;


public class ConstantBinding
{
	protected final Injector mInjector;


	ConstantBinding(Injector aInjector)
	{
		mInjector = aInjector;
	}


	public ConstantBindingNamed named(String aName)
	{
		return new ConstantBindingNamed(mInjector, aName);
	}
}
