package org.lirazs.gbackbone.validation.client.adapter;

import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 12/02/2016.
 */
public class ModelStringAdapter implements TargetDataAdapter<Model, String> {

    @Override
    public String getData(final Model model, String attribute) {
        return model.get(attribute);
    }
}
