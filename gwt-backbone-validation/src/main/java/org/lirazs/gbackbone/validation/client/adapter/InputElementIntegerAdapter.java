package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.InputElement;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class InputElementIntegerAdapter implements TargetDataAdapter<InputElement, Integer> {
    private static final String REGEX_INTEGER = "\\d+";

    @Override
    public Integer getData(final InputElement input, String attribute) throws ConversionException {
        String integerString = input.getValue().trim();
        if (!integerString.matches(REGEX_INTEGER)) {
            String message = "Expected an integer, but was " + integerString;
            throw new ConversionException(message);
        }

        return Integer.parseInt(integerString);
    }
}
