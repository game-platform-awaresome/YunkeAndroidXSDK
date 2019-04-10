package com.shykad.yunke.sdk.engine.permission.config;

/**
 * Created by WanghongHe on 2018/11/13 16:42.
 * 权限请求码
 */

public class PermissionConfig {

    /**
     * 请求PHONE权限 READ_PHONE_STATE
     */
    public static final int REQUEST_PHONE_PERM = 0x110;

    /**
     * 请求LOCATION权限码 ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION
     */
    public static final int REQUEST_LOCATION_PERM = 0x120;

    /**
     * 请求STORAGE组权限
     */
    public static final int REQUEST_STORAGE_PERM = 0x130;

    /**
     * 请求安装位置应用权限
     */
    public static final int REQUEST_INSTALL_PACKAGES_PERM = 0x140;

    /**
     * 请求以上所有权限
     */
    public static final int REQUEST_PERMISSIONS_PERM = 0X140;

}
