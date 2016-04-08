package org.lirazs.gbackbone.validation.test.client.model;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.validation.client.ModelValidator;
import org.lirazs.gbackbone.validation.client.annotation.ConfirmEmail;
import org.lirazs.gbackbone.validation.client.annotation.Email;
import org.lirazs.gbackbone.validation.client.annotation.Required;

/**
 * Created on 17/03/2016.
 */
public class TestFailedDataModel extends Model {

    @Email
    @Required
    private String email;

    @ConfirmEmail
    @Required
    private String confirmEmail;

    public TestFailedDataModel() {
        super();

        new ModelValidator(this);
        set("email", "aaa@aaa.com");
    }
}
