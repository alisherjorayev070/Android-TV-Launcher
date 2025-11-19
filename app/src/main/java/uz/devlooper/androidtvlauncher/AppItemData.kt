package uz.devlooper.androidtvlauncher

data class AppItemData(
    val id: String,
    val displayName: String,
    val packageName: String,
    val launchIntentUriDefault: String?,
    val launchIntentUriLeanback: String?
)
