package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.json.client.*;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import static org.lirazs.gbackbone.client.core.data.Options.O;
import org.lirazs.gbackbone.client.core.data.OptionsList;
import static org.lirazs.gbackbone.client.core.data.OptionsList.OL;
import org.lirazs.gbackbone.client.core.function.MatchesFunction;
import org.lirazs.gbackbone.client.core.function.UrlRootFunction;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.net.NetworkSyncStrategy;
import org.lirazs.gbackbone.client.core.test.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created on 23/10/2015.
 */
public class GwtTestCoreModel extends GWTTestCase {

    private Collection<Model> collection;
    private Model doc;

    public String getModuleName() {
        return "org.lirazs.gbackbone.CoreTest";
    }

    public void gwtSetUp() {
        doc = new Model(O(
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
        ModelOne model = new ModelOne(O(), O("collection", collection));
        assertEquals(1, model.getOne());
        assertEquals(collection, model.getCollection());
    }

    public void testInitializeWithAttributesAndOptions() {
        ModelOneFromOptions model = new ModelOneFromOptions(O(), O("one", 1));
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
                return O(
                        "first_name", "Unknown",
                        "last_name", "Unknown"
                );
            }
        }
        DefaultsModel model = new DefaultsModel(O("first_name", "John"));
        assertEquals("John", model.get("first_name"));
        assertEquals("Unknown", model.get("last_name"));
    }

    public void testParseCanReturnNull() {
        class NullParseModel extends Model {
            public NullParseModel(JSONObject model) {
                super(model);
            }

            @Override
            protected Options parse(JSONValue resp, Options options) {
                JSONObject object = resp.isObject();
                object.put("value", new JSONNumber(object.get("value").isNumber().doubleValue() + 1));
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
        Model model = new Model(O("parent_id", 1));
        model.setUrlRoot(new UrlRootFunction() {
            @Override
            public String f(Model model) {
                return "/nested/" + model.get("parent_id") + "/collection";
            }
        });
        assertEquals("/nested/1/collection", model.getUrl());
        model.set(O("id", 2));
        assertEquals("/nested/1/collection/2", model.getUrl());
    }

    public void testUnderscoreMethods() {
        Model model = new Model(O("foo", "a", "bar", "b", "baz", "c"));
        Model model2 = model.clone();

        assertEquals(Arrays.asList("foo", "bar", "baz"), Arrays.asList(model.keys()));
        assertEquals(Arrays.asList("a", "b", "c"), Arrays.asList(model.values()));
        assertEquals(O("a", "foo", "b", "bar", "c", "baz"), model.invert());
        assertEquals(O("foo", "a", "baz", "c"), model.pick("foo", "baz"));
        assertEquals(O("baz", "c"), model.omit("foo", "bar"));
    }

    public void testChain() {
        Model model = new Model(O("a", 0, "b", 1, "c", 2));
        assertEquals(Arrays.asList(1, 2), model.chain().pick("a", "b", "c").values().compact().getListValue());
    }

    public void testClone() {
        Model a = new Model(O("foo", 1, "bar", 2, "baz", 3));
        Model b = a.clone();

        assertEquals(1, a.get("foo"));
        assertEquals(2, a.get("bar"));
        assertEquals(3, a.get("baz"));
        assertEquals(a.get("foo"), b.get("foo")); // Foo should be the same on the clone.
        assertEquals(a.get("bar"), b.get("bar")); // Bar should be the same on the clone.
        assertEquals(a.get("baz"), b.get("baz")); // Baz should be the same on the clone.

        a.set(O("foo", 100));
        assertEquals(100, a.get("foo"));
        assertEquals(1, b.get("foo")); // Changing a parent attribute does not change the clone.

        Model foo = new Model(O("p", 1));
        Model bar = new Model(O("p", 2));

        bar.set(foo.clone().getAttributes(), O("unset", true));
        assertEquals(1, foo.get("p"));
        assertEquals(null, bar.get("p"));
    }

    public void testIsNew() {
        Model a = new Model(O("foo", 1, "bar", 2, "baz", 3));
        assertTrue("It should be new", a.isNew());

        a = new Model(O("foo", 1, "bar", 2, "baz", 3, "id", -5));
        assertFalse("any defined ID is legal, negative or positive", a.isNew());

        a = new Model(O("foo", 1, "bar", 2, "baz", 3, "id", 0));
        assertFalse("any defined ID is legal, including zero", a.isNew());

        assertTrue("is true when there is no id", new Model().isNew());
        assertFalse("is false for a positive integer", new Model(O("id", 2)).isNew());
        assertFalse("is false for a negative integer", new Model(O("id", -5)).isNew());
    }

    public void testGet() {
        assertEquals("The Tempest", doc.get("title"));
        assertEquals("Bill Shakespeare", doc.get("author"));
    }

    public void testEscape() {
        assertEquals("The Tempest", doc.escape("title"));

        doc.set(O("audience", "Bill & Bob"));
        assertEquals("Bill &amp; Bob", doc.escape("audience"));

        doc.set(O("audience", "Tim > Joan"));
        assertEquals("Tim &gt; Joan", doc.escape("audience"));

        doc.set(O("audience", 10101));
        assertEquals("10101", doc.escape("audience"));

        doc.unset("audience");
        assertEquals("", doc.escape("audience"));
    }

    public void testHas() {
        Model model = new Model();
        assertFalse(model.has("name"));

        model.set(O(
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

    public void testMatches() {
        Model model = new Model();

        assertFalse(model.matches(O("name", "Jonas", "cool", true)));

        model.set(O("name", "Jonas", "cool", true));

        assertTrue(model.matches(O("name", "Jonas")));
        assertTrue(model.matches(O("name", "Jonas", "cool", true)));
        assertFalse(model.matches(O("name", "Jonas", "cool", false)));
    }

    public void testMatchesWithPredicate() {
        Model model = new Model(O("a", 0));

        assertFalse(model.matches(new MatchesFunction() {
            @Override
            public boolean f(Options attributes) {
                return attributes.getInt("a") > 1 && attributes.get("b") != null;
            }
        }));

        model.set(O("a", 3, "b", true));

        assertTrue(model.matches(new MatchesFunction() {
            @Override
            public boolean f(Options attributes) {
                return attributes.getInt("a") > 1 && attributes.get("b") != null;
            }
        }));
    }

    public void testSetAndUnset() {
        class ValidateModel extends Model {
            public ValidateModel(Options options) {
                super(options);
            }

            @Override
            public Object validate(Options attributes, Options options) {
                assertFalse("validate:true passed while unsetting", attributes.get("foo") != null);

                return super.validate(attributes, options);
            }
        }

        ValidateModel a = new ValidateModel(O("id", "id", "foo", 1, "bar", 2, "baz", 3));
        final int[] changeCount = {0};

        a.on("change:foo", new Function() {
            @Override
            public void f() {
                changeCount[0] += 1;
            }
        });
        a.set(O("foo", 2));
        assertTrue("Foo should have changed.", a.get("foo").equals(2));
        assertTrue("Change count should have incremented.", changeCount[0] == 1);

        // set with value that is not new shouldn't fire change event
        a.set(O("foo", 2));
        assertTrue("Foo should NOT have changed, still 2", a.get("foo").equals(2));
        assertTrue("Change count should NOT have incremented.", changeCount[0] == 1);

        a.unset("foo", O("validate", true));
        assertFalse("Foo should have changed", a.has("foo"));
        assertTrue("Change count should have incremented for unset.", changeCount[0] == 2);

        a.unset("id");
        assertNull("Unsetting the id should remove the id property.", a.getId());
    }

    public void testSetWithFailedValidateFollowedByAnotherSetTriggersChange() {
        final int[] error = {0};
        final int[] attr = {0};
        final int[] main = {0};

        class ValidateModel extends Model {
            public ValidateModel(Options options) {
                super(options);
            }

            @Override
            public Object validate(Options attributes, Options options) {
                if(attributes.getInt("x") > 1) {
                    error[0]++;
                    return "this is an error";
                }

                return super.validate(attributes, options);
            }
        }
        ValidateModel model = new ValidateModel(O("x", 0));
        model.on("change:x", new Function() {
            @Override
            public void f() {
                attr[0] += 1;
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                main[0] += 1;
            }
        });
        model.set(O("x", 2), O("validate", true));
        model.set(O("x", 1), O("validate", true));

        assertEquals(Arrays.asList(attr[0], main[0], error[0]), Arrays.asList(1, 1, 1));
    }


    public void testSetTriggersChangesInTheCorrectOrder() {
        final String[] value = {null};
        Model model = new Model();

        model.on("last", new Function() {
            @Override
            public void f() {
                value[0] = "last";
            }
        });
        model.on("first", new Function() {
            @Override
            public void f() {
                value[0] = "first";
            }
        });

        model.trigger("first");
        model.trigger("last");

        assertEquals("last", value[0]);
    }


    public void testSetFalsyValuesInTheCorrectOrder() {
        final Model model = new Model(O("result", "result"));
        model.on("change", new Function() {
            @Override
            public void f() {
                assertNotNull(model.changedAttributes());
                Boolean result = model.previous("result");
                assertTrue(result == null || !result);
            }
        });
        model.set(O("result", null), O("silent", true));
        model.set(O("result", false), O("silent", true));
        model.set(O("result", null));
    }


    public void testNestedSetTriggersWithTheCorrectOptions() {
        final Model model = new Model(O("result", "result"));
        final Options o1 = O();
        final Options o2 = O();
        final Options o3 = O();

        model.on("change", new Function() {
            @Override
            public void f() {
                Options options = getArgument(1);

                switch (model.getInt("a")) {
                    case 1:
                        assertEquals(o1, options);
                        model.set("a", 2, o2);
                        break;

                    case 2:
                        assertEquals(o2, options);
                        model.set("a", 3, o3);
                        break;
                    case 3:
                        assertEquals(o3, options);
                        break;
                }
            }
        });
        model.set("a", 1, o1);
    }


    public void testMultipleUnsets() {
        final int[] i = {0};
        Function counter = new Function() {
            @Override
            public void f() {
                i[0]++;
            }
        };
        final Model model = new Model(O("a", 1));
        model.on("change:a", counter);
        model.set("a", 2);
        model.unset("a");
        model.unset("a");

        //Unset does not fire an event for missing attributes
        assertEquals(2, i[0]);
    }

    public void testUnsetAndChangedAttributes() {
        final Model model = new Model(O("a", 1));
        model.on("change", new Function() {
            @Override
            public void f() {
                assertTrue("changedAttributes should contain unset properties",model.changedAttributes().containsKey("a"));
            }
        });
        model.unset("a");
    }

    public void testUsingANonDefaultIdAttribute() {
        Model model = new Model(O(), O("idAttribute", "_id"));
        model.set(O("id", "eye-dee", "_id", 25, "title", "Model"));

        assertEquals("eye-dee", model.get("id"));
        assertEquals(25, model.getIdAsInt());
        assertFalse(model.isNew());

        model.unset("_id");
        assertNull(model.getId());
        assertTrue(model.isNew());
    }


    public void testSettingAnAlternativeCidPrefix() {
        Model model = new PrefixedModel();

        assertEquals('m', model.getCid().charAt(0));

        model = new Model();
        assertEquals('c', model.getCid().charAt(0));

        Collection<PrefixedModel> collection = new Collection<PrefixedModel>(
                PrefixedModel.class,
                O("id", "c5"),
                O("id", "c6"),
                O("id", "c7")
        );

        assertEquals('m', collection.get("c6").getCid().charAt(0));
        collection.set(OL(O("id", "c6", "value", "test")), O(
                "merge", true,
                "add", true,
                "remove", false
        ));
        assertTrue(collection.get("c6").has("value"));
    }

    public void testSetAnEmptyString() {
        Model model = new Model(O("name", "Model"));
        model.set(O("name", ""));
        assertEquals("", model.get("name"));
    }


    public void testSettingAnObject() {
        final int[] count = {0};

        Model model = new Model(O(
                "custom", O("foo", 1)
        ));
        model.on("change", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.set(O(
                "custom", O("foo", 1) // no change should be fired
        ));
        model.set(O(
                "custom", O("foo", 2) // change event should be fired
        ));
        assertEquals(1, count[0]);
    }

    public void testClear() {
        final boolean[] changed = {false};

        final Model model = new Model(O(
                "id", 1,
                "name", "Model"
        ));
        model.on("change:name", new Function() {
            @Override
            public void f() {
                changed[0] = true;
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                Options changedAttributes = model.changedAttributes();
                assertTrue(changedAttributes.containsKey("name"));
            }
        });
        model.clear();
        assertTrue(changed[0]);
        assertNull(model.get("name"));
    }

    public void testDefaults() {
        class Defaulted extends Model {
            public Defaulted(Options options) {
                super(options);
            }

            @Override
            protected Options defaults() {
                return O(
                        "one", 1,
                        "two", 2
                );
            }
        }
        Model model = new Defaulted(O("two", null));
        assertEquals(1, model.get("one"));
        assertEquals(2, model.get("two"));

        class Defaulted2 extends Model {
            public Defaulted2(Options options) {
                super(options);
            }

            @Override
            protected Options defaults() {
                return O(
                        "one", 3,
                        "two", 4
                );
            }
        }
        model = new Defaulted2(O("two", null));
        assertEquals(3, model.get("one"));
        assertEquals(4, model.get("two"));
    }


    public void testChangeHasChangedChangedAttributesPreviousPreviousAttributes() {
        final Model model = new Model(O("name", "Tim", "age", 10));
        assertNull(model.changedAttributes());

        model.on("change", new Function() {
            @Override
            public void f() {
                assertTrue("name changed", model.hasChanged("name"));
                assertFalse("age did not", model.hasChanged("age"));
                assertEquals(O("name", "Rob"), model.changedAttributes()); // changedAttributes returns the changed attrs
                assertEquals("Tim", model.previous("name"));
                assertEquals(O("name", "Tim", "age", 10), model.previousAttributes()); // previousAttributes is correct
            }
        });
        assertFalse(model.hasChanged());
        assertFalse(model.hasChanged(null));

        model.set(O("name", "Rob"));
        assertEquals("Rob", model.get("name"));
    }

    public void testChangedAttributes() {
        Model model = new Model(O("a", "a", "b", "b"));
        assertNull(model.changedAttributes());
        assertNull(model.changedAttributes(O("a", "a")));
        assertEquals("b", model.changedAttributes(O("a", "b")).get("a"));
    }


    public void testChangeWithOptions() {
        final String[] value = {null};

        Model model = new Model(O("name", "Rob"));
        model.on("change", new Function() {
            @Override
            public void f() {
                Model model = getArgument(0);
                Options options = getArgument(1);

                value[0] = options.get("prefix").toString() + model.get("name");
            }
        });
        model.set(O("name", "Bob"), O("prefix", "Mr. "));
        assertEquals("Mr. Bob", value[0]);
        model.set(O("name", "Sue"), O("prefix", "Ms. "));
        assertEquals("Ms. Sue", value[0]);
    }


    public void testChangeAfterInitialize() {
        final int[] changed = {0};
        Options attrs = O("id", 1, "label", "c");

        Model obj = new Model(attrs);
        obj.on("change", new Function() {
            @Override
            public void f() {
                changed[0]++;
            }
        });
        obj.set(attrs);
        assertEquals(0, changed[0]);
    }


    public void testSaveWithinChangeEvent() {
        final Model model = new Model(O("firstName", "Taylor", "lastName", "Swift"));
        model.setUrlRoot("/test");
        model.on("change", new Function() {
            @Override
            public void f() {
                model.save();
            }
        });
        model.set(O("lastName", "Hicks"));
    }


    public void testValidateAfterSave() {
        class ValidateModel extends Model {
            @Override
            public Object validate(Options attributes, Options options) {
                if(attributes.getBoolean("admin"))
                    return "Can't change admin status.";

                return super.validate(attributes, options);
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");

                JSONObject obj = new JSONObject();
                obj.put("admin", JSONBoolean.getInstance(true));

                success.f(obj);

                return null;
            }
        }

        final String[] lastError = {null};
        Model model = new ValidateModel();

        model.on("invalid", new Function() {
            @Override
            public void f() {
                String error = getArgument(1);

                lastError[0] = error;
            }
        });
        model.save(null);

        assertEquals("Can't change admin status.", lastError[0]);
        assertEquals("Can't change admin status.", model.getValidationError());
    }


    public void testSave() {
        class TestSyncUpdateModel extends Model {

            private String lastSyncMethod;

            public TestSyncUpdateModel(Options options) {
                super(options);
            }

            public String getLastSyncMethod() {
                return lastSyncMethod;
            }

            @Override
            public Promise sync(String method, Options options) {
                lastSyncMethod = method;

                return null;
            }
        }

        TestSyncUpdateModel model = new TestSyncUpdateModel(O(
                "id"     , "1-the-tempest",
                "title"  , "The Tempest",
                "author" , "Bill Shakespeare",
                "length" , 123
        ));

        model.save(O("title", "Henry V"));
        assertEquals("update", model.getLastSyncMethod());
    }


    public void testSaveFetchDestroyTriggersErrorEventWhenAnErrorOccurs() {
        final int[] count = {0};

        class TestSyncErrorModel extends Model {
            public TestSyncErrorModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                Function error = options.get("error");
                error.f();

                return null;
            }
        }

        TestSyncErrorModel model = new TestSyncErrorModel();
        model.on("error", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.save(O("data", 2, "id", 1));
        model.fetch();
        model.destroy();

        assertEquals(3, count[0]);
    }


    public void testSaveFetchDestroyCallsSuccessWithContext() {
        final int[] count = {0};

        class TestSyncSuccessModel extends Model {
            public TestSyncSuccessModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                Options context = options.get("context");
                success.f(context.toJsonObject());

                return null;
            }
        }

        final Options obj = O();
        Options options = O(
                "context", obj,
                "success", new Function() {
                @Override
                public void f() {
                    Options options = getArgument(2);
                    Options context = options.get("context");
                    assertEquals(obj, context);
                    count[0]++;
                }
            }
        );

        TestSyncSuccessModel model = new TestSyncSuccessModel();
        model.save(O("data", 2, "id", 1), options);
        model.fetch(options);
        model.destroy(options);

        assertEquals(3, count[0]);
    }

    public void testSaveFetchDestroyCallsErrorWithContext() {
        final int[] count = {0};

        class TestSyncErrorModel extends Model {
            public TestSyncErrorModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                Function error = options.get("error");
                Options context = options.get("context");
                error.f(context.toJsonObject());

                return null;
            }
        }

        final Options obj = O();
        Options options = O(
                "context", obj,
                "error", new Function() {
            @Override
            public void f() {
                Options options = getArgument(2);
                Options context = options.get("context");
                assertEquals(obj, context);
                count[0]++;
            }
        }
        );

        TestSyncErrorModel model = new TestSyncErrorModel();
        model.save(O("data", 2, "id", 1), options);
        model.fetch(options);
        model.destroy(options);

        assertEquals(3, count[0]);
    }


    public void testSaveWithPatch() {
        class TestSyncUpdateModel extends Model {

            private String lastSyncMethod;
            private Options lastAttributes;

            public TestSyncUpdateModel(Options options) {
                super(options);
            }

            public String getLastSyncMethod() {
                return lastSyncMethod;
            }

            public Options getLastAttributes() {
                return lastAttributes;
            }

            @Override
            public Promise sync(String method, Options options) {
                lastSyncMethod = method;
                lastAttributes = options.get("attrs");

                return null;
            }
        }

        TestSyncUpdateModel model = new TestSyncUpdateModel(O(
                "id"     , "1-the-tempest",
                "title"  , "The Tempest",
                "author" , "Bill Shakespeare",
                "length" , 123
        ));

        model.clear().set(O("id", 1, "a", 1, "b", 2, "c", 3, "d", 4));
        model.save();
        assertEquals("update", model.getLastSyncMethod());
        assertNull(model.getLastAttributes());

        model.save(O("b", 2, "d", 4), O("patch", true));
        assertEquals("patch", model.getLastSyncMethod());
        assertEquals(2, model.getLastAttributes().size());
        assertEquals(4, model.getLastAttributes().get("d"));
        assertEquals(null, model.getLastAttributes().get("a"));
    }


    public void testSaveWithPatchAndDifferentAttrs() {
        class TestSyncUpdateModel extends Model {
            private Options lastAttributes;

            public TestSyncUpdateModel(Options options) {
                super(options);
            }

            public Options getLastAttributes() {
                return lastAttributes;
            }

            @Override
            public Promise sync(String method, Options options) {
                lastAttributes = options.get("attrs");
                return null;
            }
        }

        TestSyncUpdateModel model = new TestSyncUpdateModel(O(
                "id"     , "1-the-tempest",
                "title"  , "The Tempest",
                "author" , "Bill Shakespeare",
                "length" , 123
        ));

        model.clear().save(O("b", 2, "d", 4), O("patch", true, "attrs", O("B", 1, "D", 3)));
        assertEquals(3, model.getLastAttributes().get("D"));
        assertEquals(null, model.getLastAttributes().get("d"));
        assertEquals(O("b", 2, "d", 4), model.getAttributes());
    }

    public void testSaveInPositionalStyle() {
        class TestSyncModel extends Model {
            public TestSyncModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }
        }

        Model model = new TestSyncModel();
        model.saveKV("title", "Twelfth Night");
        assertEquals("Twelfth Night", model.get("title"));
    }


    public void testSaveWithNonObjectSuccessResponse() {
        class TestSyncModel extends Model {
            public TestSyncModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f("", options);
                success.f(null, options);

                return null;
            }
        }

        Model model = new TestSyncModel();
        model.save(O("testing", "empty"), O(
                "success", new Function() {
                @Override
                public void f() {
                    Model model = getArgument(0);
                    assertEquals(O("testing", "empty"), model.getAttributes());
                }
            }
        ));
    }


    public void testSaveWithWaitAndSuppliedId() {
        class TestSyncModel extends Model {
            public TestSyncModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f(null, options);

                return null;
            }
        }

        final Model model = new TestSyncModel();
        model.setUrlRoot("/collection");

        model.save(O("id", 42), O("wait", true));
        assertEquals("/collection/42", model.getUrl());
    }


    public void testSaveWillPassExtraOptionsToSuccessCallback() {
        final int[] count = {0};

        class SpecialSyncModel extends Model {
            public SpecialSyncModel() {
                super();
            }

            @Override
            public String getUrl() {
                return "/test";
            }

            @Override
            public Promise sync(String method, final Options options) {
                options.extend(O("specialSync", true));
                Promise promise = super.sync(method, options);

                Function success = options.get("success");
                success.f(null, options);

                return promise;
            }
        }

        Model model = new SpecialSyncModel();

        Function onSuccess = new Function() {
            @Override
            public void f() {
                Options options = getArgument(2);

                count[0]++;
                assertTrue("Options were passed correctly to callback", options.getBoolean("specialSync"));
            }
        };

        model.save(null, O("success", onSuccess));
        assertEquals(1, count[0]);
    }


    public void testFetch() {
        class TestSyncUpdateModel extends Model {
            private String lastSyncMethod;

            public TestSyncUpdateModel(Options options) {
                super(options);
            }

            public String getLastSyncMethod() {
                return lastSyncMethod;
            }

            @Override
            public Promise sync(String method, Options options) {
                lastSyncMethod = method;
                return null;
            }
        }

        TestSyncUpdateModel model = new TestSyncUpdateModel(O(
                "id"     , "1-the-tempest",
                "title"  , "The Tempest",
                "author" , "Bill Shakespeare",
                "length" , 123
        ));
        model.fetch();
        assertEquals("read", model.getLastSyncMethod());
    }


    public void testFetchWillPassExtraOptionsToSuccessCallback() {
        final int[] count = {0};

        class SpecialSyncModel extends Model {
            public SpecialSyncModel() {
                super();
            }

            @Override
            public String getUrl() {
                return "/test";
            }

            @Override
            public Promise sync(String method, final Options options) {
                options.extend(O("specialSync", true));
                Promise promise = super.sync(method, options);

                Function success = options.get("success");
                success.f(null, options);

                return promise;
            }
        }

        Model model = new SpecialSyncModel();

        Function onSuccess = new Function() {
            @Override
            public void f() {
                Options options = getArgument(2);

                count[0]++;
                assertTrue("Options were passed correctly to callback", options.getBoolean("specialSync"));
            }
        };

        model.fetch(O("success", onSuccess));
        assertEquals(1, count[0]);
    }


    public void testDestroy() {
        class TestSyncUpdateModel extends Model {
            private String lastSyncMethod;

            public TestSyncUpdateModel(Options options) {
                super(options);
            }

            public String getLastSyncMethod() {
                return lastSyncMethod;
            }

            @Override
            public Promise sync(String method, Options options) {
                lastSyncMethod = method;
                return null;
            }
        }

        TestSyncUpdateModel model = new TestSyncUpdateModel(O(
                "id"     , "1-the-tempest",
                "title"  , "The Tempest",
                "author" , "Bill Shakespeare",
                "length" , 123
        ));
        model.destroy();
        assertEquals("delete", model.getLastSyncMethod());

        Model newModel = new Model();
        assertNull(newModel.destroy());
    }


    public void testDestroyWillPassExtraOptionsToSuccessCallback() {
        final int[] count = {0};

        class SpecialSyncModel extends Model {
            public SpecialSyncModel(Options options) {
                super(options);
            }

            @Override
            public String getUrl() {
                return "/test";
            }

            @Override
            public Promise sync(String method, final Options options) {
                options.extend(O("specialSync", true));
                Promise promise = super.sync(method, options);

                Function success = options.get("success");
                success.f(null, options);

                return promise;
            }
        }

        Model model = new SpecialSyncModel(O("id", "id"));

        Function onSuccess = new Function() {
            @Override
            public void f() {
                Options options = getArgument(1);

                count[0]++;
                assertTrue("Options were passed correctly to callback", options.getBoolean("specialSync"));
            }
        };

        model.destroy(O("success", onSuccess));
        assertEquals(1, count[0]);
    }


    public void testNonPersistedDestroy() {
        class SyncModel extends Model {
            public SyncModel(Options options) {
                super(options);
            }

            @Override
            public Promise sync(String method, final Options options) {
                throw new Error("should not be called");
            }
        }

        Model a = new SyncModel(O("foo", 1, "bar", 2, "baz", 3));
        a.destroy();

        assertTrue("non-persisted model should not call sync", true);
    }


    public void testValidate() {
        class ValidateModel extends Model {
            @Override
            public Object validate(Options attributes, Options options) {
                if(!attributes.get("admin").equals(this.get("admin")))
                    return "Can't change admin status.";

                return super.validate(attributes, options);
            }
        }

        final String[] lastError = {null};
        Model model = new ValidateModel();

        model.on("invalid", new Function() {
            @Override
            public void f() {
                String error = getArgument(1);

                lastError[0] = error;
            }
        });
        Model result = model.set(O("a", 100));
        assertEquals(result, model);
        assertEquals(100, model.get("a"));
        assertNull(lastError[0]);

        model.set(O("admin", true));
        assertTrue(model.getBoolean("admin"));

        model.set(O("a", 200, "admin", false), O("validate", true));
        assertEquals("Can't change admin status.", lastError[0]);
        assertEquals("Can't change admin status.", model.getValidationError());
        assertEquals(100, model.get("a"));
    }


    public void testValidateOnUnsetAndClear() {
        final boolean[] error = {false};

        class ValidateModel extends Model {
            public ValidateModel(Options options) {
                super(options);
            }

            @Override
            public Object validate(Options attributes, Options options) {
                if(!attributes.getBoolean("name")) {
                    error[0] = true;
                    return "No thanks.";
                }

                return super.validate(attributes, options);
            }
        }
        Model model = new ValidateModel(O("name", "One"));

        model.set(O("name", "Two"));
        assertEquals("Two", model.get("name"));
        assertFalse(error[0]);

        model.unset("name", O("validate", true));
        assertTrue(error[0]);
        assertEquals("Two", model.get("name"));

        model.clear(O("validate", true));
        assertEquals("Two", model.get("name"));

        model.clear();
        assertNull(model.get("name"));
    }


    public void testValidateWithErrorCallback() {
        class ValidateModel extends Model {
            @Override
            public Object validate(Options attributes, Options options) {
                if(attributes.getBoolean("admin"))
                    return "Can't change admin status.";

                return super.validate(attributes, options);
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");

                JSONObject obj = new JSONObject();
                obj.put("admin", JSONBoolean.getInstance(true));

                success.f(obj);

                return null;
            }
        }

        final boolean[] boundError = {false};
        Model model = new ValidateModel();

        model.on("invalid", new Function() {
            @Override
            public void f() {
                boundError[0] = true;
            }
        });
        Model result = model.set(O("a", 100), O("validate", true));
        assertEquals(result, model);
        assertEquals(100, model.get("a"));
        assertNull(model.getValidationError());
        assertFalse(boundError[0]);

        model.set(O("a", 200, "admin", true), O("validate", true));
        assertEquals(100, model.get("a"));
        assertTrue(boundError[0]);
        assertEquals("Can't change admin status.", model.getValidationError());
    }



    public void testDefaultsAlwaysExtendAttrs() {
        final int[] count = {0};

        class Defaulted extends Model {
            public Defaulted() {
                this(null);
            }

            public Defaulted(Options options) {
                super(options);

                assertEquals(1, getAttributes().get("one"));
                count[0]++;
            }

            @Override
            protected Options defaults() {
                return O(
                        "one", 1
                );
            }
        }
        new Defaulted(O());
        new Defaulted();

        assertEquals(2, count[0]);
    }


    public void testNestedChangeEventsDontClobberPreviousAttributes() {
        final int[] count = {0};

        Model model = new Model();
        model.on("change:state", new Function() {
            @Override
            public void f() {
                Model model = getArgument(0);
                String newState = getArgument(1);

                assertNull(model.previous("state"));
                assertEquals("hello", newState);

                count[0]++;

                // Fire a nested change event.
                model.set(O("other", "whatever"));
            }
        }).on("change:state", new Function() {
            @Override
            public void f() {
                Model model = getArgument(0);
                String newState = getArgument(1);

                assertNull(model.previous("state"));
                assertEquals("hello", newState);

                count[0]++;
            }
        });
        model.set(O("state", "hello"));

        assertEquals(2, count[0]);
    }


    public void testHasChangedSetShouldUseSameComparison() {
        final int[] changed = {0};

        final Model model = new Model(O("a", null));
        model.on("change", new Function() {
            @Override
            public void f() {
                assertTrue(model.hasChanged("a"));
            }
        }).on("change:a", new Function() {
            @Override
            public void f() {
                changed[0]++;
            }
        });
        model.set(O("a", 0));

        assertEquals(1, changed[0]);
    }


    public void testChangeAttributeCallbacksShouldFireAfterAllChangesHaveOccurred() {
        final Model model = new Model();

        Function assertion = new Function() {
            @Override
            public void f() {
                assertEquals("a", model.get("a"));
                assertEquals("b", model.get("b"));
                assertEquals("c", model.get("c"));
            }
        };

        model.on("change:a", assertion);
        model.on("change:b", assertion);
        model.on("change:c", assertion);

        model.set(O("a", "a", "b", "b", "c", "c"));
    }


    public void testSetWithAttributesProperty() {
        Model model = new Model();
        model.set(O("attributes", true));

        assertTrue(model.has("attributes"));
    }


    public void testSetValueRegardlessOfEqualityOrChange() {
        Model model = new Model(O("x", OL()));
        OptionsList a = OL();
        model.set(O("x", a));

        assertEquals(a, model.get("x"));
    }


    public void testSameValueDoesNotTriggerChange() {
        final int[] count = {0};

        Model model = new Model(O("x", 1));
        model.on("change change:x", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.set(O("x", 1));
        model.set(O("x", 1));

        assertEquals(0, count[0]);
    }


    public void testUnsetDoesNotFireAChangeForUndefinedAttributes() {
        final int[] count = {0};

        Model model = new Model(O("x", null));
        model.on("change:x", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.unset("x");

        assertEquals(0, count[0]);
    }


    public void testSetUndefinedValues() {
        Model model = new Model(O("x", null));
        assertTrue(model.getAttributes().containsKey("x"));
    }


    public void testHasChangedWorksOutsideOfChangeEventsAndTrueWithin() {
        final int[] count = {0};

        final Model model = new Model(O("x", 1));
        model.on("change:x", new Function() {
            @Override
            public void f() {
                assertTrue(model.hasChanged("x"));
                assertEquals(1, model.get("x"));
                count[0]++;
            }
        });
        model.set(O("x", 2), O("silent", true));
        assertTrue(model.hasChanged());
        assertTrue(model.hasChanged("x"));

        model.set(O("x", 1));
        assertTrue(model.hasChanged());
        assertTrue(model.hasChanged("x"));

        assertEquals(1, count[0]);
    }


    public void testHasChangedGetsClearedOnTheFollowingSet() {
        Model model = new Model();
        model.set(O("x", 1));
        assertTrue(model.hasChanged());

        model.set(O("x", 1));
        assertFalse(model.hasChanged());

        model.set(O("x", 2));
        assertTrue(model.hasChanged());

        model.set(O());
        assertFalse(model.hasChanged());
    }


    public void testSaveWithWaitSucceedsWithoutValidate() {
        Model model = new Model();
        model.setUrlRoot("/test");
        model.save(O("x", 1), O("wait", true));
    }

    public void testSaveWithoutWaitDoesntSetInvalidAttributes() {
        class ValidateModel extends Model {
            @Override
            public Object validate(Options attributes, Options options) {
                return 1;
            }
        }

        Model model = new ValidateModel();
        model.save(O("a", 1));
        assertNull(model.get("a"));
    }

    public void testHasChangedForFalsyKeys() {
        Model model = new Model();
        model.set(O("x", true), O("silent", true));

        assertFalse(model.hasChanged(""));
        assertFalse(model.hasChanged("0"));
    }


    public void testPreviousForFalsyKeys() {
        Model model = new Model(O(0, true, "", true));
        model.set(O(0, false, "", false), O("silent", true));

        assertTrue((Boolean) model.previous("0"));
        assertTrue((Boolean) model.previous(""));
    }


    public void testSaveWithWaitSendsCorrectAttributes() {
        final int[] changed = {0};

        Model model = new Model(O("x", 1, "y", 2));
        model.setUrlRoot("/test");
        model.on("change:x", new Function() {
            @Override
            public void f() {
                changed[0]++;
            }
        });
        model.save(O("x", 3), O("wait", true));

        JSONValue jsonValue = JSONParser.parseStrict(String.valueOf(NetworkSyncStrategy.get().getSyncArgs().get("data")));
        assertEquals(O(jsonValue), O("x", 3, "y", 2));

        assertEquals(1, model.get("x"));
        assertEquals(0, changed[0]);

        NetworkSyncStrategy.get().getSyncArgs().getSuccess().f();

        assertEquals(3, model.get("x"));
        assertEquals(1, changed[0]);
    }


    public void testAFailedSaveWithWaitDoesntLeaveAttributesBehind() {
        Model model = new Model();
        model.setUrlRoot("/test");
        model.save(O("x", 1), O("wait", true));

        assertNull(model.get("x"));
    }


    public void testSaveWithWaitResultsInCorrectAttributesIfSuccessIsCalledDuringSync() {
        final int[] count = {0};

        class TestSyncModel extends Model {
            public TestSyncModel(Options options) {
                super(options);
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f(null, options);

                return null;
            }
        }

        final Model model = new TestSyncModel(O("x", 1, "y", 2));
        model.on("change:x", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });

        model.save(O("x", 3), O("wait", true));
        assertEquals(3, model.get("x"));
        assertEquals(1, count[0]);
    }



    public void testSaveWithWaitValidatesAttributes() {
        final int[] count = {0};

        class ValidateModel extends Model {
            @Override
            public Object validate(Options attributes, Options options) {
                count[0]++;
                return super.validate(attributes, options);
            }
        }

        Model model = new ValidateModel();
        model.setUrlRoot("/test");
        model.save(O("x", 1), O("wait", true));

        assertEquals(1, count[0]);
    }

    public void testSaveTurnsOnParseFlag() {
        class TestSyncModel extends Model {
            public TestSyncModel() {
                super();
            }

            @Override
            public Promise sync(String method, Options options) {
                assertTrue(options.containsKey("parse") && options.getBoolean("parse"));

                return null;
            }
        }
        final Model model = new TestSyncModel();
        model.save();
    }


    public void testNestedSetDuringChangeAttr() {
        final List<String> events = new ArrayList<String>();
        final Model model = new Model();
        model.on("all", new Function() {
            @Override
            public void f() {
                String event = getArgument(0);
                events.add(event);
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                model.set(O("z", true), O("silent", true));
            }
        });
        model.on("change:x", new Function() {
            @Override
            public void f() {
                model.set(O("y", true));
            }
        });
        model.set(O("x", true));
        assertEquals(events, Arrays.asList("change:y", "change:x", "change"));

        events.clear();
        model.set(O("z", true));
        assertTrue(events.isEmpty());
    }

    public void testNestedChangeOnlyFiresOnce() {
        final int[] count = {0};

        final Model model = new Model();
        model.on("change", new Function() {
            @Override
            public void f() {
                count[0]++;
                model.set(O("x", true));
            }
        });
        model.set(O("x", true));

        assertEquals(1, count[0]);
    }


    public void testNestedSetDuringChange() {
        final int[] count = {0};

        final Model model = new Model();
        model.on("change", new Function() {
            @Override
            public void f() {
                switch(count[0]++) {
                    case 0:
                        assertEquals(model.changedAttributes(), O("x", true));
                        assertNull(model.previous("x"));
                        model.set(O("y", true));
                        break;
                    case 1:
                        assertEquals(model.changedAttributes(), O("x", true, "y", true));
                        assertNull(model.previous("x"));
                        model.set(O("z", true));
                        break;
                    case 2:
                        assertEquals(model.changedAttributes(), O("x", true, "y", true, "z", true));
                        assertNull(model.previous("y"));
                        break;
                }

            }
        });
        model.set(O("x", true));

        assertEquals(3, count[0]);
    }


    public void testNestedChangeWithSilent() {
        final int[] count = {0};

        final Model model = new Model();
        model.on("change:y", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                switch(count[0]++) {
                    case 0:
                        assertEquals(model.changedAttributes(), O("x", true));
                        model.set(O("y", true), O("silent", true));
                        model.set(O("z", true));
                        break;
                    case 1:
                        assertEquals(model.changedAttributes(), O("x", true, "y", true, "z", true));
                        break;
                    case 2:
                        assertEquals(model.changedAttributes(), O("z", false));
                        break;
                }

            }
        });
        model.set(O("x", true));
        model.set(O("z", false));

        assertEquals(3, count[0]);
    }


    public void testNestedChangeAttrWithSilent() {
        final int[] count = {0};

        final Model model = new Model();
        model.on("change:y", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                model.set(O("y", true), O("silent", true));
                model.set(O("z", true));
            }
        });
        model.set(O("x", true));

        assertEquals(0, count[0]);
    }


    public void testMultipleNestedChangesWithSilent() {
        final int[] count = {0};

        final Model model = new Model();

        model.on("change:x", new Function() {
            @Override
            public void f() {
                model.set(O("y", 1), O("silent", true));
                model.set(O("y", 2));
            }
        });
        model.on("change:y", new Function() {
            @Override
            public void f() {
                int value = getArgument(1);

                assertEquals(2, value);
                count[0]++;
            }
        });
        model.set(O("x", true));

        assertEquals(1, count[0]);
    }


    public void testMultipleNestedChangesWithSilentWithVariableCounting() {
        final List<Integer> changes = new ArrayList<Integer>();
        final Model model = new Model();

        model.on("change:b", new Function() {
            @Override
            public void f() {
                int value = getArgument(1);
                changes.add(value);
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                model.set(O("b", 1));
            }
        });

        model.set(O("b", 0));
        assertEquals(Arrays.asList(0, 1), changes);
    }


    public void testBasicSilentChangeSemantics() {
        final int[] count = {0};

        final Model model = new Model();

        model.set(O("x", 1));
        model.on("change", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.set(O("x", 2), O("silent", true));
        model.set(O("x", 1));

        assertEquals(1, count[0]);
    }


    public void testNestedSetMultipleTimes() {
        final int[] count = {0};

        final Model model = new Model();

        model.on("change:b", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.on("change:a", new Function() {
            @Override
            public void f() {
                model.set(O("b", true));
                model.set(O("b", true));
            }
        });
        model.set(O("a", true));

        assertEquals(1, count[0]);
    }


    public void testClearDoesNotAlterOptions() {
        Model model = new Model();
        Options options = O();

        model.clear(options);
        assertFalse(options.containsKey("unset"));
    }


    public void testUnsetDoesNotAlterOptions() {
        Model model = new Model();
        Options options = O();

        model.unset("x", options);
        assertFalse(options.containsKey("unset"));
    }


    public void testOptionsIsPassedToSuccessCallbacks() {
        final int[] count = {0};

        class SyncModel extends Model {
            public SyncModel() {
                super();
            }

            @Override
            public Promise sync(String method, final Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }
        }

        Model model = new SyncModel();
        Options opts = O(
                "success", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        }
        );

        model.save(O("id", 1), opts);
        model.fetch(opts);
        model.destroy(opts);

        assertEquals(3, count[0]);
    }


    public void testTriggerSyncEvent() {
        final int[] count = {0};

        class SyncModel extends Model {
            public SyncModel(Options options) {
                super(options);
            }

            @Override
            public Promise sync(String method, final Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }
        }

        Model model = new SyncModel(O("id", 1));
        model.on("sync", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });

        model.fetch();
        model.save();
        model.destroy();

        assertEquals(3, count[0]);
    }


    public void testDestroyNewModelsExecuteSuccessCallback() {
        final int[] count = {0};

        new Model().on("sync", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        }).on("destroy", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        }).destroy(O("success", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        }));

        assertEquals(2, count[0]);
    }


    public void testSaveAnInvalidModelCannotBePersisted() {
        final int[] count = {0};

        class ValidateModel extends Model {
            @Override
            public Object validate(Options attributes, Options options) {
                return "invalid";
            }

            @Override
            public Promise sync(String method, final Options options) {
                count[0]++;

                return null;
            }
        }

        Model model = new ValidateModel();
        assertNull(model.save());
        assertEquals(0, count[0]);
    }


    public void testSaveWithoutAttrsTriggersError() {
        final int[] count = {0};

        class ValidateModel extends Model {
            public ValidateModel(Options options) {
                super(options);
            }

            @Override
            public Object validate(Options attributes, Options options) {
                return "invalid";
            }

            @Override
            public Promise sync(String method, final Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }
        }

        Model model = new ValidateModel(O("id", 1));
        model.on("invalid", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.save();

        assertEquals(1, count[0]);
    }


    public void testNullCanBePassedToAModelConstructorWithoutCoersion() {
        class UndefinedModel extends Model {
            public UndefinedModel() {
                super();
            }

            public UndefinedModel(Options attributes) {
                super(attributes);

                assertNull(attributes);
            }

            public UndefinedModel(Options attributes, Options options) {
                super(attributes, options);

                assertNull(attributes);
                assertNull(options);
            }

            @Override
            protected Options defaults() {
                return O("one", 1);
            }
        }
        new UndefinedModel();
        new UndefinedModel(null);
        new UndefinedModel(null, null);
    }


    public void testModelSaveDoesNotTriggerChangeOnUnchangedAttributes() {
        final int[] count = {0};

        class ChangeModel extends Model {
            public ChangeModel(Options options) {
                super(options);
            }

            @Override
            public Promise sync(String method, final Options options) {
                final Function success = options.get("success");
                success.f();

                return null;
            }
        }

        Model model = new ChangeModel(O("x", true));
        model.on("change:x", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.save(null, O("wait", true));

        assertEquals(0, count[0]);
    }


    public void testChangingFromOneValueSilentlyToAnotherBackToOriginalTriggersAChange() {
        final int[] count = {0};

        Model model = new Model(O("x", 1));
        model.on("change:x", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.set(O("x", 2), O("silent", true));
        model.set(O("x", 3), O("silent", true));
        model.set(O("x", 1));

        assertEquals(1, count[0]);
    }


    public void testMultipleSilentChangesNestedInsideAChangeEvent() {
        final List<Object> changes = new ArrayList<Object>();

        final Model model = new Model();
        model.on("change", new Function() {
            @Override
            public void f() {
                model.set(O("a", "c"), O("silent", true));
                model.set(O("b", 2), O("silent", true));
                model.unset("c", O("silent", true));
            }
        });
        model.on("change:a change:b change:c", new Function() {
            @Override
            public void f() {
                Object val = getArgument(1);
                changes.add(val);
            }
        });
        model.set(O("a", "a", "b", 1, "c", "item"));
        assertEquals(Arrays.asList("a", 1, "item"), changes);
        assertEquals(O("a", "c", "b", 2), model.getAttributes());
    }


    public void testSilentChangesInLastChangeEventBackToOriginalTriggersChange() {
        final List<Object> changes = new ArrayList<Object>();

        final Model model = new Model();
        model.on("change:a change:b change:c", new Function() {
            @Override
            public void f() {
                Object val = getArgument(1);
                changes.add(val);
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                model.set(O("a", "c"), O("silent", true));
            }
        });

        model.set(O("a", "a"));
        assertEquals(Collections.singletonList("a"), changes);
        model.set(O("a", "a"));
        assertEquals(Arrays.asList("a", "a"), changes);
    }


    public void testChangeCalculationsShouldUseIsEqual() {
        Model model = new Model(O("a", O("key", "value")));
        model.set("a", O("key", "value"), O("silent", true));

        assertNull(model.changedAttributes());
    }



    public void testFinalChangeEventIsAlwaysFiredRegardlessOfInterimChanges() {
        final int[] count = {0};

        final Model model = new Model();
        model.on("change:property", new Function() {
            @Override
            public void f() {
                model.set("property", "bar");
            }
        });
        model.on("change", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        });
        model.set("property", "foo");

        assertEquals(1, count[0]);
    }


    public void testIsValid() {
        class ValidateModel extends Model {
            public ValidateModel(Options options) {
                super(options);
            }

            @Override
            public Object validate(Options attributes, Options options) {
                if (!attributes.getBoolean("valid")) {
                    return "invalid";
                }
                return true;
            }
        }
        Model model = new ValidateModel(O("valid", true));
        assertTrue(model.isValid());
        model.set(O("valid", false), O("validate", true));
        assertTrue(model.isValid());

        model.set("valid", false);
        assertFalse(model.isValid());
        model.set(O("valid", false, "a", "a"), O("validate", true));
        assertFalse(model.has("a"));
        assertFalse(model.isValid());
    }


    public void testCreatingAModelWithValidateTrueWillCallValidateAndUseTheErrorCallback() {
        class ValidateModel extends Model {
            public ValidateModel(Options attributes, Options options) {
                super(attributes, options);
            }

            @Override
            public Object validate(Options attributes, Options options) {
                if (attributes.getInt("id") == 1) {
                    return "This shouldn't happen";
                }
                return true;
            }
        }

        Model model = new ValidateModel(O("id", 1), O("validate", true));
        assertEquals("This shouldn't happen", model.getValidationError());
    }


    public void testToJSONReceivesAttrsDuringSaveWaitTrue() {
        final int[] count = {0};

        class JSONModel extends Model {
            public JSONModel() {
                super();
                setUrlRoot("/test");
            }

            @Override
            public Options toJSON() {
                count[0]++;
                assertEquals(1, getAttributes().get("x"));
                return getAttributes().clone();
            }
        }
        Model model = new JSONModel();
        model.save(O("x", 1), O("wait", true));

        assertEquals(1, count[0]);
    }


    public void testNestedSetWithSilentOnlyTriggersOneChange() {
        final int[] count = {0};
        final Model model = new Model();

        model.on("change", new Function() {
            @Override
            public void f() {
                model.set(O("b", true), O("silent", true));
                count[0]++;
            }
        });
        model.set(O("a", true));

        assertEquals(1, count[0]);
    }


    public void testIdWillOnlyBeUpdatedIfItIsSet() {
        final Model model = new Model(O("id", 1));
        model.setIdAsInt(2);
        model.set(O("foo", "bar"));
        assertEquals(2, model.getIdAsInt());
        assertEquals(2, model.get("id"));

        model.set(O("id", 3));
        assertEquals(3, model.getIdAsInt());
        assertEquals(3, model.get("id"));

        model.set(O("id", "key"));
        assertEquals("key", model.getId());
        assertEquals("key", model.get("id"));
    }
}
