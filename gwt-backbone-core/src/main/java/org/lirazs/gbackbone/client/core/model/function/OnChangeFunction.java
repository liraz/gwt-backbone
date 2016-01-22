package org.lirazs.gbackbone.client.core.model.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 04/12/2015.
 */
public abstract class OnChangeFunction extends Function {

    @Override
    public void f() {
        f((Model)getArgument(0), (Options)getArgument(1));
    }

    public abstract void f(Model model, Options options);
}
