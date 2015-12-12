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

package org.lirazs.gbackbone.reflection.client.impl;

import org.lirazs.gbackbone.reflection.client.AnnotationType;
import org.lirazs.gbackbone.reflection.client.ArrayType;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.EnumType;
import org.lirazs.gbackbone.reflection.client.ParameterizedType;
import org.lirazs.gbackbone.reflection.client.PrimitiveType;
import org.lirazs.gbackbone.reflection.client.Type;

public abstract class TypeImpl implements Type {
	public TypeImpl() {

	}

	public abstract String getJNISignature();

	public Type getLeafType() {
		return this;
	}

	public String getParameterizedQualifiedSourceName() {
		return getQualifiedSourceName();
	}

	public ArrayType isArray() {
		return null;
	}

	public abstract ClassType isClass();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.Type#isClassOrInterface()
	 */
	public ClassType isClassOrInterface() {
		ClassType type = isClass();
		if (type != null) {
			return type;
		}
		return isInterface();
	}

	public AnnotationType isAnnotation(){
		return null;
	}
	
	public abstract ClassType isInterface();

	// public abstract ParameterizedType isParameterized();

	public abstract PrimitiveType isPrimitive();
	
	public ParameterizedType isParameterized(){
		return null;
	}
	

	public EnumType isEnum() {
		return null;
	}

}