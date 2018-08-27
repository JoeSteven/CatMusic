package com.joey.catmusic.data.playlist
import com.google.gson.annotations.SerializedName


/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */

data class PlaylistResponse(
    @SerializedName("playlist") val playlist: Playlist,
    @SerializedName("code") val code: Int
)



