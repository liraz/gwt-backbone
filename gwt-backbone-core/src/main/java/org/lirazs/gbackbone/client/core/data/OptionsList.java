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
package org.lirazs.gbackbone.client.core.data;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsList extends ArrayList<Options> implements JsonSerializable {

    public static OptionsList OL() {
        return new OptionsList();
    }
    public static OptionsList OL(Options ...options) {
        return new OptionsList(options);
    }
    public static OptionsList OL(List<Options> options) {
        return new OptionsList(options);
    }
    public static OptionsList OL(JSONArray array) {
        return new OptionsList(array);
    }

    public OptionsList() {
        super();
    }

    public OptionsList(Options ...options) {
        super(Arrays.asList(options));
    }
    public OptionsList(List<Options> options) {
        super(options);
    }

    public OptionsList(JSONArray array) {
        for (int i = 0; i < array.size(); i++) {
            JSONValue value = array.get(i);
            JSONObject object = value.isObject();
            if(object != null) {
                add(new Options(object));
            }
        }
    }

    public Options[] concat(OptionsList other) {
        Options[] optionses = toArray(new Options[size() + other.size()]);
        for (int i = 0; i < other.size(); i++) {
            Options otherOption = other.get(i);
            optionses[i + size()] = otherOption;
        }
        return optionses;
    }

    @Override
    public JSONValue toJsonValue() {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < size(); i++) {
            Options options = get(i);
            jsonArray.set(i, options.toJsonValue());
        }
        return jsonArray;
    }

    @Override
    public String toJsonString() {
        return toJsonValue().toString();
    }
}
