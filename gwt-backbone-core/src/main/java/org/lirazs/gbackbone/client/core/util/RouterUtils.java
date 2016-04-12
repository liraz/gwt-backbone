/*
 * Copyright 2016, Liraz Shilkrot
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
package org.lirazs.gbackbone.client.core.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 4/9/16.
 */
public class RouterUtils {

    /**
     * Convert a route string into a regular expression, suitable for matching
     * against the current location hash.
     *
     * @param route
     * @return
     */
    public static RegExp routeToRegExp(String route) {
        return RegExp.compile(nativeRouteToRegExp(route));
    }

    /**
     * Given a route, and a URL fragment that it matches, return the array of
     * extracted decoded parameters. Empty or unmatched parameters will be
     * treated as `null` to normalize cross-browser behavior.
     *
     * @param route
     * @param fragment
     * @return
     */
    public static String[] extractParameters(RegExp route, String fragment) {
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

    private static native String nativeRouteToRegExp(String input) /*-{
        input = input.replace(/[\-{}\[\]+?.,\\\^$|#\s]/g, '\\$&')
            .replace(/\((.*?)\)/g, '(?:$1)?')
            .replace(/(\(\?)?:\w+/g, function(match, optional) {
                return optional ? match : '([^/?]+)';
            })
            .replace(/\*\w+/g, '([^?]*?)');
        return '^' + input + '(?:\\?([\\s\\S]*))?$';
    }-*/;

    private static native String decodeURIComponent(String s) /*-{
        return decodeURIComponent(s);
    }-*/;
}
