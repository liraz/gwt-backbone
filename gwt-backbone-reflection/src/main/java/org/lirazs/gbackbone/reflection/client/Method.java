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

public interface Method extends AbstractMethod, AccessDef, Member{

	/**
	 * Invoke the method.
	 * <P>if invoke a static class, instance must be null.
	 * @param instance
	 * @param args
	 * @return
	 * @throws MethodInvokeException
	 */
	public Object invoke(Object instance, Object... args) throws MethodInvokeException;

	/**
	 * Returns a <code>Type</code> object that represents the formal return type
     of the method represented by this <code>Method</code> object.
     
	 * @return the return type for the method this object represents
	 * 
	 * @throws ReflectionRequiredException if there is no reflection information of {@link getReturnTypeName()}
	 */
	public Type getReturnType() throws ReflectionRequiredException;
	
	public String getReturnTypeName();

	public Class getReturnTypeClass();

	
	/**
   * Returns the default value for the annotation member represented by
   * this <tt>Method</tt> instance.  If the member is of a primitive type,
   * an instance of the corresponding wrapper type is returned. Returns
   * null if no default is associated with the member, or if the method
   * instance does not represent a declared member of an annotation type.
   *
   * @return the default value for the annotation member represented
   *     by this <tt>Method</tt> instance.
   * @throws TypeNotPresentException if the annotation is of type
   *     {@link Class} and no definition can be found for the
   *     default class value.
   * @since  RC2
   */
  public Object getDefaultValue();

}