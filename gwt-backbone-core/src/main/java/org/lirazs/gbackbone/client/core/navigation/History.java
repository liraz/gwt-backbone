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
 package org.lirazs.gbackbone.client.core.navigation;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Properties;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.js.JsArray;
import org.lirazs.gbackbone.client.core.navigation.function.OnRouteFunction;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.window;

public class History extends Events<History> {

    // Cached regex for detecting MSIE.
    RegExp isExplorer = RegExp.compile("msie [\\w.]+");

    // Has the history handling already been started?
    static Boolean started = false;

    class HandlerEntry {
        RegExp route;
        Function callback;

        public HandlerEntry(RegExp route, Function callback) {
            this.route = route;
            this.callback = callback;
        }

        public RegExp getRoute() {
            return route;
        }

        public Function getCallback() {
            return callback;
        }
    }

    JsArray<Properties> handlers;

    // The default interval to poll for hash changes, if necessary, is
    // twenty times a second.
    int interval = 50;

    Options options;

    Timer checkUrlInterval;
    boolean wantsHashChange;
    boolean hasHashChange;
    boolean useHashChange;
    boolean hasPushState;
    boolean wantsPushState;
    boolean usePushState;

    String root;
    String fragment;

    IFrameElement iFrame;

    private static History instance = null;
    public static History get() {
        if(instance == null) {
            instance = new History();
        }
        return instance;
    }
    public static void reset() {
        instance = new History();
    }

    private WindowLocation location = new WindowLocationImpl();

    public void registerLocationImpl(WindowLocation location) {
        this.location = location;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Handles cross-browser history management, based on either
     * [pushState](http://diveintohtml5.info/history.html) and real URLs, or
     * [onhashchange](https://developer.mozilla.org/en-US/docs/DOM/window.onhashchange)
     * and URL fragments.
     * If the browser supports neither (old IE, natch), falls back to polling.
     */
    public History() {
        handlers = JsArray.create();
    }

    public String getRoot() {
        return root;
    }

    /**
     * @return Are we at the app root?
     */
    protected boolean atRoot() {
        String path = rootCheck(location.getPath());

        String search = getSearch();
        return path.equals(this.root) && (search == null || search.isEmpty());
    }

    /**
     * @return Does the pathname match the root?
     */
    protected boolean matchRoot() {
        String path = decodeFragment(location.getPath());

        String root = "";
        int endIndex = this.root.length() - 1;
        if(endIndex <= path.length()) {
            root = path.substring(0, endIndex) + "/";
        }
        return root.equals(this.root);
    }

    /**
     * In IE6, the hash fragment and search params are incorrect if the
     * fragment contains `?`.
     *
     * @return
     */
    protected String getSearch() {
        RegExp re = RegExp.compile("\\?.+");
        String href = location.getHref().replaceAll("/#.*/", "");

        MatchResult result = re.exec(href);
        return result != null ? result.getGroup(0) : "";
    }

    /**
     * @see History#getHash(IFrameElement)
     */
    public String getHash() {
        return getHash(null);
    }

    /**
     * Gets the true hash value. Cannot use location.hash directly due to bug
     * in Firefox where location.hash will always be decoded.
     *
     * @param iframe
     * @return
     */
    public String getHash(IFrameElement iframe) {
        RegExp re = RegExp.compile("#(.*)$");
        String href = location.getHref();

        if(iframe != null) {
            href = getIFrameUrl(iframe);
        }

        MatchResult result = re.exec(href);
        return result != null && result.getGroupCount() > 1 ? result.getGroup(1) : "";
    }

    /**
     * Get the pathname and search params, without the root.
     * @return
     */
    protected String getPath() {
        int rootLastIndex = this.root.length() - 1;
        String path = decodeFragment(location.getPath() + getSearch()).substring(rootLastIndex > -1 ? rootLastIndex : 0);
        return !path.isEmpty() && path.charAt(0) == '/' ? path.substring(1) : path;
    }

    /**
     * Get the last fragment string that was saved.
     * @return
     */
    public String getLastSavedFragment() {
        return this.fragment;
    }

    /**
     * @see History#getFragment(String)
     */
    public String getFragment() {
        return getFragment(null);
    }

    /**
     * Get the cross-browser normalized URL fragment from the path or hash.
     *
     * @param fragment
     * @return
     */
    public String getFragment(String fragment) {
        if(fragment == null) {
            if(usePushState || !wantsHashChange) {
                fragment = getPath();
            } else {
                fragment = getHash();
            }
        }
        return routeStripper(fragment);
    }

    /**
     * Starting with default options
     *
     * @see History#start(Options)
     */
    public Boolean start() {
        return start(null);
    }

    /**
     * Start the hash change handling, returning `true` if the current URL matches
     * an existing route, and `false` otherwise.
     *
     * @param options
     * @return if started successfully or not
     */
    public Boolean start(Options options) {
        if (History.started) throw new Error("Backbone.history has already been started");
        History.started = true;

        // Figure out the initial configuration. Do we need an iframe?
        // Is pushState desired ... is it available?
        this.options = new Options("root", "/").extend(this.options).extend(options);
        this.root = this.options.get("root");
        this.wantsHashChange = !this.options.containsKey("hashChange") || this.options.getBoolean("hashChange");
        this.hasHashChange = isOnHashChangeSupported();
        this.useHashChange = wantsHashChange && hasHashChange;
        this.wantsPushState = this.options.getBoolean("pushState");
        this.hasPushState = this.options.getBoolean("pushState") && isPushStateSupported();
        this.usePushState = wantsPushState && hasPushState;
        this.fragment = getFragment();

        // Normalize root to always include a leading and trailing slash.
        this.root = rootStripper("/" + this.root + "/");

        // Transition from hashChange to pushState or vice versa if both are
        // requested.
        if(wantsHashChange && wantsPushState) {

            // If we've started off with a route from a `pushState`-enabled
            // browser, but we're currently in a browser that doesn't support it...
            if(!hasPushState && !atRoot()) {
                String root = this.root.substring(0, this.root.length() - 1);
                if(root.isEmpty())
                    root = "/";

                location.replace(root + "#" + getPath());
                // Return immediately as browser will do redirect to new url
                return true;

            // Or if we've started out with a hash-based route, but we're currently
            // in a browser where it could be `pushState`-based instead...
            } else if(hasPushState && atRoot()) {
                navigate(getHash(), new Options("replace", true));
            }
        }

        // Proxy an iframe to handle location events if the browser doesn't
        // support the `hashchange` event, HTML5 history, or the user wants
        // `hashChange` but not `pushState`.
        if(!hasHashChange && wantsHashChange && !usePushState) {
            this.iFrame = Document.get().createIFrameElement();
            this.iFrame.setSrc("javascript:0");
            this.iFrame.setTabIndex(-1);
            this.iFrame.getStyle().setDisplay(Style.Display.NONE);

            applyFrameInitialHash(iFrame, fragment);
        }

        // Depending on whether we're using pushState or hashes, and whether
        // 'onhashchange' is supported, determine how we check the URL state.
        if(usePushState) {
            $(window).on("popstate", this.checkUrl);
        } else if(useHashChange && iFrame == null) {
            $(window).on("hashchange", this.checkUrl);
        }else if(wantsHashChange) {
            this.checkUrlInterval = new Timer() {
                @Override
                public void run() {
                    checkUrl.f();
                }
            };
            this.checkUrlInterval.scheduleRepeating(this.interval);
        }

        if (!this.options.getBoolean("silent"))
            return loadUrl();

        return false;
    }

    /**
     * Disable Backbone.history, perhaps temporarily. Not useful in a real app,
     * but possibly useful for unit testing Routers.
     */
    public void stop() {
        // Remove window listeners.
        if (usePushState) {
            $(window).off("popstate", this.checkUrl);
        } else if(useHashChange && iFrame == null) {
            $(window).off("hashchange", this.checkUrl);
        }

        // Clean up the iframe if necessary.
        if(iFrame != null) {
            Document.get().getBody().removeChild(iFrame);
            iFrame = null;
        }

        // Some environments will throw when clearing an undefined interval.
        if (checkUrlInterval != null) {
            checkUrlInterval.cancel();
            checkUrlInterval = null;
        }

        History.started = false;
    }

    /**
     *  Add a route to be tested when the fragment changes. Routes added later
     *  may override previous routes.
     *
     * @param route
     * @param callback
     */
    public void route(RegExp route, Function callback) {
        Properties properties = Properties.create();
        properties.set("route", route);
        properties.setFunction("callback", callback);

        this.handlers.unshift(properties);
    }

    public void checkUrl() {
        checkUrl.f();
    }

    /**
     * // Checks the current URL to see if it has changed, and if it has,
     // calls `loadUrl`, normalizing across the hidden iframe.
     checkUrl(e): any {
         var current = this.getFragment();

         if (current === this.fragment && this.iframe) {
            current = this.getFragment(this.getHash(this.iframe));
         }

         if (current === this.fragment) return false;
         if (this.iframe) this.navigate(current);

         this.loadUrl();
     }
     */
    private Function checkUrl = new Function() {
        @Override
        public void f() {
            String current = getFragment();

            // If the user pressed the back button, the iframe's hash will have
            // changed and we should use that for comparison.
            if (current.equals(fragment) && iFrame != null) {
                current = getHash(iFrame);
            }

            if (current.equals(fragment))
                return;
            if (iFrame != null) navigate(current);

            loadUrl();
        }
    };

    /**
     * // Attempt to load the current URL fragment. If a route succeeds with a
     // match, returns `true`. If no defined routes matches the fragment,
     // returns `false`.
     loadUrl(fragmentOverride?: string): boolean {
         var fragment = this.fragment = this.getFragment(fragmentOverride);

         return _.any(this.handlers, function (handler) {
             if (handler.route.test(fragment)) {
                 handler.callback(fragment);
                 return true;
             }
         });
     }
     */
    private boolean loadUrl() {
        return loadUrl(null);
    }
    private boolean loadUrl(String fragmentOverride) {
        // If the root doesn't match, no routes can match either.
        if(!matchRoot()) return false;

        String fragment = this.fragment = this.getFragment(fragmentOverride);

        for (int i = 0; i < handlers.length(); i++) {
            Properties handler = handlers.get(i);
            RegExp route = handler.get("route");
            Function callback = handler.getFunction("callback");

            if (route.test(fragment)) {
                callback.f(fragment);
                return true;
            }
        }

        return false;
    }


    /**
     * @see History#navigate(String, Options)
     */
    public boolean navigate(String fragment) {
        return navigate(fragment, new Options());
    }

    /**
     * instead of: navigate(fragment, true) use navigate(fragment, new Options("trigger",true))
     * @see History#navigate(String, Options)
     */
    public boolean navigate(String fragment, Boolean trigger) {
        return navigate(fragment, new Options("trigger", trigger));
    }

    /**
     *  Save a fragment into the hash history, or replace the URL state if the
     * 'replace' option is passed. You are responsible for properly URL-encoding
     * the fragment in advance.
     *
     * The options object can contain `trigger: true` if you wish to have the
     * route callback be fired (not usually desirable), or `replace: true`, if
     * you wish to modify the current URL without adding an entry to the history.
     *
     * @param fragment
     * @param options
     * @return
     */
    public boolean navigate(String fragment, Options options) {
        if (!History.started) return false;

        // Normalize the fragment.
        fragment = getFragment(fragment != null ? fragment : "");

        // Don't include a trailing slash on the root.
        String root = this.root;
        if(fragment.isEmpty() || fragment.charAt(0) == '?') {
            root = root.substring(0, root.length() - 1);
            if(root.isEmpty())
                root = "/";
        }
        String url = root + fragment;

        // Strip the hash and decode for matching.
        fragment = decodeFragment(pathStripper(fragment));

        if(this.fragment.equals(fragment)) return false;
        this.fragment = fragment;

        // If pushState is available, we use it to set the fragment as a real URL.
        if(hasPushState) {
            if(options.getBoolean("replace")) {
                replaceHistoryState(Document.get().getTitle(), url);
            } else {
                pushHistoryState(Document.get().getTitle(), url);
            }
        // If hash changes haven't been explicitly disabled, update the hash
        // fragment to store history.
        } else if(wantsHashChange) {
            updateHash(fragment, options.getBoolean("replace"));

            if (this.iFrame != null && !fragment.equals(getHash(iFrame))) {

                // Opening and closing the iframe tricks IE7 and earlier to push a
                // history entry on hash-tag change.  When replace is true, we don't
                // want this.
                if (!options.getBoolean("replace")) {
                    openAndCloseIFrameDocument(this.iFrame);
                }
                updateHash(this.iFrame, fragment, options.getBoolean("replace"));
            }

        // If you've told us that you explicitly don't want fallback hashchange-
        // based history, then `navigate` becomes a page refresh.
        } else {
            location.assign(url);
            return true;
        }

        if (options.getBoolean("trigger"))
            return this.loadUrl(fragment);

        return false;
    }

    /**
     * // Update the hash location, either replacing the current entry, or adding
     // a new one to the browser history.
     _updateHash(location: Location, fragment: string, replace: boolean) {
         if (replace) {
             var href = location.href.replace(/(javascript:|#).*$/, '');
             location.replace(href + '#' + fragment);
         } else {
             // Some browsers require that `hash` contains a leading #.
             location.hash = '#' + fragment;
         }
     }
     *
     */
    private void updateHash(String fragment, boolean replace) {
        updateHash(null, fragment, replace);
    }
    private void updateHash(IFrameElement iFrame, String fragment, boolean replace) {
        if(replace) {
            String locationHref = location.getHref();
            if(iFrame != null) {
                locationHref = getIFrameUrl(iFrame);
            }

            String href = locationHref.replaceAll("(javascript:|#).*$", "");

            if(iFrame != null) {
                replaceFrameLocation(iFrame, href + '#' + fragment);
            } else {
                location.replace(href + '#' + fragment);
            }
        } else {
            // Some browsers require that `hash` contains a leading #.
            String hash = '#' + fragment;

            if(iFrame != null) {
                updateFrameLocationHash(iFrame, hash);
            } else {
                com.google.gwt.user.client.History.newItem(hash);
            }
        }
    }

    public History onRoute(OnRouteFunction callback) {
        return on("route", callback);
    }
    public History offRoute(OnRouteFunction callback) {
        return off("route", callback);
    }

    private native String rootCheck(String pathname) /*-{
        return pathname.replace(/[^\/]$/, '$&/');
    }-*/;

    private native String routeStripper(String input) /*-{
        return input.replace(/^[#\/]|\s+$/g, '');
    }-*/;

    private native String pathStripper(String input) /*-{
        return input.replace(/#.*$/, '');
    }-*/;

    private native String rootStripper(String input) /*-{
        return input.replace(/^\/+|\/+$/g, '/');
    }-*/;

    private native void applyFrameInitialHash(IFrameElement frame, String fragment) /*-{
        var body = document.body;

        // Using `appendChild` will throw on IE < 9 if the document is not ready.
        var iWindow = body.insertBefore(frame, body.firstChild).contentWindow;
        iWindow.document.open();
        iWindow.document.close();
        iWindow.location.hash = '#' + fragment;
    }-*/;

    private native void replaceFrameLocation(IFrameElement frame, String s) /*-{
        if(frame.contentWindow) {
            frame.contentWindow.location.replace(s);
        }
    }-*/;

    private native void updateFrameLocationHash(IFrameElement frame, String hash) /*-{
        if(frame.contentWindow) {
            frame.contentWindow.location.hash = hash;
        }
    }-*/;

    private native String openAndCloseIFrameDocument(IFrameElement frame) /*-{
        if(frame.document) {
            frame.document.open().close();
        }
    }-*/;

    private native String getIFrameUrl(IFrameElement frame) /*-{
        if (frame.contentDocument !== undefined) {
            return frame.contentDocument.URL;
        } else if (frame.contentWindow !== undefined &&
            frame.contentWindow.document !== undefined)
        {
            return frame.contentWindow.document;
        } else {
            return null;
        }
    }-*/;

    protected native void replaceHistoryState(String title, String route) /*-{
        $wnd.history.replaceState({}, title, route);
    }-*/;

    protected native void pushHistoryState(String title, String route) /*-{
        $wnd.history.pushState({}, title, route);
    }-*/;

    public native String getLocationSearch() /*-{
        return $wnd.location.search;
    }-*/;

    private native boolean isOnHashChangeSupported() /*-{
        return 'onhashchange' in window && (document.documentMode === void 0 || document.documentMode > 7);
    }-*/;

    protected native boolean isPushStateSupported() /*-{
        return !!($wnd.history && $wnd.history.pushState);
    }-*/;

    /**
     * Unicode characters in `location.pathname` are percent encoded so they're
     * decoded for comparison. `%25` should not be decoded since it may be part
     * of an encoded parameter.
     *
     * @param fragment
     * @return
     */
    private native String decodeFragment(String fragment)/*-{
        return decodeURI(fragment.replace(/%25/g, '%2525'));
    }-*/;
}
