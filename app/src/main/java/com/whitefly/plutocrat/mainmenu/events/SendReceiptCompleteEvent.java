package com.whitefly.plutocrat.mainmenu.events;

import android.support.annotation.Nullable;

import com.whitefly.plutocrat.models.IAPPurchaseModel;
import com.whitefly.plutocrat.models.MetaModel;

/**
 * Created by satjapotiamopas on 6/22/16 AD.
 */
public class SendReceiptCompleteEvent {
    public enum State {
        Succeed, Failed, Duplicated
    }

    private State mState;
    private IAPPurchaseModel mPurchasedItem;
    private MetaModel mMetaError;

    public State getState() {
        return mState;
    }

    public IAPPurchaseModel getPurchasedItem() {
        return mPurchasedItem;
    }

    public MetaModel getMetaError() {
        return mMetaError;
    }

    public SendReceiptCompleteEvent(State state, IAPPurchaseModel item, @Nullable MetaModel metaError) {
        mState = state;
        mPurchasedItem = item;
        mMetaError = metaError;
    }
}
