package org.lirazs.gbackbone.reflection.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lirazs.gbackbone.reflection.client.Reflectable;

/**
 * Just sub classes, limit to without relationTypes
 * 
 * @author JamesLuo.au@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Reflectable(relationTypes=false, superClasses=false, assignableClasses=true)
public @interface Reflect_SubClasses {

}
