package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.validation.ValidationError;

import java.util.List;

/**
 * Created on 05/02/2016.
 */
public interface ValidationListener {

    /**
     * Called when all validations pass.
     */
    void onValidationSucceeded();

    /**
     * Called when one or several validations fail.
     *
     * @param errors  List containing references to the validations that failed.
     */
    void onValidationFailed(List<ValidationError> errors);
}
