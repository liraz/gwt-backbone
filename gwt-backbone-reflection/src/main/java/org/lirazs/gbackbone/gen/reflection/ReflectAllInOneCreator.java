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

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.HasAnnotations;
import com.google.gwt.core.ext.typeinfo.JAnnotationMethod;
import com.google.gwt.core.ext.typeinfo.JAnnotationType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.lirazs.gbackbone.common.client.CheckedExceptionWrapper;
import org.lirazs.gbackbone.gen.AnnotationsHelper;
import org.lirazs.gbackbone.gen.GenExclusion;
import org.lirazs.gbackbone.gen.GenUtils;
import org.lirazs.gbackbone.gen.LogableSourceCreator;
import org.lirazs.gbackbone.reflection.client.HasReflect;
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.reflection.client.ReflectionTarget;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.impl.TypeOracleImpl;

public class ReflectAllInOneCreator extends LogableSourceCreator {
	
	private List<String> allGeneratedClassNames = new ArrayList<String>();
	private Set<JClassType> relationClassesProcessed = new HashSet<JClassType>();

	public ReflectAllInOneCreator(TreeLogger logger, GeneratorContext context,
			String typeName) {
		super(logger, context, typeName);
		
//		try {
//			System.out.println(context.getPropertyOracle().getPropertyValue(logger, "locale______"));
//		} catch (BadPropertyValueException e) {
//			//nothing, there is no exclusion setting
//		}
	}

	protected GenExclusion getGenExclusion(){
		return GenExclusionCompositeReflection.INSTANCE;
	}

	@Override
	protected String getSUFFIX() {
		return GenUtils.getReflection_SUFFIX();
	}
	
	private List<JClassType> candidateList = new ArrayList<JClassType>();
	private Map<JClassType, Reflectable> candidates = new HashMap<JClassType, Reflectable>();

	@Override
	public void createSource(SourceWriter source, JClassType classType) {
		//ClassType -->> the interface name created automatically
		Map<JClassType, String> typeNameMap = new HashMap<JClassType, String>();
				
		
		genAllClasses(source, typeNameMap);
		
//		source.println("public " + getSimpleUnitName(classType) + "(){");
//		source.indent();
//		
//		for (String classname : allGeneratedClassNames){
//			source.println("new " + classname + "();");
//		}
//		source.outdent();
//		source.println("}");
		
		source.println("public org.lirazs.gbackbone.reflection.client.Type doGetType(String name) {");
		source.indent();
		//source.println("org.lirazs.gbackbone.reflection.client.Type resultType = super.doGetType(name);");
		//source.println("if (resultType != null) {return resultType;}");
		
		for (JClassType type : typeNameMap.keySet()){
			source.println("if (name.equals( \"" + type.getQualifiedSourceName() + "\")){return GWT.create(" + typeNameMap.get(type) + ".class);}");
		}
		source.println();
		source.println("return null;");
		
		source.outdent();
		source.print("}");
		
	}
	
	private void genAllClasses(SourceWriter sourceWriter, Map<JClassType, String> typeNameMap){
		for(JClassType type : candidateList){
			String className = type.getPackage().getName().replace('.', '_') + '_' + getSimpleUnitNameWithOutSuffix(type) + "_GWTENTAUTO_ClassType"; //type.getPackage().getName().replace('.', '_') + '_' + type.getSimpleSourceName().replace('.', '_'); //getSimpleUnitName(type);
			
			sourceWriter.println("@ReflectionTarget(value=\"" + type.getQualifiedSourceName() + "\")");
			sourceWriter.println("public static interface " + className + " extends org.lirazs.gbackbone.reflection.client.ClassType {}");
			
			typeNameMap.put(type, className);
		}
	}
	
	//TODO refactor by source visitor pattern
	
	private void getAllReflectionClasses() throws NotFoundException{

		//System annotations
		addClassIfNotExists(typeOracle.getType(Retention.class.getCanonicalName()), ReflectableHelper.getDefaultSettings(typeOracle));
		addClassIfNotExists(typeOracle.getType(Documented.class.getCanonicalName()), ReflectableHelper.getDefaultSettings(typeOracle));
		addClassIfNotExists(typeOracle.getType(Inherited.class.getCanonicalName()), ReflectableHelper.getDefaultSettings(typeOracle));
		addClassIfNotExists(typeOracle.getType(Target.class.getCanonicalName()), ReflectableHelper.getDefaultSettings(typeOracle));
		addClassIfNotExists(typeOracle.getType(Deprecated.class.getCanonicalName()), ReflectableHelper.getDefaultSettings(typeOracle));
		//typeOracle.getType("org.lirazs.gbackbone.client.test.reflection.TestReflectionGenerics.TestReflection1");
		
		//=====GWT0.7
		for (JClassType classType : typeOracle.getTypes()) {
			Reflectable reflectable = GenUtils.getClassTypeAnnotationWithMataAnnotation(classType, Reflectable.class);
			if (reflectable != null){
				processClass(classType, reflectable);
				
				if (reflectable.assignableClasses()){
					for (JClassType type : classType.getSubtypes()){
						processClass(type, reflectable);
					}
				}
			}
		}
		//======end of gwt0.7
	}

	private void processClass(Class<?> clazz, Reflectable reflectable) {
		processClass(typeOracle.findType(ReflectionUtils.getQualifiedSourceName(clazz)), reflectable);
	}
	
	private void processClass(JClassType classType, Reflectable reflectable) {
		if (! genExclusion(classType)){
			if (addClassIfNotExists(classType, reflectable)) { 
				processRelationClasses(classType, reflectable);
				processAnnotationClasses(classType, reflectable);
			}
		}
	}
	
	private Reflectable getNearestSetting(Class<?> clazz, Reflectable defaultSetting){
		return getNearestSetting(typeOracle.findType(ReflectionUtils.getQualifiedSourceName(clazz)), defaultSetting);
	}
	
	/**
	 * Get nearest Reflectable, if not found, using defaultSetting
	 * @param classType
	 * @param defaultSetting
	 * @return
	 */
	private Reflectable getNearestSetting(JClassType classType, Reflectable defaultSetting){
		Reflectable result = GenUtils.getClassTypeAnnotationWithMataAnnotation(classType, Reflectable.class);
		if (result != null)
			return result;
		else
			return defaultSetting;
	}
	
	private void processRelationClass(JClassType classType, Reflectable reflectable){
		Reflectable nearest = getNearestSetting(classType, reflectable);
		processRelationClasses(classType, nearest);
		processAnnotationClasses(classType, nearest);
		addClassIfNotExists(classType, nearest);
	}
	
	private boolean hasReflection(HasAnnotations type){
		return type.getAnnotation(HasReflect.class) != null;
	}
	
	private boolean hasReflectionAnnotation(HasAnnotations type){
		return (type.getAnnotation(HasReflect.class) != null) && type.getAnnotation(HasReflect.class).annotation();
	}
	
	private void processRelationClasses(JClassType classType, Reflectable reflectable){
		if (classType == null)	
			return;
		
		if (classType.isParameterized() != null)
			classType = classType.isParameterized().getBaseType();
		
		if (classType.isRawType() != null || 
				classType.isWildcard() != null || 
				classType.isTypeParameter() != null)
			classType = classType.getErasedType();
		
		if (relationClassesProcessed.contains(classType))
			return;
		
		processAnnotationClasses(classType, reflectable);
		
		if (reflectable.superClasses()){
			if (classType.getSuperclass() != null){
				processRelationClass(classType.getSuperclass(), reflectable);
			}
		}
		
		if (reflectable.relationTypes()){
			for (JClassType type : classType.getImplementedInterfaces()){
				processRelationClass(type, reflectable);
			}
		}

		relationClassesProcessed.add(classType);
		
		
		
		processFields(classType, reflectable);
		
		processMethods(classType, reflectable);
	}

	private void processFields(JClassType classType, Reflectable reflectable) {
		boolean need = reflectable.relationTypes();
		
		for (JField field : classType.getFields()) {
			if (reflectable.fieldAnnotations() || (hasReflectionAnnotation(field))){
				processAnnotationClasses(field, reflectable);
			  
				JClassType type = field.getType().isClassOrInterface();
			  if (type != null)
			  	if (need || (hasReflection(field) && field.getAnnotation(HasReflect.class).fieldType()))
			  	if (! type.isAssignableTo(classType))  //some times, it's itself of devided class
	  		  	processRelationClasses(type, reflectable);
			  
			  addClassIfNotExists(type, reflectable);
			}
				
		}
	}

	private void processMethods(JClassType classType, Reflectable reflectable) {
		boolean need = reflectable.relationTypes();
		for (JMethod method : classType.getMethods()){
			if (reflectable.fieldAnnotations() || (hasReflectionAnnotation(method))){
				processAnnotationClasses(method, reflectable);
				
				HasReflect hasReflect = method.getAnnotation(HasReflect.class);
				JClassType type = null;
				
				if (need || (hasReflect != null && hasReflect.resultType())){
					if (method.getReturnType() != null && method.getReturnType().isClassOrInterface() != null){
						type = method.getReturnType().isClassOrInterface();
						
						if (! type.isAssignableTo(classType))
							processRelationClasses(type, reflectable);
						
					  addClassIfNotExists(type, reflectable);
					}
				}
				
				
			  if (need || (hasReflect != null && hasReflect.parameterTypes())){
			  	for (JParameter parameter :method.getParameters()){
						if (parameter.getType() != null && parameter.getType().isClassOrInterface() != null){
							type = parameter.getType().isClassOrInterface();
							
							if (! type.isAssignableTo(classType))
								processRelationClasses(type, reflectable);
							
						  addClassIfNotExists(type, reflectable);
						}
					}
			  }
			}
		}
	}
	
	private void processAnnotationClasses(HasAnnotations annotations, Reflectable reflectable){
		if (! reflectable.classAnnotations())
			return;
		
	  Annotation[] annos= AnnotationsHelper.getAnnotations(annotations);
	  if (annos == null)
	    return;
	  
	  for (Annotation annotation : annos){
	    processAnnotation(annotation);
	  }
	}
	
	private Reflectable getFullSettings(){
		return ReflectableHelper.getFullSettings(typeOracle);
	}
	
	private void processClassFromAnnotationValue(Object value){
		if (value != null && value instanceof Class && (!((Class)value).getName().equals("void"))){
			processClass((Class)value, getNearestSetting((Class)value, getFullSettings()));
		}
	}
	
	private void processAnnotation(Annotation annotation){
	  if (annotation.annotationType().getName().startsWith("java.lang.annotation")){
	     return;  //Document's parent is itself, must check here
	   }else{
	     JClassType classType = this.typeOracle.findType(ReflectionUtils.getQualifiedSourceName(annotation.annotationType()));
	     
	     if (classType == null)
	    	 return; //
	     
	     addClassIfNotExists(classType, getNearestSetting(classType, getFullSettings()));
	     
	     //Go through all annotation methods, if has class, add that class to reflection as well
       JAnnotationType annoType = classType.isAnnotation();

       // JAnnotationMethod[] methods = annoType.getMethods();
       JAnnotationMethod[] methods = (JAnnotationMethod[]) annoType.getMethods();
       for (JAnnotationMethod method : methods) {
         Object value = null;
         try {
           value = annotation.annotationType().getMethod(method.getName(), new Class[]{}).invoke(annotation, null);
           //System.out.println(value);
           //System.out.println(value.getClass());
           if (value instanceof Class){
          	 processClassFromAnnotationValue(value);
           }else if (value.getClass().isArray()){
         	    for (int i = 0; i < Array.getLength(value); i++){
         	    	if (Array.get(value, i) instanceof Class)
         	    		processClassFromAnnotationValue(Array.get(value, i));
         	    }
         	  }else if (value instanceof Annotation){
         	  	processAnnotation((Annotation)value);
         	  }
         } catch (Exception e){
         	throw new CheckedExceptionWrapper(e);
         }
         
         if (method.getReturnType() != null){
        	 JType type = method.getReturnType();
        	 processJType(type);
        	 
         }
       }
	     
	     Class<? extends Annotation> annotationType = annotation.annotationType(); 
	     Annotation[] metaAnnotations = annotationType.getAnnotations();
	     for (Annotation metaAnnotation : metaAnnotations) {
	       processAnnotation(metaAnnotation);
	     }
	   }
	}
	
	private void processJType(JType type){
		JClassType classType = null;
		if (type.isClassOrInterface() != null){
			classType = type.isClassOrInterface();
		} else if (type.isArray() != null){
			processJType(type.isArray().getComponentType());
 	  } else if (type.isAnnotation() != null){
 	  	classType = type.isAnnotation();
 	  }
		
		if (classType != null)
			processClass(classType, getNearestSetting(classType, getFullSettings()));
	}
	
	private boolean addClassIfNotExists(JClassType classType, Reflectable setting){
		//Add next line we can make sure we just append normal class type, always get from TypeOracle
		//not JParameterizedType or JTypeParameter etc...
		//RC2 we support ParameterizedType now.

		if (classType != null && classType.isParameterized() == null){
//			System.out.println("addClassIfNotExists: " + classType.getQualifiedSourceName());
			classType = this.typeOracle.findType(classType.getQualifiedSourceName());
		}
		
		//we just process public classes
		if ((classType == null) || (classType.isPrivate()) || (classType.isProtected()) || (GeneratorHelper.isSystemClass(classType) && !classType.isPublic()))
		  return false;

		String qualifiedSourceName = classType.getQualifiedSourceName();

		//no need java.lang.class
		//if (qualifiedSourceName.equals("java.lang.Class"))
		//	return false;

		//no need for system or gwt core classes
		if (qualifiedSourceName.contains("java.lang") || qualifiedSourceName.contains("java.util")
				|| qualifiedSourceName.contains("java.io")
				|| qualifiedSourceName.contains("com.google.gwt"))
			return false;
		
		if (candidateList.indexOf(classType.getErasedType()) < 0){
			candidateList.add(classType.getErasedType());
			candidates.put(classType.getErasedType(), setting);
			return true;
		}
		
		return false;
	}

	protected SourceWriter doGetSourceWriter(JClassType classType) throws NotFoundException {
		if (candidates.size() <= 0){
			getAllReflectionClasses();
		}
		
		String packageName = classType.getPackage().getName();
		String simpleName = getSimpleUnitName(classType);
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
				packageName, simpleName);
		composer.setSuperclass(TypeOracleImpl.class.getCanonicalName());
		
		composer.addImport("org.lirazs.gbackbone.reflection.client.*");
		composer.addImport(classType.getPackage().getName() + ".*");
		
		composer.addImport("org.lirazs.gbackbone.reflection.client.impl.*");
		composer.addImport("com.google.gwt.core.client.*");
		composer.addImport("java.util.*");

		//James remove the following, some times client package have the same 
		//class name which is used by system(ie: Map), if using both package
		//Compiler will raise error.
//		Set<String> imports = new HashSet<String>();
//		for (JClassType aClassType : allReflectionClasses){
//			String str = aClassType.getPackage().getName() + ".*";
//			if (! imports.contains(str)){
//				imports.add(str);
//				composer.addImport(str);
//			}
//		}

		PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
		if (printWriter == null) {
			return null;
		} else {
			SourceWriter sw = composer.createSourceWriter(context, printWriter);
			return sw;
		}
	}


}
