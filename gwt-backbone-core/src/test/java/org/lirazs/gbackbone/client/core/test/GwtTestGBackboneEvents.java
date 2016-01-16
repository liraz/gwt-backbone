package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsMap;
import org.lirazs.gbackbone.client.core.collection.Collection;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.view.View;

import java.util.HashMap;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 23/10/2015.
 */
public class GwtTestGBackboneEvents extends GWTTestCase {

    public String getModuleName() {
        return "org.lirazs.gbackbone.GBackboneTest";
    }

    public void gwtSetUp() {

    }

    public void gwtTearDown() {

    }

    public void testOnAndTrigger() {
        final int[] counter = {0};
        Events obj = new Events();

        obj.on("event", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        obj.trigger("event");
        assertEquals("counter should be incremented", 1, counter[0]);

        obj.trigger("event");
        obj.trigger("event");
        obj.trigger("event");
        obj.trigger("event");
        assertEquals("counter should be incremented five times", 5, counter[0]);
    }

    public void testBindingAndTriggeringMultipleEvents() {
        final int[] counter = {0};
        Events obj = new Events();

        obj.on("a b c", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        obj.trigger("a");
        assertEquals(1, counter[0]);

        obj.trigger("a b");
        assertEquals(3, counter[0]);

        obj.trigger("c");
        assertEquals(4, counter[0]);

        obj.off("a c");
        obj.trigger("a b c");
        assertEquals(5, counter[0]);
    }


    public void testBindingAndTriggeringWithEventMaps() {
        final int[] counter = {0};
        Events obj = new Events();

        final Function increment = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.on(new HashMap<String, Function>() {
            {
                put("a", increment);
                put("b", increment);
                put("c", increment);
            }
        });

        obj.trigger("a");
        assertEquals(1, counter[0]);

        obj.trigger("a b");
        assertEquals(3, counter[0]);

        obj.trigger("c");
        assertEquals(4, counter[0]);

        obj.off(new HashMap<String, Function>() {
            {
                put("a", increment);
                put("c", increment);
            }
        });

        obj.trigger("a b c");
        assertEquals(5, counter[0]);
    }

    public void testBindingAndTriggeringMultipleEventNamesWithEventMaps() {
        final int[] counter = {0};
        Events obj = new Events();

        final Function increment = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.on(new HashMap<String, Function>() {
            {
                put("a b c", increment);
            }
        });

        obj.trigger("a");
        assertEquals(1, counter[0]);

        obj.trigger("a b");
        assertEquals(3, counter[0]);

        obj.trigger("c");
        assertEquals(4, counter[0]);

        obj.off(new HashMap<String, Function>() {
            {
                put("a c", increment);
            }
        });

        obj.trigger("a b c");
        assertEquals(5, counter[0]);
    }


    public void testListenToAndStopListening() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        a.listenTo(b, "all", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        b.trigger("anything");
        a.listenTo(b, "all", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        a.stopListening();
        b.trigger("anything");

        assertEquals(1, counter[0]);
    }


    public void testListenToAndStopListeningWithEventMaps() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function cb = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenTo(b, new HashMap<String, Function>(){
            {
                put("event", cb);
            }
        });
        b.trigger("event");
        assertEquals(1, counter[0]);

        a.listenTo(b, new HashMap<String, Function>(){
            {
                put("event2", cb);
            }
        });
        b.on("event2", cb);

        a.stopListening(b, new HashMap<String, Function>(){
            {
                put("event2", cb);
            }
        });
        b.trigger("event event2");
        assertEquals(3, counter[0]);

        a.stopListening();
        b.trigger("event event2");

        assertEquals(4, counter[0]);
    }


    public void testStopListeningWithOmittedArgs() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function cb = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenTo(b, "event", cb);
        b.on("event", cb);
        a.listenTo(b, "event2", cb);
        a.stopListening(null, new HashMap<String, Function>(){
            {
                put("event", cb);
            }
        });

        b.trigger("event event2");
        b.off();

        a.listenTo(b, "event event2", cb);
        a.stopListening(null, new HashMap<String, Function>(){
            {
                put("event", cb);
            }
        });
        a.stopListening();
        b.trigger("event2");

        assertEquals(2, counter[0]);
    }


    public void testListenToOnce() {
        final int[] counterA = {0};
        final int[] counterB = {0};

        final Events obj = new Events();

        Function incrA = new Function() {
            @Override
            public void f() {
                counterA[0]++;
                obj.trigger("event");
            }
        };
        Function incrB = new Function() {
            @Override
            public void f() {
                counterB[0]++;
            }
        };
        obj.listenToOnce(obj, "event", incrA);
        obj.listenToOnce(obj, "event", incrB);
        obj.trigger("event");

        assertEquals("counterA should have only been incremented once.", 1, counterA[0]);
        assertEquals("counterB should have only been incremented once.", 1, counterB[0]);
    }

    public void testListenToOnceAndStopListening() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function cb = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenToOnce(b, "all", cb);
        b.trigger("anything");
        b.trigger("anything");
        a.listenToOnce(b, "all", cb);
        a.stopListening();
        b.trigger("anything");

        assertEquals(1, counter[0]);
    }

    public void testListenToListenToOnceAndStopListening() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function cb = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenToOnce(b, "all", cb);
        b.trigger("anything");
        b.trigger("anything");
        a.listenTo(b, "all", cb);
        a.stopListening();
        b.trigger("anything");

        assertEquals(1, counter[0]);
    }

    public void testListenToAndStopListeningWithEventMaps2() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function cb = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenTo(b, new HashMap<String, Function>(){
            {
                put("change", cb);
            }
        });
        b.trigger("change");
        assertEquals(1, counter[0]);

        a.listenTo(b, new HashMap<String, Function>(){
            {
                put("change", cb);
            }
        });
        a.stopListening();
        b.trigger("change");

        assertEquals(1, counter[0]);
    }

    public void testListenToYourself() {
        final int[] counter = {0};
        Events e = new Events();

        e.listenTo(e, "foo", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        e.trigger("foo");
        assertEquals(1, counter[0]);
    }

    public void testListenToYourselfCleansYourselfUpWithStopListening() {
        final int[] counter = {0};
        Events e = new Events();

        e.listenTo(e, "foo", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });
        e.trigger("foo");
        e.stopListening();
        e.trigger("foo");

        assertEquals(1, counter[0]);
    }

    public void testStopListeningCleansUpReferences() {
        Events a = new Events();
        Events b = new Events();

        Function fn = new Function() {
            @Override
            public void f() {

            }
        };

        b.on("event", fn);
        a.listenTo(b, "event", fn).stopListening();
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());

        a.listenTo(b, "event", fn).stopListening(b);
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());

        a.listenTo(b, "event", fn).stopListening(b, "event");
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());

        a.listenTo(b, "event", fn).stopListening(b, "event", fn);
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());
    }

    public void testStopListeningCleansUpReferencesFromListenToOnce() {
        Events a = new Events();
        Events b = new Events();

        Function fn = new Function() {
            @Override
            public void f() {

            }
        };

        b.on("event", fn);
        a.listenToOnce(b, "event", fn).stopListening();
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());

        a.listenToOnce(b, "event", fn).stopListening(b);
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());

        a.listenToOnce(b, "event", fn).stopListening(b, "event");
        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());

        a.listenToOnce(b, "event", fn);
        a.stopListening(b, "event", fn);

        assertEquals(0, a.getListeningToCount());
        assertEquals(1, b.getEventCount("event"));
        assertEquals(0, b.getListenersCount());
    }

    public void testListenToAndOffCleansUpReferences() {
        Events a = new Events();
        Events b = new Events();

        Function fn = new Function() {
            @Override
            public void f() {

            }
        };

        a.listenTo(b, "event", fn);
        b.off();
        assertEquals(0, a.getListeningToCount());
        assertEquals(0, b.getListenersCount());

        a.listenTo(b, "event", fn);
        b.off("event");
        assertEquals(0, a.getListeningToCount());
        assertEquals(0, b.getListenersCount());

        a.listenTo(b, "event", fn);
        b.off(fn);
        assertEquals(0, a.getListeningToCount());
        assertEquals(0, b.getListenersCount());

        a.listenTo(b, "event", fn);
        b.off(a);
        assertEquals(0, a.getListeningToCount());
        assertEquals(0, b.getListenersCount());
    }

    public void testListenToAndStopListeningCleansUpReferences() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenTo(b, "all", fn);
        b.trigger("anything");
        a.listenTo(b, "other", fn);
        a.stopListening(b, "other");
        a.stopListening(b, "all");

        assertEquals(0, a.getListeningToCount());

        assertEquals(1, counter[0]);
    }

    public void testListenToOnceWithoutContextCleansUpReferencesAfterTheEventHasFired() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenToOnce(b, "all", fn);
        b.trigger("anything");

        assertEquals(0, a.getListeningToCount());
        assertEquals(1, counter[0]);
    }

    public void testListenToOnceWithEventMapsCleansUpReferences() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.listenToOnce(b, new HashMap<String, Function>() {
            {
                put("one", fn);
                put("two", fn);
            }
        });
        b.trigger("one");

        assertEquals(1, a.getListeningToCount());
        assertEquals(1, counter[0]);
    }

    public void testListenToWithEmptyCallbackDoesntThrowAnError() {
        Events e = new Events();

        e.listenTo(e, "foo", null);
        e.trigger("foo");
    }

    public void testTriggerAllForEachEvent() {
        final int[] counter = {0};
        Events obj = new Events();

        final boolean[] a = {false};
        final boolean[] b = {false};

        obj.on("all", new Function() {
            @Override
            public void f() {
                String event = getArgument(0);

                counter[0]++;
                if(event.equals("a")) a[0] = true;
                if(event.equals("b")) b[0] = true;
            }
        })
        .trigger("a b");

        assertTrue(a[0]);
        assertTrue(b[0]);

        assertEquals(2, counter[0]);
    }


    public void testOnThenUnbindAllFunctions() {
        final int[] counter = {0};
        Events obj = new Events();

        Function callback = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        obj.on("event", callback);
        obj.trigger("event");
        obj.off("event");
        obj.trigger("event");

        assertEquals("counter should have only been incremented once.", 1, counter[0]);
    }


    public void testBindTwoCallbacksUnbindOnlyOne() {
        final int[] counterA = {0};
        final int[] counterB = {0};
        Events obj = new Events();

        Function callback = new Function() {
            @Override
            public void f() {
                counterA[0]++;
            }
        };
        obj.on("event", callback);
        obj.on("event", new Function() {
            @Override
            public void f() {
                counterB[0]++;
            }
        });
        obj.trigger("event");
        obj.off("event", callback);
        obj.trigger("event");

        assertEquals("counterA should have only been incremented once.", 1, counterA[0]);
        assertEquals("counterB should have been incremented twice.", 2, counterB[0]);
    }


    public void testUnbindACallbackInTheMidstOfItFiring() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function callback = new Function() {
            @Override
            public void f() {
                counter[0]++;
                obj.off("event", this);
            }
        };
        obj.on("event", callback);
        obj.trigger("event");
        obj.trigger("event");
        obj.trigger("event");

        assertEquals("the callback should have been unbound.", 1, counter[0]);
    }


    public void testTwoBindsThatUnbindThemselves() {
        final int[] counterA = {0};
        final int[] counterB = {0};
        final Events obj = new Events();

        final Function incrA = new Function() {
            @Override
            public void f() {
                counterA[0]++;
                obj.off("event", this);
            }
        };
        Function incrB = new Function() {
            @Override
            public void f() {
                counterB[0]++;
                obj.off("event", this);
            }
        };
        obj.on("event", incrA);
        obj.on("event", incrB);

        obj.trigger("event");
        obj.trigger("event");
        obj.trigger("event");

        assertEquals("counterA should have only been incremented once.", 1, counterA[0]);
        assertEquals("counterB should have only been incremented once.", 1, counterB[0]);
    }


    public void testNestedTriggerWithUnbind() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function incr1 = new Function() {
            @Override
            public void f() {
                counter[0]++;
                obj.off("event", this);
                obj.trigger("event");
            }
        };
        Function incr2 = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        obj.on("event", incr1);
        obj.on("event", incr2);

        obj.trigger("event");

        assertEquals("counter should have been incremented three times.", 3, counter[0]);
    }

    public void testCallbackListIsNotAlteredDuringTrigger() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function incr = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        Function incrOn = new Function() {
            @Override
            public void f() {
                obj.on("event all", incr);
            }
        };
        Function incrOff = new Function() {
            @Override
            public void f() {
                obj.off("event all", incr);
            }
        };
        obj.on("event all", incrOn).trigger("event");
        assertEquals("on does not alter callback list", 0, counter[0]);

        obj.off().on("event", incrOff).on("event all", incr).trigger("event");
        assertEquals("off does not alter callback list", 2, counter[0]);
    }

    public void testAllCallbackListIsRetrievedAfterEachEvent() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function incr = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.on("x", new Function() {
            @Override
            public void f() {
                obj.on("y", incr).on("all", incr);
            }
        }).trigger("x y");
        assertEquals(2, counter[0]);
    }

    public void testIfNoCallbacksProvidedOnIsANoop() {
        final Events obj = new Events();
        obj.on("test", null).trigger("test");
    }


    public void testRemoveAllEventsForASpecificContext() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function incr = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.on("x y all", incr);
        obj.on("x y all", incr, obj);
        obj.off(null, null, obj);
        obj.trigger("x y");

        assertEquals(4, counter[0]);
    }

    public void testRemoveAllEventsForASpecificCallback() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function success = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        final Function fail = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.on("x y all", success);
        obj.on("x y all", fail);
        obj.off(fail);
        obj.trigger("x y");

        assertEquals(4, counter[0]);
    }

    public void testOffDoesNotSkipConsecutiveEvents() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function incr1 = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };
        final Function incr2 = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.on("event", incr1, obj);
        obj.on("event", incr2, obj);
        obj.off(null, null, obj);
        obj.trigger("event");

        assertEquals(0, counter[0]);
    }

    public void testOnce() {
        final int[] counterA = {0};
        final int[] counterB = {0};
        final Events obj = new Events();

        final Function incrA = new Function() {
            @Override
            public void f() {
                counterA[0]++;
                obj.trigger("event");
            }
        };
        Function incrB = new Function() {
            @Override
            public void f() {
                counterB[0]++;
            }
        };
        obj.once("event", incrA);
        obj.once("event", incrB);

        obj.trigger("event");

        assertEquals("counterA should have only been incremented once.", 1, counterA[0]);
        assertEquals("counterB should have only been incremented once.", 1, counterB[0]);
    }


    public void testOnceVariantOne() {
        final int[] counter = {0};

        Events a = new Events();
        Events b = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        a.once("event", fn);
        b.on("event", fn);

        a.trigger("event");

        b.trigger("event");
        b.trigger("event");

        assertEquals(3, counter[0]);
    }

    public void testOnceWithOff() {
        final int[] counter = {0};

        Events obj = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.once("event", fn);
        obj.off("event", fn);

        obj.trigger("event");

        assertEquals(0, counter[0]);
    }

    public void testOnceWithEventMaps() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function increment = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.once(new HashMap<String, Function>(){
            {
                put("a", increment);
                put("b", increment);
                put("c", increment);
            }
        }, obj);

        obj.trigger("a");
        assertEquals(1, counter[0]);

        obj.trigger("a b");
        assertEquals(2, counter[0]);

        obj.trigger("c");
        assertEquals(3, counter[0]);

        obj.trigger("a b c");
        assertEquals(3, counter[0]);
    }

    public void testOnceWithOffOnlyByContext() {
        final int[] counter = {0};

        Object context = new Object();
        Events obj = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.once("event", fn, context);
        obj.off(context);

        obj.trigger("event");

        assertEquals(0, counter[0]);
    }

    public void testOnceWithMultipleEvents() {
        final int[] counter = {0};
        Events obj = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        obj.once("x y", fn);
        obj.trigger("x y");

        assertEquals(2, counter[0]);
    }

    public void testOffDuringIterationWithOnce() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                obj.off("event", this);
            }
        };

        obj.on("event", fn);
        obj.once("event", new Function() {
            @Override
            public void f() {
            }
        });
        obj.on("event", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        obj.trigger("event");
        obj.trigger("event");

        assertEquals(2, counter[0]);
    }

    public void testOnceOnAllShouldWorkAsExpected() {
        final int[] counter = {0};
        final Events obj = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
                obj.trigger("all");
            }
        };

        obj.once("all", fn);
        obj.trigger("all");

        assertEquals(1, counter[0]);
    }

    public void testOnceWithoutACallbackIsANoop() {
        final Events obj = new Events();
        obj.once("event", null).trigger("event");
    }

    public void testListenToOnceWithoutACallbackIsANoop() {
        final Events obj = new Events();
        obj.listenToOnce(obj, "event", null).trigger("event");
    }

    public void testEventFunctionAreChainable() {
        Events obj = new Events();
        Events obj2 = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
            }
        };

        assertEquals(obj, obj.trigger("noeventssetyet"));
        assertEquals(obj, obj.off("noeventssetyet"));
        assertEquals(obj, obj.stopListening("noeventssetyet"));
        assertEquals(obj, obj.on("a", fn));
        assertEquals(obj, obj.once("c", fn));
        assertEquals(obj, obj.trigger("a"));
        assertEquals(obj, obj.listenTo(obj2, "a", fn));
        assertEquals(obj, obj.listenToOnce(obj2, "a", fn));
        assertEquals(obj, obj.off("a c"));
        assertEquals(obj, obj.stopListening(obj2, "a"));
        assertEquals(obj, obj.stopListening());
    }


    public void testListenToOnceWithSpaceSeparatedEvents() {
        final int[] counter = {0};

        Events one = new Events();
        Events two = new Events();

        final Function fn = new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        };

        one.listenToOnce(two, "x y", fn);

        two.trigger("x");
        two.trigger("x");
        two.trigger("y");
        two.trigger("y");

        assertEquals(2, counter[0]);
    }
}
