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
package org.lirazs.gbackbone.client.core.util;

import org.lirazs.gbackbone.client.core.js.JsObject;

public class JsObjectUtils {

    public static native JsObject defaults(JsObject obj, JsObject...args) /*-{
        if (!@org.lirazs.gbackbone.client.core.util.JsObjectUtils::isObject(Ljava/lang/Object;)(obj)) return obj;
        for (var i = 1, length = arguments.length; i < length; i++) {
            var source = arguments[i];
            for (var prop in source) {
                if (obj[prop] === void 0) obj[prop] = source[prop];
            }
        }
        return obj;
    }-*/;

    public static native boolean isObject(Object obj) /*-{
        var type = typeof obj;
        return type === 'function' || type === 'object' && !!obj;
    }-*/;

    public static native boolean keys(Object obj) /*-{
        if (!@org.lirazs.gbackbone.client.core.util.JsObjectUtils::isObject(Ljava/lang/Object;)(obj)) return [];
        if (Object.keys) return Object.keys(obj);
        var keys = [];
        for (var key in obj) if (@org.lirazs.gbackbone.client.core.util.JsObjectUtils::has(Ljava/lang/Object;Ljava/lang/String;)(obj, key)) keys.push(key);
        return keys;
    }-*/;

    public static native boolean has(Object obj, String key) /*-{
        return obj != null && hasOwnProperty.call(obj, key);
    }-*/;

    public static native boolean isFunction(Object obj) /*-{
        return toString.call(obj) === '[object Function]';
    }-*/;

    public static native boolean isEqual(Object obj1, Object obj2) /*-{
        return @org.lirazs.gbackbone.client.core.util.JsObjectUtils::isEqual(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/Object;)(obj1, obj2, [], []);
    }-*/;

    public static native boolean isEqual(Object obj1, Object obj2, Object[] aStack, Object[] bStack) /*-{
        // Identical objects are equal. `0 === -0`, but they aren't identical.
        // See the [Harmony `egal` proposal](http://wiki.ecmascript.org/doku.php?id=harmony:egal).
        if (obj1 === obj2) return obj1 !== 0 || 1 / obj1 === 1 / obj2;
        // A strict comparison is necessary because `null == undefined`.
        if (obj1 == null || obj2 == null) return obj1 === obj2;
        // Compare `[[Class]]` names.
        var className = toString.call(obj1);
        if (className !== toString.call(obj2)) return false;
        switch (className) {
            // Strings, numbers, regular expressions, dates, and booleans are compared by value.
            case '[object RegExp]':
            // RegExps are coerced to strings for comparison (Note: '' + /obj1/i === '/obj1/i')
            case '[object String]':
                // Primitives and their corresponding object wrappers are equivalent; thus, `"5"` is
                // equivalent to `new String("5")`.
                return '' + obj1 === '' + obj2;
            case '[object Number]':
                // `NaN`s are equivalent, but non-reflexive.
                // Object(NaN) is equivalent to NaN
                if (+obj1 !== +obj1) return +obj2 !== +obj2;
                // An `egal` comparison is performed for other numeric values.
                return +obj1 === 0 ? 1 / +obj1 === 1 / obj2 : +obj1 === +obj2;
            case '[object Date]':
            case '[object Boolean]':
                // Coerce dates and booleans to numeric primitive values. Dates are compared by their
                // millisecond representations. Note that invalid dates with millisecond representations
                // of `NaN` are not equivalent.
                return +obj1 === +obj2;
        }
        if (typeof obj1 != 'object' || typeof obj2 != 'object') return false;
        // Assume equality for cyclic structures. The algorithm for detecting cyclic
        // structures is adapted from ES 5.1 section 15.12.3, abstract operation `JO`.
        var length = aStack.length;
        while (length--) {
            // Linear search. Performance is inversely proportional to the number of
            // unique nested structures.
            if (aStack[length] === obj1) return bStack[length] === obj2;
        }
        // Objects with different constructors are not equivalent, but `Object`s
        // from different frames are.
        var aCtor = obj1.constructor, bCtor = obj2.constructor;
        if (
            aCtor !== bCtor &&
                // Handle Object.create(x) cases
            'constructor' in obj1 && 'constructor' in obj2 &&
            !(@org.lirazs.gbackbone.client.core.util.JsObjectUtils::isFunction(Ljava/lang/Object;)(aCtor) && aCtor instanceof aCtor &&
            @org.lirazs.gbackbone.client.core.util.JsObjectUtils::isFunction(Ljava/lang/Object;)(bCtor) && bCtor instanceof bCtor)
        ) {
            return false;
        }
        // Add the first object to the stack of traversed objects.
        aStack.push(obj1);
        bStack.push(obj2);
        var size, result;
        // Recursively compare objects and arrays.
        if (className === '[object Array]') {
            // Compare array lengths to determine if obj1 deep comparison is necessary.
            size = obj1.length;
            result = size === obj2.length;
            if (result) {
                // Deep compare the contents, ignoring non-numeric properties.
                while (size--) {
                    if (!(result = @org.lirazs.gbackbone.client.core.util.JsObjectUtils::isEqual(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/Object;)(obj1[size], obj2[size], aStack, bStack))) break;
                }
            }
        } else {
            // Deep compare objects.
            var keys = @org.lirazs.gbackbone.client.core.util.JsObjectUtils::keys(Ljava/lang/Object;)(obj1), key;
            size = keys.length;
            // Ensure that both objects contain the same number of properties before comparing deep equality.
            result = @org.lirazs.gbackbone.client.core.util.JsObjectUtils::keys(Ljava/lang/Object;)(obj2).length === size;
            if (result) {
                while (size--) {
                    // Deep compare each member
                    key = keys[size];
                    if (!(result = @org.lirazs.gbackbone.client.core.util.JsObjectUtils::has(Ljava/lang/Object;Ljava/lang/String;)(obj2, key) &&
                        @org.lirazs.gbackbone.client.core.util.JsObjectUtils::isEqual(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;[Ljava/lang/Object;)(obj1[key], obj2[key], aStack, bStack))) break;
                }
            }
        }
        // Remove the first object from the stack of traversed objects.
        aStack.pop();
        bStack.pop();
        return result;
    }-*/;
}
