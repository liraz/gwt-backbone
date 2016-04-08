package org.lirazs.gbackbone.validation.client.adapter;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class ModelIntegerAdapter implements TargetDataAdapter<Model, Integer> {
    private static final String REGEX_INTEGER = "\\d+";

    @Override
    public Integer getData(final Model model, String attribute) throws ConversionException {
        Integer result = null;

        Object value = model.get(attribute);
        if(value instanceof String) {
            String integerString = ((String)value).trim();
            if (!integerString.matches(REGEX_INTEGER)) {
                String message = "Expected an integer, but was " + integerString;
                throw new ConversionException(message);
            }

            result = Integer.parseInt(integerString);
        }

        return result;
    }
}
