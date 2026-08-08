package ru.netology.nmedia

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class NMediaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}
