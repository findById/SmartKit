package org.cn.plugin.message.utils;

import android.content.Context;

import org.cn.plugin.message.model.Message;

import info.breezes.orm.SimpleOrmSQLiteHelper;

public class OrmHelper {
    private static final String DATABASE_NAME = "iot_message.db";
    private static final int DATABASE_VERSION = 1;
    private static OrmHelper ormHelper;

    private Context mContext;

    private static SimpleOrmSQLiteHelper sqLiteHelper;

    public OrmHelper(Context ctx) {
        mContext = ctx;
        sqLiteHelper = new SimpleOrmSQLiteHelper(ctx, DATABASE_NAME, DATABASE_VERSION, Message.class);
    }

    public static void init(Context ctx) {
        ormHelper = new OrmHelper(ctx);
    }

    public static SimpleOrmSQLiteHelper getInstance() {
        if (ormHelper != null) {
            return sqLiteHelper;
        }
        return null;
    }
}
