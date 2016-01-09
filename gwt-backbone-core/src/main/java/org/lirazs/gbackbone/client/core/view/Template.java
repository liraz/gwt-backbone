package org.lirazs.gbackbone.client.core.view;

import org.lirazs.gbackbone.client.core.data.Options;

/**
 * Created on 05/01/2016.
 */
public interface Template {

    String apply();

    String apply(Options attributes);
}
