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
@ViewTemplate(filePath = "CustomerRegisterView.ejs", autoRender = false)
public class CustomRenderAnnotatedInjectedView extends View {

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

    @InjectView(".bottomTitle")
    public GQuery formBottomTitle;


    public CustomRenderAnnotatedInjectedView() {
        super();
    }

    @Override
    public View render() {
        // do rendering in a custom way..
        get$El().html(getTemplate().apply());
        // inject all views from the annotations
        injectViews();

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
