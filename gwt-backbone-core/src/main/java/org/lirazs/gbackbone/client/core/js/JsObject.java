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


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.query.client.Properties;

import java.util.Iterator;

/**
 * A Lightweight JSO class to store data.
 */
public class JsObject extends JavaScriptObject {

    protected JsObject() {
    }

    public static JsObject create() {
        return JavaScriptObject.createObject().cast();
    }
    public static JsObject create(Object ...keyValue) {
        JsObject obj = JsObject.create();

        for(int i = 0; i < keyValue.length && (i + 1) < keyValue.length; i = i + 2) {
            obj.put(keyValue[i], keyValue[i + 1]);
        }
        return obj;
    }

    // for compatibility with GQuery
    public final Properties toProperties() {
        Properties props = Properties.create();
        for (String k : keys()) {
            props.set(k, get(k));
        }

        return props;
    }

    public final native void concat(Object ary) /*-{
        if (ary) this.concat(ary);
    }-*/;



    public final JsObject extend(JsObject jso) {
        if (jso != null) {
            for (String k : jso.keys()) {
                put(k, jso.get(k));
            }
        }
        return this;
    }

    public final native JsObject clone() /*-{
        if (!typeof this === 'function' || typeof this === 'object' && !!this) return this;

        var copy = {};
        for (var prop in this) {
            if (hasOwnProperty.call(this, prop)) {
                copy[prop] = this[prop];
            }
        }
        return copy;
    }-*/;

    public final void pushAll(JavaScriptObject prevElem) {
        checkNull();
        JsObject c = prevElem.cast();
        for (int i = 0, ilen = c.length(); i < ilen; i++) {
            put(length(), c.get(i));
        }
    }

    public final native void delete(Object name) /*-{
        delete this[name];
    }-*/;

    public final void clear() {
        for (String k : keys()) {
            delete(k);
        }
    }

    public final native boolean exists(Object name) /*-{
        return !!this[name];
    }-*/;

    @SuppressWarnings("unchecked")
    public final <T> T get(Object id, Class<? extends T> clz) {
        Object o = get(id);
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

    public final native <T> T get(Object id) /*-{
        return @org.lirazs.gbackbone.client.core.js.JsObject::gwtBox(Lcom/google/gwt/core/client/JavaScriptObject;)([ this && this[id] ]);
    }-*/;

    public final JsObject getJsObject(Object id) {
        return (JsObject) get(id);
    }
    public final JsObject getJsObject(int id) {
        return (JsObject) get(id);
    }

    public final boolean getBoolean(Object id) {
        Boolean r = get(id, Boolean.class);
        return r == null ? false : r;
    }

    public final float getFloat(Object id) {
        Float r = get(id, Float.class);
        return r == null ? 0 : r;
    }

    public final double getDouble(Object id) {
        Double r = get(id, Double.class);
        return r == null ? 0 : r;
    }

    public final int getInt(Object id) {
        Integer r = get(id, Integer.class);
        return r == null ? 0 : r;
    }

    public final native String getString(Object id) /*-{
        return this[id] == null ? null : String(this[id]);
    }-*/;

    public final native JsArrayMixed getArray(Object id) /*-{
        var r = this[id];
        if (Object.prototype.toString.call(r) == '[object Array]') {
            return r;
        }
        return null;
    }-*/;

    public final <T extends JavaScriptObject> T getJavaScriptObject(Object name) {
        Object o = get(name);
        return (o != null && o instanceof JavaScriptObject) ? ((JavaScriptObject) o).<T> cast() : null;
    }

    public final native boolean isEmpty() /*-{
        for (k in this) {
            if (this.hasOwnProperty(k))
                return false;
        }
        return true;
    }-*/;

    public final native void remove(String o) /*-{
        this[o] = null;
        delete this[o];
    }-*/;

    public final native int indexOf(Object o) /*-{
        // HtmlUnit fails when this returns 0
        return this.indexOf(o);
    }-*/;

    public final native JsObject putBoolean(Object id, boolean b) /*-{
        this[id] = b;
        return this;
    }-*/;

    public final native JsObject putNumber(Object id, double n) /*-{
        this[id] = n;
        return this;
    }-*/;

    public final JsObject put(Object id, Object obj) {
        if (obj instanceof Boolean) {
            putBoolean(id, ((Boolean) obj).booleanValue());
        } else if (obj instanceof Number) {
            putNumber(id, ((Number) obj).doubleValue());
        } else {
            putObject(id, obj);
        }
        return this;
    }

    public final native JsObject putObject(Object id, Object obj) /*-{
        this[id] = obj;
        return this;
    }-*/;

    public final native int length() /*-{
        if (typeof(this.length) == 'number')
            return this.length;
        var key, ret = 0;
        // Chrome in DevMode injects a property to JS objects
        for (key in this) {
            if (key != "__gwt_ObjectId")
                ret ++;
        }
        return ret;
    }-*/;

    public final int[] indexes() {
        checkNull();
        JsArrayString a = keysImpl();
        int[] ret = new int[a.length()];
        for (int i = 0; i < a.length(); i++) {
            try {
                ret[i] = Integer.valueOf(a.get(i));
            } catch (Exception e) {
                ret[i] = i;
            }
        }
        return ret;
    }

    public final String[] keys() {
        checkNull();
        JsArrayString a = keysImpl();
        String[] ret = new String[a.length()];
        for (int i = 0; i < a.length(); i++) {
            ret[i] = a.get(i);
        }
        return ret;
    }

    public final Object[] elements() {
        String[] keys = keys();
        Object[] ret = new Object[keys.length];
        int i = 0;
        for (String s : keys) {
            ret[i++] = get(s);
        }
        return ret;
    }



    public final String toObjectString() {
        String ret = getClass().getName() + "{ ";
        for (String k : keys()) {
            ret += k + "=" + get(k) + " ";
        }
        return ret + "}";
    }

    // In dev-mode a null object casted to JavascriptObject does not throw a NPE
    public final void checkNull() {
        checkNull(this);
    }

    public final native String toJsonString() /*-{
        return JSON.stringify(this);
    }-*/;

    private native JsArrayString keysImpl() /*-{
        var key, keys=[];
        // Chrome in DevMode sets '__gwt_ObjectId' to JS objects
        // GWT sets '$H' when calling getHashCode (see com/google/gwt/core/client/impl/Impl.java)
        for(key in this) {
            if (this.hasOwnProperty(key) && key != '__gwt_ObjectId' && key != '$H')
                keys.push(String(key));
        }
        return keys;
    }-*/;

    /**
     * Throw a NPE when a js is null.
     */
    public static final <T extends JavaScriptObject> T checkNull(T js) {
        if (!GWT.isProdMode() && js == null) {
            throw new NullPointerException();
        }
        return js;
    }

    /**
     * Gets an object wrapped in a js array and boxes it with the appropriate
     * object in the GWT world.
     *
     * It is thought to be called from other jsni code without dealing with casting issues.
     *
     * It will box the unique element in the array with a Boolean or a Double in the case
     * of primitive variables, otherwise it returns the object itself, or null if undefined.
     *
     * Example
     * <pre>
     * native Object myMethod() /*-{
     *   var myJsVar = ... ;
     *   return @org.thisdotshare.gbackbone.core.js.JsCache::gwtBox(*)([ myJsVar ]);
     * }-* /
     * </pre>
     *
     */
    public static native Object gwtBox(JavaScriptObject oneElementArray) /*-{
        var r = oneElementArray;
        if (typeof r == 'object' && r.length == 1) {
            var r = r[0], t = typeof r;
            if (t == 'boolean') return @java.lang.Boolean::valueOf(Z)(r);
            if (t == 'number')  return @java.lang.Double::valueOf(D)(r);
        }
        return r || null;
    }-*/;
}
