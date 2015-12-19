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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lirazs.gbackbone.reflection.client.AccessDef;
import org.lirazs.gbackbone.reflection.client.ClassHelper;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.Constructor;
import org.lirazs.gbackbone.reflection.client.EnumType;
import org.lirazs.gbackbone.reflection.client.Field;
import org.lirazs.gbackbone.reflection.client.FieldIllegalAccessException;
import org.lirazs.gbackbone.reflection.client.HasAnnotations;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.MethodInvokeException;
import org.lirazs.gbackbone.reflection.client.NotFoundException;
import org.lirazs.gbackbone.reflection.client.Package;
import org.lirazs.gbackbone.reflection.client.Parameter;
import org.lirazs.gbackbone.reflection.client.PrimitiveType;
import org.lirazs.gbackbone.reflection.client.ReflectionRequiredException;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

/**
 * Type representing a Java class or interface type.
 */
public class ClassTypeImpl<T> extends TypeImpl implements AccessDef, HasAnnotations, ClassType<T> {

	private final Set<ClassTypeImpl<?>> allSubtypes = new HashSet<ClassTypeImpl<?>>();
	private final Annotations annotations = new Annotations();

	private Method[] cachedOverridableMethods;

	private final List<ConstructorImpl> constructors = new ArrayList<ConstructorImpl>();

	private ClassTypeImpl<?> enclosingType;

	private final Map<String, FieldImpl> fields = new HashMap<String, FieldImpl>();

	private List<ClassType<?>> lasyinterfaces = null;
	private final List<Class<?>> interfaces = new ArrayList<Class<?>>();
	private final List<ParameterizedTypeImpl> interfacesParameterized = new ArrayList<ParameterizedTypeImpl>();

	private boolean isInterface = false;

	private boolean isLocalType = true;

	// private String lazyHash;

	private final Map methods = new LinkedHashMap<String, List>();

	private int modifierBits;

	private String nestedName;

	private final Map nestedTypes = new HashMap();

	private ClassType<? super T> superclass;
	private final Class<T> declaringClass;

	private Package declaringPackage;

	private boolean savedIsDefaultInstantiable;

	protected void checkInvokeParams(String methodName, int paramCount, Object[] args) throws IllegalArgumentException {
		if (args.length != paramCount) {
			throw new IllegalArgumentException("Method: " + methodName + " request " + paramCount + " params, but invoke provide " + args.length + " params.");
		}
	}

	public Object invoke(Object instance, String methodName, Object[] args) throws MethodInvokeException {
		if (this.getSuperclass() != null)
			return getSuperclass().invoke(instance, methodName, args);
		else
			throw new NotFoundException(methodName + " not found or unimplement?");
	}

	public ClassTypeImpl(Class<T> declaringClass) {
		TypeOracleImpl.putType(this, ReflectionUtils.getQualifiedSourceName(declaringClass));
		this.declaringClass = declaringClass;

		// if (! qualifiedName.equals("java.lang.Object"))
		// setSuperclass((ClassTypeImpl)TypeOracleImpl.findType("java.lang.Object").isClassOrInterface());
	}

	public void addImplementedInterface(Class<?> clazz) {
		interfaces.add(clazz);
	}

	public void addImplementedInterface(String baseClassName, String[] actArgsType) {
		interfacesParameterized.add(new ParameterizedTypeImpl(baseClassName, actArgsType));
	}

	public void addModifierBits(int bits) {
		modifierBits |= bits;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#findField(java.lang.String)
	 */
	public Field findField(String name) {
		Field field = fields.get(name);
		if (field == null && this.getSuperclass() != null)
			field = this.getSuperclass().findField(name);

		return field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#findMethod(java.lang.String,
	 * org.lirazs.gbackbone.client.reflection.Type[])
	 */
	public Method findMethod(String name, Type[] paramTypes) {
		Method method = null;
		if (paramTypes == null)
			paramTypes = new Type[] {};

		Method[] overloads = getOverloads(name);
		for (int i = 0; i < overloads.length; i++) {
			Method candidate = overloads[i];
			if (((MethodImpl) candidate).hasParamTypes(paramTypes)) {
				method = candidate;
			}
		}

		if (method == null && this.getSuperclass() != null)
			method = this.getSuperclass().findMethod(name, paramTypes);

		return method;
	}

	public Method findMethod(String name, Class<?>... paramTypes) {
		if (paramTypes == null)
			paramTypes = new Class<?>[0];

		Type[] types = new Type[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			types[i] = ClassHelper.AsClass(paramTypes[i]).getType();
		}

		return findMethod(name, types);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#findMethod(java.lang.String,
	 * java.lang.String[])
	 */
	public Method findMethod(String name, String[] paramTypes) {
		Method method = null;

		if (paramTypes == null)
			paramTypes = new String[0];

		Method[] overloads = getOverloads(name);
		for (int i = 0; i < overloads.length; i++) {
			MethodImpl candidate = (MethodImpl) overloads[i];
			if (candidate.hasParamTypesByTypeName(paramTypes)) {
				method = candidate;
			}
		}

		if (method == null && this.getSuperclass() != null)
			method = this.getSuperclass().findMethod(name, paramTypes);

		return method;
	}

	public ClassType<?> findNestedType(String typeName) {
		String[] parts = typeName.split("\\.");
		return findNestedTypeImpl(parts, 0);
	}

	public ClassTypeImpl<?> getEnclosingType() {
		return enclosingType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getField(java.lang.String)
	 */
	public Field getField(String name) {
		Field field = findField(name);
		// assert (field != null);
		return field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getFields()
	 */
	public FieldImpl[] getFields() {
		return (FieldImpl[]) fields.values().toArray(TypeOracleImpl.NO_JFIELDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getImplementedInterfaces()
	 */
	public ClassType<?>[] getImplementedInterfaces() throws ReflectionRequiredException {
		if (lasyinterfaces == null) {
			lasyinterfaces = new ArrayList<ClassType<?>>();
			for (Class<?> clazz : interfaces) {
				ClassType<?> type = TypeOracle.Instance.getClassType(clazz);
				if (type != null)
					lasyinterfaces.add(type);
			}

			for (Type type : this.interfacesParameterized) {
				// if (type.isClassOrInterface() != null)
				lasyinterfaces.add((ClassType<?>) type);
			}
		}
		return lasyinterfaces.toArray(TypeOracleImpl.NO_JCLASSES);
	}

	public String getJNISignature() {
		String typeName = nestedName.replace('.', '$');
		String packageName = getPackage().getName().replace('.', '/');
		if (packageName.length() > 0) {
			packageName += "/";
		}
		return "L" + packageName + typeName + ";";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getMethod(java.lang.String,
	 * org.lirazs.gbackbone.client.reflection.Type[])
	 */
	public Method getMethod(String name, Type[] paramTypes) throws NotFoundException {
		Method result = findMethod(name, paramTypes);
		// if (result == null) {
		// throw new NotFoundException();
		// }
		return result;
	}

	/*
	 * Returns the declared methods of this class (not any superclasses or
	 * superinterfaces).
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getMethods()
	 */
	public MethodImpl[] getMethods() {
		List resultMethods = new ArrayList();
		for (Iterator iter = methods.values().iterator(); iter.hasNext();) {
			List overloads = (List) iter.next();
			resultMethods.addAll(overloads);
		}
		return (MethodImpl[]) resultMethods.toArray(TypeOracleImpl.NO_JMETHODS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getName()
	 */
	public String getName() {
		// return nestedName;
		return ReflectionUtils.getQualifiedSourceName(declaringClass);
	}

	public ClassType getNestedType(String typeName) throws NotFoundException {
		ClassType result = findNestedType(typeName);
		if (result == null) {
			throw new NotFoundException();
		}
		return result;
	}

	public ClassType[] getNestedTypes() {
		return (ClassType[]) nestedTypes.values().toArray(TypeOracleImpl.NO_JCLASSES);
	}

	public MethodImpl[] getOverloads(String name) {
		List resultMethods = (List) methods.get(name);
		if (resultMethods != null) {
			return (MethodImpl[]) resultMethods.toArray(TypeOracleImpl.NO_JMETHODS);
		} else {
			return TypeOracleImpl.NO_JMETHODS;
		}
	}

	/**
	 * Iterates over the most-derived declaration of each unique overridable
	 * method available in the type hierarchy of the specified type, including
	 * those found in superclasses and superinterfaces. A method is overridable if
	 * it is not <code>final</code> and its accessibility is <code>public</code>,
	 * <code>protected</code>, or package protected.
	 * 
	 * Deferred binding generators often need to generate method implementations;
	 * this method offers a convenient way to find candidate methods to implement.
	 * 
	 * Note that the behavior does not match
	 * {@link Class#getMethod(String, Class[])}, which does not return the most
	 * derived method in some cases.
	 * 
	 * @return an array of {@link MethodImpl} objects representing overridable
	 *         methods
	 */
	// public Method[] getOverridableMethods() {
	// if (cachedOverridableMethods == null) {
	// Map methodsBySignature = new HashMap();
	// getOverridableMethodsOnSuperinterfacesAndMaybeThisInterface(methodsBySignature);
	// if (isClass() != null) {
	// getOverridableMethodsOnSuperclassesAndThisClass(methodsBySignature);
	// }
	// int size = methodsBySignature.size();
	// Collection leafMethods = methodsBySignature.values();
	// cachedOverridableMethods = (Method[]) leafMethods
	// .toArray(new Method[size]);
	// }
	// return cachedOverridableMethods;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getPackage()
	 */
	public Package getPackage() {
		return declaringPackage;
	}

	public String getQualifiedSourceName() {
		return this.getName();
	}

	public String getSimpleSourceName() {
		return this.getName();
	}

	public ClassType[] getSubtypes() {
		return (ClassType[]) allSubtypes.toArray(TypeOracleImpl.NO_JCLASSES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.lirazs.gbackbone.client.reflection.ClassType#getSuperclass()
	 */
	public ClassType<? super T> getSuperclass() {
		if (superclass == null && superclassName != null)
			try {
				this.setSuperclass(TypeOracle.Instance.getClassType(superclassName));
			} catch (ReflectionRequiredException e) {
				return null;
			}

		return superclass;
	}

	public boolean isAbstract() {
		return 0 != (modifierBits & TypeOracleImpl.MOD_ABSTRACT);
	}

	public ArrayTypeImpl isArray() {
		// intentional null
		return null;
	}

	public boolean isAssignableFrom(ClassType possibleSubtype) {
		if (possibleSubtype == this) {
			return true;
		}
		if (allSubtypes.contains(possibleSubtype)) {
			return true;
			// } else if (this == getOracle().getJavaLangObject()) {
			// // This case handles the odd "every interface is an Object"
			// // but doesn't actually have Object as a superclass.
			// //
			// return true;
		} else {
			return false;
		}
	}

	public boolean isAssignableTo(ClassTypeImpl possibleSupertype) {
		return possibleSupertype.isAssignableFrom(this);
	}

	public ClassType isClass() {
		return isInterface ? null : this;
	}

	/**
	 * Determines if the class can be constructed using a simple <code>new</code>
	 * operation. Specifically, the class must
	 * <ul>
	 * <li>be a class rather than an interface,</li>
	 * <li>have either no constructors or a parameterless constructor, and</li>
	 * <li>be a top-level class or a static nested class.</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if the type is default instantiable, or
	 *         <code>false</code> otherwise
	 */
	public boolean isDefaultInstantiable() {

		return savedIsDefaultInstantiable;
	}

	public ClassType isInterface() {
		return isInterface ? this : null;
	}

	/**
	 * Tests if this type is a local type (within a method).
	 * 
	 * @return true if this type is a local type, whether it is named or
	 *         anonymous.
	 */
	public boolean isLocalType() {
		return isLocalType;
	}

	/**
	 * Tests if this type is contained within another type.
	 * 
	 * @return true if this type has an enclosing type, false if this type is a
	 *         top-level type
	 */
	public boolean isMemberType() {
		return enclosingType != null;
	}

	// public JParameterizedType isParameterized() {
	// // intentional null
	// return null;
	// }
	//
	public PrimitiveType isPrimitive() {
		// intentional null
		return null;
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

	public void setSuperclass(ClassType type) {
		// assert (type != null);
		// assert (isInterface() == null);
		this.superclass = type;
		// ClassType realSuperType;
		// if (type.isParameterized() != null) {
		// realSuperType = type.isParameterized().getBaseType();
		// } else if (type.isRawType() != null) {
		// realSuperType = type.isRawType().getGenericType();
		// } else {
		// realSuperType = (JRealClassType) type;
		// }
		annotations.setParent(type);
	}

	private String superclassName = null;

	public void setSuperclassName(String superclassName) {
		this.superclassName = superclassName;
	}

	public String toString() {
		if (isInterface) {
			return "interface " + getQualifiedSourceName();
		} else {
			return "class " + getQualifiedSourceName();
		}
	}

	protected int getModifierBits() {
		return modifierBits;
	}

	protected void addField(FieldImpl field) {
		Object existing = fields.put(field.getName(), field);
		assert (existing == null);
	}

	public void addMethod(MethodImpl method) {
		String methodName = method.getName();
		List overloads = (List) methods.get(methodName);
		if (overloads == null) {
			overloads = new ArrayList();
			methods.put(methodName, overloads);
		}
		overloads.add(method);
	}

	void addNestedType(ClassTypeImpl type) {
		Object existing = nestedTypes.put(type.getSimpleSourceName(), type);
	}

	ClassType findNestedTypeImpl(String[] typeName, int index) {
		ClassTypeImpl found = (ClassTypeImpl) nestedTypes.get(typeName[index]);
		if (found == null) {
			return null;
		} else if (index < typeName.length - 1) {
			return found.findNestedTypeImpl(typeName, index + 1);
		} else {
			return found;
		}
	}

	// void notifySuperTypes() {
	// notifySuperTypesOf(this);
	// }
	//
	// private void acceptSubtype(ClassTypeImpl me) {
	// allSubtypes.add(me);
	// notifySuperTypesOf(me);
	// }

	private String computeInternalSignature(MethodImpl method) {
		StringBuffer sb = new StringBuffer();
		sb.setLength(0);
		sb.append(method.getName());
		Parameter[] params = method.getParameters();
		for (int j = 0; j < params.length; j++) {
			Parameter param = params[j];
			sb.append("/");
			sb.append(param.getType().getQualifiedSourceName());
		}
		return sb.toString();
	}

	// private void getOverridableMethodsOnSuperclassesAndThisClass(
	// Map methodsBySignature) {
	// // assert (isClass() != null);
	//
	// // Recurse first so that more derived methods will clobber less derived
	// // methods.
	// ClassType superClass = getSuperclass();
	// if (superClass != null) {
	// superClass
	// .getOverridableMethodsOnSuperclassesAndThisClass(methodsBySignature);
	// }
	//
	// MethodImpl[] declaredMethods = getMethods();
	// for (int i = 0; i < declaredMethods.length; i++) {
	// MethodImpl method = declaredMethods[i];
	//
	// // Ensure that this method is overridable.
	// if (method.isFinal() || method.isPrivate()) {
	// // We cannot override this method, so skip it.
	// continue;
	// }
	//
	// // We can override this method, so record it.
	// String sig = computeInternalSignature(method);
	// methodsBySignature.put(sig, method);
	// }
	// }

	/**
	 * Gets the methods declared in interfaces that this type extends. If this
	 * type is a class, its own methods are not added. If this type is an
	 * interface, its own methods are added. Used internally by
	 * {@link #getOverridableMethods()}.
	 * 
	 * @param methodsBySignature
	 */
//	private void getOverridableMethodsOnSuperinterfacesAndMaybeThisInterface(Map methodsBySignature) {
//		// Recurse first so that more derived methods will clobber less derived
//		// methods.
//		ClassType[] superIntfs = getImplementedInterfaces();
//		for (int i = 0; i < superIntfs.length; i++) {
//			ClassTypeImpl superIntf = (ClassTypeImpl) superIntfs[i];
//			superIntf.getOverridableMethodsOnSuperinterfacesAndMaybeThisInterface(methodsBySignature);
//		}
//
//		if (isInterface() == null) {
//			// This is not an interface, so we're done after having visited its
//			// implemented interfaces.
//			return;
//		}
//
//		MethodImpl[] declaredMethods = getMethods();
//		for (int i = 0; i < declaredMethods.length; i++) {
//			MethodImpl method = declaredMethods[i];
//
//			String sig = computeInternalSignature(method);
//			Method existing = (Method) methodsBySignature.get(sig);
//			if (existing != null) {
//				// ClassType existingType = existing.getEnclosingType();
//				// ClassType thisType = method.getEnclosingType();
//				// if (thisType.isAssignableFrom(existingType)) {
//				// // The existing method is in a more-derived type, so don't
//				// replace it.
//				// continue;
//				// }
//			}
//			methodsBySignature.put(sig, method);
//		}
//	}

	/**
	 * Tells this type's superclasses and superinterfaces about it.
	 */
	// private void notifySuperTypesOf(ClassTypeImpl me) {
	// if (superclass != null) {
	// superclass.acceptSubtype(me);
	// }
	// for (int i = 0, n = lasyinterfaces.size(); i < n; ++i) {
	// ClassTypeImpl intf = (ClassTypeImpl) lasyinterfaces.get(i);
	// intf.acceptSubtype(me);
	// }
	// }
	public boolean isFinal() {
		return false;
	}

	void addConstructor(ConstructorImpl ctor) {
		// assert (!constructors.contains(ctor));
		constructors.add(ctor);
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return annotations.getAnnotation(annotationClass);
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return annotations.isAnnotationPresent(annotationClass);
	}

	public Annotation[] getAnnotations() {
		return annotations.getAnnotations();
	}

	public Annotation[] getDeclaredAnnotations() {
		return annotations.getDeclaredAnnotations();
	}

	public Constructor findConstructor(String[] paramTypes) {
		for (ConstructorImpl candidate : constructors) {
			if (candidate.hasParamTypesByTypeName(paramTypes)) {
				return candidate;
			}
		}

		return null;
	}

	public Class<T> getDeclaringClass() {
		return declaringClass;
	}

	// sxf add
	public Object getFieldValue(Object instance, String fieldName) throws FieldIllegalAccessException {
		// no need to call findField(),because we don't want field in super class
		Field field = fields.get(fieldName);
		if (field != null && field.isPrivate()) {
			throw new FieldIllegalAccessException(getName() + "." + fieldName + " is private,can't access");
		}
		if (this.getSuperclass() != null)
			return getSuperclass().getFieldValue(instance, fieldName);
		else
			throw new NotFoundException(fieldName + " not found or unimplement?");
	}

	// sxf add
	public void setFieldValue(Object instance, String fieldName, Object value) throws FieldIllegalAccessException {

		// no need to call findField(),because we don't want field in super class
		Field field = fields.get(fieldName);
		if (field != null && field.isPrivate()) {
			throw new FieldIllegalAccessException(getName() + "." + fieldName + " is private,can't access");
		}
		if (field != null && field.isFinal()) {
			throw new FieldIllegalAccessException(getName() + "." + fieldName + " is final,can't access");
		}

		if (this.getSuperclass() != null)
			getSuperclass().setFieldValue(instance, fieldName, value);
		else
			throw new NotFoundException(fieldName + " not found or unimplement?");
	}

	public void addAnnotation(Annotation ann) {
		annotations.addAnnotation(ann);
	}

}
