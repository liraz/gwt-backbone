package org.lirazs.gbackbone.validation.client.adapter;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class ModelDoubleAdapter implements TargetDataAdapter<Model, Double> {
    private static final String REGEX_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    @Override
    public Double getData(final Model model, String attribute) throws ConversionException {

        Double result = null;

        Object value = model.get(attribute);
        if(value instanceof String) {
            String doubleString = ((String)value).trim();
            if (!doubleString.matches(REGEX_DECIMAL)) {
                String message = "Expected a floating point number, but was " + doubleString;
                throw new ConversionException(message);
            }
            result = Double.parseDouble(doubleString);
        }

        return result;
    }
}
