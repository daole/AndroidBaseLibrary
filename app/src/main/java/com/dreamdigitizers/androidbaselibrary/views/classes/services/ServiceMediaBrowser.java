package com.dreamdigitizers.androidbaselibrary.views.classes.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.dreamdigitizers.androidbaselibrary.utilities.UtilsString;
import com.dreamdigitizers.androidbaselibrary.views.classes.activities.ActivityDummy;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.CustomQueueItem;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.IPlayback;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.LocalPlayback;
import com.dreamdigitizers.androidbaselibrary.views.classes.services.support.MediaPlayerNotificationReceiver;

import java.util.ArrayList;
import java.util.List;

public abstract class ServiceMediaBrowser extends MediaBrowserServiceCompat implements IPlayback.ICallback {
    public static final String ERROR_CODE__MEDIA_UNKNOWN = "-1";
    public static final String ERROR_CODE__MEDIA_SKIP = "-2";
    public static final String ERROR_CODE__MEDIA_NO_MATCHED_TRACK = "-3";
    public static final String ERROR_CODE__MEDIA_UNPLAYABLE = "-4";
    public static final String ERROR_CODE__MEDIA_INVALID_INDEX = "-5";

    public static final String ACTION__MEDIA_COMMAND = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.ACTION__MEDIA_COMMAND";
    public static final String COMMAND__NAME = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.COMMAND__NAME";
    public static final String COMMAND__SKIP_TO_PREVIOUS = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.SKIP_TO_PREVIOUS";
    public static final String COMMAND__REWIND = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.REWIND";
    public static final String COMMAND__TOGGLE_PLAYBACK = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.TOGGLE_PLAYBACK";
    public static final String COMMAND__PLAY_PAUSE = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.PLAY_PAUSE";
    public static final String COMMAND__PLAY = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.PLAY";
    public static final String COMMAND__PAUSE = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.PAUSE";
    public static final String COMMAND__STOP = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.STOP";
    public static final String COMMAND__FAST_FORWARD = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.FAST_FORWARD";
    public static final String COMMAND__SKIP_TO_NEXT = "com.dreamdigitizers.androidbaselibrary.views.classes.services.ServiceMediaBrowser.SKIP_TO_NEXT";

    protected static final float PLAYBACK_SPEED = 1.0f;
    //protected static final int STOP_DELAY = 30000;
    protected static final int REQUEST_CODE = 0;

    private List<CustomQueueItem> mPlayingQueue;
    //private DelayedStopHandler mDelayedStopHandler;
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
        //this.mDelayedStopHandler = new DelayedStopHandler(this);
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
            if (ServiceMediaBrowser.ACTION__MEDIA_COMMAND.equals(action)) {
                String command = pIntent.getStringExtra(ServiceMediaBrowser.COMMAND__NAME);
                if (!UtilsString.isEmpty(command)) {
                    switch (command) {
                        case ServiceMediaBrowser.COMMAND__SKIP_TO_PREVIOUS:
                            this.processSkipToPreviousRequest();
                            break;
                        case ServiceMediaBrowser.COMMAND__REWIND:
                            //this.handleRewindRequest();
                            break;
                        case ServiceMediaBrowser.COMMAND__PLAY_PAUSE:
                            this.processPlayPauseRequest();
                            break;
                        case ServiceMediaBrowser.COMMAND__PLAY:
                            this.processPlayRequest();
                            break;
                        case ServiceMediaBrowser.COMMAND__PAUSE:
                            this.processPauseRequest();
                            break;
                        case ServiceMediaBrowser.COMMAND__STOP:
                            this.processStopRequest(null);
                            break;
                        case ServiceMediaBrowser.COMMAND__FAST_FORWARD:
                            //this.handleFastForwardRequest();
                            break;
                        case ServiceMediaBrowser.COMMAND__SKIP_TO_NEXT:
                            this.processSkipToNextRequest();
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        //this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        //this.mDelayedStopHandler.sendEmptyMessageDelayed(0, ServiceMediaBrowser.STOP_DELAY);
        return ServiceMediaBrowser.START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.processStopRequest(null);
        //this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        this.mMediaSession.release();
    }

    @Override
    public void onTaskRemoved(Intent pRootIntent) {
        Intent intent = new Intent(this, ActivityDummy.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
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
    public void onError(String pErrorCode) {
        this.updatePlaybackState(pErrorCode);
    }

    protected void processSkipToPreviousRequest() {
        this.mCurrentIndexOnQueue--;
        if (this.mCurrentIndexOnQueue < 0 && this.mPlayingQueue != null) {
            this.mCurrentIndexOnQueue = this.mPlayingQueue.size() - 1;
        }
        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.processPlayRequest();
        } else {
            this.processStopRequest(ServiceMediaBrowser.ERROR_CODE__MEDIA_SKIP);
        }
    }

    protected void processPlayFromMediaIdRequest(String pMediaId, Bundle pExtras) {
        this.mCurrentIndexOnQueue = this.findIndexOnQueue(pMediaId, this.mPlayingQueue);
        if (this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.processPlayRequest();
        } else {
            this.processStopRequest(ServiceMediaBrowser.ERROR_CODE__MEDIA_NO_MATCHED_TRACK);
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
        //this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!this.mIsStarted) {
            //this.startService(new Intent(this, ServiceMediaBrowser.class));
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
        //this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        //this.mDelayedStopHandler.sendEmptyMessageDelayed(0, ServiceMediaBrowser.STOP_DELAY);
    }

    protected void processStopRequest(String pErrorCode) {
        this.mPlayback.stop(true);
        //this.mDelayedStopHandler.removeCallbacksAndMessages(null);
        //this.mDelayedStopHandler.sendEmptyMessageDelayed(0, ServiceMediaBrowser.STOP_DELAY);
        this.updatePlaybackState(pErrorCode);
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
            this.processStopRequest(ServiceMediaBrowser.ERROR_CODE__MEDIA_SKIP);
        }
    }

    protected void processCustomActionRequest(String pAction, Bundle pExtras) {
    }

    protected void updateMetadata() {
        if (!this.isIndexPlayable(this.mCurrentIndexOnQueue, this.mPlayingQueue)) {
            this.updatePlaybackState(ServiceMediaBrowser.ERROR_CODE__MEDIA_INVALID_INDEX);
            return;
        }

        CustomQueueItem customQueueItem = this.mPlayingQueue.get(this.mCurrentIndexOnQueue);
        MediaMetadataCompat mediaMetadata = customQueueItem.getMediaMetadata();
        this.mMediaSession.setMetadata(mediaMetadata);

        MediaDescriptionCompat mediaDescription = mediaMetadata.getDescription();
        if (mediaDescription.getIconBitmap() == null && mediaDescription.getIconUri() != null) {
            this.fetchArt(customQueueItem);
        }
    }

    protected void fetchArt(CustomQueueItem pCustomQueueItem) {
    }

    protected void updatePlaybackState(String pErrorCode) {
        /*
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (this.mPlayback != null && this.mPlayback.isConnected()) {
            position = this.mPlayback.getCurrentStreamPosition();
        }
        */
        long position = this.mPlayback.getCurrentStreamPosition();

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(this.getAvailableActions());

        int state = mPlayback.getState();

        if (pErrorCode != null) {
            stateBuilder.setErrorMessage(pErrorCode);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        stateBuilder.setState(state, position, ServiceMediaBrowser.PLAYBACK_SPEED, SystemClock.elapsedRealtime());

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
                if (UtilsString.equals(pMediaId, customQueueItem.getQueueItem().getDescription().getMediaId())) {
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
        PendingIntent mediaButtonPendingIntent = PendingIntent.getBroadcast(this, ServiceMediaBrowser.REQUEST_CODE, mediaButtonIntent, 0);

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

    /*
    protected final DelayedStopHandler getDelayedStopHandler() {
        return this.mDelayedStopHandler;
    }
    */

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

    /*
    private class DelayedStopHandler extends Handler {
        private final WeakReference<ServiceMediaBrowser> mWeakReference;

        private DelayedStopHandler(ServiceMediaBrowser pService) {
            this.mWeakReference = new WeakReference<>(pService);
        }

        @Override
        public void handleMessage(Message pMessage) {
            ServiceMediaBrowser service = this.mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
                    return;
                }
                service.stopSelf();
                service.mIsStarted = false;
            }
        }
    }
    */

    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onSkipToPrevious() {
            ServiceMediaBrowser.this.processSkipToPreviousRequest();
        }

        @Override
        public void onPlayFromMediaId(String pMediaId, Bundle pExtras) {
            ServiceMediaBrowser.this.processPlayFromMediaIdRequest(pMediaId, pExtras);
        }

        @Override
        public void onPlay() {
            ServiceMediaBrowser.this.processPlayRequest();
        }

        @Override
        public void onPause() {
            ServiceMediaBrowser.this.processPauseRequest();
        }

        @Override
        public void onStop() {
            ServiceMediaBrowser.this.processStopRequest(null);
        }

        @Override
        public void onSeekTo(long pPosition) {
            ServiceMediaBrowser.this.processSeekToRequest((int) pPosition);
        }

        @Override
        public void onSkipToNext() {
            ServiceMediaBrowser.this.processSkipToNextRequest();
        }

        @Override
        public void onCustomAction(String pAction, Bundle pExtras) {
            ServiceMediaBrowser.this.processCustomActionRequest(pAction, pExtras);
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
                                command = ServiceMediaBrowser.COMMAND__SKIP_TO_PREVIOUS;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_REWIND:
                                command = ServiceMediaBrowser.COMMAND__REWIND;
                                break;
                            case KeyEvent.KEYCODE_HEADSETHOOK:
                            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                command = ServiceMediaBrowser.COMMAND__PLAY_PAUSE;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PLAY:
                                command = ServiceMediaBrowser.COMMAND__PLAY;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                command = ServiceMediaBrowser.COMMAND__PAUSE;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_STOP:
                                command = ServiceMediaBrowser.COMMAND__STOP;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                                command = ServiceMediaBrowser.COMMAND__FAST_FORWARD;
                                break;
                            case KeyEvent.KEYCODE_MEDIA_NEXT:
                                command = ServiceMediaBrowser.COMMAND__SKIP_TO_NEXT;
                                break;
                            default:
                                break;
                        }
                        if (!UtilsString.isEmpty(command)) {
                            Intent intent = new Intent(ServiceMediaBrowser.ACTION__MEDIA_COMMAND);
                            intent.setPackage(pContext.getPackageName());
                            intent.putExtra(ServiceMediaBrowser.COMMAND__NAME, command);
                            pContext.startService(intent);
                        }
                    }
                }
            }
        }
    }
}
