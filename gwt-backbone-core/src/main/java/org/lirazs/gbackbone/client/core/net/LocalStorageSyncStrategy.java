package org.lirazs.gbackbone.client.core.net;

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.plugins.deferred.Deferred;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.data.OptionsList;
import org.lirazs.gbackbone.client.core.js.JsArray;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.util.UUID;

/**
 * Created on 21/01/2016.
 */
public class LocalStorageSyncStrategy implements SyncStrategy {
    // the namespace we save all the data under..
    private String namespace;
    // list of the record ids that were saved
    private JsArray<String> records;

    public LocalStorageSyncStrategy(String namespace) {
        this.namespace = namespace;

        String store = getItem(namespace);
        this.records = store != null ?  JsArray.<String>create().add(store.split(",")) : JsArray.<String>create();
    }

    @Override
    public Promise sync(String method, Synchronized syncModel, Options options) {
        JSONValue response = null;
        String errorMessage = null;

        Deferred syncDfd = new Deferred();

        try {
            if(method.equals("read")) {
                if(syncModel instanceof Model) {
                    Model model = (Model) syncModel;
                    response = find(model.getId()).toJsonValue();

                } else if(syncModel instanceof Collection) {
                    response = findAll().toJsonValue();
                }
            } else if(method.equals("create")) {
                response = create((Options) syncModel.toJSON()).toJsonValue();
            } else if(method.equals("update")) {
                response = update((Options) syncModel.toJSON()).toJsonValue();
            } else if(method.equals("delete")) {
                response = destroy((Options) syncModel.toJSON()).toJsonValue();
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }

        if(response != null) {
            syncModel.trigger("sync", syncModel, response, options);

            if(options != null && options.containsKey("success")) {
                Function success = options.get("success");
                success.f(response);
            }
            syncDfd.resolve(response);
        } else {
            errorMessage = errorMessage != null ? errorMessage : "Record Not Found";

            if(options != null && options.containsKey("error")) {
                Function error = options.get("error");
                error.f(errorMessage);
            }
            syncDfd.reject(errorMessage);
        }

        // add compatibility with $.ajax
        // always execute callback for success and error
        if(options != null && options.containsKey("complete")) {
            Function complete = options.get("complete");
            complete.f(response);
        }

        return syncDfd.promise();
    }

    /**
     * Save the current state of the **Store** to *localStorage*.
     */
    public void save() {
        setItem(namespace, records.join(","));
    }

    /**
     * Add a model, giving it a (hopefully)-unique GUID, if it doesn't already
     * have an id of it's own.
     *
     * @param model
     */
    public Options create(Options model) {
        if(!model.containsKey("id")) {
            model.put("id", UUID.uuid());
        }

        setItem(namespace + "-" + model.get("id"), model.toJsonString());
        records.add(model.get("id", String.class));
        save();

        return find(model.get("id", String.class));
    }

    /**
     * Update a model by replacing its copy in `this.data`.
     *
     * @param model
     * @return
     */
    public Options update(Options model) {
        setItem(namespace + "-" + model.get("id"), model.toJsonString());

        if(!records.contains(model.get("id"))) {
            records.add(model.get("id", String.class));
            save();
        }

        return find(model.get("id", String.class));
    }

    /**
     * Retrieve a model from `this.data` by id.
     *
     * @param modelId
     * @return
     */
    public Options find(String modelId) {
        String data = getItem(namespace + "-" + modelId);
        return jsonData(data);
    }

    /**
     * Return the array of all models currently in storage.
     *
     * @return
     */
    public OptionsList findAll() {
        OptionsList optionsList = new OptionsList();
        for (int i = 0; i < records.length(); i++) {
            String id = records.get(i);
            String data = getItem(namespace + "-" + id);

            if (id != null && data != null) {
                optionsList.add(jsonData(data));
            }
        }
        return optionsList;
    }

    /**
     * Delete a model from `this.data`, returning it.
     *
     * @param model
     * @return
     */
    public Options destroy(Options model) {
        if(!model.containsKey("id"))
            return null;

        removeItem(namespace + "-" + model.get("id"));
        records.remove(model.get("id"));

        save();
        return model;
    }

    private Options jsonData(String data) {
        return data != null ? Options.O(JSONParser.parseStrict(data)) : null;
    }

    private native String getItem(String key) /*-{
        return $wnd.localStorage.getItem(key);
    }-*/;

    private native void setItem(String key, String data) /*-{
        $wnd.localStorage.setItem(key, data);
    }-*/;

    private native void removeItem(String key) /*-{
        $wnd.localStorage.removeItem(key);
    }-*/;
}
