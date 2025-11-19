package uz.devlooper.androidtvlauncher

import android.app.Application
import uz.devlooper.androidtvlauncher.services.SharedPreference

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPreference.init(this)
    }

}