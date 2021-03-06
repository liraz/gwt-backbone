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
package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.query.client.Promise;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

public class TestSyncCreateModel extends Model {

    private String lastSyncMethod;

    public TestSyncCreateModel(Options options) {
        super(options);
    }

    public String getLastSyncMethod() {
        return lastSyncMethod;
    }

    public TestSyncCreateModel(Options attributes, Options options) {
        super(attributes, options);
    }

    @Override
    public Promise sync(String method, Options options) {
        lastSyncMethod = method;

        return super.sync(method, options);
    }
}
