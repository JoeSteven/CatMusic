package com.joey.catmusic.api

import com.joey.catmusic.data.playlist.PlaylistResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
interface MusicService {

    @GET("cloudmusic/?type=playlist")
    fun queryTracks(@Query("id") id:Long) : Observable<PlaylistResponse>

}