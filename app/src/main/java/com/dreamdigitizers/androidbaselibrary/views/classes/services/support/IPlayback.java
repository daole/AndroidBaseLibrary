package com.dreamdigitizers.androidbaselibrary.views.classes.services.support;

import android.support.v4.media.session.MediaSessionCompat;

public interface IPlayback {
    //void start();// Unused now

    void play(MediaSessionCompat.QueueItem pItem);

    void pause();

    void stop(boolean pNotifyListeners);

    void seekTo(int pPosition);

    //boolean isConnected();

    boolean isPlaying();

    //void setCurrentMediaId(String pCurrentMediaId);

    //String getCurrentMediaId();

    void setState(int pState);

    int getState();

    int getCurrentStreamPosition();

    //void setCurrentStreamPosition(int pPosition);

    //void updateLastKnownStreamPosition();// Unused now

    void setCallback(ICallback pCallback);

    interface ICallback {
        void onCompletion();
        void onPlaybackStatusChanged(int pState);
        void onError(String pError);
        //void onMetadataChanged(String pMediaId);// Unused now
    }
}
