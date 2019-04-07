package com.armanniu.feedlist.item;

import android.content.Context;
import com.armanniu.feedlist.BaseFLItem;
import com.armanniu.feedlist.FeedItem;
import com.armanniu.feedlist.data.AbstractFLData;
import com.armanniu.feedlist.data.NewStringFLData;

@FeedItem("NewAbsStringFLItem")
public class NewAbsStringFLItem extends AbsStringFLItem {

    public NewAbsStringFLItem(Context context) {
        super(context);
    }

    @Override
    protected void onBindData(AbstractFLData<String> stringAbstractFLData) throws Exception {

    }
}
