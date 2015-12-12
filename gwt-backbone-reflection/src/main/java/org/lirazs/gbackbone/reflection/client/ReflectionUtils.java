package org.lirazs.gbackbone.reflection.client;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {
	
	public static String getQualifiedSourceName(Class<?> clazz){
		return clazz.getName().replace('$', '.');
	}
	
	/**
	 * Get the full description of a class by using reflection
	 * @param clazz
	 * @return
	 */
	public static String getDescription(Class<?> clazz){
		ClassType type = TypeOracle.Instance.getClassType(clazz);
		if (type == null)
			return clazz.getName() +  ": Not Reflection Information available.";
		
		StringBuilder sb = new StringBuilder();
		printAnnotations(type, sb);
		sb.append(type.getName()).append("\n");
		sb.append("\n");
		sb.append("Fields:").append("\n");
		for (Field field : type.getFields()){
			printAnnotations(field, sb);
			sb.append(field.getTypeName()).append(" ").append(field.getName()).append("\n");
		}
		
		sb.append("\n");
		if (type.findConstructor() != null){
			sb.append("Constructor:").append("\n");
			sb.append(type.findConstructor().toString()).append("\n");
		}else{
			sb.append("No default Contructor\n");
		}
		
		sb.append("\n");
		sb.append("Methods:").append("\n");
		for (Method method : type.getMethods()){
			printAnnotations(method, sb);
			sb.append(method.toString()).append("\n");
		}
		
		return sb.toString();
	}
	
	private static void printAnnotations(HasAnnotations annotations, StringBuilder sb){
		if (annotations.getAnnotations().length <= 0)
			return;
		
		//sb.append("Annotation(s):\n");
		for (Annotation anno : annotations.getAnnotations()){
			sb.append(annotationToString(anno)).append("\n");
		}
	}
	
	public static String annotationToString(Annotation anno){
		StringBuilder sb = new StringBuilder();
		
		sb.append(anno.annotationType().getName()).append("(");
		ClassType type = TypeOracle.Instance.getClassType(anno.annotationType());
		for (Method method : type.getMethods()){
			sb.append(method.getName()).append("=").append(method.invoke(anno)).append(";");
		}
		sb.append(")");
		
		return sb.toString();
	}
  
	public static String[] getGetterNames(String name){
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		String[] result = {"get" + name,
				"is" + name};
		return result;
	}
	
	public static String[] getSetterNames(String name){
		String[] result = {"set" + name.substring(0, 1).toUpperCase() + name.substring(1)};
		return result;
	}
	
	/**
	 * Guess setter method of a field name
	 * if get more then 1 method, this function will raise an error
	 * if you have the value to set into a field, please using 
	 * getSetter(ClassType, fieldName, ObjectVlaue)
	 * 
	 * @param classType
	 * @param fieldName
	 * @return
	 */
	public static Method getSetter(ClassType classType, String fieldName){
		for (String methodName : getSetterNames(fieldName)){
			List<Method> methods = new ArrayList<Method>();
      for (Method method : classType.getMethods()){
        if ((method.getName().equals(methodName)) && (method.getParameters().length == 1)){
          methods.add(method);
        }
      }
      
      if (methods.size() == 1)
        return methods.get(0);
      else{
      	if (methods.size() > 1)
        	throw new RuntimeException("Found more then one setter of " + fieldName + " in class " + classType.getName());
      }
		}
		
		if (classType.getSuperclass() != null)
			return getSetter(classType.getSuperclass(), fieldName);
		
		return null;
	}
	
	/**
	 * If you have value to set into field
	 * please using this function, this function will check more and 
	 * found the right method of the value
	 * 
	 * @param classType
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public static Method getSetter(ClassType classType, String fieldName, Object value){
		if (value != null){
			String typeName = value.getClass().getName();
			for (String methodName : getSetterNames(fieldName)){
				Method method = classType.findMethod(methodName, new String[]{typeName});
				
				if (method != null)
					return method;
			}
		}
		
		return getSetter(classType, fieldName);
	}
	
	public static Method getGetter(ClassType classType, String fieldName){
		for (String methodName : getGetterNames(fieldName)){
			Method method = classType.findMethod(methodName);
			if (method != null)
				return method;
		}
		
		return null;
	}
  
  public static void reflectionRequired(String className, String msg){
    throw new ReflectionRequiredException(createReflectionRequireMsg(className, msg));
  }

	public static String createReflectionRequireMsg(String className, String msg) {
		return "your class (" + className + ") should have reflection information before this opeartion. This can be done by annotated class with \"@Reflectable\" annotations, i.e: \"@Reflectable\", \"@Reflect_Domain\", \"@Reflect_Full\", \"@Reflect_Mini\", \"@Validtable\", \"@DataContract\" or implement flag interface \"Reflection\". if you are facing the class which you can not modify(i.e java.*, javax.*), you can using @Reflectable(relationTypes=true) or @Reflect_Domain to your class." +
    		" If your class have done previous steps, please make sure your class is a public class. Current message is : " + "\n" + msg;
	}
  
  /**
   * clazz must have reflection information before continue
   * 
   * @param clazz
   */
  public static void reflectionRequired(Class<?> clazz){
    reflectionRequired(clazz.getName(), null);
  }
  
  public static void reflectionRequired(Class<?> clazz, String msg){
    reflectionRequired(clazz.getName(), msg);
  }
  
  public static boolean checkReflection(String className){
    boolean result = TypeOracle.Instance.getClassType(className) != null;
    
    if (! result)
      ReflectionUtils.reflectionRequired(className, "");
    
    return result;
  }

  /**
   * Check clazz to see if it have reflection information
   * if not, raise a ReflectionRequiredException
   * @param clazz
   */
  public static void checkReflection(Class<?> clazz){
    boolean result = TypeOracle.Instance.getClassType(clazz) != null;
    
    if (! result)
      ReflectionUtils.reflectionRequired(clazz.getName(), "");
  }
  
  /**
   * Get value from annotation which named "methodName"
   * i.e: Annotation(value="abc")
   * 
   * @param annotation annotation
   * @param methodName "value"
   * @return "abc"
   */
  public static Object getAnnotationValueByName(Annotation annotation, String methodName){
  	ClassType type = TypeOracle.Instance.getClassType(annotation.annotationType());
  	if (type == null)
  		reflectionRequired(annotation.annotationType().getName(), "");
  	
  	Method method = type.findMethod(methodName);
  	return method.invoke(annotation);
  }
  
  
  public static Map<String, Object> getAnnotationValues(Annotation annotation){
  	ClassType type = TypeOracle.Instance.getClassType(annotation.annotationType());
  	if (type == null)
  		reflectionRequired(annotation.annotationType().getName(), "");
  	
  	Map<String, Object> result = new HashMap<String, Object>();
  	
  	Method[] methods = type.getMethods();
  	for (Method method : methods){
  		result.put(method.getName(), getAnnotationValueByName(annotation, method.getName()));
  	}
  	
  	return result;
  }
  
  /**
   * Get Annotation.
   * <p>@Entity
   * <p>public class Abc{}
   * 
   *  <p> getAnnotation(Abc.class, Entity.class) will return Entity annotation
   * @param <T>
   * @param clazz 
   * @param annotationClass
   * @return
   */
  public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationClass){
  	ClassType type = TypeOracle.Instance.getClassType(clazz);
  	if (type == null)
  		reflectionRequired(clazz);
  	
  	return type.getAnnotation(annotationClass);
  }
  
  
	/**
	 * Find annotation from array of annotations
	 * @param annos the array of annotations
	 * @param clazz the class of annotation
	 * @return the annotation which meet clazz
	 */
  public static <T extends Annotation> T getAnnotation(Annotation[] annos, Class<T>clazz){
    ClassType classType = TypeOracle.Instance.getClassType(clazz);
    for (Annotation anno : annos){
      if (anno.annotationType().getName() == classType.getName())
        return (T) anno;
    }
    
    return null;
  }
  
  /**
   * Get meta annotation from an annotation
   * @param store the annotation which annotated by meta annotation
   * @param clazz the meta annotation
   * @return if store has annotated by meta annotation, return that annotation, otherwise, return null
   */
  public static <T extends Annotation>T getMetaAnnotation(Annotation store, Class<T> clazz) {
    ClassType annoClass = TypeOracle.Instance.getClassType(store.annotationType());
    if (annoClass != null){
      return ReflectionUtils.getAnnotation(annoClass.getAnnotations(), clazz);
    }
    return null;
  }
  
  /**
   * Get all fields which annotated by clazz.
   * @param classType the classType which contains fields
   * @param clazz the annotation class
   * @return all fields which annotated by clazz
   */
  public static Field[] getAllFields(ClassType classType, Class<? extends Annotation> clazz){
  	List<Field> fields = new ArrayList<Field>();
  	for (Field field : classType.getFields()){
  		Annotation annotation = getAnnotation(field.getAnnotations(), clazz);
  		if (annotation != null)
  			fields.add(field);
  	}
  	return fields.toArray(new Field[0]);
  }
  
  /**
   * Get all methods which annotated by clazz
   * @param classType
   * @param clazz
   * @return
   */
  public static Method[] getAllMethods(ClassType classType, Class<? extends Annotation> clazz){
  	List<Method> methods = new ArrayList<Method>();
  	for (Method method : classType.getMethods()){
  		Annotation annotation = getAnnotation(method.getAnnotations(), clazz);
  		if (annotation != null)
  			methods.add(method);
  	}
  	return methods.toArray(new Method[0]);
  }
  
  public static Method findMethodByName(ClassType classType, String methodName){
  	ClassType parent = classType;
	  while (parent != null){
	    for (Method method : parent.getMethods()){
	    	if (method.getName().equals(methodName))
	    		return method;
	    }
	    
	    parent = parent.getSuperclass();
	  }
	  
	  return null;
  }
  
  /**
   * return true if "classToTest" is assignable to "parentClass"
   * @param parentClass
   * @param classToTest
   * @return
   */
  public static boolean isAssignable(Class<?> parentClass, Class<?> classToTest){
  	checkReflection(classToTest);
  	
  	ClassType typeToTest = TypeOracle.Instance.getClassType(classToTest);
  	
  	if (testAssignableWithoutSuper(parentClass, typeToTest))
  		return true;
  	
  	for (ClassType type : typeToTest.getImplementedInterfaces()){
			if (isAssignable(parentClass, type.getDeclaringClass()))
				return true;
		}
  	
  	ClassType parentToTest = typeToTest.getSuperclass();
  	while (parentToTest != null){
  		if (isAssignable(parentClass, parentToTest.getDeclaringClass()))
  			return true;
  		
  		parentToTest = parentToTest.getSuperclass();
  	}
  	
  	return false;
  }
  
  private static boolean testAssignableWithoutSuper(Class<?> parentClass, ClassType typeToTest){
  	if (typeToTest.getDeclaringClass() == parentClass)
  		return true;
  	else
  		return false;
  }
}
