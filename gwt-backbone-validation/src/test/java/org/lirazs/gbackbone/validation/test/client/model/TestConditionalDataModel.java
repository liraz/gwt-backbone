package org.lirazs.gbackbone.validation.test.client.model;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.validation.client.ModelValidator;
import org.lirazs.gbackbone.validation.client.annotation.*;

/**
 * Created on 17/03/2016.
 */
public class TestConditionalDataModel extends Model {

    @RequiredIfAttributeNotEmpty(attribute = {"address1", "country", "city"})
    private String city;

    @Length(max = 16)
    private String address1;

    @RequiredIfAttributeNotEmpty(attribute = {"address1", "country", "city"})
    private String address2;

    @RequiredIfAttributeNotEmpty(attribute = {"address1", "country", "city"})
    private String country;

    public TestConditionalDataModel() {
        super();

        new ModelValidator(this);
    }
}
