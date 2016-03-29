package org.lirazs.gbackbone.client.core.validation;

import org.lirazs.gbackbone.reflection.client.Reflectable;

/**
 * Created on 05/02/2016.
 */
@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public interface Rule<VALIDATABLE> {

    /**
     * Checks if the rule is valid.
     *
     * @param validatable  Element on which the validation is applied, could be a data type or a View.
     *
     * @return true if valid, false otherwise.
     */
    boolean isValid(VALIDATABLE validatable);

    /**
     * Returns a failure message associated with the rule.
     *
     * @return A failure message.
     */
    String getMessage();
}
