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
package org.lirazs.gbackbone.client.core.model;

import org.lirazs.gbackbone.client.core.data.Options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created on 24/10/2015.
 */
public class ModelChainBuilder {

    private Options optionsValue;
    private List<?> listValue;

    public ModelChainBuilder(Options attributes) {
        this.optionsValue = attributes;
    }

    public List<?> getListValue() {
        return listValue;
    }

    public Options getOptionsValue() {
        return optionsValue;
    }

    /**
     * Retrieve the names of an object's properties.
     *
     * @return
     */
    public ModelChainBuilder keys() {
        listValue = new ArrayList<String>(optionsValue.keySet());
        return this;
    }

    /**
     * Retrieve the values of an object's properties.
     *
     * @return
     */
    public ModelChainBuilder values() {
        listValue = new ArrayList<Object>(optionsValue.values());
        return this;
    }

    /**
     * Returns a copy of the array with all falsy values removed.
     * The following values will be removed: false, null, 0, "".
     *
     * @return
     */
    public ModelChainBuilder compact() {
        Iterator<?> iterator = listValue.iterator();
        while(iterator.hasNext()) {
            Object next = iterator.next();

            if(next == null)
                iterator.remove();
            else if(next instanceof String && ((String) next).isEmpty())
                iterator.remove();
            else if(next instanceof Boolean && !((Boolean) next))
                iterator.remove();
            else if(next.toString().equals("0") || next.toString().equals("0.0"))
                iterator.remove();
        }

        return this;
    }

    /**
     * Invert the keys and values of an object. The values must be serializable.
     *
     * @return
     */
    public ModelChainBuilder invert() {
        Options invert = new Options();
        Set<String> keySet = this.optionsValue.keySet();
        for (String key : keySet) {
            invert.put(this.optionsValue.get(key).toString(), key);
        }

        this.optionsValue = invert;
        return this;
    }

    /**
     * Return a copy of the object only containing the whitelisted properties.
     *
     * @param keys
     * @return
     */
    public ModelChainBuilder pick(String ...keys) {
        Options result = new Options();
        for (String key : keys) {
            if(this.optionsValue.containsKey(key))
                result.put(key, this.optionsValue.get(key));
        }
        this.optionsValue = result;
        return this;
    }

    /**
     * Return a copy of the object without the blacklisted properties.
     *
     * @param keys
     * @return
     */
    public ModelChainBuilder omit(String ...keys) {
        for (String key : keys) {
            if(optionsValue.containsKey(key))
                optionsValue.remove(key);
        }
        return this;
    }
}
