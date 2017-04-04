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
 * Contains information about a chain administrator
 */
public class ChainAdmin implements Serializable {
    private static final long serialVersionUID = -7964179218957406027L;

    private long adminId;
    private long heightAdded;
    SchnorrPublicKey pubKey;

    public ChainAdmin() { }

    public ChainAdmin(long adminId, long heightAdded, SchnorrPublicKey pubKey) {
        super();
        this.adminId = adminId;
        this.heightAdded = heightAdded;
        this.pubKey = pubKey;
    }

    public long getAdminId() {
        return adminId;
    }
    public void setAdminId(long adminId) {
        this.adminId = adminId;
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
        s.append("   ChainAdmin: adminId: ").append(String.format("0x%08x", adminId))
         .append(", heightAdded: ").append(heightAdded)
         .append(", pubKey: ").append(pubKey).append('\n');

        return s.toString();
    }
}
