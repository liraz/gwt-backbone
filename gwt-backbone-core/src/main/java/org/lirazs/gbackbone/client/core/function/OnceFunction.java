package org.lirazs.gbackbone.client.core.function;

import com.google.gwt.query.client.Function;

/**
 * Created on 11/12/2015.
 */
public abstract class OnceFunction extends Function {
    private boolean blocked = false;

    private String name;
    private Function callback;

    public OnceFunction(String name, Function callback) {
        this.name = name;
        this.callback = callback;
    }

    public String getName() {
        return name;
    }

    public Function getCallback() {
        return callback;
    }

    @Override
    public void f() {
        if (!blocked) {
            once();
        }
        blocked = true;
    }

    public abstract void once();
}
