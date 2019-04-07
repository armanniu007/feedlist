package com.armanniu.feedlist.item;

import android.content.Context;
import com.armanniu.feedlist.BaseFLItem;
import com.armanniu.feedlist.FeedItem;
import com.armanniu.feedlist.data.StringFLData;

@FeedItem("StringFLItem")
public class StringFLItem extends BaseFLItem<StringFLData> {

    public StringFLItem(Context context) {
        super(context);
    }

    @Override
    protected void onBindData(StringFLData stringFLData) throws Exception {

    }
}
