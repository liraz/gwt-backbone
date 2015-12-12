package org.lirazs.gbackbone.gen.reflection;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.lirazs.gbackbone.reflection.client.PrimitiveType;
import org.lirazs.gbackbone.reflection.client.Type;
import org.lirazs.gbackbone.reflection.client.impl.ArrayTypeImpl;

/**
 * 返回classtype对应的代码，如GWT.create(... ...)
 * 
 * @author sxf
 * 
 */
public class TypeHelper {
	protected static String getSimpleUnitNameWithOutSuffix(JClassType classType) {
		return classType.getName().replace('.', '_');
	}

	private static String getClassInterface(JClassType type) {
		String className = type.getPackage().getName().replace('.', '_') + '_'
				+ getSimpleUnitNameWithOutSuffix(type)
				+ "_GWTENTAUTO_ClassType"; // type.getPackage().getName().replace('.',
		// '_') + '_' +
		// type.getSimpleSourceName().replace('.',
		// '_'); //getSimpleUnitName(type);

		return className;
	}

	private static String getClassInterface(String type) {
		String className = type.replace('.', '_') + "_GWTENTAUTO_ClassType";
		;

		return className;
	}

	public static StringBuffer getClassTypeCode(String type) {
		StringBuffer result = new StringBuffer();
		if (type.endsWith("[]")) {
			String remainder = type.substring(0, type.length() - 2);
			StringBuffer classTypeCode = getClassTypeCode(remainder);
			result.append(" new ArrayTypeImpl(" + classTypeCode + ")");
			return result;
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

			return getClassTypeCode(rawTypeName);

		}
		int indexOf = type.indexOf("extends");
		if (indexOf != -1) {
			type = type.substring(indexOf + "extends ".length());
			return getClassTypeCode(type);
		}

		String str = findPrimitiveTypeString(type);
		if (str != null) {
			result.append(str);
			return result;
		}
		String classInterface = getClassInterface(type);
		result.append("(ClassType)GWT.create(" + classInterface + ".class)");
		return result;
	}

	private static String findPrimitiveTypeString(String name) {
		if (PrimitiveType.BOOLEAN.getSimpleSourceName().equals(name))
			return "PrimitiveType.BOOLEAN";
		else if (PrimitiveType.BYTE.getSimpleSourceName().equals(name))
			return "PrimitiveType.BYTE";
		else if (PrimitiveType.CHAR.getSimpleSourceName().equals(name))
			return "PrimitiveType.CHAR";
		else if (PrimitiveType.DOUBLE.getSimpleSourceName().equals(name))
			return "PrimitiveType.DOUBLE";
		else if (PrimitiveType.FLOAT.getSimpleSourceName().equals(name))
			return "PrimitiveType.FLOAT";
		else if (PrimitiveType.INT.getSimpleSourceName().equals(name))
			return "PrimitiveType.INT";
		else if (PrimitiveType.LONG.getSimpleSourceName().equals(name))
			return "PrimitiveType.LONG";
		else if (PrimitiveType.SHORT.getSimpleSourceName().equals(name))
			return "PrimitiveType.SHORT";
		else if (PrimitiveType.VOID.getSimpleSourceName().equals(name))
			return "PrimitiveType.VOID";
		else
			return null;
	}

}
