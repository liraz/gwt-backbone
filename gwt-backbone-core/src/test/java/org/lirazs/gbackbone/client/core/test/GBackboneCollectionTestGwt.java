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
package org.lirazs.gbackbone.client.core.test;


import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.*;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.data.OptionsList;
import org.lirazs.gbackbone.client.core.function.FilterFunction;
import org.lirazs.gbackbone.client.core.function.MapFunction;
import org.lirazs.gbackbone.client.core.function.MinMaxFunction;
import org.lirazs.gbackbone.client.core.function.ModelClassFunction;
import org.lirazs.gbackbone.client.core.js.JsArray;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.net.Synchronized;
import org.lirazs.gbackbone.client.core.test.model.*;

import java.util.*;

public class GBackboneCollectionTestGwt extends GWTTestCase {

    private Model a;
    private Model b;
    private Model c;
    private Model d;

    private Collection<Model> col;
    private Collection<Model> otherCol;

    public String getModuleName() {
        return "org.lirazs.gbackbone.GBackboneTest";
    }

    public void gwtSetUp() {
        a = new Model(new Options("id", 3, "label", "a"));
        b = new Model(new Options("id", 2, "label", "b"));
        c = new Model(new Options("id", 1, "label", "c"));
        d = new Model(new Options("id", 0, "label", "d"));

        col = new Collection<Model>(a, b, c, d);
        otherCol = new Collection<Model>();
    }

    public void gwtTearDown() {

    }


    public void testNewAndSort() {
        final int[] counter = {0};

        col.on("sort", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        Object[] a = col.pluck("label");
        Object[] b = {"a", "b", "c", "d"};

        assertEquals(Arrays.asList(a), Arrays.asList(b));

        col.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model a, Model b) {
                return a.getIdAsInt() > b.getIdAsInt() ? -1 : 1;
            }
        });
        col.sort();

        assertEquals(counter[0], 1);
        a = col.pluck("label");
        assertEquals(Arrays.asList(a), Arrays.asList(b));

        col.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model a, Model b) {
                return a.getIdAsInt();
            }
        });
        col.sort();
        assertEquals(2, counter[0]);

        Object[] c = {"d", "c", "b", "a"};
        a = col.pluck("label");
        assertEquals(Arrays.asList(c), Arrays.asList(a));
        assertEquals(4, col.length());
    }

    public void testIntegerComparator() {
        Collection<Model> collection = new Collection<Model>(
                new Model(new Options("id", 3)),
                new Model(new Options("id", 1)),
                new Model(new Options("id", 2)));

        collection.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return Integer.valueOf(o1.getIdAsInt()).compareTo(o2.getIdAsInt());
            }
        });
        collection.sort();

        Object[] b = {1, 2, 3};
        Object[] a = collection.pluck("id");

        assertEquals(Arrays.asList(b), Arrays.asList(a));
    }

    public void testNewAndParse() {
        // mocking a JSON response from a service
        JSONArray models = new JSONArray();

        JSONObject a = new JSONObject();
        a.put("a", new JSONNumber(1));

        JSONObject b = new JSONObject();
        b.put("a", new JSONNumber(2));

        JSONObject c = new JSONObject();
        c.put("a", new JSONNumber(3));

        JSONObject d = new JSONObject();
        d.put("a", new JSONNumber(4));

        models.set(0, a);
        models.set(1, b);
        models.set(2, c);
        models.set(3, d);

        Collection<Model> collection = new Collection<Model>(models) {
            @Override
            public List<Model> parse(JSONValue resp, Options options) {
                List<Model> result = new ArrayList<Model>();
                JSONArray array = resp.isArray();
                if(array != null) {
                    for (int i = 0; i < array.size(); i++) {
                        JSONValue value = array.get(i);
                        JSONObject object = value.isObject();
                        if(object != null) {
                            if(object.get("a").isNumber().doubleValue() % 2 == 0) {
                                Model model = GWT.create(Model.class);
                                model.set(new Options(object), options);

                                result.add(model);
                            }
                        }
                    }
                }
                return result;
            }
        };

        assertEquals(2, collection.length());
        assertEquals(2, collection.first().getInt("a"));
        assertEquals(4, collection.last().getInt("a"));
    }

    public void testClonePreservesModelAndComparator() {

        Comparator<TestModel> comparator = new Comparator<TestModel>() {
            @Override
            public int compare(TestModel o1, TestModel o2) {
                return Integer.valueOf(o1.getIdAsInt()).compareTo(o2.getIdAsInt());
            }
        };

        JSONArray models = new JSONArray();

        JSONObject a = new JSONObject();
        a.put("id", new JSONNumber(1));
        models.set(0, a);

        Collection<TestModel> collection = new Collection<TestModel>(TestModel.class, models);
        collection.registerComparator(comparator);

        Collection<TestModel> clonedCollection = collection.clone();

        JSONObject b = new JSONObject();
        b.put("id", new JSONNumber(2));

        clonedCollection.add(b);

        assertTrue(clonedCollection.at(0) != null);
        assertTrue(clonedCollection.at(1) != null);

        assertTrue(clonedCollection.hasComparator());
    }

    public void testGet() {
        assertEquals(d, col.get(0));
        assertEquals(d, col.get(d.clone()));
        assertEquals(b, col.get(2));
        assertEquals(c, col.get(new Options("id", 1)));
        assertEquals(c, col.get(c.clone()));
        assertEquals(col.first(), col.get(col.first().getCid()));
    }

    public void testGetWithNonDefaultIds() {
        JSONObject a = new JSONObject();
        a.put("_id", new JSONNumber(100));

        MongoModel model = new MongoModel(a);
        Collection<MongoModel> col = new Collection<MongoModel>(MongoModel.class, model);
        assertEquals(model, col.get(100));
        assertEquals(model, col.get(model.getCid()));
        assertEquals(model, col.get(model));
        assertEquals(col.get(101), null);

        Collection<MongoModel> col2 = new Collection<MongoModel>();
        col2.registerModelClass(MongoModel.class);
        col2.add(model.getAttributes().toJsonObject());

        assertEquals(col2.first(), col2.get((MongoModel) model.clone()));
    }

    public void testGetWithUndefinedId() {
        JSONArray array = new JSONArray();

        JSONObject a = new JSONObject();
        a.put("id", new JSONNumber(1));

        JSONObject b = new JSONObject();
        b.put("id", null);

        array.set(0, a);
        array.set(1, b);

        Collection<Model> collection = new Collection<Model>(array);
        assertEquals(1, collection.get(1).getIdAsInt());
    }

    public void testUpdateIndexWhenIdChanges() {

        final Collection<Model> col = new Collection<Model>();
        col.add(new OptionsList(
                new Options("id", 0, "name", "one"),
                new Options("id", 1, "name", "two")
        ));

        Model one = col.get(0);
        assertEquals(one.get("name"), "one");

        col.on("change:name", new Function() {
            @Override
            public void f() {
                Model model = getArgument(1);
                assertNotNull(col.get(model));
            }
        });
        one.set(new Options("name", "dalmatians", "id", 101));

        assertEquals(null, col.get(0));
        assertEquals("dalmatians", col.get(101).get("name"));
    }

    public void testAt() {
        assertEquals(c, col.at(2));
        assertEquals(c, col.at(-2));
    }

    public void testJsPluck() {
        JsArray<String> labels = col.jsPluck("label");
        assertEquals("a b c d", labels.join(" "));
    }

    public void testPluck() {
        String[] strings = "a b c d".split(" ");
        Object[] labels = col.pluck("label");

        for (int i = 0; i < labels.length; i++) {
            Object label = labels[i];
            assertEquals(strings[i], label);
        }
    }

    public void testAdd() {
        final String[] added = new String[1];
        final Boolean[] secondAdded = new Boolean[1];
        final Options[] opts = new Options[1];

        Model e = new Model(new Options("id", 10, "label", "e"));
        otherCol.add(e);
        otherCol.on("add", new Function() {
            @Override
            public void f() {
                secondAdded[0] = true;
            }
        });
        col.on("add", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);
                Options options = this.getArgument(3);

                added[0] = model.get("label");
                opts[0] = options;
            }
        });
        col.add(e, new Options("amazing", true));
        assertEquals("e", added[0]);
        assertEquals(5, col.length());
        assertEquals(e, col.last());
        assertEquals(1, otherCol.length());
        assertEquals(null, secondAdded[0]);
        assertTrue(opts[0].getBoolean("amazing"));

        Model f = new Model(new Options("id", 20, "label", "f"));
        Model g = new Model(new Options("id", 21, "label", "g"));
        Model h = new Model(new Options("id", 22, "label", "h"));

        Collection<Model> atCol = new Collection<Model>(f, g, h);
        assertEquals(3, atCol.length());
        atCol.add(e, new Options("at", 1));
        assertEquals(4, atCol.length());
        assertEquals(e, atCol.at(1));
        assertEquals(h, atCol.last());

        Collection<Model> col1 = new Collection<Model>(Arrays.<Model>asList(null, null));
        final int[] addCount = {0};
        col1.on("add", new Function() {
            @Override
            public void f() {
                addCount[0] += 1;
            }
        });
        col1.add(Arrays.asList(null, f, g));
        assertEquals(5, col1.length());
        assertEquals(3, addCount[0]);
        col1.add(Arrays.<Model>asList(null, null, null, null));
        assertEquals(9, col1.length());
        assertEquals(7, addCount[0]);
    }

    public void testAddMultipleModels() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("at", 0),
                new Options("at", 1),
                new Options("at", 9)
        ));
        col.add(new OptionsList(
                new Options("at", 2),
                new Options("at", 3),
                new Options("at", 4),
                new Options("at", 5),
                new Options("at", 6),
                new Options("at", 7),
                new Options("at", 8)
        ), new Options("at", 2));

        for (int i = 0; i < 5; i++) {
            assertEquals(i, col.at(i).get("at"));
        }
    }

    public void testAddAtShouldHavePreferenceOverComparator() {
        Comparator<Model> colComparator = new Comparator<Model>() {
            @Override
            public int compare(Model a, Model b) {
                return a.getIdAsInt() > b.getIdAsInt() ? -1 : 1;
            }
        };

        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 2),
                new Options("id", 3)
        ), new Options("comparator", colComparator));

        col.add(new Model(new Options("id", 1)), new Options("at", 1));

        assertEquals("3 1 2", col.jsPluck("id").join(" "));
    }

    public void testCantAddModelToCollectionTwice() {
        Collection<Model> col = new Collection<Model>(
                new Options("id", 1),
                new Options("id", 2),
                new Options("id", 1),
                new Options("id", 2),
                new Options("id", 3)
        );
        assertEquals("1 2 3", col.jsPluck("id").join(" "));
    }

    public void testCantAddDifferentModelWithSameIdToCollectionTwice() {
        Collection<Model> col = new Collection<Model>();
        col.unshift(new Options("id", 101));
        col.add(new Options("id", 101));

        assertEquals(1, col.length());
    }

    public void testMergeInDuplicateModelsWithMergeTrue() {
        Collection<Model> col = new Collection<Model>();
        col.add(new OptionsList(
                new Options("id", 1, "name", "Moe"),
                new Options("id", 2, "name", "Curly"),
                new Options("id", 3, "name", "Larry")
        ));

        col.add(new Options("id", 1, "name", "Moses"));
        assertEquals("Moe", col.first().get("name"));

        col.add(new Options("id", 1, "name", "Moses"), new Options("merge", true));
        assertEquals("Moses", col.first().get("name"));

        col.add(new Options("id", 1, "name", "Tim"), new Options("merge", true, "silent", true));
        assertEquals("Tim", col.first().get("name"));
    }

    public void testAddModelToMultipleCollections() {
        final int[] counter = {0};
        final Collection<Model> colF = new Collection<Model>();
        final Collection<Model> colE = new Collection<Model>();

        final Model e = new Model(new Options("id", 10, "label", "e"));
        e.on("add", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(0);
                Collection collection = this.getArgument(1);

                counter[0]++;
                assertEquals(model, e);

                if (counter[0] > 1) {
                    assertEquals(colF, collection);
                } else {
                    assertEquals(colE, collection);
                }
            }
        });

        colE.on("add", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);
                Collection collection = this.getArgument(2);

                assertEquals(model, e);
                assertEquals(collection, colE);
            }
        });
        colF.on("add", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);
                Collection collection = this.getArgument(2);

                assertEquals(model, e);
                assertEquals(collection, colF);
            }
        });

        colE.add(e);
        assertEquals(colE, e.getCollection());
        colF.add(e);
        assertEquals(colE, e.getCollection());
    }

    public void testAddModelWithParse() {
        Collection<ParseModel> col = new Collection<ParseModel>(ParseModel.class);

        JSONObject obj = new JSONObject();
        obj.put("value", new JSONNumber(1));

        col.add(obj, new Options("parse", true));

        assertEquals(2, col.at(0).get("value"));
    }

    public void testAddWithParseAndMerge() {
        class ParseCollection extends Collection<Model> {
            @Override
            public List<Model> parse(OptionsList models, Options options) {
                OptionsList filtered = new OptionsList();
                for (Options model : models) {
                    if(model.containsKey("model"))
                        filtered.add(model.<Options>get("model"));
                }

                return super.parse(filtered, options);
            }
        }

        Collection<Model> collection = new ParseCollection();
        collection.add(new Options("id", 1));
        collection.add(new OptionsList(new Options("model", new Options("id", 1, "name", "Alf"))), new Options("parse", true, "merge", true));

        assertEquals("Alf", collection.first().get("name"));
    }

    public void testAddModelToCollectionWithSortStyleComparator() {
        Collection<Model> col = new Collection<Model>();
        col.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model a, Model b) {
                return a.<String>get("name").compareTo(b.<String>get("name"));
            }
        });

        Model tom = new Model(new Options("name", "Tom"));
        Model rob = new Model(new Options("name", "Rob"));
        Model tim = new Model(new Options("name", "Tim"));

        col.add(tom);
        col.add(rob);
        col.add(tim);

        assertEquals(0, col.indexOf(rob));
        assertEquals(1, col.indexOf(tim));
        assertEquals(2, col.indexOf(tom));
    }

    public void testComparatorThatDependsOnThis() {

        class NegativeCollection extends Collection<Model> {

            public void registerNegativeComparator() {
                registerComparator(new Comparator<Model>() {
                    @Override
                    public int compare(Model a, Model b) {
                        return a.getIdAsInt();
                    }
                });
            }

            public void registerDoubleNegativeComparator() {
                registerComparator(new Comparator<Model>() {
                    @Override
                    public int compare(Model a, Model b) {
                        return NegativeCollection.this.negative(b.getIdAsInt()) - NegativeCollection.this.negative(a.getIdAsInt());
                    }
                });
            }

            protected int negative(int num) {
                return -num;
            }
        }

        NegativeCollection col = new NegativeCollection();
        col.registerNegativeComparator();
        col.add(new OptionsList(
                new Options("id", 1),
                new Options("id", 2),
                new Options("id", 3)
        ));
        assertEquals(Arrays.asList(new Object[]{3, 2, 1}), Arrays.asList(col.pluck("id")));

        col.registerDoubleNegativeComparator();
        col.sort();
        assertEquals(Arrays.asList(new Object[]{1, 2, 3}), Arrays.asList(col.pluck("id")));
    }

    public void testRemove() {
        final String[] removed = {null};
        final Boolean[] otherRemoved = {null};

        col.on("remove", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);
                Options options = this.getArgument(3);

                removed[0] = model.get("label");
                assertEquals(3, options.get("index"));
            }
        });

        otherCol.on("remove", new Function() {
            @Override
            public void f() {
                otherRemoved[0] = true;
            }
        });
        col.remove(d);

        assertEquals("d", removed[0]);
        assertEquals(3, col.length());
        assertEquals(a, col.first());
        assertEquals(otherRemoved[0], null);
    }

    public void testShiftAndPop() {
        Collection<Model> col = new Collection<Model>(
                new Options("a", "a"),
                new Options("b", "b"),
                new Options("c", "c")
        );
        assertEquals("a", col.shift().get("a"));
        assertEquals("c", col.pop().get("c"));
    }

    public void testSlice() {
        Collection<Model> col = new Collection<Model>(
                new Options("a", "a"),
                new Options("b", "b"),
                new Options("c", "c")
        );
        List<Model> array = col.slice(1, 3);

        assertEquals(2, array.size());
        assertEquals("b", array.get(0).get("b"));
    }

    public void testEventsAreUnboundOnRemove() {
        final int[] counter = {0};

        Model dj = new Model();
        Collection<Model> emcees = new Collection<Model>(dj);
        emcees.on("change", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        dj.set(new Options("name", "Kool"));
        dj.set(new Options("name", "Kool2"));
        assertEquals(1, counter[0]);

        emcees.reset();
        assertEquals(null, dj.getCollection());

        dj.set(new Options("name", "Shadow"));
        assertEquals(1, counter[0]);
    }

    public void testRemoveInMultipleCollections() {
        Options modelData = new Options("id", 5, "title", "Othello");
        final boolean[] passed = {false};

        Model e = new Model(modelData);
        Model f = new Model(modelData);

        f.on("remove", new Function() {
            @Override
            public void f() {
                passed[0] = true;
            }
        });

        Collection<Model> colE = new Collection<Model>(e);
        Collection<Model> colF = new Collection<Model>(f);

        assertTrue(!e.equals(f));
        assertTrue(colE.length() == 1);
        assertTrue(colF.length() == 1);

        colE.remove(e);
        assertEquals(false, passed[0]);

        assertTrue(colE.length() == 0);

        colF.remove(f);
        assertTrue(colF.length() == 0);
        assertEquals(true, passed[0]);
    }

    public void testRemoveSameModelInMultipleCollection() {
        final int[] counter = {0};
        final Model e = new Model(new Options("id", 5, "title", "Othello"));
        final Collection<Model> colE = new Collection<Model>(e);
        final Collection<Model> colF = new Collection<Model>(e);

        e.on("remove", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(0);
                Collection collection = this.getArgument(1);

                counter[0]++;

                assertEquals(model, e);

                if(counter[0] > 1) {
                    assertEquals(colE, collection);
                } else {
                    assertEquals(colF, collection);
                }
            }
        });

        colE.on("remove", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);
                Collection collection = this.getArgument(2);

                assertEquals(model, e);
                assertEquals(collection, colE);
            }
        });

        colF.on("remove", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);
                Collection collection = this.getArgument(2);

                assertEquals(model, e);
                assertEquals(collection, colF);
            }
        });

        assertEquals(e.getCollection(), colE);

        colF.remove(e);
        assertEquals(0, colF.length());
        assertEquals(1, colE.length());

        assertEquals(1, counter[0]);
        assertEquals(e.getCollection(), colE);

        colE.remove(e);
        assertEquals(e.getCollection(), null);
        assertEquals(0, colE.length());
        assertEquals(2, counter[0]);
    }

    public void testModelDestroyRemovesFromAllCollections() {
        class LocalModel extends Model {
            public LocalModel(Options attributes) {
                super(attributes);
            }

            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }
        }

        LocalModel e = new LocalModel(new Options("id", 5, "title", "Othello"));

        Collection<LocalModel> colE = new Collection<LocalModel>(e);
        Collection<LocalModel> colF = new Collection<LocalModel>(e);

        e.destroy();

        assertEquals(0, colE.length());
        assertEquals(0, colF.length());

        assertEquals(e.getCollection(), null);
    }

    public void testCollectionNonPersistedModelDestroyRemovesFromAllCollections() {
        class LocalModel extends Model {
            public LocalModel(Options attributes) {
                super(attributes);
            }

            @Override
            public Promise sync(String method, Options options) {
                throw new Error("Should not be called");
            }
        }
        LocalModel e = new LocalModel(new Options("title", "Othello"));

        Collection<LocalModel> colE = new Collection<LocalModel>(e);
        Collection<LocalModel> colF = new Collection<LocalModel>(e);

        e.destroy();

        assertEquals(0, colE.length());
        assertEquals(0, colF.length());

        assertEquals(e.getCollection(), null);
    }

    public void testFetchWithAnErrorResponseTriggersAnErrorEvent() {
        class LocalCollection extends Collection<Model> {
            @Override
            public Promise sync(String method, Options options) {
                Function error = options.get("error");
                error.f();

                return null;
            }
        }

        final int[] counter = {0};

        LocalCollection collection = new LocalCollection();
        collection.on("error", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        collection.fetch();

        assertEquals(1, counter[0]);
    }

    public void testFetchWithAnErrorResponseCallsErrorWithOption() {
        class LocalCollection extends Collection<Model> {
            @Override
            public Promise sync(String method, Options options) {
                Function error = options.get("error");
                error.f();

                return null;
            }
        }

        final Object errorObject = new Object();

        Options options = new Options(
                "context", errorObject,
                "error", new Function() {
                @Override
                public void f() {
                    Options options = this.getArgument(2);
                    assertEquals(errorObject, options.get("context"));
                }
            }
        );

        LocalCollection collection = new LocalCollection();
        collection.fetch(options);
    }

    public void testEnsureFetchOnlyParsesOnce() {
        final int[] counter = {0};

        class LocalCollection extends Collection<Model> {
            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }

            @Override
            public List<Model> parse(JSONValue resp, Options options) {
                counter[0]++;
                return super.parse(resp, options);
            }
        }
        LocalCollection collection = new LocalCollection();
        collection.setUrl("/test");
        collection.fetch();

        assertEquals(1, counter[0]);
    }

    public void testCreate() {
        Collection<TestSyncCreateModel> collection = new Collection<TestSyncCreateModel>(TestSyncCreateModel.class);
        collection.setUrl("/test");

        TestSyncCreateModel model = collection.create(new Options("label", "f"), new Options("wait", true));

        assertEquals("create", model.getLastSyncMethod());
        assertEquals("f", model.get("label"));
        assertEquals(collection, model.getCollection());
    }

    public void testCreateWithValidateTrueEnforcesValidation() {
        Collection<ValidatingModel> col = new Collection<ValidatingModel>(ValidatingModel.class);
        col.on("invalid", new Function() {
            @Override
            public void f() {
                String error = getArgument(2);
                Options options = getArgument(3);

                assertEquals("fail", error);
                assertEquals("fail", options.get("validationError"));
            }
        });

        ValidatingModel validatingModel = col.create(new Options("foo", "bar"), new Options("validate", true));
        assertEquals(false, validatingModel.isValid());
    }

    public void testCreateWillPassExtraOptionsToSuccessCallback() {
        Collection<ExtendedOptionsSyncModel> collection = new Collection<ExtendedOptionsSyncModel>(ExtendedOptionsSyncModel.class, new Options("url", "/test"));

        Function success = new Function() {
            @Override
            public void f() {
                Options options = getArgument(2);
                assertTrue(options.getBoolean("specialSync"));
            }
        };
        collection.create(new Options(), new Options("success", success));
    }

    public void testAFailingCreateReturnsModelWithErrors() {
        Collection<ValidatingModel> col = new Collection<ValidatingModel>(ValidatingModel.class);
        ValidatingModel model = col.create(new Options("foo", "bar"));

        assertFalse(model.isValid());
        assertEquals(1, col.length());
    }

    public void testInitialize() {
        class InitializeCollection extends Collection {
            private int one;

            public int getOne() {
                return one;
            }

            @Override
            protected void initialize(List models) {
                this.one = 1;
            }
        }

        InitializeCollection col = new InitializeCollection();
        assertEquals(1, col.getOne());
    }

    public void testToJSON() {
        assertEquals("[{\"id\":3, \"label\":\"a\"},{\"id\":2, \"label\":\"b\"},{\"id\":1, \"label\":\"c\"},{\"id\":0, \"label\":\"d\"}]", col.toJSON().toJsonString());
    }

    public void testWhereAndFindWhere() {
        Model model = new Model(new Options("a", 1));
        Collection<Model> coll = new Collection<Model>();
        coll.add(model);
        coll.add(
                new OptionsList(
                        new Options("a", 1),
                        new Options("a", 1, "b", 2),
                        new Options("a", 2, "b", 2),
                        new Options("a", 3)
                )
        );

        assertEquals(3, coll.where(new Options("a", 1)).size());
        assertEquals(1, coll.where(new Options("a", 2)).size());
        assertEquals(1, coll.where(new Options("a", 3)).size());
        assertEquals(0, coll.where(new Options("b", 1)).size());
        assertEquals(2, coll.where(new Options("b", 2)).size());
        assertEquals(1, coll.where(new Options("a", 1, "b", 2)).size());
        assertEquals(model, coll.findWhere(new Options("a", 1)));
        assertEquals(null, coll.findWhere(new Options("a", 4)));
    }

    public void testUnderscoreMethods() {
        JsArray<String> labels = col.jsMap(new MapFunction<String, Model>() {
            @Override
            public String f(Model model, int index, List<Model> models) {
                return model.get("label");
            }
        });
        assertEquals(labels.join(" "), "a b c d");

        boolean hasAny = col.any(new FilterFunction<Model>() {
            @Override
            public boolean f(Model model, int index, List<Model> models) {
                return model.getIdAsInt() == 100;
            }
        });
        assertFalse(hasAny);

        hasAny = col.any(new FilterFunction<Model>() {
            @Override
            public boolean f(Model model, int index, List<Model> models) {
                return model.getIdAsInt() == 0;
            }
        });
        assertTrue(hasAny);

        assertEquals(1, col.indexOf(b));

        List<Model> rest = col.rest();
        assertEquals(3, rest.size());
        assertFalse(rest.contains(a));
        assertTrue(rest.contains(b));

        assertFalse(col.isEmpty());

        assertFalse(col.without(d).contains(d));

        Model maxModel = col.max(new MinMaxFunction<Model>() {
            @Override
            public int f(Model model, int index, List<Model> models) {
                return model.getIdAsInt();
            }
        });
        assertEquals(3, maxModel.getIdAsInt());


        Model minModel = col.min(new MinMaxFunction<Model>() {
            @Override
            public int f(Model model, int index, List<Model> models) {
                return model.getIdAsInt();
            }
        });
        assertEquals(0, minModel.getIdAsInt());
        assertEquals(Arrays.asList(new Model[]{a, b}), col.difference(new Model[]{c, d}));
        assertTrue(col.contains(col.sample()));

        Model first = col.first();
        assertEquals(first, col.indexBy("id").get(first.getId()));
    }

    public void testReset() {
        List<Model> models = col.slice();

        final int[] resetCount = {0};
        col.on("reset", new Function() {
            @Override
            public void f() {
                resetCount[0]++;
            }
        });
        col.reset();

        assertEquals(1, resetCount[0]);
        assertEquals(0, col.length());
        assertEquals(null, col.last());

        col.reset(models);

        assertEquals(2, resetCount[0]);
        assertEquals(4, col.length());
        assertEquals(d, col.last());

        col.reset(new OptionsList(col.map(new MapFunction<Options, Model>() {
            @Override
            public Options f(Model model, int index, List<Model> models) {
                return model.getAttributes();
            }
        })));

        assertEquals(3, resetCount[0]);
        assertEquals(4, col.length());
        assertNotSame(d, col.last());
        assertEquals(d.getAttributes(), col.last().getAttributes());

        col.reset();
        assertEquals(0, col.length());
        assertEquals(4, resetCount[0]);

        Model f = new Model(new Options("id", 20, "label", "f"));
        col.reset(Arrays.asList(null, f));
        assertEquals(2, col.length());
        assertEquals(5, resetCount[0]);

        col.reset(new Model[4]);
        assertEquals(4, col.length());
        assertEquals(6, resetCount[0]);
    }

    public void testWithDifferentValues() {
        Collection<Model> col = new Collection<Model>(new Options("id", 1));
        col.reset(new Options("id", 1, "a", 1));

        assertEquals(1, col.get(1).get("a"));
    }

    public void testSameReferencesInReset() {
        Model model = new Model(new Options("id", 1));
        Collection<Model> collection = new Collection<Model>(new Options("id", 1));

        collection.reset(model);

        assertEquals(model, collection.get(1));
    }

    public void testResetPassesCallerOptions() {
        Collection<ParameterModel> col = new Collection<ParameterModel>(ParameterModel.class);
        col.reset(new OptionsList(
                new Options("astring", "green", "anumber", 1),
                new Options("astring", "blue", "anumber", 2)
        ), new Options("model_parameter", "model parameter"));

        assertEquals(2, col.length());

        for (ParameterModel model : col) {
            assertEquals("model parameter", model.getModelParameter());
        }
    }

    public void testResetDoesNotAlterOptionsByReference() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 1)
        ));
        final Options origOpts = new Options();

        col.on("reset", new Function() {
            @Override
            public void f() {
                Options opts = getArgument(1);
                List<Model> previousModels = opts.get("previousModels");

                assertFalse(origOpts.containsKey("previousModels"));
                assertEquals(1, previousModels.get(0).getIdAsInt());
            }
        });
        col.reset(new ArrayList<Model>(), origOpts);
    }

    public void testTriggerCustomEventsOnModels() {
        final boolean[] fired = {false};

        a.on("custom", new Function() {
            @Override
            public void f() {
                fired[0] = true;
            }
        });
        a.trigger("custom");
        assertTrue(fired[0]);
    }

    public void testAddDoesNotAlterArguments() {
        Options attrs = new Options();
        OptionsList models = new OptionsList(attrs);

        new Collection().add(models);
        assertEquals(1, models.size());

        assertEquals(models.get(0), attrs);
    }

    public void testAccessModelCollectionInABrandNewModel() {
        Collection<TestAccessModel> collection = new Collection<TestAccessModel>(TestAccessModel.class);
        collection.setUrl("/test");

        collection.create(new Options("prop", "value"));
    }

    public void testRemoveItsOwnReferenceToTheModelsArray() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 1),
                new Options("id", 2),
                new Options("id", 3),
                new Options("id", 4),
                new Options("id", 5),
                new Options("id", 6)
        ));

        assertEquals(6, col.length());
        col.remove(col.slice());

        assertEquals(0, col.length());
    }

    public void testAddingModelsToACollectionWhichDoNotPassValidationWithValidateTrue() {
        final boolean[] invalidCalled = {false};

        Collection<ValidatingThreeFailModel> collection = new Collection<ValidatingThreeFailModel>(ValidatingThreeFailModel.class);
        collection.on("invalid", new Function() {
            @Override
            public void f() {
                invalidCalled[0] = true;
            }
        });

        collection.add(new OptionsList(
                new Options("id", 1),
                new Options("id", 2),
                new Options("id", 3),
                new Options("id", 4),
                new Options("id", 5),
                new Options("id", 6)
        ), new Options("validate", true));

        assertTrue(invalidCalled[0]);
        assertEquals(Arrays.asList(1, 2, 4, 5, 6), Arrays.asList(collection.pluck("id")));
    }

    public void testInvalidModelsAreDiscardedWithValidateTrue() {
        final boolean[] testWasTriggered = {false};

        Collection<ValidatingInvalidModel> collection = new Collection<ValidatingInvalidModel>(ValidatingInvalidModel.class);
        collection.on("test", new Function() {
            @Override
            public void f() {
                testWasTriggered[0] = true;
            }
        });

        ValidatingInvalidModel model = new ValidatingInvalidModel(new Options("id", 1, "valid", true));
        collection.add(model, new Options("validate", true));
        collection.add(new Options("id", 2), new Options("validate", true));

        model.trigger("test");
        assertTrue(testWasTriggered[0]);

        assertNotNull(collection.get(model.getCid()));
        assertNotNull(collection.get(1));
        assertNull(collection.get(2));

        assertEquals(1, collection.length());
    }

    public void testMultipleCopiesOfTheSameModel() {
        Collection<Model> col = new Collection<Model>();
        Model model = new Model();

        col.add(Arrays.asList(model, model));
        assertEquals(1, col.length());

        col.add(new OptionsList(
                new Options("id", 1),
                new Options("id", 1)
        ));
        assertEquals(2, col.length());
        assertEquals(1, col.last().getIdAsInt());
    }

    public void testPassingOptionsModelSetsCollectionModel() {
        Collection<TestModel> col = new Collection<TestModel>(TestModel.class, new OptionsList(
                new Options("id", 1)
        ));
        assertFalse(col.registerModelClass(TestModel.class));
        assertTrue(col.at(0) != null);
    }

    public void testNullAndUndefinedAreInvalidIds() {
        Model model = new Model(new Options("id", 1));
        Collection<Model> collection = new Collection<Model>(model);

        model.set(new Options("id", null));
        assertNull(collection.get("null"));

        model.set(new Options("id", 1));
        model.set(new Options("id", null));

        assertNull(collection.get("null"));
    }

    public void testOptionsIsPassedToSuccessCallbacks() {
        class LocalCollection extends Collection<SimpleSyncModel> {
            @Override
            public Promise sync(String method, Options options) {
                Function success = options.get("success");
                success.f();

                return null;
            }
        }

        SimpleSyncModel m = new SimpleSyncModel(new Options("x", 1));
        LocalCollection col = new LocalCollection();

        Options opts = new Options(
                "opts", true,
                "success", new Function() {
            @Override
            public void f() {
                Options options = this.getArgument(2);
                assertTrue(options.getBoolean("opts"));
            }
        });

        col.fetch(opts);
        col.create(m, opts);
    }

    public void testTriggerRequestAndSyncEvents() {
        class LocalCollection extends Collection<AjaxSyncModel> {
            public LocalCollection() {
                super(AjaxSyncModel.class);
            }

            @Override
            public Promise sync(String method, Options options) {
                Promise sync = super.sync(method, options);

                Function success = options.get("success");
                success.f();

                return sync;
            }
        }

        final LocalCollection collection = new LocalCollection();
        collection.setUrl("/test");

        collection.on("request", new Function() {
            @Override
            public void f() {
                Synchronized obj = getArgument(0);
                Promise xhr = getArgument(1);
                Options options = getArgument(2);

                assertEquals(collection, obj);
            }
        });
        collection.on("sync", new Function() {
            @Override
            public void f() {
                Synchronized obj = getArgument(0);

                assertEquals(collection, obj);
            }
        });
        collection.fetch();
        collection.off();

        collection.on("request", new Function() {
            @Override
            public void f() {
                Synchronized obj = getArgument(1);

                assertEquals(collection.get(1), obj);
            }
        });
        collection.on("sync", new Function() {
            @Override
            public void f() {
                Synchronized obj = getArgument(0);

                assertEquals(collection.get(1), obj);
            }
        });
        collection.create(new Options("id", 1));
        collection.off();
    }

    public void testCreateWithWaitAddsModel() {
        final int[] addCount = {0};

        Collection<SimpleSyncModel> collection = new Collection<SimpleSyncModel>(SimpleSyncModel.class);
        SimpleSyncModel model = new SimpleSyncModel();

        collection.on("add", new Function() {
            @Override
            public void f() {
                addCount[0]++;
            }
        });

        collection.create(model, new Options("wait", true));
        assertEquals(1, addCount[0]);
    }

    public void testAddSortsCollectionAfterMerge() {
        Collection<Model> collection = new Collection<Model>(new OptionsList(
                new Options("id", 1, "x", 1),
                new Options("id", 2, "x", 2)
        ));

        collection.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return o1.get("x");
            }
        });
        collection.add(new Options("id", 1, "x", 3), new Options("merge", true));

        assertEquals(Arrays.asList(collection.pluck("id")), Arrays.asList(new Object[]{2, 1}));
    }

    public void testGroupByCanBeUsedWithAStringArgument() {
        Collection<Model> collection = new Collection<Model>(new OptionsList(
                new Options("x", 1),
                new Options("x", 2)
        ));
        Map<Integer, List<Model>> grouped = collection.groupBy("x");

        assertEquals(2, grouped.keySet().size());
        assertEquals(1, grouped.get(1).get(0).getInt("x"));
        assertEquals(2, grouped.get(2).get(0).getInt("x"));
    }

    public void testSortByCanBeUsedWithAStringArgument() {
        Collection<Model> collection = new Collection<Model>(new OptionsList(
                new Options("x", 3),
                new Options("x", 1),
                new Options("x", 2)
        ));

        List<Model> sortBy = collection.sortBy("x");

        assertEquals(1, sortBy.get(0).getInt("x"));
        assertEquals(2, sortBy.get(1).getInt("x"));
        assertEquals(3, sortBy.get(2).getInt("x"));
    }

    public void testRemovalDuringIteration() {
        final Collection<Model> collection = new Collection<Model>(new OptionsList(
                new Options(), new Options()
        ));
        collection.on("add", new Function() {
            @Override
            public void f() {
                collection.at(0).destroy();
            }
        });
        assertEquals(2, collection.length());
        collection.add(new Options(), new Options("at", 0));

        assertEquals(2, collection.length());
    }

    public void testSortDuringAddTriggersCorrectly() {
        final Collection<Model> collection = new Collection<Model>();
        collection.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return o1.get("x");
            }
        });

        final List<Integer> added = new ArrayList<Integer>();
        collection.on("add", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(1);

                model.set("x", 3);
                collection.sort();
                added.add(model.getIdAsInt());
            }
        });

        collection.add(new OptionsList(new Options("id", 1, "x", 1), new Options("id", 2, "x", 2)));
        assertEquals(Arrays.asList(1, 2), added);
    }

    public void testSortShouldntAlwaysFireOnAdd() {
        final int[] sortCount = {0};

        Collection<Model> c = new Collection<Model>(new OptionsList(
                new Options("id", 1),
                new Options("id", 2),
                new Options("id", 3)
        ));
        c.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return Integer.compare(o1.getIdAsInt(), o2.getIdAsInt());
            }
        });
        c.on("sort", new Function() {
            @Override
            public void f() {
                sortCount[0]++;
            }
        });

        c.sort(); // triggers sort
        c.add(new OptionsList()); // doesn't triggers sort (empty list)
        c.add(new Options("id", 1)); // doesn't triggers sort (object already exists)
        c.add(new OptionsList(new Options("id", 2), new Options("id", 3))); // doesn't triggers sort (objects already exists)
        c.add(new Options("id", 4)); // triggers sort

        assertEquals(2, sortCount[0]);
    }

    public void testModelAndCollectionParse() {
        // mocking a JSON response from a service
        JSONArray models = new JSONArray();

        JSONObject a = new JSONObject();
        a.put("a", new JSONNumber(1));

        JSONObject b = new JSONObject();
        b.put("a", new JSONNumber(2));

        JSONObject c = new JSONObject();
        c.put("a", new JSONNumber(3));

        JSONObject d = new JSONObject();
        d.put("a", new JSONNumber(4));

        models.set(0, a);
        models.set(1, b);
        models.set(2, c);
        models.set(3, d);

        Collection<Model> collection = new Collection<Model>(models);

        assertEquals(4, collection.length());
        assertEquals(1, collection.first().getInt("a"));
        assertEquals(4, collection.last().getInt("a"));
    }

    public void testResetIncludesPreviousModelsInTriggeredEvent() {
        final Model model = new Model();

        Collection<Model> collection = new Collection<Model>(Arrays.asList(model));
        collection.on("reset", new Function() {
            @Override
            public void f() {
                Options options = this.getArgument(1);

                assertEquals(Collections.singletonList(model), options.get("previousModels"));
            }
        });
        collection.reset();
    }

    public void testSet() {
        final Model m1 = new Model();
        final Model m2 = new Model(new Options("id", 2));
        final Model m3 = new Model();

        Collection<Model> c = new Collection<Model>(m1, m2);

        // Test add/change/remove events
        c.on("add", new Function() {
            @Override
            public void f() {
                Model model = getArgument(1);
                assertEquals(m3, model);
            }
        });
        c.on("change", new Function() {
            @Override
            public void f() {
                Model model = getArgument(1);
                assertEquals(m2, model);
            }
        });
        c.on("remove", new Function() {
            @Override
            public void f() {
                Model model = getArgument(1);
                assertEquals(m1, model);
            }
        });

        // remove: false doesn't remove any models
        c.set(Collections.<Model>emptyList(), new Options("remove", false));
        assertEquals(2, c.length());

        // add: false doesn't add any models
        c.set(Arrays.asList(m1, m2, m3), new Options("add", false));
        assertEquals(2, c.length());

        // merge: false doesn't change any models
        c.set(Arrays.asList( m1, new Model(new Options("id", 2, "a", 1)) ), new Options("merge", false));
        assertFalse(m2.has("a"));

        // add: false, remove: false only merges existing models
        c.set(Arrays.asList( m1, new Model(new Options("id", 2, "a", 0)), m3, new Model(new Options("id", 4)) ), new Options("add", false, "remove", false));
        assertEquals(2, c.length());
        assertEquals(0, m2.get("a"));

        // default options add/remove/merge as appropriate
        c.set(Arrays.asList(new Model(new Options("id", 2, "a", 1)), m3));
        assertEquals(2, c.length());
        assertEquals(1, m2.get("a"));

        // Test removing models not passing an argument
        c.off("remove").on("remove", new Function() {
            @Override
            public void f() {
                Model model = getArgument(1);
                assertTrue(model == m2 || model == m3);
            }
        });
        c.set(Collections.<Model>emptyList());
        assertEquals(0, c.length());

        // Test null models on set doesn't clear collection
        c.off();
        c.set(new Options[]{new Options("id", 1) });
        c.set();
        assertEquals(1, c.length());
    }

    public void testSetWithOnlyCids() {
        final Model m1 = new Model();
        final Model m2 = new Model();

        Collection<Model> c = new Collection<Model>();
        c.set(m1, m2);
        assertEquals(2, c.length());
        c.set(m1);
        assertEquals(1, c.length());
        c.set(Arrays.asList(m1, m1, m1, m2, m2), new Options("remove", false));
        assertEquals(2, c.length());
    }

    public void testSetWithOnlyIdAttribute() {
        Options m1 = new Options("_id", 1);
        Options m2 = new Options("_id", 2);

        Collection<MongoModel> c = new Collection<MongoModel>();
        c.set(m1, m2);
        assertEquals(2, c.length());
        c.set(m1);
        assertEquals(1, c.length());
        c.set(new OptionsList(m1, m1, m1, m2, m2), new Options("remove", false));
    }

    public void testSetPlusMergeWithDefaultValuesDefined() {
        DefaultKeyModel m = new DefaultKeyModel(new Options("id", 1));
        Collection<DefaultKeyModel> col = new Collection<DefaultKeyModel>(DefaultKeyModel.class, m);
        assertEquals("value", col.first().get("key"));

        col.set(new Options("id", 1, "key", "other"));
        assertEquals("other", col.first().get("key"));

        col.set(new Options("id", 1, "other", "value"));
        assertEquals("other", col.first().get("key"));
        assertEquals(1, col.length());
    }

    public void testMergeWithoutMutation() {
        OptionsList data = new OptionsList(new Options("id", 1, "child", new Options("id", 2)));
        Collection<CreateChildModel> collection = new Collection<CreateChildModel>(
                CreateChildModel.class, data);

        assertEquals(1, collection.first().getIdAsInt());
        collection.set(data);
        assertEquals(1, collection.first().getIdAsInt());
        collection.set(new OptionsList(new Options("id", 2, "child", new Options("id", 2))).concat(data));

        assertEquals(Arrays.asList(collection.pluck("id")), Arrays.asList(2, 1));
    }

    public void testSetAndModelLevelParse() {
        Model model = new Model(new Options("id", 1));

        Collection<Model> collection = new Collection<Model>(model) {
            @Override
            public List<Model> parse(JSONValue resp, Options options) {
                List<Model> result = new ArrayList<Model>();
                JSONArray array = resp.isObject().get("models").isArray();
                if(array != null) {
                    for (int i = 0; i < array.size(); i++) {
                        JSONValue value = array.get(i);
                        JSONObject object = value.isObject();
                        if(object != null) {
                            JSONObject jsonModel = object.get("model").isObject();
                            if(jsonModel != null) {
                                Model model = GWT.create(Model.class);
                                model.set(new Options(jsonModel), options);

                                result.add(model);
                            }
                        }
                    }
                }
                return result;
            }
        };

        // mocking a JSON response from a service
        JSONObject response = new JSONObject();
        JSONArray models = new JSONArray();

        JSONObject model1 = new JSONObject();
        JSONObject a = new JSONObject();
        a.put("id", new JSONNumber(1));
        model1.put("model", a);

        JSONObject model2 = new JSONObject();
        JSONObject b = new JSONObject();
        b.put("id", new JSONNumber(2));
        model2.put("model", b);

        models.set(0, model1);
        models.set(1, model2);
        response.put("models", models);

        collection.set(response);
        assertEquals(model, collection.first());
    }

    public void testSetDataIsOnlyParsedOnce() {
        Collection<ParseOnceModel> collection = new Collection<ParseOnceModel>(ParseOnceModel.class);
        collection.set(new JSONObject());
    }

    public void testSetMatchesInputOrderInTheAbsenceOfAComparator() {
        Model one = new Model(new Options("id", 1));
        Model two = new Model(new Options("id", 2));
        Model three = new Model(new Options("id", 3));

        Collection<Model> collection = new Collection<Model>(one, two, three);
        collection.set(new OptionsList(new Options("id", 3), new Options("id", 2), new Options("id", 1)));
        assertEquals(collection.toList(), Arrays.asList(three, two, one));

        collection.set(new OptionsList(new Options("id", 1), new Options("id", 2)));
        assertEquals(collection.toList(), Arrays.asList(one, two));

        collection.set(two, three, one);
        assertEquals(collection.toList(), Arrays.asList(two, three, one));

        collection.set(new OptionsList(new Options("id", 1), new Options("id", 2)), new Options("remove", false));
        assertEquals(collection.toList(), Arrays.asList(two, three, one));

        collection.set(new OptionsList(new Options("id", 1), new Options("id", 2), new Options("id", 3)), new Options("merge", false));
        assertEquals(collection.toList(), Arrays.asList(one, two, three));

        collection.set(Arrays.asList(three, two, one, new Model(new Options("id", 4))), new Options("add", false));
        assertEquals(collection.toList(), Arrays.asList(one, two, three));
    }

    public void testPushShouldNotTriggerASort() {

        Collection<Model> collection = new Collection<Model>() {
            @Override
            public Collection<Model> sort() {
                assertTrue(false); // should not be triggered
                return null;
            }
        };
        collection.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return Integer.compare(o1.getIdAsInt(), o2.getIdAsInt());
            }
        });
        collection.push(new Options("id", 1));
    }

    public void testPushDuplicateModelsReturnTheCorrectOne() {
        Collection<Model> col = new Collection<Model>();
        Model model1 = col.push(new Options("id", 101));
        Model model2 = col.push(new Options("id", 101));

        assertEquals(model2.getCid(), model1.getCid());
    }

    public void testSetWithNonNormalId() {
        Collection<MongoModel> collection = new Collection<MongoModel>(
                MongoModel.class,
                new OptionsList(new Options("_id", 1))
        );
        collection.set(new OptionsList(new Options("_id", 1, "a", 1)), new Options("add", false));
        assertEquals(1, collection.first().get("a"));
    }

    public void testSortCanOptionallyBeTurnedOff() {
        Collection<Model> collection = new Collection<Model>() {
            @Override
            public Collection<Model> sort() {
                assertTrue(false); // should not be triggered
                return null;
            }
        };
        collection.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return Integer.compare(o1.getIdAsInt(), o2.getIdAsInt());
            }
        });
        collection.add(new OptionsList(new Options("id", 1)), new Options("sort", false));
    }

    public void testParseDataInTheRightOrderInSet() {
        Collection<Model> collection = new Collection<Model>() {
            @Override
            public List<Model> parse(JSONValue resp, Options options) {
                JSONObject data = resp.isObject();

                assertEquals("ok", data.get("status").isString().stringValue());
                return super.parse(data.get("data"), options);
            }
        };

        JSONObject response = new JSONObject();
        response.put("status", new JSONString("ok"));

        JSONArray models = new JSONArray();

        JSONObject a = new JSONObject();
        a.put("id", new JSONNumber(1));

        models.set(0, a);
        response.put("data", models);

        collection.set(response);
    }

    public void testAddOnlySortsWhenNecessary() {
        Collection<Model> collection = new Collection<Model>();
        collection.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                Integer a1 = o1.has("a") ? o1.<Integer>get("a") : -1;
                Integer a2 = o2.has("a") ? o2.<Integer>get("a") : -1;
                return a1.compareTo(a2);
            }
        });
        collection.on("sort", new Function() {
            @Override
            public void f() {
                assertTrue(true);
            }
        });
        collection.add(new Options("id", 4));
        collection.add(new Options("id", 1, "a", 1), new Options("merge", true)); // do sort, comparator change
        collection.add(new Options("id", 1, "b", 1), new Options("merge", true)); // do sort, comparator change

        collection.on("sort", new Function() {
            @Override
            public void f() {
                assertTrue(false); // function should not be called
            }
        });
        collection.add(new Options("id", 1, "a", 1), new Options("merge", true)); // don't sort, no comparator change
        collection.add(collection.toList()); // don't sort, nothing new
        collection.add(collection.toList(), new Options("merge", true));  // don't sort
    }

    public void testAttachOptionsToCollection() {
        Comparator<TestModel> comparator = new Comparator<TestModel>() {
            @Override
            public int compare(TestModel o1, TestModel o2) {
                return o1.getId().compareTo(o2.getId()) * -1;
            }
        };

        Collection<TestModel> collection = new Collection<TestModel>(new OptionsList(new Options("id", 1), new Options("id", 2)), new Options(
                "model", TestModel.class,
                "comparator", comparator
        ));

        assertEquals(TestModel.class, collection.first().getClass());
        assertEquals(2, collection.first().getIdAsInt());
    }

    public void testAddOverridesSetFlags() {
        final Collection collection = new Collection();
        collection.once("add", new Function() {
            @Override
            public void f() {
                Options options = getArgument(3);
                collection.add(new Options("id", 2), options);
            }
        });
        collection.set(new Options("id", 1));
        assertEquals(2, collection.length());
    }

    public void testCollectionCreateSuccessArguments() {
        Collection collection = new Collection() {
            @Override
            public Model create(Options attrs, Options options) {
                Model model = super.create(attrs, options);

                Function success = options.get("success");
                success.f(model, "response", options);

                return model;
            }
        };
        collection.setUrl("test");
        collection.create(new Options(), new Options(
                "success", new Function() {
            @Override
            public void f() {
                String response = getArgument(1);
                assertEquals("response", response);
            }
        }
        ));
    }

    public void testNestedParseWorksWithCollectionSet() {

        JSONObject data = generateDummyJSONData("");
        JSONObject newData = generateDummyJSONData("New");

        JobModel job = new JobModel(data);
        assertEquals("JobName", job.get("name"));
        assertEquals("Sub1", job.getItems().at(0).get("name"));
        assertEquals(2, job.getItems().length());

        assertEquals("One", job.getItems().get(1).getSubItems().get(1).get("subName"));
        assertEquals("Three", job.getItems().get(2).getSubItems().get(3).get("subName"));

        job.set(newData);
        assertEquals("NewJobName", job.get("name"));
        assertEquals("NewSub1", job.getItems().at(0).get("name"));
        assertEquals(2, job.getItems().length());

        assertEquals("NewOne", job.getItems().get(1).getSubItems().get(1).get("subName"));
        assertEquals("NewThree", job.getItems().get(2).getSubItems().get(3).get("subName"));
    }

    private JSONObject generateDummyJSONData(String prefix) {
        JSONObject data = new JSONObject();
        data.put("name", new JSONString(prefix + "JobName"));
        data.put("id", new JSONNumber(1));

        JSONArray items = new JSONArray();

        JSONObject subItem1 = new JSONObject();
        subItem1.put("id", new JSONNumber(1));
        subItem1.put("name", new JSONString(prefix + "Sub1"));

        JSONArray subItem1SubItems = new JSONArray();

        JSONObject subItem1SubItem1 = new JSONObject();
        subItem1SubItem1.put("id", new JSONNumber(1));
        subItem1SubItem1.put("subName", new JSONString(prefix + "One"));

        JSONObject subItem1SubItem2 = new JSONObject();
        subItem1SubItem2.put("id", new JSONNumber(2));
        subItem1SubItem2.put("subName", new JSONString(prefix + "Two"));

        subItem1SubItems.set(0, subItem1SubItem1);
        subItem1SubItems.set(1, subItem1SubItem2);

        subItem1.put("subItems", subItem1SubItems);

        JSONObject subItem2 = new JSONObject();
        subItem2.put("id", new JSONNumber(2));
        subItem2.put("name", new JSONString(prefix + "Sub2"));

        JSONArray subItem2SubItems = new JSONArray();

        JSONObject subItem2SubItem1 = new JSONObject();
        subItem2SubItem1.put("id", new JSONNumber(3));
        subItem2SubItem1.put("subName", new JSONString(prefix + "Three"));

        JSONObject subItem2SubItem2 = new JSONObject();
        subItem2SubItem2.put("id", new JSONNumber(4));
        subItem2SubItem2.put("subName", new JSONString(prefix + "Four"));

        subItem2SubItems.set(0, subItem2SubItem1);
        subItem2SubItems.set(1, subItem2SubItem2);

        subItem2.put("subItems", subItem2SubItems);

        items.set(0, subItem1);
        items.set(1, subItem2);

        data.put("items", items);

        return data;
    }

    public void testRemoveReferenceUnbindsAllCollectionEventsAndTies() {
        final int[] remove = {0};

        Model model = new Model(new Options("id", 1));
        Collection<Model> collection = new Collection<Model>(model) {
            @Override
            protected void removeReference(Model model) {
                super.removeReference(model);
                remove[0]++;
                assertNull(get(model.getId()));
                assertNull(get(model.getCid()));
                assertNull(model.getCollection());
            }
        };
        collection.remove(model);

        assertEquals(1, remove[0]);
    }

    public void testDoNotAllowDuplicateModelsToBeAddedOrSet() {
        Collection c = new Collection();

        c.add(new OptionsList(new Options("id", 1), new Options("id", 1)));
        assertEquals(1, c.length());
        assertEquals(1, c.toList().size());

        c.set(new OptionsList(new Options("id", 1), new Options("id", 1)));
        assertEquals(1, c.length());
        assertEquals(1, c.toList().size());
    }

    public void testSetWithAddFalseShouldNotGrow() {
        Collection collection = new Collection();
        collection.set(new OptionsList(new Options("id", 1)), new Options("add", false));

        assertEquals(0, collection.size());
        assertEquals(0, collection.toList().size());
    }

    public void testCreateWithWaitModelInstance() {
        Collection<TestSyncCollectionModel> collection = new Collection<TestSyncCollectionModel>();
        TestSyncCollectionModel model = new TestSyncCollectionModel(new Options("id", 1));
        model.setCollectionToTest(collection);

        collection.create(model, new Options("wait", true));
    }

    public void testPolymorphicModelsWorkWithSimpleConstructors() {

        Collection<Model> collection = new Collection<Model>(new OptionsList(
                new Options("id", 1, "type", "a"), new Options("id", 2, "type", "b")
        ), new Options("model", new ModelClassFunction<Model>() {
            @Override
            public Class<? extends Model> f(Options attributes) {
                String type = attributes.get("type");
                return type != null && type.equals("a") ? AModel.class : BModel.class;
            }
        }));

        assertEquals(2, collection.length());
        assertTrue(collection.at(0) instanceof AModel);
        assertEquals(1, collection.at(0).getIdAsInt());
        assertTrue(collection.at(1) instanceof BModel);
        assertEquals(2, collection.at(1).getIdAsInt());
    }

    public void testAddingAtIndexFiresWithCorrectAt() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("at", 0),
                new Options("at", 4)
        ));
        col.on("add", new Function() {
            @Override
            public void f() {
                Model model = getArgument(1);
                Options options = getArgument(3);

                assertEquals(options.getInt("index").intValue(), model.getInt("at"));
            }
        });
        col.add(new OptionsList(new Options("at", 1), new Options("at", 2), new Options("at", 3)), new Options("at", 1));
    }

    public void testIndexIsNotSentWhenAtIsNotSpecified() {
        Collection<Model> col = new Collection<Model>(new OptionsList(new Options("at", 0)));
        col.on("add", new Function() {
            @Override
            public void f() {
                Options options = getArgument(3);
                assertNull(options.get("index"));
            }
        });
        col.add(new OptionsList(new Options("at", 1), new Options("at", 2)));
    }

    public void testOrderChangingShouldTriggerASort() {
        Model one = new Model(new Options("id", 1));
        Model two = new Model(new Options("id", 2));
        Model three = new Model(new Options("id", 3));

        final int[] sortCallCount = {0};

        Collection<Model> collection = new Collection<Model>(one, two, three);
        collection.on("sort", new Function() {
            @Override
            public void f() {
                sortCallCount[0]++;
            }
        });
        collection.set(new OptionsList(new Options("id", 3), new Options("id", 2), new Options("id", 1)));
        assertEquals(1, sortCallCount[0]);
    }

    public void testAddingAModelShouldTriggerASort() {
        Model one = new Model(new Options("id", 1));
        Model two = new Model(new Options("id", 2));
        Model three = new Model(new Options("id", 3));

        final int[] sortCallCount = {0};

        Collection<Model> collection = new Collection<Model>(one, two, three);
        collection.on("sort", new Function() {
            @Override
            public void f() {
                sortCallCount[0]++;
            }
        });
        collection.set(new OptionsList(new Options("id", 1), new Options("id", 2), new Options("id", 3), new Options("id", 0)));
        assertEquals(1, sortCallCount[0]);
    }

    public void testOrderNotChangingShouldNotTriggerASort() {
        Model one = new Model(new Options("id", 1));
        Model two = new Model(new Options("id", 2));
        Model three = new Model(new Options("id", 3));

        final int[] sortCallCount = {0};

        Collection<Model> collection = new Collection<Model>(one, two, three);
        collection.on("sort", new Function() {
            @Override
            public void f() {
                sortCallCount[0]++;
            }
        });
        collection.set(new OptionsList(new Options("id", 1), new Options("id", 2), new Options("id", 3)));
        assertEquals(0, sortCallCount[0]);
    }

    public void testAddSupportsNegativeIndexes() {
        Collection<Model> col = new Collection<Model>(new OptionsList(new Options("id", 1)));
        col.add(new OptionsList(new Options("id", 2), new Options("id", 3)), new Options("at", -1));
        col.add(new OptionsList(new Options("id", 2.5)), new Options("at", -2));
        col.add(new OptionsList(new Options("id", 0.5)), new Options("at", -6));

        assertEquals("0.5,1,2,2.5,3", col.jsPluck("id").join(","));
    }

    public void testSetAcceptsOptionsAtAsAString() {
        Collection<Model> col = new Collection<Model>(new OptionsList(new Options("id", 1), new Options("id", 2)));
        col.add(new OptionsList(new Options("id", 3)), new Options("at", "1"));

        assertEquals(Arrays.asList(1, 3, 2), Arrays.asList(col.pluck("id")));
    }

    public void testAddingMultipleModelsTriggersUpdateEventOnce() {
        Collection<Model> col = new Collection<Model>();

        final int[] updateCallCount = {0};
        col.on("update", new Function() {
            @Override
            public void f() {
                updateCallCount[0]++;
            }
        });

        col.add(new OptionsList(new Options("id", 1), new Options("id", 2), new Options("id", 3)));

        assertEquals(1, updateCallCount[0]);
    }

    public void testRemovingModelsTriggersUpdateEventOnce() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 1), new Options("id", 2), new Options("id", 3)
        ));

        final int[] updateCallCount = {0};
        col.on("update", new Function() {
            @Override
            public void f() {
                updateCallCount[0]++;
            }
        });

        col.remove(new OptionsList(new Options("id", 1), new Options("id", 2)));

        assertEquals(1, updateCallCount[0]);
    }

    public void testRemoveDoesNotTriggerSetWhenNothingRemoved() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 1), new Options("id", 2)
        ));

        final int[] updateCallCount = {0};
        col.on("update", new Function() {
            @Override
            public void f() {
                updateCallCount[0]++;
            }
        });

        col.remove(new OptionsList(new Options("id", 3)));

        assertEquals(0, updateCallCount[0]);
    }

    public void testSetTriggersSetEventOnce() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 1), new Options("id", 2)
        ));

        final int[] updateCallCount = {0};
        col.on("update", new Function() {
            @Override
            public void f() {
                updateCallCount[0]++;
            }
        });

        col.set(new OptionsList(new Options("id", 1), new Options("id", 3)));

        assertEquals(1, updateCallCount[0]);
    }

    public void testSetDoesNotTriggerUpdateEventWhenNothingAddedNorRemoved() {
        Collection<Model> col = new Collection<Model>(new OptionsList(
                new Options("id", 1), new Options("id", 2)
        ));

        final int[] updateCallCount = {0};
        col.on("update", new Function() {
            @Override
            public void f() {
                updateCallCount[0]++;
            }
        });

        col.set(new OptionsList(new Options("id", 1), new Options("id", 2)));

        assertEquals(0, updateCallCount[0]);
    }
}
