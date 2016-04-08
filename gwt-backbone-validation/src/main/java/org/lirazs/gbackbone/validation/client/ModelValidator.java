package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.util.StringUtils;
import org.lirazs.gbackbone.client.core.view.View;
import org.lirazs.gbackbone.reflection.client.Field;
import org.lirazs.gbackbone.validation.client.adapter.ModelBooleanAdapter;
import org.lirazs.gbackbone.validation.client.adapter.ModelDoubleAdapter;
import org.lirazs.gbackbone.validation.client.adapter.ModelIntegerAdapter;
import org.lirazs.gbackbone.validation.client.adapter.ModelStringAdapter;
import org.lirazs.gbackbone.validation.client.annotation.*;

import java.util.List;

/**
 * Created on 05/03/2016.
 */
public class ModelValidator extends AnnotationValidator {

    /**
     * Constructor.
     *
     * @param controller The class containing targets to be validated.
     */
    public ModelValidator(Model controller) {
        super(controller);

        // make the model class use this validator as it's internal validation mechanism
        controller.registerValidator(this);

        registerTargetAdapters();

        // fill up the model with all annotated fields attribute names
        List<Field> validationAnnotatedFields = getValidationAnnotatedFields(getController().getClass());
        for (Field validationAnnotatedField : validationAnnotatedFields) {
            String attribute = getAttribute(validationAnnotatedField);
            Model model = (Model) getController();

            // make sure model has this attribute in it's attributes map with a default value (null)
            if(!model.has(attribute)) {
                model.set(attribute, null);
            }
        }
    }

    protected boolean eligibleSuperClassTargetFieldsScan(Class<?> superClass) {
        return super.eligibleSuperClassTargetFieldsScan(superClass) && !superClass.equals(Model.class);
    }

    protected Object getTarget(final Field field) {
        return getController(); // model for all fields is the target with Model Validator
    }

    private void registerTargetAdapters() {
        register(Model.class, /*Boolean.class,*/
                new ModelBooleanAdapter(),
                AssertFalse.class, AssertTrue.class, Checked.class);

        register(Model.class, /*Integer.class,*/
                new ModelIntegerAdapter(),
                Select.class);

        register(Model.class, /*Double.class,*/
                new ModelDoubleAdapter(),
                DecimalMax.class, DecimalMin.class);

        register(Model.class, /*Integer.class,*/
                new ModelIntegerAdapter(),
                Max.class, Min.class);

        register(Model.class, /*String.class,*/
                new ModelStringAdapter(),
                ConfirmEmail.class, ConfirmPassword.class, CreditCard.class,
                Digits.class, Domain.class, Email.class, FutureDate.class,
                IpAddress.class, Isbn.class, Length.class, Required.class, RequiredIfAttributeNotEmpty.class,
                Password.class, PastDate.class, Pattern.class, Url.class);
    }
}
