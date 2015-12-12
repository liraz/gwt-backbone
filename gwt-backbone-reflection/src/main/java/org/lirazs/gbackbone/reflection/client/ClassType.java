/*******************************************************************************
 *  Copyright 2001, 2007 JamesLuo(JamesLuo.au@gmail.com)
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 * 
 *  Contributors:
 *******************************************************************************/


package org.lirazs.gbackbone.reflection.client;


/**
 * 
 * {@code private interface ClassTypeOfA extends ClassType<ClassA>{}
 * }
 * <p>
 * All reflection settings will be search from ClassA until find a {@link Reflectable} annotation.
 * This {@link Reflectable} will be the settings, if no {@link Reflectable} found, will using the default one which equals "@Reflectable()".
 * 
 * @author JamesLuo.au@gmail.com
 *
 * @param <T> T is the class you want generate reflection information
 */
public interface ClassType<T> extends HasAnnotations, Type {

	/**
	 * Find Field
	 * if not found in current class
	 * will try find it in parent class
	 * @param name
	 * @return
	 */
	public Field findField(String name);

	/**
	 * Find method
	 * If not found in current class
	 * will try to find it in parent class
	 * 
	 * @param name
	 * @param paramTypes
	 * @return
	 */
	public Method findMethod(String name, Class<?>... paramTypes);
	
	/**
	 * Find method
	 * If not found in current class
	 * will try to find it in parent class
	 * 
	 * @param name
	 * @param paramTypes
	 * @return
	 */
	public Method findMethod(String name, Type[] paramTypes);

	//public Method findMethod(String name, String[] paramTypes);
	
	/**
	 * Find method
	 * If not found in current class
	 * will try to find it in parent class
	 */
	public Method findMethod(String name, String[] paramTypes);
	
	/**
	 * For now this function just return if the class can be constructed using a simple <code>new</code>
   * operation. Specifically, the class must
   * <ul>
   * <li>be a class rather than an interface, </li>
   * <li>have either no constructors or a parameterless constructor, and</li>
   * <li>be a top-level class or a static nested class.</li>
   * </ul>
   * 
	 * @param paramTypes
	 * @return
	 */
	public Constructor<T> findConstructor(String... paramTypes);

	/**
	 * Get field
	 * @param name the field name to find
	 * @return the field, if not found, return null
	 */
	public Field getField(String name);

	/**
	 * The Fields in current Class
	 * @return
	 */
	public Field[] getFields();

	public ClassType<?>[] getImplementedInterfaces() throws ReflectionRequiredException;

	/**
	 * get method
	 * @param name the method name
	 * @param paramTypes the Type of parameters, can be null.
	 * @return if not found, return null
	 */
	public Method getMethod(String name, Type[] paramTypes);
	
	//public List getMetaDataMerge(String tagName);

	/*
	 * Returns the declared methods of this class (not include superclasses or
	 * superinterfaces).
	 */
	public Method[] getMethods();

	public String getName();

	//For now not support packagej
//	public Package getPackage();

	public ClassType<? super T> getSuperclass() throws ReflectionRequiredException;
	public Class<T> getDeclaringClass();
	
	public Object invoke(Object instance, String methodName, Object... args) throws MethodInvokeException;
	
	/**
	 * get field value
	 * @param fieldName
	 * @return
	 */
	//sxf add
	public Object getFieldValue(Object instance, String fieldName) throws FieldIllegalAccessException;
	/**
	 * set field value
	 * @param fieldName
	 * @param value
	 * @return
	 */
	//sxf add
	public void setFieldValue(Object instance, String fieldName, Object value)throws FieldIllegalAccessException;

}