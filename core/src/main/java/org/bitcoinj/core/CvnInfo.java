/*
 * Copyright 2017 Thomas König
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.core;

import java.io.Serializable;

/**
 * Contains information about a CVN
 */
public class CvnInfo implements Serializable {
    private static final long serialVersionUID = 6877986129537161798L;

    private long nodeId;
    private long heightAdded;
    SchnorrPublicKey pubKey;

    public CvnInfo() { }

    public CvnInfo(long nodeId, long heightAdded, SchnorrPublicKey pubKey) {
        super();
        this.nodeId = nodeId;
        this.heightAdded = heightAdded;
        this.pubKey = pubKey;
    }

    public long getNodeId() {
        return nodeId;
    }
    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }
    public long getHeightAdded() {
        return heightAdded;
    }
    public void setHeightAdded(long heightAdded) {
        this.heightAdded = heightAdded;
    }
    public SchnorrPublicKey getPubKey() {
        return pubKey;
    }
    public void setPubKey(SchnorrPublicKey pubKey) {
        this.pubKey = pubKey;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("   CvnInfo: nodeId: ").append(String.format("0x%08x", nodeId))
         .append(", heightAdded: ").append(heightAdded)
         .append(", pubKey: ").append(pubKey).append('\n');

        return s.toString();
    }
}
