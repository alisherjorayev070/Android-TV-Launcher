package uz.devlooper.androidtvlauncher.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import uz.devlooper.androidtvlauncher.models.AppItemData

class AppResolver {
    companion object {
        private val launcherCategories = arrayOf(
            Intent.CATEGORY_LEANBACK_LAUNCHER,
            Intent.CATEGORY_LAUNCHER,
        )

        const val APP_ID_PREFIX = "app:"
    }

    fun getApplication(context: Context, packageId: String): AppItemData? {
        val packageManager = context.packageManager

        return launcherCategories
            .map { category ->
                val intent = Intent(Intent.ACTION_MAIN, null)
                    .addCategory(category)
                    .setPackage(packageId)
                packageManager.queryIntentActivities(intent)
            }
            .flatten()
            .distinctBy { it.activityInfo.packageName }
            .map { it.toApp(packageManager) }
            .firstOrNull()
    }

    fun getApplications(context: Context): List<AppItemData> {
        val packageManager = context.packageManager

        return launcherCategories
            .map { category ->
                val intent = Intent(Intent.ACTION_MAIN, null).addCategory(category)
                packageManager.queryIntentActivities(intent)
            }
            .flatten()
            .distinctBy { it.activityInfo.packageName }
            .map { it.toApp(packageManager) }
    }

    private fun PackageManager.queryIntentActivities(intent: Intent) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))

        else ->
            queryIntentActivities(intent, 0)
    }

    private fun ResolveInfo.toApp(packageManager: PackageManager) = AppItemData(
        id = "$APP_ID_PREFIX${activityInfo.packageName}",

        displayName = activityInfo.loadLabel(packageManager).toString(),
        packageName = activityInfo.packageName,

        launchIntentUriDefault = packageManager.getLaunchIntentForPackage(activityInfo.packageName)
            ?.toUri(0),
        launchIntentUriLeanback = packageManager.getLeanbackLaunchIntentForPackage(activityInfo.packageName)
            ?.toUri(0)
    )
}