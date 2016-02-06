package com.dreamdigitizers.androidbaselibrary.views.classes.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.dreamdigitizers.androidbaselibrary.R;
import com.dreamdigitizers.androidbaselibrary.utilities.UtilsString;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.CustomQueueItem;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.IPlayback;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.LocalPlayback;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.MediaPlayerNotificationReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class ServiceMediaPlayer extends MediaBrowserServiceCompat implements IPlayback.ICallback {
    public static final String ACTION__MEDIA_COMMAND = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.ACTION__MEDIA_COMMAND";
    public static final String COMMAND__NAME = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.COMMAND__NAME";
    public static final String COMMAND__SKIP_TO_PREVIOUS = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.SKIP_TO_PREVIOUS";
    public static final String COMMAND__REWIND = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.REWIND";
    public static final String COMMAND__TOGGLE_PLAYBACK = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.TOGGLE_PLAYBACK";
    public static final String COMMAND__PLAY_PAUSE = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.PLAY_PAUSE";
    public static final String COMMAND__PLAY = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.PLAY";
    public static final String COMMAND__PAUSE = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.PAUSE";
    public static final String COMMAND__STOP = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.STOP";
    public static final String COMMAND__FAST_FORWARD = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.FAST_FORWARD";
    public static final String COMMAND__SKIP_TO_NEXT = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaPlayer.SKIP_TO_NEXT";

    protected static final float PLAYBACK_SPEED = 1.0f;
    protected static final int STOP_DELAY = 30000;
    protected static final int REQUEST_CODE = 0;

    private List<CustomQueueItem> mPlayingQueue;
    private DelayedStopHandler mDelayedStopHandler;
    private MediaPlayerNotificationReceiver mMediaPlayerNotificationReceiver;
    private IPlayback mPlayback;
    private MediaSessionCompat mMediaSession;

    private int mCurrentIndexOnQueue;
    private boolean mIsStarted;

    @Override
    public void onCreate() {
        super.onCreate();
        this.buildMediaSession();

        this.mPlayingQueue = new ArrayList<>();
        this.mDelayedStopHandler = new DelayedStopHandler(this);
        this.mMediaPlayerNotificationReceiver = this.createMediaPlayerNotificationReceiver();

        this.mPlayback = this.createPlayback();
        this.mPlayback.setState(PlaybackStateCompat.STATE_NONE);
        this.mPlayback.setCallback(this);
        //this.mPlayback.start();

        this.updatePlaybackState(null);
    }

    @Override
    public int onStartCommand(Intent pIntent, int pFlags, int pStartId) {
        if (pIntent != null) {
            String action = pIntent.getAction();
            if (ServiceMediaPlayer.ACTION__MEDIA_COMMAND.equals(action)) {
                String command = pIntent.getStringExtra(ServiceMediaPlayer.COMMAND__NAME);
                if (!UtilsString.isEmpty(command)) {
                    switch (command) {
                        case ServiceMediaPlayer.COMMAND__SKIP_TO_PREVIOUS:
                            this.processSkipToPreviousRequest();
                            break;
                        case ServiceMediaPlayer.COMMAND__REWIND:
                            //this.handleRewindRequest();
                            break;
                        case ServiceMediaPlayer.COMMAND__PLAY_PAUSE:
                            this.processPlayPauseRequest();
                            break;
                        case ServiceMediaPlayer.COMMAND__PLAY:
                            this.processPlayRequest();
                            break;
                        case ServiceMediaPlayer.COMMAND__PAUSE:
                            this.processPauseRequest();
                            break;
                        case ServiceMediaPlayer.COMMAND__STOP:
                            this.processStopRequest(null);
                            break;
                        case ServiceMediaPlayer.COMMAND__FAST_FORWARD:
                            //this.handleFastForwardRequest();
                            break;
                        case ServiceMediaPlayer.COMMAND__SKIP_TO_NEXT:
                            this.processSkipToNextRequest();
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        this.mDelayedStopHandler.sendEmptyMessageDelayed(0, ServiceMediaPlayer.STOP_DELAY);
        return ServiceMediaPlayer.START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.processStopRequest(null);
        this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        this.mMediaSession.release();
    }

    @Override
    public void onCompletion() {
        this.mCurrentIndexOnQueue++;
        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.processPlayRequest();
        } else {
            this.processStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int pState) {
        this.updatePlaybackState(null);
    }

    @Override
    public void onError(String pError) {
        this.updatePlaybackState(pError);
    }

    protected void processSkipToPreviousRequest() {
        this.mCurrentIndexOnQueue--;
        if (this.mCurrentIndexOnQueue < 0 && this.mPlayingQueue != null) {
            this.mCurrentIndexOnQueue = this.mPlayingQueue.size() - 1;
        }
        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.processPlayRequest();
        } else {
            this.processStopRequest(this.getString(R.string.error__media_skip));
        }
    }

    protected void processPlayFromMediaIdRequest(String pMediaId, Bundle pExtras) {
        this.mCurrentIndexOnQueue = this.findIndexOnQueue(pMediaId, this.mPlayingQueue);
        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.processPlayRequest();
        } else {
            this.processStopRequest(this.getString(R.string.error__media_search));
        }
    }

    protected void processPlayPauseRequest() {
        int state = this.mPlayback.getState();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            this.processPauseRequest();
        } else {
            this.processPlayRequest();
        }
    }

    protected void processPlayRequest() {
        this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!this.mIsStarted) {
            //this.startService(new Intent(this, ServiceMediaPlayer.class));
            this.mIsStarted = true;
        }

        if (!this.mMediaSession.isActive()) {
            this.mMediaSession.setActive(true);
        }

        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.updateMetadata();
            this.mPlayback.play(this.mPlayingQueue.get(this.mCurrentIndexOnQueue));
        }
    }

    protected void processPauseRequest() {
        this.mPlayback.pause();
        this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        this.mDelayedStopHandler.sendEmptyMessageDelayed(0, ServiceMediaPlayer.STOP_DELAY);
    }

    protected void processStopRequest(String pError) {
        this.mPlayback.stop(true);
        this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        this.mDelayedStopHandler.sendEmptyMessageDelayed(0, ServiceMediaPlayer.STOP_DELAY);
        this.updatePlaybackState(pError);
        this.stopSelf();
        this.mIsStarted = false;
    }

    protected void processSeekToRequest(int pPosition) {
        this.mPlayback.seekTo(pPosition);
    }

    protected void processSkipToNextRequest() {
        this.mCurrentIndexOnQueue++;
        if (this.mCurrentIndexOnQueue >= this.mPlayingQueue.size() && this.mPlayingQueue != null) {
            this.mCurrentIndexOnQueue = 0;
        }
        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.processPlayRequest();
        } else {
            this.processStopRequest(this.getString(R.string.error__media_skip));
        }
    }

    protected void updateMetadata() {
        if (!this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.updatePlaybackState(this.getString(R.string.error__media_unplayable));
            return;
        }

        CustomQueueItem customQueueItem = this.mPlayingQueue.get(this.mCurrentIndexOnQueue);
        MediaMetadataCompat mediaMetadata = customQueueItem.getMediaMetadata();
        this.mMediaSession.setMetadata(mediaMetadata);

        if (mediaMetadata.getDescription().getIconBitmap() == null && mediaMetadata.getDescription().getIconUri() != null) {
            this.fetchArt(customQueueItem);
        }
    }

    protected void fetchArt(CustomQueueItem pCustomQueueItem) {
    }

    protected void updatePlaybackState(String pError) {
        /*
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (this.mPlayback != null && this.mPlayback.isConnected()) {
            position = this.mPlayback.getCurrentStreamPosition();
        }
        */
        long position = this.mPlayback.getCurrentStreamPosition();

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(this.getAvailableActions());

        int state = mPlayback.getState();

        if (pError != null) {
            stateBuilder.setErrorMessage(pError);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        stateBuilder.setState(state, position, ServiceMediaPlayer.PLAYBACK_SPEED, SystemClock.elapsedRealtime());

        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            MediaSessionCompat.QueueItem queueItem = this.mPlayingQueue.get(this.mCurrentIndexOnQueue).getQueueItem();
            stateBuilder.setActiveQueueItemId(queueItem.getQueueId());
        }

        this.mMediaSession.setPlaybackState(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            this.mMediaPlayerNotificationReceiver.startNotification();
        }
    }

    protected long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        if (this.mPlayingQueue == null || this.mPlayingQueue.isEmpty()) {
            return actions;
        }
        if (this.mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        return actions;
    }

    protected IPlayback createPlayback() {
        return new LocalPlayback(this, this.isOnlineStreaming());
    }

    protected final boolean isIndexPlayable(int pIndex, List<CustomQueueItem> pPlayingQueue) {
        if (pPlayingQueue != null && pIndex >= 0 && pIndex < pPlayingQueue.size()) {
             return true;
        }
        return  false;
    }

    protected final int findIndexOnQueue(String pMediaId, List<CustomQueueItem> pPlayingQueue) {
        if (pPlayingQueue != null) {
            int index = 0;
            for (CustomQueueItem customQueueItem : pPlayingQueue) {
                if (customQueueItem.getQueueItem().getDescription().getMediaId().equals(pMediaId)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    protected final void buildMediaSession() {
        ComponentName mediaButtonReceiverComponent = new ComponentName(this, MediaButtonReceiver.class);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponent);
        PendingIntent mediaButtonPendingIntent = PendingIntent.getBroadcast(this, ServiceMediaPlayer.REQUEST_CODE, mediaButtonIntent, 0);

        this.mMediaSession = new MediaSessionCompat(this, this.getClass().getName(), mediaButtonReceiverComponent, mediaButtonPendingIntent);
        this.mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        this.mMediaSession.setCallback(new MediaSessionCallback());
        this.setSessionToken(this.mMediaSession.getSessionToken());
    }

    protected final int getCurrentIndexOnQueue()  {
        return this.mCurrentIndexOnQueue;
    }

    protected final void setCurrentIndexOnQueue(int pCurrentIndexOnQueue) {
        this.mCurrentIndexOnQueue = pCurrentIndexOnQueue;
    }

    protected final boolean isStarted() {
        return this.mIsStarted;
    }

    protected final void setStarted(boolean pIsStarted) {
        this.mIsStarted = pIsStarted;
    }

    protected final List<CustomQueueItem> getPlayingQueue() {
        return this.mPlayingQueue;
    }

    protected final void setPlayingQueue(List<CustomQueueItem> pPlayingQueue) {
        this.mPlayingQueue = pPlayingQueue;
    }

    protected final DelayedStopHandler getDelayedStopHandler() {
        return this.mDelayedStopHandler;
    }

    protected final MediaPlayerNotificationReceiver getMediaPlayerNotificationReceiver() {
        return this.mMediaPlayerNotificationReceiver;
    }

    protected final IPlayback getPlayback() {
        return this.mPlayback;
    }

    protected final MediaSessionCompat getMediaSession() {
        return this.mMediaSession;
    }

    protected abstract boolean isOnlineStreaming();
    protected abstract MediaPlayerNotificationReceiver createMediaPlayerNotificationReceiver();

    private class DelayedStopHandler extends Handler {
        private final WeakReference<ServiceMediaPlayer> mWeakReference;

        private DelayedStopHandler(ServiceMediaPlayer pService) {
            this.mWeakReference = new WeakReference<>(pService);
        }

        @Override
        public void handleMessage(Message pMessage) {
            ServiceMediaPlayer service = this.mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
                    return;
                }
                service.stopSelf();
                service.mIsStarted = false;
            }
        }
    }

    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onSkipToPrevious() {
            ServiceMediaPlayer.this.processSkipToPreviousRequest();
        }

        @Override
        public void onPlayFromMediaId(String pMediaId, Bundle pExtras) {
            ServiceMediaPlayer.this.processPlayFromMediaIdRequest(pMediaId, pExtras);
        }

        @Override
        public void onPlay() {
            ServiceMediaPlayer.this.processPlayRequest();
        }

        @Override
        public void onPause() {
            ServiceMediaPlayer.this.processPauseRequest();
        }

        @Override
        public void onStop() {
            ServiceMediaPlayer.this.processStopRequest(null);
        }

        @Override
        public void onSeekTo(long pPosition) {
            ServiceMediaPlayer.this.processSeekToRequest((int) pPosition);
        }

        @Override
        public void onSkipToNext() {
            ServiceMediaPlayer.this.processSkipToNextRequest();
        }
    }

    public static class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            if (pIntent != null) {
                String action = pIntent.getAction();
                if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
                    KeyEvent keyEvent = (KeyEvent) pIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        String command = null;
                        int keyCode = keyEvent.getKeyCode();
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                command = ServiceMediaPlayer.COMMAND__SKIP_TO_PREVIOUS;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_REWIND:
                                command = ServiceMediaPlayer.COMMAND__REWIND;
                                break;
                            case KeyEvent.KEYCODE_HEADSETHOOK:
                            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                command = ServiceMediaPlayer.COMMAND__PLAY_PAUSE;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PLAY:
                                command = ServiceMediaPlayer.COMMAND__PLAY;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                command = ServiceMediaPlayer.COMMAND__PAUSE;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_STOP:
                                command = ServiceMediaPlayer.COMMAND__STOP;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                                command = ServiceMediaPlayer.COMMAND__FAST_FORWARD;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_NEXT:
                                command = ServiceMediaPlayer.COMMAND__SKIP_TO_NEXT;
                                break;
                            default:
                                break;
                        }
                        if (!UtilsString.isEmpty(command)) {
                            Intent intent = new Intent(ServiceMediaPlayer.ACTION__MEDIA_COMMAND);
                            intent.putExtra(ServiceMediaPlayer.COMMAND__NAME, command);
                            pContext.startService(intent);
                        }
                    }
                }
            }
        }
    }
}
