package uz.devlooper.androidtvlauncher.utils

import android.util.Log

inline fun <reified T> T.log(tag: String = "TV Launcher") {
    Log.d(tag, "${(T::class.java).simpleName} ${this.toString()}")
//    Timber.tag(tag).d("${(T::class.java).simpleName} ${this.toString()}")
}