package com.armanniu.feedlist;

import android.content.Context;
import android.view.View;

@FeedItem("")
public class EmptyFLItem extends BaseFLItem {

    public EmptyFLItem(Context context) {
        super(context);
        setContentView(new View(context));
    }

    @Override
    protected void onBindData(FLItemData data) throws Exception {

    }
}
