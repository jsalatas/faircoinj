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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Arrays;

import com.google.common.primitives.Ints;

/**
 * A SchnorrNonce just wraps a byte[] so that equals and hashcode work correctly, allowing it to be used as keys in a
 * map. It also checks that the length is correct and provides a bit more type safety.
 */
public class SchnorrNonce implements Serializable, Comparable<SchnorrNonce> {
    private static final long serialVersionUID = -2794857887601012955L;

    public static final int LENGTH = 64; // bytes
    public static final SchnorrNonce ALL_ZERO = wrap(new byte[LENGTH]);

    private final byte[] bytes;

    /**
     * Use {@link #wrap(byte[])} instead.
     */
    private SchnorrNonce(byte[] rawSigBytes) {
        checkArgument(rawSigBytes.length == LENGTH);
        this.bytes = rawSigBytes;
    }

    /**
     * Use {@link #wrap(String)} instead.
     */
    private SchnorrNonce(String hexString) {
        checkArgument(hexString.length() == LENGTH * 2);
        this.bytes = Utils.HEX.decode(hexString);
    }

    /**
     * Creates a new instance that wraps the given hash value.
     *
     * @param rawSigBytes the raw hash bytes to wrap
     * @return a new instance
     * @throws IllegalArgumentException if the given array length is not exactly 32
     */
    public static SchnorrNonce wrap(byte[] rawSigBytes) {
        return new SchnorrNonce(rawSigBytes);
    }

    /**
     * Creates a new instance that wraps the given hash value (represented as a hex string).
     *
     * @param hexString a hash value represented as a hex string
     * @return a new instance
     * @throws IllegalArgumentException if the given string is not a valid
     *         hex string, or if it does not represent exactly 32 bytes
     */
    public static SchnorrNonce wrap(String hexString) {
        return wrap(Utils.HEX.decode(hexString));
    }

    public static SchnorrNonce wrapReversed(byte[] rawSigBytes) {
        return wrap(Utils.reverseBytes(rawSigBytes));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(bytes, ((SchnorrNonce)o).bytes);
    }

    /**
     * Returns the last four bytes of the wrapped hash. This should be unique enough to be a suitable hash code even for
     * blocks, where the goal is to try and get the first bytes to be zeros (i.e. the value as a big integer lower
     * than the target value).
     */
    @Override
    public int hashCode() {
        // Use the last 4 bytes, not the first 4 which are often zeros in Bitcoin.
        return Ints.fromBytes(bytes[LENGTH - 4], bytes[LENGTH - 3], bytes[LENGTH - 2], bytes[LENGTH - 1]);
    }

    @Override
    public String toString() {
        return Utils.HEX.encode(bytes);
    }

    /**
     * Returns the internal byte array, without defensively copying. Therefore do NOT modify the returned array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Returns a reversed copy of the internal byte array.
     */
    public byte[] getReversedBytes() {
        return Utils.reverseBytes(bytes);
    }

    @Override
    public int compareTo(final SchnorrNonce other) {
        for (int i = LENGTH - 1; i >= 0; i--) {
            final int thisByte = this.bytes[i] & 0xff;
            final int otherByte = other.bytes[i] & 0xff;
            if (thisByte > otherByte)
                return 1;
            if (thisByte < otherByte)
                return -1;
        }
        return 0;
    }
}
