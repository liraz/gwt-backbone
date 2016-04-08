package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.dom.client.SelectElement;

/**
 * Created on 12/02/2016.
 */
public class SelectElementStringAdapter implements TargetDataAdapter<SelectElement, String> {

    @Override
    public String getData(final SelectElement gqueryInput, String attribute) {
        return gqueryInput.getValue();
    }
}
