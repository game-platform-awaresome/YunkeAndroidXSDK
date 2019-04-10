package com.shykad.yunke.sdk.engine.permission;

import android.os.Build;
import android.view.View;


import com.shykad.yunke.sdk.R;

import java.util.Arrays;

import androidx.annotation.RequiresApi;

/**
 * Created by WanghongHe on 2018/11/12 17:38.
 * Click listener for either {@link RationaleDialogFragment} or {@link RationaleDialogFragmentCompat}.
 */

class RationaleDialogClickListener implements View.OnClickListener {

    private Object mHost;
    private RationaleDialogConfig mConfig;
    private EasyPermissions.PermissionCallbacks mCallbacks;

    RationaleDialogClickListener(RationaleDialogFragmentCompat compatDialogFragment,
                                 RationaleDialogConfig config,
                                 EasyPermissions.PermissionCallbacks callbacks) {

        mHost = compatDialogFragment.getParentFragment() != null
                ? compatDialogFragment.getParentFragment()
                : compatDialogFragment.getActivity();

        mConfig = config;
        mCallbacks = callbacks;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    RationaleDialogClickListener(RationaleDialogFragment dialogFragment,
                                 RationaleDialogConfig config,
                                 EasyPermissions.PermissionCallbacks callbacks) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mHost = dialogFragment.getParentFragment() != null ?
                    dialogFragment.getParentFragment() :
                    dialogFragment.getActivity();
        } else {
            mHost = dialogFragment.getActivity();
        }

        mConfig = config;
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_pos) {
            EasyPermissions.executePermissionsRequest(mHost, mConfig.permissions, mConfig.requestCode);

        } else if (i == R.id.btn_neg) {
            notifyPermissionDenied();

        }
    }

    private void notifyPermissionDenied() {
        if (mCallbacks != null) {
            mCallbacks.onPermissionsDenied(mConfig.requestCode,
                    Arrays.asList(mConfig.permissions));
        }
    }
}

