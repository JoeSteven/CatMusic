package com.joey.catmusic.music

import com.joey.catmusic.data.playlist.Playlist
import com.joey.cheetah.mvp.IView

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
interface IMusicView  : IView{
    fun showList(list: List<Playlist>)
}