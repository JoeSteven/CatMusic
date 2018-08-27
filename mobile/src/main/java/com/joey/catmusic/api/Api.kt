package com.joey.catmusic.api

import com.joey.cheetah.core.net.NetworkCore

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
object Api {
    private const val MUSIC = 1


    fun init(){
        NetworkCore.init(1)
                .registerService(MUSIC, "https://api.imjad.cn/", MusicService::class.java)
    }

    fun music():MusicService{
        return NetworkCore.inst().service<MusicService>(MUSIC)
    }
}