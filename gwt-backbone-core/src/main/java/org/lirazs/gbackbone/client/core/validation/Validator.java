package org.lirazs.gbackbone.client.core.validation;

import org.lirazs.gbackbone.client.core.data.Options;

import java.util.List;

/**
 * Created on 05/02/2016.
 */
public interface Validator {

    // async validation
    void validate();

    // sync validation
    List<ValidationError> isValid();

    // sync validation
    List<ValidationError> isValid(Options attributes);
}
