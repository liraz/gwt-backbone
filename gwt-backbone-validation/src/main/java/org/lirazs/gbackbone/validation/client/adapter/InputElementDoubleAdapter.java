package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class InputElementDoubleAdapter implements TargetDataAdapter<InputElement, Double> {
    private static final String REGEX_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    @Override
    public Double getData(final InputElement editText) throws ConversionException {
        String doubleString = editText.getValue().trim();
        if (!doubleString.matches(REGEX_DECIMAL)) {
            String message = "Expected a floating point number, but was " + doubleString;
            throw new ConversionException(message);
        }

        return Double.parseDouble(doubleString);
    }
}
