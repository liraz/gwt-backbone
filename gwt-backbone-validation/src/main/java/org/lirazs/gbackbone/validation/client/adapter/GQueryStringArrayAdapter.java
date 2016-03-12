package org.lirazs.gbackbone.validation.client.adapter;

import com.google.gwt.query.client.GQuery;

/**
 * Created on 12/02/2016.
 */
public class GQueryStringArrayAdapter implements TargetDataAdapter<GQuery, String[]> {

    @Override
    public String[] getData(final GQuery gqueryInput) {
        return gqueryInput.vals();
    }
}
