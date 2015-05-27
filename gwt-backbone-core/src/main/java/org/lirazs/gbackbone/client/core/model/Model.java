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
package org.lirazs.gbackbone.client.core.model;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.net.Sync;
import org.lirazs.gbackbone.client.core.net.Synchronized;
import org.lirazs.gbackbone.client.core.util.UUID;
import org.lirazs.gbackbone.client.generator.Reflectable;
import org.lirazs.gbackbone.client.generator.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Model extends Events implements Synchronized, Reflectable {

    /**
     * attributes: any;
         cid: string;
         collection: Collection;
         id;
     */
    private int id = -1;
    private Options attributes = null;
    private String cid;

    private Collection<? extends Model> collection;

    private String urlRoot = null;

    /**
     * _changing;
     _pending;
     _previousAttributes;
     _parse;

     // A hash of attributes whose current and previous value differ.
     changed = null;

     // The value returned during the last failed validation.
     validationError = null;

     // The default name for the JSON `id` attribute is `"id"`. MongoDB and
     // CouchDB users may want to set this to `"_id"`.
     idAttribute: string;
     */
    private boolean changing;
    private boolean pending;
    private Options previousAttributes;
    private boolean parse;

    // A hash of attributes whose current and previous value differ.
    private Options changed = null;

    // The value returned during the last failed validation.
    private Object validationError;

    public Object getValidationError() {
        return validationError;
    }

    // The default name for the JSON `id` attribute is `"id"`. MongoDB and
    // CouchDB users may want to set this to `"_id"`.
    protected String idAttribute;

    /**
     *
     if (!this.idAttribute) this.idAttribute = 'id';
     var defaults;
     var attrs = attributes || {};
     options || (options = {});
     this.cid = _.uniqueId('c');
     this.attributes = {};

     if (options.collection) this.collection = options.collection;
     if (options.parse) attrs = this.parse(attrs, options) || {};

     options._attrs || (options._attrs = attrs);

     if (defaults = _.result(this, 'defaults')) {
        attrs = _.defaults({}, attrs, defaults);
     }
     this.set(attrs, options);
     this.changed = {};

     this.initialize.apply(this, arguments);
     */

    public Model() {
        this(new Options(), null);
    }
    public Model(JSONObject model) {
        this(model, null);
    }
    public Model(JSONObject model, Options options) {
        Options attributes = new Options();
        if(options == null) options = new Options();

        this.cid = "c" + UUID.uuid();
        this.attributes = new Options();

        if (options.containsKey("collection"))
            this.collection = options.get("collection");

        Options attrs = this.parse(model, options);
        if(attrs != null) attributes = attrs;

        Options defaults = defaults();
        if(defaults != null) {
            attributes = new Options().defaults(attributes, defaults);
        }

        if(attributes.containsKey("idAttribute")) {
            idAttribute = attributes.get("idAttribute");
        } else if(idAttribute == null) {
            idAttribute = "id";
        }

        this.set(attributes, options);
        this.changed = new Options();
    }
    public Model(Options attributes) {
        this(attributes, null);
    }
    public Model(Options attributes, Options options) {
        if(attributes == null) attributes = new Options();
        if(options == null) options = new Options();

        this.cid = "c" + UUID.uuid();
        this.attributes = new Options();

        if (options.containsKey("collection"))
            this.collection = options.get("collection");

        Options defaults = defaults();
        if(defaults != null) {
            attributes = new Options().defaults(attributes, defaults);
        }

        if(attributes.containsKey("idAttribute")) {
            idAttribute = attributes.get("idAttribute");
        } else if(idAttribute == null) {
            idAttribute = "id";
        }

        this.set(attributes, options);
        this.changed = new Options();
    }

    // should be overridden with the default properties of model
    protected Options defaults() {
        return null; // override
    }

    /**
      Run validation against the next complete set of model attributes,
      returning `true` if all is well. Otherwise, fire an `"invalid"` event.
     */
    public Object validate(Options attributes, Options options) {
        return null; // override
    }


    public void setCollection(Collection<? extends Model> collection) {
        this.collection = collection;
    }

    public Collection<? extends Model> getCollection() {
        return collection;
    }

    /**
     * // Return a copy of the model's `attributes` object.
     toJSON(options?: any) {
     return _.clone(this.attributes);
     }
     */
    public Options toJSON() {
        return (Options) attributes.clone();
    }

    /**
     * // Proxy `Backbone.sync` by default -- but override this if you need
     // custom syncing semantics for *this* particular model.
         sync(...args): any {
            return Backbone.sync.apply(this, arguments);
         }
     */
    public Promise sync(String method, Options options) {
        return Sync.get().sync(method, this, options);
    }

    public int getId() {
        return id;
    }
    public String getCid() {
        return cid;
    }

    public String getIdAttribute() {
        return idAttribute;
    }
    public Options getAttributes() {
        return attributes;
    }

    /**
     * // Get the value of an attribute.
     get(attr: string): any {
     return this.attributes[attr];
     }
     */
    public <T> T get(String attr) {
        return attributes.get(attr);
    }

    public int getInt(String attr) {
        return attributes.getInt(attr);
    }

    public boolean getBoolean(String attr) {
        return attributes.getBoolean(attr);
    }

    /**
     * // Get the HTML-escaped value of an attribute.
     escape(attr: string): string {
         return _.escape(this.get(attr));
     }
     */
    public String escape(String attr) {
        return SafeHtmlUtils.htmlEscape(this.<String>get(attr));
    }

    /**
     * // Returns `true` if the attribute contains a value that is not null
         // or undefined.
         has(attr: string): boolean {
            return this.get(attr) != null;
         }
     */
    public boolean has(String attr) {
        return attributes.containsKey(attr);
    }

    /**
     * // Set a hash of model attributes on the object, firing `"change"`. This is
     // the core primitive operation of a model, updating the data and notifying
     // anyone who needs to know about the change in state. The heart of the beast.
     set(key: string, val: any, options?: ModelSetOptions): any
     set(obj: any, options?: ModelSetOptions): any
     set(key: any, val?: any, options?: any): any {
         var attr, attrs, unset, changes, silent, changing, prev, current;

         if (key == null) return this;

         // Handle both `"key", value` and `{key: value}` -style arguments.
         if (typeof key === 'object') {
             attrs = key;
             options = val;
         } else {
            (attrs = {})[key] = val;
         }

         options || (options = {});

         // Run validation.
         if (!this._validate(attrs, options)) return false;

         // Extract attributes and options.
         unset = options.unset;
         silent = options.silent;

         changes = [];
         changing = this._changing;
         this._changing = true;

         if (!changing) {
             this._previousAttributes = _.clone(this.attributes);
             this.changed = {};
         }
         current = this.attributes, prev = this._previousAttributes;

         // Check for changes of `id`.
         if (this.idAttribute in attrs) this.id = attrs[this.idAttribute];

         // For each `set` attribute, update or delete the current value.
         for (attr in attrs) {
         val = attrs[attr];
         if (!_.isEqual(current[attr], val)) changes.push(attr);
         if (!_.isEqual(prev[attr], val)) {
         this.changed[attr] = val;
         } else {
         delete this.changed[attr];
         }
         unset ? delete current[attr] : current[attr] = val;
         }

         // Trigger all relevant attribute changes.
         if (!silent) {
         if (changes.length) this._pending = true;
         for (var i = 0, l = changes.length; i < l; i++) {
         this.trigger('change:' + changes[i], this, current[changes[i]], options);
         }
         }

         // You might be wondering why there's a `while` loop here. Changes can
         // be recursively nested within `"change"` events.
         if (changing) return this;
         if (!silent) {
         while (this._pending) {
         this._pending = false;
         this.trigger('change', this, options);
         }
         }
         this._pending = false;
         this._changing = false;
         return this;
     }
     */
    public Model set(JSONObject jsonObject) {
        return set(jsonObject, null);
    }
    public Model set(JSONObject jsonObject, Options options) {
        return set(parse(jsonObject, options), options);
    }

    public <T> Model set(String name, T value) {
        return set(new Options().put(name, value));
    }

    public <T> Model set(String name, T value, Options options) {
        if(name == null || name.isEmpty())
            return this;

        return set(new Options().put(name, value), options);
    }

    public <T> Model set(Options attributes) {
        return set(attributes, null);
    }

    public <T> Model set(Options attributes, Options options) {

        // Run validation.
        if(!internalValidate(attributes, options)) return this;

        boolean unset = options != null && options.getBoolean("unset");
        boolean silent = options != null && options.getBoolean("silent");

        List<String> changes = new ArrayList<String>();
        boolean changing = this.changing;
        this.changing = true;

        if(!changing) {
            previousAttributes = this.attributes.clone();
            changed = new Options();
        }
        Options current = this.attributes;
        Options prev = previousAttributes;

        // Check for changes of `id`.
        if(attributes.containsKey(idAttribute)) {
            this.id = attributes.getInt(idAttribute);
        }

        // For each `set` attribute, update or delete the current value.
        Set<String> keys = attributes.keySet();
        for (String attr : keys) {
            Object value = attributes.get(attr);

            //if (!OptionsUtils.isEqual(current.get(attr), value))
            if (current.containsKey(attr) && !current.get(attr).equals(value))
                changes.add(attr);

            //if (!OptionsUtils.isEqual(prev.get(attr), value)) {
            if (prev.containsKey(attr) && !prev.get(attr).equals(value)) {
                changed.put(attr, value);
            } else {
                changed.remove(attr);
            }
            if(unset)
                current.remove(attr);
            else
                current.put(attr, value);
        }

        // Trigger all relevant attribute changes.
        if (!silent) {
            if(changes.size() > 0) pending = true;

            for (int i = 0; i < changes.size(); i++) {
                Object change = changes.get(i);
                this.trigger("change:" + change, this, current.get(change), options);
            }
        }

        // You might be wondering why there's a `while` loop here. Changes can
        // be recursively nested within `"change"` events.
        if(changing) return this;
        if (!silent) {
            while (this.pending) {
                this.pending = false;
                this.trigger("change", this, options);
            }
        }

        this.pending = false;
        this.changing = false;

        return this;
    }

    /**
     * // Remove an attribute from the model, firing `"change"`. `unset` is a noop
     // if the attribute doesn't exist.
     unset(attr: string, options?: SilentOptions) {
     return this.set(attr, void 0, _.extend({}, options, { unset: true }));
     }
     */
    // Remove an attribute from the model, firing `"change"`. `unset` is a noop
    // if the attribute doesn't exist.
    public Model unset(String attr) {
        return unset(attr, null);
    }
    public Model unset(String attr, Options options) {
        if(options == null)
            options = new Options();
        options.put("unset", true);

        return set(attr, null, options);
    }

    /**
     * // Clear all attributes on the model, firing `"change"`.
     clear(options?: SilentOptions) {
     var attrs = {};
     for (var key in this.attributes) attrs[key] = void 0;
     return this.set(attrs, _.extend({}, options, { unset: true }));
     }
     */
    public Model clear() {
        return clear(null);
    }

    public Model clear(Options options) {
        Options attrs = new Options();
        Set<String> keys = attributes.keySet();
        for (String key : keys) {
            attrs.put(key, null);
        }
        if(options == null)
            options = new Options();
        options.put("unset", true);

        return this.set(attrs, options);
    }

    /**
     * // Determine if the model has changed since the last `"change"` event.
     // If you specify an attribute name, determine if that attribute has changed.
     hasChanged(attr?: string): boolean {
         if (attr == null) return !_.isEmpty(this.changed);
         return _.has(this.changed, attr);
     }
     */
    public boolean hasChanged() {
        return hasChanged(null);
    }
    public boolean hasChanged(String attr) {
        if(attr == null)
            return this.changed.size() > 0;
        return this.changed.containsKey(attr);
    }

    /**
     * // Return an object containing all the attributes that have changed, or
     // false if there are no changed attributes. Useful for determining what
     // parts of a view need to be updated and/or what attributes need to be
     // persisted to the server. Unset attributes will be set to undefined.
     // You can also pass an attributes object to diff against the model,
     // determining if there *would be* a change.
     changedAttributes(diff: any): any {
         if (!diff) return this.hasChanged() ? _.clone(this.changed) : false;
         var val, changed = false;
         var old = this._changing ? this._previousAttributes : this.attributes;
         for (var attr in diff) {
             if (_.isEqual(old[attr], (val = diff[attr]))) continue;
             (changed || (<any> changed = {}))[attr] = val;
         }
         return changed;
     }
     */
    public Options changedAttributes() {
        return changedAttributes(null);
    }
    public Options changedAttributes(Options diff) {
        if(diff == null)
            return this.hasChanged() ? this.changed.clone() : null;

        Options changed = null;
        Options old = this.changing ? this.previousAttributes : this.attributes;

        Set<String> keys = diff.keySet();
        for (String attr : keys) {
            Object value = diff.get(attr);
            if(old.get(attr).equals(value)) continue;

            if(changed == null)
                changed = new Options();
            changed.put(attr, value);
        }

        return changed;
    }

    /**
     * // Get the previous value of an attribute, recorded at the time the last
     // `"change"` event was fired.
     previous(attr: string): any {
         if (attr == null || !this._previousAttributes) return null;
         return this._previousAttributes[attr];
     }
     */
    public <T> T previous(String attr) {
        if(attr == null || this.previousAttributes == null) return null;
        return this.previousAttributes.get(attr);
    }

    /**
     * // Get all of the attributes of the model at the time of the previous
     // `"change"` event.
     previousAttributes(): any {
        return _.clone(this._previousAttributes);
     }
     */
    public Options previousAttributes() {
        return previousAttributes.clone();
    }

    /**
     * // Fetch the model from the server. If the server's representation of the
     // model differs from its current attributes, they will be overridden,
     // triggering a `"change"` event.
     fetch(options?: ModelFetchOptions): JQueryXHR
     fetch(options?: any): JQueryXHR {
         options = options ? _.clone(options) : {};
         if (options.parse === void 0) options.parse = true;
         var model = this;
         var success = options.success;
         options.success = function (resp) {
             if (!model.set(model.parse(resp, options), options)) return false;
             if (success) success(model, resp, options);
             model.trigger('sync', model, resp, options);
         };
         Helpers.wrapError(this, options);
         return this.sync('read', this, options);
     }
     */
    public Promise fetch() {
        return fetch(new Options());
    }
    public Promise fetch(final Options options) {
        if(!options.containsKey("parse"))
            options.put("parse", true);

        final Function success = options.get("success");
        options.put("success", new Function() {
            @Override
            public void f() {
                JSONObject response = getArgument(0);
                Options parsedResponse = parse(response, options);

                set(parsedResponse, options);

                if(success != null) {
                    success.f(Model.this, response, options);
                }
                Model.this.trigger("sync", Model.this, response, options);
            }
        });

        final Function error = options.get("error");
        options.put("error", new Function() {
            @Override
            public void f() {
                JavaScriptObject response = getArgument(0);
                if(error != null) {
                    error.f(Model.this, response, options);
                }
                Model.this.trigger("error", Model.this, response, options);
            }
        });
        return sync("read", options);
    }

    /**
     * // Set a hash of model attributes, and sync the model to the server.
     // If the server returns an attributes hash that differs, the model's
     // state will be `set` again.
     save(attributes: any, options?: ModelSaveOptions): any
     save(key, val?, options?): any {
         var attrs, method, xhr, attributes = this.attributes;

         // Handle both `"key", value` and `{key: value}` -style arguments.
         if (key == null || typeof key === 'object') {
             attrs = key;
             options = val;
         } else {
            (attrs = {})[key] = val;
         }

         options = _.extend({ validate: true }, options);

         // If we're not waiting and attributes exist, save acts as
         // `set(attr).save(null, opts)` with validation. Otherwise, check if
         // the model will be valid when the attributes, if any, are set.
         if (attrs && !options.wait) {
            if (!this.set(attrs, options)) return false;
         } else {
            if (!this._validate(attrs, options)) return false;
         }

         // Set temporary attributes if `{wait: true}`.
         if (attrs && options.wait) {
            this.attributes = _.extend({}, attributes, attrs);
         }

         // After a successful server-side save, the client is (optionally)
         // updated with the server-side state.
         if (options.parse === void 0) options.parse = true;

         var model = this;
         var success = options.success;

         options.success = function (resp) {
             // Ensure attributes are restored during synchronous saves.
             model.attributes = attributes;
             var serverAttrs = model.parse(resp, options);
             if (options.wait) serverAttrs = _.extend(attrs || {}, serverAttrs);

             if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
                return false;
             }
             if (success) success(model, resp, options);
             model.trigger('sync', model, resp, options);
         };
         Helpers.wrapError(this, options);

         method = this.isNew() ? 'create' : (options.patch ? 'patch' : 'update');

         if (method === 'patch') options.attrs = attrs;
         xhr = this.sync(method, this, options);

         // Restore attributes.
         if (attrs && options.wait) this.attributes = attributes;

         return xhr;
     }
     */
    public Promise save(String key, Object value, Options options) {
        return save(new Options().put(key, value), options);
    }
    public Promise save(Options attributes) {
        return save(attributes, null);
    }
    public Promise save(final Options attributes, final Options options) {
        final Options initialAttributes = this.attributes;
        final Options saveOptions = new Options().put("validate", true).extend(options);

        // If we're not waiting and attributes exist, save acts as
        // `set(attr).save(null, opts)` with validation. Otherwise, check if
        // the model will be valid when the attributes, if any, are set.
        if (attributes != null && !saveOptions.getBoolean("wait")) {
            this.set(attributes, saveOptions);
        } else {
            if (!this.internalValidate(attributes, saveOptions))
                return null;
        }

        // Set temporary attributes if `{wait: true}`.
        if (attributes != null && saveOptions.getBoolean("wait")) {
            this.attributes = new Options().extend(attributes);
        }

        // After a successful server-side save, the client is (optionally)
        // updated with the server-side state.
        if(!saveOptions.containsKey("parse"))
            saveOptions.put("parse", true);

        final Function success = saveOptions.get("success");
        saveOptions.put("success", new Function() {
            @Override
            public void f() {
                JSONObject response = getArgument(0);

                // Ensure attributes are restored during synchronous saves.
                Model.this.attributes = initialAttributes;

                Options serverAttrs = Model.this.parse(response, saveOptions);
                if (saveOptions.getBoolean("wait")) {
                    Options attrs = attributes != null ? attributes : new Options();
                    serverAttrs = attrs.extend(serverAttrs);
                }
                Model.this.set(serverAttrs, saveOptions);

                if(success != null) {
                    success.f(Model.this, response, saveOptions);
                }
                Model.this.trigger("sync", Model.this, response, saveOptions);
            }
        });

        final Function error = saveOptions.get("error");
        saveOptions.put("error", new Function() {
            @Override
            public void f() {
                JSONObject response = getArgument(0);
                if(error != null) {
                    error.f(Model.this, response, saveOptions);
                }
                Model.this.trigger("error", Model.this, response, saveOptions);
            }
        });

        String method = this.isNew() ? "create" : (options.getBoolean("patch") ? "patch" : "update");

        if (method.equals("patch"))
            options.put("attrs", attributes);

        Promise deferred = sync(method, options);

        // Restore attributes.
        if (attributes != null && options.getBoolean("wait"))
            this.attributes = attributes;

        return deferred;
    }


    /**
     * // Destroy this model on the server if it was already persisted.
     // Optimistically removes the model from its collection, if it has one.
     // If `wait: true` is passed, waits for the server to respond before removal.
     destroy(options?: ModelDestroyOptions) {
         options = options ? _.clone(options) : {};

         var model = this;
         var success = options.success;

         (<any>options).success = function (resp) {
             if (options.wait || model.isNew())
                model.trigger('destroy', model, model.collection, options);

             if (success) success(model, resp, options);
             if (!model.isNew()) model.trigger('sync', model, resp, options);
         };

         if (this.isNew()) {
             options.success();
             return false;
         }
         Helpers.wrapError(this, options);

         var xhr = this.sync('delete', this, options);
         if (!options.wait)
            model.trigger('destroy', model, model.collection, options);

         return xhr;
     }
     */
    public Promise destroy() {
        return destroy(new Options());
    }
    public Promise destroy(final Options options) {

        final Function success = options.get("success");
        options.put("success", new Function() {
            @Override
            public void f() {
                JavaScriptObject response = getArgument(0);

                if (options.getBoolean("wait") || Model.this.isNew()) {
                    Model.this.trigger("destroy", Model.this, Model.this.collection, options);
                }

                if(success != null) {
                    success.f(Model.this, response, options);
                }
                if(!Model.this.isNew())
                    Model.this.trigger("sync", Model.this, response, options);
            }
        });

        if (this.isNew()) {
            Function successFunction = options.get("success");
            successFunction.f();
            return null;
        }

        final Function error = options.get("error");
        options.put("error", new Function() {
            @Override
            public void f() {
                JavaScriptObject response = getArgument(0);
                if(error != null) {
                    error.f(Model.this, response, options);
                }
                Model.this.trigger("error", Model.this, response, options);
            }
        });

        Promise deferred = this.sync("delete", options);
        if (!options.getBoolean("wait"))
            Model.this.trigger("destroy", Model.this, Model.this.collection, options);

        return deferred;
    }

    /**
     * // Default URL for the model's representation on the server -- if you're
     // using Backbone's restful methods, override this to change the endpoint
     // that will be called.
     url() {
         var base = _.result(this, 'urlRoot') || _.result(this.collection, 'url') || Helpers.urlError();
         if (this.isNew()) return base;
         return base + (base.charAt(base.length - 1) === '/' ? '' : '/') + encodeURIComponent(this.id);
     }
     */
    public String getUrl() {
        String base;

        if(urlRoot != null)
            base = urlRoot;
        else if(collection != null)
            base = collection.getUrl();
        else
            throw new Error("A 'url' property or function must be specified");

        if(this.isNew()) {
            return base;
        }

        return base + (base.charAt(base.length() - 1) == '/' ? "" : '/') + encodeURIComponent(String.valueOf(this.id));
    }

    private native String encodeURIComponent(String s) /*-{
        return encodeURIComponent(s);
    }-*/;

    /**
     * // **parse** converts a response into the hash of attributes to be `set` on
     // the model. The default implementation is just to pass the response along.
     parse(resp?: any, options?: any) {
     return resp;
     }
     */
    protected Options parse(JSONObject resp, Options options) {
        return new Options(resp);
    }

    /**
     * // Create a new model with identical attributes to this one.
     clone(): Model {
        return new (<any>this).constructor(this.attributes);
     }
     */
    public Model clone() {
        Model result = GWT.<Reflection>create(Reflection.class).instantiateModel(getClass(), null, null);

        result.id = id;
        result.attributes = attributes.clone();
        result.collection = collection;
        result.urlRoot = urlRoot;
        result.changing = changing;
        result.pending = pending;
        result.previousAttributes = previousAttributes.clone();
        result.parse = parse;
        result.changed = changed.clone();
        result.idAttribute = idAttribute;

        return result;
    }

    /**
     * // A model is new if it has never been saved to the server, and lacks an id.
     isNew(): boolean {
     return this.id == null;
     }
     */
    public boolean isNew() {
        return this.id == -1;
    }

    /**
     * // Check if the model is currently in a valid state.
     isValid(options?: any): boolean {
     return this._validate({}, _.extend(options || {}, { validate: true }));
     }
     */
    public boolean isValid() {
        return isValid(null);
    }
    public boolean isValid(Options options) {
        if(options == null)
            options = new Options();
        options.put("validate", true);

        return this.internalValidate(new Options(), options);
    }

    protected boolean internalValidate(Options attributes, Options options) {
        if(options != null && !options.getBoolean("validate")) return true;

        Object error = validationError = validate(attributes, options);
        if(error == null || (error instanceof Boolean && ((Boolean) error)))
            return true;

        if(options == null)
            options = new Options();

        this.trigger("invalid", this, error, options);
        return false;
    }

    // Retrieve the names of an object's properties.
    public String[] keys() {
        return (String[]) attributes.keySet().toArray();
    }
    // Retrieve the values of an object's properties.
    public Object[] values() {
        return attributes.values().toArray();
    }

    // Convert an object into a list of `[key, value]` pairs.
    public Object[][] pairs() {
        String[] keys = keys();
        int length = keys.length;
        Object[][] pairs = new Object[length][2];

        for (int i = 0; i < length; i++) {
            pairs[i] = new Object[] { keys[i], get(keys[i]) };
        }
        return pairs;
    }

    // Invert the keys and values of an object. The values must be serializable.
    public Options invert() {
        Options result = new Options();
        String[] keys = keys();
        for (String key : keys) {
            result.put(get(key).toString(), key);
        }
        return result;
    }

    // Return a copy of the object only containing the whitelisted properties.
    public Options pick(String ...keys) {
        Options result = new Options();
        for (String key : keys) {
            if(has(key)) result.put(key, get(key));
        }
        return result;
    }

    // Return a copy of the object without the blacklisted properties.
    public Options omit(String ...keys) {
        Options result = attributes.clone();
        for (String key : keys) {
            if(result.containsKey(key)) result.remove(key);
        }
        return result;
    }

}
