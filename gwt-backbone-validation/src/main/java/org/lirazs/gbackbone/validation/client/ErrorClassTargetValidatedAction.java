package org.lirazs.gbackbone.validation.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.GQuery;

/**
 * Created on 26/02/2016.
 */
public class ErrorClassTargetValidatedAction implements TargetValidatedAction {

    @Override
    public void onAllRulesPassed(final Object target) {
        if(target instanceof GQuery) {
            ((GQuery) target).removeClass("ERROR");
        } else if(target instanceof Element) {
            ((Element) target).removeClassName("ERROR");
        }
    }
}
