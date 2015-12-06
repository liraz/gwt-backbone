package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.user.client.ui.RootPanel;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import static org.lirazs.gbackbone.client.core.data.Options.O;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.view.View;

/**
 * Created on 23/10/2015.
 */
public class GBackboneViewTestGwt extends GWTTestCase {

    private View view;

    public String getModuleName() {
        return "org.lirazs.gbackbone.GBackboneTest";
    }

    public void gwtSetUp() {
        /*$('#qunit-fixture').append(
                '<div id="testElement"><h1>Test</h1></div>'
        );*/
        GQuery.$("body").off();
        GQuery.$("body").undelegate();
        GQuery.$("body").append("<div id='testElement'><h1>Test</h1></div>");

        view = new View(O(
                "id", "test-view",
                "className", "test-view",
                "other", "non-special-option"
        ));
    }

    public void gwtTearDown() {
        GQuery.$("#testElement").remove();
    }


    public void testConstructor() {
        assertEquals("test-view", view.getEl().getId());
        assertEquals("test-view", view.getEl().getClassName());
        assertNull(view.getEl().getPropertyString("other"));
    }


    public void test$() {
        View view = new View();
        view.setElement("<p><a><b>test</b></a></p>");

        GQuery result = view.$("a b");

        assertEquals("test", result.get(0).getInnerHTML());
    }


    public void test$el() {
        View view = new View();
        view.setElement("<p><a><b>test</b></a></p>");

        assertEquals(1, view.getEl().getNodeType());
        assertEquals(view.getEl(), view.get$El().get(0));
    }


    public void testInitialize() {
        class CustomView extends View {
            private int one;
            private int two;

            @Override
            protected void initialize() {
                super.initialize();
                one = 1;
            }

            @Override
            protected void initialize(Options options) {
                super.initialize(options);
                two = 2;
            }

            public int getOne() {
                return one;
            }

            public int getTwo() {
                return two;
            }
        }
        assertEquals(1, new CustomView().getOne());
        assertEquals(2, new CustomView().getTwo());
    }


    public void testRender() {
        View view = new View();
        assertEquals("#render returns the view instance", view, view.render());
    }


    public void testDelegateEvents() {
        final int[] counter1 = {0};
        final int[] counter2 = {0};

        View view = new View(O("el", "#testElement"));
        Function increment = new Function() {
            @Override
            public void f() {
                counter1[0]++;
            }
        };

        view.get$El().on("click", new Function() {
            @Override
            public void f() {
                counter2[0]++;
            }
        });

        Properties events = Properties.create().set("click h1", increment);
        view.delegateEvents(events);

        view.$("h1").trigger("click");

        assertEquals(1, counter1[0]);
        assertEquals(1, counter2[0]);

        view.$("h1").trigger("click");

        assertEquals(2, counter1[0]);
        assertEquals(2, counter2[0]);

        view.delegateEvents(events);
        view.delegateEvents(events);
        view.delegateEvents(events);
        view.delegateEvents(events);

        view.$("h1").trigger("click");

        assertEquals(3, counter1[0]);
        assertEquals(3, counter2[0]);
    }


    public void testDelegate() {
        final int[] counter = {0};

        View view = new View(O("el", "#testElement"));
        view.delegate("click", "h1", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        view.delegate("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        view.$("h1").trigger("click");

        assertEquals(2, counter[0]);
        assertEquals("#delegate returns the view instance", view, view.delegate("test", new Function() {
            @Override
            public void f() {
                // do nothing.. just a test
            }
        }));
    }


    public void testDelegateEventsAllowsFunctionsForCallbacks() {
        final int[] counter = {0};

        View view = new View(O("el", "<p></p>"));

        Properties events = Properties.create().set("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        view.delegateEvents(events);
        view.get$El().trigger("click");
        assertEquals(1, counter[0]);

        view.get$El().trigger("click");
        assertEquals(2, counter[0]);

        view.delegateEvents(events);
        view.get$El().trigger("click");
        assertEquals(3, counter[0]);
    }


    public void testDelegateEventsIgnoreUndefinedMethods() {
        View view = new View(O("el", "<p></p>"));

        Properties events = Properties.create().set("click", null);
        view.delegateEvents(events);
        view.get$El().trigger("click");
    }


    public void testUndelegateEvents() {
        final int[] counter1 = {0};
        final int[] counter2 = {0};

        View view = new View(O("el", "#testElement"));
        Function increment = new Function() {
            @Override
            public void f() {
                counter1[0]++;
            }
        };

        view.get$El().on("click", new Function() {
            @Override
            public void f() {
                counter2[0]++;
            }
        });

        Properties events = Properties.create().set("click h1", increment);
        view.delegateEvents(events);

        view.$("h1").trigger("click");

        assertEquals(1, counter1[0]);
        assertEquals(1, counter2[0]);

        view.undelegateEvents();
        view.$("h1").trigger("click");

        assertEquals(1, counter1[0]);
        assertEquals(2, counter2[0]);

        view.delegateEvents(events);

        view.$("h1").trigger("click");

        assertEquals(2, counter1[0]);
        assertEquals(3, counter2[0]);
    }


    public void testUndelegate() {
        final int[] counter = {0};

        View view = new View(O("el", "#testElement"));
        view.delegate("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        view.delegate("click", "h1", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        view.undelegate("click");

        view.$("h1").trigger("click");
        view.get$El().trigger("click");

        assertEquals(0, counter[0]);
        assertEquals("#undelegate returns the view instance", view, view.undelegate());
    }


    public void testUndelegateWithPassedHandler() {
        final int[] counter = {0};

        View view = new View(O("el", "#testElement"));

        Function listener = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        view.delegate("click", listener);
        view.delegate("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        view.undelegate("click", listener);

        view.get$El().trigger("click");

        assertEquals(1, counter[0]);
    }


    public void testUndelegateWithSelector() {
        final int[] counter = {0};

        View view = new View(O("el", "#testElement"));
        view.delegate("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        view.delegate("click", "h1", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        view.undelegate("click", "h1");

        view.$("h1").trigger("click");
        view.get$El().trigger("click");

        assertEquals(2, counter[0]);
    }


    public void testUndelegateWithHandlerAndSelector() {
        final int[] counter = {0};

        View view = new View(O("el", "#testElement"));
        view.delegate("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        Function handler = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        view.delegate("click", "h1", handler);
        view.undelegate("click", "h1", handler);

        view.$("h1").trigger("click");
        view.get$El().trigger("click");

        assertEquals(2, counter[0]);
    }


    public void testTagNameCanBeProvidedAsAString() {
        class CustomView extends View {
            public CustomView() {
                super(O("tagName", "span"));
            }
            public CustomView(Options options) {
                super(options.defaults(O("tagName", "span")));
            }
        }

        View view = new View(O("tagName", "span"));

        assertEquals("SPAN", view.getEl().getTagName());
        assertEquals("SPAN", new CustomView().getEl().getTagName());
        assertEquals("SPAN", new CustomView(O("key", "value")).getEl().getTagName());
    }


    public void testTagNameCanBeProvidedAsAFunction() {
        class CustomView extends View {
            public CustomView() {
                super();
            }

            @Override
            public String getTagName() {
                return "p";
            }
        }

        assertTrue(new CustomView().get$El().is("p"));
    }


    public void testEnsureElementWithDOMNodeEl() {
        View view = new View(O("el", Document.get().getBody()));

        assertEquals(Document.get().getBody(), view.getEl());
    }


    public void testEnsureElementWithStringEl() {
        View view = new View(O("el", "body"));
        assertEquals(Document.get().getBody(), view.getEl());

        view = new View(O("el", "#testElement > h1"));
        assertEquals(GQuery.$("#testElement > h1").get(0), view.getEl());

        view = new View(O("el", "#nonexistent"));
        assertNull(view.getEl());
    }


    public void testWithClassNameAndIdFunctions() {
        class CustomView extends View {
            public CustomView() {
                super();
            }

            @Override
            public String getClassName() {
                return "className";
            }

            @Override
            public String getId() {
                return "id";
            }
        }

        assertEquals("className", new CustomView().getEl().getClassName());
        assertEquals("id", new CustomView().getEl().getId());
    }


    public void testWithAttributes() {
        class CustomView extends View {
            public CustomView() {
                super(O(
                        "attributes",
                        O(
                                "id", "id",
                                "class", "class"
                        )
                        ));
            }
        }

        assertEquals("class", new CustomView().getEl().getClassName());
        assertEquals("id", new CustomView().getEl().getId());
    }


    public void testWithAttributesAsAFunction() {
        class CustomView extends View {
            public CustomView() {
                super();
            }

            @Override
            public Options getAttributes() {
                return O("class", "dynamic");
            }
        }

        assertEquals("dynamic", new CustomView().getEl().getClassName());
    }


    public void testShouldDefaultToClassNameIdProperties() {
        View view = new View(O(
                "className", "backboneClass",
                "id", "backboneId",
                "attributes", O(
                    "class", "attributeClass",
                    "id", "attributeId"
                )
        ));

        assertEquals("backboneClass", view.getEl().getClassName());
        assertEquals("backboneId", view.getEl().getId());
        assertEquals("backboneClass", view.get$El().attr("class"));
        assertEquals("backboneId", view.get$El().attr("id"));
    }


    public void testMultipleViewsPerElement() {
        final int[] count = {0};
        final GQuery $el = GQuery.$("<p></p>");

        class CustomView extends View {
            public CustomView() {
                super(O("el", $el));
            }

            @Override
            protected Properties events() {
                return Properties.create().set(
                        "click", new Function() {
                            @Override
                            public void f() {
                                count[0]++;
                            }
                        }
                );
            }
        }

        View view1 = new CustomView();
        $el.trigger("click");
        assertEquals(1, count[0]);

        View view2 = new CustomView();
        $el.trigger("click");
        assertEquals(3, count[0]);

        view1.delegateEvents();
        $el.trigger("click");
        assertEquals(5, count[0]);
    }


    public void testCustomEvents() {
        final int[] count = {0};

        final GQuery button1 = GQuery.$("<button></button>");

        class CustomView extends View {
            public CustomView() {
                super(O("el", button1));
            }

            @Override
            protected Properties events() {
                return Properties.create().set(
                        "fake$event", new Function() {
                            @Override
                            public void f() {
                                count[0]++;
                            }
                        }
                );
            }
        }

        View view = new CustomView();
        button1.trigger("fake$event");
        button1.trigger("fake$event");

        button1.off("fake$event");
        button1.trigger("fake$event");

        assertEquals(2, count[0]);
    }


    public void testSetElementUsesProvidedObject() {
        GQuery $el = GQuery.$("body");

        View view = new View(O("el", $el));
        assertTrue($el == view.get$El());

        view.setElement($el = GQuery.$($el));
        assertTrue($el == view.get$El());
    }


    public void testUndelegateBeforeChangingElement() {
        GQuery button1 = GQuery.$("<button></button>");
        GQuery button2 = GQuery.$("<button></button>");

        final int[] count = {0};

        class CustomView extends View {
            public CustomView(Options options) {
                super(options);
            }

            @Override
            protected Properties events() {
                return Properties.create().set(
                        "click", new Function() {
                            @Override
                            public void f() {
                                count[0]++;
                            }
                        }
                );
            }
        }

        View view = new CustomView(O("el", button1));
        view.setElement(button2);

        button1.trigger("click");
        button2.trigger("click");

        assertEquals(1, count[0]);
    }


    public void testCloneAttributesObject() {

        class CustomView extends View {
            public CustomView() {
                super();
            }

            public CustomView(Options options) {
                super(options);
            }

            @Override
            public Options getAttributes() {
                return O("foo", "bar");
            }
        }

        View view1 = new CustomView(O("id", "foo"));
        assertEquals("foo", view1.getEl().getId());

        View view2 = new CustomView();
        // if id has nothing it returns an empty string instead of null...
        assertEquals("", view2.getEl().getId());
    }


    public void testViewsStopListening() {
        final int[] count = {0};

        class CustomView extends View {
            public CustomView(Options options) {
                super(options);
            }

            @Override
            protected void initialize() {
                this.listenTo(getModel(), "all x", new Function() {
                    @Override
                    public void f() {
                        count[0]++;
                    }
                });
                this.listenTo(getCollection(), "all x", new Function() {
                    @Override
                    public void f() {
                        count[0]++;
                    }
                });
            }
        }

        View view = new CustomView(O(
                "model", new Model(),
                "collection", new Collection<Model>()
        ));

        view.stopListening();
        view.getModel().trigger("x");
        view.getCollection().trigger("x");

        assertEquals(0, count[0]);
    }


    public void testProvideFunctionForEl() {
        class CustomView extends View {
            public CustomView() {
                super();
            }

            @Override
            public String getElSelector() {
                return "<p><a></a></p>";
            }
        }

        CustomView view = new CustomView();

        assertTrue(view.get$El().is("p"));
        assertTrue(view.get$El().has("a").length() > 0);
    }


    public void testEventsPassedInOptions() {
        final int[] counter = {0};

        class CustomView extends View {
            public CustomView(Options options) {
                super(O("el", "#testElement").extend(options));
            }
        }

        Function increment = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        View view = new CustomView(O(
                "events", Properties.create().set(
                    "click h1", increment
                )
        ));
        view.$("h1").trigger("click").trigger("click");

        assertEquals(2, counter[0]);
    }


    public void testRemove() {
        final int[] counter = {0};

        View view = new View();
        Document.get().getBody().appendChild(view.getEl());

        view.delegate("click", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        view.listenTo(view, "all x", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        assertEquals("#remove returns the view instance", view, view.remove());
        view.get$El().trigger("click");
        view.trigger("x");

        // In IE8 and below, parentNode still exists but is not document.body.
        assertNotSame(Document.get().getBody(), view.getEl().getParentNode());

        assertEquals(0, counter[0]);
    }


    public void testSetElement() {
        final int[] count = {0};

        View view = new View(O(
                "events", Properties.create().set(
                "click", new Function() {
                    @Override
                    public void f() {
                        count[0]++;
                    }
                }
            )
        ));

        Element oldEl = view.getEl();
        GQuery $oldEl = view.get$El();

        view.setElement(Document.get().createElement("div"));

        $oldEl.click();
        view.get$El().click();

        assertNotSame(view.getEl(), oldEl);
        assertNotSame(view.get$El(), $oldEl);

        assertEquals(1, count[0]);
    }

}
