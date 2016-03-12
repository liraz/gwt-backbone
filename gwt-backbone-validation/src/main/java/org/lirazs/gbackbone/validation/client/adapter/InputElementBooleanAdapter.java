package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.InputElement;

/**
 * Created on 12/02/2016.
 */
public class InputElementBooleanAdapter implements TargetDataAdapter<InputElement, Boolean> {

    @Override
    public Boolean getData(final InputElement input) {
        return input.isChecked();
    }
}
