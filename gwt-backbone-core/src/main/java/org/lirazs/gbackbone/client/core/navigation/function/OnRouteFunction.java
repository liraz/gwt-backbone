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
package org.lirazs.gbackbone.client.core.navigation.function;

import com.google.gwt.query.client.Function;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.navigation.Router;

/**
 * Created on 04/12/2015.
 */
public abstract class OnRouteFunction extends Function {

    @Override
    public void f() {
        f((Router)getArgument(0), (String)getArgument(1), (String[])getArgument(2));
    }

    public abstract void f(Router router, String route, String[] args);
}
