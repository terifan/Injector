package org.terifan.injector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


interface Visitor
{
	default void visitClass(Context aContext, Object aInstance, Class aType) throws Exception
	{
	}


	default void visitField(Context aContext, Object aInstance, Class aType, Field aField) throws Exception
	{
	}


	default void visitMethod(Context aContext, Object aInstance, Class aType, Method aMethod) throws Exception
	{
	}
}
