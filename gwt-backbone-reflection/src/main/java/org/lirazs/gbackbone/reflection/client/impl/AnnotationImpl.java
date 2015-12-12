package org.lirazs.gbackbone.reflection.client.impl;

import java.lang.annotation.Annotation;

import com.google.gwt.core.client.GWT;

/**
 * 
 * @author JamesLuo.au@gmail.com
 *
 */
public abstract class AnnotationImpl<T extends Annotation> implements Annotation {
	
	private final Class<T> clazz;
	
	public AnnotationImpl(Class<T> clazz){
		this.clazz = clazz;
	}

	public Class<T> annotationType() {
		return clazz;
		
	}

}
