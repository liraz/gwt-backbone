package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.InputElement;

/**
 * Created on 12/02/2016.
 */
public class InputElementStringAdapter implements TargetDataAdapter<InputElement, String> {

    @Override
    public String getData(final InputElement input, String attribute) {
        return input.getValue();
    }
}
