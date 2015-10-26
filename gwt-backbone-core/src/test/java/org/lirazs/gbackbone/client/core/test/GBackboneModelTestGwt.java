package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.junit.client.GWTTestCase;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.function.UrlRootFunction;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.test.model.ModelIncrementValue;
import org.lirazs.gbackbone.client.core.test.model.ModelOne;
import org.lirazs.gbackbone.client.core.test.model.ModelOneFromOptions;

import java.util.Arrays;

/**
 * Created on 23/10/2015.
 */
public class GBackboneModelTestGwt extends GWTTestCase {

    private Collection<Model> collection;
    private Model doc;

    public String getModuleName() {
        return "org.lirazs.gbackbone.GBackboneTest";
    }

    public void gwtSetUp() {
        doc = new Model(new Options(
                "id"     , "1-the-tempest",
                "title"  , "The Tempest",
                "author" , "Bill Shakespeare",
                "length" , 123
        ));
        collection = new Collection<Model>();
        collection.setUrl("/collection");
        collection.add(doc);
    }

    public void gwtTearDown() {

    }

    public void testInitialize() {
        ModelOne model = new ModelOne(new Options(), new Options("collection", collection));
        assertEquals(1, model.getOne());
        assertEquals(collection, model.getCollection());
    }

    public void testInitializeWithAttributesAndOptions() {
        ModelOneFromOptions model = new ModelOneFromOptions(new Options(), new Options("one", 1));
        assertEquals(1, model.getOne());
    }

    public void testInitializeWithParsedAttributes() {
        JSONObject o = new JSONObject();
        o.put("value", new JSONNumber(1));

        ModelIncrementValue model = new ModelIncrementValue(o);
        assertEquals(2, model.get("value"));
    }

    public void testInitializeWithDefaults() {
        class DefaultsModel extends Model {
            public DefaultsModel(Options attributes) {
                super(attributes);
            }

            @Override
            protected Options defaults() {
                return new Options(
                        "first_name", "Unknown",
                        "last_name", "Unknown"
                );
            }
        }
        DefaultsModel model = new DefaultsModel(new Options("first_name", "John"));
        assertEquals("John", model.get("first_name"));
        assertEquals("Unknown", model.get("last_name"));
    }

    public void testParseCanReturnNull() {
        class NullParseModel extends Model {
            public NullParseModel(JSONObject model) {
                super(model);
            }

            @Override
            protected Options parse(JSONObject resp, Options options) {
                resp.put("value", new JSONNumber(resp.get("value").isNumber().doubleValue() + 1));
                return null;
            }
        }

        JSONObject o = new JSONObject();
        o.put("value", new JSONNumber(1));

        NullParseModel model = new NullParseModel(o);
        assertEquals("{}", model.toJSON().toJsonString());
    }

    public void testUrl() {
        assertEquals("/collection/1-the-tempest", doc.getUrl());
        doc.getCollection().setUrl("/collection/");
        assertEquals("/collection/1-the-tempest", doc.getUrl());
        doc.setCollection(null);

        try {
            doc.getUrl();
        } catch (Exception e) {
            assertNotNull(e);
        }
        doc.setCollection(collection);
    }

    public void testUrlWhenUsingUrlRooAndUriEncoding() {
        Model model = new Model();
        model.setUrlRoot("/collection");
        assertEquals("/collection", model.getUrl());

        model.set("id", "+1+");
        assertEquals("/collection/%2B1%2B", model.getUrl());
    }

    public void testUrlWhenUsingUrlRootAsAFunctionToDetermineUrlRootAtRuntime() {
        Model model = new Model(new Options("parent_id", 1));
        model.setUrlRoot(new UrlRootFunction() {
            @Override
            public String f(Model model) {
                return "/nested/" + model.get("parent_id") + "/collection";
            }
        });
        assertEquals("/nested/1/collection", model.getUrl());
        model.set(new Options("id", 2));
        assertEquals("/nested/1/collection/2", model.getUrl());
    }

    public void testUnderscoreMethods() {
        Model model = new Model(new Options("foo", "a", "bar", "b", "baz", "c"));
        Model model2 = model.clone();

        assertEquals(Arrays.asList("foo", "bar", "baz"), Arrays.asList(model.keys()));
        assertEquals(Arrays.asList("a", "b", "c"), Arrays.asList(model.values()));
        assertEquals(new Options("a", "foo", "b", "bar", "c", "baz"), model.invert());
        assertEquals(new Options("foo", "a", "baz", "c"), model.pick("foo", "baz"));
        assertEquals(new Options("baz", "c"), model.omit("foo", "bar"));
    }

    public void testChain() {
        Model model = new Model(new Options("a", 0, "b", 1, "c", 2));
        assertEquals(Arrays.asList(1, 2), model.chain().pick("a", "b", "c").values().compact().getListValue());
    }

    public void testClone() {
        Model a = new Model(new Options("foo", 1, "bar", 2, "baz", 3));
        Model b = a.clone();

        assertEquals(1, a.get("foo"));
        assertEquals(2, a.get("bar"));
        assertEquals(3, a.get("baz"));
        assertEquals(a.get("foo"), b.get("foo")); // Foo should be the same on the clone.
        assertEquals(a.get("bar"), b.get("bar")); // Bar should be the same on the clone.
        assertEquals(a.get("baz"), b.get("baz")); // Baz should be the same on the clone.

        a.set(new Options("foo", 100));
        assertEquals(100, a.get("foo"));
        assertEquals(1, b.get("foo")); // Changing a parent attribute does not change the clone.

        Model foo = new Model(new Options("p", 1));
        Model bar = new Model(new Options("p", 2));

        bar.set(foo.clone().getAttributes(), new Options("unset", true));
        assertEquals(1, foo.get("p"));
        assertEquals(null, bar.get("p"));
    }

    public void testIsNew() {
        Model a = new Model(new Options("foo", 1, "bar", 2, "baz", 3));
        assertTrue("It should be new", a.isNew());

        a = new Model(new Options("foo", 1, "bar", 2, "baz", 3, "id", -5));
        assertFalse("any defined ID is legal, negative or positive", a.isNew());

        a = new Model(new Options("foo", 1, "bar", 2, "baz", 3, "id", 0));
        assertFalse("any defined ID is legal, including zero", a.isNew());

        assertTrue("is true when there is no id", new Model().isNew());
        assertFalse("is false for a positive integer", new Model(new Options("id", 2)).isNew());
        assertFalse("is false for a negative integer", new Model(new Options("id", -5)).isNew());
    }

    public void testGet() {
        assertEquals("The Tempest", doc.get("title"));
        assertEquals("Bill Shakespeare", doc.get("author"));
    }

    public void testEscape() {
        assertEquals("The Tempest", doc.escape("title"));

        doc.set(new Options("audience", "Bill & Bob"));
        assertEquals("Bill &amp; Bob", doc.escape("audience"));

        doc.set(new Options("audience", "Tim > Joan"));
        assertEquals("Tim &gt; Joan", doc.escape("audience"));

        doc.set(new Options("audience", 10101));
        assertEquals("10101", doc.escape("audience"));

        doc.unset("audience");
        assertEquals("", doc.escape("audience"));
    }

    public void testHas() {
        Model model = new Model();
        assertFalse(model.has("name"));

        model.set(new Options(
                "0", 0,
                "1", 1,
                "true", true,
                "false", false,
                "empty", "",
                "name", "name",
                "null", null
        ));

        assertTrue(model.has("0"));
        assertTrue(model.has("1"));
        assertTrue(model.has("true"));
        assertTrue(model.has("false"));
        assertTrue(model.has("empty"));
        assertTrue(model.has("name"));

        model.unset("name");

        assertFalse(model.has("name"));
        assertFalse(model.has("null"));
    }
}
