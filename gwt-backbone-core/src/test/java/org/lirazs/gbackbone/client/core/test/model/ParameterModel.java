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

import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.model.Model;

public class ParameterModel extends Model {

    private String modelParameter;

    public String getModelParameter() {
        return modelParameter;
    }

    public void setModelParameter(String modelParameter) {
        this.modelParameter = modelParameter;
    }

    public ParameterModel(Options attributes, Options options) {
        super(attributes, options);

        modelParameter = options.get("model_parameter");
    }
}
