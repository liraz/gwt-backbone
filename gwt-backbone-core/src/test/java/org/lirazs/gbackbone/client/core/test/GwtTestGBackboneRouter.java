package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.navigation.History;
import org.lirazs.gbackbone.client.core.navigation.Router;
import org.lirazs.gbackbone.client.core.navigation.function.OnRouteFunction;
import org.lirazs.gbackbone.client.core.test.router.AnnotatedTestRouter;
import org.lirazs.gbackbone.client.core.test.router.TestRouter;
import org.lirazs.gbackbone.client.core.test.router.WindowLocationEmulation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 23/10/2015.
 */
public class GwtTestGBackboneRouter extends AbstractPushStateTest {

    public String getModuleName() {
        return "org.lirazs.gbackbone.GBackboneTest";
    }

    private WindowLocationEmulation location;
    private TestRouter router;

    private String lastRoute;
    private String[] lastArgs;
    private int onRouteCount;

    private OnRouteFunction onRoute = new OnRouteFunction() {
        @Override
        public void f(Router router, String route, String[] args) {
            lastRoute = route;
            lastArgs = args;

            onRouteCount++;
        }
    };

    public void gwtSetUp() throws Exception {
        super.gwtSetUp();

        // make sure push support is on
        testNoPushStateSupport();

        location = new WindowLocationEmulation("http://example.com");

        History.reset();
        History.get().registerLocationImpl(location);
        router = new TestRouter(O("testing", 101));

        History.get().setInterval(9);
        History.get().start(O(
                "pushState", false
        ));

        lastRoute = null;
        lastArgs = new String[0];

        History.get().onRoute(onRoute);
    }

    public void gwtTearDown() {
        History.get().stop();
        History.get().off();

        onRouteCount = 0;
    }

    public void testInitialize() {
        assertEquals(101, router.getTesting());
    }

    public void testRoutesSimple() {
        location.replace("http://example.com#search/news");
        History.get().checkUrl();

        assertEquals("news", router.getQuery());
        assertEquals(null, router.getPage());
        assertEquals("search", lastRoute);
        assertEquals("news", lastArgs[0]);
    }

    public void testRoutesSimpleButUnicode() {
        location.replace("http://example.com#search/тест");
        History.get().checkUrl();

        assertEquals("тест", router.getQuery());
        assertEquals(null, router.getPage());
        assertEquals("search", lastRoute);
        assertEquals("тест", lastArgs[0]);
    }

    public void testRoutesTwoPart() {
        location.replace("http://example.com#search/nyc/p10");
        History.get().checkUrl();

        assertEquals("nyc", router.getQuery());
        assertEquals("10", router.getPage());
    }

    public void testRoutesViaNavigate() {
        History.get().navigate("search/manhattan/p20", O("trigger", true));

        assertEquals("manhattan", router.getQuery());
        assertEquals("20", router.getPage());
    }

    public void testRoutesViaNavigateWithParams() {
        History.get().navigate("query/test?a=b", O("trigger", true));

        assertEquals("a=b", router.getQueryArgs());
    }

    public void testRoutesViaNavigateForBackwardsCompatibility() {
        History.get().navigate("search/manhattan/p20", true);

        assertEquals("manhattan", router.getQuery());
        assertEquals("20", router.getPage());
    }

    public void testReportsMatchedRouteViaNavigate() {
        assertTrue(History.get().navigate("search/manhattan/p20", true));
    }

    public void testRoutePrecedenceViaNavigate() {
        History.get().navigate("contacts", true);
        assertEquals("index", router.getContact());
        History.get().navigate("contacts", O("trigger", true));
        assertEquals("index", router.getContact());

        History.get().navigate("contacts/new", true);
        assertEquals("new", router.getContact());
        History.get().navigate("contacts/new", O("trigger", true));
        assertEquals("new", router.getContact());

        History.get().navigate("contacts/foo", true);
        assertEquals("load", router.getContact());
        History.get().navigate("contacts/foo", O("trigger", true));
        assertEquals("load", router.getContact());
    }

    public void testLoadUrlIsNotCalledForIdenticalRoutes() {
        location.replace("http://example.com#route");

        History.get().navigate("route");
        History.get().navigate("/route");
        History.get().navigate("/route");

        assertEquals(0, onRouteCount);
    }

    public void testUseImplicitCallbackIfNoneProvided() {
        router.navigate("implicit", O("trigger", true));
        assertEquals(1, router.getCount());
    }

    public void testRoutesViaNavigateWithReplaceTrue() {
        final int[] counter = {0};

        location.replace("http://example.com#start_here");
        History.get().checkUrl();

        location = new WindowLocationEmulation("http://example.com#start_here") {
            @Override
            public void replace(String href) {
                super.replace(href);

                if (!href.equals("http://example.com#start_here")) {
                    assertEquals(new WindowLocationEmulation("http://example.com#end_here").getHref(), href);
                    counter[0]++;
                }
            }
        };
        History.get().registerLocationImpl(location);

        History.get().navigate("end_here", O("replace", true));
        assertEquals(1, counter[0]);
    }

    public void testRoutesSplats() {
        location.replace("http://example.com#splat/long-list/of/splatted_99args/end");
        History.get().checkUrl();

        assertEquals("long-list/of/splatted_99args", router.getArgs());
    }

    public void testRoutesGithub() {
        location.replace("http://example.com#backbone/compare/1.0...braddunbar:with/slash");
        History.get().checkUrl();

        assertEquals("backbone", router.getRepo());
        assertEquals("1.0", router.getFrom());
        assertEquals("braddunbar:with/slash", router.getTo());
    }

    public void testRoutesOptional() {
        location.replace("http://example.com#optional");
        History.get().checkUrl();

        assertNull(router.getArg());
        location.replace("http://example.com#optional/thing");
        History.get().checkUrl();

        assertEquals("thing", router.getArg());
    }

    public void testRoutesComplex() {
        location.replace("http://example.com#one/two/three/complex-part/four/five/six/seven");
        History.get().checkUrl();

        assertEquals("one/two/three", router.getFirst());
        assertEquals("part", router.getPart());
        assertEquals("four/five/six/seven", router.getRest());
    }

    public void testRoutesQuery() {
        location.replace("http://example.com#query/mandel?a=b&c=d");
        History.get().checkUrl();

        assertEquals("mandel", router.getEntity());
        assertEquals("a=b&c=d", router.getQueryArgs());
        assertEquals("query", lastRoute);
        assertEquals("mandel", lastArgs[0]);
        assertEquals("a=b&c=d", lastArgs[1]);
    }

    public void testRoutesAnything() {
        location.replace("http://example.com#doesnt-match-a-route");
        History.get().checkUrl();

        assertEquals("doesnt-match-a-route", router.getAnything());
    }

    public void testRoutesFunction() {
        router.on("route", new Function() {
            @Override
            public void f() {
                String name = getArgument(0);
                assertEquals("", name);
            }
        });
        assertEquals("unset", router.getValue());

        location.replace("http://example.com#function/set");
        History.get().checkUrl();

        assertEquals("set", router.getValue());
    }

    public void testDecodeNamedParametersNotSplats() {
        location.replace("http://example.com#decode/a%2Fb/c%2Fd/e");
        History.get().checkUrl();

        assertEquals("a/b", router.getNamed());
        assertEquals("c/d/e", router.getPath());
    }

    public void testFiresEventWhenRouterDoesntHaveCallbackOnIt() {
        final int[] counter = {0};

        router.on("route:noCallback", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        location.replace("http://example.com#noCallback");
        History.get().checkUrl();

        assertEquals(1, counter[0]);
    }

    public void testNoEventsAreTriggeredIfExecuteReturnsFalse() {
        final int[] counter = {0};

        class CustomRouter extends Router {
            @Override
            protected LinkedHashMap<String, ?> routes() {
                LinkedHashMap<String, Object> routes = new LinkedHashMap<String, Object>();
                routes.put("foo", new Function() {
                    @Override
                    public void f() {
                        counter[0]++;
                    }
                });
                return routes;
            }

            @Override
            protected boolean execute(Function callback, String[] args, String name) {
                callback.f(args);
                return false;
            }
        }
        Router customeRouter = new CustomRouter();
        customeRouter.on("route route:foo", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        History.get().on("route", new Function() {
            @Override
            public void f() {
                counter[0]++;
            }
        });

        location.replace("http://example.com#foo");
        History.get().checkUrl();

        assertEquals(1, counter[0]);
    }

    public void testLeadingSlash() {
        location.replace("http://example.com/root/foo");

        History.get().stop();
        History.get().registerLocationImpl(location);
        History.get().start(O(
                "root", "/root",
                "hashChange", false,
                "silent", true
        ));

        assertEquals("foo", History.get().getFragment());

        History.get().stop();
        History.get().registerLocationImpl(location);
        History.get().start(O(
                "root", "/root/",
                "hashChange", false,
                "silent", true
        ));

        assertEquals("foo", History.get().getFragment());
    }


    public void testRouteCallbackGetsPassedEncodedValues() {
        String route = "has%2Fslash/complex-has%23hash/has%20space";
        History.get().navigate(route, O("trigger", true));

        assertEquals("has/slash", router.getFirst());
        assertEquals("has#hash", router.getPart());
        assertEquals("has space", router.getRest());
    }


    public void testCorrectlyHandlerURLWithPercent() {

        location.replace("http://example.com#search/fat%3A1.5%25");
        History.get().checkUrl();
        location.replace("http://example.com#search/fat");
        History.get().checkUrl();

        assertEquals("fat", router.getQuery());
        assertEquals(null, router.getPage());
        assertEquals("search", lastRoute);
    }

    public void testHashesWithUTF8InThem() {
        History.get().navigate("charñ", O("trigger", true));
        assertEquals("UTF", router.getCharType());
        History.get().navigate("char%C3%B1", O("trigger", true));

        assertEquals("UTF", router.getCharType());
    }

    public void testUsePathnameWhenHashChangeIsNotWanted() {
        History.get().stop();
        location.replace("http://example.com/path/name#hash");

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "hashChange", false
        ));

        String fragment = History.get().getFragment();
        assertEquals(locationPathReplace(location.getPath()), fragment);
    }

    private native String locationPathReplace(String path) /*-{
        return path.replace(/^\//, '');
    }-*/;

    public void testStripLeadingSlashBeforeLocationAssign() {
        History.get().stop();
        location.replace("http://example.com/root/");

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "hashChange", false,
                "root", "/root/"
        ));

        History.get().navigate("/fragment");
        assertEquals("/root/fragment", location.getLastLocationAssign());
    }

    public void testRootFragmentWithoutTrailingSlash() {
        History.get().stop();
        location.replace("http://example.com/root");

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "root", "/root/",
                "hashChange", true
        ));

        assertEquals("", History.get().getFragment());
    }

    public void testHistoryDoesNotPrependRootToFragment() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        location.replace("http://example.com/root/");

        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/root/x", route);
                counter[0]++;
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "root", "/root/",
                "pushState", true,
                "hashChange", false
        ));
        history.navigate("x");

        assertEquals("x", history.getLastSavedFragment());
        assertEquals(1, counter[0]);
    }

    public void testNormalizeRoot() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        History.get().stop();
        location.replace("http://example.com/root");

        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/root/fragment", route);
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "pushState", true,
                "root", "/root",
                "hashChange", false
        ));
        history.navigate("fragment");
    }

    public void testNormalizeRoot2() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        History.get().stop();
        location.replace("http://example.com/root#fragment");

        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/root/fragment", route);
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "pushState", true,
                "root", "/root"
        ));
    }

    public void testNormalizeRootLeadingSlash() {
        History.get().stop();
        location.replace("http://example.com/root");

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "root", "root"
        ));

        assertEquals("/root/", History.get().getRoot());
    }

    public void testTransitionFromHashChangeToPushState() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        History.get().stop();
        location.replace("http://example.com/root#x/y");

        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/root/x/y", route);
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "root", "root",
                "pushState", true
        ));
    }

    public void testNormalizeEmptyRoot() {
        History.get().stop();
        location.replace("http://example.com/");

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "root", ""
        ));

        assertEquals("/", History.get().getRoot());
    }

    public void testNavigateWithEmptyRoot() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        History.get().stop();
        location.replace("http://example.com/");

        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/fragment", route);
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "root", "",
                "pushState", true,
                "hashChange", false
        ));

        history.navigate("fragment");
    }

    public void testTransitionFromPushStateToHashChange() {
        History.get().stop();
        location.replace("http://example.com/root/x/y?a=b");

        History history = new History() {
            @Override
            protected boolean isPushStateSupported() {
                return false;
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "root", "root",
                "pushState", true,
                "hashChange", true
        ));

        assertEquals("/root#x/y?a=b", location.getHref());
    }

    public void testHashChangeToPushStateWithSearch() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        location.replace("http://example.com/root#x/y?a=b");

        History history = new History() {

            @Override
            protected void replaceHistoryState(String title, String route) {
                super.replaceHistoryState(title, route);
                assertEquals("/root/x/y?a=b", route);

                counter[0]++;
            }

            @Override
            protected void pushHistoryState(String title, String route) {}
        };
        history.registerLocationImpl(location);
        history.start(O(
                "root", "root",
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testRouterAllowsEmptyRoute() {
        new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("", "empty");
                    }
                };
            }

            public void empty() {

            }

            @Override
            protected Router route(String route, String name) {
                assertEquals("", route);
                return super.route(route, name);
            }
        };
    }

    public void testTrailingSpaceInFragments() {
        History history = new History();
        assertEquals("fragment", history.getFragment("fragment   "));
    }

    public void testLeadingSlashAndTrailingSpace() {
        History history = new History();
        assertEquals("fragment", history.getFragment("/fragment "));
    }

    public void testOptionalParameters() {

        location.replace("http://example.com#named/optional/y");
        History.get().checkUrl();

        assertEquals(null, router.getZ());

        location.replace("'http://example.com#named/optional/y123");
        History.get().checkUrl();

        assertEquals("123", router.getZ());
    }

    public void testTriggerRouteEventOnRouterInstance() {
        final int[] counter = {0};

        router.on("route", new Function() {
            @Override
            public void f() {
                String name = getArgument(0);
                String[] args = getArgument(1);

                assertEquals("routeEvent", name);
                assertEquals(Arrays.asList("x"), Arrays.asList(args));

                counter[0]++;
            }
        });

        location.replace("http://example.com#route-event/x");
        History.get().checkUrl();

        assertEquals(1, counter[0]);
    }


    public void testHashChangeToPushStateOnlyIfBothRequested() {
        final int[] counter = {0};

        History history = new History() {
            @Override
            protected void replaceHistoryState(String title, String route) {
                counter[0]++;
            }
        };
        history.stop();

        location.replace("http://example.com/root?a=b#x/y");

        history.registerLocationImpl(location);
        history.start(O(
                "root", "root",
                "pushState", true,
                "hashChange", false
        ));

        assertEquals(0, counter[0]);
    }


    public void testNoHashFallback() {
        final int[] counter = {0};

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("hash", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        location.replace("http://example.com/");
        History.get().start(O(
                "pushState", true,
                "hashChange", false
        ));

        location.replace("http://example.com/nomatch#hash");
        History.get().checkUrl();

        assertEquals(0, counter[0]);
    }

    public void testNoTrailingSlashOnRoot() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/root", route);
                counter[0]++;
            }
        };
        history.registerLocationImpl(location);

        location.replace("http://example.com/root/path");

        history.start(O(
                "pushState", true,
                "hashChange", false,
                "root", "root"
        ));
        history.navigate("");

        assertEquals(1, counter[0]);
    }

    public void testNoTrailingSlashOnRoot2() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/", route);
                counter[0]++;
            }
        };
        history.registerLocationImpl(location);

        location.replace("http://example.com/path");
        history.start(O(
                "pushState", true,
                "hashChange", false
        ));

        history.navigate("");
        assertEquals(1, counter[0]);
    }

    public void testNoTrailingSlashOnRoot3() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                assertEquals("/root?x=1", route);
                counter[0]++;
            }
        };
        history.registerLocationImpl(location);

        location.replace("http://example.com/root/path");
        history.start(O(
                "pushState", true,
                "hashChange", false,
                "root", "root"
        ));

        history.navigate("?x=1");
        assertEquals(1, counter[0]);
    }

    public void testFragmentMatchingSansQueryHash() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("path", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        location.replace("http://example.com/");
        History.get().start(O(
                "pushState", true,
                "hashChange", false
        ));

        History.get().navigate("path?query#hash", true);
        assertEquals(1, counter[0]);
    }

    public void testDoNotDecodeTheSearchParams() {
        final int[] counter = {0};

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("path", new Function() {
                            @Override
                            public void f() {
                                assertEquals("x=y%3Fz", getArgument(0));
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().navigate("path?x=y%3Fz", true);
        assertEquals(1, counter[0]);
    }

    public void testNavigateToAHashUrl() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "pushState", true
        ));

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("path", new Function() {
                            @Override
                            public void f() {
                                assertEquals("x=y", getArgument(0));
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        location.replace("http://example.com/path?x=y#hash");
        History.get().checkUrl();

        assertEquals(1, counter[0]);
    }

    public void testNavigateToAHashUrl2() {
        final int[] counter = {0};

        History.get().stop();

        History.get().registerLocationImpl(location);
        History.get().start(O(
                "pushState", true
        ));

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("path", new Function() {
                            @Override
                            public void f() {
                                assertEquals("x=y", getArgument(0));
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().navigate("path?x=y#hash", true);
        assertEquals(1, counter[0]);
    }

    public void testUnicodePathname() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        location.replace("http://example.com/myyjä");
        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("myyjä", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testUnicodePathnameWithPercentInAParameter() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        location.replace("http://example.com/myyjä/foo%20%25%3F%2f%40%25%20bar");
        location.setPath("/myyj%C3%A4/foo%20%25%3F%2f%40%25%20bar");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("myyjä/:query", new Function() {
                            @Override
                            public void f() {
                                assertEquals("foo %?/@% bar", getArgument(0));
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testNewLineInRoute() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        location.replace("http://example.com/stuff%0Anonsense?param=foo%0Abar");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("stuff\nnonsense", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testRouterExecuteReceivesCallbackArgsName() {
        final int[] counter = {0};

        location.replace("http://example.com#foo/123/bar?x=y");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("foo/:id/bar", "foo");
                    }
                };
            }

            public void foo(String id) {}

            @Override
            protected boolean execute(Function callback, String[] args, String name) {
                assertEquals("foo", name);
                assertEquals(Arrays.asList("123", "x=y"), Arrays.asList(args));
                counter[0]++;

                return true;
            }
        };

        History.get().start();
        assertEquals(1, counter[0]);
    }

    public void testPushStateToHashChangeWithOnlySearchParams() {
        History.get().stop();
        location.replace("http://example.com?a=b");

        History history = new History() {
            @Override
            protected boolean isPushStateSupported() {
                return false;
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "pushState", true
        ));

        assertEquals("/#?a=b", location.getHref());
    }

    public void testHistoryNavigateDecodesBeforeComparison() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        History.get().stop();
        location.replace("http://example.com/shop/search?keyword=short%20dress");

        History history = new History() {
            @Override
            protected void pushHistoryState(String title, String route) {
                counter[0]++;
            }

            @Override
            protected void replaceHistoryState(String title, String route) {
                counter[0]++;
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "pushState", true
        ));
        history.navigate("shop/search?keyword=short%20dress", true);

        assertEquals("shop/search?keyword=short dress", history.getFragment());
        assertEquals(0, counter[0]);
    }

    public void testUrlsInTheParams() {
        final int[] counter = {0};

        History.get().stop();
        location.replace("http://example.com#login?a=value&backUrl=https%3A%2F%2Fwww.msn.com%2Fidp%2Fidpdemo%3Fspid%3Dspdemo%26target%3Db");

        History.get().registerLocationImpl(location);

        Router router = new Router();
        router.route("login", new Function() {
            @Override
            public void f() {
                assertEquals("a=value&backUrl=https%3A%2F%2Fwww.msn.com%2Fidp%2Fidpdemo%3Fspid%3Dspdemo%26target%3Db", getArgument(0));
                counter[0]++;
            }
        });

        History.get().start();
        assertEquals(1, counter[0]);
    }

    public void testPushStateToHashChangeWithSearchParams() {
        History.get().stop();
        location.replace("http://example.com/root?foo=bar");

        History history = new History() {
            @Override
            protected boolean isPushStateSupported() {
                return false;
            }
        };
        history.registerLocationImpl(location);
        history.start(O(
                "root", "/root",
                "pushState", true
        ));

        assertEquals("/root#?foo=bar", location.getHref());
    }

    public void testPathsThatDontMatchTheRootShouldNotMatchNoRoot() {
        final int[] counter = {0};

        location.replace("http://example.com/foo");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("foo", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "root", "root",
                "pushState", true
        ));
        assertEquals(0, counter[0]);
    }

    public void testPathsThatDontMatchTheRootShouldNotMatchRootsOfTheSameLength() {
        final int[] counter = {0};

        location.replace("http://example.com/xxxx/foo");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("foo", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "root", "root",
                "pushState", true
        ));
        assertEquals(0, counter[0]);
    }

    public void testRootsWithRegexCharacters() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        location.replace("http://example.com/x+y.z/foo");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("foo", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "root", "x+y.z",
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testRootsWithUnicodeCharacters() {
        // IE8 & IE9 does not support pushstate tests
        if(GQuery.browser.ie8 || GQuery.browser.ie9)
            return;

        final int[] counter = {0};

        location.replace("http://example.com/®ooτ/foo");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("foo", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "root", "®ooτ",
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testRootsWithoutSlash() {
        final int[] counter = {0};

        location.replace("http://example.com/®ooτ");

        History.get().stop();
        History.get().registerLocationImpl(location);

        Router router = new Router() {
            @Override
            protected Map<String, ?> routes() {
                return new HashMap<String, Object>() {
                    {
                        put("", new Function() {
                            @Override
                            public void f() {
                                counter[0]++;
                            }
                        });
                    }
                };
            }
        };

        History.get().start(O(
                "root", "®ooτ",
                "pushState", true
        ));
        assertEquals(1, counter[0]);
    }

    public void testAnnotatedRouter() {
        AnnotatedTestRouter annotatedRouter = new AnnotatedTestRouter();

        location.replace("http://example.com#search/news");
        History.get().checkUrl();

        assertEquals("news", annotatedRouter.getQuery());
        assertEquals(null, annotatedRouter.getPage());
        assertEquals("search", lastRoute);
        assertEquals("news", lastArgs[0]);

        location.replace("http://example.com#search/тест");
        History.get().checkUrl();

        assertEquals("тест", annotatedRouter.getQuery());
        assertEquals(null, annotatedRouter.getPage());
        assertEquals("search", lastRoute);
        assertEquals("тест", lastArgs[0]);

        location.replace("http://example.com#search/nyc/p10");
        History.get().checkUrl();

        assertEquals("nyc", annotatedRouter.getQuery());
        assertEquals("10", annotatedRouter.getPage());

        History.get().navigate("search/manhattan/p20", O("trigger", true));

        assertEquals("manhattan", annotatedRouter.getQuery());
        assertEquals("20", annotatedRouter.getPage());

        History.get().navigate("query/test?a=b", O("trigger", true));

        assertEquals("a=b", annotatedRouter.getQueryArgs());

        History.get().navigate("contacts", true);
        assertEquals("index", annotatedRouter.getContact());
        History.get().navigate("contacts", O("trigger", true));
        assertEquals("index", annotatedRouter.getContact());

        History.get().navigate("contacts/new", true);
        assertEquals("new", annotatedRouter.getContact());
        History.get().navigate("contacts/new", O("trigger", true));
        assertEquals("new", annotatedRouter.getContact());

        History.get().navigate("contacts/foo", true);
        assertEquals("load", annotatedRouter.getContact());
        History.get().navigate("contacts/foo", O("trigger", true));
        assertEquals("load", annotatedRouter.getContact());
    }
}
