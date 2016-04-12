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

import java.math.BigDecimal;

/**
 * Created on 05/12/2015.
 */
public class ObjectUtils {

    public static int compare(Object o1, Object o2) {
        int result = 0;

        if(o1 != null) {
            result = 1;

            if (o2 != null) {
                if(o1 instanceof String) {
                    result = ((String) o1).compareTo((String)o2);
                } else if(o1 instanceof Integer) {
                    result = ((Integer) o1).compareTo((Integer)o2);
                } else if(o1 instanceof Float) {
                    result = ((Float) o1).compareTo((Float)o2);
                } else if(o1 instanceof Double) {
                    result = ((Double) o1).compareTo((Double)o2);
                } else if(o1 instanceof BigDecimal) {
                    result = ((BigDecimal) o1).compareTo((BigDecimal)o2);
                } else if(o1 instanceof Long) {
                    result = ((Long) o1).compareTo((Long)o2);
                }
            }
        } else if(o2 != null) {
            result = -1;
        }

        return result;
    }
}
