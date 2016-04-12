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
package org.lirazs.gbackbone.navigation.client.storage;

import com.google.gwt.json.client.JSONParser;
import org.lirazs.gbackbone.client.core.data.Options;

/**
 *
 */
public class LocalBrowserStorage implements BrowserStorage {

    @Override
    public boolean hasItem(String storageKey) {
        return getItemInternal(storageKey) != null;
    }

    @Override
    public Options getItem(String storageKey) {

        String data = getItemInternal(storageKey);
        return data != null ? Options.O(JSONParser.parseStrict(data)) : null;
    }

    @Override
    public void setItem(String storageKey, Options data) {
        if(data != null) {
            setItemInternal(storageKey, data.toJsonString());
        }
    }

    @Override
    public void removeItem(String storageKey) {
        removeItemInternal(storageKey);
    }

    @Override
    public void removeAll() {
        removeAllInternal();
    }

    private native String getItemInternal(String key) /*-{
        return $wnd.localStorage.getItem(key);
    }-*/;

    private native void setItemInternal(String key, String data) /*-{
        $wnd.localStorage.setItem(key, data);
    }-*/;

    private native void removeItemInternal(String key) /*-{
        $wnd.localStorage.removeItem(key);
    }-*/;

    private native void removeAllInternal() /*-{
        $wnd.localStorage.clear();
    }-*/;
}
