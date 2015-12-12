package org.lirazs.gbackbone.reflection.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lirazs.gbackbone.reflection.client.Reflectable;

/**
 * Annotation for a Domain Class
 * All reflection information will be generated.
 * 
 * @author JamesLuo.au@gmail.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Reflectable(classAnnotations = true, fieldAnnotations = true, relationTypes=true, superClasses=true, assignableClasses=true)
public @interface Reflect_Domain {

}
