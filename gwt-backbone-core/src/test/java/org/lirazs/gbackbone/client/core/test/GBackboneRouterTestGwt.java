package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.junit.client.GWTTestCase;
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

    class ExternalObject {
        private String value = "unset";

        public void routingFunction(String value) {
            this.value = value;
        }
    }

    private OnRouteFunction onRoute = new OnRouteFunction() {
        @Override
        public void f(Router router, String route, String[] args) {
            lastRoute = route;
            lastArgs = args;
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
}
