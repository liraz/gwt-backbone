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
import java.lang.reflect.Method;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JAnnotationMethod;
import com.google.gwt.core.ext.typeinfo.JAnnotationType;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.lirazs.gbackbone.gen.AnnotationsHelper;
import org.lirazs.gbackbone.gen.GenUtils;
import org.lirazs.gbackbone.gen.LogableSourceCreator;
import org.lirazs.gbackbone.reflection.client.HasReflect;
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.impl.AnnotationTypeImpl;
import org.lirazs.gbackbone.reflection.client.impl.PrimitiveTypeImpl;

public class ReflectionCreator extends LogableSourceCreator {

	private final boolean isUseLog = true;

	static final String SUFFIX = "__Reflection";

	private JClassType reflectionType = null;

	public ReflectionCreator(TreeLogger logger, GeneratorContext context,
			String typeName) {
		super(logger, context, typeName);
	}

	public static class ReflectionSourceCreator {
		private final String className;
		private final SourceWriter sourceWriter;
		private final JClassType classType;
		private final com.google.gwt.core.ext.typeinfo.TypeOracle typeOracle;
		private final Reflectable reflectable;
		private final TreeLogger logger;

		public ReflectionSourceCreator(String className, JClassType classType,
				SourceWriter sourceWriter,
				com.google.gwt.core.ext.typeinfo.TypeOracle typeOracle,
				TreeLogger logger, Reflectable reflectable) {
			this.className = className;
			this.sourceWriter = sourceWriter;
			this.classType = classType;
			this.typeOracle = typeOracle;
			this.logger = logger;
			this.reflectable = reflectable;
		}

		public void createSource() {
			// if (classType.isAnnotation() != null){
			// createAnnotationImpl(classType.isAnnotation());
			// }
			
			if (classType.isAnnotation() != null) {
				createAnnotationImpl(this.sourceWriter, classType
						.isAnnotation());

			}

			sourceWriter.println("public " + className + "(){");
			sourceWriter.indent();
			// sourceWriter.println("super(\"" +
			// classType.getQualifiedSourceName() +
			// "\");");
			// sourceWriter.println("super(" +
			// classType.getQualifiedSourceName() +
			// ".class);");
			sourceWriter.println("super(" + classType.getQualifiedSourceName()
					+ ".class);");
			// sourceWriter.println("addClassMeta();");
			sourceWriter.println("addAnnotations();");
			sourceWriter.println("addFields();");
			sourceWriter.println("addMethods();");

			if (this.reflectable.constructors()) {
				if ((classType.isClass() != null)
						&& GenUtils.hasPublicDefaultConstructor(classType)) {
					if ((!classType.isAbstract())
							&& (classType.isDefaultInstantiable())) {
						sourceWriter.println("new ConstructorImpl(this){");
						sourceWriter
								.println("	public java.lang.Object newInstance() {");
						sourceWriter.println("return new "
								+ classType.getQualifiedSourceName() + "();");
						// sourceWriter.println("		return GWT.create(" +
						// classType.getQualifiedSourceName() + ".class);");
						sourceWriter.println("	}");
						sourceWriter.println("};");
					}
				}
			}

			sourceWriter.println("");
			if (classType.getSuperclass() != null
					&& classType.getSuperclass().isPublic()) {
				// sourceWriter.println("if (" + "TypeOracleImpl.findType(" +
				// classType.getSuperclass().getQualifiedSourceName() +
				// ".class)" + " != null)");
				if (classType.getSuperclass().isParameterized() != null) {
					// new String[]{java.lang.String, java.lang.Integer}
					String actArgs = GeneratorHelper
							.stringArrayToCode(GeneratorHelper
									.convertJClassTypeToStringArray(classType
											.getSuperclass().isParameterized()
											.getTypeArgs()));
					sourceWriter
							.println("  setSuperclass(new ParameterizedTypeImpl(\""
									+ classType.getSuperclass()
											.getQualifiedSourceName()
									+ "\", "
									+ actArgs + "));");
				} else {
					sourceWriter.println("  setSuperclassName(\""
							+ classType.getSuperclass()
									.getQualifiedSourceName() + "\");");
				}

			}

			sourceWriter.println();
			for (JClassType type : classType.getImplementedInterfaces()) {
				if (type.isParameterized() != null) {
					String actArgs = GeneratorHelper
							.stringArrayToCode(GeneratorHelper
									.convertJClassTypeToStringArray(type
											.isParameterized().getTypeArgs()));
					sourceWriter.println("addImplementedInterface(\""
							+ type.getQualifiedSourceName() + "\", " + actArgs
							+ ");");
				} else {
					sourceWriter.println("addImplementedInterface("
							+ type.getQualifiedSourceName() + ".class);");
				}
			}
			sourceWriter.outdent();
			sourceWriter.println("}");

			// sourceWriter
			// .println(
			// "protected void checkInvokeParams(String methodName, int paramCount, Object[] args) throws IllegalArgumentException{"
			// );
			// sourceWriter.indent();
			// sourceWriter.println("if (args.length != paramCount){");
			// sourceWriter.indent();
			// sourceWriter
			// .println(
			// "throw new IllegalArgumentException(\"Method: \" + methodName + \"request \" + paramCount + \" params, but invoke provide \" + args.length + \" params.\");"
			// );
			// sourceWriter.outdent();
			// sourceWriter.println("}");
			// sourceWriter.outdent();
			// sourceWriter.println("}");
			sourceWriter.println();

			invoke(classType);
			sourceWriter.println();

			// -----Add Class MetaData--------------------------------
			// addClassMeta(classType, sourceWriter);
			// -----Add Class Annotation------------------------------------
			addClassAnnotation(classType, sourceWriter);
			// -----Add fields----------------------------------------
			addFields(classType, sourceWriter);
			// -----Add methods---------------------------------------
			addMethods(classType, sourceWriter);

			setFieldValue(classType, sourceWriter);

			getFieldValue(classType, sourceWriter);
		}

		private void invoke(JClassType classType) {
			JMethod[] methods = classType.getMethods();

			sourceWriter
					.println("public java.lang.Object invoke(java.lang.Object instance, String methodName, Object[] args) throws MethodInvokeException {");
			sourceWriter.indent();

			sourceWriter.println(classType.getQualifiedSourceName()
					+ " content = (" + classType.getQualifiedSourceName()
					+ ")instance;");

			sourceWriter.println("if (args == null){");
			sourceWriter.indent();
			sourceWriter.println("args = new Object[]{};");
			sourceWriter.outdent();
			sourceWriter.println("}");

			for (JMethod method : methods) {
				if (!method.isPublic())
					continue;

				if (!this.reflectable.methods()) {
					HasReflect hasReflect = method
							.getAnnotation(HasReflect.class);
					if (hasReflect == null)
						continue;
				}

				String methodName = method.getName();
				JParameter[] methodParameters = method.getParameters();
				JType returnType = method.getReturnType();

				sourceWriter.println("if (methodName.equals(\"" + methodName
						+ "\")) {");
				sourceWriter.indent();
				sourceWriter.println("checkInvokeParams(methodName, "
						+ methodParameters.length + ", args);");

				if (needCatchException(method)) {
					sourceWriter.println("try{");
					sourceWriter.indent();
				}

				if (!returnType.getSimpleSourceName().equals("void")) {
					sourceWriter.println("return "
							+ boxIfNeed(returnType.getQualifiedSourceName(),
									"content."
											+ methodName
											+ "("
											+ getInvokeParams(methodParameters,
													"args") + ")") + ";");
				} else {
					sourceWriter.println("content." + methodName + "("
							+ getInvokeParams(methodParameters, "args") + ")"
							+ ";");
					sourceWriter.println("return null;");
				}

				if (needCatchException(method)) {
					sourceWriter.println("}catch (Throwable e){");
					sourceWriter.println("throw new MethodInvokeException(e);");
					sourceWriter.println("}");
					sourceWriter.outdent();
				}

				sourceWriter.outdent();
				sourceWriter.print("} else ");

			}
			sourceWriter
					.println("return super.invoke(instance, methodName, args);");
			// sourceWriter.println("{");
			// sourceWriter.indent();
			// sourceWriter
			// .println(
			// "throw new IllegalArgumentException(\"Method: \" + methodName + \" can't found.\");"
			// );
			// sourceWriter.outdent();
			// sourceWriter.println("}");
			sourceWriter.outdent();
			sourceWriter.println("}");
		}

		// protected void addClassMeta(JClassType classType, SourceWriter
		// source) {
		// source.println();
		//
		// source.println("protected void addClassMeta(){");
		// source.indent();
		//
		// GeneratorHelper.addMetaDatas("this", source, classType);
		//
		// source.outdent();
		// source.println("}");
		// }

		protected void addClassAnnotation(JClassType classType,
				SourceWriter source) {
			source.println();

			source.println("protected void addAnnotations(){");
			source.indent();

			if (this.reflectable.classAnnotations()) {
				Annotation[] annotations = AnnotationsHelper
						.getAnnotations(classType);
				GeneratorHelper.addAnnotations_AnnotationImpl(this.typeOracle,
						"this", source, annotations, logger);
			}

			source.outdent();
			source.println("}");
		}

		protected void addFields(JClassType classType, SourceWriter source) {
			source.println();

			source.println("protected void addFields(){");
			source.indent();

			boolean needReflect = this.reflectable.fields();
			source.println("FieldImpl field = null;");

			JField[] fields = classType.getFields();

			for (int i = 0; i < fields.length; i++) {
				JField field = fields[i];

				if (needReflect
						|| field.getAnnotation(HasReflect.class) != null) {
					if (field.isEnumConstant() == null)
						source.println("field = new FieldImpl(this, \""
								+ field.getName() + "\");");
					else
						source.println("field = new EnumConstantImpl(this, \""
								+ field.getName() + "\", "
								+ field.isEnumConstant().getOrdinal() + ");");

					source.println("field.addModifierBits("
							+ GeneratorHelper.AccessDefToInt(field) + "); ");
					source
							.println("field.setTypeName(\""
									+ field.getType().getQualifiedSourceName()
									+ "\");");

					// GeneratorHelper.addMetaDatas("field", source, field);

					if (this.reflectable.fieldAnnotations()
							|| (field.getAnnotation(HasReflect.class) != null && field
									.getAnnotation(HasReflect.class)
									.annotation())) {
						Annotation[] annotations = AnnotationsHelper
								.getAnnotations(field);
						GeneratorHelper.addAnnotations_AnnotationImpl(
								this.typeOracle, "field", source, annotations,
								logger);
					}

					source.println();
				}
			}
			source.outdent();
			source.println("}");
		}

		protected void addMethods(JClassType classType, SourceWriter source) {
			source.println();

			source.println("protected void addMethods(){");
			source.indent();

			source.println("MethodImpl method = null;");

			JMethod[] methods = classType.getMethods();

			boolean needReflect = this.reflectable.methods();

			for (int i = 0; i < methods.length; i++) {
				JMethod method = methods[i];
				// sxf update
				// if (!method.isPublic())
				// continue;

				if (method.isPrivate()) {
					continue;
				}

				if (needReflect
						|| method.getAnnotation(HasReflect.class) != null) {
					source.println("method = new MethodImpl(this, \""
							+ method.getName() + "\");");
					source.println("method.addModifierBits("
							+ GeneratorHelper.AccessDefToInt(method) + "); ");
					source.println("method.setReturnTypeName(\""
							+ method.getReturnType().getQualifiedSourceName()
							+ "\");");

					if (method.isAnnotationMethod() != null) {
						try {
							Class<?> clazz = Class.forName(classType.getQualifiedBinaryName());
							Method m = clazz.getMethod(method.getName());
							if (m != null) {
								source.println("method.setDefaultValue("
										+ GeneratorHelper
												.annoValueToCode(typeOracle, m
														.getDefaultValue(), logger)
										+ ");");
							}
						} catch (ClassNotFoundException e) {
							this.logger
									.log(
											TreeLogger.Type.ERROR,
											"Default value of method will not avaliable for reflection. "
													+ method.toString()
													+ e.toString());
						} catch (SecurityException e) {
							this.logger
									.log(
											TreeLogger.Type.ERROR,
											"Default value of method will not avaliable for reflection. "
													+ method.toString()
													+ e.toString());
						} catch (NoSuchMethodException e) {
							this.logger
									.log(
											TreeLogger.Type.ERROR,
											"Default value of method will not avaliable for reflection. "
													+ method.toString()
													+ e.toString());
						}
					}

					// GeneratorHelper.addMetaDatas("method", source, method);
					JParameter[] params = method.getParameters();
					for (int j = 0; j < params.length; j++) {
						JParameter param = params[j];
						source.println("new ParameterImpl(method, \""
								+ param.getType().getQualifiedSourceName()
								+ "\", \"" + param.getName() + "\");");
						// TODO Support annotation of Parameter
					}

					if (this.reflectable.fieldAnnotations()
							|| (method.getAnnotation(HasReflect.class) != null && method
									.getAnnotation(HasReflect.class)
									.annotation())) {
						Annotation[] annotations = AnnotationsHelper
								.getAnnotations(method);
						GeneratorHelper.addAnnotations_AnnotationImpl(
								this.typeOracle, "method", source, annotations,
								logger);
					}

					source.println();
				}
			}

			source.outdent();
			source.println("}");
		}

		// sxf add
		protected void setFieldValue(JClassType classType, SourceWriter source) {
			sourceWriter
					.println("public void setFieldValue(Object instance, String fieldName, Object value) {");
			sourceWriter.indent();

			sourceWriter.println(classType.getQualifiedSourceName()
					+ " content = (" + classType.getQualifiedSourceName()
					+ ")instance;");

			JField[] fields = classType.getFields();

			for (int i = 0; i < fields.length; i++) {
				JField jField = fields[i];
				
				if (jField.isPrivate() || jField.isFinal() || (jField.isProtected() && GeneratorHelper.isSystemClass(classType))) {
					continue;
				}

				if (!this.reflectable.fields()) {
					HasReflect hasReflect = jField
							.getAnnotation(HasReflect.class);
					if (hasReflect == null)
						continue;
				}

				String fieldName = jField.getName();

				sourceWriter.println("if (fieldName.equals(\"" + fieldName
						+ "\")) {");
				sourceWriter.indent();

				String value = unboxWithoutConvert(jField.getType()
						.getQualifiedSourceName(), "value");
				sourceWriter
						.println("content." + fieldName + "=" + value + ";");

				sourceWriter.outdent();
				sourceWriter.print("} else ");

			}
			sourceWriter
					.println("    super.setFieldValue(instance, fieldName, value);");
			// sourceWriter.println("{");
			// sourceWriter.indent();
			// sourceWriter
			// .println(
			// "throw new IllegalArgumentException(\"Method: \" + methodName + \" can't found.\");"
			// );
			// sourceWriter.outdent();
			// sourceWriter.println("}");
			sourceWriter.outdent();
			sourceWriter.println("}");
			sourceWriter.println();
		}

		// sxf add
		protected void getFieldValue(JClassType classType, SourceWriter source) {
			sourceWriter
					.println("public Object getFieldValue(Object instance, String fieldName) {");
			sourceWriter.indent();

			sourceWriter.println(classType.getQualifiedSourceName()
					+ " content = (" + classType.getQualifiedSourceName()
					+ ")instance;");

			JField[] fields = classType.getFields();

			for (int i = 0; i < fields.length; i++) {
				JField jField = fields[i];
				//Private or protected field under package java.x, javax.x is not accessible 
				if (jField.isPrivate() || (jField.isProtected() && GeneratorHelper.isSystemClass(classType))) {
					continue;
				}

				if (!this.reflectable.fields()) {
					HasReflect hasReflect = jField
							.getAnnotation(HasReflect.class);
					if (hasReflect == null)
						continue;
				}

				String fieldName = jField.getName();

				sourceWriter.println("if (fieldName.equals(\"" + fieldName
						+ "\")) {");
				sourceWriter.indent();

				sourceWriter.println("return content." + fieldName + ";");

				sourceWriter.outdent();
				sourceWriter.print("} else ");

			}
			sourceWriter
					.println("    return super.getFieldValue(instance, fieldName);");
			// sourceWriter.println("{");
			// sourceWriter.indent();
			// sourceWriter
			// .println(
			// "throw new IllegalArgumentException(\"Method: \" + methodName + \" can't found.\");"
			// );
			// sourceWriter.outdent();
			// sourceWriter.println("}");
			sourceWriter.outdent();
			sourceWriter.println("}");
			sourceWriter.println();
		}

		private boolean needCatchException(JMethod method) {
			boolean result = false;
			JClassType runtimeException = typeOracle.findType(
					RuntimeException.class.getCanonicalName())
					.isClassOrInterface();
			for (JType type : method.getThrows()) {
				result = !type.isClassOrInterface().isAssignableTo(
						runtimeException);
				if (result)
					return result;
			}
			return result;
		}

		protected String getInvokeParams(JParameter[] methodParams,
				String argeName) {
			StringBuilder result = new StringBuilder("");
			for (int i = 0; i < methodParams.length; i++) {
				String requestType = methodParams[i].getType()
						.getQualifiedSourceName();
				JType paramType = methodParams[i].getType();
				if (paramType instanceof JArrayType) {
					paramType = ((JArrayType) paramType).getComponentType();
				}
				if (paramType.isTypeParameter() != null)
					requestType = paramType.isTypeParameter().getBaseType()
							.getQualifiedSourceName();
				if (methodParams[i].getType() instanceof JArrayType) {
					if (!requestType.contains("[]"))
						requestType += "[]";
				}
				result.append("("
						+ unboxIfNeed(requestType, argeName + "[" + i + "]")
						+ ")");

				if (i != methodParams.length - 1) {
					result.append(", ");
				}
			}
			return result.toString();
		}

		private void createAnnotationImpl(SourceWriter sourceWriter,
				JAnnotationType annotation) {
			sourceWriter.println();
			String className = annotation.getQualifiedSourceName();
			String implClassName = className.replace('.', '_') + "Impl";
			// we don't have class object,instead,we have a className string,
			// so can't extends AnnotationImpl
			sourceWriter
					.println("public static class " + implClassName
							// + " extends AnnotationImpl "
							+ " implements "
							+ annotation.getQualifiedSourceName() + "{");
			sourceWriter.indent();
			JMethod[] methods = annotation.getMethods();
			// declare variable
			for (JMethod method : methods) {
				sourceWriter.println("private final "
						+ method.getReturnType().getQualifiedSourceName() + " "
						+ method.getName() + ";");
			}

			// Constructor
			StringBuilder sb = new StringBuilder();
			sb.append("public ").append(implClassName).append(
					"(Object[] values){");
			sourceWriter.println(sb.toString());

			// for (JAnnotationMethod method : methods){
			// sb.append(", ").append(method.getReturnType().getQualifiedSourceName()).append(" ").append(method.getName());
			// }
			int i = 0;
			for (JMethod method : methods) {
				sourceWriter.println("  this."
						+ method.getName()
						+ " = "
						+ unboxIfNeed(method.getReturnType()
								.getQualifiedSourceName(), "values[" + i + "]")
						+ ";");
				i++;
			}
			sourceWriter.println("}");

			// Methods
			for (JMethod method : methods) {
				sourceWriter.println("public "
						+ method.getReturnType().getQualifiedSourceName() + " "
						+ method.getName() + "() {");
				sourceWriter.println("  return " + method.getName() + ";");
				sourceWriter.println("}");
			}

			sourceWriter.outdent();

			sourceWriter
					.println("public Class<"+annotation.getQualifiedSourceName()+"> annotationType() {");
			sourceWriter.indent();

			sourceWriter.println("return "+annotation.getQualifiedSourceName()+".class;");
			sourceWriter.outdent();
			sourceWriter.println("  }");

			sourceWriter.println("}");
			sourceWriter.println();

			// create createAnnotation method
			sourceWriter
					.println("public "
							+ implClassName
							+ " createAnnotation(Object[] params) {");
			sourceWriter.indent();
			sourceWriter.println("return new " + implClassName
					+ "(params);");
			sourceWriter.outdent();
			sourceWriter.println("}");
		}

		/**
		 * jdk1.4 did support box and unbox, so
		 * 
		 * @param type
		 * @return
		 */
		public String ensureObjectType(String type) {
			if (type.equals("String")) {
				return "String";
			} else if (type.equals("int")) {
				return "Integer";
			} else if (type.equals("byte")) {
				return "Byte";
			}
			if (type.equals("short")) {
				return "Short";
			}
			if (type.equals("long")) {
				return "Long";
			}
			if (type.equals("float")) {
				return "Float";
			}
			if (type.equals("double")) {
				return "Double";
			}
			if (type.equals("boolean")) {
				return "Boolean";
			}
			if (type.equals("char")) {
				return "Character";
			} else {
				return type;
			}
		}

		/**
		 * object type not equals type, that means PrimitiveType
		 * 
		 * @param type
		 * @return
		 */
		public boolean isPrimitiveType(String type) {
			return !(ensureObjectType(type).equals(type));
		}

		/**
		 * 
		 * @param requestType
		 * @param argeName
		 * @return
		 */
		public String unboxIfNeed(String requestType, String argeName) {
			// System.out.println("requestType: " + requestType + " argeName: "
			// + argeName);
			// return "(" + requestType + ")" + argeName;

			if (!isPrimitiveType(requestType)) {
				return "(" + requestType + ")" + argeName;
			} else if (requestType.equals("int")) {
				return "((Integer)" + argeName + ").intValue()";
			} else if (requestType.equals("byte")) {
				return "((Byte)" + argeName + ").byteValue()";
			}
			if (requestType.equals("short")) {
				return "((Short)" + argeName + ").shortValue()";
			}
			if (requestType.equals("long")) {
				return "((Long)" + argeName + ").longValue()";
			}
			if (requestType.equals("float")) {
				return "((Float)" + argeName + ").floatValue()";
			}
			if (requestType.equals("double")) {
				return "((Double)" + argeName + ").doubleValue()";
			}
			if (requestType.equals("boolean")) {
				return "((Boolean)" + argeName + ").booleanValue()";
			}
			if (requestType.equals("char")) {
				return "((Character)" + argeName + ").charValue()";
			} else {
				return "(" + requestType + ")" + argeName;
			}
		}

		// sxf add
		public String unboxWithoutConvert(String requestType, String argeName) {
			// System.out.println("requestType: " + requestType + " argeName: "
			// + argeName);
			// return "(" + requestType + ")" + argeName;

			if (!isPrimitiveType(requestType)) {
				return "(" + requestType + ")" + argeName;
			} else if (requestType.equals("int")) {
				return "((Integer)" + argeName + ")";
			} else if (requestType.equals("byte")) {
				return "((Byte)" + argeName + ")";
			}
			if (requestType.equals("short")) {
				return "((Short)" + argeName + ")";
			}
			if (requestType.equals("long")) {
				return "((Long)" + argeName + ")";
			}
			if (requestType.equals("float")) {
				return "((Float)" + argeName + ")";
			}
			if (requestType.equals("double")) {
				return "((Double)" + argeName + ")";
			}
			if (requestType.equals("boolean")) {
				return "((Boolean)" + argeName + ")";
			}
			if (requestType.equals("char")) {
				return "((Character)" + argeName + ")";
			} else {
				return "(" + requestType + ")" + argeName;
			}
		}

		/**
		 * Method invoke return an Object, so this auto box
		 * 
		 * @param requestType
		 * @param argeName
		 * @return
		 */
		public String boxIfNeed(String requestType, String argeName) {
			return argeName;

			// if (!isPrimitiveType(requestType)) {
			// // return "(" + requestType + ")" + argeName;
			// // Change to Object to avoid import problem
			// return "(Object)" + argeName;
			// } else if (requestType.equals("integer")) {
			// return "Integer.valueOf(" + argeName + ")";
			// } else if (requestType.equals("Byte")) {
			// return "Byte.valueOf(" + argeName + ")";
			// }
			// if (requestType.equals("Short")) {
			// return "Short.valueOf(" + argeName + ")";
			// }
			// if (requestType.equals("long")) {
			// return "Long.valueOf(" + argeName + ")";
			// }
			// if (requestType.equals("float")) {
			// return "Float.valueOf(" + argeName + ")";
			// }
			// if (requestType.equals("double")) {
			// return "Double.valueOf(" + argeName + ")";
			// }
			// if (requestType.equals("boolean")) {
			// return "Boolean.valueOf(" + argeName + ")";
			// }
			// if (requestType.equals("char")) {
			// return "Character.valueOf(" + argeName + ")";
			// } else {
			// return "(" + requestType + ")" + argeName;
			// }
		}

	}

	protected void createSource(SourceWriter source, JClassType classType) {
		String className = getSimpleUnitName(classType);
		Reflectable reflectable = GenUtils
				.getClassTypeAnnotationWithMataAnnotation(
						getReflectionType(classType), Reflectable.class);
		if (reflectable == null)
			reflectable = ReflectableHelper.getFullSettings(typeOracle);

		new ReflectionSourceCreator(className, getReflectionType(classType),
				source, this.typeOracle, logger, reflectable).createSource();
	}

	/**
	 * SourceWriter instantiation. Return null if the resource already exist.
	 * 
	 * @return sourceWriter
	 */
	public SourceWriter doGetSourceWriter(JClassType classType) {
		String packageName = getPackageName(classType);
		String simpleName = getSimpleUnitName(classType);
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
				packageName, simpleName);
		if(getReflectionType(classType).isAnnotation()!=null){
			composer
			.setSuperclass(AnnotationTypeImpl.class.getName()+ "<"
					+ getReflectionType(classType)
							.getQualifiedSourceName() + ">");
			 
		}else if (getReflectionType(classType).isEnum() == null){
			composer
					.setSuperclass("org.lirazs.gbackbone.reflection.client.impl.ClassTypeImpl<"
							+ getReflectionType(classType)
									.getQualifiedSourceName() + ">");
		}else{
			composer
					.setSuperclass("org.lirazs.gbackbone.reflection.client.impl.EnumTypeImpl<"
							+ getReflectionType(classType)
									.getQualifiedSourceName() + ">");
		}

		// composer.addImplementedInterface(classType.getQualifiedSourceName());
		composer.addImport("java.lang.Object"); // Some times user has there own
		// Object class
		composer.addImport(classType.getQualifiedSourceName());

		composer.addImport("org.lirazs.gbackbone.common.client.*");
		composer.addImport("org.lirazs.gbackbone.reflection.client.*");
		composer.addImport("org.lirazs.gbackbone.reflection.client.impl.*");
		composer.addImport("com.google.gwt.core.client.*");
		composer.addImport("java.util.*");
		composer.addImport(classType.getPackage().getName() + ".*");
		composer.addImport(getReflectionType(classType).getPackage().getName()
				+ ".*");

		PrintWriter printWriter = context.tryCreate(logger, packageName,
				simpleName);
		if (printWriter == null) {
			return null;
		} else {
			SourceWriter sw = composer.createSourceWriter(context, printWriter);
			return sw;
		}
	}

	protected String getPackageName(JClassType classType) {
		classType = getReflectionType(classType);
		String packageName = classType.getPackage().getName();
		// avoid java.lang.SecurityException: Prohibited package name:
		// java.lang...
		if (GeneratorHelper.isSystemClass(classType)) {
			packageName = Reflectable.class.getPackage().getName();
		}
		return packageName;
		// return "org.lirazs.gbackbone.reflection.client.gen."
		// + classType.getPackage().getName();
	}

	protected String getSimpleUnitName(JClassType classType) {
		// sxf update
		// String simpleUnitNameWithOutSuffix =
		// getSimpleUnitNameWithOutSuffix(getReflectionType(classType));
		// String simpleUnitNameWithOutSuffix2 =
		// getSimpleUnitNameWithOutSuffix(classType);
		// String string = simpleUnitNameWithOutSuffix
		// + "_" + simpleUnitNameWithOutSuffix2 + getSUFFIX();

		// String reflectionTypeName = getReflectionType(classType).getName();
		// return reflectionTypeName+ getSUFFIX();

		// return getSimpleUnitNameWithOutSuffix(getReflectionType(classType)) +
		// "_" + getSimpleUnitNameWithOutSuffix(classType) + getSUFFIX();
		JClassType refectionType = getReflectionType(classType);
		if (GeneratorHelper.isSystemClass(refectionType)) {
			return refectionType.getPackage().getName().replace('.', '_') + "_" 
				+ getSimpleUnitNameWithOutSuffix(refectionType)
				+ "_" + getSUFFIX();
		}else{
			return getSimpleUnitNameWithOutSuffix(refectionType)
			+ "_" + getSUFFIX();
		}
		
	}

	protected Type createTypeByJType(JType jtype) {
		if (jtype instanceof JPrimitiveType) {
			return PrimitiveTypeImpl.valueOf(((JPrimitiveType) jtype)
					.getSimpleSourceName());
		} else if (jtype instanceof JClassType) {

		}
		return null;
	}

	@Override
	protected String getSUFFIX() {
		return SUFFIX;
	}

	protected JClassType getReflectionType(JClassType classType) {
		if (reflectionType == null)
			reflectionType = GeneratorHelper.getReflectionClassType(typeOracle,
					classType);

		return reflectionType;
	}

}
