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
package org.lirazs.gbackbone.navigation.client.storage;

import org.lirazs.gbackbone.client.core.data.Options;

/**
 *
 */
public interface BrowserStorage {

    /**
     *
     * @param storageKey
     * @return
     */
    boolean hasItem(String storageKey);

    /**
     *
     * @param storageKey
     * @return
     */
    Options getItem(String storageKey);

    /**
     *
     * @param storageKey
     * @param data
     */
    void setItem(String storageKey, Options data);

    /**
     *
     * @param storageKey
     */
    void removeItem(String storageKey);

    /**
     *
     */
    void removeAll();
}
