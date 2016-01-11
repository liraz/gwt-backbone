package org.lirazs.gbackbone.client.core.annotation;

import java.lang.annotation.*;

/**
 * Created on 05/12/2015.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectView {

    /**
     * Will be used as the view selector.
     * If not provided, the field name will be taken as the element's ID.
     *
     * @return
     */
    String value() default "";
}
