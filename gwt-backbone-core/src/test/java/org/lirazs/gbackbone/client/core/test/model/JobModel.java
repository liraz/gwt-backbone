package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

/**
 * Created on 25/09/2015.
 */
public class JobModel extends Model {

    private Collection<JobItemModel> items;

    public JobModel() {
        super();
    }

    public JobModel(Options attributes) {
        super(attributes);
    }

    public JobModel(JSONObject model) {
        super(model);
    }

    public Collection<JobItemModel> getItems() {
        return items;
    }

    @Override
    protected Options parse(JSONValue resp, Options options) {
        JSONObject object = resp.isObject();

        items = new Collection<JobItemModel>(JobItemModel.class);
        items.set(object.get("items"));
        object.put("items", null);

        return super.parse(resp, options);
    }
}
