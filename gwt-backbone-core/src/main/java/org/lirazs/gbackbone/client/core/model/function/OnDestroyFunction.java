package org.lirazs.gbackbone.client.core.model.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 04/12/2015.
 */
public abstract class OnDestroyFunction extends Function {

    @Override
    public void f() {
        f((Model)getArgument(0), (Collection)getArgument(1), (Options)getArgument(2));
    }

    public abstract void f(Model model, Collection collection, Options options);
}
