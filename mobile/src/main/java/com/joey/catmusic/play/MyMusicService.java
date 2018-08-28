package com.joey.catmusic.play;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.LongSparseArray;

import com.joey.catmusic.constant.Constant;
import com.joey.catmusic.data.playlist.Playlist;
import com.joey.catmusic.data.playlist.Track;
import com.joey.catmusic.music.IMusicView;
import com.joey.catmusic.music.MusicData;
import com.joey.catmusic.music.MusicPresenter;
import com.joey.cheetah.core.global.Global;
import com.joey.cheetah.core.storage.SharedPrefHelper;
import com.joey.cheetah.core.utils.CLog;
import com.joey.cheetah.mvp.auto.Presenter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 * <p>
 * To implement a MediaBrowserService, you need to:
 * <p>
 * <ul>
 * <p>
 * <li> Extend {@link MediaBrowserServiceCompat}, implementing the media browsing
 * related methods {@link MediaBrowserServiceCompat#onGetRoot} and
 * {@link MediaBrowserServiceCompat#onLoadChildren};
 * <li> In onCreate, start a new {@link MediaSessionCompat} and notify its parent
 * with the session's token {@link MediaBrowserServiceCompat#setSessionToken};
 * <p>
 * <li> Set a callback on the {@link MediaSessionCompat#setCallback(MediaSessionCompat.Callback)}.
 * The callback will receive all the user's actions, like play, pause, etc;
 * <p>
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 * {@link android.media.MediaPlayer})
 * <p>
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * {@link MediaSessionCompat#setPlaybackState(android.support.v4.media.session.PlaybackStateCompat)}
 * {@link MediaSessionCompat#setMetadata(android.support.v4.media.MediaMetadataCompat)} and
 * {@link MediaSessionCompat#setQueue(java.util.List)})
 * <p>
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 * <p>
 * </ul>
 * <p>
 * To make your app compatible with Android Auto, you also need to:
 * <p>
 * <ul>
 * <p>
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 * <p>
 * </ul>
 */
public class MyMusicService extends MediaBrowserServiceCompat implements AudioPlayer.AudioCallback, IMusicView {

    private MediaSessionCompat mSession;
    private LongSparseArray<Track> tracks = new LongSparseArray<>();
    private AudioPlayer mPlayer = new AudioPlayer();
    private Track currentTrack;
    private MediaSessionCallback callback;
    private Playlist currentPlaylist;
    private MusicPresenter presenter;

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.forceLog(true);
        CLog.d("CatMusic", "My Service start");
        mSession = new MediaSessionCompat(this, "MyMusicService");
        setSessionToken(mSession.getSessionToken());
        callback =  new MediaSessionCallback();
        mSession.setCallback(callback);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPlayer.setCallback(this);
        presenter = new MusicPresenter(this);
        requestFocus();
        if (MusicData.INSTANCE.getPlaylists().isEmpty()) {
            presenter.init();
        } else {
            restoreData();
        }
    }

    private void requestFocus() {
        AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (manager == null) return;
        int result = manager.requestAudioFocus(focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mPlayer.stop();
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        CLog.d("CatMusic", "request focus:" + result);
    }

    @Override
    public void showList(@NotNull List<Playlist> list) {
        restoreData();
    }

    private void restoreData() {
        int trackID = SharedPrefHelper.from(Global.context()).getInt(Constant.SP_KEY_LAST_TRACK, -1);
        long playlistID = SharedPrefHelper.from(Global.context()).getLong(Constant.SP_KEY_LAST_PLAYLIST, -1);
        if (trackID != -1 && playlistID != -1) {
            currentPlaylist = MusicData.INSTANCE.getPlaylist(playlistID);
            tracks.clear();
            for (Track track: currentPlaylist.getTracks()) {
                tracks.put(track.getId(), track);
            }

           showCurrentSong(tracks.get(trackID));
        }
    }

    @Override
    public void onDestroy() {
        mSession.release();
        mPlayer.release();
        if (currentTrack != null && currentPlaylist != null) {
            SharedPrefHelper.from(Global.context())
                    .put(Constant.SP_KEY_LAST_TRACK, currentTrack.getId())
                    .apply(Constant.SP_KEY_LAST_PLAYLIST, currentPlaylist.getId());
        }
        presenter.onDestroyed(this);
    }

    private void showCurrentSong(Track track) {
        if (track == null) return;
        currentTrack = track;
        MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, track.getName())
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAl().getName())
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(track.getId()))
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getAl().getPicUrl())
                .build();
        mSession.setMetadata(mediaMetadataCompat);
        mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_STOPPED));
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints) {
        Bundle bundle = new Bundle();
        bundle.putString("xxx", "test");
        return new BrowserRoot("root", bundle);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        CLog.d("CatMusic", "load children:" + parentMediaId);
        if ("root".equals(parentMediaId)) {
            showPlaylist(result);
        } else {
            showTracks(parentMediaId, result);
        }
    }

    private void showTracks(String parentMediaId, Result<List<MediaItem>> result) {
        List<MediaItem> items = new ArrayList<>();
        currentPlaylist = MusicData.INSTANCE.getPlaylist(Long.parseLong(parentMediaId));
        tracks.clear();

        for (Track track: currentPlaylist.getTracks()) {
            tracks.put(track.getId(), track);
            CLog.d("CatMusic" , "track:" + track.getName() +" id:" + track.getId() );
            MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(track.getId()))
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getName())
                    .build();
            items.add(new MediaItem(mediaMetadataCompat.getDescription(), MediaItem.FLAG_PLAYABLE));
        }
        result.sendResult(items);
    }

    private void showPlaylist(Result<List<MediaItem>> result) {
        List<Playlist> list = MusicData.INSTANCE.getPlaylists();
        List<MediaItem> items = new ArrayList<>();
        for (Playlist playlist:list) {
            MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(playlist.getId()))
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playlist.getName())
                    .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, playlist.getCoverImgUrl())
                    .build();
            items.add(new MediaItem(mediaMetadataCompat.getDescription(), MediaItem.FLAG_BROWSABLE));
        }
        result.sendResult(items);
    }

    @Override
    public void onPlaying() {
        mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING));
    }

    @Override
    public void onError() {
        mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_ERROR));
    }

    @Override
    public void onComplete() {
        callback.onSkipToNext();
    }

    @Override
    public void onPause() {
        mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PAUSED));
    }

    private PlaybackStateCompat buildState(int state) {
       return new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY|PlaybackStateCompat.ACTION_PAUSE
                        |PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                        |PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                        |PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        |PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(state, 0, 1,
                        SystemClock.elapsedRealtime())
                .build();
    }


    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            boolean resume = false;
            if (currentTrack != null) {
                resume = mPlayer.resume("http://music.163.com/song/media/outer/url?id="+currentTrack.getId()+".mp3");
            }
            mSession.setPlaybackState(buildState(resume? PlaybackStateCompat.STATE_PLAYING: PlaybackStateCompat.STATE_BUFFERING));
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            CLog.d("CatMusic", "onSkipToQueueItem" + queueId);
        }

        @Override
        public void onSeekTo(long position) {
            CLog.d("CatMusic", "onSeekTo" + position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            CLog.d("CatMusic", "onPlayFromMediaId" + mediaId);

            Track track = tracks.get(Long.parseLong(mediaId));
            showCurrentSong(track);
            mPlayer.play("http://music.163.com/song/media/outer/url?id="+track.getId()+".mp3");
            mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_BUFFERING));
        }

        @Override
        public void onPause() {
            CLog.d("CatMusic", "onPause");
            mPlayer.pause();
        }

        @Override
        public void onStop() {
            CLog.d("CatMusic", "onStop");
            mPlayer.stop();
            mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_STOPPED));
        }

        @Override
        public void onSkipToNext() {
            int pos = tracks.indexOfKey(currentTrack.getId());
            pos++;
            if (pos > tracks.size()-1) {
                pos = 0;
            }
            onPlayFromMediaId(String.valueOf(tracks.get(tracks.keyAt(pos)).getId()), null);

        }

        @Override
        public void onSkipToPrevious() {
            CLog.d("CatMusic", "onSkipToPrevious");
            int pos = tracks.indexOfKey(currentTrack.getId());
            pos--;
            if (pos < 0) {
                pos = tracks.size()-1;
            }
            onPlayFromMediaId(String.valueOf(tracks.get(tracks.keyAt(pos)).getId()), null);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            CLog.d("CatMusic", "onCustomAction" + action);
        }

        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            CLog.d("CatMusic", "onPlayFromSearch" + query);
        }
    }

    @Override
    public void toast(String s) {

    }

    @Override
    public void toast(int i) {

    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return new Lifecycle() {
            @Override
            public void addObserver(@NonNull LifecycleObserver observer) {

            }

            @Override
            public void removeObserver(@NonNull LifecycleObserver observer) {

            }

            @NonNull
            @Override
            public State getCurrentState() {
                return null;
            }
        };
    }
}
