package com.joey.catmusic

import android.app.Application
import android.content.Context
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
        CheetahApplicationInitializer.beforeSuperOnCreate()
        super.onCreate()
        CheetahApplicationInitializer.afterSuperOnCreate()
    }
}