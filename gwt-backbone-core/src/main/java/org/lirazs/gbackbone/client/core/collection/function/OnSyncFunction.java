package org.lirazs.gbackbone.client.core.collection.function;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 04/12/2015.
 */
public abstract class OnSyncFunction extends Function {

    @Override
    public void f() {
        f((Collection)getArgument(0), (JSONValue)getArgument(1), (Options)getArgument(1));
    }

    abstract void f(Collection collection, JSONValue response, Options options);
}
