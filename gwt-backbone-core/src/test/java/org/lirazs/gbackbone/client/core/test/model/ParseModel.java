package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;


public class ParseModel extends Model {

    @Override
    protected Options parse(JSONObject resp, Options options) {
        double value = resp.get("value").isNumber().doubleValue();
        resp.put("value", new JSONNumber(value + 1));

        return super.parse(resp, options);
    }
}
