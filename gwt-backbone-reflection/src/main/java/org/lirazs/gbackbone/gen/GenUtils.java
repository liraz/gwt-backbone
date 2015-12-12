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


package org.lirazs.gbackbone.gen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JType;
import org.lirazs.gbackbone.common.client.CheckedExceptionWrapper;

public class GenUtils {
	
	public static String getReflection_SUFFIX(){
		return "_Visitor";
	}
	
	public static String getAOP_SUFFIX(){
		return "__AOP";
	}
	
	public static String getTemplate_SUFFIX(){
	  return "__Template";
	}

	public static Class<?> gwtTypeToJavaClass(JType type){
		try {
			return Class.forName(type.getJNISignature().substring(1, type.getJNISignature().length() - 1).replace('/', '.'));
		} catch (ClassNotFoundException e) {
			throw new CheckedExceptionWrapper("Cann't get class from gwt JClassType." + e.getMessage(), e);
		}
	}
	
	/**
	 * Convert GWT method to 
	 * @param method GWT JMethod method
	 * @return Java Method Object
	 */
	public static Method gwtMethodToJavaMethod(JMethod method){
		Class<?> clazz = gwtTypeToJavaClass(method.getEnclosingType());
		Class<?>[] paramClasses = new Class<?>[method.getParameters().length];
		JParameter[] params = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			paramClasses[i] = gwtTypeToJavaClass(params[i].getType());
		}
		try {
			return clazz.getMethod(method.getName(), paramClasses);
		} catch (SecurityException e) {
			throw new CheckedExceptionWrapper(e);
		} catch (NoSuchMethodException e) {
			throw new CheckedExceptionWrapper("NoSuchMethod? GWT Method: " + method.toString() + " EnclosingType: " + method.getEnclosingType().toString(), e);
		}
	}
	
	public static String getParamTypeNames(JMethod method, char quotationMark){
		StringBuilder result = new StringBuilder("");
		boolean needComma = false;
		for (JParameter param : method.getParameters()){
			if (needComma)
				result.append(',').append(quotationMark + param.getType().getQualifiedSourceName() + quotationMark);
			else{
				result.append(quotationMark + param.getType().getQualifiedSourceName() + quotationMark);
				needComma = true;
			}
		}
		
		return result.toString();
	}
	
	public static String getParamNames(JMethod method){
		StringBuilder result = new StringBuilder("");
		boolean needComma = false;
		for (JParameter param : method.getParameters()){
			if (needComma)
				result.append(',').append(param.getName());
			else{
				result.append(param.getName());
				needComma = true;
			}
		}
		
		return result.toString();
	}
	
	public static boolean hasPublicDefaultConstructor(JClassType classType){
		for (JConstructor constructor : classType.getConstructors()){
			if ((constructor.getParameters().length == 0) && constructor.isPublic())
				return true;
		}
		
		return false;
	}
	
	public static boolean checkIfReturnVoid(JMethod method){
		return method.getReturnType().getSimpleSourceName().equals("void");
	}
	
	/**
	 * Find a field, if not found in current classtype, then search it in supper classs
	 * @param classType
	 * @param fieldName
	 * @return
	 */
	public static JField findField(JClassType classType, String fieldName){
	  JField result = null;
    JClassType parent = classType;
    while (parent != null){
      result = parent.findField(fieldName);
      if (result != null)
        return result;
      
      parent = parent.getSuperclass();
    }
    
    return null;
  }
	
	/**
	 * Find a method, if not found in current classType, then find it in super class.
	 * @param classType
	 * @param name
	 * @param paramTypes
	 * @return
	 */
	public static JMethod findMethod(JClassType classType, String name, JType[] paramTypes){
	  JMethod result = null;
    JClassType parent = classType;
    while (parent != null){
      result = parent.findMethod(name, paramTypes);
      if (result != null)
        return result;
      
      parent = parent.getSuperclass();
    }
    
    return null;
	}
	
	/**
	 * Return annotation instance of classType which match annotation class.
	 * NOTE: this function will check classType and all it's parent to see if annotation class exists
	 * @param <T>  the type of annotation
	 * @param classType 
	 * @param annotationClass
	 * @return
	 */
	public static <T extends Annotation> T getClassTypeAnnotation(JClassType classType, Class<T> annotationClass){
	  JClassType parent = classType;
	  while (parent != null){
	    if (parent.getAnnotation(annotationClass) != null)
	      return parent.getAnnotation(annotationClass);
	    
	    parent = parent.getSuperclass();
	  }
	  
	  return null;
	}
	
	/**
	 * Check annotation and its meta reflection to see if it's match annotationClass
	 * if match, return the instance of that annotation, otherwise return null
	 * @param <T>
	 * @param annotation
	 * @param annotationClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
  public static <T extends Annotation> T getAnnotationFromAnnotation(Annotation annotation, Class<T> annotationClass){	  
	 if (annotation.annotationType() == annotationClass){
	   return (T) annotation;
	 }else if (annotation.annotationType().getName().startsWith("java.lang.annotation")){
     return null;  //Document's parent is itself? must check here
   }else{
	   Class<? extends Annotation> annotationType = annotation.annotationType(); 
	   Annotation[] metaAnnotations = annotationType.getAnnotations();
	   for (Annotation metaAnnotation : metaAnnotations) {
	     T result = getAnnotationFromAnnotation(metaAnnotation, annotationClass);
	     if (result != null) {
	       return result;
	     }
	   }
	 }
	 
	 return null;
	}
	
	/**
	 * Get annotation from a classType.
	 * This function will search all super class to try to find out the annotation.
	 * If not found in super class, this function will try all implement interfaces.
	 * 
	 * @param <T>
	 * @param classType The class type 
	 * @param annotationClass The annotation class
	 * @return if found, return the annotation, otherwise return null
	 */
	public static <T extends Annotation> T getClassTypeAnnotationWithMataAnnotation(JClassType classType, Class<T> annotationClass){
	  JClassType parent = classType;
    while (parent != null){
      Annotation[] annotations = AnnotationsHelper.getAnnotations(parent);
      for (Annotation annotation : annotations){
        T result = getAnnotationFromAnnotation(annotation, annotationClass);
        
        if (result != null)
          return result;
      }
      
      parent = parent.getSuperclass();
    }
    
    //if not found in super class, found it in implement interfaces
    parent = classType;
    while (parent != null){
    	for (JClassType inter : parent.getImplementedInterfaces()){
    		T result = getClassTypeAnnotationWithMataAnnotation(inter, annotationClass);
    		
    		if (result != null)
    			return result;
    	}
    	
    	parent = parent.getSuperclass();
    }
    
    return null;
	}
	
	
	public static <T extends Annotation> T getMethodAnnotation(JMethod method, Class<T> annotationClass){
	  if (method.getAnnotation(annotationClass) != null)
	    return method.getAnnotation(annotationClass);
	  
//	  JClassType parent = method.getEnclosingType();
//	  //method.getEnclosingType().getSuperclass().getMethod(method.getName(), method.getParameters().);
//    while (parent != null){
//      if (parent.getAnnotation(annotationClass) != null)
//        return parent.getAnnotation(annotationClass);
//      
//      parent = parent.getSuperclass();
//    }
   
	  
    return null;
  }
	
	
	/**
	 * Get All annotations from classType
	 * NOTE: This is ordered by ParentClass to DevidedClass
	 * The parentclass's annotation comes first
	 * @param <T>
	 * @param classType
	 * @param annotationClass
	 * @return
	 */
	public static <T extends Annotation> Map<Object, T> getAllAnnotations(JClassType classType, Class<T> annotationClass){
		Map<Object, T> results = new HashMap<Object, T>();
		
		JClassType parent = classType.getSuperclass();
		if (parent != null){
			results.putAll(getAllAnnotations(parent, annotationClass));
		}
		
		T a = classType.getAnnotation(annotationClass);
		if (a != null){
			results.put(classType, a);
		}
		
		for (JField field : classType.getFields()){
			a = field.getAnnotation(annotationClass);
			if (a != null)
				results.put(field, a);
		}
		
		for (JMethod method : classType.getMethods()){
			a = method.getAnnotation(annotationClass);
			if (a != null)
				results.put(method, a);
		}

		return results;
	}
	
	/**
	 * Get the full class name, for exampel:
	 * <p> org.lirazs.gbackbone.test.client.UIScreen will return com_gwtent_test_client_UIScreen
	 * @param classType
	 * @return
	 */
	public static String getQualifiedFullNmae(JClassType classType){
		return classType.getQualifiedSourceName().replace(".", "_");
	}
	
}
