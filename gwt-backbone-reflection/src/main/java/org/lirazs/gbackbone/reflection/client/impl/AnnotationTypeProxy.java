package org.lirazs.gbackbone.reflection.client.impl;

import java.lang.annotation.Annotation;

import org.lirazs.gbackbone.reflection.client.AnnotationType;


public class AnnotationTypeProxy<T> extends ClassTypeProxy<T> implements AnnotationType<T> {

	public T createAnnotation(Object[] params) {
		AnnotationType <?> t = (AnnotationType<?>) classType;
		return (T) t.createAnnotation(params);
	}

	public void addAnnotation(Annotation ann) {
		// TODO Auto-generated method stub
		
	}
	 
}
