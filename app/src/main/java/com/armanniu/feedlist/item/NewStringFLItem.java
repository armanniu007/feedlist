package com.armanniu.feedlist.item;

import android.content.Context;
import com.armanniu.feedlist.FeedItem;
import com.armanniu.feedlist.data.StringFLData;

@FeedItem("NewStringFLItem")
public class NewStringFLItem extends StringFLItem{

    public NewStringFLItem(Context context) {
        super(context);
    }

    @Override
    protected void onBindData(StringFLData stringFLData) throws Exception {

    }
}
