package org.terifan.injector;

import java.awt.Color;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class InjectNGTest
{
	public InjectNGTest()
	{
	}


	@Test
	public void testBindSelf()
	{
		Injector injector = new Injector();

		injector.bind(Fruit.class);
		injector.bind(FruitProperty.class);
		injector.bind(String.class).named("value").toInstance("string-value");

		Fruit fruit1 = injector.getInstance(Fruit.class);
		Fruit fruit2 = injector.getInstance(Fruit.class);

		assertNotNull(fruit1);
		assertNotNull(fruit2);
		assertNotSame(fruit1, fruit2);
		assertTrue(fruit1 instanceof Fruit);
		assertTrue(fruit2 instanceof Fruit);

		assertEquals(fruit1.mFruitProperty1.mValue, "string-value");
		assertEquals(fruit1.mFruitProperty2.mValue, "string-value");
		assertEquals(fruit1.mFruitProperty3.mValue, "string-value");
		assertEquals(fruit2.mFruitProperty1.mValue, "string-value");
		assertEquals(fruit2.mFruitProperty2.mValue, "string-value");
		assertEquals(fruit2.mFruitProperty3.mValue, "string-value");
	}


	@Test
	public void testBind()
	{
		Injector injector = new Injector().setStrict(true);

		injector.bind(FruitProperty.class);
		injector.bind(AppleProperty.class);
		injector.bind(Fruit.class).to(Apple.class);

		Apple fruit1 = (Apple)injector.getInstance(Fruit.class);
		Apple fruit2 = (Apple)injector.getInstance(Fruit.class);

		assertNotNull(fruit1);
		assertNotNull(fruit2);
		assertNotSame(fruit1, fruit2);
		assertTrue(fruit1 instanceof Apple);
		assertTrue(fruit2 instanceof Apple);
	}


	@Test
	public void testBindToSingleton()
	{
		Injector injector = new Injector();

		injector.bind(Fruit.class).to(Apple.class).asSingleton();

		Fruit fruit1 = injector.getInstance(Fruit.class);
		Fruit fruit2 = injector.getInstance(Fruit.class);

		assertNotNull(fruit1);
		assertNotNull(fruit2);
		assertSame(fruit1, fruit2);
		assertTrue(fruit1 instanceof Apple);
	}


	@Test
	public void testBindToInInstance()
	{
		Injector injector = new Injector();

		injector.bind(Color.class).in(Apple.class).toInstance(Color.RED);
		injector.bind(Color.class).in(Fruit.class).toInstance(Color.GREEN);

		Fruit fruit1 = injector.getInstance(Fruit.class);
		Fruit fruit2 = injector.getInstance(Fruit.class);
		Apple apple1 = injector.getInstance(Apple.class);
		Apple apple2 = injector.getInstance(Apple.class);

		assertSame(fruit1.mFruitProperty4, Color.GREEN);
		assertSame(fruit2.mFruitProperty4, Color.GREEN);
		assertSame(apple1.mAppleProperty4, Color.RED);
		assertSame(apple2.mAppleProperty4, Color.RED);
	}


	@Test
	public void testBindInstance()
	{
		Apple expected = new Apple(null, null);

		Injector injector = new Injector();

		injector.bind(Fruit.class).toInstance(expected);

		Fruit fruit1 = injector.getInstance(Fruit.class);
		Fruit fruit2 = injector.getInstance(Fruit.class);

		assertSame(fruit1, expected);
		assertSame(fruit2, expected);
	}


	@Test
	public void testNamed()
	{
		Injector injector = new Injector();

		injector.bind(String.class).named("a").toInstance("A");
		injector.bind(String.class).named("b").toInstance("B");
		injector.bind(String.class).named("c").toInstance("C");
		injector.bind(String.class).named("d").toInstance("D");

		NamedSample instance = injector.getInstance(NamedSample.class);

		assertNotNull(instance);
		assertEquals(instance.a, "A");
		assertEquals(instance.b, "B");
		assertEquals(instance.c, "C");
		assertEquals(instance.d, "D");
	}


	@Test
	public void testBindProvider()
	{
		Injector injector = new Injector();

		injector.bind(Fruit.class).toProvider(() -> new Apple(null, null));

		Fruit fruit1 = injector.getInstance(Fruit.class);
		Fruit fruit2 = injector.getInstance(Fruit.class);

		assertNotNull(fruit1);
		assertNotNull(fruit2);
		assertNotSame(fruit1, fruit2);
		assertTrue(fruit1 instanceof Apple);
	}


	@Test
	public void testPostConstruct()
	{
		Injector injector = new Injector();

		injector.bind(PostConstructSample.class);

		PostConstructSample instance = injector.getInstance(PostConstructSample.class);

		assertTrue(instance.mPostConstructWasRun);
	}


	@Test
	public void testOptionalNamed()
	{
		Injector injector = new Injector();

		injector.bind(OptionalNamedSample.class);

		OptionalNamedSample instance = injector.getInstance(OptionalNamedSample.class);

		assertNull(instance.mValue);
	}


	@Test(expectedExceptions = InjectionException.class, expectedExceptionsMessageRegExp = "Type not bound.*")
	public void testMandatoryNamed()
	{
		Injector injector = new Injector();

		injector.bind(MandatoryNamedSample.class);

		injector.getInstance(MandatoryNamedSample.class);
	}


	@Test
	public void testOptionalSample()
	{
		Injector injector = new Injector();

		OptionalSample instance = injector.getInstance(OptionalSample.class);

		assertNull(instance.mFruit);
	}


	@Test
	public void testMandatorySample()
	{
		Injector injector = new Injector();

		MandatorySample instance = injector.getInstance(MandatorySample.class);

		assertNotNull(instance.mFruit);
	}


	@Test
	public void testConstructorSample()
	{
		Injector injector = new Injector();

		injector.bind(String.class).named("value").toInstance("test");

		ConstructorSample instance = injector.getInstance(ConstructorSample.class);

		assertNotNull(instance);
		assertNotNull(instance.mValue);
		assertNotNull(instance.mFruit);
		assertEquals(instance.mValue, "test");
	}


	@Test
	public void testSetterSample()
	{
		Fruit fruit = new Fruit(null);

		Injector injector = new Injector();

		injector.bind(Fruit.class).toInstance(fruit);

		SetterSample instance = injector.getInstance(SetterSample.class);

		assertNotNull(instance);
		assertSame(instance.mFruit, fruit);
	}


	@Test
	public void testInjectStaticClassInnerInstance()
	{
		Injector injector = new Injector();

		injector.bind(String.class).named("value").in(InjectStaticClassInnerInstanceSample.class).toInstance("OUTER");
		injector.bind(String.class).named("value").in(InjectStaticClassInnerInstanceSample.InnerScopeSample.class).toInstance("INNER");

		InjectStaticClassInnerInstanceSample instance = injector.getInstance(InjectStaticClassInnerInstanceSample.class);

		assertNotNull(instance);
		assertEquals(instance.mValue, "OUTER");
		assertEquals(instance.mInstance.mValue, "INNER");
	}


	@Test
	public void testInjectClassInnerInstanceSample()
	{
		Injector injector = new Injector();

		injector.bind(String.class).named("value").in(InjectClassInnerInstanceSample.class).toInstance("OUTER");
		injector.bind(String.class).named("value").in(InjectClassInnerInstanceSample.InnerScopeSample.class).toInstance("INNER");

		InjectClassInnerInstanceSample instance = injector.getInstance(InjectClassInnerInstanceSample.class);

		assertNotNull(instance);
		assertEquals(instance.mValue, "OUTER");
		assertEquals(instance.mInstance.mValue, "INNER");
	}


	@Test
	public void testInjectClassStaticInnerInstanceSample()
	{
		Injector injector = new Injector();

		injector.bind(String.class).named("value").in(InjectClassStaticInnerInstanceSample.class).toInstance("OUTER");
		injector.bind(String.class).named("value").in(InjectClassStaticInnerInstanceSample.InnerScopeSample.class).toInstance("INNER");

		InjectClassStaticInnerInstanceSample instance = injector.getInstance(InjectClassStaticInnerInstanceSample.class);

		assertNotNull(instance);
		assertEquals(instance.mValue, "OUTER");
		assertEquals(instance.mInstance.mValue, "INNER");
	}


	@Test
	public void testNamedParameters()
	{
		Injector injector = new Injector();

		injector.bind(String.class).named("name").in(NamedParametersSample.class).toInstance("dave");
		injector.bind(String.class).named("phone").in(NamedParametersSample.class).toInstance("123");

		NamedParametersSample instance = injector.getInstance(NamedParametersSample.class);

		assertNotNull(instance);
		assertEquals(instance.mName, "dave");
		assertEquals(instance.mPhone, "123");
	}


	@Test
	public void testSingletonAnnotation()
	{
		Injector injector = new Injector();

		injector.bind(SingletonAnnotationSample.class).to(SingletonAnnotationSample.class);

		SingletonAnnotationSample instance1 = injector.getInstance(SingletonAnnotationSample.class);
		SingletonAnnotationSample instance2 = injector.getInstance(SingletonAnnotationSample.class);

		assertNotNull(instance1);
		assertNotNull(instance2);
		assertSame(instance1, instance2);
	}


	@Test(expectedExceptions = InjectionException.class, expectedExceptionsMessageRegExp = "Circular dependency detected.*")
	public void testCircularError()
	{
		Injector injector = new Injector();

		CircularSample1 instance = injector.getInstance(CircularSample1.class);

		assertNotNull(instance);
	}


	@Test
	public void testToString()
	{
		Injector injector = new Injector();

		injector.bind(Fruit.class);

		assertNotNull(injector.toString());
	}


	@Test
	public void testProvider()
	{
		Injector injector = new Injector();

		ProviderSample instance1 = injector.getInstance(ProviderSample.class);
		ProviderSample instance2 = injector.getInstance(ProviderSample.class);

		assertNotNull(instance1);
		assertNotNull(instance1.mProvider);
		assertNotNull(instance1.mProvider.get());

		assertNotNull(instance2);
		assertNotNull(instance2.mProvider);
		assertNotNull(instance2.mProvider.get());

		assertNotSame(instance1.mProvider.get(), instance1.mProvider.get());
	}


	@Test
	public void testProviderSingleton()
	{
		Injector injector = new Injector();

		ProviderSingletonSample instance1 = injector.getInstance(ProviderSingletonSample.class);
		ProviderSingletonSample instance2 = injector.getInstance(ProviderSingletonSample.class);

		assertNotNull(instance1);
		assertNotNull(instance1.mProvider);
		assertNotNull(instance1.mProvider.get());

		assertNotNull(instance2);
		assertNotNull(instance2.mProvider);
		assertNotNull(instance2.mProvider.get());

		assertSame(instance1.mProvider.get(), instance1.mProvider.get());
	}


	@Test
	public void testConstantProvider()
	{
		Injector injector = new Injector();
		injector.bindConstant().named("x").to(1);
		injector.bindConstant().named("y").to(2.0);
		injector.bindConstant().named("z").in(ConstantProviderSample.class).to(3);
		injector.bindConstant().named("z").in(ConstantProviderSample.InnerClass.class).to(-3);

		ConstantProviderSample instance = injector.getInstance(ConstantProviderSample.class);

		assertNotNull(instance);
		assertEquals(instance.mX, 1);
		assertEquals(instance.mY, 2.0);
		assertEquals(instance.mZ, 3);
		assertEquals(instance.mInnerClass.mZ, -3);
	}


	@Test
	public void testMethodArgumentProvider()
	{
		Injector injector = new Injector();

		MethodArgumentProviderSample instance = injector.getInstance(MethodArgumentProviderSample.class);

		assertNotNull(instance);
		assertTrue(instance.mStrings instanceof Provider);
		assertTrue(instance.mStrings.get() instanceof String);
	}


	@Test
	public void testMethodArgumentCollection()
	{
		List<String> listStrings = Arrays.asList("test");
		List<Date> listDates = Arrays.asList(new Date());

		Injector injector = new Injector();

		injector.bind(StringListProvider.class);

//		injector.bind(new TypeLiteral<List<String>>(){}).toProvider(()->Arrays.asList("test"));

		injector.bind(List.class).toInstance(listStrings);
		injector.bind(List.class).toInstance(listDates);

		MethodArgumentCollectionSample instance = injector.getInstance(MethodArgumentCollectionSample.class);

		assertNotNull(instance);
		assertTrue(instance.mStrings instanceof List);
		assertSame(instance.mStrings, listStrings);
		assertTrue(instance.mDates instanceof List);
		assertSame(instance.mDates, listDates);
	}

	static class StringListProvider
	{
		//@Provides
		public List<String> get()
		{
			return null;
		}
	}


	static class Fruit
	{
		@Inject FruitProperty mFruitProperty1;
		FruitProperty mFruitProperty2;
		FruitProperty mFruitProperty3;
		@Inject(optional = true) Color mFruitProperty4;


		@Inject
		public Fruit(FruitProperty aFruitProperty2)
		{
			mFruitProperty2 = aFruitProperty2;
		}


		@Inject
		public void initFruit(FruitProperty aFruitProperty3)
		{
			mFruitProperty3 = aFruitProperty3;
		}
	}


	static class Apple extends Fruit
	{
		@Inject AppleProperty mAppleProperty1;
		AppleProperty mAppleProperty2;
		AppleProperty mAppleProperty3;
		@Inject(optional = true) Color mAppleProperty4;


		@Inject
		public Apple(AppleProperty aAppleProperty2, FruitProperty aFruitProperty2)
		{
			super(aFruitProperty2);

			mAppleProperty2 = aAppleProperty2;
		}


		@Inject
		public void initApple(AppleProperty aAppleProperty3)
		{
			mAppleProperty3 = aAppleProperty3;
		}
	}


	static class FruitProperty
	{
		@Inject(optional = true) @Named("value") String mValue;
	}


	static class AppleProperty
	{
	}


	static class PostConstructSample
	{
		boolean mPostConstructWasRun;


		@PostConstruct
		public void init()
		{
			mPostConstructWasRun = true;
		}
	}


	static class NamedSample
	{
		@Inject @Named("a") String a;
		@Inject @Named("b") String b;
		String c;
		String d;

		@Inject
		void method1(@Named("c") String c)
		{
			this.c = c;
		}

		@Inject @Named("d")
		void method2(String d)
		{
			this.d = d;
		}
	}


	static class OptionalSample
	{
		@Inject(optional = true) Fruit mFruit;
	}


	static class MandatorySample
	{
		@Inject Fruit mFruit;
	}


	static class OptionalNamedSample
	{
		@Inject(optional = true) @Named("value") String mValue;
	}


	static class MandatoryNamedSample
	{
		@Inject @Named("value") String mValue;
	}


	static class ConstructorSample
	{
		String mValue;
		Fruit mFruit;


		@Inject
		public ConstructorSample(@Named("value") String aValue, Fruit aFruit)
		{
			mValue = aValue;
			mFruit = aFruit;
		}
	}


	static class SetterSample
	{
		Fruit mFruit;


		@Inject
		public void setFruit(Fruit aFruit)
		{
			mFruit = aFruit;
		}
	}


	static class InjectStaticClassInnerInstanceSample
	{
		@Inject @Named("value") String mValue;
		@Inject InnerScopeSample mInstance;


		class InnerScopeSample
		{
			@Inject @Named("value") String mValue;
		}
	}


	class InjectClassInnerInstanceSample
	{
		@Inject @Named("value") String mValue;
		@Inject InnerScopeSample mInstance;


		class InnerScopeSample
		{
			@Inject @Named("value") String mValue;
		}
	}


	class NamedParametersSample
	{
		String mName;
		String mPhone;


		@Inject
		void callMe(@Named("name") String aName, @Named("phone") String aPhone)
		{
			mName = aName;
			mPhone = aPhone;
		}
	}


	@Singleton
	class SingletonAnnotationSample
	{
	}


	static class CircularSample1
	{
		@Inject CircularSample2 mInstance;
	}


	static class CircularSample2
	{
		@Inject CircularSample3 mInstance;
	}


	static class CircularSample3
	{
		@Inject CircularSample1 mInstance;
	}


	static class ProviderSample
	{
		@Inject Provider<Fruit> mProvider;
	}


	static class ProviderSingletonSample
	{
		@Inject Provider<SingletonAnnotationSample> mProvider;
	}


	static class ConstantProviderSample
	{
		@Inject @Named("x") int mX;
		@Inject @Named("y") double mY;
		@Inject @Named("z") int mZ;
		@Inject InnerClass mInnerClass;

		class InnerClass
		{
			@Inject @Named("z") int mZ;
		}
	}


	static class MethodArgumentProviderSample
	{
		Provider<String> mStrings;

		@Inject
		void x(Provider<String> aStrings)
		{
			mStrings = aStrings;
		}
	}


	static class MethodArgumentCollectionSample
	{
		List<String> mStrings;
		List<Date> mDates;

		@Inject
		void x(List<String> aStrings, List<Date> aDates)
		{
			mStrings = aStrings;
			mDates = aDates;
		}
	}
}


class InjectClassStaticInnerInstanceSample
{
	@Inject @Named("value") String mValue;
	@Inject InnerScopeSample mInstance;


	static class InnerScopeSample
	{
		@Inject @Named("value") String mValue;
	}
}
