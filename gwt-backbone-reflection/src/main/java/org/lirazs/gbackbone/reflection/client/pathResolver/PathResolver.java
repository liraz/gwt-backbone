package org.lirazs.gbackbone.reflection.client.pathResolver;

import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.Field;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

public class PathResolver {

  private static boolean isMethod(String path){
    return (path.indexOf("()") >= 0);
  }
  
  private static void pathNotFound(String path, String fullPath){
    throw new RuntimeException("Path("+ path +") of (" + fullPath + ")not found, please make sure its exists and can be access by subclass.");
  }
  
  
  public static String getFirstElementByPath(String path){
    String[] paths = path.split("\\.");
    if (paths.length > 1)
      return paths[0];
    else
      return path;
  }
  
  public static String getResetElementByPath(String path){
    String[] paths = path.split("\\.");
    if (paths.length > 1){
      StringBuilder sb = new StringBuilder();
      for (int i = 1; i < paths.length; i++){
        if (i == 1)
          sb.append(paths[i]);
        else
          sb.append(".").append(paths[i]);
      }
      return sb.toString();
    }
    else
      return "";
  }
  
  public static String getLastElementByPath(String path){
    String[] paths = path.split("\\.");
    String lastPath = "";
    if (paths.length > 1)
      lastPath = paths[paths.length - 1];
    else
      lastPath = path;
    
    return lastPath;
  }
  
  private static ClassType getClassTypeBySubPath(ClassType parent, String path, String fullPath){
    String typeName = null;
    if (! isMethod(path)){
      Field field = parent.findField(path);
      if (field != null)
        typeName = field.getTypeName();
    }else{
      Method method = parent.findMethod(path, new String[0]);
      
      if (method != null)
        typeName = method.getReturnTypeName();
    }
    
    if (typeName == null)
      throw new RuntimeException("Path("+ path +") not found or returns null, please make sure its exists and can be access by subclass. full path: " + fullPath + "current path: " + path);
    
    ReflectionUtils.checkReflection(typeName);
    
    return TypeOracle.Instance.getClassType(typeName);
  }
  
  /**
   * Get the class type from a path
   * i.e: if fullPath is A.B.C.d
   * this function return the ClassType of C
   * @param clazz
   * @param fullPath
   * @return
   */
  public static ClassType getLastClassTypeByPath(Class<?> clazz, String fullPath){
    ReflectionUtils.checkReflection(clazz);
    
    String[] paths = fullPath.split("\\.");
    ClassType parent = TypeOracle.Instance.getClassType(clazz);
    for (int i = 0; i < paths.length - 1; i ++){
      parent = getClassTypeBySubPath(parent, paths[i], fullPath);
    }
    
    return parent;
  }
  
  private static Object getInstanceBySubPath(Object instance, String path, String fullPath){
    Object object = null;
    ClassType parent = TypeOracle.Instance.getClassType(instance.getClass());
    if (! isMethod(path)){
      Field field = parent.findField(path);
      if (field == null)
        pathNotFound(path, fullPath);
      
      object = field.getFieldValue(instance);
    }else{
      Method method = parent.findMethod(path, new String[0]);
      if (method == null)
        pathNotFound(path, fullPath);
      
      object = method.invoke(instance, null);
    }
    
    if (object == null)
      throw new ENullInPath(fullPath, path);
    
    ReflectionUtils.checkReflection(object.getClass());
    return object;
  }
  
  /**
   * Get the instance from a path
   * i.e: if path is A.B.C.d
   * The rootInstance is A
   * this function return the instance of C
   * @param rootInstance
   * @param path
   * @return
   */
  public static Object getInstanceLastLevelByPath(Object rootInstance, String path){
    if (rootInstance == null)
      return null;
    
    ReflectionUtils.checkReflection(rootInstance.getClass());
    
    String[] paths = path.split("\\.");
    Object parentModel = rootInstance;
    for (int i = 0; i < paths.length - 1; i ++){
      parentModel = getInstanceBySubPath(parentModel, paths[i], path);
    }
    
    return parentModel;
  }
  
  
  public static class ENullInPath extends RuntimeException{

		private static final long serialVersionUID = 1L;
		private final String fullPath;
		private final String errorPath;
		public ENullInPath(String fullPath, String errorPath){
			this.fullPath = fullPath;
			this.errorPath = errorPath;
		}
		
		public String getErrorPath() {
			return errorPath;
		}
		public String getFullPath() {
			return fullPath;
		}
		
		public String getMessage() {
			return "Path returns null, full path: " + fullPath + "current error path: " + errorPath;
		}
  	
  }
}
