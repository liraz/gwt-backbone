package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Promise;
import com.google.gwt.query.client.Properties;
import com.google.gwt.user.client.ui.RootPanel;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import static org.lirazs.gbackbone.client.core.data.Options.O;

import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.test.view.*;
import org.lirazs.gbackbone.client.core.view.Template;
import org.lirazs.gbackbone.client.core.view.TemplateFactory;
import org.lirazs.gbackbone.client.core.view.View;

import java.util.HashMap;

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

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("evaluate", "<%([\\s\\S]+?)%>");
        templateSettings.put("interpolate", "<%=([\\s\\S]+?)%>");
        templateSettings.put("escape", "<%-([\\s\\S]+?)%>");
        templateSettings.put("variable", null); // by default put all values in local scope
        TemplateFactory.templateSettings(templateSettings);
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

    public void testAnnotatedView() {
        final int[] counter = {0};

        AnnotatedView annotatedView = new AnnotatedView(O("el", "#testElement"), counter);
        annotatedView.$("h1").trigger("click").trigger("click");

        assertEquals(6, counter[0]);
    }

    public void testTemplate() {
        Template template = TemplateFactory.template("Hello world!!");
        String result = template.apply(O("a", 1, "b", 2, "c", "3"));

        assertEquals("Hello world!!", result);

        Template basicTemplate = TemplateFactory.template("<%= thing %> is gettin' on my noives!");
        result = basicTemplate.apply(O("thing", "This"));

        assertEquals("can do basic attribute interpolation", "This is gettin' on my noives!", result);

        Template sansSemicolonTemplate = TemplateFactory.template("A <% this %> B");
        assertEquals("A  B", sansSemicolonTemplate.apply());

        Template backslashTemplate = TemplateFactory.template("<%= thing %> is \\\\ridanculous");
        assertEquals("This is \\\\ridanculous", backslashTemplate.apply(O("thing", "This")));

        Template escapeTemplate  = TemplateFactory.template("<%= a ? 'checked=\"checked\"' : '' %>");
        assertEquals("can handle slash escapes in interpolations.", "checked=\"checked\"", escapeTemplate.apply(O("a", true)));

        Template fancyTemplate  = TemplateFactory.template("<ul><% for (var key in people) { %><li><%= people[key] %></li><% } %></ul>");
        result = fancyTemplate.apply(O("people", O("moe", "Moe", "larry", "Larry", "curly", "Curly")));
        assertEquals("can run arbitrary javascript in templates", "<ul><li>Moe</li><li>Larry</li><li>Curly</li></ul>", result);

        Template escapedCharsInJavascriptTemplate  = TemplateFactory.template("<ul><% numbers.split('\\n').forEach(function(item) { %><li><%= item %></li><% }) %></ul>");
        result = escapedCharsInJavascriptTemplate.apply(O("numbers", "one\ntwo\nthree\nfour"));
        assertEquals("Can use escaped characters (e.g. \n) in JavaScript", "<ul><li>one</li><li>two</li><li>three</li><li>four</li></ul>", result);

        Template namespaceCollisionTemplate  = TemplateFactory.template("<%= pageCount %> <%= thumbnails[pageCount] %> <% for(var p in thumbnails) { %><div class='thumbnail' rel='<%= thumbnails[p] %>'></div><% } %>");
        result = namespaceCollisionTemplate.apply(O(
                "pageCount", 3,
                "page", true,
                "thumbnails", O(
                        1, "p1-thumbnail.gif",
                        2, "p2-thumbnail.gif",
                        3, "p3-thumbnail.gif"
                )
        ));
        assertEquals("3 p3-thumbnail.gif <div class='thumbnail' rel='p1-thumbnail.gif'></div><div class='thumbnail' rel='p2-thumbnail.gif'></div><div class='thumbnail' rel='p3-thumbnail.gif'></div>", result);

        Template noInterpolateTemplate = TemplateFactory.template("<div><p>Just some text. Hey, I know this is silly but it aids consistency.</p></div>");
        assertEquals("<div><p>Just some text. Hey, I know this is silly but it aids consistency.</p></div>", noInterpolateTemplate.apply());

        Template quoteTemplate = TemplateFactory.template("It's its, not it's");
        assertEquals("It's its, not it's", quoteTemplate.apply());

        Template quoteInStatementAndBody = TemplateFactory.template("<%   if(foo == 'bar'){ %>Statement quotes and 'quotes'.<% } %>");
        assertEquals("Statement quotes and 'quotes'.", quoteInStatementAndBody.apply(O("foo", "bar")));

        Template withNewlinesAndTabs = TemplateFactory.template("This\n\t\tis: <%= x %>.\n\tok.\nend.");
        assertEquals("This\n\t\tis: that.\n\tok.\nend.", withNewlinesAndTabs.apply(O("x", "that")));

        template = TemplateFactory.template("<i><%- value %></i>");
        assertEquals("<i>&lt;script&gt;</i>", template.apply(O("value", "<script>")));

        template = TemplateFactory.template("\n " +
                "  <%\n " +
                "  // a comment\n " +
                "  if (data) { data += 12345; }; %>\n " +
                "  <li><%= data %></li>\n ");
        assertEquals("<li>24690</li>", template.apply(O("data", 12345)).replaceAll("/\\s/g", "").trim());

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("evaluate", "\\{\\{([\\s\\S]+?)\\}\\}");
        templateSettings.put("interpolate", "\\{\\{=([\\s\\S]+?)\\}\\}");
        TemplateFactory.templateSettings(templateSettings);

        Template custom = TemplateFactory.template("<ul>{{ for (var key in people) { }}<li>{{= people[key] }}</li>{{ } }}</ul>");
        result = custom.apply(O("people", O("moe", "Moe", "larry", "Larry", "curly", "Curly")));
        assertEquals("can run arbitrary javascript in templates", "<ul><li>Moe</li><li>Larry</li><li>Curly</li></ul>", result);

        Template customQuote = TemplateFactory.template("It's its, not it's");
        assertEquals("It's its, not it's", customQuote.apply());

        quoteInStatementAndBody = TemplateFactory.template("{{ if(foo == 'bar'){ }}Statement quotes and 'quotes'.{{ } }}");
        assertEquals("Statement quotes and 'quotes'.", quoteInStatementAndBody.apply(O("foo", "bar")));

        templateSettings = new HashMap<String, String>();
        templateSettings.put("evaluate", "<\\?([\\s\\S]+?)\\?>");
        templateSettings.put("interpolate", "<\\?=([\\s\\S]+?)\\?>");
        TemplateFactory.templateSettings(templateSettings);

        Template customWithSpecialChars = TemplateFactory.template("<ul><? for (var key in people) { ?><li><?= people[key] ?></li><? } ?></ul>");
        result = customWithSpecialChars.apply(O("people", O("moe", "Moe", "larry", "Larry", "curly", "Curly")));
        assertEquals("can run arbitrary javascript in templates", "<ul><li>Moe</li><li>Larry</li><li>Curly</li></ul>", result);

        Template customWithSpecialCharsQuote = TemplateFactory.template("It's its, not it's");
        assertEquals("It's its, not it's", customWithSpecialCharsQuote.apply());

        quoteInStatementAndBody = TemplateFactory.template("<? if(foo == 'bar'){ ?>Statement quotes and 'quotes'.<? } ?>");
        assertEquals("Statement quotes and 'quotes'.", quoteInStatementAndBody.apply(O("foo", "bar")));

        templateSettings = new HashMap<String, String>();
        templateSettings.put("evaluate", "");
        templateSettings.put("escape", "");
        templateSettings.put("variable", "");
        templateSettings.put("interpolate", "\\{\\{(.+?)\\}\\}");
        TemplateFactory.templateSettings(templateSettings);

        Template mustache = TemplateFactory.template("Hello {{planet}}!");
        assertEquals("can mimic mustache.js", "Hello World!", mustache.apply(O("planet", "World")));

        Template templateWithNull = TemplateFactory.template("a null undefined {{planet}}");
        assertEquals("can handle missing escape and evaluate settings", "a null undefined world", templateWithNull.apply(O("planet", "world")));
    }

    public void testTemplateHandlesUnicode() {
        Template tmpl = TemplateFactory.template("<p>\u2028<%= \"\\u2028\\u2029\" %>\u2029</p>");
        assertEquals("<p>\u2028\u2028\u2029\u2029</p>", tmpl.apply());
    }

    public void testTemplateSettingsVariable() {
        String s = "<%=data.x%>";
        Options data = O("x", "x");

        Template tmp = TemplateFactory.template(s, O("variable", "data"));
        assertEquals("x", tmp.apply(data));

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("variable", "data");
        TemplateFactory.templateSettings(templateSettings);

        tmp = TemplateFactory.template(s);
        assertEquals("x", tmp.apply(data));
    }

    public void testTemplateSettingsIsUnchangedByCustomSettings() {
        String s = "<%=data.x%>";
        Options data = O("x", "x");

        Template tmp = TemplateFactory.template(s, O("variable", "data"));
        assertEquals("x", tmp.apply(data));

        tmp = TemplateFactory.template("<%= x %>");
        assertEquals("x", tmp.apply(data));
    }

    public void testUndefinedTemplateVariables() {
        Template template = TemplateFactory.template("<%=x%>");
        assertEquals("", template.apply(O("x", null)));

        Template templateEscaped = TemplateFactory.template("<%-x%>");
        assertEquals("", templateEscaped.apply(O("x", null)));

        Template templateWithProperty = TemplateFactory.template("<%=x.foo%>");
        assertEquals("", templateWithProperty.apply(O("x", O())));

        Template templateWithPropertyEscaped = TemplateFactory.template("<%-x.foo%>");
        assertEquals("", templateWithPropertyEscaped.apply(O("x", O())));
    }

    public void testInterpolateEvaluatesCodeOnlyOnce() {
        final int[] count = {0};
        Template template = TemplateFactory.template("<%= f() %>");
        template.apply(O("f", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        }));

        template = TemplateFactory.template("<%- f() %>");
        template.apply(O("f", new Function() {
            @Override
            public void f() {
                count[0]++;
            }
        }));

        assertEquals(2, count[0]);
    }

    public void testDelimetersAreAppliedToUnescapedText() {
        Template template = TemplateFactory.template("<<\nx\n>>", O("evaluate", "<<(.*?)>>"));
        assertEquals("<<\nx\n>>", template.apply());
    }

    public void testTemplateFromScriptElement() {
        GQuery $script = GQuery.$("<script><i><%= value %></i></script>");

        Template template = TemplateFactory.template($script);
        assertEquals("<i>test</i>", template.apply(O("value", "test")));
    }

    public void testLoadTemplateFile() {
        final int[] count = {0};
        delayTestFinish(5000);

        final String filePath = "https://raw.githubusercontent.com/liraz/gwt-backbone/master/gwt-backbone-core/src/test/resources/com/lirazs/gbackbone/client/core/test/template/script.tpl";

        final Promise[] promise = {TemplateFactory.loadTemplate(filePath)};
        promise[0].done(new Function() {
            @Override
            public void f() {
                Template template = getArgument(0);
                assertNotNull(template);
                assertEquals("<script><i>test</i></script>", template.apply(O("value", "test")));
                count[0]++;

                // checking that it comes from cache on the second time
                promise[0] = TemplateFactory.loadTemplate(filePath);
                promise[0].done(new Function() {
                    @Override
                    public void f() {
                        Template template = getArgument(0);
                        assertNotNull(template);
                        assertEquals("<script><i>test</i></script>", template.apply(O("value", "test")));
                        count[0]++;
                    }
                });

                assertEquals(2, count[0]);
                finishTest();
            }
        });
    }

    public void testLoadTemplateFileWithRoot() {
        final int[] count = {0};
        delayTestFinish(5000);

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("urlRoot", "https://raw.githubusercontent.com/liraz/gwt-backbone/master/gwt-backbone-core/src/test/resources/com/lirazs/gbackbone/client/core/test/template/");
        TemplateFactory.templateSettings(templateSettings);

        final String filePath = "script.tpl";

        final Promise promise = TemplateFactory.loadTemplate(filePath);
        promise.done(new Function() {
            @Override
            public void f() {
                Template template = getArgument(0);
                assertNotNull(template);
                assertEquals("<script><i>test</i></script>", template.apply(O("value", "test")));
                count[0]++;

                final String filePath2 = "people.ejs";

                // checking that it comes from cache on the second time
                Promise promise2 = TemplateFactory.loadTemplate(filePath2);
                promise2.done(new Function() {
                    @Override
                    public void f() {
                        Template template = getArgument(0);
                        assertNotNull(template);
                        assertEquals("<ul><li>Moe</li><li>Larry</li><li>Curly</li></ul>",
                                template.apply(O("people", O("moe", "Moe", "larry", "Larry", "curly", "Curly"))));
                        count[0]++;

                        assertEquals(2, count[0]);
                        finishTest();
                    }
                });
            }
        });
    }

    public void testAnnotatedTemplateView() {
        delayTestFinish(5000);

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("urlRoot", "https://raw.githubusercontent.com/liraz/gwt-backbone/master/gwt-backbone-core/src/test/resources/com/lirazs/gbackbone/client/core/test/template/");
        TemplateFactory.templateSettings(templateSettings);

        Model model = new Model(O("value", "annotated"));

        AnnotatedTemplateView templateView = new AnnotatedTemplateView(O("model", model));
        assertEquals("annotated", templateView.get$El().html());

        Events events = new Events();

        Model peopleModel = new Model(O("people", O("moe", "Moe", "larry", "Larry", "curly", "Curly")));
        final AsyncAnnotatedTemplateView asyncTemplateView = new AsyncAnnotatedTemplateView(O("model", peopleModel));

        events.listenToOnce(asyncTemplateView, "template:complete", new Function() {
            @Override
            public void f() {
                assertEquals("<ul><li>Moe</li><li>Larry</li><li>Curly</li></ul>", asyncTemplateView.get$El().html());
                finishTest();
            }
        });
    }

    public void testInjectedViews() {
        delayTestFinish(5000);

        HashMap<String, String> templateSettings = new HashMap<String, String>();
        templateSettings.put("urlRoot", "https://raw.githubusercontent.com/liraz/gwt-backbone/master/gwt-backbone-core/src/test/resources/com/lirazs/gbackbone/client/core/test/template/");
        TemplateFactory.templateSettings(templateSettings);

        final Events events = new Events();

        final AnnotatedInjectedView annotatedInjectedView = new AnnotatedInjectedView();

        events.listenToOnce(annotatedInjectedView, "template:complete", new Function() {
            @Override
            public void f() {
                assertNotNull(annotatedInjectedView.emailInput);
                assertNotNull(annotatedInjectedView.emailRetypeInput);
                assertNotNull(annotatedInjectedView.phoneNumberInput);
                assertNotNull(annotatedInjectedView.addressInput);
                assertNotNull(annotatedInjectedView.cityInput);
                assertNotNull(annotatedInjectedView.formTitle);
                assertNotNull(annotatedInjectedView.getFormBottomTitle());

                final CustomRenderAnnotatedInjectedView customAnnotatedInjectedView = new CustomRenderAnnotatedInjectedView();

                assertNotNull(customAnnotatedInjectedView.emailInput);
                assertNotNull(customAnnotatedInjectedView.emailRetypeInput);
                assertNotNull(customAnnotatedInjectedView.phoneNumberInput);
                assertNotNull(customAnnotatedInjectedView.addressInput);
                assertNotNull(customAnnotatedInjectedView.cityInput);
                assertNotNull(customAnnotatedInjectedView.formTitle);
                assertNotNull(customAnnotatedInjectedView.formBottomTitle);

                finishTest();
            }
        });
    }
}
