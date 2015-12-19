package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.net.Sync;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 23/10/2015.
 */
public class GBackboneSyncTestGwt extends GWTTestCase {

    public String getModuleName() {
        return "org.lirazs.gbackbone.GBackboneTest";
    }

    private Collection<Model> library;
    private Options attrs = O(
            "title", "The Tempest",
            "author", "Bill Shakespeare",
            "length", 123
    );

    public void gwtSetUp() {
        library = new Collection<Model>();
        library.setUrl("/library");
        library.create(attrs, O("wait", false));
    }

    public void gwtTearDown() {

    }

    public void testRead() {
        library.fetch();

        Ajax.Settings syncArgs = Sync.get().getSyncArgs();
        assertEquals("/library", syncArgs.getUrl());
        assertEquals("GET", syncArgs.getType());
        assertEquals("json", syncArgs.getDataType());

        String data = JsonUtils.stringify((JavaScriptObject) syncArgs.getData());
        assertEquals("{}", data); // making sure it's an empty JSON string
    }

    public void testPassingData() {
        library.fetch(O("data", O("a", "a", "one", 1)));

        Ajax.Settings syncArgs = Sync.get().getSyncArgs();
        assertEquals("/library", syncArgs.getUrl());

        String data = JsonUtils.stringify((JavaScriptObject) syncArgs.getData());
        Options dataOptions = O(JSONParser.parseStrict(data)).get("data");

        assertEquals("a", dataOptions.get("a"));
        assertEquals(1, dataOptions.get("one"));
    }
}
