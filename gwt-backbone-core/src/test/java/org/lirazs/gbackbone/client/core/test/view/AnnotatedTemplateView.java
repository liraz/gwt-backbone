package org.lirazs.gbackbone.client.core.test.view;

import org.lirazs.gbackbone.client.core.annotation.ViewTemplate;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.view.View;

/**
 * Created on 12/12/2015.
 */
@ViewTemplate("<%= value %>")
public class AnnotatedTemplateView extends View {

    public AnnotatedTemplateView(Options options) {
        super(options);
    }
}
