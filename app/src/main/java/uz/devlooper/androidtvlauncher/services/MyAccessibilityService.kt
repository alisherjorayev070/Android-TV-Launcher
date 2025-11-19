package uz.devlooper.androidtvlauncher.services

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import uz.devlooper.androidtvlauncher.MainActivity
import uz.devlooper.androidtvlauncher.log

class MyAccessibilityService : AccessibilityService() {

    private val TAG: String = "MyAccessibilityService"

    private val validHomeSources = setOf(
        "com.android.systemui",        // Stock Android
        "com.android.tvlauncher",        // Android
        "com.google.android.tvlauncher", // Google TV
        "com.google.android.apps.tv.launcherx", // Google TV
        "com.sony.dtv.homelauncher",    // Sony Bravia
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        "action=${event?.action}  ${event?.eventType}  ${event?.source?.viewIdResourceName}  ${event?.packageName}"
            .log(TAG)

        event?.run {
            when (eventType) {
                AccessibilityEvent.TYPE_VIEW_CLICKED -> handleViewClick()
                AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> handleGesture()
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = event.packageName?.toString()
                    if (isSystemLauncher(packageName)) {
                        launchOurLauncher()
                    }
                }

                else -> Unit
            }
        }
    }

    override fun onInterrupt() {

    }

    private fun launchOurLauncher() {
        if (!SharedPreference.isLauncherByUser) return

        try {
            val result = startActivity(homeIntent)
            if (result == null) {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                startActivity(launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        } catch (e: Exception) {
            // Handle exception
        }
    }


    private val homeIntent by lazy {
        Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
        }
    }

    private fun AccessibilityEvent.handleViewClick() {
        val source = source ?: return
        val isHomeButton = source.viewIdResourceName?.let { id ->
            id.contains("home", true) || validHomeSources.contains(packageName?.toString())
        } ?: false

        if (isHomeButton) {
            launchOurLauncher()
        }
        source.recycle()
    }

    private fun AccessibilityEvent.handleGesture() {
        if (contentChangeTypes.and(AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED) != 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                launchOurLauncher()
            }, 50) // Bypass system animation
        }
    }

    // Add key event handling for physical buttons
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_HOME && SharedPreference.isLauncherByUser) {
            Handler(Looper.getMainLooper()).postDelayed({
                launchOurLauncher()
            }, 30)
            return true
        }
        return super.onKeyEvent(event)
    }

    private fun isSystemLauncher(pkg: CharSequence?): Boolean {
        val systemLaunchers = setOf(
            "com.google.android.tvlauncher",
            "com.google.android.apps.tv.launcherx",
            "com.android.tvlauncher",
            "com.sony.dtv.homelauncher"
        )
        return systemLaunchers.contains(pkg?.toString())
    }

}

fun Context.isAccessibilityServiceEnabled(
    serviceClass: Class<out AccessibilityService>
): Boolean {
    val expectedComponent = ComponentName(this, serviceClass).flattenToString()

    val enabledServices = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
}
