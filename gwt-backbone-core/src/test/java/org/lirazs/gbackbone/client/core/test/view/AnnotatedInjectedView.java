package org.lirazs.gbackbone.client.core.test.view;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.annotation.InjectView;
import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.view.View;

/**
 * Created on 12/12/2015.
 */
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class AnnotatedInjectedView extends View {

    @InjectView
    public InputElement emailInput;

    @InjectView("#emailRetypeSpecialId")
    public InputElement emailRetypeInput;

    @InjectView(".phoneInputClass")
    public InputElement phoneNumberInput;

    @InjectView(".addressInputAsGQuery")
    public GQuery addressInput;

    @InjectView
    public GQuery cityInput;

    @InjectView
    public SpanElement formTitle;

    // injectable fields must be public or have a setter at least
    @InjectView(".bottomTitle")
    private GQuery formBottomTitle;


    public AnnotatedInjectedView() {
        super();
    }

    public GQuery getFormBottomTitle() {
        return formBottomTitle;
    }

    public void setFormBottomTitle(GQuery formBottomTitle) {
        this.formBottomTitle = formBottomTitle;
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
}
