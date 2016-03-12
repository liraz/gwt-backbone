package org.lirazs.gbackbone.validation.test.client.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.annotation.EventHandler;
import org.lirazs.gbackbone.client.core.annotation.InjectView;
import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.validation.ValidationError;
import org.lirazs.gbackbone.client.core.view.View;
import org.lirazs.gbackbone.validation.client.ErrorClassTargetValidatedAction;
import org.lirazs.gbackbone.validation.client.ValidationListener;
import org.lirazs.gbackbone.validation.client.ViewValidator;
import org.lirazs.gbackbone.validation.client.annotation.*;

import java.util.List;

/**
 * Created on 05/03/2016.
 */
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class TestFormView extends View implements ValidationListener {

    @Email
    @Required
    @InjectView
    public InputElement emailInput;

    @ConfirmEmail
    @Required
    @InjectView("#emailRetypeSpecialId")
    public InputElement emailRetypeInput;

    @Pattern(regex = "^\\+?[0-9. ()-]{10,25}$", message = "Phone Number must be in the form XXX-XXXXXXX")
    @Required
    @InjectView(".phoneInputClass")
    public InputElement phoneNumberInput;

    @Length(min = 1, max = 5, message = "Address must be less than 5 characters.")
    @Required
    @InjectView(".addressInputAsGQuery")
    public GQuery addressInput;

    @Required
    @InjectView
    public GQuery cityInput;

    @InjectView
    public SpanElement formTitle;

    @InjectView(".bottomTitle")
    public GQuery formBottomTitle;

    private ViewValidator validator;

    public TestFormView() {
        super();

        validator = new ViewValidator(this);
        validator.setTargetValidatedAction(new ErrorClassTargetValidatedAction());

        //TODO: Support for conditional validation
        /*validator.setExpression(cityInput, new RequiredIfExpression() {
            public boolean execute(GQuery target, View controller, Object value) {
                Model model = controller.getModel();

                String valueStr = addressInput.val();
                return !valueStr.isEmpty();
            }
        });*/
    }

    @Override
    public View render() {
        formTitle.setInnerText("Title of Form");
        formBottomTitle.text("bottom title from GQuery");

        emailInput.setValue("myemail@test.com");
        emailRetypeInput.setValue("myemail@test.com");
        phoneNumberInput.setValue("2321321323");
        addressInput.val("Address 1");
        cityInput.val("The Big City");

        return this;
    }

    @EventHandler("click #formTitle")
    public void onClickTitle() {
        validator.validate();
    }

    @Override
    public void onValidationSucceeded() {
        formBottomTitle.text("Validation succeeded");
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        formBottomTitle.text("Validation failed");

        for (ValidationError error : errors) {
            Object target = error.getTarget();
            if(target instanceof GQuery) {
                ((GQuery) target).addClass("ERROR");
            } else if(target instanceof Element) {
                ((Element) target).addClassName("ERROR");
            }
        }
    }
}
