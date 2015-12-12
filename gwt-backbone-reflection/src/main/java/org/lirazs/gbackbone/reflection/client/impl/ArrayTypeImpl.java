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

import org.lirazs.gbackbone.reflection.client.ArrayType;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.Constructor;
import org.lirazs.gbackbone.reflection.client.EnumType;
import org.lirazs.gbackbone.reflection.client.Field;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.MethodInvokeException;
import org.lirazs.gbackbone.reflection.client.PrimitiveType;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

/**
 * Type representing a Java array.
 */
public class ArrayTypeImpl extends TypeImpl implements ArrayType {

  private Type componentType;

  private String lazyQualifiedName;

  private String lazySimpleName;

  ArrayTypeImpl(Type componentType) {
    this.componentType = componentType;
  }

  public Type getComponentType() {
    return componentType;
  }

  public String getJNISignature() {
    return "[" + ((TypeImpl)componentType).getJNISignature();
  }

  public Type getLeafType() {
    return ((TypeImpl)componentType).getLeafType();
  }

  public String getParameterizedQualifiedSourceName() {
    return getComponentType().getParameterizedQualifiedSourceName() + "[]";
  }

  public String getQualifiedSourceName() {
    if (lazyQualifiedName == null) {
      lazyQualifiedName = getComponentType().getQualifiedSourceName() + "[]";
    }
    return lazyQualifiedName;
  }

  public int getRank() {
    ArrayType componentArrayType = componentType.isArray();
    if (componentArrayType != null) {
      return 1 + componentArrayType.getRank();
    }

    return 1;
  }

  public String getSimpleSourceName() {
    if (lazySimpleName == null) {
      lazySimpleName = getComponentType().getSimpleSourceName() + "[]";
    }
    return lazySimpleName;
  }

  public ArrayTypeImpl isArray() {
    return this;
  }

  public ClassType isClass() {
    // intentional null
    return null;
  }

  public ClassType isInterface() {
    // intentional null
    return null;
  }

//  public ParameterizedType isParameterized() {
//    // intentional null
//    return null;
//  }

  public PrimitiveType isPrimitive() {
    // intentional null
    return null;
  }

  public void setLeafType(TypeImpl type) {
//    ArrayType componentTypeIsArray = componentType.isArray();
//    if (componentTypeIsArray != null) {
//      componentTypeIsArray.setLeafType(type);
//    } else {
//      componentType = type;
//    }
  }

  public String toString() {
    return getQualifiedSourceName();
  }

	public Constructor findConstructor(String... paramTypes) {
		return null;
	}

	public Field findField(String name) {
		return null;
	}

	public Method findMethod(String name, Type[] paramTypes) {
		return null;
	}

	public Method findMethod(String name, String... paramTypes) {
		return null;
	}

	public Class<?> getDeclaringClass() {
		return null;
	}

	public Field getField(String name) {
		return null;
	}

	public Field[] getFields() {
		return new Field[0];
	}

	public ClassType[] getImplementedInterfaces() {
		return new ClassType[0];
	}

	public Method getMethod(String name, Type[] paramTypes) {
		return null;
	}

	public Method[] getMethods() {
		return new Method[0];
	}

	public String getName() {
		return this.getSimpleSourceName();
	}

	public ClassType getSuperclass() {
		return TypeOracle.Instance.getJavaLangObject();
	}

	public Object invoke(Object instance, String methodName, Object... args)
			throws MethodInvokeException {
		return null;
	}

	public EnumType isEnum() {
		return null;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	public Annotation[] getAnnotations() {
		return new Annotation[0];
	}

	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return false;
	}

	public Method findMethod(String name, Class... paramTypes) {
		return null;
	}

	//sxf add
	public Object getFieldValue(Object instance, String fieldName) {
		throw new UnsupportedOperationException();
	}
	//sxf add
	public void setFieldValue(Object instance, String fieldName, Object value) {
		throw new UnsupportedOperationException();		
	}

	public void addAnnotation(Annotation ann) {
		throw new UnsupportedOperationException();
	}
}

