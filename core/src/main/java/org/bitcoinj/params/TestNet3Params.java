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
        packetMagic = 0x0b110907;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 239;
        port = 41404;
        genesisBlock.setCreatorId(0xc001d00dL);
        genesisBlock.setTime(1493456001L);
        genesisBlock.setHashPayload(Sha256Hash.wrap("332ef9fee6186c31d30599c832ed14820c584d843e962f60399c20e75690725f"));
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();

        checkState(genesisHash.equals("9eaf9d5e1b4c9e2b1f9908009f46394ce6ef9cae6ed0522a710cdc40b44e2b17"));
        alertSigningKey = Utils.HEX.decode("045894f38e9dd72b6f210c261d40003eb087030c42b102d3b238b396256d02f5a380ff3b7444d306d9e118fa1fc7b2b7594875f4eb64bbeaa31577391d85eb5a8a");

        genesisBlock.setCreatorSignature(SchnorrSignature.wrap("bbe18aea45eb5b2cb2f091655ddda6d6d439e6648ed1d50017f03254b4518279d8a30bf3d0be24513b0099d0c0b48d95086ffd382819b1364e1f821a83c74b97"));

        dnsSeeds = new String[] {

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
