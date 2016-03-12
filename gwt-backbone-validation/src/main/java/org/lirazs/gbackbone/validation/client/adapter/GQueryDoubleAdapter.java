package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class GQueryDoubleAdapter implements TargetDataAdapter<GQuery, Double> {
    private static final String REGEX_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    @Override
    public Double getData(final GQuery editText) throws ConversionException {
        String doubleString = editText.val().trim();
        if (!doubleString.matches(REGEX_DECIMAL)) {
            String message = "Expected a floating point number, but was " + doubleString;
            throw new ConversionException(message);
        }

        return Double.parseDouble(doubleString);
    }
}
