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
package org.lirazs.gbackbone.client.core.data;

import com.google.gwt.json.client.*;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Properties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class Options extends LinkedHashMap<String, Object> implements JsonSerializable {

    public static Options O() {
        return new Options();
    }
    public static Options O(JSONValue jsonObject) {
        return new Options(jsonObject);
    }
    public static Options O(Object ...keyValue) {
        return new Options(keyValue);
    }

    public Options() {
        super();
    }

    public Options(JSONValue jsonObject) {
        JSONObject object = jsonObject.isObject();

        if (object != null) {
            for (String s : object.keySet()) {
                JSONValue jsonValue = object.get(s);
                Object value = jsonValue;

                JSONNumber number = jsonValue.isNumber();
                if(number != null) {
                    if(number.toString().contains(".")) {
                        value = number.doubleValue();
                    } else {
                        value = (int)number.doubleValue();
                    }
                }

                JSONBoolean jsonBoolean = jsonValue.isBoolean();
                if(jsonBoolean != null)
                    value = jsonBoolean.booleanValue();

                JSONNull jsonNull = jsonValue.isNull();
                if(jsonNull != null)
                    value = null;

                JSONString jsonString = jsonValue.isString();
                if(jsonString != null)
                    value = jsonString.stringValue();

                put(s, value);
            }
        }
    }

    public Options(Object ...keyValue) {
        super();
        for(int i = 0; i < keyValue.length && (i + 1) < keyValue.length; i = i + 2) {
            put(String.valueOf(keyValue[i]), keyValue[i + 1]);
        }
    }

    public Options(Map<String, ?> map) {
        super();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    // for compatibility with GQuery
    public final Properties toProperties() {
        Properties props = Properties.create();
        for (String k : keySet()) {
            Object val = get(k);
            if (val instanceof Options) {
                props.set(k, ((Options) val).toProperties());
            } else if (val instanceof Function) {
                props.setFunction(k, (Function) val);
            } else {
                props.set(k, val);
            }
        }

        return props;
    }

    public Options defaults(Options ...args) {
        for (Options source : args) {
            if (source != null) {
                for (String key : source.keySet()) {
                    if (!containsKey(key) || get(key) == null) {
                        put(key, source.get(key));
                    }
                }
            }
        }
        return this;
    }

    public Options extend(Options ...args) {
        for (Options arg : args) {
            extend(arg);
        }
        return this;
    }

    public Options extend(Options o) {
        if (o != null) {
            for (String k : o.keySet()) {
                put(k, o.get(k));
            }
        }
        return this;
    }

    public Options put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Integer getInt(String key) {
        return get(key, Integer.class);
    }

    public boolean getBoolean(String key) {
        Boolean r = (Boolean) super.get(key);
        return r == null ? false : r;
    }

    public <T> T get(String key) {
        return (T) super.get(key);
    }

    public final <T> T get(String id, Class<? extends T> clz) {
        Object o = super.get(id);
        if (clz != null) {
            if (o instanceof Double) {
                Double d = (Double)o;
                if (clz == Float.class) o = d.floatValue();
                else if (clz == Integer.class) o = d.intValue();
                else if (clz == Long.class) o = d.longValue();
                else if (clz == Short.class) o = d.shortValue();
                else if (clz == Byte.class) o = d.byteValue();
            } else if (clz == Boolean.class && !(o instanceof Boolean)) {
                o = Boolean.valueOf(String.valueOf(o));
            } else if (clz == String.class && !(o instanceof String)) {
                o = String.valueOf(o);
            } else if(clz == Integer.class && o instanceof String) {
                o = Integer.parseInt((String) o);
            }
        }
        return (T)o;
    }

    public Options clone() {
        Options options = new Options();

        for (String key : this.keySet()) {
            Object o = this.get(key);
            options.put(key, o);
        }
        return options;
    }

    public JSONObject toJsonObject() {
        return (JSONObject) toJsonValue();
    }

    @Override
    public JSONValue toJsonValue() {
        JSONObject j = new JSONObject();

        for (String key : this.keySet()) {
            JSONValue value = null;
            Object o = this.get(key);

            if (o == null)
                value = JSONNull.getInstance();
            else if (o instanceof Integer)
                value = new JSONNumber((Integer) o);
            else if (o instanceof Float)
                value = new JSONNumber((Float) o);
            else if (o instanceof Double)
                value = new JSONNumber((Double) o);
            else if (o instanceof Long)
                value = new JSONNumber((Long) o);
            else if (o instanceof Short)
                value = new JSONNumber((Short) o);
            else if (o instanceof Byte)
                value = new JSONNumber((Byte) o);
            else if (o instanceof Boolean) {
                value = JSONBoolean.getInstance((Boolean) o);
            } else if (o instanceof String) {
                value = new JSONString((String) o);
            } else if (o instanceof Options) {
                value = ((Options) o).toJsonObject();
            }
            j.put(key, value);
        }
        return j;
    }

    @Override
    public String toJsonString() {
        return toJsonValue().toString();
    }
}
