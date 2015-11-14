package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 25/09/2015.
 */
public class JobItemModel extends Model {

    private Collection subItems;

    public JobItemModel() {
        super();
    }

    public JobItemModel(JSONObject model) {
        super(model);
    }

    public JobItemModel(Options attributes) {
        super(attributes);
    }

    public Collection getSubItems() {
        return subItems;
    }

    @Override
    protected Options parse(JSONValue resp, Options options) {
        JSONObject object = resp.isObject();

        subItems = new Collection();
        subItems.set(object.get("subItems"));
        object.put("subItems", null);

        return super.parse(resp, options);
    }
}
