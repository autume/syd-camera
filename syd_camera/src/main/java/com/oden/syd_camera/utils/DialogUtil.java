package com.oden.syd_camera.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.oden.syd_camera.R;

/**
 * Created by syd on 2017/6/20.
 */

public class DialogUtil {

    public static void showPermissionDeniedDialog(final Activity context, String str) {
        new AlertDialog.Builder(context).setTitle("获取" + str + "权限被禁用")
                .setMessage("请在 设置-应用管理-" + context.getString(R.string.app_name) + "-权限管理 (将" + str + "权限打开)")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                        //锤子手机中，应用管理中没权限设置，而是在安全中心
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        intent.setData(Uri.parse("package:" + context.getPackageName()));
//                        context.startActivity(intent);
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static void showPermissionRemindDiaog(final Activity context, String str, final String[] deniedPermissions, final int requestCode) {
        new AlertDialog.Builder(context).setTitle("温馨提示")
                .setMessage("我们需要" + str + "才能正常使用该功能")
                .setNegativeButton("取消", null)
                .setPositiveButton("验证权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        XPermissionUtils.requestPermissionsAgain(context, deniedPermissions,
                                requestCode);
                    }
                })
                .show();
    }



}