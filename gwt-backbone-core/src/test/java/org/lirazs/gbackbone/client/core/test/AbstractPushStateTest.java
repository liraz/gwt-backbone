package org.lirazs.gbackbone.client.core.test;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.History;
import de.barop.gwt.client.HistoryConverter;
import de.barop.gwt.client.HistoryConverterPushState;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import java.util.Stack;

/**
 * Base class for GWT unit tests with mocked pushState support.
 *
 * <p>
 * The embedded HtmlUnit FF3 browser does not have pushState support. In order to unit test it
 * easily the pushState API is mocked.
 * </p>
 *
 * @author <a href="mailto:jb@barop.de">Johannes Barop</a>
 *
 */
public abstract class AbstractPushStateTest extends GWTTestCase {

    /**
     * A pushed history state.
     */
    static class State {

        JavaScriptObject data;
        String title;
        String url;

        State(final JavaScriptObject data, final String title, final String url) {
            this.data = data;
            this.title = title;
            this.url = url;
        }

        @Override
        public String toString() {
            return "State [data=" + data + ", title=" + title + ", url=" + url + "]";
        }

    }

    /**
     * The pushState implementation which gets exported to JavaScript.
     */
    public static class PushStateApi implements Exportable {

        @Export("$wnd.history.pushState")
        public static void pushState(final JavaScriptObject data, final String title, final String url) {
            states.push(new State(data, title, url));
        }
        @Export("$wnd.history.replaceState")
        public static void replaceState(final JavaScriptObject data, final String title, final String url) {
            if (states.size() > 0) {
                State current = states.peek();

                current.data = data;
                current.title = title;
                current.url = url;
            } else {
                pushState(data, title, url);
            }
        }
    }

    /**
     * All pushed states.
     */
    protected static Stack<State> states;

    /**
     * The number of the items in {@link #states} at test start.
     */
    protected int statesOnTestStart;

    @Override
    public String getModuleName() {
        return "de.barop.gwt.PushStateTest";
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();

        if (states == null) {
            ExporterUtil.exportAll();
            states = new Stack<State>();
            GWT.create(History.class);
        }

        statesOnTestStart = states.size();
    }

    /**
     * Set's the hash part of the browsers address bar.
     */
    protected native void setHash(String hash) /*-{
        $wnd.location.hash = hash;
    }-*/;

    /**
     * Pop the previous history state.
     */
    protected void popState() {
        assert states.size() > 1;
        states.pop(); // go back
        callOnPopState(states.peek().data); // pop the current state
    }

    /**
     * Native JavaScript which calls the onpopstate handler with the given state.
     */
    private native void callOnPopState(JavaScriptObject data) /*-{
        var event = {
            state : data
        };

        $wnd.onpopstate(event);
    }-*/;

    /**
     * Assert that the test runs with pushState support.
     */
    public void testNoPushStateSupport() {
        assertTrue(GWT.create(HistoryConverter.class) instanceof HistoryConverterPushState);
    }

}
