package org.lirazs.gbackbone.client.core.test.view;

import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.view.View;

/**
 * Created on 12/12/2015.
 */
@ViewTemplate(filePath = "people.ejs")
public class AsyncAnnotatedTemplateView extends View {

    public AsyncAnnotatedTemplateView(Options options) {
        super(options);
    }
}
