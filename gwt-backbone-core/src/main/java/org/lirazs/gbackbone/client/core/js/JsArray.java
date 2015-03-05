/*
 * Copyright 2015, Liraz Shilkrot
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
import com.google.gwt.query.client.js.JsCache;

import java.util.Comparator;

public final class JsArray<T> extends JavaScriptObject {

    public static <T> JsArray<T> create() {
        return JavaScriptObject.createArray().cast();
    }

    protected JsArray() {
    }

    private JsCache c() {
        return cast();
    }

    public JsArray<T> add(T...vals) {
        for (T t: vals) {
            if (t instanceof Number) {
                c().putNumber(length(), (((Number)t).doubleValue()));
            } else if (t instanceof Boolean) {
                c().putBoolean(length(), ((Boolean)t).booleanValue());
            } else {
                c().put(length(), t);
            }
        }
        return this;
    }

    public JsArray<T> add(int i, T val) {
        c().put(i, val);
        return this;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T)c().get(index);
    }

    public int length() {
        return c().length();
    }

    public void set(int i, T val) {
        c().put(i, val);
    }

    public void concat(JsArray<T> ary) {
        c().concat(ary);
    }

    public void pushAll(JavaScriptObject prevElem) {
        c().pushAll(prevElem);
    }

    public void removeAll() {
        c().clear();
    }

    public boolean contains(Object o) {
        return c().contains(o);
    }

    public void remove(Object... objects) {
        for (Object o : objects) {
            c().remove(o);
        }
    }

    public Object[] elements() {
        return c().elements();
    }

    public final native String toJsonString() /*-{
        return JSON.stringify(this);
    }-*/;

    public final native void sort(Comparator<? extends T> comparator) /*-{
        this.sort(function (a, b) {
            return comparator.@java.util.Comparator::compare(Ljava/lang/Object;Ljava/lang/Object;)(a, b);
        });
    }-*/;

    public native T[] slice(int ...args) /*-{
        return Array.prototype.slice.apply(this, arguments);
    }-*/;

    public native void addAt(JavaScriptObject objects, int index) /*-{
        Array.prototype.splice.apply(this, [index, 0].concat(objects));
    }-*/;


    public native void removeAt(int index) /*-{
        this.splice(index, 1);
    }-*/;

    public native void unshift(T object) /*-{
        this.unshift(object);
    }-*/;

    public native String join(String separator) /*-{
        return this.join(separator);
    }-*/;
}

