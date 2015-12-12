package org.lirazs.gbackbone.reflection.client.impl;

import org.lirazs.gbackbone.reflection.client.AnnotationType;

/**
 * 
 * @author James Luo
 */
public abstract class AnnotationTypeImpl<T> extends ClassTypeImpl<T> implements
		AnnotationType<T> {

	public AnnotationTypeImpl(Class<T> declaringClass) {
		super(declaringClass);
	}

	public AnnotationType<T> isAnnotation() {
		return this;
	}

	

}
