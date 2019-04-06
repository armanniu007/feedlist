package com.armanniu.feedlist;

import android.view.View;

/**
 * feed item
 */
public interface FLItem<T extends FLItemData> {
    View getView();

    /**
     * 绑定数据
     * @param data 数据
     */
    void bindData(FLItemData data);

    /**
     * 获取绑定的数据
     * @return 绑定的数据
     */
    T getBoundData();
}
