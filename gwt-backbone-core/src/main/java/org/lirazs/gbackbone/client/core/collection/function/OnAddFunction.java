package org.lirazs.gbackbone.client.core.collection.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 04/12/2015.
 */
public abstract class OnAddFunction<M extends Model> extends Function {

    @Override
    public void f() {
        f((M)getArgument(1), (Collection)getArgument(2), (Options)getArgument(3));
    }

    public abstract void f(M model, Collection collection, Options options);
}
