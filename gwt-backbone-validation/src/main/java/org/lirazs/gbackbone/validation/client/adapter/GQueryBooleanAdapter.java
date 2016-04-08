package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.query.client.GQuery;

/**
 * Created on 12/02/2016.
 */
public class GQueryBooleanAdapter implements TargetDataAdapter<GQuery, Boolean> {

    @Override
    public Boolean getData(final GQuery gqueryInput, String attribute) {
        return gqueryInput.is(":checked");
    }
}
