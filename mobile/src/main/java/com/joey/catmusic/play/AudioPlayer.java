package com.joey.catmusic.play;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player;
    private AudioCallback callback;

    public AudioPlayer() {
        player = new MediaPlayer();
        setAllListeners(player);
    }

    public void play(String url) {
        if (player == null) {
            player = new MediaPlayer();
            setAllListeners(player);
        }
        player.reset();
        try {
            player.setDataSource(url);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (player != null) player.pause();
        callback.onPause();
    }

    public void resume() {
        if (player == null) return;
        player.start();
    }

    public void stop() {
        if (player != null) player.stop();
    }

    public void release() {
        if (player != null) {
            stop();
            player.release();
        }
        if (callback != null) {
            callback = null;
        }
    }

    private void setAllListeners(MediaPlayer player) {
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (callback != null) {
            callback.onPlaying();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (callback != null) {
            callback.onError();
        }
        return false;
    }

    public interface AudioCallback{
        void onPlaying();
        void onPause();
        void onError();
    }

    public void setCallback(AudioCallback callback) {
        this.callback = callback;
    }
}
