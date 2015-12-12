package org.lirazs.gbackbone.reflection.client.impl;

import java.lang.annotation.Annotation;
import java.util.List;

import org.lirazs.gbackbone.reflection.client.ArrayType;
import org.lirazs.gbackbone.reflection.client.ClassHelper;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.Constructor;
import org.lirazs.gbackbone.reflection.client.EnumType;
import org.lirazs.gbackbone.reflection.client.Field;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.MethodInvokeException;
import org.lirazs.gbackbone.reflection.client.ParameterizedType;
import org.lirazs.gbackbone.reflection.client.PrimitiveType;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

/**
 * 
 * @author James Luo
 *
 * 13/08/2010 3:39:16 PM
 */
public class ParameterizedTypeImpl<T> extends TypeImpl implements ParameterizedType<T> {

	private ClassTypeImpl<T> baseType;
	private final String baseTypeName;
	private final String[] actArgsType;
	private ClassType<?>[] actualTypeArguments;
	
	public ParameterizedTypeImpl(String baseClassTypeName, String[] actArgsType) {
		assert baseClassTypeName != null;
		
		baseTypeName = baseClassTypeName;
		
		
		if (actArgsType == null)
			actArgsType = new String[0];
		this.actArgsType = actArgsType;
	}
	
	private ClassType<T> getBaseType(){
		if (baseType == null){
			//Type type = TypeOracleImpl.findType(baseTypeName);
			Type type = TypeOracle.Instance.getType(baseTypeName);
			if (type == null)
				ReflectionUtils.checkReflection(baseTypeName);
			
			baseType = (ClassTypeImpl<T>)type.isClassOrInterface();
			if (baseType == null)
				throw new RuntimeException("Super class of a parameterized type must a class or interface. current type name:" + baseTypeName);
		}
		
		return baseType;
	}

	public ClassType<?>[] getActualTypeArguments() {
		if (actualTypeArguments == null){
			actualTypeArguments = new ClassType[actArgsType.length];
			for (int i = 0; i < actArgsType.length; i++){
				String name = actArgsType[i];
				ClassType<?> type = TypeOracle.Instance.getClassType(name);
				actualTypeArguments[i] = type;
			}
		}
		
		return actualTypeArguments;
	}

	public Type getOwnerType() {
		return null;
	}

	public ClassType<T> getRawType() {
		return getBaseType();
	}

	@Override
	public String getJNISignature() {
		return ((ClassTypeImpl<T>)getBaseType()).getJNISignature();
	}

	@Override
	public ClassType<?> isClass() {
		return getBaseType().isClass();
	}

	@Override
	public ClassType<?> isInterface() {
		return getBaseType().isInterface();
	}

	@Override
	public PrimitiveType isPrimitive() {
		return getBaseType().isPrimitive();
	}

	public Constructor findConstructor(String... paramTypes) {
		return getBaseType().findConstructor(paramTypes);
	}

	public Field findField(String name) {
		return getBaseType().findField(name);
	}

	public Method findMethod(String name, Class... paramTypes) {
		return getBaseType().findMethod(name, paramTypes);
	}

	public Method findMethod(String name, Type[] paramTypes) {
		return getBaseType().findMethod(name, paramTypes);
	}

	public Method findMethod(String name, String[] paramTypes) {
		return getBaseType().findMethod(name, paramTypes);
	}

	public Class<T> getDeclaringClass() {
		return getBaseType().getDeclaringClass();
	}

	public Field getField(String name) {
		return getBaseType().getField(name);
	}

	public Field[] getFields() {
		return getBaseType().getFields();
	}

	public ClassType<?>[] getImplementedInterfaces() {
		return getBaseType().getImplementedInterfaces();
	}

	public Method getMethod(String name, Type[] paramTypes) {
		return getBaseType().getMethod(name, paramTypes);
	}

	public Method[] getMethods() {
		return getBaseType().getMethods();
	}

	public String getName() {
		return getBaseType().getName();
	}

	public ClassType<? super T> getSuperclass() {
		return getBaseType().getSuperclass();
	}

	public Object invoke(Object instance, String methodName, Object... args)
			throws MethodInvokeException {
		return getBaseType().invoke(instance, methodName, args);
	}

	public EnumType<?> isEnum() {
		return getBaseType().isEnum();
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return getBaseType().getAnnotation(annotationClass);
	}

	public Annotation[] getAnnotations() {
		return getBaseType().getAnnotations();
	}

	public Annotation[] getDeclaredAnnotations() {
		return getBaseType().getDeclaredAnnotations();
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getBaseType().isAnnotationPresent(annotationClass);
	}

	public String getQualifiedSourceName() {
		return getBaseType().getQualifiedSourceName();
	}

	public String getSimpleSourceName() {
		return getBaseType().getSimpleSourceName();
	}

	public ParameterizedType<T> isParameterized(){
		return this;
	}

	public ArrayType isArray() {
		return null;
	}
	//sxf add
	public Object getFieldValue(Object instance, String fieldName) {
		return this.getBaseType().getFieldValue(instance, fieldName);
	}
	//sxf add
	public void setFieldValue(Object instance, String fieldName, Object value) {
		this.getBaseType().setFieldValue(instance, fieldName, value);
	}

	public void addAnnotation(Annotation ann) {
		getBaseType().addAnnotation(ann);
	}

}
