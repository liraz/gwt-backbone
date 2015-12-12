package org.lirazs.gbackbone.reflection.client.impl;

import java.lang.annotation.Annotation;

import org.lirazs.gbackbone.reflection.client.EnumConstant;
import org.lirazs.gbackbone.reflection.client.EnumType;


public class EnumTypeProxy<T> extends ClassTypeProxy<T> implements EnumType<T> {

	public EnumConstant[] getEnumConstants() {
		EnumType <?> t = (EnumType<?>) classType;
		return   t.getEnumConstants();
	}

	public void addAnnotation(Annotation ann) {
		// TODO Auto-generated method stub
		
	}

 
	 
}
