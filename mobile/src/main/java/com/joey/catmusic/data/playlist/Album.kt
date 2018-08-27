package com.joey.catmusic.data.playlist

import com.google.gson.annotations.SerializedName

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
data class Album(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("picUrl") val picUrl: String,
        @SerializedName("pic") val pic: Long
)