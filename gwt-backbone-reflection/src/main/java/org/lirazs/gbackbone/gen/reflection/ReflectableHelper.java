package org.lirazs.gbackbone.gen.reflection;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.reflection.client.annotations.Reflect_Full;

public class ReflectableHelper {
	
	@Reflectable()
	public static class DefaultSettings{
		
	}
	
	@Reflectable(relationTypes=true, superClasses=true, assignableClasses=true)
	public static class FullSettings{
		
	}
	
	
	public static Reflectable getDefaultSettings(TypeOracle typeOracle){
		JClassType type = typeOracle.findType(Reflect_Full.class.getCanonicalName());
		return type.getAnnotation(Reflectable.class);
	}
	
	public static Reflectable getFullSettings(TypeOracle typeOracle){
		JClassType type = typeOracle.findType(Reflect_Full.class.getCanonicalName());
		return type.getAnnotation(Reflectable.class);
	}
	
}
