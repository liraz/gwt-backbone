package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.SelectElement;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class SelectElementFloatAdapter implements TargetDataAdapter<SelectElement, Float> {
    private static final String REGEX_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    @Override
    public Float getData(final SelectElement input, String attribute) throws ConversionException {
        String floatString = input.getValue().trim();
        if (!floatString.matches(REGEX_DECIMAL)) {
            String message = "Expected a floating point number, but was " + floatString;
            throw new ConversionException(message);
        }

        return Float.parseFloat(floatString);
    }
}
