package org.lirazs.gbackbone.client.core.model.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 04/12/2015.
 */
public abstract class OnChangeAttrFunction<V> extends Function {

    @Override
    public void f() {
        f((Model)getArgument(0), (V)getArgument(1), (Options)getArgument(2));
    }

    abstract void f(Model model, V value, Options options);
}
