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

import org.lirazs.gbackbone.reflection.client.AccessDef;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.EnumConstant;
import org.lirazs.gbackbone.reflection.client.Field;
import org.lirazs.gbackbone.reflection.client.FieldIllegalAccessException;
import org.lirazs.gbackbone.reflection.client.HasAnnotations;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.ReflectionRequiredException;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

public class FieldImpl implements Field, AccessDef, HasAnnotations {

	private final ClassType<?> enclosingType;

	private final Annotations annotations = new Annotations();

	private int modifierBits;

	private final String name;

	private Type type;
	private String typeName;
	private Class typeClass;

	public FieldImpl(ClassTypeImpl<?> enclosingType, String name) {
		this.enclosingType = enclosingType;
		this.name = name;

		// assert (enclosingType != null);
		enclosingType.addField(this);
	}

	public void addModifierBits(int modifierBits) {
		this.modifierBits |= modifierBits;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.Field#getEnclosingType()
	 */
	public ClassType<?> getEnclosingType() {
		return enclosingType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.Field#getName()
	 */
	public String getName() {
		// assert (name != null);
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.Field#getType()
	 */
	public Type getType() throws ReflectionRequiredException {
		if (type == null){
			type = TypeOracle.Instance.getType(this.getTypeName());
		}
		
		return type;
	}

	public boolean isDefaultAccess() {
		return 0 == (modifierBits & (TypeOracleImpl.MOD_PUBLIC
				| TypeOracleImpl.MOD_PRIVATE | TypeOracleImpl.MOD_PROTECTED));
	}

	public boolean isFinal() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_FINAL);
	}

	public boolean isPrivate() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_PRIVATE);
	}

	public boolean isProtected() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_PROTECTED);
	}

	public boolean isPublic() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_PUBLIC);
	}

	public boolean isStatic() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_STATIC);
	}

	public boolean isTransient() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_TRANSIENT);
	}

	public boolean isVolatile() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_VOLATILE);
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

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

	/**
	 * NOTE: This method is for testing purposes only.
	 */
	public Annotation[] getAnnotations() {
		return annotations.getAnnotations();
	}

	/**
	 * NOTE: This method is for testing purposes only.
	 */
	public Annotation[] getDeclaredAnnotations() {
		return annotations.getDeclaredAnnotations();
	}
	
	public Object getFieldValue(Object instance) throws FieldIllegalAccessException{
		Method getter = ReflectionUtils.getGetter(this.getEnclosingType(), this
				.getName());
		if (getter != null) {
			return getter.invoke(instance, new Object[] {});
		} else {
			//sxf update
//			throw new RuntimeException("Can not found getter of field (" + getName()
//					+ ").");
			return this.getEnclosingType().getFieldValue(instance,getName());
		}
	}

	private Method setter = null;

	public void setFieldValue(Object instance, Object value) throws FieldIllegalAccessException{
		if (setter == null) {
			setter = ReflectionUtils.getSetter(this.getEnclosingType(), this
					.getName(), value);
		}

		if (setter != null) {
			setter.invoke(instance, new Object[] { value });
		} else {
			//sxf update
//			throw new RuntimeException("Can not found setter of field (" + getName()
//					+ ").");
			this.getEnclosingType().setFieldValue(instance,getName(),value);
					
		}
	}

	public EnumConstant isEnumConstant() {
		return null;
	}

	public Class<?> getDeclaringClass() {
		return this.getEnclosingType().getDeclaringClass();
	}

	public int getModifiers() {
		return this.modifierBits;
	}

	public String toString() {
		String[] names = TypeOracleImpl.modifierBitsToNames(modifierBits);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(names[i]);
		}
		if (names.length > 0) {
			sb.append(" ");
		}
		sb.append(type.getParameterizedQualifiedSourceName());
		sb.append(" ");
		sb.append(getName());
		return sb.toString();
	}

	public void addAnnotation(Annotation ann) {
		annotations.addAnnotation(ann);
	}
}
