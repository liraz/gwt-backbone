package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.navigation.History;
import org.lirazs.gbackbone.client.core.navigation.Router;
import org.lirazs.gbackbone.client.core.navigation.function.OnRouteFunction;
import org.lirazs.gbackbone.client.core.test.router.WindowLocationEmulation;
import org.lirazs.gbackbone.client.core.test.router.TestRouter;

import static org.lirazs.gbackbone.client.core.data.Options.O;

/**
 * Created on 23/10/2015.
 */
public class GBackboneRouterTestGwt extends GWTTestCase {

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

    public void gwtSetUp() {
        location = new WindowLocationEmulation("http://example.com");

        History.get().registerLocationImpl(location);
        router = new TestRouter(O("testing", 101));

        History.get().setInterval(9);
        History.get().start(O("pushState", false));

        lastRoute = null;
        lastArgs = new String[0];

        History.get().onRoute(onRoute);
    }

    public void gwtTearDown() {
        History.get().stop();
        History.get().offRoute(onRoute);

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
}
