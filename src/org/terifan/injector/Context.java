package org.terifan.injector;

import java.util.ArrayList;



class Context
{
	Context mParent;
	Object mEnclosingInstance;


	public Context()
	{
	}


	private Context(Context aParent, Object aInstance)
	{
		mParent = aParent;
		mEnclosingInstance = aInstance;

		System.out.println(this);
	}


	public Context next(Object aInstance)
	{
		return new Context(this, aInstance);
	}


	@Override
	public String toString()
	{
		ArrayList<String> list = new ArrayList<>();
		Context ctx = this;
		while (ctx != null)
		{
			list.add(ctx.mEnclosingInstance == null ? null : ctx.mEnclosingInstance.toString());
			ctx = ctx.mParent;
		}
		return list.toString();
	}
}
