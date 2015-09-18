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
package org.lirazs.gbackbone.client.core.collection;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.js.JsMap;
import com.google.gwt.user.client.Random;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.data.OptionsList;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.function.FilterFunction;
import org.lirazs.gbackbone.client.core.function.MapFunction;
import org.lirazs.gbackbone.client.core.function.MinMaxFunction;
import org.lirazs.gbackbone.client.core.function.SortFunction;
import org.lirazs.gbackbone.client.core.js.JsArray;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.net.Sync;
import org.lirazs.gbackbone.client.core.net.Synchronized;
import org.lirazs.gbackbone.client.generator.Reflection;

import java.util.*;

public class Collection<T extends Model> extends Events implements Synchronized, Iterable<T> {

    /**
     * // Default options for `Collection#set`.
     var setOptions = { add: true, remove: true, merge: true };
     var addOptions = { add: true, remove: false };
     */
    // Default options for `Collection#set`.
    Options setOptions = new Options("add", true, "remove", true, "merge", true);
    Options addOptions = new Options("add", true, "remove", false);

    Comparator<T> comparator;

    /**
     * length: number;
     models: Model[];
     */
    int length;
    List<T> models;
    Map<String, T> byId;
    Class<T> modelClass;

    /**
     * constructor(models?: Model[], options?: CollectionOptions) {
         super();

         if (!this.model) this.model = Model;
         options || (options = {});

         if (options.model) this.model = options.model;
         if (options.comparator !== void 0) this.comparator = options.comparator;

         this._reset();

         this.initialize.apply(this, arguments);
         if (models) this.reset(models, _.extend({ silent: true }, options));
     }
     */
    public Collection() {
        this(null, new ArrayList<T>(), null);
    }
    public Collection(Class<T> modelClass) {
        this(modelClass, new ArrayList<T>(), null);
    }
    public Collection(Class<T> modelClass, Options options) {
        this(modelClass, new ArrayList<T>(), options);
    }
    public Collection(OptionsList models) {
        this(null, models, null);
    }
    public Collection(OptionsList models, Options options) {
        this(null, models, options);
    }
    public Collection(Options ...models) {
        this(null, new OptionsList(models), null);
    }
    public Collection(Class<T> modelClass, OptionsList models) {
        this(modelClass, models, null);
    }
    public Collection(Class<T> modelClass, OptionsList models, Options options) {
        this.modelClass = modelClass;

        List<T> parsedModels = parse(models, options);

        if(options == null)
            options = new Options();

        if(options.containsKey("comparator"))
            comparator = options.get("comparator");

        if(options.containsKey("url"))
            url = options.get("url");

        internalReset();

        initialize(parsedModels);
        initialize(parsedModels, options);
        if(models != null && parsedModels.size() > 0) {
            options.put("silent", options.containsKey("silent") ? options.get("silent") : true);
            reset(parsedModels, options);
        }
    }
    public Collection(T ...models) {
        this(null, Arrays.asList(models), null);
    }
    public Collection(JSONArray models) {
        this(null, models, null);
    }
    public Collection(Class<T> modelClass, JSONArray models, Options options) {
        this.modelClass = modelClass;

        List<T> parsedModels = parse(models, options);

        if(options == null)
            options = new Options();

        if(options.containsKey("comparator"))
            comparator = options.get("comparator");

        if(options.containsKey("url"))
            url = options.get("url");

        internalReset();

        initialize(parsedModels);
        initialize(parsedModels, options);
        if(models != null && parsedModels.size() > 0) {
            options.put("silent", options.containsKey("silent") ? options.get("silent") : true);
            reset(parsedModels, options);
        }
    }

    public Collection(Class<T> modelClass, T ...models) {
        this(modelClass, Arrays.asList(models), null);
    }
    public Collection(Class<T> modelClass, JSONArray models) {
        this(modelClass, models, null);
    }

    public Collection(List<T> models) {
        this(null, models, null);
    }
    public Collection(Options options, Class<T> modelClass) {
        this(modelClass, new ArrayList<T>(), options);
    }
    public Collection(Class<T> modelClass, List<T> models) {
        this(modelClass, models, null);
    }
    public Collection(Class<T> modelClass, List<T> models, Options options) {
        this.modelClass = modelClass;

        if(options == null)
            options = new Options();

        if(options.containsKey("comparator"))
            comparator = options.get("comparator");

        if(options.containsKey("url"))
            url = options.get("url");

        internalReset();

        initialize(models);
        initialize(models, options);
        if(models != null) {
            options.put("silent", options.containsKey("silent") ? options.get("silent") : true);
            reset(models, options);
        }
    }

    public boolean registerModelClass(Class<T> modelClass) {
        if(!Objects.equals(this.modelClass, modelClass)) {
            this.modelClass = modelClass;
            return true;
        }
        return false;
    }
    public void registerComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    protected void initialize(List<T> models) {
        // override
    }
    protected void initialize(List<T> models, Options options) {
        // override
    }

    public int length() {
        return this.length;
    }

    /**
     * // The JSON representation of a Collection is an array of the
     // models' attributes.
     toJSON(options?: any) {
        return (<any> this).map(function (model) { return model.toJSON(options); });
     }
     */
    public OptionsList toJSON() {
        OptionsList modelsJson = new OptionsList();
        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);
            modelsJson.add(model.toJSON());
        }
        return modelsJson;
    }

    /**
     * // Proxy `Backbone.sync` by default.
     sync(...args) {
        return Backbone.sync.apply(this, arguments);
     }
     */
    public Promise sync(String method, Options options) {
        return Sync.get().sync(method, this, options);
    }

    private String url;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * // Add a model, or list of models to the set.
     add(model: Model, options?: CollectionAddOptions): any
     add(models: Model[], options?: CollectionAddOptions): any
     add(models: any, options?: CollectionAddOptions): any {
        return this.set(models, _.extend({ merge: false }, options, addOptions));
     }
     */
    public Collection add(JSONObject jsonObject) {
        return add(jsonObject, null);
    }
    public Collection add(JSONObject jsonObject, Options options) {
        T model = prepareModel(new Options(jsonObject), options);
        return model != null ? add(model, options) : this;
    }
    public Collection add(JSONArray models) {
        return add(models, null);
    }
    public Collection add(JSONArray models, Options options) {
        return add(parse(models, options), options);
    }
    public Collection add(OptionsList models) {
        return add(models, null);
    }
    public Collection add(OptionsList models, Options options) {
        return add(parse(models, options), options);
    }

    public Collection add(Options attrs) {
        return add(attrs, null);
    }
    public Collection add(Options attrs, Options options) {
        T model = prepareModel(attrs, options);
        return model != null ? add(model, options) : this;
    }
    public Collection add(T model) {
        return add(Collections.singletonList(model), null);
    }
    public Collection add(T model, Options options) {
        return add(Collections.singletonList(model), options);
    }
    public Collection add(List<T> models) {
        return add(models, null);
    }
    public Collection add(List<T> models, Options options) {
        return set(models, new Options("merge", false).extend(options).extend(addOptions));
    }

    /**
     * // Remove a model, or a list of models from the set.
     remove(model: Model, options?: CollectionRemoveOptions): any
     remove(models: Model[], options?: CollectionRemoveOptions): any
     remove(models: any, options?: CollectionRemoveOptions): any {
         models = _.isArray(models) ? models.slice() : [models];
         options || (options = {});
         var i, l, index, model;
         for (i = 0, l = models.length; i < l; i++) {
             model = this.get(models[i]);
             if (!model) continue;
             delete this._byId[model.id];
             delete this._byId[model.cid];
             index = this.indexOf(model);
             this.models.splice(index, 1);
             this.length--;
             if (!options.silent) {
                 options.index = index;
                 model.trigger('remove', model, this, options);
             }
             this._removeReference(model);
         }
         return this;
     }
     */
    public Collection remove(T model) {
        return remove(Collections.singletonList(model), null);
    }
    public Collection remove(T model, Options options) {
        return remove(Collections.singletonList(model), options);
    }
    public Collection remove(List<T> models) {
        return remove(models, null);
    }
    public Collection remove(List<T> models, Options options) {
        if(options == null)
            options = new Options();

        for (int i = 0; i < models.size(); i++) {
            T m = models.get(i);
            T model = get(m);

            if(model != null) {
                byId.remove(String.valueOf(model.getId()));
                byId.remove(model.getCid());

                int index = indexOf(model);
                this.models.remove(index);

                this.length--;
                boolean silent = options.containsKey("silent") && options.<Boolean>get("silent");
                if(!silent) {
                    options.put("index", index);
                    model.trigger("remove", model, this, options);
                }
                removeReference(model);
            }
        }

        return this;
    }


    /**
     * // Update a collection by `set`-ing a new list of models, adding new ones,
     // removing models that are no longer present, and merging models that
     // already exist in the collection, as necessary. Similar to **Model#set**,
     // the core operation for updating the data contained by the collection.

     set(models: Model[], options?: CollectionSetOptions): Collection {
         options = _.defaults({}, options, setOptions);

         if (options.parse) models = this.parse(models, options);
         if (!_.isArray(models)) models = models ? [models] : [];
         var i, l, model, attrs, existing, sort;

         var at = options.at;
         var sortable = this.comparator && (at == null) && options.sort !== false;
         var sortAttr = _.isString(this.comparator) ? this.comparator : null;
         var toAdd = [], toRemove = [], modelMap = {};
         var add = options.add, merge = options.merge, remove = options.remove;
         var order = !sortable && add && remove ? [] : null;

         // Turn bare objects into model references, and prevent invalid models
         // from being added.
         for (i = 0, l = models.length; i < l; i++) {
             if (!(model = this._prepareModel(attrs = models[i], options))) continue;

             // If a duplicate is found, prevent it from being added and
             // optionally merge it into the existing model.
             if (existing = this.get(model)) {
                 if (remove) modelMap[existing.cid] = true;
                 if (merge) {
                     attrs = attrs === model ? model.attributes : options._attrs;
                     existing.set(attrs, options);
                     if (sortable && !sort && existing.hasChanged(sortAttr)) sort = true;
                 }

                 // This is a new model, push it to the `toAdd` list.
             } else if (add) {
                 toAdd.push(model);

                 // Listen to added models' events, and index models for lookup by
                 // `id` and by `cid`.
                 model.on('all', this._onModelEvent, this);
                 this._byId[model.cid] = model;
                 if (model.id != null) this._byId[model.id] = model;
             }
             if (order) order.push(existing || model);
             delete options._attrs;
         }

         // Remove nonexistent models if appropriate.
         if (remove) {
             for (i = 0, l = this.length; i < l; ++i) {
                if (!modelMap[(model = this.models[i]).cid]) toRemove.push(model);
             }
            if (toRemove.length) this.remove(toRemove, options);
         }

         // See if sorting is needed, update `length` and splice in new models.
         if (toAdd.length || (order && order.length)) {
             if (sortable) sort = true;
                this.length += toAdd.length;
             if (at != null) {
                Array.prototype.splice.apply(this.models, [at, 0].concat(toAdd));
             } else {
                 if (order) this.models.length = 0;
                 Array.prototype.push.apply(this.models, order || toAdd);
             }
         }

         // Silently sort the collection if appropriate.
         if (sort) this.sort({ silent: true });

         if (options.silent) return this;

         // Trigger `add` events.
         for (i = 0, l = toAdd.length; i < l; i++) {
            (model = toAdd[i]).trigger('add', model, this, options);
         }

         // Trigger `sort` if the collection was sorted.
         if (sort || (order && order.length)) this.trigger('sort', this, options);

         return this;
     }
     */
    public Collection set() {
        // don't do nothing... since models are null
        return this;
    }
    public Collection set(JSONArray models) {
        return set(models, null);
    }
    public Collection set(JSONArray models, Options options) {
        return set(parse(models, options), options);
    }
    public Collection set(Options ...objects) {
        return set(objects, null);
    }
    public Collection set(OptionsList objects) {
        return set(objects, null);
    }
    public Collection set(OptionsList objects, Options options) {
        return set(objects.toArray(new Options[objects.size()]), options);
    }
    public Collection set(Options[] objects, Options options) {
        if(objects == null)
            return this;

        options = new Options().defaults(options, setOptions);

        int at = options.getInt("at");
        boolean sort = false;
        boolean sortable = comparator != null && !options.containsKey("at") && (!options.containsKey("sort") || options.getBoolean("sort"));

        List<T> toAdd = new ArrayList<T>();
        List<T> toRemove = new ArrayList<T>();
        List<T> toOrder = new ArrayList<T>();
        JsMap<String, Boolean> modelMap = JsMap.create();

        boolean add = options.getBoolean("add");
        boolean merge = options.getBoolean("merge");
        boolean remove = options.getBoolean("remove");

        boolean order = !sortable && add && remove;

        // Turn bare objects into model references, and prevent invalid models
        // from being added.
        for (Options model : objects) {
            T preparedModel = prepareModel(model);

            //if(model == null)
            //    model = preparedModel;

            //if(preparedModel != null) {
            if(model != null) {
                T existing = get(preparedModel);
                // If a duplicate is found, prevent it from being added and
                // optionally merge it into the existing model.
                if(existing != null) {
                    if(remove)
                        modelMap.put(existing.getCid(), true);

                    if(merge) {
                        //Options attrs = (preparedModel == model) ? model.getAttributes() : options.<Options>get("_attrs");
                        Options attrs = model;
                        existing.set(attrs, options);

                        if(sortable && !options.getBoolean("sort"))
                            sort = true;
                    }
                } else if(add) { // This is a new model, push it to the `toAdd` list
                    toAdd.add(preparedModel);

                    // Listen to added models' events, and index models for lookup by
                    // `id` and by `cid`.
                    preparedModel.on("all", onModelEvent, this);
                    byId.put(preparedModel.getCid(), preparedModel);

                    if(!preparedModel.isNew())
                        byId.put(String.valueOf(preparedModel.getId()), preparedModel);
                }
                if (order){
                    toOrder.add(existing != null ? existing : preparedModel);
                }

                options.remove("_attrs");
            }
        }

        // Remove nonexistent models if appropriate.
        if(remove) {
            for (int i = 0; i < length; i++) {
                T model = this.models.get(i);
                if(modelMap.get(model.getCid()) == null)
                    toRemove.add(model);
            }

            if(toRemove.size() > 0)
                remove(toRemove, options);
        }

        if(toAdd.size() > 0 || (order && toOrder.size() > 0)) {
            if(sortable)
                sort = true;

            length += toAdd.size();

            if(options.containsKey("at")) {
                this.models.addAll(at, toAdd);
            } else {
                if(order)
                    this.models.clear();

                //Array.prototype.push.apply(this.models, order || toAdd);
                this.models.addAll((order && toOrder.size() > 0) ? toOrder : toAdd);
            }
        }

        if(sort)
            this.sort(new Options("silent", true));

        if(!options.getBoolean("silent")) {
            for (int i = 0; i < toAdd.size(); i++) {
                T model = toAdd.get(i);
                model.trigger("add", model, this, options);
            }

            if(sort || (order && toOrder.size() > 0)) {
                this.trigger("sort", this, options);
            }
        }

        return this;
    }


    public Collection set(T ...model) {
        return set(Arrays.asList(model), null);
    }
    public Collection set(T model, Options options) {
        return set(Collections.singletonList(model), options);
    }
    public Collection set(List<T> models) {
        return set(models, null);
    }
    public Collection set(List<T> models, Options options) {
        if(models == null)
            return this;

        options = new Options().defaults(options, setOptions);

        int at = options.getInt("at");
        boolean sort = false;
        boolean sortable = comparator != null && !options.containsKey("at") && (!options.containsKey("sort") || options.getBoolean("sort"));

        List<T> toAdd = new ArrayList<T>();
        List<T> toRemove = new ArrayList<T>();
        List<T> toOrder = new ArrayList<T>();
        JsMap<String, Boolean> modelMap = JsMap.create();

        boolean add = options.getBoolean("add");
        boolean merge = options.getBoolean("merge");
        boolean remove = options.getBoolean("remove");

        boolean order = !sortable && add && remove;

        // Turn bare objects into model references, and prevent invalid models
        // from being added.
        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);
            T preparedModel = prepareModel(model);

            if(model == null)
                model = preparedModel;

            if(preparedModel != null) {
                T existing = get(preparedModel);
                // If a duplicate is found, prevent it from being added and
                // optionally merge it into the existing model.
                if(existing != null) {
                    if(remove)
                        modelMap.put(existing.getCid(), true);

                    if(merge) {
                        Options attrs = (preparedModel == model) ? model.getAttributes() : options.<Options>get("_attrs");
                        existing.set(attrs, options);

                        if(sortable && !options.getBoolean("sort"))
                            sort = true;
                    }
                } else if(add) { // This is a new model, push it to the `toAdd` list
                    toAdd.add(model);

                    // Listen to added models' events, and index models for lookup by
                    // `id` and by `cid`.
                    model.on("all", onModelEvent, this);
                    byId.put(model.getCid(), model);

                    if(!model.isNew())
                        byId.put(String.valueOf(model.getId()), model);
                }
                if (order){
                    toOrder.add(existing != null ? existing : model);
                }

                options.remove("_attrs");
            }
        }

        // Remove nonexistent models if appropriate.
        if(remove) {
            for (int i = 0; i < length; i++) {
                T model = this.models.get(i);
                if(modelMap.get(model.getCid()) == null)
                    toRemove.add(model);
            }

            if(toRemove.size() > 0)
                remove(toRemove, options);
        }

        if(toAdd.size() > 0 || (order && toOrder.size() > 0)) {
            if(sortable)
                sort = true;

            length += toAdd.size();

            if(options.containsKey("at")) {
                this.models.addAll(at, toAdd);
            } else {
                if(order)
                    this.models.clear();

                //Array.prototype.push.apply(this.models, order || toAdd);
                this.models.addAll((order && toOrder.size() > 0) ? toOrder : toAdd);
            }
        }

        if(sort)
            this.sort(new Options("silent", true));

        if(!options.getBoolean("silent")) {
            for (int i = 0; i < toAdd.size(); i++) {
                T model = toAdd.get(i);
                model.trigger("add", model, this, options);
            }

            if(sort || (order && toOrder.size() > 0)) {
                this.trigger("sort", this, options);
            }
        }

        return this;
    }

    /**
     * // When you have more items than you want to add or remove individually,
     // you can reset the entire set with a new list of models, without firing
     // any granular `add` or `remove` events. Fires `reset` when finished.
     // Useful for bulk operations and optimizations.
     reset: function(models, options) {
         options = options ? _.clone(options) : {};

         for (var i = 0; i < this.models.length; i++) {
            this._removeReference(this.models[i], options);
         }
         options.previousModels = this.models;
         this._reset();
         models = this.add(models, _.extend({silent: true}, options));
         if (!options.silent) this.trigger('reset', this, options);

         return models;
     },
     */
    public Collection reset(JSONArray models) {
        return reset(models, null);
    }
    public Collection reset(JSONArray models, Options options) {
        return reset(parse(models, options), options);
    }
    public Collection reset(Options ...models) {
        return reset(new OptionsList(models), null);
    }
    public Collection reset(OptionsList models) {
        return reset(models, null);
    }
    public Collection reset(OptionsList models, Options options) {
        return reset(parse(models, options), options);
    }
    public Collection reset() {
        return reset(new ArrayList<T>(), null);
    }
    public Collection reset(T ...models) {
        return reset(models, null);
    }
    public Collection reset(T[] models, Options options) {
        return reset(Arrays.asList(models), options);
    }
    public Collection reset(List<T> models) {
        return reset(models, null);
    }
    public Collection reset(List<T> models, Options options) {
        options = options != null ? options.clone() : new Options();

        for (int i = 0; i < this.models.size(); i++) {
            T model = this.models.get(i);
            if (model != null) {
                removeReference(model);
            }
        }

        options.put("previousModels", this.models);
        internalReset();

        add(models, new Options("silent", true).extend(options));

        if(!options.getBoolean("silent"))
            this.trigger("reset", this, options);

        return this;
    }

    /**
     * // Add a model to the end of the collection.
     push(model: Model, options?: CollectionAddOptions): Model {
         model = this._prepareModel(model, options);
         this.add(model, _.extend({ at: this.length }, options));
         return model;
     }
     */

    public T push(Options attrs) {
        return push(attrs, null);
    }
    public T push(Options attrs, Options options) {
        T preparedModel = prepareModel(attrs, options);
        return push(preparedModel, options);
    }

    public T push(T model) {
        return push(model, null);
    }
    public T push(T model, Options options) {
        T preparedModel = prepareModel(model);
        add(preparedModel, new Options("at", this.length).extend(options));

        return preparedModel;
    }

    /**
     * // Remove a model from the end of the collection.
     pop(options?: SilentOptions): Model {
         var model = this.at(this.length - 1);
         this.remove(model, options);
         return model;
     }
     */
    public T pop() {
        return pop(null);
    }
    public T pop(Options options) {
        T model = at(this.length - 1);
        remove(model, options);

        return model;
    }

    /**
     * // Add a model to the beginning of the collection.
     unshift(model: Model, options?: CollectionAddOptions): Model {
         model = this._prepareModel(model, options);
         this.add(model, _.extend({ at: 0 }, options));
         return model;
     }
     */
    public T unshift(Options attrs) {
        return unshift(attrs, null);
    }
    public T unshift(Options attrs, Options options) {
        T preparedModel = prepareModel(attrs, options);
        return unshift(preparedModel, options);
    }
    public T unshift(T model) {
        return unshift(model, null);
    }
    public T unshift(T model, Options options) {
        T preparedModel = prepareModel(model);

        add(preparedModel, new Options("at", 0).extend(options));
        return preparedModel;
    }

    /**
     * Remove a model from the beginning of the collection.
     *
     * @return
     */
    public T shift() {
        return shift(null);
    }
    public T shift(Options options) {
        T model = at(0);
        remove(model, options);

        return model;
    }

    /**
     * // Slice out a sub-array of models from the collection.
     slice(): any[] {
        return Array.prototype.slice.apply(this.models, arguments);
     }
     */
    public List<T> slice() {
        return new ArrayList<T>(this.models);
    }
    public List<T> slice(int begin) {
        return this.models.subList(begin, length);
    }
    public List<T> slice(int begin, int end) {
        return this.models.subList(begin, end);
    }


    /**
     * // Get a model from the set by id.
     get(obj: any): Model {
         if (obj == null) return <any> void 0;
         return this._byId[obj.id] || this._byId[obj.cid] || this._byId[obj];
     }
     */
    public T get(T model) {
        T modelFromCollection = null;

        if (model != null) {
            modelFromCollection = byId.get(String.valueOf(model.getId()));
            if(modelFromCollection == null || model.getId() == -1)
                modelFromCollection = byId.get(model.getCid());
        }

        return modelFromCollection;
    }

    public T get(int id) {
        return byId.get(String.valueOf(id));
    }
    public T get(String cid) {
        return byId.get(cid);
    }
    public T get(Options object) {
        T modelFromCollection = null;

        if(object.containsKey("id") || object.containsKey("cid")) {
            String id = null;
            if(object.containsKey("id"))
                id = String.valueOf(object.getInt("id"));
            else if(object.containsKey("cid"))
                id = object.get("cid");

            modelFromCollection = byId.get(id);
        }
        return modelFromCollection;
    }

    /**
     // Get the model at the given index.
     at: function(index) {
         if (index < 0) index += this.length;
         return this.models[index];
     },
     */
    public T at(int index) {
        if(index < 0) index += this.length;
        return this.models.get(index);
    }

    /**
     * // Return models with matching attributes. Useful for simple cases of
     // `filter`.
     where(attrs: any, first: boolean): Model[] {
         if (_.isEmpty(attrs)) return first ? <any> void 0 : [];
         return this[first ? 'find' : 'filter'](function (model) {
             for (var key in attrs) {
             if (attrs[key] !== model.get(key)) return false;
             }
             return true;
         });
     }
     */
    /**
     * // Return the first model with matching attributes. Useful for simple cases
     // of `find`.
     findWhere(attrs: any): Model {
     return <any> this.where(attrs, true);
     }
     */
    public T findWhere(Options attrs) {
        if(attrs.isEmpty()) return null;

        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);

            int hasKeyCount = 0;
            Options modelAttributes = model.getAttributes();

            Set<String> keys = attrs.keySet();
            for (String attr : keys) {
                Object value = attrs.get(attr);
                if(modelAttributes.containsKey(attr) && modelAttributes.get(attr).equals(value)) {
                    hasKeyCount++;
                }
            }
            if(hasKeyCount == attrs.size())
                return model;
        }

        return null;
    }

    public List<T> where(Options attrs) {
        if(attrs.isEmpty()) return null;

        List<T> foundModels = new ArrayList<T>();

        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);
            int hasKeyCount = 0;
            Options modelAttributes = model.getAttributes();

            Set<String> keys = attrs.keySet();
            for (String attr : keys) {
                Object value = attrs.get(attr);
                if(modelAttributes.containsKey(attr) && modelAttributes.get(attr).equals(value)) {
                    hasKeyCount++;
                }
            }

            if(hasKeyCount == attrs.size())
                foundModels.add(model);
        }

        return foundModels;
    }

    /**
     * // Force the collection to re-sort itself. You don't need to call this under
     // normal circumstances, as the set will maintain sort order as each item
     // is added.
     sort(options?: SilentOptions): Collection {
         if (!this.comparator) throw new Error('Cannot sort a set without a comparator');
         options || (options = {});

         // Run sort based on type of `comparator`.
         if (_.isString(this.comparator) || this.comparator.length === 1) {
            this.models = this.sortBy(this.comparator, this);
         } else {
            this.models.sort(_.bind(this.comparator, this));
         }

         if (!options.silent) this.trigger('sort', this, options);
         return this;
     }
     */
    public Collection<T> sort() {
        return sort(null);
    }
    public Collection<T> sort(Options options) {
        if(this.comparator == null) throw new Error("Cannot sort a set without a comparator");
        if(options == null)
            options = new Options();

        //TODO: Support for sortBy with sort attribute?!
        // Run sort based on type of `comparator`.
        Collections.sort(this.models, this.comparator);

        if(!options.getBoolean("silent"))
            this.trigger("sort", this, options);
        return this;
    }

    public boolean hasComparator() {
        return this.comparator != null;
    }

    /**
     * // Figure out the smallest index at which a model should be inserted so as
     // to maintain order.
     sortedIndex(model: Model, value: (model: Model) => any, context): number {
         value || (value = this.comparator);
         var iterator = _.isFunction(value) ? value : function (model) {
            return model.get(value);
         };
         return _.sortedIndex(this.models, model, iterator, context);
     }
     */
    /**
    _.sortedIndex = function(array, obj, iteratee, context) {
        iteratee = _.iteratee(iteratee, context, 1);
        var value = iteratee(obj);
        var low = 0, high = array.length;
        while (low < high) {
          var mid = low + high >>> 1;
          if (iteratee(array[mid]) < value) low = mid + 1; else high = mid;
        }
        return low;
    };
     */
    //TODO: No idea how this can be implemented, probably by receiving the "sortAttr", so you would know what property to take
    /*public int sortedIndex(Model model) {
        int value = model.get("")
    }*/

    /**
     * // Pluck an attribute from each model in the collection.
     pluck(attr: any): any {
        return _.invoke(this.models, 'get', attr);
     }
     */
    public Object[] pluck(String attr) {
        Object[] attrs = new Object[this.length];

        for (int i = 0; i < this.models.size(); i++) {
            T model = this.models.get(i);
            attrs[i] = model.get(attr);
        }
        return attrs;
    }

    /**
     * Will support only primitive and JSO objects as V, or else cast will fail
     *
     * @param attr
     * @param <V>
     * @return
     */
    public <V> JsArray<V> jsPluck(String attr) {
        JsArray<V> array = JsArray.create();

        for (int i = 0; i < this.models.size(); i++) {
            T model = this.models.get(i);
            array.add(i, (V)model.get(attr));
        }
        return array;
    }

    /**
     * Return the results of applying the iteratee to each element.
     *
     * @param applyFunction
     * @return
     */
    public <K> JsArray<K> jsMap(MapFunction<K, T> applyFunction) {
        if(applyFunction == null)
            return JsArray.create();

        JsArray<K> attrs = JsArray.create();

        for (int i = 0; i < this.models.size(); i++) {
            T model = this.models.get(i);
            attrs.add(i, applyFunction.f(model, i, this.models));
        }
        return attrs;
    }

    public <K> List<K> map(MapFunction<K, T> applyFunction) {
        if(applyFunction == null)
            return new ArrayList<K>();

        List<K> attrs = new ArrayList<K>();

        for (int i = 0; i < this.models.size(); i++) {
            T model = this.models.get(i);
            attrs.add(i, applyFunction.f(model, i, this.models));
        }
        return attrs;
    }

    /**
     *
     * @return
     */
    public Boolean any() {
        return size() > 0;
    }
    public Boolean any(FilterFunction<T> applyFunction) {
        return filter(applyFunction).size() > 0;
    }

    /**
     *
     * @return
     */
    public Boolean isEmpty() {
        return !any();
    }

    /**
     *
     * @return
     */
    public int size() {
        return this.length;
    }

    /**
     *
     * @return
     */
    public List<T> rest() {
        return rest(1);
    }
    public List<T> rest(int index) {
        return index == -1 ? slice(size() - 1) : slice(index);
    }

    /**
     *
     * @param models
     * @return
     */
    public List<T> without(T ...models) {
        List<T> result = new ArrayList<T>();

        for (int i = 0; i < this.length; i++) {
            boolean includeModel = true;
            T model = this.models.get(i);

            for (T withoutModel : models) {
                if (model.equals(withoutModel) || model.getId() == withoutModel.getId()) {
                    includeModel = false;
                    break;
                }
            }
            if(includeModel)
                result.add(model);
        }

        return result;
    }

    /**
     *
     * @param applyFunction
     * @return
     */
    public T max(MinMaxFunction<T> applyFunction) {
        int maxValue = Integer.MIN_VALUE;
        T result = null;

        if (applyFunction != null) {
            for (int i = 0; i < this.models.size(); i++) {
                T model = this.models.get(i);
                int value = applyFunction.f(model, i, this.models);
                if(value > maxValue) {
                    result = model;
                    maxValue = value;
                }
            }
        }
        return result;
    }

    /**
     *
     * @param applyFunction
     * @return
     */
    public T min(MinMaxFunction<T> applyFunction) {
        int minValue = Integer.MAX_VALUE;
        T result = null;

        if (applyFunction != null) {
            for (int i = 0; i < this.models.size(); i++) {
                T model = this.models.get(i);
                int value = applyFunction.f(model, i, this.models);
                if(value < minValue) {
                    result = model;
                    minValue = value;
                }
            }
        }
        return result;
    }

    /**
     *
     * @param applyFunction
     * @return
     */
    public List<T> filter(FilterFunction<T> applyFunction) {
        if(applyFunction == null)
            return new ArrayList<T>();

        List<T> result = new ArrayList<T>();

        for (int i = 0; i < this.models.size(); i++) {
            T model = this.models.get(i);

            boolean includeModel = applyFunction.f(model, i, this.models);
            if(includeModel)
                result.add(model);
        }
        return result;
    }

    /**
     *
     * @param models
     * @return
     */
    public List<T> difference(T[] models) {
        return without(models);
    }

    /**
     *
     * @param model
     * @return
     */
    public boolean contains(Model model) {
        return contains(model, 0);
    }

    /**
     *
     * @param model
     * @param fromIndex
     * @return
     */
    public boolean contains(Model model, int fromIndex) {

        if (model != null) {
            if(fromIndex < 0)
                fromIndex = 0;

            for (int i = fromIndex; i < models.size(); i++) {
                Model existingModel = models.get(i);
                if (model.equals(existingModel) || model.getId() == existingModel.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public T sample() {
        return isEmpty() ? null : sample(1).get(0);
    }

    /**
     *
     * @param quantity
     * @return
     */
    public List<T> sample(int quantity) {
        List<T> result = new ArrayList<T>();
        JsArray<Integer> indexes = getRandomIndexes(quantity);

        for (int i = 0; i < indexes.length(); i++) {
            Integer randomIndex = indexes.getInt(i);
            result.add(models.get(randomIndex));
        }
        return result;
    }

    /**
     *
     * @param attribute
     * @return
     */
    public Options indexBy(String attribute) {
        Options result = new Options();

        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            if(model.has(attribute)) {
                String valueAsKey = String.valueOf(model.get(attribute));
                result.put(valueAsKey, model);
            }
        }
        return result;
    }

    /**
     *
     * @param key
     * @return
     */
    public <K> Map<K, List<T>> groupBy(final String key) {
        return groupBy(new MapFunction<K, T>() {
            @Override
            public K f(T model, int index, List<T> models) {
                return model.get(key);
            }
        });
    }

    /**
     * Splits a collection into sets, grouped by the result of running each value through iteratee.
     *
     * @param applyFunction
     * @param <K>
     * @return
     */
    public <K> Map<K, List<T>> groupBy(MapFunction<K, T> applyFunction) {
        if(applyFunction == null)
            return new HashMap<K, List<T>>();

        Map<K, List<T>> result = new HashMap<K, List<T>>();

        for (int i = 0; i < this.models.size(); i++) {
            List<T> groupedModels;
            T model = this.models.get(i);

            K key = applyFunction.f(model, i, this.models);
            if(result.containsKey(key)) {
                groupedModels = result.get(key);
            } else {
                groupedModels = new ArrayList<T>();
                result.put(key, groupedModels);
            }
            groupedModels.add(model);
        }
        return result;
    }

    /**
     *
     * @param key
     * @param <K>
     * @return
     */
    public <K extends Comparable<K>> List<T> sortBy(final String key) {
        return sortBy(new SortFunction<Comparable, T>() {
            @Override
            public K f(T model) {
                return model.get(key);
            }
        });
    }

    /**
     *
     * @param applyFunction
     * @param <K>
     * @return
     */
    public <K extends Comparable<K>> List<T> sortBy(final SortFunction<K, T> applyFunction) {
        if(applyFunction == null)
            return new ArrayList<T>(this.models);

        List<T> result = new ArrayList<T>(this.models);

        Collections.sort(result, new Comparator<T>() {
            @Override
            public int compare(T model1, T model2) {
                K value1 = applyFunction.f(model1);
                K value2 = applyFunction.f(model2);
                return value1.compareTo(value2);
            }
        });
        return result;
    }

    private JsArray<Integer> getRandomIndexes(int quantity) {
        return getRandomIndexes(quantity, JsArray.<Integer>create());
    }
    private JsArray<Integer> getRandomIndexes(int quantity, JsArray<Integer> excludes) {
        int number;

        if(excludes.length() == quantity)
            return excludes;

        do {
            number = Random.nextInt(size());
        } while (excludes.contains(number));

        return getRandomIndexes(quantity, excludes.add(number));
    }

    /**
     * // Fetch the default set of models for this collection, resetting the
     // collection when they arrive. If `reset: true` is passed, the response
     // data will be passed through the `reset` method instead of `set`.
     fetch(options?: CollectionFetchOptions): JQueryXHR
     fetch(options?: any): JQueryXHR {
         options = options ? _.clone(options) : {};
         if (options.parse === void 0) options.parse = true;
         var success = options.success;
         var collection = this;
         options.success = function (resp) {
             var method = options.reset ? 'reset' : 'set';
             collection[method](resp, options);

             if (success) success(collection, resp, options);
             collection.trigger('sync', collection, resp, options);
         };
         Helpers.wrapError(this, options);
         return this.sync('read', this, options);
     }
     */
    public Promise fetch() {
        return fetch(new Options());
    }
    public Promise fetch(final Options options) {

        final Function success = options.get("success");
        options.put("success", new Function() {
            @Override
            public void f() {
                JSONArray response = getArgument(1);

                if(options.getBoolean("reset")) {
                    Collection.this.reset(response, options);
                } else {
                    Collection.this.set(response, options);
                }

                if(success != null) {
                    success.f(Collection.this, response, options);
                }
                Collection.this.trigger("sync", Collection.this, response, options);
            }
        });

        final Function error = options.get("error");
        options.put("error", new Function() {
            @Override
            public void f() {
                JavaScriptObject response = getArgument(1);
                if(error != null) {
                    error.f(Collection.this, response, options);
                }
                Collection.this.trigger("error", Collection.this, response, options);
            }
        });
        return sync("read", options);
    }

    /**
     // Create a new instance of a model in this collection. Add the model to the
     // collection immediately, unless `wait: true` is passed, in which case we
     // wait for the server to agree.
     create(model: any, options?: ModelSaveOptions): Model
     create(model: any, options?: any): Model {
         options = options ? _.clone(options) : {};

         if (!(model = this._prepareModel(model, options))) return <any> false;
         if (!options.wait) this.add(model, options);

         var collection = this;
         var success = options.success;

         options.success = function (model, resp, options) {
             if (options.wait) collection.add(model, options);
             if (success) success(model, resp, options);
         };
         model.save(null, options);

         return model;
     }
     */
    public T create(Options attrs) {
        return create(attrs, null);
    }
    public T create(Options attrs, Options options) {
        T preparedModel = prepareModel(attrs, options);

        return create(preparedModel, options);
    }

    public T create(T model) {
        return create(model, null);
    }
    public T create(final T model, Options options) {
        if(options == null)
            options = new Options();

        T preparedModel = prepareModel(model);
        if(preparedModel == null)
            return null;

        if(!options.getBoolean("wait"))
            this.add(model, options);

        final Options finalOptions = options;
        final Function success = options.get("success");

        options.put("success", new Function() {
            @Override
            public void f() {
                if(finalOptions.getBoolean("wait"))
                    add(model, finalOptions);

                if(success != null)
                    success.f(getArguments());
            }
        });

        preparedModel.save(null, options);

        return preparedModel;
    }

    /**
     * // **parse** converts a response into a list of models to be added to the
     // collection. The default implementation is just to pass it through.
     parse(resp: any, options: any): any {
        return resp;
     }
     */
    public List<T> parse(List<Options> models, Options options) {
        return parse(new OptionsList(models), options);
    }

    public List<T> parse(JSONValue resp, Options options) {
        JSONArray array = resp != null ? resp.isArray() : new JSONArray();

        if(resp != null && resp.isObject() != null) {
            array.set(0, resp.isObject());
        }
        return parse(new OptionsList(array), options);
    }

    public List<T> parse(OptionsList models, Options options) {
        List<T> result = new ArrayList<T>();

        if(options == null)
            options = new Options();

        for (Options attributes : models) {
            T model = prepareModel(attributes, options);
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    /**
     * // Create a new collection with an identical list of models as this one.
     clone(): Collection {
        return new (<any> this).constructor(this.models);
     }
     */
    public Collection<T> clone() {
        Collection<T> result = new Collection<T>(modelClass, models);
        result.comparator = comparator;

        return result;
    }

    /**
     * // Private method to reset all internal state. Called when the collection
     // is first initialized or reset.
     _reset() {
         this.length = 0;
         this.models = [];
         this._byId = {};
     }
     */
    private void internalReset() {
        this.length = 0;
        this.models = new ArrayList<T>();
        this.byId = new HashMap<String, T>();
    }

    /**
     * // Prepare a hash of attributes (or other model) to be added to this
     // collection.
     _prepareModel(attrs?: any, options?: ModelOptions): Model {
         if (attrs instanceof Model) {
             if (!attrs.collection) attrs.collection = this;
                return attrs;
         }
         options || (options = {});
         options.collection = this;
         var model = new this.model(attrs, options);

         if (!model.validationError) return model;

         this.trigger('invalid', this, attrs, options);
         return <any> false;
     }
     */
    private T prepareModel(Options attrs) {
        return prepareModel(attrs, null);
    }
    private T prepareModel(Options attrs, Options options) {
        if(options == null)
            options = new Options();

        options.put("collection", this);

        T model = instantiateModel(attrs, options);

        Object validationError = model.getValidationError();
        if (validationError == null || (validationError instanceof Boolean && ((Boolean) validationError))) return model;
        this.trigger("invalid", this, attrs, options);

        return null;
    }
    private T prepareModel(T attrs) {
        if(attrs == null)
            attrs = instantiateModel();

        if (attrs.getCollection() == null)
            attrs.setCollection(this);
        return attrs;
    }

    private T instantiateModel() {
        return instantiateModel(null, null);
    }
    private T instantiateModel(Options attributes) {
        return instantiateModel(attributes, null);
    }
    private T instantiateModel(Options attributes, Options options) {
        T model = null;
        if (modelClass != null) {
            model = GWT.<Reflection>create(Reflection.class).instantiateModel(modelClass, attributes, options);
        } else {
            model = (T)new Model(attributes, options);
        }
        return model;
    }

    private T[] instantiateModelArray(int length) {
        T[] models = null;
        if (modelClass != null) {
            models = GWT.<Reflection>create(Reflection.class).instantiateArray(modelClass, length);
        } else {
            models = (T[]) new Model[length];
        }
        return models;
    }

    /**
     * // Internal method to sever a model's ties to a collection.
     _removeReference(model: Model): void {
         if (this === model.collection) delete model.collection;
         model.off('all', this._onModelEvent, this);
     }
     */
    private void removeReference(Model model) {
        if(model.getCollection() != null && model.getCollection().equals(this))
            model.setCollection(null);

        model.off("all", onModelEvent, this);
    }

    /**
     * // Internal method called every time a model in the set fires an event.
     // Sets need to update their indexes when models change ids. All other
     // events simply proxy through. "add" and "remove" events that originate
     // in other collections are ignored.
     _onModelEvent(event: string, model: Model, collection: Collection, options: any) {
         if ((event === 'add' || event === 'remove') && collection !== this) return;
         if (event === 'destroy') this.remove(model, options);
         if (model && event === 'change:' + model.idAttribute) {
             delete this._byId[model.previous(model.idAttribute)];
             if (model.id != null) this._byId[model.id] = model;
         }
         this.trigger.apply(this, arguments);
     }
     */
    private Function onModelEvent = new Function() {
        @Override
        public void f() {
            String event = getArgument(0);
            T model = getArgument(1);

            Collection collection = null;
            if (arguments.length >= 3 && arguments[2] instanceof Collection) {
                collection = getArgument(2);
            }
            Options options = getArgument(3);

            if(event != null) {
                if (collection != null) {
                    if((event.equals("add") || event.equals("remove")) && !collection.equals(Collection.this))
                        return;
                }

                if(model != null) {
                    if(event.equals("destroy"))
                        remove(model, options);

                    if(event.equals("change:" + model.getIdAttribute())) {
                        byId.remove(String.valueOf(model.previous(model.getIdAttribute())));
                        if(!model.isNew())
                            byId.put(String.valueOf(model.getId()), model);
                    }
                }
                trigger(event, getArguments());
            }
        }
    };


    /**
     *
     all: (iterator: (element: Model, index: number) => boolean, context?: any) => boolean;
     any: (iterator: (element: Model, index: number) => boolean, context?: any) => boolean;
     collect: (iterator: (element: Model, index: number, context?: any) => any[], context?: any) => any[];
     chain: () => any;
     compact: () => Model[];
     contains: (value: any) => boolean;
     countBy: (iterator: (element: Model, index: number) => any) => any[];
     detect: (iterator: (item: any) => boolean, context?: any) => any; // ???
     difference: (...model: Model[]) => Model[];
     drop: (n?: number) => Model[];
     each: (iterator: (element: Model, index: number, list?: any) => void , context?: any) => void;
     every: (iterator: (element: Model, index: number) => boolean, context?: any) => boolean;
     filter: (iterator: (element: Model, index: number) => boolean, context?: any) => Model[];
     find: (iterator: (element: Model, index: number) => boolean, context?: any) => Model;
     first: (n?: number) => Model[];
     flatten: (shallow?: boolean) => Model[];
     foldl: (iterator: (memo: any, element: Model, index: number) => any, initialMemo: any, context?: any) => any;
     forEach: (iterator: (element: Model, index: number, list?: any) => void , context?: any) => void;
     include: (value: any) => boolean;
     indexOf: (element: Model, isSorted?: boolean) => number;
     initial: (n?: number) => Model[];
     inject: (iterator: (memo: any, element: Model, index: number) => any, initialMemo: any, context?: any) => any;
     intersection: (...model: Model[]) => Model[];
     isEmpty: (object: any) => boolean;
     invoke: (methodName: string, arguments?: any[]) => any;
     last: (n?: number) => Model[];
     lastIndexOf: (element: Model, fromIndex?: number) => number;
     map: (iterator: (element: Model, index: number, context?: any) => any[], context?: any) => any[];
     max: (iterator?: (element: Model, index: number) => any, context?: any) => Model;
     min: (iterator?: (element: Model, index: number) => any, context?: any) => Model;
     object: (...values: any[]) => any[];
     reduce: (iterator: (memo: any, element: Model, index: number) => any, initialMemo: any, context?: any) => any;
     select: (iterator: any, context?: any) => any[];
     size: () => number;
     shuffle: () => any[];
     some: (iterator: (element: Model, index: number) => boolean, context?: any) => boolean;
     sortBy: (iterator: (element: Model, index: number) => number, context?: any) => Model[];
     reduceRight: (iterator: (memo: any, element: Model, index: number) => any, initialMemo: any, context?: any) => any[];
     reject: (iterator: (element: Model, index: number) => boolean, context?: any) => Model[];
     rest: (n?: number) => Model[];
     tail: (n?: number) => Model[];
     toArray: () => any[];
     union: (...model: Model[]) => Model[];
     uniq: (isSorted?: boolean, iterator?: (element: Model, index: number) => boolean) => Model[];
     without: (...values: any[]) => Model[];
     zip: (...model: Model[]) => Model[];
     */
    //TODO: Support for underscore methods

    /**
     _.indexOf = function(array, item, isSorted) {
         if (array == null) return -1;
         var i = 0, length = array.length;

         if (isSorted) {
             if (typeof isSorted == 'number') {
                i = isSorted < 0 ? Math.max(0, length + isSorted) : isSorted;
             } else {
                 i = _.sortedIndex(array, item);
                 return array[i] === item ? i : -1;
             }
         }
         for (; i < length; i++) if (array[i] === item) return i;

         return -1;
     };
     */
    public int indexOf(T item) {
        return indexOf(item, false);
    }

    public int indexOf(T item, int sortIndex) {
        int i = 0, length = this.length;

        i = sortIndex < 0 ? Math.max(0, length + sortIndex) : sortIndex;
        for (; i < length; i++) if (at(i).equals(item)) return i;

        return -1;
    }

    public int indexOf(T item, boolean isSorted) {
        int i = 0, length = this.length;

        if(isSorted) {
            i = sortedIndex(item);
            return at(i).equals(item) ? i : -1;
        }
        for (; i < length; i++) if (at(i).equals(item)) return i;

        return -1;
    }

    /**
     *
     * // Use a comparator function to figure out the smallest index at which
     // an object should be inserted so as to maintain order. Uses binary search.
     _.sortedIndex = function(array, obj) {

         var low = 0, high = array.length;
         while (low < high) {
             var mid = low + high >>> 1;
             if (array[mid] < obj) low = mid + 1; else high = mid;
         }
         return low;
     };
     */
    public int sortedIndex(T item) {
        int low = 0;
        int high = this.length;

        while(low < high) {
            int mid = low + high >>> 1;
            if(this.comparator.compare(at(mid), item) < 0)
                low = mid + 1;
            else
                high = mid;
        }
        return low;
    }

    public T first() {
        if(this.models.size() > 0) {
            return this.models.get(0);
        }
        return null;
    }

    public T last() {
        if(this.models.size() > 0) {
            return this.models.get(this.models.size() - 1);
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return size() > i;
            }

            @Override
            public T next() {
                return models.get(i++);
            }

            @Override
            public void remove() {
                Collection.this.remove(models.get(i));
            }
        };
    }
}
