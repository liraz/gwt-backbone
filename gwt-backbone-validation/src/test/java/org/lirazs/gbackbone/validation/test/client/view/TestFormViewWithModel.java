package org.lirazs.gbackbone.validation.test.client.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.annotation.EventHandler;
import org.lirazs.gbackbone.client.core.annotation.InjectView;
import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.validation.ValidationError;
import org.lirazs.gbackbone.client.core.view.View;
import org.lirazs.gbackbone.validation.client.ErrorClassTargetValidatedAction;
import org.lirazs.gbackbone.validation.client.ValidationListener;
import org.lirazs.gbackbone.validation.client.ViewValidator;
import org.lirazs.gbackbone.validation.client.annotation.*;
import org.lirazs.gbackbone.validation.test.client.model.TestDataModel;

import java.util.List;

/**
 * Created on 05/03/2016.
 */
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class TestFormViewWithModel extends View implements ValidationListener {

    @InjectView
    public InputElement emailInput;

    @InjectView("#emailRetypeSpecialId")
    public InputElement emailRetypeInput;

    private ViewValidator validator;

    public TestFormViewWithModel(TestDataModel model) {
        super(new Options("model", model));
    }

    @Override
    public View render() {
        emailInput.setValue("myemail@test.com");
        emailRetypeInput.setValue("myemail@test.com");

        return this;
    }

    @Override
    public void onValidationSucceeded() {

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {

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
