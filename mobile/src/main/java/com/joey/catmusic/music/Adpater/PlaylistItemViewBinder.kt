package com.joey.catmusic.music.Adpater

import android.view.View
import android.widget.TextView
import com.joey.catmusic.R
import com.joey.catmusic.data.playlist.Playlist
import com.joey.cheetah.core.list.AbsItemViewBinder
import com.joey.cheetah.core.list.AbsViewHolder

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
class PlaylistItemViewBinder : AbsItemViewBinder<Playlist, PlaylistItemViewBinder.PlaylistViewHolder>() {
    override fun createViewHolder(itemView: View): PlaylistViewHolder {
        return PlaylistViewHolder(itemView)
    }

    override fun layout(): Int {
        return R.layout.item_playlist
    }

    override fun onBind(holder: PlaylistViewHolder, playlist: Playlist) {
        if (holder.itemView is TextView) holder.itemView.text = playlist.name
    }

    inner class PlaylistViewHolder(itemView: View) : AbsViewHolder<Playlist>(itemView)
}