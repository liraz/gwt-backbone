package org.lirazs.gbackbone.client.core.navigation.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.navigation.Router;

/**
 * Created on 04/12/2015.
 */
public abstract class OnRouteFunction extends Function {

    @Override
    public void f() {
        f((Router)getArgument(0), (String)getArgument(1), (String[])getArgument(2));
    }

    public abstract void f(Router router, String route, String[] args);
}
