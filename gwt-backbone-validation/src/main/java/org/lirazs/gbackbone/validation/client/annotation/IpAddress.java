package org.lirazs.gbackbone.validation.client.annotation;

import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.rule.IpAddressRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 05/02/2016.
 */
@ValidateUsing(IpAddressRule.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Reflectable(classAnnotations = true, fields = false, methods = false, constructors = false,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public @interface IpAddress {

    String message() default "Invalid IP address";
}
