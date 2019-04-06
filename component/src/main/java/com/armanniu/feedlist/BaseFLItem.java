package com.armanniu.feedlist;

import android.content.Context;
import android.os.Looper;
import android.view.View;

public abstract class BaseFLItem<T extends FLItemData> implements FLItem<T> {

    private View mContentView;
    private T mData;
    private Context mContext;


    public BaseFLItem(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContentView(View contentView) {
        this.mContentView = contentView;
    }

    @Override
    public final View getView() {
        return mContentView;
    }

    @Override
    public final void bindData(FLItemData data) {
        try {
            //noinspection unchecked
            T castData = (T) data;
            onBindData(castData);
            mData = castData;
        } catch (Exception e) {
            onBindDataFailure(data, e);
        }
    }

    @Override
    public final T getBoundData() {
        return mData;
    }

    /**
     * 绑定数据
     *
     * @param data data
     * @throws Exception 可能抛出异常
     */
    protected abstract void onBindData(T data) throws Exception;

    @SuppressWarnings("WeakerAccess")
    protected void onBindDataFailure(FLItemData data, Throwable error) {
        if (mContentView != null){
            if (Looper.getMainLooper() == Looper.myLooper()) {
                mContentView.setVisibility(View.GONE);
            } else {
                mContentView.post(() -> mContentView.setVisibility(View.GONE));
            }
        }
    }
}
