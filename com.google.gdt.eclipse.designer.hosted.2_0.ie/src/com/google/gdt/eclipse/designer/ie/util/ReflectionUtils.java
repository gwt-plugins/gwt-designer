/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.ie.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

/**
 * Contains different Java reflection utilities.
 * 
 * @author scheglov_ke
 * @coverage core.util
 */
public class ReflectionUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private ReflectionUtils() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Signature
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param clazz
	 *            the class
	 * @param runtime
	 *            flag <code>true</code> if we need name for class loading, <code>false</code> if we need name
	 *            for source generation.
	 * 
	 * @return the fully qualified name of given {@link Class}
	 */
	public static String getFullyQualifiedName(Class<?> clazz, boolean runtime) {
		Assert.isNotNull(clazz);
		if (runtime) {
			// primitive
			if (clazz.isPrimitive()) {
				if (clazz == void.class) {
					return "V";
				} else if (clazz == boolean.class) {
					return "Z";
				} else if (clazz == byte.class) {
					return "B";
				} else if (clazz == char.class) {
					return "C";
				} else if (clazz == short.class) {
					return "S";
				} else if (clazz == int.class) {
					return "I";
				} else if (clazz == long.class) {
					return "J";
				} else if (clazz == float.class) {
					return "F";
				} else if (clazz == double.class) {
					return "D";
				}
			}
			// array
			if (clazz.isArray()) {
				return "[" + getFullyQualifiedName(clazz.getComponentType(), runtime);
			}
			// object
			return "L" + clazz.getName() + ";";
		} else {
			// primitive
			if (clazz.isPrimitive()) {
				return clazz.getName();
			}
			// array
			if (clazz.isArray()) {
				return getFullyQualifiedName(clazz.getComponentType(), runtime) + "[]";
			}
			// object
			return clazz.getName().replace('/', '.').replace('$', '.');
		}
	}
	/**
	 * Appends fully qualified names of given parameter types (appends also <code>"()"</code>).
	 */
	private static void appendParameterTypes(StringBuilder buffer, Class<?>[] parameterTypes) {
		buffer.append('(');
		boolean firstParameter = true;
		for (Class<?> parameterType : parameterTypes) {
			if (firstParameter) {
				firstParameter = false;
			} else {
				buffer.append(',');
			}
			buffer.append(getFullyQualifiedName(parameterType, false));
		}
		buffer.append(')');
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Method
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return signature for given {@link Method}. This signature is not same signature as in JVM or JDT, just
	 *         some string that unique identifies method in its {@link Class}.
	 */
	public static String getMethodSignature(Method method) {
		Assert.isNotNull(method);
		return getMethodSignature(method.getName(), method.getParameterTypes());
	}
	/**
	 * Returns the signature of {@link Method} with given combination of name and parameter types. This
	 * signature is not same signature as in JVM or JDT, just some string that unique identifies method in its
	 * {@link Class}.
	 * 
	 * @param name
	 *            the name of {@link Method}.
	 * @param parameterTypes
	 *            the types of {@link Method} parameters.
	 * 
	 * @return signature of {@link Method}.
	 */
	private static String getMethodSignature(String name, Class<?>... parameterTypes) {
		Assert.isNotNull(name);
		Assert.isNotNull(parameterTypes);
		//
		StringBuilder buffer = new StringBuilder();
		buffer.append(name);
		appendParameterTypes(buffer, parameterTypes);
		return buffer.toString();
	}
	/**
	 * Returns the {@link Method} defined in {@link Class}. This method can have any visibility, i.e. we can
	 * find even protected/private methods. Can return <code>null</code> if no method with given signature
	 * found.
	 * 
	 * @param clazz
	 *            the {@link Class} to get method from it, or its superclass.
	 * @param signature
	 *            the signature of method in same format as {@link #getMethodSignature(Method)}.
	 * 
	 * @return the {@link Method} for given signature, or <code>null</code> if no such method found.
	 */
	public static Method getMethodBySignature(Class<?> clazz, String signature) {
		Assert.isNotNull(clazz);
		Assert.isNotNull(signature);
		// extract name of method to perform fast check only by name, full signature requires too much time
		String signatureName = StringUtils.substringBefore(signature, "(");
		// check methods of given class and its super classes
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			Method method = getMethodBySignature0(c, signatureName, signature);
			if (method != null) {
				return method;
			}
		}
		// check methods of implemented interfaces
		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			Method method = getMethodBySignature0(interfaceClass, signatureName, signature);
			if (method != null) {
				return method;
			}
		}
		// not found
		return null;
	}
	/**
	 * Returns the {@link Method} defined in {@link Class} (exactly).
	 * 
	 * @see #getMethodBySignature(Class, String).
	 */
	private static Method getMethodBySignature0(Class<?> clazz, String signatureName, String signature) {
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(signatureName) && getMethodSignature(method).equals(signature)) {
				method.setAccessible(true);
				return method;
			}
		}
		// not found
		return null;
	}
	/**
	 * @return the {@link Object} result of invoking method with given signature.
	 */
	public static Object invokeMethod(Object object, String signature, Object... arguments) throws Exception {
		Assert.isNotNull(object);
		Assert.isNotNull(arguments);
		// prepare class/object
		Class<?> refClass = getRefClass(object);
		Object refObject = getRefObject(object);
		// prepare method
		Method method = getMethodBySignature(refClass, signature);
		Assert.isNotNull(method, "Can not find method " + signature + " in " + refClass);
		// do invoke
		try {
			return method.invoke(refObject, arguments);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			}
			throw e;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return signature for given {@link Constructor}. This signature is not same signature as in JVM or JDT,
	 *         just some string that unique identifies constructor in its {@link Class}.
	 */
	public static String getConstructorSignature(Constructor<?> constructor) throws Exception {
		Assert.isNotNull(constructor);
		return getConstructorSignature(constructor.getParameterTypes());
	}
	/**
	 * Returns the signature of {@link Constructor} with given parameter types. This signature is not same
	 * signature as in JVM or JDT, just some string that unique identifies constructor in its {@link Class}.
	 * 
	 * @param parameterTypes
	 *            the types of {@link Constructor} parameters.
	 * 
	 * @return signature of {@link Constructor}.
	 */
	public static String getConstructorSignature(Class<?>... parameterTypes) throws Exception {
		Assert.isNotNull(parameterTypes);
		//
		StringBuilder buffer = new StringBuilder();
		buffer.append("<init>");
		appendParameterTypes(buffer, parameterTypes);
		return buffer.toString();
	}
	/**
	 * @param signature
	 *            the signature of method in same format as {@link #getConstructorSignature(Constructor)}.
	 * 
	 * @return the {@link Constructor} for given signature. This constructor can have any visibility, i.e. we
	 *         can find even protected/private constructors.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getConstructorBySignature(Class<T> clazz, String signature)
			throws Exception {
		Assert.isNotNull(clazz);
		Assert.isNotNull(signature);
		// check all declared constructors
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (getConstructorSignature(constructor).equals(signature)) {
				constructor.setAccessible(true);
				return (Constructor<T>) constructor;
			}
		}
		// not found
		return null;
	}
	/**
	 * @param parameterTypes
	 *            the array of parameter types.
	 * 
	 * @return the {@link Constructor} for given signature. This constructor can have any visibility, i.e. we
	 *         can find even protected/private constructors.
	 */
	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes)
			throws Exception {
		Assert.isNotNull(clazz);
		Assert.isNotNull(parameterTypes);
		String signature = getConstructorSignature(parameterTypes);
		return getConstructorBySignature(clazz, signature);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Field
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Field} of given class with given name or <code>null</code> if no such {@link Field}
	 *         found.
	 */
	public static Field getFieldByName(Class<?> clazz, String name) {
		Assert.isNotNull(clazz);
		Assert.isNotNull(name);
		// check fields of given class and its super classes
		while (clazz != null) {
			// check all declared field
			Field[] declaredFields = clazz.getDeclaredFields();
			for (Field field : declaredFields) {
				if (field.getName().equals(name)) {
					field.setAccessible(true);
					return field;
				}
			}
			// check interfaces
			{
				Class<?>[] interfaceClasses = clazz.getInterfaces();
				for (Class<?> interfaceClass : interfaceClasses) {
					Field field = getFieldByName(interfaceClass, name);
					if (field != null) {
						return field;
					}
				}
			}
			// check superclass
			clazz = clazz.getSuperclass();
		}
		// not found
		return null;
	}
	/**
	 * @return the {@link Object} value of field with given name.
	 */
	public static Object getFieldObject(final Object object, final String name) throws Exception {
		Class<?> refClass = getRefClass(object);
		Object refObject = getRefObject(object);
		Field field = getFieldByName(refClass, name);
		if (field == null) {
			throw new IllegalArgumentException("Unable to find '" + name + "' in " + refClass);
		}
		return field.get(refObject);
	}
	/**
	 * Sets {@link Object} value of field with given name.
	 */
	public static void setField(Object object, String name, Object value) throws Exception {
		Class<?> refClass = getRefClass(object);
		Object refObject = getRefObject(object);
		getFieldByName(refClass, name).set(refObject, value);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Class} of given {@link Object} or casted object, if it is {@link Class} itself.
	 */
	private static Class<?> getRefClass(Object object) {
		return object instanceof Class ? (Class<?>) object : object.getClass();
	}
	/**
	 * @return the {@link Object} that should be used as argument for {@link Field#get(Object)} and
	 *         {@link Method#invoke(Object, Object[])}.
	 */
	private static Object getRefObject(Object object) {
		return object instanceof Class ? null : object;
	}
}
