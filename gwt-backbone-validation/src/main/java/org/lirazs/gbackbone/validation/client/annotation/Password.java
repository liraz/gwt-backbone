package org.lirazs.gbackbone.validation.client.annotation;

import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.rule.PasswordRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 05/02/2016.
 */
@ValidateUsing(PasswordRule.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Reflectable(classAnnotations = true, fields = false, methods = false, constructors = false,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public @interface Password {

    int min()           default 6;
    Scheme scheme()     default Scheme.ANY;

    String message()    default "Invalid password";

    enum Scheme {
        ANY, ALPHA, ALPHA_MIXED_CASE,
        NUMERIC, ALPHA_NUMERIC, ALPHA_NUMERIC_MIXED_CASE,
        ALPHA_NUMERIC_SYMBOLS, ALPHA_NUMERIC_MIXED_CASE_SYMBOLS
    }
}
