package com.hp.jetadvantage.link.pkgmgt.utils;

import android.content.pm.PackageManager;
import android.os.UserHandle;

import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemCall {
    public static final String FUNCTION_NAME_GRANT_RUNTIME_PERMISSION = "grantRuntimePermission";

    public static void grantRuntimePermission(String packageName, String permission, UserHandle userHandle) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PackageManager pm = PackageManagerApplication.getAppContext().getPackageManager();
        Method grantRuntimePermissionMethod = PackageManager.class.getDeclaredMethod(
                FUNCTION_NAME_GRANT_RUNTIME_PERMISSION, String.class, String.class, android.os.UserHandle.class
        );
        grantRuntimePermissionMethod.setAccessible(true);
        grantRuntimePermissionMethod.invoke(pm, packageName, permission, userHandle);
    }
}
