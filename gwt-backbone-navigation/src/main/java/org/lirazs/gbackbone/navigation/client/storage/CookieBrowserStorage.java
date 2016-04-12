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

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import org.lirazs.gbackbone.client.core.data.Options;

import java.util.Calendar;
import java.util.List;

/**
 *
 */
public class CookieBrowserStorage implements BrowserStorage {

    private String contextPath = "/";

    public CookieBrowserStorage() {
    }
    public CookieBrowserStorage(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public boolean hasItem(String storageKey) {
        return internalRead(storageKey) != null;
    }

    @Override
    public Options getItem(String storageKey) {
        String data = internalRead(storageKey);
        return data != null ? Options.O(JSONParser.parseStrict(data)) : null;
    }

    @Override
    public void setItem(String storageKey, Options data) {

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DAY_OF_MONTH, 1);

        if(data != null) {
            internalWrite(storageKey, data.toJsonString(), new Options(
                    "expires", instance.getTime(),
                    "path", contextPath,
                    "domain", getLocationHostName()
            ).toJsonObject());
        }
    }

    @Override
    public void removeItem(String storageKey) {

        internalWrite(storageKey, "", new Options(
                "expires", -1,
                "path", contextPath,
                "domain", getLocationHostName()
        ).toJsonObject());
    }

    @Override
    public void removeAll() {

        List<String> cookies = internalReadAll();
        for (String cookie : cookies) {
            removeItem(cookie);
        }
    }

    private native String internalWrite(String key, String value, JSONObject attributes) /*-{

        if(!attributes.path) {
            attributes.path = '/';
        }

        if (typeof attributes.expires === 'number') {
            var expires = new Date();
            expires.setMilliseconds(expires.getMilliseconds() + attributes.expires * 864e+5);
            attributes.expires = expires;
        }

        value = encodeURIComponent(String(value))
            .replace(/%(23|24|26|2B|3A|3C|3E|3D|2F|3F|40|5B|5D|5E|60|7B|7D|7C)/g, decodeURIComponent);

        key = encodeURIComponent(String(key));
        key = key.replace(/%(23|24|26|2B|5E|60|7C)/g, decodeURIComponent);
        key = key.replace(/[\(\)]/g, escape);

        return (document.cookie = [
            key, '=', value,
            attributes.expires && '; expires=' + attributes.expires.toUTCString(), // use expires attribute, max-age is not supported by IE
            attributes.path    && '; path=' + attributes.path,
            attributes.domain  && '; domain=' + attributes.domain,
            attributes.secure ? '; secure' : ''
        ].join(''));
    }-*/;

    private native String internalRead(String key) /*-{

        var result = null;

        var cookies = document.cookie ? document.cookie.split('; ') : [];
        var rdecode = /(%[0-9A-Z]{2})+/g;
        var i = 0;

        for (; i < cookies.length; i++) {
            var parts = cookies[i].split('=');
            var name = parts[0].replace(rdecode, decodeURIComponent);
            var cookie = parts.slice(1).join('=');

            if (cookie.charAt(0) === '"') {
                cookie = cookie.slice(1, -1);
            }

            try {
                cookie = cookie.replace(rdecode, decodeURIComponent);

                if (key === name) {
                    result = cookie;
                    break;
                }
            } catch (e) {}
        }

        return result;
    }-*/;

    private native List<String> internalReadAll() /*-{
        return document.cookie ? document.cookie.split('; ') : [];
    }-*/;

    private native String getLocationHostName() /*-{
        return location.hostname;
    }-*/;
}
