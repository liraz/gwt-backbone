package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;


public class ParseModel extends Model {

    public ParseModel(JSONObject model) {
        super(model);
    }

    @Override
    protected Options parse(JSONValue resp, Options options) {
        JSONObject object = resp.isObject();
        double value = object.get("value").isNumber().doubleValue();
        object.put("value", new JSONNumber(value + 1));

        return super.parse(resp, options);
    }
}
