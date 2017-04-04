package org.bitcoinj.core.listeners;

import org.bitcoinj.core.Coin;

public interface TransactionFeeChangedListener {
    void onTransactionFeeChanged(final Coin transactionFee);
}
