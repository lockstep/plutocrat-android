package com.whitefly.plutocrat.login.events;

import com.whitefly.plutocrat.models.MetaModel;

/**
 * Created by Satjapot on 6/23/16 AD.
 */
public class ResetPassword1ErrorEvent {
    private MetaModel mMetaModel;

    public MetaModel getMetaModel() {
        return mMetaModel;
    }

    public ResetPassword1ErrorEvent(MetaModel model) {
        mMetaModel = model;
    }
}
