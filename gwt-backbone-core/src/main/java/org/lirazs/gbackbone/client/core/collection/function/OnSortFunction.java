package org.lirazs.gbackbone.client.core.collection.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;

/**
 * Created on 04/12/2015.
 */
public abstract class OnSortFunction extends Function {

    @Override
    public void f() {
        f((Collection)getArgument(0), (Options)getArgument(1));
    }

    abstract void f(Collection collection, Options options);
}
