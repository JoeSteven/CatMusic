package com.joey.catmusic.play;

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

import com.joey.catmusic.data.playlist.Playlist;
import com.joey.catmusic.data.playlist.Track;
import com.joey.catmusic.music.MusicData;

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
public class MyMusicService extends MediaBrowserServiceCompat implements AudioPlayer.AudioCallback {

    private MediaSessionCompat mSession;
    private LongSparseArray<Track> tracks = new LongSparseArray<>();
    private AudioPlayer mPlayer = new AudioPlayer();
    private Track currentTrack;

    @Override
    public void onCreate() {
        super.onCreate();

        mSession = new MediaSessionCompat(this, "MyMusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPlayer.setCallback(this);
    }


    @Override
    public void onDestroy() {
        mSession.release();
        mPlayer.release();
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
        Log.d("CatMusic", "load children:" + parentMediaId);
        if ("root".equals(parentMediaId)) {
            showPlaylist(result);
        } else {
            showTracks(parentMediaId, result);
        }
    }

    private void showTracks(String parentMediaId, Result<List<MediaItem>> result) {
        List<MediaItem> items = new ArrayList<>();
        Playlist playlist = MusicData.INSTANCE.getPlaylist(Long.parseLong(parentMediaId));
        for (Track track: playlist.getTracks()) {
            tracks.put(track.getId(), track);
            Log.d("CatMusic" , "track:" + track.getName() +" id:" + track.getId() );
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
            mPlayer.resume();
            mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING));
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            Log.d("CatMusic", "onSkipToQueueItem" + queueId);
        }

        @Override
        public void onSeekTo(long position) {
            Log.d("CatMusic", "onSeekTo" + position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d("CatMusic", "onPlayFromMediaId" + mediaId);

            Track track = tracks.get(Long.parseLong(mediaId));
            currentTrack = track;
            MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                    .putText(MediaMetadataCompat.METADATA_KEY_TITLE, track.getName())
                    .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAl().getName())
                    .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(track.getId()))
                    .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.getAl().getPicUrl())
                    .build();
            mSession.setMetadata(mediaMetadataCompat);
            mPlayer.play("http://music.163.com/song/media/outer/url?id="+track.getId()+".mp3");
            mSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_BUFFERING));
        }

        @Override
        public void onPause() {
            Log.d("CatMusic", "onPause");
            mPlayer.pause();
        }

        @Override
        public void onStop() {
            Log.d("CatMusic", "onStop");
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
            Log.d("CatMusic", "onSkipToPrevious");
            int pos = tracks.indexOfKey(currentTrack.getId());
            pos--;
            if (pos < 0) {
                pos = tracks.size()-1;
            }
            onPlayFromMediaId(String.valueOf(tracks.get(tracks.keyAt(pos)).getId()), null);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            Log.d("CatMusic", "onCustomAction" + action);
        }

        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            Log.d("CatMusic", "onPlayFromSearch" + query);
        }
    }
}
