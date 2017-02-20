package org.cn.iot.device.internal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by chenning on 2016/8/24.
 */
public class BaseFragment extends Fragment {

    protected ProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        hideLoading();
        super.onDetach();
    }

    protected void showLoading(String title, String message, boolean cancelable) {
        showLoading(title, message, cancelable, null);
    }

    protected void showLoading(String title, String message, boolean cancelable, DialogInterface.OnCancelListener listener) {
        hideLoading();
        mProgressDialog = ProgressDialog.show(getActivity(), title, message, true, cancelable, listener);
    }

    protected void hideLoading() {
        if (mProgressDialog != null && !isDetached() && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
