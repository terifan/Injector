package org.terifan.injector;


public class ConstantBindingNamed extends ConstantBindingNamedTo
{
	ConstantBindingNamed(Injector aInjector, String aNamed)
	{
		super(aInjector, aNamed, null);
	}


	public ConstantBindingNamedTo in(Class aEnclosingType)
	{
		return new ConstantBindingNamedTo(mInjector, mNamed, aEnclosingType);
	}
}
