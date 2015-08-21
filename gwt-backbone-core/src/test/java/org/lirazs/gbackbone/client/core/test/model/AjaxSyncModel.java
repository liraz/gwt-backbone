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
package org.lirazs.gbackbone.client.core.test.model;

import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Promise;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

public class AjaxSyncModel extends Model {

    public AjaxSyncModel() {
        super();
    }

    public AjaxSyncModel(Options attributes) {
        super(attributes);
    }

    @Override
    public Promise sync(String method, Options options) {
        Promise sync = super.sync(method, options);

        Function success = options.get("success");
        success.f(this, null, options);

        return sync;
    }
}
