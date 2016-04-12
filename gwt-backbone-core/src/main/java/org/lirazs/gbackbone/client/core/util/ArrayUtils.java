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

import org.lirazs.gbackbone.client.core.js.JsArray;

public class ArrayUtils {

    public static boolean hasElements(Object[] array) {
        for (int i=0; i<array.length; i++) {
            if (array[i] != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the given array contains the given object.
     *
     * @param object The object to search for.
     * @param array The array to search in.
     * @return <code>true</code> if the array contains the given object, <code>false</code> otherwise.
     */
    public static boolean contains(Object object, Object[] array) {
        for (int i=0; i<array.length; i++) {
            if (object.equals(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the given string array contains the given string while ignoring the case.
     *
     * @param string The string to search for.
     * @param array The array to search in.
     * @return <code>true</code> if the array contains the given string, <code>false</code> otherwise.
     */
    public static boolean containsIgnoreCase(String string, String[] array) {
        for (int i=0; i<array.length; i++) {
            if (string.equalsIgnoreCase(array[i])) {
                return true;
            }
        }
        return false;
    }

    public static Object[] joinArrays (Object[] arr1, Object[] arr2) {
        Object[] arr3 = new Object[arr1.length + arr2.length];
        int i;
        for (i = 0; i < arr1.length; i++) {
            arr3[i] = arr1[i];
        }
        for (i = 0; i < arr2.length; i++) {
            arr3[arr1.length + i] = arr2[i];
        }
        return arr3;
    }

    /**
     * Join strings inserting separator between them.
     */
    public static String join(String[] strings, String separator) {
        StringBuffer result = new StringBuffer();

        for (String s : strings) {
            if (result.length() != 0) {
                result.append(separator);
            }
            result.append(s);
        }

        return result.toString();
    }
}
