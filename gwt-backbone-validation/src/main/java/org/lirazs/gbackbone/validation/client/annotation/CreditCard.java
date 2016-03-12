package org.lirazs.gbackbone.validation.client.annotation;

import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.rule.CreditCardRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 05/02/2016.
 */
@ValidateUsing(CreditCardRule.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Reflectable(classAnnotations = true, fields = false, methods = false, constructors = false,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public @interface CreditCard {

    Type[] cardTypes()  default {
            Type.AMEX, Type.DINERS, Type.DISCOVER,
            Type.MASTERCARD, Type.VISA,
    };
    String message()    default "Invalid card";

    enum Type {
        AMEX, DINERS, DISCOVER, MASTERCARD, VISA, NONE
    }
}
