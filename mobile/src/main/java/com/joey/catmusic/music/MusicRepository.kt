package com.joey.catmusic.music

import android.text.TextUtils
import com.google.gson.Gson
import com.joey.catmusic.api.Api
import com.joey.catmusic.constant.Constant
import com.joey.catmusic.data.playlist.Playlist
import com.joey.catmusic.data.playlist.PlaylistIds
import com.joey.catmusic.data.playlist.PlaylistResponse
import com.joey.cheetah.core.global.Global
import com.joey.cheetah.core.storage.SharedPrefHelper
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
class MusicRepository {

    fun refresh(ids: PlaylistIds): Single<List<Playlist>> {
        return Observable.fromIterable(ids.list)
                .flatMap { Observable
                        .concat(queryTracksFromNet(it),
                                queryTracksFromLocal(it))
                        .firstOrError()
                        .toObservable()
                        }
                .map { it.playlist }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun queryTracksFromNet(id: Long): Observable<PlaylistResponse> {
        return Api.music()
                .queryTracks(id)
                .doOnNext{ t -> cacheTracks(id, t) }
    }

    fun queryTracksFromLocal(id:Long) : Observable<PlaylistResponse> {
        return Observable.create<PlaylistResponse> {
            emitter ->
            val data = SharedPrefHelper.from(Global.context())
                    .getString(Constant.SP_KEY_TRACKS + id, "")
            if (!TextUtils.isEmpty(data)) {
                emitter.onNext(Gson().fromJson(data, PlaylistResponse::class.java))
                emitter.onComplete()
            } else {
                emitter.onError(Throwable("no cache tracks!"))
            }
        }
    }




    private fun cacheTracks(id: Long, t: PlaylistResponse) {
        val data = Gson().toJson(t)
        SharedPrefHelper.from(Global.context())
                .apply(Constant.SP_KEY_TRACKS + id, data)
    }

}
