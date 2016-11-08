package dmonner.xlbp.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionTools
{
	public static Class<?> box(final Class<?> clazz)
	{
		if(clazz == int.class)
			return Integer.class;
		if(clazz == short.class)
			return Short.class;
		if(clazz == byte.class)
			return Byte.class;
		if(clazz == long.class)
			return Long.class;
		if(clazz == float.class)
			return Float.class;
		if(clazz == double.class)
			return Double.class;
		if(clazz == boolean.class)
			return Boolean.class;
		if(clazz == char.class)
			return Character.class;
		return clazz;
	}

	public static Class<?> findClass(final String classname, final String[] packages)
	{
		// find it directly if a package is already specified in classname
		try
		{
			return Class.forName(classname);
		}
		catch(final ClassNotFoundException ex)
		{
			// Do nothing and try the next package in the path.
		}

		for(final String pkg : packages)
		{
			try
			{
				return Class.forName(pkg + "." + classname);
			}
			catch(final ClassNotFoundException ex)
			{
				// Do nothing and try the next package in the path.
			}
		}

		throw new IllegalArgumentException("Class name not found: " + classname);
	}

	public static Constructor<?> findConstructor(final Class<?> clazz, final Class<?>[] signature)
	{
		for(final Constructor<?> c : clazz.getConstructors())
		{
			final Class<?>[] csign = c.getParameterTypes();

			if(csign.length == signature.length)
			{
				boolean found = true;
				for(int i = 0; i < csign.length; i++)
				{
					if(!csign[i].isAssignableFrom(signature[i]))
					{
						found = false;
						break;
					}
				}

				if(found)
					return c;
			}
		}

		throw new IllegalArgumentException("Cannot find constructor for " + clazz.getCanonicalName()
				+ " matching args " + Arrays.deepToString(signature));
	}

	public static Method findMethod(final Class<?> clazz, final String name,
			final Class<?>[] signature)
	{
		for(final Method m : clazz.getMethods())
		{
			final Class<?>[] msign = m.getParameterTypes();

			if(m.getName().equals(name) && msign.length == signature.length)
			{
				boolean found = true;
				for(int i = 0; i < msign.length; i++)
				{
					if(!msign[i].isAssignableFrom(signature[i]))
					{
						found = false;
						break;
					}
				}

				if(found)
					return m;
			}
		}

		throw new IllegalArgumentException("Cannot find method " + name + " for "
				+ clazz.getCanonicalName() + " matching args " + Arrays.deepToString(signature));
	}

	public static Class<?> unbox(final Class<?> clazz)
	{
		if(clazz == Integer.class)
			return int.class;
		if(clazz == Short.class)
			return short.class;
		if(clazz == Byte.class)
			return byte.class;
		if(clazz == Long.class)
			return long.class;
		if(clazz == Float.class)
			return float.class;
		if(clazz == Double.class)
			return double.class;
		if(clazz == Boolean.class)
			return boolean.class;
		if(clazz == Character.class)
			return char.class;
		return clazz;
	}

}
