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
package org.lirazs.gbackbone.client.core.net;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.*;
import com.google.gwt.query.client.js.JsMap;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import org.lirazs.gbackbone.client.core.data.JsonSerializable;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.util.JsObjectUtils;

public class Sync {

    /**
     * // Map from CRUD to HTTP for our default `Backbone.sync` implementation.
         var methodMap = {
             'create': 'POST',
             'update': 'PUT',
             'patch': 'PATCH',
             'delete': 'DELETE',
             'read': 'GET'
         };
     */
    JsMap<String, String> methodMap;

    private static Sync instance = null;
    public static Sync get() {
        if(instance == null) {
            instance = new Sync();
        }
        return instance;
    }

    protected Sync() {
        methodMap = JsMap.create();
        methodMap.put("create", "POST");
        methodMap.put("update", "PUT");
        methodMap.put("patch", "PATCH");
        methodMap.put("delete", "DELETE");
        methodMap.put("read", "GET");
    }

    /**
     * // Override this function to change the manner in which Backbone persists
     // models to the server. You will be passed the type of request, and the
     // model in question. By default, makes a RESTful Ajax request
     // to the model's `url()`. Some possible customizations could be:
     //
     // * Use `setTimeout` to batch rapid-fire updates into a single request.
     // * Send up the models as XML instead of JSON.
     // * Persist models via WebSockets instead of Ajax.
     //
     // Turn on `Backbone.emulateHTTP` in order to send `PUT` and `DELETE` requests
     // as `POST`, with a `_method` parameter containing the true HTTP method,
     // as well as all requests with the body as `application/x-www-form-urlencoded`
     // instead of `application/json` with the model in a param named `model`.
     // Useful when interfacing with server-side languages like **PHP** that make
     // it difficult to read the body of `PUT` requests.

     export function sync(method, model, options) {
         var type = methodMap[method];

         // Default options, unless specified.
         _.defaults(options || (options = {}), {
             emulateHTTP: Backbone.emulateHTTP,
             emulateJSON: Backbone.emulateJSON
         });

         // Default JSON-request options.
         var params = <any> { type: type, dataType: 'json' };

         // Ensure that we have a URL.
         if (!options.url) {
            params.url = _.result(model, 'url') || Helpers.urlError();
         }

         // Ensure that we have the appropriate request data.
         if (options.data == null && model && (method === 'create' || method === 'update' || method === 'patch')) {
             params.contentType = 'application/json';
             params.data = JSON.stringify(options.attrs || model.toJSON(options));
         }

         // For older servers, emulate JSON by encoding the request into an HTML-form.
         if (options.emulateJSON) {
             params.contentType = 'application/x-www-form-urlencoded';
             params.data = params.data ? { model: params.data } : {};
         }

         // For older servers, emulate HTTP by mimicking the HTTP method with `_method`
         // And an `X-HTTP-Method-Override` header.
         if (options.emulateHTTP && (type === 'PUT' || type === 'DELETE' || type === 'PATCH')) {
             params.type = 'POST';

             if (options.emulateJSON) params.data._method = type;

             var beforeSend = options.beforeSend;

             options.beforeSend = function (xhr) {
                 xhr.setRequestHeader('X-HTTP-Method-Override', type);
                 if (beforeSend) return beforeSend.apply(this, arguments);
             };
         }

         // Don't process data on a non-GET request.
         if (params.type !== 'GET' && !options.emulateJSON) {
            params.processData = false;
         }

         // If we're sending a `PATCH` request, and we're in an old Internet Explorer
         // that still has ActiveX enabled by default, override jQuery to use that
         // for XHR instead. Remove this line when jQuery supports `PATCH` on IE8.
     if (params.type === 'PATCH' && noXhrPatch) {
     params.xhr = function () {
     return new ActiveXObject("Microsoft.XMLHTTP");
             };
         }

         // Make the request, allowing the user to override any Ajax options.
         var xhr = options.xhr = Backbone.ajax(_.extend(params, options));
         model.trigger('request', model, xhr, options);

         return xhr;
     };
     */
    public Promise sync(String method, Synchronized model, Options options) {
        String type = methodMap.get(method);

        /**
         * // Turn on `emulateHTTP` to support legacy HTTP servers. Setting this option
         // will fake `"PATCH"`, `"PUT"` and `"DELETE"` requests via the `_method` parameter and
         // set a `X-Http-Method-Override` header.
         export var emulateHTTP = false;

         // Turn on `emulateJSON` to support legacy servers that can't deal with direct
         // `application/json` requests ... will encode the body as
         // `application/x-www-form-urlencoded` instead and will send the model in a
         // form param named `model`.
         export var emulateJSON = false;
         */
        // Default options, unless specified.
        if(options == null) options = new Options();
        options.defaults(new Options(
                "emulateHTTP", false,
                "emulateJSON", false
        ));

        // Default JSON-request options.
        Ajax.Settings settings = Ajax.createSettings();
        settings.setType(type);
        settings.setDataType("json");

        // Ensure that we have a URL.
        if(!options.containsKey("url")) {
            String url = model.getUrl();
            if(url == null || url.isEmpty())
                throw new Error("A 'url' property or function must be specified");

            settings.setUrl(url);
        }

        // Ensure that we have the appropriate request data.
        if (!options.containsKey("data") && model != null && (method.equals("create") || method.equals("update") || method.equals("patch"))) {
            settings.setContentType("application/json");

            String data;
            if(options.containsKey("attrs")) {
                JsonSerializable attrs = options.get("attrs");
                data = attrs.toJsonString();
            } else {
                JsonSerializable attrs = model.toJSON();
                data = attrs.toJsonString();
            }
            settings.setData(data);
        }

        // For older servers, emulate JSON by encoding the request into an HTML-form.
        if (options.getBoolean("emulateJSON")) {
            settings.setContentType("application/x-www-form-urlencoded");
            IsProperties data = settings.getData();
            if(data != null) {
                data = Properties.create().$$("model", data.toJson());
            } else {
                data = Properties.create();
            }
            settings.setData(data);
        }

        // For older servers, emulate HTTP by mimicking the HTTP method with `_method`
        // And an `X-HTTP-Method-Override` header.
        if (options.getBoolean("emulateHTTP") && (type.equals("PUT") || type.equals("DELETE") || type.equals("PATCH"))) {
            settings.setType("POST");

            if (options.getBoolean("emulateJSON"))
                settings.getData().set("_method", type);

            Function beforeSend = options.get("beforeSend");

            //NOTE: Before send implementation is not supported by GQuery (another way for backbone users?)
            /*options.beforeSend = function (xhr) {
                xhr.setRequestHeader('X-HTTP-Method-Override', type);
                if (beforeSend) return beforeSend.apply(this, arguments);
            };*/
        }

        // Don't process data on a non-GET request.
        if (!settings.getType().equals("GET") && !options.getBoolean("emulateJSON")) {
            //NOTE: Disable of process data is not available in the GQuery Ajax API
            //params.processData = false;
        }

        /**
         * // If we're sending a `PATCH` request, and we're in an old Internet Explorer
         // that still has ActiveX enabled by default, override jQuery to use that
         // for XHR instead. Remove this line when jQuery supports `PATCH` on IE8.
         if (params.type === 'PATCH' && noXhrPatch) {
             params.xhr = function () {
                return new ActiveXObject("Microsoft.XMLHTTP");
             };
         }
         */
        // If we're sending a `PATCH` request, and we're in an old Internet Explorer
        // that still has ActiveX enabled by default, override jQuery to use that
        // for XHR instead. Remove this line when jQuery supports `PATCH` on IE8.
        if(settings.getType().equals("PATCH") && noXhrPatch()) {
            //NOTE: no set Xhr in the GQuery Ajax Library
            //settings.setXhr();
        }

        // Make the request, allowing the user to override any Ajax options.
        /**
         * Ajax.Settings setContentType(String var1);
         Ajax.Settings setContext(Element var1);
         Ajax.Settings setData(Object var1);
         Ajax.Settings setDataString(String var1);
         Ajax.Settings setDataType(String var1);
         Ajax.Settings setError(Function var1);
         Ajax.Settings setHeaders(IsProperties var1);
         Ajax.Settings setPassword(String var1);
         Ajax.Settings setSuccess(Function var1);
         Ajax.Settings setTimeout(int var1);
         Ajax.Settings setType(String var1);
         Ajax.Settings setUrl(String var1);
         Ajax.Settings setUsername(String var1);
         Ajax.Settings setWithCredentials(boolean var1);
         */
        if(options.containsKey("contentType"))
            settings.setContentType(options.<String>get("contentType"));
        if(options.containsKey("context"))
            settings.setContext(options.<Element>get("context"));
        if(options.containsKey("data"))
            settings.setData(options.get("data"));
        if(options.containsKey("dataString"))
            settings.setDataString(options.<String>get("dataString"));
        if(options.containsKey("dataType"))
            settings.setDataString(options.<String>get("dataType"));
        if(options.containsKey("error"))
            settings.setError(options.<Function>get("error"));
        //TODO: Support for headers
        /*if(options.containsKey("headers"))
            settings.setHeaders(options.getJsObject("headers").toProperties());*/
        if(options.containsKey("password"))
            settings.setDataString(options.<String>get("password"));
        if(options.containsKey("success"))
            settings.setSuccess(options.<Function>get("success"));
        if(options.containsKey("timeout"))
            settings.setTimeout(options.getInt("timeout"));
        if(options.containsKey("type"))
            settings.setType(options.<String>get("type"));
        if(options.containsKey("url"))
            settings.setUrl(options.<String>get("url"));
        if(options.containsKey("username"))
            settings.setUsername(options.<String>get("username"));
        if(options.containsKey("withCredentials"))
            settings.setWithCredentials(options.getBoolean("withCredentials"));

        Promise xhr = GQuery.ajax(settings);
        options.put("xhr", xhr);

        if(model != null)
            model.trigger("request", model, xhr, options);

        return xhr;
    }

    /**
     * var noXhrPatch = typeof window !== 'undefined' && !!_window.ActiveXObject && !(_window.XMLHttpRequest && (new XMLHttpRequest).dispatchEvent);
     */
    private native boolean noXhrPatch() /*-{
        return typeof window !== 'undefined' && !!window.ActiveXObject && !(window.XMLHttpRequest && (new XMLHttpRequest).dispatchEvent);
    }-*/;
}
