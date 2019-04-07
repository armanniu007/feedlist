package com.armanniu.feedlist.item;

import android.content.Context;
import com.armanniu.feedlist.BaseFLItem;
import com.armanniu.feedlist.FeedItem;
import com.armanniu.feedlist.data.AbstractFLData;

@FeedItem("AbsStringFLItem")
public class AbsStringFLItem extends BaseFLItem<AbstractFLData<String>> {

    public AbsStringFLItem(Context context) {
        super(context);
    }

    @Override
    protected void onBindData(AbstractFLData<String> stringAbstractFLData) throws Exception {

    }
}
