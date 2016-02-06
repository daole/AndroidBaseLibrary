package com.dreamdigitizers.androidbaselibrary.views.classes.services.support;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer;

public abstract class MediaPlayerNotificationReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;

    private static final int REQUEST_CODE__SKIP_TO_PREVIOUS = 0;
    private static final int REQUEST_CODE__REWIND = 1;
    private static final int REQUEST_CODE__TOGGLE_PLAYBACK = 2;
    private static final int REQUEST_CODE__PLAY = 3;
    private static final int REQUEST_CODE__PAUSE = 4;
    private static final int REQUEST_CODE__STOP = 5;
    private static final int REQUEST_CODE__FAST_FORWARD = 6;
    private static final int REQUEST_CODE__SKIP_TO_NEXT = 7;

    protected final NotificationManager mNotificationManager;

    protected ServiceMediaPlayer mService;
    protected MediaSessionCompat.Token mSessionToken;
    protected MediaControllerCompat mMediaController;
    protected MediaControllerCompat.TransportControls mTransportControls;
    protected MediaControllerCallback mMediaControllerCallback;
    protected PlaybackStateCompat mPlaybackState;
    protected MediaMetadataCompat mMediaMetadata;

    protected PendingIntent mSkipToPreviousPendingIntent;
    protected PendingIntent mRewindPendingIntent;
    protected PendingIntent mTogglePlaybackPendingIntent;
    protected PendingIntent mPlayPendingIntent;
    protected PendingIntent mPausePendingIntent;
    protected PendingIntent mStopPendingIntent;
    protected PendingIntent mFastForwardPendingIntent;
    protected PendingIntent mSkipToNextPendingIntent;

    protected boolean mStarted;

    public MediaPlayerNotificationReceiver(ServiceMediaPlayer pService) {
        this.mService = pService;
        this.mMediaControllerCallback = new MediaControllerCallback();
        this.mNotificationManager = (NotificationManager) this.mService.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mNotificationManager.cancelAll();
        this.buildSupportedPendingIntents();
        this.updateSessionToken();
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {
        String action = pIntent.getAction();
        if (ServiceMediaPlayer.ACTION__MEDIA_COMMAND.equals(action)) {
            String command = pIntent.getStringExtra(ServiceMediaPlayer.COMMAND__NAME);
            switch (command) {
                case ServiceMediaPlayer.COMMAND__SKIP_TO_PREVIOUS:
                    this.mTransportControls.skipToPrevious();
                    break;
                case ServiceMediaPlayer.COMMAND__REWIND:
                    this.mTransportControls.rewind();
                    break;
                case ServiceMediaPlayer.COMMAND__PLAY:
                    this.mTransportControls.play();
                    break;
                case ServiceMediaPlayer.COMMAND__PAUSE:
                    this.mTransportControls.pause();
                    break;
                case ServiceMediaPlayer.COMMAND__STOP:
                    this.mTransportControls.stop();
                    break;
                case ServiceMediaPlayer.COMMAND__FAST_FORWARD:
                    this.mTransportControls.fastForward();
                    break;
                case ServiceMediaPlayer.COMMAND__SKIP_TO_NEXT:
                    this.mTransportControls.skipToNext();
                    break;
                default:
                    break;
            }
        }
    }

    public PendingIntent getTogglePlaybackPendingIntent() {
        return this.mTogglePlaybackPendingIntent;
    }

    public PendingIntent getPlayPendingIntent() {
        return this.mPlayPendingIntent;
    }

    public PendingIntent getPausePendingIntent() {
        return this.mPausePendingIntent;
    }

    public PendingIntent getStopPendingIntent() {
        return this.mStopPendingIntent;
    }

    public PendingIntent getSkipToPreviousPendingIntent() {
        return this.mSkipToPreviousPendingIntent;
    }

    public PendingIntent getSkipToNextPendingIntent() {
        return this.mSkipToNextPendingIntent;
    }

    public PendingIntent getRewindPendingIntent() {
        return this.mRewindPendingIntent;
    }

    public PendingIntent getFastForwardPendingIntent() {
        return this.mFastForwardPendingIntent;
    }

    public void startNotification() {
        if (!this.mStarted) {
            this.mMediaMetadata = this.mMediaController.getMetadata();
            this.mPlaybackState = this.mMediaController.getPlaybackState();

            Notification notification = this.createNotification();
            if (notification != null) {
                this.mMediaController.registerCallback(this.mMediaControllerCallback);
                IntentFilter filter = new IntentFilter(ServiceMediaPlayer.ACTION__MEDIA_COMMAND);
                this.mService.registerReceiver(this, filter);
                this.mService.startForeground(MediaPlayerNotificationReceiver.NOTIFICATION_ID, notification);
                this.mStarted = true;
            }
        }
    }

    public void stopNotification() {
        if (this.mStarted) {
            this.mStarted = false;
            this.mMediaController.unregisterCallback(this.mMediaControllerCallback);
            this.mNotificationManager.cancel(MediaPlayerNotificationReceiver.NOTIFICATION_ID);
            this.mService.unregisterReceiver(this);
            this.mService.stopForeground(true);
        }
    }

    private void onPlaybackStateChanged(PlaybackStateCompat pPlaybackState) {
        this.mPlaybackState = pPlaybackState;
        int state = this.mPlaybackState.getState();
        if (state == PlaybackStateCompat.STATE_STOPPED || state == PlaybackStateCompat.STATE_NONE) {
            this.stopNotification();
        } else {
            Notification notification = this.createNotification();
            if (notification != null) {
                this.mNotificationManager.notify(MediaPlayerNotificationReceiver.NOTIFICATION_ID, notification);
            }
        }
    }

    private void onMetadataChanged(MediaMetadataCompat pMediaMetadata) {
        this.mMediaMetadata = pMediaMetadata;
        Notification notification = this.createNotification();
        if (notification != null) {
            this.mNotificationManager.notify(MediaPlayerNotificationReceiver.NOTIFICATION_ID, notification);
        }
    }

    private void onSessionDestroyed() {
        this.updateSessionToken();
    }

    private void updateSessionToken() {
        MediaSessionCompat.Token newToken = this.mService.getSessionToken();
        if (this.mSessionToken == null || !this.mSessionToken.equals(newToken)) {
            if (this.mMediaController != null) {
                this.mMediaController.unregisterCallback(this.mMediaControllerCallback);
            }
            this.mSessionToken = newToken;
            try {
                this.mMediaController = new MediaControllerCompat(this.mService, this.mSessionToken);
                this.mTransportControls = this.mMediaController.getTransportControls();
                if (this.mStarted) {
                    this.mMediaController.registerCallback(this.mMediaControllerCallback);
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void buildSupportedPendingIntents() {
        this.mSkipToPreviousPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__SKIP_TO_PREVIOUS, MediaPlayerNotificationReceiver.REQUEST_CODE__SKIP_TO_PREVIOUS);
        this.mRewindPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__REWIND, MediaPlayerNotificationReceiver.REQUEST_CODE__REWIND);
        this.mTogglePlaybackPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__TOGGLE_PLAYBACK, MediaPlayerNotificationReceiver.REQUEST_CODE__TOGGLE_PLAYBACK);
        this.mPlayPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__PLAY, MediaPlayerNotificationReceiver.REQUEST_CODE__PLAY);
        this.mPausePendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__PAUSE, MediaPlayerNotificationReceiver.REQUEST_CODE__PAUSE);
        this.mStopPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__STOP, MediaPlayerNotificationReceiver.REQUEST_CODE__STOP);
        this.mFastForwardPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__FAST_FORWARD, MediaPlayerNotificationReceiver.REQUEST_CODE__FAST_FORWARD);
        this.mSkipToNextPendingIntent = this.buildSupportedPendingIntent(ServiceMediaPlayer.COMMAND__SKIP_TO_NEXT, MediaPlayerNotificationReceiver.REQUEST_CODE__SKIP_TO_NEXT);
    }

    private PendingIntent buildSupportedPendingIntent(String pCommand, int pRequestCode) {
        Intent intent = new Intent(ServiceMediaPlayer.ACTION__MEDIA_COMMAND);
        intent.setPackage(this.mService.getPackageName());
        intent.putExtra(ServiceMediaPlayer.COMMAND__NAME, pCommand);
        return PendingIntent.getBroadcast(this.mService, pRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    protected abstract Notification createNotification();

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat pPlaybackState) {
            MediaPlayerNotificationReceiver.this.onPlaybackStateChanged(pPlaybackState);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat pMediaMetadata) {
            MediaPlayerNotificationReceiver.this.onMetadataChanged(pMediaMetadata);
        }

        @Override
        public void onSessionDestroyed() {
            MediaPlayerNotificationReceiver.this.onSessionDestroyed();
        }
    }
}
