package org.lirazs.gbackbone.validation.client.rule;

import com.google.gwt.dom.client.Element;
import org.lirazs.gbackbone.client.core.validation.Rule;

/**
 * Created on 05/02/2016.
 */
public interface ElementRule<E extends Element> extends Rule<E> {

    /**
     * Checks if the rule is valid.
     *
     * @param element  The {@link Element} on which the rule has to be applied.
     *
     * @return true if valid, false otherwise.
     */
    boolean isValid(E element);
}
