package com.joey.catmusic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Description:
 * author:Joey
 * date:2018/8/28
 */
class CatMusicReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("CatMusic", "action:" + intent?.action)
    }
}