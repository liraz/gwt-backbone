package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public class GQueryIntegerAdapter implements TargetDataAdapter<GQuery, Integer> {
    private static final String REGEX_INTEGER = "\\d+";

    @Override
    public Integer getData(final GQuery gQueryInput, String attribute) throws ConversionException {
        String integerString = gQueryInput.val().trim();
        if (!integerString.matches(REGEX_INTEGER)) {
            String message = "Expected an integer, but was " + integerString;
            throw new ConversionException(message);
        }

        return Integer.parseInt(integerString);
    }
}
