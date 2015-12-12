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

import com.google.gwt.core.client.GWT;

public interface TypeOracle {
	public static TypeOracle Instance = (TypeOracle) GWT.create(TypeOracle.class);

	/**
   * Parses the string form of a type to produce the corresponding type object.
   * The types that can be parsed include primitives, class and interface names,
   * simple parameterized types (those without wildcards or bounds), and arrays
   * of the preceding.
   * <p>
   * Examples of types that can be parsed by this method.
   * <ul>
   * <li><code>int</code></li>
   * <li><code>java.lang.Object</code></li>
   * <li><code>java.lang.String[]</code></li>
   * <li><code>char[][]</code></li>
   * <li><code>void</code></li>
   * <li><code>List&lt;Shape&gt;</code> <b>This will only return List class</b> </li> 
   * <li><code>List&lt;List&lt;Shape&gt;&gt;</code> <b>This will only return List class</b></li>
   * </ul>
   * </p>
   * 
   * @param type a type signature to be parsed
   * @return the type object corresponding to the parse type
   * @throws ReflectionRequiredException If not reflection information available for clazz, will throw ReflectionRequiredException exception
   */
	public Type getType(String name) throws ReflectionRequiredException;

	/**
	 * 
	 * @param name
	 * @return
	 * @throws ReflectionRequiredException If not reflection information available for clazz, will throw ReflectionRequiredException exception
	 */
	public ClassType getClassType(String name) throws ReflectionRequiredException;

	/**
	 * Get Class type, this supported:
	 * <p>
	 * <ul>
	 * <li>Class</li>
	 * <li>Enum</li>
	 * <li>Array</li>
	 * </ul>
	 * @param clazz
	 * @return 
	 * @throws ReflectionRequiredException If not reflection information available for clazz, will throw ReflectionRequiredException exception
	 */
	public <T> ClassType<T> getClassType(Class<T> clazz) throws ReflectionRequiredException;

	/**
	 * Gets the type object that represents an array of the specified type.
	 * 
	 * @param componentType
	 *          the component type of the array, which can itself be an array type
	 * @return a type object representing an array of the component type
	 * 
	 * @since RC2
	 */
	public ArrayType getArrayType(Type componentType);

	/**
	 * Gets a reference to the type object representing
	 * <code>java.lang.Object</code>.
	 * 
	 * @since RC2
	 */
	public ClassType getJavaLangObject();
}