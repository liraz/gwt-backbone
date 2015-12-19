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

import com.google.gwt.query.client.Function;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.Method;
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.reflection.client.TypeOracle;

import java.util.HashMap;

@Reflectable(classAnnotations = true, fields = false, methods = true, constructors = false,
        fieldAnnotations = true, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class Router extends Events {

    /**
     * var optionalParam = /\((.*?)\)/g;
     var namedParam = /(\(\?)?:\w+/g;
     var splatParam = /\*\w+/g;
     var escapeRegExp = /[\-{}\[\]+?.,\\\^$|#\s]/g;
     */
    RegExp optionalParam = RegExp.compile("\\((.*?)\\)", "g");
    //RegExp namedParam = RegExp.compile("(\\(\\?)?:\\w+", "g"); // cannot be used, since js function is needed
    RegExp splatParam = RegExp.compile("\\*\\w+", "g");
    RegExp escapeRegExp = RegExp.compile("[\\-{}\\[\\]+?.,\\\\\\^$|#\\s]", "g");

    private HashMap<String, String> routes;


    /**
     * constructor(options?: RouterOptions) {
         super();
         options || (options = <any> {});
         if (options.routes) this.routes = options.routes;
         this._bindRoutes();
         this.initialize.apply(this, arguments);
     }
     */
    public Router() {
        this(null);
    }
    public Router(Options options) {
        if(options == null)
            options = new Options();

        if(options.containsKey("routes"))
            this.routes = options.get("routes");

        bindRoutes();
        initialize(options);
    }

    // Initialize is an empty function by default. Override it with your own
    // initialization logic.
    protected void initialize(Options options) {

    }

    protected HashMap<String, String> routes() {
        return null;
    }

    /**
     * // Manually bind a single named route to a callback. For example:
     //
     //     this.route('search/:query/p:num', 'search', function(query, num) {
     //       ...
     //     });
     //
     route(route: string, name: string, callback?: (...parameter: any[]) => void ): Router
     route(route: any, name: any, callback?: any): Router {
         if (!_.isRegExp(route)) route = this._routeToRegExp(route);
         if (_.isFunction(name)) {
             callback = name;
             name = '';
         }
         if (!callback) callback = this[name];
         var router = this;
         Backbone.history.route(route, function (fragment) {
             var args = router._extractParameters(route, fragment);
             callback && callback.apply(router, args);
             router.trigger.apply(router, ['route:' + name].concat(args));
             router.trigger('route', name, args);
             Backbone.history.trigger('route', router, name, args);
         });
         return this;
     }
     */
    protected Router route(String route, String name) {
        return route(route, name, null);
    }
    protected Router route(String route, Function callback) {
        return route(route, "", callback);
    }
    protected Router route(String route, String name, Function callback) {
        RegExp regExp = routeToRegExp(route);
        return route(regExp, name, callback);
    }
    protected Router route(RegExp route, Function callback) {
        return route(route, "", callback);
    }
    protected Router route(final RegExp route, final String name, Function callback) {

        if(callback == null) {
            callback = new Function() {
                @Override
                public void f() {
                    ClassType classType = TypeOracle.Instance.getClassType(Router.this.getClass());
                    Class[] params = new Class[getArguments().length];
                    for (int i = 0; i < getArguments().length; i++) {
                        params[i] = String.class;
                    }

                    Method method = classType.findMethod(name, params);
                    if(method != null) {
                        method.invoke(Router.this, getArguments());
                    }
                }
            };
        }

        final Function innerCallback = callback;

        History.get().route(route, new Function() {
            @Override
            public void f() {
                String fragment = this.getArgument(0);

                String[] args = extractParameters(route, fragment);
                if(innerCallback != null) {
                    innerCallback.f(args);
                }
                trigger("route:" + name, args);
                trigger("route", name, args);

                History.get().trigger("route", Router.this, name, args);
            }
        });
        return this;
    }

    /**
     * // Simple proxy to `Backbone.history` to save a fragment into the history.
     navigate(fragment: string, options?: HistoryNavigateOptions): Router {
         Backbone.history.navigate(fragment, options);
         return this;
     }
     */
    public Router navigate(String fragment, Options options) {
        History.get().navigate(fragment, options);
        return this;
    }

    /**
     *
     *
     * // Bind all defined routes to `Backbone.history`. We have to reverse the
     // order of the routes here to support behavior where the most general
     // routes can be defined at the bottom of the route map.
     _bindRoutes(): void {
         if (!this.routes) return;
         this.routes = _.result(this, 'routes');
         var route, routes = _.keys(this.routes);

         while ((route = routes.pop()) != null) {
            this.route(route, this.routes[route]);
         }
     }
     */
    private void bindRoutes() {
        HashMap<String, String> routesMap = routes();
        if(routesMap == null && this.routes == null) return;

        if (routesMap != null) {
            this.routes = routesMap;
        }

        for (String route : this.routes.keySet()) {
            String name = this.routes.get(route);
            route(route, name);
        }
    }

    /**
     * // Convert a route string into a regular expression, suitable for matching
     // against the current location hash.
     _routeToRegExp(route: string): RegExp {
         route = route.replace(escapeRegExp, '\\$&')
                         .replace(optionalParam, '(?:$1)?')
                         .replace(namedParam, function (match, optional) {
                            return optional ? match : '([^\/]+)';
                         })
                         .replace(splatParam, '(.*?)');

         return new RegExp('^' + route + '$');
     }
     *
     */
    private RegExp routeToRegExp(String route) {
        route = route.replaceAll(escapeRegExp.getSource(), "\\$&");
        route = route.replaceAll(optionalParam.getSource(), "(?:$1)?");

        route = replaceNamedParam(route);
        route = route.replaceAll(splatParam.getSource(), "(.*?)");

        return RegExp.compile("^" + route + "$");
    }

    private native String replaceNamedParam(String input) /*-{
        return input.replace(new RegExp("(\\(\\?)?:\\w+", "g"), function (match, optional) {
            return optional ? match : '([^\/]+)';
        });
    }-*/;

    /**
     * // Given a route, and a URL fragment that it matches, return the array of
     // extracted decoded parameters. Empty or unmatched parameters will be
     // treated as `null` to normalize cross-browser behavior.
     _extractParameters(route: RegExp, fragment: string): string[] {
         var params = route.exec(fragment).slice(1);
         return _.map(params, function (param) {
         return param ? decodeURIComponent(param) : null;
         });
     }
     */
    private String[] extractParameters(RegExp route, String fragment) {
        MatchResult matchResult = route.exec(fragment);
        int groupCount = matchResult.getGroupCount() - 1;
        if(groupCount < 0)
            groupCount = 0;

        String[] result = new String[groupCount];

        for (int i = 0; i < groupCount; i++) {
            String param = matchResult.getGroup(i + 1);
            result[i] = decodeURIComponent(param);
        }
        return result;
    }

    private native String decodeURIComponent(String s) /*-{
        return decodeURIComponent(s);
    }-*/;
}
