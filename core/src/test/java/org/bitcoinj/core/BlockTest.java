/*
 * Copyright 2011 Google Inc.
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

package org.bitcoinj.core;

import com.google.common.io.ByteStreams;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class BlockTest {
    private static final NetworkParameters TESTNET = TestNet3Params.get();
    private static final NetworkParameters UNITTEST = UnitTestParams.get();
    private static final NetworkParameters MAINNET = MainNetParams.get();

    private byte[] block126001Bytes;
    private Block block126001;

    @Before
    public void setUp() throws Exception {
        new Context(TESTNET);
        // One with some of transactions in, so a good test of the merkle tree hashing.
        block126001Bytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_testnet_126001.dat"));
        block126001 = TESTNET.getDefaultSerializer().makeBlock(block126001Bytes);
        assertEquals("5ebb6fc342232dcdd162b1a998e23ee368c10595ee13b15a342dc5870c10aa1b", block126001.getHashAsString());
    }

    @Test
    public void testWork() throws Exception {
        BigInteger work = TESTNET.getGenesisBlock().getWork();
        double log2Work = Math.log(work.longValue()) / Math.log(2);
        assertEquals(4.321928, log2Work, 0.0000001);
    }

    @Test
    public void testBlockVerification() throws Exception {
        block126001.verify(Block.BLOCK_HEIGHT_GENESIS, EnumSet.noneOf(Block.VerifyFlag.class));
    }
    
    @Test
    public void testDate() throws Exception {
        assertEquals("2019-04-18T12:29:59Z", Utils.dateTimeFormat(block126001.getTime()));
    }

    @Test
    public void testBadTransactions() throws Exception {
        // Re-arrange so the coinbase transaction is not first.
        Transaction tx1 = block126001.transactions.get(0);
        Transaction tx2 = block126001.transactions.get(1);
        block126001.transactions.set(0, tx2);
        block126001.transactions.set(1, tx1);
        try {
            block126001.verify(Block.BLOCK_HEIGHT_GENESIS, EnumSet.noneOf(Block.VerifyFlag.class));
            fail();
        } catch (VerificationException e) {
            // We should get here.
        }
    }

    @Test
    public void testHeaderParse() throws Exception {
        Block header = block126001.cloneAsHeader();
        Block reparsed = TESTNET.getDefaultSerializer().makeBlock(header.bitcoinSerialize());
        assertEquals(reparsed, header);
    }

    @Test
    public void testBitcoinSerialization() throws Exception {
        // We have to be able to reserialize everything exactly as we found it for hashing to work. This test also
        // proves that transaction serialization works, along with all its subobjects like scripts and in/outpoints.
        //
        // NB: This tests the bitcoin serialization protocol.
        assertTrue(Arrays.equals(block126001Bytes, block126001.bitcoinSerialize()));

        // test different versions of blocks in mainnet

        byte[] blockBytes;
        Block block;

        // tx|cvninfo|params|admins (genesis block)
        blockBytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("genesis_mainnet.dat"));
        block = MAINNET.getDefaultSerializer().makeBlock(blockBytes);
        assertEquals("beed44fa5e96150d95d56ebd5d2625781825a9407a5215dd7eda723373a0a1d7", block.getHashAsString());
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));

        // tx|cvninfo (block 113)
        blockBytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_mainnet_113.dat"));
        block = MAINNET.getDefaultSerializer().makeBlock(blockBytes);
        assertEquals("b86c2fb4d2bbccf73336a6426b41514c2ccd5ce3394830bc0aac7bbe0b64d2c4", block.getHashAsString());
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));

        // tx|admins (block 60)
        blockBytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_mainnet_60.dat"));
        block = MAINNET.getDefaultSerializer().makeBlock(blockBytes);
        assertEquals("ef4c3f4f50d716b35a31fd4aa078bd1d8e7a539b7075f86accdc0f66a5c16cad", block.getHashAsString());
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));

        // tx|params (block 163)
        blockBytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_mainnet_163.dat"));
        block = MAINNET.getDefaultSerializer().makeBlock(blockBytes);
        assertEquals("33376f3ff0266525eebbd7d0fe2f8750d0a2687b7c900ef9a06aafb11274b55e", block.getHashAsString());
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));

        // tx|supply (block 143)
        blockBytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_mainnet_143.dat"));
        block = MAINNET.getDefaultSerializer().makeBlock(blockBytes);
        assertEquals("23625991f93736d6b7b55da1c6013e6f1e614d83fe919f1fda41d9065b3bd4fa", block.getHashAsString());
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));

        // missing signatures (block 1623)
        blockBytes = ByteStreams.toByteArray(BlockTest.class.getResourceAsStream("block_mainnet_1623.dat"));
        block = MAINNET.getDefaultSerializer().makeBlock(blockBytes);
        assertEquals("326a23c500506784b02ee958b5185840efa924f3d1ebb822c4ab8ef468e7b42e", block.getHashAsString());
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));
    }

    @Test
    public void testUpdateLength() {
        Block block = UNITTEST.getGenesisBlock().createNextBlockWithCoinbase(1 + Block.TX_PAYLOAD, new ECKey().getPubKey(), Block.BLOCK_HEIGHT_GENESIS);
        assertEquals(block.bitcoinSerialize().length, block.length);
        final int origBlockLen = block.length;
        Transaction tx = new Transaction(UNITTEST);
        // this is broken until the transaction has > 1 input + output (which is required anyway...)
        //assertTrue(tx.length == tx.bitcoinSerialize().length && tx.length == 8);
        byte[] outputScript = new byte[10];
        Arrays.fill(outputScript, (byte) ScriptOpCodes.OP_FALSE);
        tx.addOutput(new TransactionOutput(UNITTEST, null, Coin.SATOSHI, outputScript));
        tx.addInput(new TransactionInput(UNITTEST, null, new byte[] {(byte) ScriptOpCodes.OP_FALSE},
                new TransactionOutPoint(UNITTEST, 0, Sha256Hash.of(new byte[] { 1 }))));
        int origTxLength = 8 + 2 + 8 + 1 + 10 + 40 + 1 + 1;
        assertEquals(tx.unsafeBitcoinSerialize().length, tx.length);
        assertEquals(origTxLength, tx.length);
        block.addTransaction(tx);
        assertEquals(block.unsafeBitcoinSerialize().length, block.length);
        assertEquals(origBlockLen + tx.length, block.length);
        block.getTransactions().get(1).getInputs().get(0).setScriptBytes(new byte[] {(byte) ScriptOpCodes.OP_FALSE, (byte) ScriptOpCodes.OP_FALSE});
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength + 1);
        block.getTransactions().get(1).getInputs().get(0).clearScriptBytes();
        assertEquals(block.length, block.unsafeBitcoinSerialize().length);
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength - 1);
        block.getTransactions().get(1).addInput(new TransactionInput(UNITTEST, null, new byte[] {(byte) ScriptOpCodes.OP_FALSE},
                new TransactionOutPoint(UNITTEST, 0, Sha256Hash.of(new byte[] { 1 }))));
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength + 41); // - 1 + 40 + 1 + 1
    }

    @Test
    public void testCoinbaseHeightTestnet() throws Exception {
        Block block = TESTNET.getDefaultSerializer().makeBlock(
            ByteStreams.toByteArray(getClass().getResourceAsStream("block_testnet_9111.dat")));

        // Check block.
        assertEquals("6dad629859427376bcd4070e550190cb263b99cefd0ca9451f4a7fcfcceb7692", block.getHashAsString());
        block.verify(9111, EnumSet.of(Block.VerifyFlag.HEIGHT_IN_COINBASE));
     }
}
