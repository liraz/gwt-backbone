package org.lirazs.gbackbone.validation.client.adapter;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class ModelFloatAdapter implements TargetDataAdapter<Model, Float> {
    private static final String REGEX_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    @Override
    public Float getData(final Model model, String attribute) throws ConversionException {
        Float result = null;

        Object value = model.get(attribute);
        if(value instanceof String) {
            String floatString = ((String)value).trim();
            if (!floatString.matches(REGEX_DECIMAL)) {
                String message = "Expected a floating point number, but was " + floatString;
                throw new ConversionException(message);
            }
            result = Float.parseFloat(floatString);
        }

        return result;
    }
}
