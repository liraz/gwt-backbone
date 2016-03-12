package org.lirazs.gbackbone.validation.client.annotation;

import org.lirazs.gbackbone.validation.client.rule.AnnotationRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 05/02/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ValidateUsing {
    Class<? extends AnnotationRule> value();
}
