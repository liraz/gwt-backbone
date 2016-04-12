/*
 * Copyright 2016, Liraz Shilkrot
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.lirazs.gbackbone.client.core.js;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.js.JsCache;
import com.google.gwt.query.client.js.JsUtils;

/**
 * Lightweight JSO backed implemented of a Map.
 *
 * @param <S>
 * @param <T>
 */
public final class JsMap<S, T> extends JavaScriptObject {

    protected JsMap() {
    }

    private JsCache c() {
        return cast();
    }

    public T get(S key) {
        return c().get(String.valueOf(key));
    }

    public final boolean hasFunction(S key) {
        final Object o = c().get(String.valueOf(key));
        if (o != null) {
            if (o instanceof Function) {
                return true;
            } else if (o instanceof JavaScriptObject) {
                Object f = ((JavaScriptObject) o).<JsMap> cast().getObject("__f");
                if (f != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public final Function getFunction(S key) {
        final Object o = c().get(String.valueOf(key));
        if (o != null) {
            if (o instanceof Function) {
                return (Function) o;
            } else if (o instanceof JavaScriptObject) {
                Object f = ((JavaScriptObject) o).<JsMap> cast().getObject("__f");
                if (f != null && f instanceof Function) {
                    return (Function) f;
                }
                return new JsUtils.JsFunction((JavaScriptObject) o);
            }
        }
        return null;
    }

    protected final Object getObject(Object name) {
        return c().get(String.valueOf(name));
    }

    public void put(S key, T val) {
        c().put(String.valueOf(key), val);
    }

    /**
     * Adds a new native js function to the properties object.
     * This native function will wrap the passed java Function.
     *
     * Its useful for exporting or importing to javascript.
     *
     */
    public final native void putFunction(S key, Function f) /*-{
        if (!f) return;
        this[key] = function() {
            f.@com.google.gwt.query.client.Function::fe(Ljava/lang/Object;)(arguments);
        }
        // We store the original function reference
        this[key].__f = f;
    }-*/;

    public T remove(S key) {
        T old = get(key);
        c().delete(key);
        return old;
    }

    public String[] keys() {
        return c().keys();
    }

    public static <S, T> JsMap<S, T> create() {
        return createObject().cast();
    }
}
