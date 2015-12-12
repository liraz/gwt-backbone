package org.lirazs.gbackbone.reflection.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for Field and Method<br>
 * If this annotation exists in Field or Method,
 * No matter what's the settings in @Reflectable,
 * Allways generate reflection info of that Field or Method. 
 * 
 * @see Reflectable
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface HasReflect {
	/**
	 * True if you want generate annotation information
	 */
	public boolean annotation() default true;
	
	/**
	 * Only for Field
	 * True if generate field type reflection information
	 */
	public boolean fieldType() default true;
	
	/**
	 * Only for Method
	 * True if generate result type reflection information
	 */
	public boolean resultType() default false;
	
	
	/**
	 * Only for Method
	 * True if generate parameter type reflection information
	 */
	public boolean parameterTypes() default false;
}
