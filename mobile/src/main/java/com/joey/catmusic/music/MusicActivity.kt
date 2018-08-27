package com.joey.catmusic.music

import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.joey.catmusic.R
import com.joey.catmusic.data.playlist.Playlist
import com.joey.catmusic.data.playlist.Track
import com.joey.catmusic.music.Adpater.PlaylistItemViewBinder
import com.joey.catmusic.play.AudioPlayer
import com.joey.cheetah.core.list.CheetahAdapter
import com.joey.cheetah.mvp.AbsActivity
import com.joey.cheetah.mvp.auto.Presenter
import kotlinx.android.synthetic.main.activity_music.*


class MusicActivity : AbsActivity(), IMusicView {


    @Presenter
    private val presenter: MusicPresenter = MusicPresenter(this)
    private val adapter : CheetahAdapter = CheetahAdapter()

    override fun initLayout(): Int {
        return R.layout.activity_music
    }

    override fun initPresenter() {
    }

    override fun initView() {
        btnAdd.setOnClickListener { presenter.addPlaylist(etPlaylist.text.toString()) }
        btnRefresh.setOnClickListener { presenter.refresh() }
        adapter.register(Playlist::class.java, PlaylistItemViewBinder().setOnLongClickListener{_, playlist ->  presenter.deletePlaylist(playlist.id)
             true
        })
        rvSuccess.layoutManager = LinearLayoutManager(this)
        rvSuccess.adapter = adapter
    }

    override fun showList(list: List<Playlist>) {
        adapter.items = list
        adapter.notifyDataSetChanged()
    }

}
