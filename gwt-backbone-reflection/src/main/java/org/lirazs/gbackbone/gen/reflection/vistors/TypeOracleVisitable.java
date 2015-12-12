package org.lirazs.gbackbone.gen.reflection.vistors;

import com.google.gwt.core.ext.typeinfo.JType;

/**
 * 
 * @author James Luo
 *
 * 20/08/2010 4:24:40 PM
 */
public class TypeOracleVisitable {
	
	public void accept(JType type, TypeVisitor tv){
		if (type.isAnnotation() != null){
			tv.visitAnnotationType(type.isAnnotation());
		} else if (type.isArray() != null){
			tv.visitArrayType(type.isArray());
		}else if (type.isEnum() != null){
			tv.visitEnumType(type.isEnum());
		}else if (type.isGenericType() != null){
			tv.visitGenericType(type.isGenericType());
		}else if (type.isParameterized() != null){
			tv.visitParameterizedType(type.isParameterized());
		}else if (type.isPrimitive() != null){
			tv.visitPrimitiveType(type.isPrimitive());
		}else if (type.isRawType() != null){
			tv.visitRawType(type.isRawType());
		}else if (type.isTypeParameter() != null){
			tv.visitTypeParameter(type.isTypeParameter());
		}else if (type.isWildcard() != null){
			tv.visitWildcardType(type.isWildcard());
		}
	}
}
