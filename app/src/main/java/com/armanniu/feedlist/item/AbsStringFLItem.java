package com.armanniu.feedlist.item;

import android.content.Context;
import com.armanniu.feedlist.BaseFLItem;
import com.armanniu.feedlist.FeedItem;
import com.armanniu.feedlist.data.AbstractFLData;

@FeedItem("AbsStringFLItem")
public class AbsStringFLItem<T> extends BaseFLItem<AbstractFLData<T>> {

    public AbsStringFLItem(Context context) {
        super(context);
    }

    @Override
    protected void onBindData(AbstractFLData<T> tAbstractFLData) throws Exception {

    }
}
