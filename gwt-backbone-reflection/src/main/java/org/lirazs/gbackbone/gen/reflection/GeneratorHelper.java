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


package org.lirazs.gbackbone.gen.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JAnnotationMethod;
import com.google.gwt.core.ext.typeinfo.JAnnotationType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;
import org.lirazs.gbackbone.common.client.CheckedExceptionWrapper;
import org.lirazs.gbackbone.gen.reflection.accessadapter.JFeildAdapter;
import org.lirazs.gbackbone.gen.reflection.accessadapter.JMethodAdapter;
import org.lirazs.gbackbone.reflection.client.AccessDef;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.ReflectionTarget;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.impl.TypeOracleImpl;

public class GeneratorHelper {
	
	public static boolean isSystemClass(JClassType type){
		return type.getPackage().getName().startsWith("java.") || type.getPackage().getName().startsWith("javax.");
	}
	
	/**
	 * private interface ClassTypeOfA extends ClassType<ClassA>{
	 * <p>
	 * <p>	}
	 * 
	 * <p> find a Parameterized class/interface in super classs/interfaces,
	 * this class should a sub class of ClassType class and will point out what's the class need generate reflection information
	 * 
	 * <p> if can NOT found, give a error, user should correct this before final compile
	 * @param classType
	 * @return
	 */
	public static JClassType getReflectionClassType(TypeOracle oracle, JClassType classType){
		JClassType classClassType;
		try {
			classClassType = oracle.getType(ClassType.class.getCanonicalName());
		} catch (NotFoundException e) {
			throw new RuntimeException("Can not found reflection class, forgot include module xml file?" + e.getMessage());
		}
		
		ReflectionTarget target = classType.getAnnotation(ReflectionTarget.class);
		if (target != null){
			if (target.value() != null && target.value().length() > 0){
				try {
					return oracle.getType(target.value());
				} catch (NotFoundException e) {
					
				}
			}
		}
		
		for (JClassType supClass : classType.getFlattenedSupertypeHierarchy()){
			if (supClass.isParameterized() != null && supClass.isAssignableTo(classClassType)){
				if (supClass.isParameterized().getTypeArgs().length == 1){
					return supClass.isParameterized().getTypeArgs()[0];
				}else{
					throw new RuntimeException("ClassType should have only one Parameterized type, please see document of ClassType interface. Current processing type: " + classType.getQualifiedSourceName() + ", Current parameterized type count:" + classType.isParameterized().getTypeArgs().length);
				}
			}
		}
		
		throw new RuntimeException("ClassType should have at least one Parameterized type or annotated by @ReflectionTarget, please see document of ClassType interface. Current processing type: " + classType.getQualifiedSourceName());
	}
	
	
	public static int AccessDefToInt(AccessDef accessDef){
		int result = 0;
		
		if (accessDef.isFinal()) result += TypeOracleImpl.MOD_FINAL;
		if (accessDef.isPrivate()) result += TypeOracleImpl.MOD_PRIVATE;
		if (accessDef.isProtected()) result += TypeOracleImpl.MOD_PROTECTED;
		if (accessDef.isPublic()) result += TypeOracleImpl.MOD_PUBLIC;
		if (accessDef.isStatic()) result += TypeOracleImpl.MOD_STATIC;
		
		return result;
	}
	
	public static int AccessDefToInt(JField field){
		JFeildAdapter adapter = new JFeildAdapter(field);
		return AccessDefToInt(adapter);
	}
	
	public static int AccessDefToInt(JMethod method){
		JMethodAdapter adapter = new JMethodAdapter(method);
		return AccessDefToInt(adapter);
	}
	
	
	/**
	 * Give a array of JClassTypes, return the array of qualified source name of it. 
	 * @param types
	 * @return
	 */
	public static String[] convertJClassTypeToStringArray(JClassType[] types){
		String[] result = new String[types.length];
		
		for (int i = 0; i < types.length; i++)
			result[i] = types[i].getQualifiedSourceName();
			
		return result;
	}
	
	
	public static String stringArrayToCode(String[] strs){
		StringBuilder result = new StringBuilder("new String[]{");
		
		for (int i = 0; i < strs.length; i++){
			result.append("\"" + processInvertedComma(strs[i]) + "\"");
			
			if (i < (strs.length - 1)) result.append(", ");
		}
		
		result.append("}");
		
		return result.toString();
	}
	
	/**
	 * generator metaData
	 * @param dest field or method or class
	 * @param source source to print code
	 * @param metaData 
	 */
	public static void addMetaDatas(String dest, SourceWriter source, com.google.gwt.core.ext.typeinfo.HasMetaData metaData) {
		String[] tags = metaData.getMetaDataTags();
		for (int j = 0; j < tags.length; j++){
			String[][] metas = metaData.getMetaData(tags[j]);
			for (int k = 0; k < metas.length; k++){
				source.println(dest + ".addMetaData(\"" + tags[j] + "\", " + GeneratorHelper.stringArrayToCode(metas[k]) +  ");");
			}
		}
	}
	
	public static String processInvertedComma(String str){
//		return str.replaceAll("\"", "\\\"");
		StringBuffer sb = new StringBuffer();
		int length = str.length();
		for (int i = 0; i < length; i++){
			//not first char and last char  (i != 0) && (i != (length - 1)) && 
			if ((str.charAt(i) == '"')){
				sb.append("\\\"");
			}else{
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
		
	}
	
	private static String toString(Object object){
	  if (object instanceof Class)
	    return ((Class)object).getName();
	  else if (object.getClass().isArray()){
	    StringBuilder sb = new StringBuilder();
	    sb.append("[");
	    for (int i = 0; i < Array.getLength(object); i++){
	      if (i > 0)
	        sb.append(", ");
	      sb.append(Array.get(object, i).toString());
	    }
	    sb.append("]");
	    return sb.toString();
	  }
	  else
	    return object.toString();
	}
	
	
	public static void addAnnotations_AnnotationImpl(TypeOracle typeOracle,
	    String dest, SourceWriter source, Annotation[] annotations, TreeLogger logger){
	
	  if (annotations.length <= 0)
		  return;
		
	  for (Annotation annotation : annotations) {
	  	JClassType classType = typeOracle.findType(ReflectionUtils.getQualifiedSourceName(annotation.annotationType()));
	  	if (classType != null){
	  		source.print(dest + ".addAnnotation(" + createAnnotationValues(typeOracle, annotation, logger) + ");");
				
	  	}else{
	  		logger.log(Type.ERROR, "Annotation (" + ReflectionUtils.getQualifiedSourceName(annotation.annotationType()) + ") not exists in compiled client source code, please ensure this class is exists and included in your module(.gwt.xml) file. GWTENT reflection process will ignore it and continue. ");
	  	}
    }
	}
	
	public static String createAnnotationValues(TypeOracle typeOracle,
			Annotation annotation, TreeLogger logger){
		StringBuilder sb = new StringBuilder();
		JClassType classType = typeOracle.findType(ReflectionUtils.getQualifiedSourceName(annotation.annotationType()));
  	if (classType != null){
  		sb.append("org.lirazs.gbackbone.reflection.client.impl.AnnotationValues.toAnnotation(new org.lirazs.gbackbone.reflection.client.impl.AnnotationValues(");
  		sb.append("\"" + classType.getQualifiedSourceName() + "\", new Object[]{");
  		
  		JAnnotationType annoType = classType.isAnnotation();
  		// JAnnotationMethod[] methods = annoType.getMethods();
  		JAnnotationMethod[] methods = (JAnnotationMethod[]) annoType.getMethods();
  		int index = 0;
			for (JAnnotationMethod method : methods) {
			  Object value = null;
			  try {
			    value = annotation.annotationType().getMethod(method.getName(), new Class[]{}).invoke(annotation);
			    if (index > 0)
			    	sb.append(", ");
			    sb.append(annoValueToCode(typeOracle, value, logger));
			    index ++;
			  } catch (Exception e){
			  	throw new CheckedExceptionWrapper(e);
			  }
			}
			sb.append("}))");
			
  	}else{
  		logger.log(Type.ERROR, "Annotation (" + ReflectionUtils.getQualifiedSourceName(annotation.annotationType()) + ") not exists in compiled client source code, please ensure this class is exists and included in your module(.gwt.xml) file. GWTENT reflection process will ignore it and continue. ");
  	}
  	return sb.toString();
	}
	
	/**
	 * value type: primitive, String, Class, enumerated, annotation, array of
	 * 
	 * primitive need take care. 
	 * For example float 2.0, it need changed to 2.0F, otherwise it's becomes a Double
	 * 
	 * @param object
	 * @return
	 */
	public static String annoValueToCode(TypeOracle typeOracle, Object object, TreeLogger logger){
		if (object == null)
			return "null";
		
		if (object instanceof String){
	  	return "\"" + object.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	  }else if (object instanceof Class){
	    return ((Class)object).getCanonicalName() + ".class";    //inner class will got problem
	  }else if (object.getClass().isArray()){
	    StringBuilder sb = new StringBuilder();
	    //new ElementType[]{ElementType.ANNOTATION_TYPE};
	    Class<?> compType = object.getClass().getComponentType();
	    sb.append("new ").append(compType.getCanonicalName()).append("[] {");
	    for (int i = 0; i < Array.getLength(object); i++){
	      if (i > 0)
	        sb.append(", ");
	      if (compType.isAnnotation())
	      	sb.append("(").append(compType.getCanonicalName()).append(")");
	      sb.append(annoValueToCode(typeOracle, Array.get(object, i), logger));
	    }
	    sb.append("}");
	    return sb.toString();
	  } else if (object.getClass().isEnum()){
	  	return object.getClass().getCanonicalName() + "." + ((Enum)object).name();
	  }else if (object instanceof Annotation){
	  	return createAnnotationValues(typeOracle, (Annotation)object, logger);
	  }else if (object instanceof Float){
	  	return object.toString() + "F";
	  }else if (object instanceof Double){
	  	return object.toString() + "D";
	  } else if (object instanceof Long){
	  	return object.toString() + "L";
	  } 
		
	  return object.toString();
	}
	
	
	
}
