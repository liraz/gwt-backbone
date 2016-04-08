package org.lirazs.gbackbone.validation.client.adapter;

import org.lirazs.gbackbone.validation.client.exception.ConversionException;

/**
 * Created on 12/02/2016.
 */
public interface TargetDataAdapter<TARGET, DATA> {

    /**
     * Extract and return the appropriate data from a given target.
     *
     * @param target  The target from which contains the data that we are
     *      interested in.
     *
     * @param attribute
     * @return The interested data.
     *
     * @throws ConversionException If the adapter is unable to convert the data to the expected
     *      data type.
     */
    DATA getData(TARGET target, String attribute) throws ConversionException;
}
