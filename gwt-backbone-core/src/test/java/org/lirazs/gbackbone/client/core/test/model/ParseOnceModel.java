package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;


public class ParseOnceModel extends Model {

    public ParseOnceModel(JSONObject model) {
        super(model);
    }

    @Override
    protected Options parse(JSONValue resp, Options options) {
        JSONObject object = resp.isObject();

        assert !object.containsKey("parsed");
        object.put("parsed", new JSONNumber(1));

        return super.parse(resp, options);
    }
}
