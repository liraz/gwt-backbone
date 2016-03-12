package org.lirazs.gbackbone.client.core.validation;

/**
 * Created on 05/02/2016.
 */
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
