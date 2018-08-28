package com.joey.catmusic

import android.app.Application
import android.content.Context
import android.util.Log
import com.joey.catmusic.init.CatInitManager
import com.joey.cheetah.core.CheetahApplicationInitializer

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
class CatMusicApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        CheetahApplicationInitializer.attachBaseContext(this, CatInitManager())
    }

    override fun onCreate() {
        Log.d("CatMusic", "Application start")
        CheetahApplicationInitializer.beforeSuperOnCreate()
        super.onCreate()
        CheetahApplicationInitializer.afterSuperOnCreate()
    }
}