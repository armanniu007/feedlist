package com.armanniu.feedlist.data;

import com.armanniu.feedlist.FLItemData;

public abstract class AbstractFLData<Child> implements FLItemData {
    @Override
    public int getIntegerId() {
        return 0;
    }
}
