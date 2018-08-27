package com.joey.catmusic.music

import android.os.Bundle
import com.google.gson.Gson
import com.joey.catmusic.constant.Constant
import com.joey.catmusic.data.playlist.PlaylistIds
import com.joey.cheetah.core.global.Global
import com.joey.cheetah.core.storage.SharedPrefHelper
import com.joey.cheetah.mvp.AbsPresenter

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
class MusicPresenter(view: IMusicView) : AbsPresenter<IMusicView>(view) {
    private val musicRepo = MusicRepository()
    private var playlistIds = PlaylistIds(ArrayList())

    override fun onSaveData(p0: Bundle?) {
    }

    override fun onRestoredData(p0: Bundle?) {
    }

    fun init() {
        val ids = Gson().fromJson(SharedPrefHelper.from(Global.context())
                .getString(Constant.SP_KEY_PLAYLIST, ""), PlaylistIds::class.java)
        if (ids?.list != null) playlistIds = ids
        refresh()
    }

    fun refresh() {
        if ( playlistIds.list.isEmpty()){
            mView.toast("add playlist first")
            mView.showList(ArrayList())
            return
        }
        add(musicRepo.refresh(playlistIds)
                .doOnSuccess { list -> MusicData.setList(list) }
                .subscribe({ list -> mView.showList(list) },
                        { e -> mView.toast("error $e") }))
    }

    fun addPlaylist(playlistId: String) {
        playlistIds.list.add(playlistId.toLong())
        SharedPrefHelper.from(Global.context())
                .apply(Constant.SP_KEY_PLAYLIST, Gson().toJson(playlistIds))
        refresh()
    }

    fun deletePlaylist(playlistId: Long) {
        playlistIds.list.remove(playlistId)
        SharedPrefHelper.from(Global.context())
                .apply(Constant.SP_KEY_PLAYLIST, Gson().toJson(playlistIds))
        refresh()
    }


}