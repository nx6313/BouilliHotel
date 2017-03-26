/**
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smackx.entitycaps.cache;

import com.bouilli.nxx.bouillihotel.push.org.jivesoftware.smackx.packet.DiscoverInfo;

import java.io.IOException;

public interface EntityCapsPersistentCache {
    /**
     * Add an DiscoverInfo to the persistent Cache
     * 
     * @param node
     * @param info
     */
    void addDiscoverInfoByNodePersistent(String node, DiscoverInfo info);

    /**
     * Replay the Caches data into EntityCapsManager
     */
    void replay() throws IOException;

    /**
     * Empty the Cache
     */
    void emptyCache();
}
