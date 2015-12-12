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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lirazs.gbackbone.reflection.client.AbstractMethod;
import org.lirazs.gbackbone.reflection.client.ArrayType;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.Constructor;
import org.lirazs.gbackbone.reflection.client.HasAnnotations;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.Parameter;
import org.lirazs.gbackbone.reflection.client.Type;

public abstract class AbstractMethodImpl implements HasAnnotations, AbstractMethod {

	private boolean isVarArgs = false;

	private final Annotations annotations =  new Annotations();


	private int modifierBits;
	private final String name;

	private final List params = new ArrayList();

	private final List thrownTypes = new ArrayList();

	private final List typeParams = new ArrayList();

	// Only the builder can construct
	AbstractMethodImpl(String name) {
		this.name = name;
	}

	public void addModifierBits(int bits) {
		modifierBits |= bits;
	}

	public void addThrows(Type type) {
		thrownTypes.add(type);
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#findParameter(java.lang.String)
	 */
	public Parameter findParameter(String name) {
		Iterator iterator = params.iterator();
		Parameter param = null;
		while (iterator.hasNext()) {
			param = (Parameter) iterator.next();
			if (param.getName().equals(name)) {
				return param;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#getEnclosingType()
	 */
	public abstract ClassType getEnclosingType();

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#getParameters()
	 */
	public Parameter[] getParameters() {
		return (Parameter[]) params.toArray(TypeOracleImpl.NO_JPARAMS);
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#getReadableDeclaration()
	 */
	public abstract String getReadableDeclaration();

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#getThrows()
	 */
	public Type[] getThrows() {
		return (Type[]) thrownTypes.toArray(TypeOracleImpl.NO_JTYPES);
	}

	// public TypeParameter[] getTypeParameters() {
	// return typeParams.toArray(new TypeParameter[typeParams.size()]);
	// }

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#isConstructor()
	 */
	public abstract Constructor<?> isConstructor();

	public boolean isDefaultAccess() {
		return 0 == (modifierBits & (TypeOracleImpl.MOD_PUBLIC
				| TypeOracleImpl.MOD_PRIVATE | TypeOracleImpl.MOD_PROTECTED));
	}

	public abstract Method isMethod();

	public boolean isPrivate() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_PRIVATE);
	}

	public boolean isProtected() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_PROTECTED);
	}

	public boolean isPublic() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_PUBLIC);
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#isVarArgs()
	 */
	public boolean isVarArgs() {
		return isVarArgs;
	}

	/* (non-Javadoc)
	 * @see org.lirazs.gbackbone.client.reflection.AbstractMethod#setVarArgs()
	 */
	public void setVarArgs() {
		isVarArgs = true;
	}

	protected int getModifierBits() {
		return modifierBits;
	}

	protected void toStringParamsAndThrows(StringBuffer sb) {
		sb.append("(");
		boolean needComma = false;
		for (int i = 0, c = params.size(); i < c; ++i) {
			Parameter param = (Parameter) params.get(i);
			if (needComma) {
				sb.append(", ");
			} else {
				needComma = true;
			}
			if ((isVarArgs() && i == c - 1) && (param.getType() != null)) {
				ArrayType arrayType = param.getType().isArray();
				assert (arrayType != null);
				sb.append(arrayType.getComponentType().getParameterizedQualifiedSourceName());
				sb.append("...");
			} else {
				sb.append(param.getTypeName());
			}
			sb.append(" ");
			sb.append(param.getName());
		}
		sb.append(")");

		if (!thrownTypes.isEmpty()) {
			sb.append(" throws ");
			needComma = false;
			Iterator iterator = thrownTypes.iterator();
			while (iterator.hasNext()) {
				TypeImpl thrownType = (TypeImpl) iterator.next();
				if (needComma) {
					sb.append(", ");
				} else {
					needComma = true;
				}
				sb.append(thrownType.getParameterizedQualifiedSourceName());
			}
		}
	}

	// protected void toStringTypeParams(StringBuilder sb) {
	// sb.append("<");
	// boolean needComma = false;
	// for (TypeParameter typeParam : typeParams) {
	// if (needComma) {
	// sb.append(", ");
	// } else {
	// needComma = true;
	// }
	// sb.append(typeParam.getName());
	// sb.append(typeParam.getBounds().toString());
	// }
	// sb.append(">");
	// }

	void addParameter(Parameter param) {
		params.add(param);
	}

	// void addTypeParameter(TypeParameter typeParameter) {
	// typeParams.add(typeParameter);
	// }

	boolean hasParamTypes(Type[] paramTypes) {
		if (params.size() != paramTypes.length) {
			return false;
		}

		for (int i = 0; i < paramTypes.length; i++) {
			Parameter candidate = (Parameter) params.get(i);
			// Identity tests are ok since identity is durable within an oracle.
			//
			if (candidate.getType() != paramTypes[i]) {
				return false;
			}
		}
		return true;
	}
	
	boolean hasParamTypesByTypeName(String[] paramTypes) {
		if (params.size() != paramTypes.length) {
			return false;
		}

		for (int i = 0; i < paramTypes.length; i++) {
			Parameter candidate = (Parameter) params.get(i);
			// Identity tests are ok since identity is durable within an oracle.
			//
			if (!candidate.getTypeName().equals(paramTypes[i])) {
				return false;
			}
		}
		return true;
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
  
  public void addAnnotation(Annotation ann) {
		annotations.addAnnotation(ann);
	}
  
  public String toString(){
  	return getReadableDeclaration();
  }
}
