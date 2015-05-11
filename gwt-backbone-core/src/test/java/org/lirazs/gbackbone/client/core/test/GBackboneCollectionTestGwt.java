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
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.data.OptionsList;
import org.lirazs.gbackbone.client.core.js.JsArray;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.test.model.MongoModel;
import org.lirazs.gbackbone.client.core.test.model.ParseModel;
import org.lirazs.gbackbone.client.core.test.model.TestModel;
import org.lirazs.gbackbone.client.core.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
                return a.getId() > b.getId() ? -1 : 1;
            }
        });
        col.sort();

        assertEquals(counter[0], 1);
        a = col.pluck("label");
        assertEquals(Arrays.asList(a), Arrays.asList(b));

        col.registerComparator(new Comparator<Model>() {
            @Override
            public int compare(Model a, Model b) {
                return a.getId();
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
                return Integer.valueOf(o1.getId()).compareTo(o2.getId());
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
                return Integer.valueOf(o1.getId()).compareTo(o2.getId());
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
        assertEquals(1, collection.get(1).getId());
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
                Model model = getArgument(0);
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
                Model model = this.getArgument(0);
                Options options = this.getArgument(2);

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
                return a.getId() > b.getId() ? -1 : 1;
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
                Model model = this.getArgument(0);
                Collection collection = this.getArgument(1);

                assertEquals(model, e);
                assertEquals(collection, colE);
            }
        });
        colF.on("add", new Function() {
            @Override
            public void f() {
                Model model = this.getArgument(0);
                Collection collection = this.getArgument(1);

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

        assertEquals(2.0d, col.at(0).get("value"));
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
                        return a.getId();
                    }
                });
            }

            public void registerDoubleNegativeComparator() {
                registerComparator(new Comparator<Model>() {
                    @Override
                    public int compare(Model a, Model b) {
                        return NegativeCollection.this.negative(b.getId()) - NegativeCollection.this.negative(a.getId());
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
}
