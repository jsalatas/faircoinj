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

import static org.bitcoinj.core.Utils.uint32ToByteStreamLE;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Represents an "addr" message on the P2P network, which contains broadcast IP addresses of other peers. This is
 * one of the ways peers can find each other without using the DNS or IRC discovery mechanisms. However storing and
 * using addr messages is not presently implemented.</p>
 *
 * <p>Instances of this class are not safe for use by multiple threads.</p>
 */
public class NoncePoolMessage extends Message {
    private long cvnId;
    private Sha256Hash hashRootBlock;
    private List<SchnorrNonce> nonces;
    private long creationTime;
    private SchnorrSignature msgSig;

    /**
     * Contruct a new 'addr' message.
     * @param params NetworkParameters object.
     * @param offset The location of the first payload byte within the array.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    NoncePoolMessage(NetworkParameters params, byte[] payload, int offset, MessageSerializer setSerializer, int length) throws ProtocolException {
        super(params, payload, offset, setSerializer, length);
    }

    /**
     * Contruct a new 'addr' message.
     * @param params NetworkParameters object.
     * @param serializer the serializer to use for this block.
     * @param length The length of message if known.  Usually this is provided when deserializing of the wire
     * as the length will be provided as part of the header.  If unknown then set to Message.UNKNOWN_LENGTH
     * @throws ProtocolException
     */
    NoncePoolMessage(NetworkParameters params, byte[] payload, MessageSerializer serializer, int length) throws ProtocolException {
        super(params, payload, 0, serializer, length);
    }

    NoncePoolMessage(NetworkParameters params, byte[] payload, int offset) throws ProtocolException {
        super(params, payload, offset, params.getDefaultSerializer(), UNKNOWN_LENGTH);
    }

    NoncePoolMessage(NetworkParameters params, byte[] payload) throws ProtocolException {
        super(params, payload, 0, params.getDefaultSerializer(), UNKNOWN_LENGTH);
    }

    @Override
    protected void parse() throws ProtocolException {
        cvnId = readUint32();
        hashRootBlock = readHash();
        creationTime = readUint32();

        long numNonces = readVarInt();

        nonces = new ArrayList<>((int) numNonces);
        for (int i = 0; i < numNonces; i++) {
            SchnorrNonce nonce = readNonce();
            nonces.add(nonce);
        }

        msgSig = readSignature();

        length = cursor;
    }

    @Override
    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        uint32ToByteStreamLE(cvnId, stream);
        stream.write(hashRootBlock.getReversedBytes());

        stream.write(new VarInt(nonces.size()).encode());
        for (SchnorrNonce nonce : nonces)
            stream.write(nonce.getReversedBytes());

        uint32ToByteStreamLE(creationTime, stream);
        stream.write(msgSig.getReversedBytes());
    }

    /**
     * @return An unmodifiableList view of the backing List of nonces.  Addresses contained within the list may be safely modified.
     */
    public List<SchnorrNonce> getNonces() {
        return Collections.unmodifiableList(nonces);
    }

    public void addNonce(SchnorrNonce nonce) {
        unCache();
        nonces.add(nonce);
        if (length == UNKNOWN_LENGTH)
            getMessageSize();
        else
            length += 64;
    }

    public void removeNonce(int index) {
        unCache();
        nonces.remove(index);
        if (length == UNKNOWN_LENGTH)
            getMessageSize();
        else
            length -= 64;
    }

//    @Override
//    public String toString() {
//        return nonces.size() + " nonces: " + Utils.join(nonces);
//    }
}
