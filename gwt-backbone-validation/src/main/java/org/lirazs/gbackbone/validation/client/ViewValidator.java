package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.view.View;

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
    }
}
