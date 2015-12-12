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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lirazs.gbackbone.reflection.client.ArrayType;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.NotFoundException;
import org.lirazs.gbackbone.reflection.client.Parameter;
import org.lirazs.gbackbone.reflection.client.PrimitiveType;
import org.lirazs.gbackbone.reflection.client.ReflectionRequiredException;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

public abstract class TypeOracleImpl implements TypeOracle {

	/**
	 * A reserved metadata tag to indicates that a field type, method return
	 * type or method parameter type is intended to be parameterized. Note that
	 * constructor type parameters are not supported at present.
	 */
	public static final String TAG_TYPEARGS = "gwt.typeArgs";

	public static final int MOD_ABSTRACT = 0x00000001;

	public static final int MOD_FINAL = 0x00000002;

	public static final int MOD_NATIVE = 0x00000004;

	public static final int MOD_PRIVATE = 0x00000008;

	public static final int MOD_PROTECTED = 0x00000010;

	public static final int MOD_PUBLIC = 0x00000020;

	public static final int MOD_STATIC = 0x00000040;

	public static final int MOD_TRANSIENT = 0x00000080;

	public static final int MOD_VOLATILE = 0x00000100;

	static final ClassType[] NO_JCLASSES = new ClassType[0];

	// static final JConstructor[] NO_JCTORS = new JConstructor[0];
	static final FieldImpl[] NO_JFIELDS = new FieldImpl[0];

	static final MethodImpl[] NO_JMETHODS = new MethodImpl[0];

	// static final Package[] NO_JPACKAGES = new Package[0];
	static final Parameter[] NO_JPARAMS = new Parameter[0];

	static final Type[] NO_JTYPES = new Type[0];

	static final String[][] NO_STRING_ARR_ARR = new String[0][];

	static final String[] NO_STRINGS = new String[0];

	static String combine(String[] strings, int startIndex) {
		StringBuffer sb = new StringBuffer();
		for (int i = startIndex; i < strings.length; i++) {
			String s = strings[i];
			sb.append(s);
		}
		return sb.toString();
	}

	static String[] modifierBitsToNames(int bits) {
    List<String> strings = new ArrayList<String>();

    // The order is based on the order in which we want them to appear.
    //
    if (0 != (bits & MOD_PUBLIC)) {
      strings.add("public");
    }

    if (0 != (bits & MOD_PRIVATE)) {
      strings.add("private");
    }

    if (0 != (bits & MOD_PROTECTED)) {
      strings.add("protected");
    }

    if (0 != (bits & MOD_STATIC)) {
      strings.add("static");
    }

    if (0 != (bits & MOD_ABSTRACT)) {
      strings.add("abstract");
    }

    if (0 != (bits & MOD_FINAL)) {
      strings.add("final");
    }

    if (0 != (bits & MOD_NATIVE)) {
      strings.add("native");
    }

    if (0 != (bits & MOD_TRANSIENT)) {
      strings.add("transient");
    }

    if (0 != (bits & MOD_VOLATILE)) {
      strings.add("volatile");
    }

    return strings.toArray(NO_STRINGS);
  }

	
	private static String removeAnonymousNumber(String name){
		if (name == null || name.length() <= 0)
			return name;
		
		int lastIndex = name.lastIndexOf(".");
		try {
			Integer.parseInt(name.substring(lastIndex + 1));
			
			name = name.substring(0, lastIndex);
			removeAnonymousNumber(name);
			return name;
		} catch (NumberFormatException e) {
			return name;
		}
	}
	
	public abstract Type doGetType(String name);
	
	public Type getType(String name) throws ReflectionRequiredException {
	// Remove all internal and external whitespace.
    //
		name = name.replaceAll("\\\\s", "");
    try {
			Type result = parseImpl(name);
			if (result == null)
				throw new ReflectionRequiredException();
			else
				return result;
		} catch (ReflectionRequiredException e) {
			Type result = doGetType(name);
			if (result == null)
				throw new ReflectionRequiredException(ReflectionUtils.createReflectionRequireMsg(name, ""));
			else
				return result;
		}
	}
	
	public static Type findType(String name) {
		Type type = typeMap.get(name);
		return type;
	}
	
	public static Type findType(Class<?> clazz) {
		Type type = typeMap.get(clazz.getName().replace('$', '.'));
		return type;
	}

	public ClassType getClassType(String name) throws ReflectionRequiredException {
		Type type = this.getType(name);

		if (type instanceof ClassType)
				return (ClassType)type;
			else
				throw new RuntimeException(name + " not a class type.");
	}
	
	private final Map<Type, ArrayType> arrayTypes = new HashMap<Type, ArrayType>();
	/**
   * Gets the type object that represents an array of the specified type. 
   * 
   * @param componentType the component type of the array, which can itself be
   *          an array type
   * @return a type object representing an array of the component type
   */
  public ArrayType getArrayType(Type componentType) {
    ArrayType arrayType = arrayTypes.get(componentType);
    if (arrayType == null) {
      arrayType = new ArrayTypeImpl(componentType);
      arrayTypes.put(componentType, arrayType);
    }
    return arrayType;
  }

  private ClassType javaLangObject;
  /**
   * Gets a reference to the type object representing
   * <code>java.lang.Object</code>.
   */
  public ClassType getJavaLangObject() {
    if (javaLangObject == null) {
      try {
				javaLangObject = this.getClassType("java.lang.Object");
			} catch (ReflectionRequiredException e) {
				throw new RuntimeException("Not include gwt reflection module? " + e.getMessage());
			}
      assert javaLangObject != null;
    }
    return javaLangObject;
  }

	public static void putType(Type type) {
		putType(type, type.getQualifiedSourceName());
	}

	public static void putType(Type type, String qualifiedSourceName) {
		typeMap.put(qualifiedSourceName, type);
	}

	private static Map<String, Type> typeMap = new HashMap<String, Type>();

	public <T> ClassType<T> getClassType(Class<T> classz) throws ReflectionRequiredException {
		if (classz.isArray()){
			return this.getArrayType(getClassType(classz.getComponentType()));
		}
		
		return getClassType(classz.getName().replace('$', '.'));
	}
	
	
	private Type parseImpl(String type) throws ReflectionRequiredException {
		if (type.endsWith("[]")) {
		  String remainder = type.substring(0, type.length() - 2);
		  Type componentType = this.getType(remainder);
		  return getArrayType(componentType);
		}
		
		if (type.endsWith(">")) {
      int bracket = type.indexOf('<');
      if (bracket == -1) {
        throw new RuntimeException(
            "Mismatched brackets; expected '<' to match subsequent '>'");
      }

      // Resolve the raw type.
      //
      String rawTypeName = type.substring(0, bracket);
      Type rawType = getType(rawTypeName);
      
      //For parameterised type, we just erase it.
      
      if (rawType != null)
      	return rawType;
    }

    Type result = findPrimitiveType(type);
    if (result != null) {
      return result;
    }

    result = findType(type);
    if (result != null) {
      return result;
    }

    throw new ReflectionRequiredException(ReflectionUtils.createReflectionRequireMsg(type, "Unable to recognize '" + type + "' as a type name (is it fully qualified?)"));
	}
	
	private PrimitiveType findPrimitiveType(String name){
		if (PrimitiveType.BOOLEAN.getSimpleSourceName().equals(name))
			return PrimitiveType.BOOLEAN;
		else if (PrimitiveType.BYTE.getSimpleSourceName().equals(name))
			return PrimitiveType.BYTE;
		else if (PrimitiveType.CHAR.getSimpleSourceName().equals(name))
			return PrimitiveType.CHAR;
		else if (PrimitiveType.DOUBLE.getSimpleSourceName().equals(name))
			return PrimitiveType.DOUBLE;
		else if (PrimitiveType.FLOAT.getSimpleSourceName().equals(name))
			return PrimitiveType.FLOAT;
		else if (PrimitiveType.INT.getSimpleSourceName().equals(name))
			return PrimitiveType.INT;
		else if (PrimitiveType.LONG.getSimpleSourceName().equals(name))
			return PrimitiveType.LONG;
		else if (PrimitiveType.SHORT.getSimpleSourceName().equals(name))
			return PrimitiveType.SHORT;
		else if (PrimitiveType.VOID.getSimpleSourceName().equals(name))
			return PrimitiveType.VOID;
		else 
			return null;
	}
}
