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



public interface Type {

	public String getQualifiedSourceName();
	public String getParameterizedQualifiedSourceName();

	public String getSimpleSourceName();

	public ClassType isClass();

	public ClassType isClassOrInterface();

	public ClassType isInterface();
	
	public AnnotationType isAnnotation();
	
	public ArrayType isArray();

	public PrimitiveType isPrimitive();
	
	public ParameterizedType isParameterized();
	
	public EnumType isEnum();

}