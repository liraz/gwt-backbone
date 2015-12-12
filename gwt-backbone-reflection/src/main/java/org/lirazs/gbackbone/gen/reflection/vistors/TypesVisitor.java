package org.lirazs.gbackbone.gen.reflection.vistors;

import java.lang.annotation.Annotation;

import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JEnumConstant;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JRawType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;

/**
 * 
 * @author James Luo
 *
 * 20/08/2010 4:33:11 PM
 */
public interface TypesVisitor {
	
	public static interface JPrimitiveTypeVisitor{
		
	}
	
	public static interface JClassTypeVisitor{
		AnnotationVisitor visitAnnotation(Annotation ann);
		JClassTypeVisitor visitSuperclass(JClassType type);
		JClassTypeVisitor visitImplementedInterface(JClassType type);
		ConstructorVisitor visitConstructor(JConstructor constructor);
	  MethodVisitor visitMethod(JMethod method);
	  FieldVisitor visitField(JField field);
	}
	
	public static interface JArrayTypeVistor extends JClassTypeVisitor{
		TypeVisitor visitComponentType(JType componentType);
	}
	
	public static interface JRealClassTypeVisitor extends JClassTypeVisitor{
		
	}
	
	public static interface JDelegatingClassTypeVisitor extends JClassTypeVisitor{
		JClassTypeVisitor visitBaseType(JClassType type);
	}
	
	public static interface JMaybeParameterizedTypeVisitor extends JDelegatingClassTypeVisitor{
		
	}
	
	public static interface JTypeParameterVisitor extends JDelegatingClassTypeVisitor{
		
	}
	
	public static interface JWildcardTypeVisitor extends JDelegatingClassTypeVisitor{
		
	}
	
	public static interface JParameterizedTypeVisitor extends JMaybeParameterizedTypeVisitor{
		
	}
	
	public static interface JRawTypeVisitor extends JMaybeParameterizedTypeVisitor{
		
	}
	
	public static interface JAnnotationTypeVisitor extends JRealClassTypeVisitor{
		
	}
	
	public static interface JEnumTypeVisitor extends JRealClassTypeVisitor{
		EnumConstantVisitor visitEnumConstant(JEnumConstant enumConstant);
	}
	
	public static interface JGenericTypeVisitor extends JRealClassTypeVisitor{
		JRawTypeVisitor visitRawType(JRawType type);
		JTypeParameterVisitor visitTypeParameter(JTypeParameter type);
	}
	
	
}
