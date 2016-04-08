package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.SelectElement;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class SelectElementIntegerAdapter implements TargetDataAdapter<SelectElement, Integer> {
    private static final String REGEX_INTEGER = "\\d+";

    @Override
    public Integer getData(final SelectElement input, String attribute) throws ConversionException {
        String integerString = input.getValue().trim();
        if (!integerString.matches(REGEX_INTEGER)) {
            String message = "Expected an integer, but was " + integerString;
            throw new ConversionException(message);
        }

        return Integer.parseInt(integerString);
    }
}
