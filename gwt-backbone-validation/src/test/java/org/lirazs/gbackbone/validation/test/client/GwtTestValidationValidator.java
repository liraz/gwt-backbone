package org.lirazs.gbackbone.validation.test.client;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.view.TemplateFactory;
import org.lirazs.gbackbone.validation.test.client.view.TestFormView;

import java.util.HashMap;

/**
 * Created on 23/10/2015.
 */
public class GwtTestValidationValidator extends GWTTestCase {

    public String getModuleName() {
        return "org.lirazs.gbackbone.validation.test.ValidationTest";
    }

    public void gwtSetUp() {
    }

    public void gwtTearDown() {

    }

    public void testValidationView() {
        // on IE8 the test is failing on "$oldEl.click();" but the code works on browser. (still a GQuery bug in this case)
        if(GQuery.browser.ie8)
            return;
        //TODO: HTMLUnit does not initialize the DomImpl for IE9 & IE10, causing all GQuery functionality to fail
        if((GQuery.browser.ie9 || GQuery.browser.msie) && !GQuery.browser.ie8)
            return;

        delayTestFinish(5000);

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("urlRoot", "https://raw.githubusercontent.com/liraz/gwt-backbone/master/gwt-backbone-core/src/test/resources/com/lirazs/gbackbone/client/core/test/template/");
        TemplateFactory.templateSettings(templateSettings);

        final TestFormView testFormView = new TestFormView();

        final Events events = new Events();
        events.listenToOnce(testFormView, "template:complete", new Function() {
            @Override
            public void f() {

                // trigger the validator using a click on the form title..
                GQuery.$(testFormView.formTitle).click();

                finishTest();
            }
        });
    }

    public void testValidationViewWithModel() {
        //TODO: create the test
    }
}
