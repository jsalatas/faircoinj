/*
 * Copyright 2011 Noa Resare
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

import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.*;

public class BitcoinSerializerTest {
    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final byte[] ADDRESS_MESSAGE_BYTES = HEX.decode("fabfb5da6164647200000000000000001f000000ed52399b01e215104d010000000000000000000000000000000000ffff0a000001208d");

    private static final byte[] TRANSACTION_MESSAGE_BYTES = HEX.withSeparator(" ", 2).decode(
            "fa bf b5 da 74 78 00 00  00 00 00 00 00 00 00 00" +
                    "02 01 00 00 e2 93 cd be  01 00 00 00 01 6d bd db" +
                    "08 5b 1d 8a f7 51 84 f0  bc 01 fa d5 8d 12 66 e9" +
                    "b6 3b 50 88 19 90 e4 b4  0d 6a ee 36 29 00 00 00" +
                    "00 8b 48 30 45 02 21 00  f3 58 1e 19 72 ae 8a c7" +
                    "c7 36 7a 7a 25 3b c1 13  52 23 ad b9 a4 68 bb 3a" +
                    "59 23 3f 45 bc 57 83 80  02 20 59 af 01 ca 17 d0" +
                    "0e 41 83 7a 1d 58 e9 7a  a3 1b ae 58 4e de c2 8d" +
                    "35 bd 96 92 36 90 91 3b  ae 9a 01 41 04 9c 02 bf" +
                    "c9 7e f2 36 ce 6d 8f e5  d9 40 13 c7 21 e9 15 98" +
                    "2a cd 2b 12 b6 5d 9b 7d  59 e2 0a 84 20 05 f8 fc" +
                    "4e 02 53 2e 87 3d 37 b9  6f 09 d6 d4 51 1a da 8f" +
                    "14 04 2f 46 61 4a 4c 70  c0 f1 4b ef f5 ff ff ff" +
                    "ff 02 40 4b 4c 00 00 00  00 00 19 76 a9 14 1a a0" +
                    "cd 1c be a6 e7 45 8a 7a  ba d5 12 a9 d9 ea 1a fb" +
                    "22 5e 88 ac 80 fa e9 c7  00 00 00 00 19 76 a9 14" +
                    "0e ab 5b ea 43 6a 04 84  cf ab 12 48 5e fd a0 b7" +
                    "8b 4e cc 52 88 ac 00 00  00 00");

    @Test
    public void testAddr() throws Exception {
        MessageSerializer serializer = MAINNET.getDefaultSerializer();
        // the actual data from https://en.bitcoin.it/wiki/Protocol_specification#addr
        AddressMessage addressMessage = (AddressMessage) serializer.deserialize(ByteBuffer.wrap(ADDRESS_MESSAGE_BYTES));
        assertEquals(1, addressMessage.getAddresses().size());
        PeerAddress peerAddress = addressMessage.getAddresses().get(0);
        assertEquals(8333, peerAddress.getPort());
        assertEquals("10.0.0.1", peerAddress.getAddr().getHostAddress());
        ByteArrayOutputStream bos = new ByteArrayOutputStream(ADDRESS_MESSAGE_BYTES.length);
        serializer.serialize(addressMessage, bos);

        assertEquals(31, addressMessage.getMessageSize());
        addressMessage.addAddress(new PeerAddress(MAINNET, InetAddress.getLocalHost()));
        assertEquals(61, addressMessage.getMessageSize());
        addressMessage.removeAddress(0);
        assertEquals(31, addressMessage.getMessageSize());

        //this wont be true due to dynamic timestamps.
        //assertTrue(LazyParseByteCacheTest.arrayContains(bos.toByteArray(), addrMessage));
    }

    @Test
    public void testCachedParsing() throws Exception {
        MessageSerializer serializer = MAINNET.getSerializer(true);

        // first try writing to a fields to ensure uncaching and children are not affected
        Transaction transaction = (Transaction) serializer.deserialize(ByteBuffer.wrap(TRANSACTION_MESSAGE_BYTES));
        assertNotNull(transaction);
        assertTrue(transaction.isCached());

        transaction.setLockTime(1);
        // parent should have been uncached
        assertFalse(transaction.isCached());
        // child should remain cached.
        assertTrue(transaction.getInputs().get(0).isCached());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializer.serialize(transaction, bos);
        assertFalse(Arrays.equals(TRANSACTION_MESSAGE_BYTES, bos.toByteArray()));

        // now try writing to a child to ensure uncaching is propagated up to parent but not to siblings
        transaction = (Transaction) serializer.deserialize(ByteBuffer.wrap(TRANSACTION_MESSAGE_BYTES));
        assertNotNull(transaction);
        assertTrue(transaction.isCached());

        transaction.getInputs().get(0).setSequenceNumber(1);
        // parent should have been uncached
        assertFalse(transaction.isCached());
        // so should child
        assertFalse(transaction.getInputs().get(0).isCached());

        bos = new ByteArrayOutputStream();
        serializer.serialize(transaction, bos);
        assertFalse(Arrays.equals(TRANSACTION_MESSAGE_BYTES, bos.toByteArray()));

        // deserialize/reserialize to check for equals.
        transaction = (Transaction) serializer.deserialize(ByteBuffer.wrap(TRANSACTION_MESSAGE_BYTES));
        assertNotNull(transaction);
        assertTrue(transaction.isCached());
        bos = new ByteArrayOutputStream();
        serializer.serialize(transaction, bos);
        assertTrue(Arrays.equals(TRANSACTION_MESSAGE_BYTES, bos.toByteArray()));

        // deserialize/reserialize to check for equals.  Set a field to it's existing value to trigger uncache
        transaction = (Transaction) serializer.deserialize(ByteBuffer.wrap(TRANSACTION_MESSAGE_BYTES));
        assertNotNull(transaction);
        assertTrue(transaction.isCached());

        transaction.getInputs().get(0).setSequenceNumber(transaction.getInputs().get(0).getSequenceNumber());

        bos = new ByteArrayOutputStream();
        serializer.serialize(transaction, bos);
        assertTrue(Arrays.equals(TRANSACTION_MESSAGE_BYTES, bos.toByteArray()));
    }

    /**
     * Get 1 header of the block number 1 (the first one is 0) in the chain
     */
    @Test
    public void testHeaders1() throws Exception {
        MessageSerializer serializer = MAINNET.getDefaultSerializer();

        byte[] headersMessageBytes = HEX.decode(
                        "fabfb5da" +
                        "686561646572730000000000" +
                        "6d000000" +
                        "8eef1854" +
                        "01" +
                        "0f000000" + // version
                        "0000000000000000000000000000000000000000000000000000000000000000" + // previous
                        "46c0044a9aaa9f3cbe99c004ba5e4f6dd3a68e7bf7f87730ed678ec2e2ad277c" + // merkle root
                        "0e6cd245cabb9fea3935d63f6d0e7c4b83febf7625cbbcd4149618f76eb87a2b" + // payload hash
                        "00c06d59" + // time
                        "0dd001c0" // creator
            );
        HeadersMessage headersMessage = (HeadersMessage) serializer.deserialize(ByteBuffer.wrap(headersMessageBytes));

        // The first block after the genesis
        // http://blockexplorer.com/b/1
        Block block = headersMessage.getBlockHeaders().get(0);
        assertEquals("36ef395f7d05041bd7d747b636e314cffd83aba4bcb369e5b11bc89c7e7eafeb", block.getHashAsString());
        assertNotNull(block.transactions);
        assertEquals("7c27ade2c28e67ed3077f8f77b8ea6d36d4f5eba04c099be3c9faa9a4a04c046", Utils.HEX.encode(block.getMerkleRoot().getBytes()));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        serializer.serialize(headersMessage, byteArrayOutputStream);
        byte[] serializedBytes = byteArrayOutputStream.toByteArray();
        assertArrayEquals(headersMessageBytes, serializedBytes);
    }

    /**
     * Get 6 headers of blocks 1-6 in the chain
     */
    @Test
    public void testHeaders2() throws Exception {
        MessageSerializer serializer = MAINNET.getDefaultSerializer();

        byte[] headersMessageBytes = HEX.decode("fabfb5da" +
                "686561646572730000000000" +
                "89020000" +
                "a6e3e65f" +
                "06" +
                "0f000000" + // genesis block
                        "0000000000000000000000000000000000000000000000000000000000000000" + // previous
                        "46c0044a9aaa9f3cbe99c004ba5e4f6dd3a68e7bf7f87730ed678ec2e2ad277c" + // merkle root
                        "0e6cd245cabb9fea3935d63f6d0e7c4b83febf7625cbbcd4149618f76eb87a2b" + // payload hash
                        "00c06d59" + // time
                        "0dd001c0" + // creator
                "01000000" + // block 1
                        "d7a1a0733372da7edd15527a40a925187825265dbd6ed5950d15965efa44edbe" + // previous
                        "2e650550b15c34886f559aaf0d4ccfdf5cb8a339a90f9b3b77917cb54c99b58f" + // merkle root
                        "48ffb4570c3cdab9a08b7f27e2a07ecfd9324ff6eab695520d5cf10e297efa12" + // payload hash
                        "b4c06d59" + // time
                        "0dd001c0" + // creator
                "01000000" + // block 2
                        "4f8c74360ae6a7606edee41f93947ee4f5fbdc0c14d395a80e672a8cea75db2b" + // previous
                        "79e36417d06d34469c97a7689732c72263fa69a73ce3e781564760435be4c8de" + // merkle root
                        "c857b1609aa1e7900f2546d37fac7e855c13588ebc5b14482dc8dad978a9311f" + // payload hash
                        "68c16d59" + // time
                        "0dd001c0" + // creator
                "01000000" +  // block 3
                        "f345ff66936550e3d2bc10c75974e1e8c430a1b92188e990938f85bb7216bdda" + // previous
                        "d84d3a9ca9fcee84b35c9ccab4496ede7f037b8879458a66fdfb01789f15248d" + // merkle root
                        "1beebba56c4c94e8d721a5a4d523a4f110a456f08b2460fa691f57aba76ba0ab" + // payload hash
                        "1cc26d59" + // time
                        "0dd001c0" + // creator
                "01000000" + // block 4
                        "ce2214d9768ff04db36e225064e7ac0cebc1bcc9d6dcfcebf5c5a486842fedc6" + // previous
                        "a513d4dc1702a1f59faa566d6566990ccd7fba4766b98d3d0d443a27b3aff014" + // merkle root
                        "329b54567d061f08bc2d3e215047863b1817bc3ecc109c96f02da50dde6d01d0" + // payload hash
                        "d0c26d59" + // time
                        "0dd001c0" + // creator
                "01000000" + // block 5
                        "d754e333343421a37b2814078c3479286136062396b282a93fb695e046e4214a" + // previous
                        "d742ad0d447f92e57b70e262b18a76966ec0cac2f1802dc0677478d385ab21cb" + // merkle root
                        "6d3f3a5979c1ad80b4f8724a6397dacf4d798c3b87c62ec2297890b07303a3ad" + // payload hash
                        "84c36d59" + // time
                        "0dd001c0" // creator
        );
        HeadersMessage headersMessage = (HeadersMessage) serializer.deserialize(ByteBuffer.wrap(headersMessageBytes));

        assertEquals(6, headersMessage.getBlockHeaders().size());

        // index 0 block is the number 1 block in the block chain
        // http://blockexplorer.com/b/1
        Block zeroBlock = headersMessage.getBlockHeaders().get(0);
        assertEquals("36ef395f7d05041bd7d747b636e314cffd83aba4bcb369e5b11bc89c7e7eafeb",
                zeroBlock.getHashAsString());

        // index 3 block is the number 4 block in the block chain
        // http://blockexplorer.com/b/4
        Block thirdBlock = headersMessage.getBlockHeaders().get(3);
        assertEquals("43c50fee4e4178be496e4b1736584c381884070502d66f9b647b2c55a5b45486",
                thirdBlock.getHashAsString());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        serializer.serialize(headersMessage, byteArrayOutputStream);
        byte[] serializedBytes = byteArrayOutputStream.toByteArray();
        assertArrayEquals(headersMessageBytes, serializedBytes);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testBitcoinPacketHeaderTooShort() {
        new BitcoinSerializer.BitcoinPacketHeader(ByteBuffer.wrap(new byte[]{0}));
    }

    @Test(expected = ProtocolException.class)
    public void testBitcoinPacketHeaderTooLong() {
        // Message with a Message size which is 1 too big, in little endian format.
        byte[] wrongMessageLength = HEX.decode("000000000000000000000000010000020000000000");
        new BitcoinSerializer.BitcoinPacketHeader(ByteBuffer.wrap(wrongMessageLength));
    }

    @Test(expected = BufferUnderflowException.class)
    public void testSeekPastMagicBytes() {
        // Fail in another way, there is data in the stream but no magic bytes.
        byte[] brokenMessage = HEX.decode("000000");
        MAINNET.getDefaultSerializer().seekPastMagicBytes(ByteBuffer.wrap(brokenMessage));
    }

    /**
     * Tests serialization of an unknown message.
     */
    @Test(expected = Error.class)
    public void testSerializeUnknownMessage() throws Exception {
        MessageSerializer serializer = MAINNET.getDefaultSerializer();

        Message unknownMessage = new Message() {
            @Override
            protected void parse() throws ProtocolException {
            }
        };
        ByteArrayOutputStream bos = new ByteArrayOutputStream(ADDRESS_MESSAGE_BYTES.length);
        serializer.serialize(unknownMessage, bos);
    }
}
