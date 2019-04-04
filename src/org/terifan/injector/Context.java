package org.terifan.injector;

import java.util.ArrayList;


class Context
{
	private Context mParent;
	private Class mEnclosingType;
	private Object mEnclosingInstance;


	public Context()
	{
	}


	public Context(Context aParent, Object aEnclosingInstance)
	{
		mParent = aParent;
		mEnclosingInstance = aEnclosingInstance;
		mEnclosingType = aEnclosingInstance.getClass();

		Context ctx = mParent;
		while (ctx != null && ctx.mEnclosingType != null)
		{
			if (ctx.mEnclosingType == mEnclosingType)
			{
				throw new InjectionException("Circular dependency detected: " + this);
			}

			ctx = ctx.mParent;
		}
	}


	public Object getEnclosingInstance()
	{
		return mEnclosingInstance;
	}


	@Override
	public String toString()
	{
		ArrayList<String> list = new ArrayList<>();
		Context ctx = this;
		while (ctx != null && ctx.mEnclosingType != null)
		{
			list.add(ctx.mEnclosingType.getSimpleName());
			ctx = ctx.mParent;
		}
		return list.toString();
	}
}
