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
package org.lirazs.gbackbone.client.core.function;

import com.google.gwt.query.client.Function;

/**
 * Created on 11/12/2015.
 */
public abstract class OnceFunction extends Function {
    private boolean blocked = false;

    private String name;
    private Function callback;

    public OnceFunction(String name, Function callback) {
        this.name = name;
        this.callback = callback;
    }

    public String getName() {
        return name;
    }

    public Function getCallback() {
        return callback;
    }

    @Override
    public void f() {
        if (!blocked) {
            once();
        }
        blocked = true;
    }

    public abstract void once();
}
