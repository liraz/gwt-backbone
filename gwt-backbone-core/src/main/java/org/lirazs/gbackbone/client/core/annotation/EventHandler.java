package org.lirazs.gbackbone.client.core.annotation;

import java.lang.annotation.*;

/**
 * Created on 05/12/2015.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    String value() default "";
}
