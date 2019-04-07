package com.armanniu.feedlist;

import android.content.Context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class FLAdapter<T extends FLItemData> {

    public static FLAdapter DEFAULT = new FLAdapter<EmptyData>("") {
        @Override
        public FLItem create(Context context) {
            return new EmptyFLItem(context);
        }
    };

    private final String mTplId;
    private final Class<T> mType;

    protected FLAdapter(String tplId) {
        mTplId = tplId;
        Type genType = getClass().getGenericSuperclass();
        //noinspection unchecked,ConstantConditions
        mType = (Class<T>) ((ParameterizedType) genType).getActualTypeArguments()[0];
    }

    public abstract FLItem create(Context context);

    public final String getTplId() {
        return mTplId;
    }

    public final Class<T> getType() {
        return mType;
    }

    public static FLAdapter getAdater(String tplId) {
        return DEFAULT;
    }
}