package org.lirazs.gbackbone.gen.reflection.vistors;

import java.lang.annotation.Annotation;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JAnnotationType;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JRawType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.JWildcardType;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JArrayTypeVistor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JClassTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JDelegatingClassTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JEnumTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JGenericTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JMaybeParameterizedTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JParameterizedTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JRawTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JRealClassTypeVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JTypeParameterVisitor;
import org.lirazs.gbackbone.gen.reflection.vistors.TypesVisitor.JWildcardTypeVisitor;

/**
 * 
 * @author James Luo
 *
 * 24/08/2010 9:55:50 PM
 */
public class TypesLogVisitor implements TypeVisitor {
	
	public static interface TypeLogger{
		TypeLogger branch(String msg);
		void log(String msg);
	}
	
	public static class TypeLoggerTreeImpl implements TypeLogger{

		private final TreeLogger logger;
		
		public TypeLoggerTreeImpl(TreeLogger logger){
			this.logger = logger;
		}
		
		public TypeLogger branch(String msg) {
			TreeLogger l = logger.branch(Type.TRACE, msg);
			return new TypeLoggerTreeImpl(l);
		}

		public void log(String msg) {
			logger.log(Type.TRACE, msg);
		}
		
	}
	
	private final TreeLogger treeLogger;
	private final TypeLogger logger;
	
	public TypesLogVisitor(TreeLogger logger){
		this.treeLogger = logger;
		this.logger = new TypeLoggerTreeImpl(treeLogger);
	}

	private TypeLogger logNewType(JType type){
		return logger.branch(type.getQualifiedSourceName() + "(" + type.getClass().getSimpleName() + ")");
	}
	
	public JAnnotationTypeVisitor visitAnnotationType(JAnnotationType type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JArrayTypeVistor visitArrayType(JArrayType type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JEnumTypeVisitor visitEnumType(JEnumType type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JGenericTypeVisitor visitGenericType(JGenericType type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JParameterizedTypeVisitor visitParameterizedType(
			JParameterizedType type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JPrimitiveTypeVisitor visitPrimitiveType(JPrimitiveType type) {
		TypeLogger l = logNewType(type);
		l.log("Boxed name:" + type.getQualifiedBoxedSourceName());
		return null;
	}

	public JRawTypeVisitor visitRawType(JRawType type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JTypeParameterVisitor visitTypeParameter(JTypeParameter type) {
		// TODO Auto-generated method stub
		return null;
	}

	public JWildcardTypeVisitor visitWildcardType(JWildcardType type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class ClassTypeLogger implements JClassTypeVisitor{

		public AnnotationVisitor visitAnnotation(Annotation ann) {
			// TODO Auto-generated method stub
			return null;
		}

		public ConstructorVisitor visitConstructor(JConstructor constructor) {
			// TODO Auto-generated method stub
			return null;
		}

		public FieldVisitor visitField(JField field) {
			// TODO Auto-generated method stub
			return null;
		}

		public JClassTypeVisitor visitImplementedInterface(JClassType type) {
			// TODO Auto-generated method stub
			return null;
		}

		public MethodVisitor visitMethod(JMethod method) {
			// TODO Auto-generated method stub
			return null;
		}

		public JClassTypeVisitor visitSuperclass(JClassType type) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
