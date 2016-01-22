package org.lirazs.gbackbone.client.core.net;

import com.google.gwt.query.client.Promise;
import org.lirazs.gbackbone.client.core.data.Options;

/**
 * Created on 21/01/2016.
 */
public interface SyncStrategy {

    Promise sync(String method, Synchronized model, Options options);
}
