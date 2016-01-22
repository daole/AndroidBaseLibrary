package com.dreamdigitizers.androidbaselibrary.views.classes.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.dreamdigitizers.androidbaselibrary.utils.UtilsMediaPlayer;

public abstract class OldServiceMediaPlayer extends Service implements AudioManager.OnAudioFocusChangeListener, UtilsMediaPlayer.CustomMediaPlayer.IOnMediaPlayerActionResultListener {
    protected static final String TAG__WIFI_LOCK = "UtilsMediaPlayer.WifiLock";

    protected static final int NOTIFICATION_ID = 1;
    protected static final int REQUEST_CODE = 0;

    protected static final int STATE__PREPARING = 0;
    protected static final int STATE__PLAYING = 1;
    protected static final int STATE__PAUSED = 2;
    protected static final int STATE__STOPPED = 3;

    protected static final int AUDIO_FOCUS__NO_FOCUS_NO_DUCK = 0;
    protected static final int AUDIO_FOCUS__NO_FOCUS_CAN_DUCK = 1;
    protected static final int AUDIO_FOCUS__FOCUSED = 2;

    public static final float VOLUME__DUCK = 0.1f;
    public static final float VOLUME__LOUD = 1.0f;

    public static final String ACTION__TOGGLE_PLAYBACK = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.TOGGLE_PLAYBACK";
    public static final String ACTION__PLAY = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.PLAY";
    public static final String ACTION__PAUSE = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.PAUSE";
    public static final String ACTION__STOP = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.STOP";
    public static final String ACTION__SKIP_TO_PREVIOUS = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.SKIP_TO_PREVIOUS";
    public static final String ACTION__SKIP_TO_NEXT = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.SKIP_TO_NEXT";
    public static final String ACTION__REWIND = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.REWIND";
    public static final String ACTION__FAST_FORWARD = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.FAST_FORWARD";
    //public static final String ACTION__URL = "com.dreamdigitizers.androidbaselibrary.views.classes.services.OldServiceMediaPlayer.URL";

    private int mState;
    private int mAudioFocus;
    private Track mCurrentTrack;
    private UtilsMediaPlayer mUtilsMediaPlayer;
    private MediaSessionCompat mMediaSession;

    private WifiManager.WifiLock mWifiLock;
    private AudioManager mAudioManager;
    private NotificationManager mNotificationManager;
    private ComponentName mMediaButtonReceiverComponent;
    private PendingIntent mMediaPendingIntent;

    private PendingIntent mPlayPendingIntent;
    private PendingIntent mPausePendingIntent;
    private PendingIntent mStopPendingIntent;
    private PendingIntent mSkipToPreviousPendingIntent;
    private PendingIntent mSkipToNextPendingIntent;
    private PendingIntent mRewindPendingIntent;
    private PendingIntent mFastForwardPendingIntent;

    @Override
    public void onCreate() {
        this.mState = OldServiceMediaPlayer.STATE__STOPPED;
        this.mAudioFocus = OldServiceMediaPlayer.AUDIO_FOCUS__NO_FOCUS_NO_DUCK;

        this.mUtilsMediaPlayer = new UtilsMediaPlayer(this);
        this.mWifiLock = ((WifiManager) this.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, OldServiceMediaPlayer.TAG__WIFI_LOCK);
        this.mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        this.mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        this.buildMediaSession();
        this.buildSupportedPendingIntents();
        this.initialize();
    }

    @Override
    public int onStartCommand(Intent pIntent, int pFlags, int pStartId) {
        String action = pIntent.getAction();
        this.processAction(action);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        this.mState = OldServiceMediaPlayer.STATE__STOPPED;
        //this.releaseResources(true);
        this.dispose();
    }

    @Override
    public void onPrepared(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer) {
        this.mState = OldServiceMediaPlayer.STATE__PLAYING;
        //this.updateNotification(String.format(this.getString(R.string.message__playing), this.mCurrentTrack.getTitle()));
        this.adaptMediaPlayer();
        this.publishState(true);
    }

    @Override
    public void onSeekComplete(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer) {
    }

    @Override
    public void onCompletion(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer) {
        //this.playTrack();
    }

    @Override
    public boolean onError(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer, int pWhat, int pExtra) {
        this.mState = OldServiceMediaPlayer.STATE__STOPPED;
        this.releaseResources(true);
        return true;
    }

    @Override
    public void onAudioFocusChange(int pFocusChange) {
        switch (pFocusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                this.gainedAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                this.lostAudioFocus(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                this.lostAudioFocus(true);
                break;
            default:
                break;
        }
    }

    protected void buildMediaSession() {
        this.mMediaButtonReceiverComponent = new ComponentName(this, MediaButtonReceiver.class);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(this.mMediaButtonReceiverComponent);
        this.mMediaPendingIntent = PendingIntent.getBroadcast(this, OldServiceMediaPlayer.REQUEST_CODE, mediaButtonIntent, 0);

        this.mMediaSession = new MediaSessionCompat(this, this.getClass().getName(), this.mMediaButtonReceiverComponent, this.mMediaPendingIntent);
        this.mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        this.mMediaSession.setActive(true);
        this.mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent pIntent) {
                if (pIntent != null) {
                    if (pIntent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                        OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__PAUSE);
                        return true;
                    } else if (pIntent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                        KeyEvent keyEvent = (KeyEvent) pIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            switch (keyEvent.getKeyCode()) {
                                case KeyEvent.KEYCODE_HEADSETHOOK:
                                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__TOGGLE_PLAYBACK);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_PLAY:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__PLAY);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__PAUSE);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_STOP:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__STOP);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__SKIP_TO_PREVIOUS);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_NEXT:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__SKIP_TO_NEXT);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_REWIND:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__REWIND);
                                    return true;
                                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                                    OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__FAST_FORWARD);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    }
                }
                return super.onMediaButtonEvent(pIntent);
            }

            @Override
            public void onPlay() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__PLAY);
            }

            @Override
            public void onPause() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__PAUSE);
            }

            @Override
            public void onStop() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__STOP);
            }

            @Override
            public void onSkipToPrevious() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__SKIP_TO_PREVIOUS);
            }

            @Override
            public void onSkipToNext() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__SKIP_TO_NEXT);
            }

            @Override
            public void onRewind() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__REWIND);
            }

            @Override
            public void onFastForward() {
                OldServiceMediaPlayer.this.processAction(OldServiceMediaPlayer.ACTION__FAST_FORWARD);
            }
        });
    }

    private void buildSupportedPendingIntents() {
        this.mPlayPendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__PLAY);
        this.mPausePendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__PAUSE);
        this.mStopPendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__STOP);
        this.mSkipToPreviousPendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__SKIP_TO_PREVIOUS);
        this.mSkipToNextPendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__SKIP_TO_NEXT);
        this.mRewindPendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__REWIND);
        this.mFastForwardPendingIntent = this.buildSupportedPendingIntent(OldServiceMediaPlayer.ACTION__FAST_FORWARD);
    }

    private PendingIntent buildSupportedPendingIntent(String pAction) {
        Intent intent = new Intent(pAction);
        return PendingIntent.getService(this, OldServiceMediaPlayer.REQUEST_CODE, intent, 0);
    }

    protected void processAction(String pAction) {
        switch (pAction) {
            case OldServiceMediaPlayer.ACTION__TOGGLE_PLAYBACK:
                this.processTogglePlaybackRequest();
                break;
            case OldServiceMediaPlayer.ACTION__PLAY:
                this.processPlayRequest();
                break;
            case OldServiceMediaPlayer.ACTION__PAUSE:
                this.processPauseRequest();
                break;
            case OldServiceMediaPlayer.ACTION__STOP:
                this.processStopRequest();
                break;
            case OldServiceMediaPlayer.ACTION__SKIP_TO_PREVIOUS:
                this.processSkipToPreviousRequest();
                break;
            case OldServiceMediaPlayer.ACTION__SKIP_TO_NEXT:
                this.processSkipToNextRequest();
                break;
            case OldServiceMediaPlayer.ACTION__REWIND:
                this.processRewindRequest();
                break;
            case OldServiceMediaPlayer.ACTION__FAST_FORWARD:
                this.processFastForwardRequest();
                break;
            default:
                break;
        }
    }

    protected void updateMetadata(Track pTrack) {
        MediaMetadataCompat.Builder mediaMetadataBuilder = new MediaMetadataCompat.Builder();
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, pTrack.getTitle());
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, pTrack.getArtist());
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, pTrack.getAlbum());
        mediaMetadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, pTrack.getDuration());
        //mediaMetadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, pTrack.getAlbum());
        this.mMediaSession.setMetadata(mediaMetadataBuilder.build());
    }

    protected void publishState(boolean pIsUpdateNotification) {
        PlaybackStateCompat.Builder PlaybackStateBuilder = new PlaybackStateCompat.Builder();
        PlaybackStateBuilder.setActions(this.getProtectedPlaybackActions());
        switch (this.mState) {
            case OldServiceMediaPlayer.STATE__PLAYING:
                PlaybackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1);
                break;
            case OldServiceMediaPlayer.STATE__PREPARING:
            case OldServiceMediaPlayer.STATE__PAUSED:
                PlaybackStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 0);
                break;
            case OldServiceMediaPlayer.STATE__STOPPED:
                PlaybackStateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 0);
                break;
            default:
                break;
        }
        this.mMediaSession.setPlaybackState(PlaybackStateBuilder.build());

        if(pIsUpdateNotification) {
            this.updateNotification(this.mCurrentTrack);
        }
    }

    protected void createMediaPlayerIfNeeded() {
        this.mUtilsMediaPlayer.createMediaPlayerIfNeeded(this);
    }

    protected void adaptMediaPlayer() {
        if (this.mAudioFocus == OldServiceMediaPlayer.AUDIO_FOCUS__NO_FOCUS_NO_DUCK) {
            if (this.mUtilsMediaPlayer.isPlaying()) {
                this.mUtilsMediaPlayer.pause();
            }
            return;
        } else if (this.mAudioFocus == OldServiceMediaPlayer.AUDIO_FOCUS__NO_FOCUS_CAN_DUCK) {
            this.mUtilsMediaPlayer.setVolume(OldServiceMediaPlayer.VOLUME__DUCK, OldServiceMediaPlayer.VOLUME__DUCK);
        } else {
            this.mUtilsMediaPlayer.setVolume(OldServiceMediaPlayer.VOLUME__LOUD, OldServiceMediaPlayer.VOLUME__LOUD);
        }
        if (!this.mUtilsMediaPlayer.isPlaying()) {
            this.mUtilsMediaPlayer.start();
        }
    }

    protected void gainedAudioFocus() {
        this.mAudioFocus = OldServiceMediaPlayer.AUDIO_FOCUS__FOCUSED;
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING) {
            this.adaptMediaPlayer();
        }
    }

    protected void lostAudioFocus(boolean pIsDuck) {
        this.mAudioFocus = pIsDuck ? OldServiceMediaPlayer.AUDIO_FOCUS__NO_FOCUS_CAN_DUCK : OldServiceMediaPlayer.AUDIO_FOCUS__NO_FOCUS_NO_DUCK;
        if (this.mUtilsMediaPlayer.isPlaying()) {
            this.adaptMediaPlayer();
        }
    }

    protected void releaseResources(boolean pIsReleaseMediaPlayer) {
        this.stopForeground(pIsReleaseMediaPlayer);

        if(pIsReleaseMediaPlayer) {
            this.mUtilsMediaPlayer.reset();
            this.mUtilsMediaPlayer.release();
            this.releaseAudioFocus();
        }

        this.releaseWifiLock();
    }

    protected void dispose() {
        this.stopForeground(true);
        this.releaseAudioFocus();
        this.releaseWifiLock();
        this.mMediaSession.release();
        this.mMediaSession = null;
        this.mUtilsMediaPlayer.dispose();
        this.mUtilsMediaPlayer = null;
        this.mCurrentTrack = null;
        this.mWifiLock = null;
        this.mAudioManager = null;
        this.mNotificationManager = null;
        this.mMediaButtonReceiverComponent = null;
        this.mMediaPendingIntent = null;
        this.mPlayPendingIntent = null;
        this.mPausePendingIntent = null;
        this.mStopPendingIntent = null;
        this.mSkipToPreviousPendingIntent = null;
        this.mSkipToNextPendingIntent = null;
        this.mRewindPendingIntent = null;
        this.mFastForwardPendingIntent = null;
    }

    protected boolean requestAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == this.mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    protected boolean abandonAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == this.mAudioManager.abandonAudioFocus(this);
    }

    protected void acquireAudioFocus() {
        if (this.mAudioFocus != OldServiceMediaPlayer.AUDIO_FOCUS__FOCUSED && this.requestAudioFocus()) {
            this.mAudioFocus = OldServiceMediaPlayer.AUDIO_FOCUS__FOCUSED;
        }
    }

    protected void releaseAudioFocus() {
        if (this.mAudioFocus == OldServiceMediaPlayer.AUDIO_FOCUS__FOCUSED && this.abandonAudioFocus()) {
            this.mAudioFocus = OldServiceMediaPlayer.AUDIO_FOCUS__NO_FOCUS_NO_DUCK;
        }
    }

    protected void acquireWifiLock() {
        this.mWifiLock.acquire();
    }

    protected void releaseWifiLock() {
        if (this.mWifiLock.isHeld()) {
            this.mWifiLock.release();
        }
    }

    protected void processTogglePlaybackRequest() {
        if (this.mState == OldServiceMediaPlayer.STATE__PAUSED || this.mState == OldServiceMediaPlayer.STATE__STOPPED) {
            this.processPlayRequest();
        } else {
            this.processPauseRequest();
        }
    }

    protected void processPlayRequest() {
        this.acquireAudioFocus();

        if (this.mState == OldServiceMediaPlayer.STATE__STOPPED) {
            Track track = new Track("title", "artist", "album", "https://api.soundcloud.com/tracks/241005444/stream?client_id=3087264eee9aea2645a592f1cf600c0d", 227703);
            this.playTrack(track);
            //this.playTrack();
        } else if (mState == OldServiceMediaPlayer.STATE__PAUSED) {
            this.mState = OldServiceMediaPlayer.STATE__PLAYING;
            this.adaptMediaPlayer();
            this.publishState(true);
        }
    }

    protected void processPauseRequest() {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING) {
            this.mState = OldServiceMediaPlayer.STATE__PAUSED;
            this.mUtilsMediaPlayer.pause();
            this.releaseResources(false);
            this.publishState(true);
        }
    }

    protected void processRewindRequest() {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING || this.mState == OldServiceMediaPlayer.STATE__PAUSED) {
            this.mUtilsMediaPlayer.seekTo(0);
        }
    }

    protected void processFastForwardRequest() {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING || this.mState == OldServiceMediaPlayer.STATE__PAUSED) {
            //this.mUtilsMediaPlayer.seekTo(0);
        }
    }

    protected void processSkipToPreviousRequest() {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING || this.mState == OldServiceMediaPlayer.STATE__PAUSED) {
            this.acquireAudioFocus();
            this.playTrack(null);
        }
    }

    protected void processSkipToNextRequest() {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING || this.mState == OldServiceMediaPlayer.STATE__PAUSED) {
            this.acquireAudioFocus();
            this.playTrack(null);
        }
    }

    protected void processStopRequest() {
        this.processStopRequest(false);
    }

    protected void processStopRequest(boolean pIsForced) {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING || this.mState == OldServiceMediaPlayer.STATE__PAUSED || pIsForced) {
            this.mState = OldServiceMediaPlayer.STATE__STOPPED;
            this.releaseResources(true);
            this.publishState(false);
            this.stopSelf();
        }
    }

    /*
    protected void processAddRequest(Intent pIntent) {
        if (this.mState == OldServiceMediaPlayer.STATE__PLAYING || this.mState == OldServiceMediaPlayer.STATE__PAUSED || this.mState == OldServiceMediaPlayer.STATE__STOPPED) {
            this.acquireAudioFocus();
            this.playTrack((Track) pIntent.getExtras());
        }
    }
    */

    protected void playTrack() {
        this.playTrack(this.mCurrentTrack);
    }

    protected void playTrack(Track pTrack) {
        if(pTrack == null) {
            return;
        }
        this.mCurrentTrack = pTrack;
        this.mState = OldServiceMediaPlayer.STATE__STOPPED;
        this.releaseResources(false);
        this.createMediaPlayerIfNeeded();
        if(!this.mUtilsMediaPlayer.setDataSource(this.mCurrentTrack.getSourcePath())) {
            return;
        }

        this.mState = OldServiceMediaPlayer.STATE__PREPARING;
        this.setUpAsForeground(this.mCurrentTrack);
        this.updateMetadata(this.mCurrentTrack);
        this.publishState(false);
        this.mUtilsMediaPlayer.prepareAsync();

        if (this.isRemotePlayback()) {
            this.acquireWifiLock();
        } else {
            this.releaseWifiLock();
        }
    }

    protected void setUpAsForeground(Track pTrack) {
        this.startForeground(OldServiceMediaPlayer.NOTIFICATION_ID, this.createNotification(pTrack));
    }

    protected void updateNotification(Track pTrack) {
        this.mNotificationManager.notify(OldServiceMediaPlayer.NOTIFICATION_ID, this.createNotification(pTrack));
    }

    protected int getState() {
        return this.mState;
    }

    protected int getAudioFocus() {
        return this.mAudioFocus;
    }

    protected Track getCurrentTrack() {
        return this.mCurrentTrack;
    }

    protected UtilsMediaPlayer getUtilsMediaPlayer() {
        return this.mUtilsMediaPlayer;
    }

    protected MediaSessionCompat getMediaSession() {
        return this.mMediaSession;
    }

    protected PendingIntent getPlayPendingIntent() {
        return this.mPlayPendingIntent;
    }

    protected PendingIntent getPausePendingIntent() {
        return this.mPausePendingIntent;
    }

    protected PendingIntent getStopPendingIntent() {
        return this.mStopPendingIntent;
    }

    protected PendingIntent getSkipToPreviousPendingIntent() {
        return this.mSkipToPreviousPendingIntent;
    }

    protected PendingIntent getSkipToNextPendingIntent() {
        return this.mSkipToNextPendingIntent;
    }

    protected PendingIntent getRewindPendingIntent() {
        return this.mRewindPendingIntent;
    }

    protected PendingIntent getFastForwardPendingIntent() {
        return this.mFastForwardPendingIntent;
    }

    protected void initialize() {
    }

    protected abstract boolean isRemotePlayback();
    protected abstract long getProtectedPlaybackActions();
    protected abstract Notification createNotification(Track pTrack);

    public static class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            if (pIntent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__PAUSE));
            } else if (pIntent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent keyEvent = (KeyEvent) pIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__TOGGLE_PLAYBACK));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__PLAY));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__PAUSE));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__STOP));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__SKIP_TO_PREVIOUS));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__SKIP_TO_NEXT));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_REWIND:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__REWIND));
                            break;
                        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                            pContext.startService(new Intent(OldServiceMediaPlayer.ACTION__FAST_FORWARD));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public static class Track {
        private String mTitle;
        private String mArtist;
        private String mAlbum;
        private String mSourcePath;
        private long mDuration;

        public Track(String pTitle, String pArtist, String pAlbum, String pSourcePath, long pDuration) {
            this.mTitle = pTitle;
            this.mArtist = pArtist;
            this.mAlbum = pAlbum;
            this.mSourcePath = pSourcePath;
            this.mDuration = pDuration;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public String getArtist() {
            return this.mArtist;
        }

        public String getAlbum() {
            return this.mAlbum;
        }

        public String getSourcePath() {
            return this.mSourcePath;
        }

        public long getDuration() {
            return this.mDuration;
        }
    }
}