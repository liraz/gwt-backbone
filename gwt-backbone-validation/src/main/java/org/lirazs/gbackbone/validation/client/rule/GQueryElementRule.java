package org.lirazs.gbackbone.validation.client.rule;

import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.validation.Rule;

/**
 * Created on 05/02/2016.
 */
public interface GQueryElementRule extends Rule<GQuery> {

    /**
     * Checks if the rule is valid.
     *
     * @param element  The {@link GQuery} on which the rule has to be applied.
     *
     * @return true if valid, false otherwise.
     */
    boolean isValid(GQuery element);
}
