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
import org.lirazs.gbackbone.client.core.annotation.Route;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.event.Events;
import org.lirazs.gbackbone.reflection.client.*;

import java.util.*;

@Reflectable(classAnnotations = true, fields = false, methods = true, constructors = false,
        fieldAnnotations = true, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class Router extends Events {

    private Map<String, ?> routes;


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

    protected Map<String, ?> routes() {
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
    public Router route(String route, Function callback) {
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
                    String[] params = new String[getArguments().length];
                    for (int i = 0; i < getArguments().length; i++) {
                        params[i] = ReflectionUtils.getQualifiedSourceName( String.class );
                    }

                    Method method = classType.findMethod(name, params);

                    if(method != null) {
                        method.invoke(Router.this, getArguments());
                    } else {
                        // try another look for a String[] array parameter
                        method = classType.findMethod(name, new String[] { "java.lang.String[]" });
                        if(method != null) {
                            method.invoke(Router.this, new Object[] { getArguments() });
                        }
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

                if (execute(innerCallback, args, name)) {
                    trigger("route:" + name, args);
                    trigger("route", name, args);

                    History.get().trigger("route", Router.this, name, args);
                }
            }
        });
        return this;
    }

    // Execute a route handler with the provided parameters.  This is an
    // excellent place to do pre-route setup or post-route cleanup.
    /**execute: function(callback, args, name) {
        if (callback) callback.apply(this, args);
    },*/
    protected boolean execute(Function callback, String[] args, String name) {
        if(callback != null) {
            callback.f(args);
        }
        return true;
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
        Map routesMap = routes();
        Map<String, String> annotatedMethodRoutes = getAnnotatedMethodRoutes();

        if(routesMap == null && this.routes == null && annotatedMethodRoutes.isEmpty()) return;

        if (routesMap != null) {
            if(!annotatedMethodRoutes.isEmpty()) {
                routesMap.putAll(annotatedMethodRoutes);
            }
            this.routes = routesMap;
        } else {
            this.routes = annotatedMethodRoutes;
        }

        List<String> keys = new ArrayList<String>(routes.keySet());
        for(int i = keys.size() - 1; i >= 0; i--){
            String route = keys.get(i);

            Object o = this.routes.get(route);
            if (o instanceof String) {
                String name = (String) o;
                route(route, name);
            } else if(o instanceof Function) {
                Function callback = (Function) o;
                route(route, callback);
            }
        }
    }

    private Map<String, String> getAnnotatedMethodRoutes() {
        Map<String, String> routes = new LinkedHashMap<String, String>();

        try {
            ClassType classType = TypeOracle.Instance.getClassType(getClass());

            Method[] methods = classType.getMethods();

            for (final Method method : methods) {
                Route annotation = method.getAnnotation(Route.class);
                if(annotation != null && method.isPublic()) {

                    String methodName = method.getName();
                    String[] values = annotation.value();

                    for (String value : values) {
                        if(!value.isEmpty()) {
                            routes.put(value, methodName);
                        }
                    }
                }
            }
        } catch (MethodInvokeException e) {
            e.printStackTrace();
        } catch (ReflectionRequiredException e) {
            // do nothing... a reflection operation was operated on an inner class
        }
        return routes;
    }

    /**
     *
     * var optionalParam = /\((.*?)\)/g;
     var namedParam    = /(\(\?)?:\w+/g;
     var splatParam    = /\*\w+/g;
     var escapeRegExp  = /[\-{}\[\]+?.,\\\^$|#\s]/g;
     *
     * // Convert a route string into a regular expression, suitable for matching
     // against the current location hash.
     _routeToRegExp: function(route) {
         route = route.replace(escapeRegExp, '\\$&')
             .replace(optionalParam, '(?:$1)?')
             .replace(namedParam, function(match, optional) {
                return optional ? match : '([^/?]+)';
             })
             .replace(splatParam, '([^?]*?)');
         return new RegExp('^' + route + '(?:\\?([\\s\\S]*))?$');
     },
     *
     */
    private RegExp routeToRegExp(String route) {
        return RegExp.compile(nativeRouteToRegExp(route));
    }

    private native String nativeRouteToRegExp(String input) /*-{
        input = input.replace(/[\-{}\[\]+?.,\\\^$|#\s]/g, '\\$&')
            .replace(/\((.*?)\)/g, '(?:$1)?')
            .replace(/(\(\?)?:\w+/g, function(match, optional) {
                return optional ? match : '([^/?]+)';
            })
            .replace(/\*\w+/g, '([^?]*?)');
        return '^' + input + '(?:\\?([\\s\\S]*))?$';
    }-*/;

    /**
     * Given a route, and a URL fragment that it matches, return the array of
     * extracted decoded parameters. Empty or unmatched parameters will be
     * treated as `null` to normalize cross-browser behavior.
     *
     * @param route
     * @param fragment
     * @return
     */
    private String[] extractParameters(RegExp route, String fragment) {
        MatchResult matchResult = route.exec(fragment);
        int groupCount = matchResult.getGroupCount() - 1;
        if(groupCount < 0)
            groupCount = 0;

        List<String> params = new ArrayList<String>();

        for (int i = 0; i < groupCount; i++) {
            String param = matchResult.getGroup(i + 1);

            if (param != null && !param.isEmpty()) {
                // Don't decode the search params.
                if(i == groupCount - 1) {
                    params.add(param);
                } else {
                    params.add(decodeURIComponent(param));
                }
            }
        }
        return params.toArray(new String[params.size()]);
    }

    private native String decodeURIComponent(String s) /*-{
        return decodeURIComponent(s);
    }-*/;
}
