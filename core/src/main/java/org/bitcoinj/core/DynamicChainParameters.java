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
 * Contains information about a dynamic chain parameteres
 */
public class DynamicChainParameters implements Serializable {
	private static final long serialVersionUID = -4996358818253901932L;

	private long version;

    /** chain admin signatures */
    private long minAdminSigs;
    private long maxAdminSigs;

    private long blockSpacing; // in seconds
    private long blockSpacingGracePeriod; // in seconds

    private long transactionFee; // in µFAIR
    private long dustThreshold; // in µFAIR

    /** for a node to create the next block it needs to have co-signed */
    /** the last nMinSuccessiveSignatures blocks */
    private long minSuccessiveSignatures;

    /** The number of blocks to consider for calculation of the mean number of signature */
    private long blocksToConsiderForSigCheck;

    /** minimum percentage of the number of nSignatureMean that are required to create the next block */
    private long percentageOfSignaturesMean;

    /** The maximum allowed size for a serialized block */
    private long maxBlockSize;

    /** The time (in sec.) to wait before CVNs start to create chain signatures again */
    private long blockPropagationWaitTime;

    /** If a CVN has not received all partial signatures of a set it re-tries every
     ** nRetryNewSigSetInterval sec. to create a new set without the CVN IDs that were missing*/
    private long retryNewSigSetInterval;

    /** A short description of the changes
     * A description string should be built like this:
     * #nnnnn &lt;URI to a document where the decision is documented&gt; &lt;text that describes the change&gt; */
    private String description;

	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public long getMinAdminSigs() {
		return minAdminSigs;
	}
	public void setMinAdminSigs(long minAdminSigs) {
		this.minAdminSigs = minAdminSigs;
	}
	public long getMaxAdminSigs() {
		return maxAdminSigs;
	}
	public void setMaxAdminSigs(long maxAdminSigs) {
		this.maxAdminSigs = maxAdminSigs;
	}
	public long getBlockSpacing() {
		return blockSpacing;
	}
	public void setBlockSpacing(long blockSpacing) {
		this.blockSpacing = blockSpacing;
	}
	public long getBlockSpacingGracePeriod() {
		return blockSpacingGracePeriod;
	}
	public void setBlockSpacingGracePeriod(long blockSpacingGracePeriod) {
		this.blockSpacingGracePeriod = blockSpacingGracePeriod;
	}
	public long getTransactionFee() {
		return transactionFee;
	}
	public void setTransactionFee(long transactionFee) {
		this.transactionFee = transactionFee;
	}
	public long getDustThreshold() {
		return dustThreshold;
	}
	public void setDustThreshold(long dustThreshold) {
		this.dustThreshold = dustThreshold;
	}
	public long getMinSuccessiveSignatures() {
		return minSuccessiveSignatures;
	}
	public void setMinSuccessiveSignatures(long minSuccessiveSignatures) {
		this.minSuccessiveSignatures = minSuccessiveSignatures;
	}
	public long getBlocksToConsiderForSigCheck() {
		return blocksToConsiderForSigCheck;
	}
	public void setBlocksToConsiderForSigCheck(long blocksToConsiderForSigCheck) {
		this.blocksToConsiderForSigCheck = blocksToConsiderForSigCheck;
	}
	public long getPercentageOfSignaturesMean() {
		return percentageOfSignaturesMean;
	}
	public void setPercentageOfSignaturesMean(long percentageOfSignaturesMean) {
		this.percentageOfSignaturesMean = percentageOfSignaturesMean;
	}
	public long getMaxBlockSize() {
		return maxBlockSize;
	}
	public void setMaxBlockSize(long maxBlockSize) {
		this.maxBlockSize = maxBlockSize;
	}
	public long getBlockPropagationWaitTime() {
        return blockPropagationWaitTime;
    }
    public void setBlockPropagationWaitTime(long blockPropagationWaitTime) {
        this.blockPropagationWaitTime = blockPropagationWaitTime;
    }
    public long getRetryNewSigSetInterval() {
        return retryNewSigSetInterval;
    }
    public void setRetryNewSigSetInterval(long retryNewSigSetInterval) {
        this.retryNewSigSetInterval = retryNewSigSetInterval;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s
        .append("   Dynamic chain parameters: version: ").append(version)
        .append(", minAdminSigs: ").append(minAdminSigs)
        .append(", maxAdminSigs: ").append(maxAdminSigs)
        .append(", blockSpacing: ").append(blockSpacing)
        .append(", blockSpacingGracePeriod: ").append(blockSpacingGracePeriod)
        .append(", transactionFee: ").append(transactionFee)
        .append(", dustThreshold: ").append(dustThreshold)
        .append(", minSuccessiveSignatures: ").append(minSuccessiveSignatures)
        .append(", blocksToConsiderForSigCheck: ").append(blocksToConsiderForSigCheck)
        .append(", percentageOfSignaturesMean: ").append(percentageOfSignaturesMean)
        .append(", maxBlockSize: ").append(maxBlockSize)
        .append(", blockPropagationWaitTime: ").append(blockPropagationWaitTime)
        .append(", retryNewSigSetInterval: ").append(retryNewSigSetInterval)
        .append(", description: ").append(description);

        return s.toString();
    }
}
