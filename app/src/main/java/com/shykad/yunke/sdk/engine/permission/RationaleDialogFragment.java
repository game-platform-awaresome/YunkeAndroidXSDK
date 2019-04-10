package com.shykad.yunke.sdk.engine.permission;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.shykad.yunke.sdk.ui.widget.AlertYunkeDialog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


/**
 * Created by WanghongHe on 2018/11/12 17:39.
 * {@link DialogFragment} to display rationale for permission requests when the request comes from
 * a Fragment or Activity that can host a Fragment.
 */
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
public class RationaleDialogFragment extends DialogFragment {

    private EasyPermissions.PermissionCallbacks permissionCallbacks;
    private RationaleDialogConfig config;
    private RationaleDialogClickListener clickListener;

    static RationaleDialogFragment newInstance(
            @NonNull String positiveButton, @NonNull String negativeButton,
            @NonNull String rationaleMsg, int requestCode, @NonNull String[] permissions) {

        // Create new Fragment
        RationaleDialogFragment dialogFragment = new RationaleDialogFragment();

        // Initialize configuration as arguments
        RationaleDialogConfig config = new RationaleDialogConfig(
                positiveButton, negativeButton, rationaleMsg, requestCode, permissions);
        dialogFragment.setArguments(config.toBundle());

        return dialogFragment;
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // getParentFragment() requires API 17 or higher
        boolean isAtLeastJellyBeanMR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

        if (isAtLeastJellyBeanMR1
                && getParentFragment() != null
                && getParentFragment() instanceof EasyPermissions.PermissionCallbacks) {
            permissionCallbacks = (EasyPermissions.PermissionCallbacks) getParentFragment();
        } else if (context instanceof EasyPermissions.PermissionCallbacks) {
            permissionCallbacks = (EasyPermissions.PermissionCallbacks) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        permissionCallbacks = null;
    }

    @NonNull
    @Override
    public AlertYunkeDialog onCreateDialog(Bundle savedInstanceState) {
        // Rationale dialog should not be cancelable
        setCancelable(false);

        // Get config from arguments, create click listener
        config = new RationaleDialogConfig(getArguments());
        clickListener = new RationaleDialogClickListener(this, config, permissionCallbacks);

        // Create an AlertDialog
        return config.createDialog(getActivity(), clickListener);
    }

}