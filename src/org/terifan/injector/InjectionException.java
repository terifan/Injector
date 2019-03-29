package org.terifan.injector;

import java.io.Serializable;


public class InjectionException extends RuntimeException implements Serializable
{
	private static final long serialVersionUID = 1L;


	public InjectionException(String aMessage)
	{
		super(aMessage);
	}


	public InjectionException(Throwable aCause)
	{
		super(aCause);
	}


	public InjectionException(String aMessage, Throwable aCause)
	{
		super(aMessage, aCause);
	}
}
