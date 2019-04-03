package org.terifan.injector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


interface Visitor
{
	default void visitClass(Context aContext, Object aInstance, Class aEnclosingType) throws Exception
	{
	}


	default void visitField(Context aContext, Object aInstance, Class aEnclosingType, Field aField) throws Exception
	{
	}


	default void visitMethod(Context aContext, Object aInstance, Class aEnclosingType, Method aMethod) throws Exception
	{
	}
}
