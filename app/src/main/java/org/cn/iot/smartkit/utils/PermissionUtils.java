package org.cn.iot.smartkit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import org.cn.iot.smartkit.CoreApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PermissionUtils {
    public static final String TAG = "PermissionUtils";
    //安全的reqCode
    private static AtomicInteger integer = new AtomicInteger(0);
    //保存了reqCode 对应的 请求实体
    private static final SparseArray<RequestPermissionEntity> PERMISSION_MAP = new SparseArray<>(7);

    //这里需要 获得 ApplicationContext
    private static final Context aContext = CoreApplication.getInstance().getApplicationContext(); //ApplicationContext

    public static void requestPermissions(OnPermissionsCallback onPermissionsCallback, String... manifestPermissions) {
        requestPermissions(null, onPermissionsCallback, manifestPermissions);
    }

    public static void requestPermissions(Activity activity, OnPermissionsCallback onPermissionsCallback, String... manifestPermissions) {
        if (manifestPermissions == null || manifestPermissions.length <= 0) {
            return;
        }
        if (checkSelfPermission(manifestPermissions).length == 0) {
            //都已经授权了
            if (onPermissionsCallback != null) {
                boolean[] showRequestRationale = new boolean[manifestPermissions.length];
                Arrays.fill(showRequestRationale, false);

                int[] grantResult = new int[manifestPermissions.length];
                Arrays.fill(grantResult, PackageManager.PERMISSION_GRANTED);

                onPermissionsCallback.onRequestPermissionsResult(true, manifestPermissions, grantResult, showRequestRationale);
            }
        } else {
            //没有授权
            RequestPermissionEntity entity = new RequestPermissionEntity(onPermissionsCallback, manifestPermissions);
            PERMISSION_MAP.put(entity.reqCode, entity);
            entity.requestPermissions(activity);
        }
    }


    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        RequestPermissionEntity entity = PERMISSION_MAP.get(requestCode);
        if (entity != null) {
            boolean success = true;
            boolean[] showRequestRationales = new boolean[permissions.length]; //用户勾选 不再提示

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i]; //本次权限
                int grantResult = i >= grantResults.length ? PackageManager.PERMISSION_DENIED : grantResults[i]; //当前权限的状态
                boolean showRequestRationale = false; //是否需要解释 授权理由

                if (grantResult != PackageManager.PERMISSION_GRANTED) { //没有授权
                    success = false;
                    Activity activity = getActivity();
                    if (checkActivity(activity)) {
                        showRequestRationale = !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
                    }
                }
                showRequestRationales[i] = showRequestRationale;
            }

            if (entity.callback != null) {
                entity.callback.onRequestPermissionsResult(success, permissions, grantResults, showRequestRationales);
            }

            PERMISSION_MAP.remove(requestCode);
        } else {
            return false;
        }
        return true;
    }

    /**
     * 检查 是否授权
     *
     * @param manifestPermission
     * @return
     */
    public static boolean checkSelfPermission(String manifestPermission) {
        return ContextCompat.checkSelfPermission(aContext, manifestPermission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 批量检查是否授权
     *
     * @param manifestPermissions
     * @return 返回没有授权的 权限
     */
    public static String[] checkSelfPermission(String... manifestPermissions) {
        List<String> list = new ArrayList<>(3);
        for (int i = 0, len = manifestPermissions == null ? 0 : manifestPermissions.length; i < len; i++) {
            if (!checkSelfPermission(manifestPermissions[i])) {
                list.add(manifestPermissions[i]);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static Activity getActivity() {
        Activity activity = CoreApplication.getInstance().getTopActivity();
        return activity;
    }


    private static boolean checkActivity(Activity activity) {
        return activity != null;
    }

    private static void handlerNoActivity(RequestPermissionEntity entity) {
        if (entity != null) {
            OnPermissionsCallback callback = entity.callback;
            if (callback != null) {
                int[] grantResult = new int[entity.getPermission().length];
                Arrays.fill(grantResult, PackageManager.PERMISSION_DENIED);

                boolean[] showRequestRationale = new boolean[entity.getPermission().length];
                Arrays.fill(showRequestRationale, false);

                callback.onRequestPermissionsResult(false, entity.getPermission(), grantResult, showRequestRationale);
            }
            PERMISSION_MAP.remove(entity.reqCode);
        }
    }

    public static void toAppSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri aPackage = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        Uri aPackage = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(aPackage);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 请求权限回调接口
     */
    public interface OnPermissionsCallback {
        /**
         * 请求权限 回调
         *
         * @param success              权限是否成功
         * @param permission           当前申请的权限
         * @param grantResult          当前的请求结果
         * @param showRequestRationale 权限被拒后 是否显示 权限申请理由
         */
        void onRequestPermissionsResult(boolean success, String[] permission, int[] grantResult, boolean[] showRequestRationale);
    }


    /**
     * 请求权限实体
     */
    private static class RequestPermissionEntity {
        final int reqCode;
        String[] permissions;
        private OnPermissionsCallback callback;


        public RequestPermissionEntity() {
            this(null, null);
        }


        public RequestPermissionEntity(OnPermissionsCallback callback, String[] permissions) {
            this.permissions = permissions;
            this.callback = callback;
            reqCode = integer.getAndIncrement();
        }

        public String[] getPermission() {
            return permissions;
        }


        /**
         * 请求权限
         */
        public void requestPermissions(Activity activity) {
            if (permissions == null || permissions.length <= 0) {
                Log.d(TAG, "requestPermissions: permissions == null || permissions.length <= 0");
                PERMISSION_MAP.remove(reqCode);
                return;
            }
            if (activity == null) {
                activity = getActivity();
            }
            if (checkActivity(activity)) {
                ActivityCompat.requestPermissions(activity, permissions, reqCode);
            } else {
                handlerNoActivity(this);
            }
        }
    }
}


