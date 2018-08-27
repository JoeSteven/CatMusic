package com.joey.catmusic.init

import com.joey.cheetah.core.init.InitManager

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
class CatInitManager : InitManager() {
    override fun addTask() {
        add(ApiTask())
    }

}