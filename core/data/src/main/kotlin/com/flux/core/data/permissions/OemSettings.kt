package com.flux.core.data.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Brand screens that are not part of Android. Each list is tried in order and the first one the
 * phone has is opened; null means this phone has none (the step is not shown). The packages are
 * declared in the manifest's <queries> so they are visible on Android 11+.
 */
object OemSettings {
    private val AUTOSTART = listOf(
        "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
        "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
        "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager",
        "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
        "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
        "com.oplus.safecenter" to "com.oplus.safecenter.permission.startup.StartupAppListActivity",
        "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
        "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
        "com.samsung.android.lool" to "com.samsung.android.sm.battery.ui.BatteryActivity",
    )

    private const val MIUI_PERMISSION_EDITOR = "miui.intent.action.APP_PERM_EDITOR"
    private const val MIUI_PACKAGE_EXTRA = "extra_pkgname"
    private const val VIVO_PACKAGE = "com.vivo.permissionmanager"
    private const val VIVO_DETAIL = "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"
    private const val VIVO_PACKAGE_EXTRA = "packagename"
    private const val OPPO_PACKAGE = "com.coloros.safecenter"
    private const val OPPO_FLOATING = "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"

    fun autostartIntent(context: Context): Intent? =
        AUTOSTART.map { (pkg, cls) -> Intent().setComponent(ComponentName(pkg, cls)) }.firstOrNull { context.canOpen(it) }

    /** Pop-up windows and "show on lock screen" (per app, inside the brand's permission screen). */
    fun popupsIntent(context: Context): Intent? = listOf(
        Intent(MIUI_PERMISSION_EDITOR).putExtra(MIUI_PACKAGE_EXTRA, context.packageName),
        Intent().setComponent(ComponentName(VIVO_PACKAGE, VIVO_DETAIL)).putExtra(VIVO_PACKAGE_EXTRA, context.packageName),
        Intent().setComponent(ComponentName(OPPO_PACKAGE, OPPO_FLOATING)),
    ).firstOrNull { context.canOpen(it) }

    private fun Context.canOpen(intent: Intent): Boolean =
        packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
}
