package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class GQueryFloatAdapter implements TargetDataAdapter<GQuery, Float> {
    private static final String REGEX_DECIMAL = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    @Override
    public Float getData(final GQuery gQueryInput, String attribute) throws ConversionException {
        String floatString = gQueryInput.val().trim();
        if (!floatString.matches(REGEX_DECIMAL)) {
            String message = "Expected a floating point number, but was " + floatString;
            throw new ConversionException(message);
        }

        return Float.parseFloat(floatString);
    }
}
