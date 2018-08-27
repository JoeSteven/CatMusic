package com.joey.catmusic.music

import android.util.LongSparseArray
import com.joey.catmusic.data.playlist.Playlist

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
object MusicData{
    private var playlistMap:LongSparseArray<Playlist> = LongSparseArray()
    private var playlists:List<Playlist> = ArrayList()

    fun setList(list: List<Playlist>) {
        playlists = list
        for (playlist in list) {
            playlistMap.append(playlist.id, playlist)
        }
    }

    fun getPlaylists() : List<Playlist>{
        return playlists
    }

    fun getPlaylist(id:Long) : Playlist{
        return playlistMap[id]
    }

    fun deletePlaylist(id: Long) {
        playlistMap.delete(id)
    }

    fun clearPlaylist() {
        playlistMap.clear()
    }

}