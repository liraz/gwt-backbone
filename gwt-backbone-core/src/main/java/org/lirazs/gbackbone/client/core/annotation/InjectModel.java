package org.lirazs.gbackbone.client.core.annotation;

import java.lang.annotation.*;

/**
 * Created on 05/12/2015.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectModel {

    /**
     * Will be used as the attribute to be fetched from the view's model.
     * If not provided, the field name will be taken as the attribute name.
     * If view does not have any model, it'll be taken from the attributes object.
     *
     * @return
     */
    String value() default "";
}
