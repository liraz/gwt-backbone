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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Properties;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.client.core.js.JsArray;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.window;

public class History extends Events {

    /**
     * // Cached regex for stripping a leading hash/slash and trailing space.
     var routeStripper = /^[#\/]|\s+$/g;

     // Cached regex for stripping leading and trailing slashes.
     var rootStripper = /^\/+|\/+$/g;

     // Cached regex for detecting MSIE.
     var isExplorer = /msie [\w.]+/;

     // Cached regex for removing a trailing slash.
     var trailingSlash = /\/$/;
     */
    // Cached regex for stripping a leading hash/slash and trailing space.
    RegExp routeStripper = RegExp.compile("/^[#\\/]|\\s+$/g");

    // Cached regex for stripping leading and trailing slashes.
    RegExp rootStripper = RegExp.compile("/^\\/+|\\/+$/g");

    // Cached regex for detecting MSIE.
    RegExp isExplorer = RegExp.compile("/msie [\\w.]+/");

    // Cached regex for removing a trailing slash.
    RegExp trailingSlash = RegExp.compile("/\\/$/");

    /**
     * fragment;
     handlers: any[];
     history;
     iframe;
     location;
     options;
     root;

     _checkUrlInterval
     _hasPushState;
     _wantsHashChange;
     _wantsPushState;

     // Has the history handling already been started?
     static started = false;

     // The default interval to poll for hash changes, if necessary, is
     // twenty times a second.
     interval = 50;
     */
    static Boolean started = false;

    JsArray<Properties> handlers;
    int interval = 50;
    Options options;

    Timer checkUrlInterval;
    Boolean hasPushState;
    Boolean wantsHashChange;
    Boolean wantsPushState;

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

    /**
     * constructor() {
         super();
         this.handlers = [];
         _.bindAll(this, 'checkUrl');

         // Ensure that `History` can be used outside of the browser.
         if (typeof window !== 'undefined') {
             this.location = window.location;
             this.history = window.history;
         }
     }
     */
    protected History() {
        handlers = JsArray.create();
    }

    /**
     * // Gets the true hash value. Cannot use location.hash directly due to bug
     // in Firefox where location.hash will always be decoded.
     getHash(window?: Window): string {
         var match = (window || this).location.href.match(/#(.*)$/);
         return match ? match[1] : '';
     }
     */
    public String getHash() {
        return getHash(null);
    }
    public String getHash(IFrameElement iframe) {
        RegExp re = RegExp.compile("/#(.*)$/");
        String href = Window.Location.getHref();

        if(iframe != null) {
            href = getIFrameUrl(iframe);
        }

        MatchResult result = re.exec(href);
        return result != null && result.getGroupCount() > 1 ? result.getGroup(1) : "";
    }

    /**
     * // Get the cross-browser normalized URL fragment, either from the URL,
     // the hash, or the override.
     getFragment(fragment?: string, forcePushState?: boolean): string {
         if (fragment == null) {
             if (this._hasPushState || !this._wantsHashChange || forcePushState) {
                 fragment = this.location.pathname;
                 var root = this.root.replace(trailingSlash, '');
                 if (!fragment.indexOf(root)) fragment = fragment.slice(root.length);
             } else {
                fragment = this.getHash();
             }
         }
         return fragment.replace(routeStripper, '');
     }
     */
    public String getFragment() {
        return getFragment(null, null);
    }
    public String getFragment(String fragment) {
        return getFragment(fragment, false);
    }
    public String getFragment(String fragment, Boolean forcePushState) {
        if(fragment == null) {
            if(hasPushState || !wantsHashChange || forcePushState) {
                fragment = Window.Location.getPath();
                String root = this.root.replaceAll(trailingSlash.getSource(), "");
                if(fragment.contains(root)) fragment = fragment.substring(root.length());
            } else {
                fragment = getHash();
            }
        }
        return fragment.replaceAll(routeStripper.getSource(), "");
    }

    /**
     * // Start the hash change handling, returning `true` if the current URL matches
     // an existing route, and `false` otherwise.
     start(options?: HistoryStartOptions): boolean {
         if (History.started) throw new Error("Backbone.history has already been started");
         History.started = true;

         // Figure out the initial configuration. Do we need an iframe?
         // Is pushState desired ... is it available?
         this.options = _.extend({}, { root: '/' }, this.options, options);
         this.root = this.options.root;
         this._wantsHashChange = this.options.hashChange !== false;
         this._wantsPushState = !!this.options.pushState;
         this._hasPushState = !!(this.options.pushState && this.history && this.history.pushState);
         var fragment = this.getFragment();
         var docMode = document.documentMode;
         var oldIE = (isExplorer.exec(navigator.userAgent.toLowerCase()) && (!docMode || docMode <= 7));

         // Normalize root to always include a leading and trailing slash.
         this.root = ('/' + this.root + '/').replace(rootStripper, '/');

         if (oldIE && this._wantsHashChange) {
         this.iframe = (<any>Backbone.$('<iframe src="javascript:0" tabindex="-1" />').hide().appendTo('body')[0]).contentWindow;
         this.navigate(fragment);
         }

         // Depending on whether we're using pushState or hashes, and whether
         // 'onhashchange' is supported, determine how we check the URL state.
         if (this._hasPushState) {
         Backbone.$(window).on('popstate', this.checkUrl);
         } else if (this._wantsHashChange && ('onhashchange' in window) && !oldIE) {
         Backbone.$(window).on('hashchange', this.checkUrl);
         } else if (this._wantsHashChange) {
         this._checkUrlInterval = setInterval(this.checkUrl, this.interval);
         }

         // Determine if we need to change the base url, for a pushState link
         // opened by a non-pushState browser.
         this.fragment = fragment;
         var loc = this.location;
         var atRoot = loc.pathname.replace(/[^\/]$/, '$&/') === this.root;

         // Transition from hashChange to pushState or vice versa if both are
         // requested.
         if (this._wantsHashChange && this._wantsPushState) {

         // If we've started off with a route from a `pushState`-enabled
         // browser, but we're currently in a browser that doesn't support it...
         if (!this._hasPushState && !atRoot) {
         this.fragment = this.getFragment(null, true);
         this.location.replace(this.root + this.location.search + '#' + this.fragment);
         // Return immediately as browser will do redirect to new url
         return true;

         // Or if we've started out with a hash-based route, but we're currently
         // in a browser where it could be `pushState`-based instead...
         } else if (this._hasPushState && atRoot && loc.hash) {
         this.fragment = this.getHash().replace(routeStripper, '');
         this.history.replaceState({}, document.title, this.root + this.fragment + loc.search);
         }

         }

         if (!this.options.silent) return this.loadUrl();
     }
     */
    public Boolean start() {
        return start(null);
    }
    public Boolean start(Options options) {
        if (History.started) throw new Error("Backbone.history has already been started");
        History.started = true;

        // Figure out the initial configuration. Do we need an iframe?
        // Is pushState desired ... is it available?
        this.options = new Options("root", "/").extend(this.options).extend(options);
        this.root = this.options.get("root");
        this.wantsHashChange = !this.options.containsKey("hashChange") || this.options.getBoolean("hashChange");
        this.wantsPushState = this.options.getBoolean("pushState");
        this.hasPushState = this.options.getBoolean("pushState") && isPushStateSupported();

        String fragment = this.getFragment();
        int documentMode = getDocumentMode();
        boolean oldIE = (isExplorer.test(Window.Navigator.getUserAgent().toLowerCase())) && documentMode <= 7;

        // Normalize root to always include a leading and trailing slash.
        this.root = ("/" + this.root + "/").replaceAll(rootStripper.getSource(), "/");

        if(oldIE && wantsHashChange) {
            IFrameElement iFrameElement = Document.get().createIFrameElement();
            iFrameElement.setSrc("javascript:0");
            iFrameElement.setTabIndex(-1);

            iFrameElement.getStyle().setDisplay(Style.Display.NONE);

            Document.get().getBody().appendChild(iFrameElement);
            this.iFrame = iFrameElement;
            this.navigate(fragment);
        }

        // Depending on whether we're using pushState or hashes, and whether
        // 'onhashchange' is supported, determine how we check the URL state.
        if(hasPushState) {
            $(window).on("popstate", this.checkUrl);
        } else if(wantsHashChange && isOnHashChangeSupported() && !oldIE) {
            $(window).on("hashchange", this.checkUrl);
        }else if(wantsHashChange) {
            this.checkUrlInterval = new Timer();
            this.checkUrlInterval.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkUrl.f();
                }
            }, this.interval);
        }

        // Determine if we need to change the base url, for a pushState link
        // opened by a non-pushState browser.
        this.fragment = fragment;
        Boolean atRoot = Window.Location.getPath().replaceAll("/[^/]$/", "$&/").equals(this.root);

        // Transition from hashChange to pushState or vice versa if both are
        // requested.
        if(wantsHashChange && wantsPushState) {
            // If we've started off with a route from a `pushState`-enabled
            // browser, but we're currently in a browser that doesn't support it...
            if(!hasPushState && !atRoot) {
                this.fragment = getFragment(null, true);
                Window.Location.replace(this.root + getLocationSearch() + "#" + this.fragment);
                // Return immediately as browser will do redirect to new url
                return true;

                // Or if we've started out with a hash-based route, but we're currently
                // in a browser where it could be `pushState`-based instead...
            } else if (hasPushState && atRoot && Window.Location.getHash() != null) {
                this.fragment = this.getHash().replaceAll(routeStripper.getSource(), "");
                replaceHistoryState(Document.get().getTitle(), this.root + this.fragment + getLocationSearch());
            }
        }

        if (!this.options.getBoolean("silent"))
            return loadUrl();

        return true;
    }

    /**
     * // Disable Backbone.history, perhaps temporarily. Not useful in a real app,
     // but possibly useful for unit testing Routers.
     stop(): void {
         Backbone.$(window).off('popstate', this.checkUrl).off('hashchange', this.checkUrl);
         clearInterval(this._checkUrlInterval);
         History.started = false;
     }
     */
    public void stop() {
        $(window).off("popstate", this.checkUrl);
        $(window).off("hashchange", this.checkUrl);

        checkUrlInterval.cancel();
        checkUrlInterval = null;

        History.started = false;
    }

    /**
     *
     * // Add a route to be tested when the fragment changes. Routes added later
     // may override previous routes.
     route(route, callback) {
        this.handlers.unshift({ route: route, callback: callback });
     }
     */
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
    public void route(String route, Function callback) {
        Properties properties = Properties.create();
        properties.set("route", RegExp.compile(route));
        properties.setFunction("callback", callback);

        this.handlers.unshift(properties);
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

            if (current.equals(fragment) && iFrame != null) {
                current = getFragment(getHash(iFrame));
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
        String fragment = this.fragment = this.getFragment(fragmentOverride);

        for (int i = 0; i < handlers.length(); i++) {
            Properties handler = handlers.get(i);
            RegExp route = handler.get("route");
            Function callback = handler.get("callback");

            if (route.test(fragment)) {
                callback.f(fragment);
                return true;
            }
        }

        return false;
    }

    /**
     *
     * // Save a fragment into the hash history, or replace the URL state if the
     // 'replace' option is passed. You are responsible for properly URL-encoding
     // the fragment in advance.
     //
     // The options object can contain `trigger: true` if you wish to have the
     // route callback be fired (not usually desirable), or `replace: true`, if
     // you wish to modify the current URL without adding an entry to the history.
     navigate(fragment: string, options?: HistoryNavigateOptions): any
     navigate(fragment: string, options?: any): any {
         if (!History.started) return false;
         if (!options || options === true) options = { trigger: !!options };

         fragment = this.getFragment(fragment || '');
         if (this.fragment === fragment) return;
         this.fragment = fragment;

         var url = this.root + fragment;

         // Don't include a trailing slash on the root.
         if (fragment === '' && url !== '/') url = url.slice(0, -1);

         // If pushState is available, we use it to set the fragment as a real URL.
         if (this._hasPushState) {
             this.history[options.replace ? 'replaceState' : 'pushState']({}, document.title, url);

             // If hash changes haven't been explicitly disabled, update the hash
             // fragment to store history.
         } else if (this._wantsHashChange) {
             this._updateHash(this.location, fragment, options.replace);
             if (this.iframe && (fragment !== this.getFragment(this.getHash(this.iframe)))) {
                 // Opening and closing the iframe tricks IE7 and earlier to push a
                 // history entry on hash-tag change.  When replace is true, we don't
                 // want this.
                 if (!options.replace) this.iframe.document.open().close();
                 this._updateHash(this.iframe.location, fragment, options.replace);
             }

             // If you've told us that you explicitly don't want fallback hashchange-
             // based history, then `navigate` becomes a page refresh.
         } else {
            return this.location.assign(url);
         }
         if (options.trigger) return this.loadUrl(fragment);
     }
     *
     */
    private boolean navigate(String fragment) {
        return navigate(fragment, null);
    }
    // instead of: navigate(fragment, true) use navigate(fragment, new Options("trigger",true))
    public boolean navigate(String fragment, Options options) {
        if (!History.started) return false;

        fragment = getFragment(fragment != null ? fragment : "");
        if(this.fragment.equals(fragment)) return false;

        this.fragment = fragment;

        String url = this.root + fragment;

        // Don't include a trailing slash on the root.
        if (fragment.isEmpty() && !url.equals('/')) url = url.substring(0, -1);

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

            if (this.iFrame != null && (fragment.equals(this.getFragment(this.getHash(this.iFrame))))) {
                // Opening and closing the iframe tricks IE7 and earlier to push a
                // history entry on hash-tag change.  When replace is true, we don't
                // want this.
                if (!options.getBoolean("replace")) openAndCloseIFrameDocument(this.iFrame);
                updateHash(this.iFrame, fragment, options.getBoolean("replace"));
            }

            // If you've told us that you explicitly don't want fallback hashchange-
            // based history, then `navigate` becomes a page refresh.
        } else {
            Window.Location.assign(url);
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
            String locationHref = Window.Location.getHref();
            if(iFrame != null) {
                locationHref = getIFrameUrl(iFrame);
            }

            String href = locationHref.replace("/(javascript:|#).*$/", "");

            if(iFrame != null) {
                replaceFrameLocation(iFrame, href + '#' + fragment);
            } else {
                Window.Location.replace(href + '#' + fragment);
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

    private native void replaceHistoryState(String title, String route) /*-{
        return $wnd.history.replaceState({}, title, route);
    }-*/;

    private native void pushHistoryState(String title, String route) /*-{
        return $wnd.history.pushState({}, title, route);
    }-*/;

    private native String getLocationSearch() /*-{
        return $wnd.location.search;
    }-*/;

    private native Boolean isOnHashChangeSupported() /*-{
        return ('onhashchange' in window);
    }-*/;

    private native Boolean isPushStateSupported() /*-{
        return typeof($wnd.history.pushState) == "function";
    }-*/;

    private native int getDocumentMode() /*-{
        return $wnd.document.documentMode;
    }-*/;
}
