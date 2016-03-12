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

import java.lang.annotation.Annotation;
import java.util.List;

import org.lirazs.gbackbone.reflection.client.*;

public class ParameterImpl implements HasAnnotations, Parameter {
	private final String name;
	
	private final Annotations annotations = new Annotations();

	private Type type;

	private String typeName;
	private Class typeClass;

	//TODO: why only Method interface?.. it can be a constructor as well..
	//private final Method enclosingMethod;
	private final AbstractMethod enclosingMethod;


	public ParameterImpl(AbstractMethod enclosingMethod, String typeName, Class typeClass, String name) {
		this.enclosingMethod = enclosingMethod;
		this.typeName = typeName;
		this.typeClass = typeClass;
		this.name = name;

		enclosingMethod.addParameter(this);
	}

	public ParameterImpl(AbstractMethod enclosingMethod, TypeImpl type, Class typeClass, String name) {
		this(enclosingMethod, getTypeName(type), typeClass, name);

		this.typeClass = typeClass;
		this.type = type;
	}
	
	public static String getTypeName(Type type){
		if (type != null)
			return type.getQualifiedSourceName();
		else
			return "";
	}


	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.Parameter#getEnclosingMethod()
	 */
	public AbstractMethod getEnclosingMethod() {
		return enclosingMethod;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.Parameter#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.Parameter#getType()
	 */
	public Type getType() throws ReflectionRequiredException {
		if (type == null)
			type = TypeOracle.Instance.getType(typeName);
		
		return type;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.Parameter#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type.getQualifiedSourceName());
		sb.append(" ");
		sb.append(getName());
		return sb.toString();
	}

	// Called when parameter types are found to be parameterized
	void setType(TypeImpl type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.Parameter#getTypeName()
	 */
	public String getTypeName() {
		return typeName;
	}

	/* (non-Javadoc)
         * @see org.lirazs.gbackbone.client.reflection.Parameter#setTypeName(java.lang.String)
         */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public Class getTypeClass() {
		return typeClass;
	}

	public void setTypeClass(Class typeClass) {
		this.typeClass = typeClass;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return annotations.getAnnotation(annotationClass);
  }
  
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return annotations.isAnnotationPresent(annotationClass);
  }
  
  public Annotation[] getAnnotations() {
    return annotations.getAnnotations();
  }

  public Annotation[] getDeclaredAnnotations() {
    return annotations.getDeclaredAnnotations();
  }
  
	public void addAnnotation(Annotation ann) {
		annotations.addAnnotation(ann);
	}
}
