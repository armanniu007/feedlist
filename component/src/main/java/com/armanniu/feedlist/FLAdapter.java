package com.armanniu.feedlist;

import java.lang.reflect.Type;

public final class FLAdapter {

    /**
     * 模板id
     */
    private final String mTplId;
    /**
     * 数据类型type
     */
    private final Type mType;
    /**
     * id
     */
    private final int mId;

    public FLAdapter(String tplId, Type type, int id) {
        mTplId = tplId;
        mType = type;
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public String getTplId() {
        return mTplId;
    }

    public Type getType() {
        return mType;
    }

    public interface Factory {

        Factory DEFAULT = new Factory() {

            private Factory realFactory;
            private FLAdapter defaultAdapter;

            {
                defaultAdapter = new FLAdapter("", EmptyFLItem.class, 0);
            }

            @Override
            public FLAdapter getAdapter(String tplId) {
                if (realFactory == null) {
                    return defaultAdapter;
                }
                FLAdapter adapter = realFactory.getAdapter(tplId);
                if (adapter != null) {
                    return adapter;
                }
                return defaultAdapter;
            }

            @Override
            public FLAdapter getAdapter(int id) {
                if (realFactory == null) {
                    return defaultAdapter;
                }
                FLAdapter adapter = realFactory.getAdapter(id);
                if (adapter != null) {
                    return adapter;
                }
                return defaultAdapter;
            }
        };

        /**
         * 通过tplId创建一个adapter
         *
         * @param tplId 模板id
         */
        FLAdapter getAdapter(String tplId);

        /**
         * 通过id创建一个adapter
         */
        FLAdapter getAdapter(int id);

    }
}