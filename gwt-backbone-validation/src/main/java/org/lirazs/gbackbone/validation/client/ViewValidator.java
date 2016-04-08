package org.lirazs.gbackbone.validation.client;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.view.View;
import org.lirazs.gbackbone.validation.client.adapter.*;
import org.lirazs.gbackbone.validation.client.annotation.*;

/**
 * Created on 05/03/2016.
 */
public class ViewValidator extends AnnotationValidator {
    /**
     * Constructor.
     *
     * @param controller The class containing targets to be validated.
     */
    public ViewValidator(View controller) {
        super(controller);

        Model model = controller.getModel();
        if(model != null) {
            model.registerValidator(this);
        }

        registerTargetAdapters();
    }

    protected boolean eligibleSuperClassTargetFieldsScan(Class<?> superClass) {
        return super.eligibleSuperClassTargetFieldsScan(superClass) && !superClass.equals(View.class);
    }

    private void registerTargetAdapters() {
        // CheckBoxBooleanAdapter
        register(GQuery.class, /*Boolean.class,*/
                new GQueryBooleanAdapter(),
                AssertFalse.class, AssertTrue.class, Checked.class);
        register(InputElement.class, /*Boolean.class,*/
                new InputElementBooleanAdapter(),
                AssertFalse.class, AssertTrue.class, Checked.class);

        // SpinnerIndexAdapter
        register(GQuery.class, /*Integer.class,*/
                new GQueryIntegerAdapter(),
                Select.class);
        register(SelectElement.class, /*Integer.class,*/
                new SelectElementIntegerAdapter(),
                Select.class);

        // TextViewDoubleAdapter
        register(GQuery.class, /*Double.class,*/
                new GQueryDoubleAdapter(),
                DecimalMax.class, DecimalMin.class);
        register(InputElement.class, /*Double.class,*/
                new InputElementDoubleAdapter(),
                DecimalMax.class, DecimalMin.class);

        // TextViewIntegerAdapter
        register(GQuery.class, /*Integer.class,*/
                new GQueryIntegerAdapter(),
                Max.class, Min.class);
        register(InputElement.class, /*Integer.class,*/
                new InputElementIntegerAdapter(),
                Max.class, Min.class);

        // TextViewStringAdapter
        register(GQuery.class, /*String.class,*/
                new GQueryStringAdapter(),
                ConfirmEmail.class, ConfirmPassword.class, CreditCard.class,
                Digits.class, Domain.class, Email.class, FutureDate.class,
                IpAddress.class, Isbn.class, Length.class, Required.class, RequiredIfAttributeNotEmpty.class,
                Password.class, PastDate.class, Pattern.class, Url.class);
        register(InputElement.class, /*String.class,*/
                new InputElementStringAdapter(),
                ConfirmEmail.class, ConfirmPassword.class, CreditCard.class,
                Digits.class, Domain.class, Email.class, FutureDate.class,
                IpAddress.class, Isbn.class, Length.class, Required.class, RequiredIfAttributeNotEmpty.class,
                Password.class, PastDate.class, Pattern.class, Url.class);
    }
}
