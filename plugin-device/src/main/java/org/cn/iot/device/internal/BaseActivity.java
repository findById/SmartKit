package org.cn.iot.device.internal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by chenning on 2016/8/24.
 */
public class BaseActivity extends AppCompatActivity {

    protected ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        super.onDestroy();
    }

    protected void showLoading(String title, String message, boolean cancelable) {
        showLoading(title, message, cancelable, null);
    }

    protected void showLoading(String title, String message, boolean cancelable, DialogInterface.OnCancelListener listener) {
        hideLoading();
        mProgressDialog = ProgressDialog.show(this, title, message, true, cancelable, listener);
    }

    protected void hideLoading() {
        if (mProgressDialog != null && !isFinishing() && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
