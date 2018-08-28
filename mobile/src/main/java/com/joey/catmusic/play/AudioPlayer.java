package com.joey.catmusic.play;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Description:
 * author:Joey
 * date:2018/8/27
 */
public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private AudioCallback callback;
    private boolean prepared;

    public AudioPlayer() {
        player = new MediaPlayer();
        setAllListeners(player);
    }

    public void play(String url) {
        prepared = false;
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
        if (player == null) return;
        if (prepared && player.isPlaying()){
            player.pause();
        }else {
            stop();
        }
        callback.onPause();
    }

    public boolean resume(String url) {
        if (player!= null && prepared) {
            player.start();
            return true;
        } else {
            play(url);
            return false;
        }
    }

    public void stop() {
        if (player != null && prepared){
            player.stop();
        }
        prepared = false;
    }

    public void release() {
        if (player != null) {
            stop();
            player.release();
        }
        if (callback != null) {
            callback = null;
        }
        prepared = false;
    }

    private void setAllListeners(MediaPlayer player) {
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (callback != null) {
            callback.onComplete();
        }
    }

    public interface AudioCallback{
        void onPlaying();
        void onPause();
        void onError();
        void onComplete();
    }

    public void setCallback(AudioCallback callback) {
        this.callback = callback;
    }
}
