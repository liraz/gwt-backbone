package org.lirazs.gbackbone.client.core.test.model;

import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 12/11/2015.
 */
public class PrefixedModel extends Model {
    public PrefixedModel() {
        super();
    }

    public PrefixedModel(Options attributes) {
        super(attributes);
    }

    @Override
    protected String getCidPrefix() {
        return "m";
    }
}
