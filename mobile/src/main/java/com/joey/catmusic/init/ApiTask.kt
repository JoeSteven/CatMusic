package com.joey.catmusic.init

import com.joey.catmusic.api.Api
import com.joey.cheetah.core.init.InitTask

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
class ApiTask : InitTask() {
    override fun execute() {
        Api.init()
    }

    override fun priority(): Priority {
        return Priority.EMERGENCY
    }
}