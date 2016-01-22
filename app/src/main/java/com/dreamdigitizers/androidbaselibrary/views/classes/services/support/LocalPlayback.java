package com.dreamdigitizers.androidbaselibrary.views.classes.services.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.dreamdigitizers.androidbaselibrary.utils.UtilsMediaPlayer;
import com.dreamdigitizers.androidbaselibrary.utils.UtilsString;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer;

public class LocalPlayback implements
        IPlayback,
        AudioManager.OnAudioFocusChangeListener,
        UtilsMediaPlayer.CustomMediaPlayer.IOnMediaPlayerActionResultListener {
    private static final String ERROR_MESSAGE__MEDIA_PLAYER_UNCLEAR = "MediaPlayer error, what: %d, extra: %d";
    private static final String TAG__WIFI_LOCK = "LocalPlayback.WifiLock";

    private static final int AUDIO_FOCUS__NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_FOCUS__NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_FOCUS__FOCUSED = 2;

    public static final float VOLUME__DUCK = 0.1f;
    public static final float VOLUME__NORMAL = 1.0f;

    private volatile String mCurrentMediaId;
    private volatile int mCurrentPosition;

    private int mState;
    private int mAudioFocus;
    private boolean mIsPlayOnFocusGain;
    private boolean mIsAudioNoisyReceiverRegistered;
    private boolean mIsOnlineStreaming;

    private ICallback mCallback;
    private final ServiceMediaPlayer mService;
    private final AudioManager mAudioManager;
    private final WifiManager.WifiLock mWifiLock;
    private final UtilsMediaPlayer mUtilsMediaPlayer;
    private final AudioNoisyReceiver mAudioNoisyReceiver;
    private final IntentFilter mAudioNoisyIntentFilter;

    public LocalPlayback(ServiceMediaPlayer pService, boolean pIsOnlineStreaming) {
        this.mService = pService;
        this.mIsOnlineStreaming = pIsOnlineStreaming;
        this.mAudioFocus = LocalPlayback.AUDIO_FOCUS__NO_FOCUS_NO_DUCK;

        this.mAudioManager = (AudioManager) pService.getSystemService(Context.AUDIO_SERVICE);
        this.mWifiLock = ((WifiManager) pService.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, LocalPlayback.TAG__WIFI_LOCK);

        this.mUtilsMediaPlayer = new UtilsMediaPlayer(this);
        this.mAudioNoisyReceiver = new AudioNoisyReceiver();
        this.mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    }

    /*
    @Override
    public void start() {
    }
    */

    @Override
    public void play(MediaSessionCompat.QueueItem pQueueItem) {
        this.mIsPlayOnFocusGain = true;
        this.requestAudioFocus();
        this.registerAudioNoisyReceiver();

        String mediaId = pQueueItem.getDescription().getMediaId();
        boolean isQueueItemChanged = !UtilsString.equals(mediaId, this.mCurrentMediaId);
        if (isQueueItemChanged) {
            this.mCurrentPosition = 0;
            this.mCurrentMediaId = mediaId;
        }

        if (this.mState == PlaybackStateCompat.STATE_PAUSED && !isQueueItemChanged && this.mUtilsMediaPlayer.isMediaPlayerCreated()) {
            this.adaptMediaPlayer();
        } else {
            this.mState = PlaybackStateCompat.STATE_STOPPED;
            this.releaseResources(false);

            this.createMediaPlayerIfNeeded();

            this.mState = PlaybackStateCompat.STATE_BUFFERING;
            MediaDescriptionCompat mediaDescription = pQueueItem.getDescription();
            String source = mediaDescription.getMediaUri().toString();
            boolean result = this.mUtilsMediaPlayer.setDataSource(source);
            if (!result) {
                if (this.mCallback != null) {
                    this.mCallback.onError("Can not set data source");
                    return;
                }
            }

            this.mUtilsMediaPlayer.prepareAsync();

            if (this.mIsOnlineStreaming) {
                this.mWifiLock.acquire();
            }

            if (this.mCallback != null) {
                this.mCallback.onPlaybackStatusChanged(this.mState);
            }
        }
    }

    @Override
    public void pause() {
        if (this.mState == PlaybackStateCompat.STATE_PLAYING) {
            if (this.mUtilsMediaPlayer.isPlaying()) {
                this.mUtilsMediaPlayer.pause();
                this.mCurrentPosition = this.mUtilsMediaPlayer.getCurrentPosition();
            }

            this.releaseResources(false);
            this.releaseAudioFocus();
        }
        this.unregisterAudioNoisyReceiver();

        this.mState = PlaybackStateCompat.STATE_PAUSED;
        if (this.mCallback != null) {
            this.mCallback.onPlaybackStatusChanged(this.mState);
        }
    }

    @Override
    public void stop(boolean pNotifyListeners) {
        this.mCurrentPosition = this.getCurrentStreamPosition();
        this.releaseResources(true);
        this.releaseAudioFocus();
        this.unregisterAudioNoisyReceiver();

        this.mState = PlaybackStateCompat.STATE_STOPPED;
        if (pNotifyListeners && this.mCallback != null) {
            this.mCallback.onPlaybackStatusChanged(this.mState);
        }
    }

    @Override
    public void seekTo(int pPosition) {
        if (!this.mUtilsMediaPlayer.isMediaPlayerCreated()) {
            this.mCurrentPosition = pPosition;
        } else {
            boolean isPlaying = this.mUtilsMediaPlayer.isPlaying();
            this.mUtilsMediaPlayer.seekTo(pPosition);

            if (isPlaying) {
                this.mState = PlaybackStateCompat.STATE_BUFFERING;
                if (this.mCallback != null) {
                    this.mCallback.onPlaybackStatusChanged(this.mState);
                }
            }
        }
    }

    /*
    @Override
    public boolean isConnected() {
        return true;
    }
    */

    @Override
    public boolean isPlaying() {
        return this.mIsPlayOnFocusGain || (this.mUtilsMediaPlayer.isPlaying());
    }

    /*
    @Override
    public String getCurrentMediaId() {
        return this.mCurrentMediaId;
    }

    @Override
    public void setCurrentMediaId(String pCurrentMediaId) {
        this.mCurrentMediaId = pCurrentMediaId;
    }
    */

    @Override
    public void setState(int pState) {
        this.mState = pState;
    }

    @Override
    public int getState() {
        return this.mState;
    }

    @Override
    public int getCurrentStreamPosition() {
        int currentPosition = this.mUtilsMediaPlayer.getCurrentPosition();
        return currentPosition >= 0 ? currentPosition : this.mCurrentPosition;
    }

    /*
    @Override
    public void setCurrentStreamPosition(int pCurrentPosition) {
        this.mCurrentPosition = pCurrentPosition;
    }

    @Override
    public void updateLastKnownStreamPosition() {
        int currentPosition = this.mUtilsMediaPlayer.getCurrentPosition();
        if (currentPosition >= 0) {
            this.mCurrentPosition = currentPosition;
        }
    }
    */

    @Override
    public void setCallback(ICallback pCallback) {
        this.mCallback = pCallback;
    }

    @Override
    public void onAudioFocusChange(int pFocusChange) {
        switch (pFocusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                this.mAudioFocus = LocalPlayback.AUDIO_FOCUS__FOCUSED;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                boolean isDucking = pFocusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                this.mAudioFocus = isDucking ? LocalPlayback.AUDIO_FOCUS__NO_FOCUS_CAN_DUCK : LocalPlayback.AUDIO_FOCUS__NO_FOCUS_NO_DUCK;
                if (this.mState == PlaybackStateCompat.STATE_PLAYING && !isDucking) {
                    this.mIsPlayOnFocusGain = true;
                }
                break;
            default:
                break;
        }

        this.adaptMediaPlayer();
    }

    @Override
    public void onPrepared(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer) {
        this.adaptMediaPlayer();
    }

    @Override
    public void onSeekComplete(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer) {
        this.mCurrentPosition = this.mUtilsMediaPlayer.getCurrentPosition();
        if (this.mState == PlaybackStateCompat.STATE_BUFFERING) {
            this.mUtilsMediaPlayer.start();

            this.mState = PlaybackStateCompat.STATE_PLAYING;
            if (this.mCallback != null) {
                this.mCallback.onPlaybackStatusChanged(mState);
            }
        }
    }

    @Override
    public void onCompletion(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer) {
        if (this.mCallback != null) {
            this.mCallback.onCompletion();
        }
    }

    @Override
    public boolean onError(UtilsMediaPlayer.CustomMediaPlayer pMediaPlayer, int pWhat, int pExtra) {
        if (this.mCallback != null) {
            this.mCallback.onError(String.format(LocalPlayback.ERROR_MESSAGE__MEDIA_PLAYER_UNCLEAR, pWhat, pExtra));
        }
        return true;
    }

    private void createMediaPlayerIfNeeded() {
        this.mUtilsMediaPlayer.createMediaPlayerIfNeeded(this.mService);
    }

    private void adaptMediaPlayer() {
        if (this.mAudioFocus == LocalPlayback.AUDIO_FOCUS__NO_FOCUS_NO_DUCK) {
            if (this.mState == PlaybackStateCompat.STATE_PLAYING) {
                this.pause();
            }
        } else {
            if (this.mAudioFocus == LocalPlayback.AUDIO_FOCUS__NO_FOCUS_CAN_DUCK) {
                this.mUtilsMediaPlayer.setVolume(LocalPlayback.VOLUME__DUCK, LocalPlayback.VOLUME__DUCK);
            } else {
                this.mUtilsMediaPlayer.setVolume(LocalPlayback.VOLUME__NORMAL, LocalPlayback.VOLUME__NORMAL);
            }

            if (this.mIsPlayOnFocusGain) {
                this.mIsPlayOnFocusGain = false;

                if (!this.mUtilsMediaPlayer.isPlaying()) {
                    if (this.mCurrentPosition == this.mUtilsMediaPlayer.getCurrentPosition()) {
                        this.mUtilsMediaPlayer.start();
                        this.mState = PlaybackStateCompat.STATE_PLAYING;
                    } else {
                        this.mUtilsMediaPlayer.seekTo(this.mCurrentPosition);
                        this.mState = PlaybackStateCompat.STATE_BUFFERING;
                    }

                    if (this.mCallback != null) {
                        this.mCallback.onPlaybackStatusChanged(this.mState);
                    }
                }
            }
        }
    }

    private void releaseResources(boolean pIsReleaseMediaPlayer) {
        this.mService.stopForeground(pIsReleaseMediaPlayer);

        if (pIsReleaseMediaPlayer) {
            this.mUtilsMediaPlayer.reset();
            this.mUtilsMediaPlayer.dispose();
        }

        if (this.mWifiLock.isHeld()) {
            this.mWifiLock.release();
        }
    }

    private void requestAudioFocus() {
        if (this.mAudioFocus != LocalPlayback.AUDIO_FOCUS__FOCUSED) {
            int result = this.mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                this.mAudioFocus = LocalPlayback.AUDIO_FOCUS__FOCUSED;
            }
        }
    }

    private void releaseAudioFocus() {
        if (this.mAudioFocus == LocalPlayback.AUDIO_FOCUS__FOCUSED) {
            int result = this.mAudioManager.abandonAudioFocus(this);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                this.mAudioFocus = LocalPlayback.AUDIO_FOCUS__NO_FOCUS_NO_DUCK;
            }
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!this.mIsAudioNoisyReceiverRegistered) {
            this.mService.registerReceiver(this.mAudioNoisyReceiver, this.mAudioNoisyIntentFilter);
            this.mIsAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (this.mIsAudioNoisyReceiverRegistered) {
            this.mService.unregisterReceiver(this.mAudioNoisyReceiver);
            this.mIsAudioNoisyReceiverRegistered = false;
        }
    }

    private class AudioNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            String action = pIntent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                if (LocalPlayback.this.isPlaying()) {
                    LocalPlayback.this.pause();
                }
            }
        }
    }
}
