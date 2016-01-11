package org.lirazs.gbackbone.client.core.test.view;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.annotation.InjectModel;
import org.lirazs.gbackbone.client.core.annotation.InjectView;
import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.view.View;

/**
 * Created on 12/12/2015.
 */
@ViewTemplate(filePath = "CustomerRegisterView.ejs")
public class AnnotatedModelInjectedView extends View {

    @InjectModel
    public String email; // injected from model, field name is taken as property name

    @InjectView
    public InputElement emailInput;

    public AnnotatedModelInjectedView(Options options) {
        super(options);
    }

    @Override
    public View render() {
        emailInput.setValue(email);

        return this;
    }
}
