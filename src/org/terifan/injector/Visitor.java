package org.terifan.injector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


interface Visitor
{
	default void visitClass(Object aInstance, Class aType) throws Exception
	{
	}


	default void visitField(Object aInstance, Class aType, Field aField) throws Exception
	{
	}


	default void visitMethod(Object aInstance, Class aType, Method aMethod) throws Exception
	{
	}
}
