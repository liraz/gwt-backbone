package org.lirazs.gbackbone.validation.client.adapter;

import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 12/02/2016.
 */
public class ModelBooleanAdapter implements TargetDataAdapter<Model, Boolean> {

    @Override
    public Boolean getData(final Model model, String attribute) {
        return model.getBoolean(attribute);
    }
}
