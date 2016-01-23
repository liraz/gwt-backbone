package org.lirazs.gbackbone.client.core.annotation;

import java.lang.annotation.*;

/**
 * Created on 09/01/2016.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ViewTagName {

    String value() default "div";
}