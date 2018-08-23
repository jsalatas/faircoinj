/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

package org.bitcoinj.params;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.core.SchnorrSignature;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

/**
 * Parameters for the testnet, a separate public instance of Bitcoin that has relaxed rules suitable for development
 * and testing of applications and new Bitcoin versions.
 */
public class TestNet3Params extends AbstractBitcoinNetParams {


    public TestNet3Params() {
        super();
        id = ID_TESTNET;
        // Genesis hash is 84132bdf7f2c1f8193c721967aa9056b1e7ee1a0b56bcaf6985e51777b0407f7
        packetMagic = 0x0c120a08;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 239;
        port = 41404;
        genesisBlock.setCreatorId(GENESIS_NODE_ID);
        genesisBlock.setTime(GENESIS_BLOCK_TIMESTAMP + 1);
        genesisBlock.setHashPayload(Sha256Hash.wrap("1c4ed40a950abbd27f4cd57e1ccb6613a956ce9edb16210cd5acb12f708389f3"));
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();

        checkState(genesisHash.equals("42327d5edf3cbb75bb139ec78bd62e517f14d7cbad451e4778741b6b4c1dfbc6"));
        alertSigningKey = Utils.HEX.decode("045894f38e9dd72b6f210c261d40003eb087030c42b102d3b238b396256d02f5a380ff3b7444d306d9e118fa1fc7b2b7594875f4eb64bbeaa31577391d85eb5a8a");

        genesisBlock.setCreatorSignature(SchnorrSignature.wrap("377599b4021c3e35a40667466734d2d1a3a1ef94cf52e1f5a6863af180ed7258982869d956ff34251ef4e13d7fd341a68c3e47007b5cbc0c67860a8956df9e71"));

        dnsSeeds = new String[] {
                "faircoin2-testnet-seed1.fair-coin.org"  // Thomas König
        };

        addrSeeds = null;
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;

        majorityEnforceBlockUpgrade = TestNet2Params.TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TestNet2Params.TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TestNet2Params.TESTNET_MAJORITY_WINDOW;
    }

    private static TestNet3Params instance;
    public static synchronized TestNet3Params get() {
        if (instance == null) {
            instance = new TestNet3Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }
}
