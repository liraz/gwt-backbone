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
                c().putNumber(length(), (((Number) t).doubleValue()));
            } else if (t instanceof Boolean) {
                c().putBoolean(length(), ((Boolean) t).booleanValue());
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

    public T get(int index) {
        return (T)c().get(index);
    }

    public final int getInt(int index) {
        Integer r = get(index, Integer.class);
        return r == null ? 0 : r;
    }

    public final <T> T get(int index, Class<? extends T> clz) {
        Object o = get(index);
        if (o != null && clz != null) {
            if (o instanceof Double) {
                Double d = (Double) o;
                if (clz == Float.class)
                    o = d.floatValue();
                else if (clz == Integer.class)
                    o = d.intValue();
                else if (clz == Long.class)
                    o = d.longValue();
                else if (clz == Short.class)
                    o = d.shortValue();
                else if (clz == Byte.class)
                    o = d.byteValue();
            } else if (clz == Boolean.class && !(o instanceof Boolean)) {
                o = Boolean.valueOf(String.valueOf(o));
            } else if (clz == String.class && !(o instanceof String)) {
                o = String.valueOf(o);
            }
        }
        return (T) o;
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
        return arrayContains(c(), o);
    }

    /**
     * Needed for IE8 which doesn't have a indexOf method
     */
    private native boolean arrayContains(JavaScriptObject cache, Object o) /*-{
        if (!Array.prototype.indexOf) {
            Array.prototype.indexOf = function(elt) {
                var len = this.length >>> 0;

                var from = Number(arguments[1]) || 0;
                from = (from < 0)
                    ? Math.ceil(from)
                    : Math.floor(from);
                if (from < 0)
                    from += len;

                for (; from < len; from++) {
                    if (from in this &&
                        this[from] === elt)
                        return from;
                }
                return -1;
            };
        }
        return cache.indexOf(o) >= 0;
    }-*/;

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

