package com.dreamdigitizers.androidbaselibrary.utilities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public class UtilsMediaPlayer {
    private CustomMediaPlayer mMediaPlayer;
    private CustomMediaPlayer.IOnMediaPlayerActionResultListener mListener;

    public UtilsMediaPlayer() {
        this(null);
    }

    public UtilsMediaPlayer(CustomMediaPlayer.IOnMediaPlayerActionResultListener pListener) {
        this.mListener = pListener;
    }

    public void setOnMediaPlayerActionResultListener(CustomMediaPlayer.IOnMediaPlayerActionResultListener pListener) {
        this.mListener = pListener;
        if(this.mMediaPlayer != null) {
            this.mMediaPlayer.setOnMediaPlayerActionResultListener(this.mListener);
        }
    }

    public void createMediaPlayerIfNeeded(Context pContext) {
        if (this.mMediaPlayer == null) {
            this.mMediaPlayer = new CustomMediaPlayer();
            this.mMediaPlayer.setWakeMode(pContext, PowerManager.PARTIAL_WAKE_LOCK);
            this.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mMediaPlayer.setOnMediaPlayerActionResultListener(this.mListener);
        } else {
            this.reset();
        }
    }

    public boolean reset() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            result = true;
        }
        return result;
    }

    public boolean setDataSource(String pPath) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE) {
                try {
                    this.mMediaPlayer.setDataSource(pPath);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean setDataSource(Context pContext, Uri pUri, Map<String, String> pHeaders) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE) {
                try {
                    this.mMediaPlayer.setDataSource(pContext, pUri, pHeaders);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean setDataSource(Context pContext, Uri pUri) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE) {
                try {
                    this.mMediaPlayer.setDataSource(pContext, pUri);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean setDataSource(FileDescriptor pFileDescriptor) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE) {
                try {
                    this.mMediaPlayer.setDataSource(pFileDescriptor);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean setDataSource(FileDescriptor pFileDescriptor, long pOffset, long pLength) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE) {
                try {
                    this.mMediaPlayer.setDataSource(pFileDescriptor, pOffset, pLength);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean setDataSource(MediaDataSource pDataSource) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE && Build.VERSION.SDK_INT >= 23) {
                this.mMediaPlayer.setDataSource(pDataSource);
                result = true;
            }
        }
        return result;
    }

    public boolean prepare() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED) {
                this.mMediaPlayer.prepare();
                result = true;
            }
        }
        return result;
    }

    public boolean prepareAsync() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED) {
                this.mMediaPlayer.prepareAsync();
                result = true;
            }
        }
        return result;
    }

    public boolean start() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                this.mMediaPlayer.start();
                result = true;
            }
        }
        return result;
    }

    public boolean pause() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                this.mMediaPlayer.pause();
                result = true;
            }
        }
        return result;
    }

    public boolean stop() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                this.mMediaPlayer.stop();
                result = true;
            }
        }
        return result;
    }

    public boolean release() {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            result = true;
        }
        return result;
    }

    public boolean dispose() {
        boolean result = this.release();
        if (result) {
            this.mMediaPlayer.setOnMediaPlayerActionResultListener(null);
            this.mMediaPlayer = null;
        }
        return result;
    }

    public boolean isMediaPlayerCreated() {
        return this.mMediaPlayer != null ? true : false;
    }

    public boolean isPlaying() {
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                return this.mMediaPlayer.isPlaying();
            }
        }
        return false;
    }

    public int getCurrentPosition() {
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                return this.mMediaPlayer.getCurrentPosition();
            }
        }
        return -1;
    }

    public int getDuration() {
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                return this.mMediaPlayer.getDuration();
            }
        }
        return -1;
    }

    public boolean setVolume(float pLeftVolume, float pRightVolume) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                this.mMediaPlayer.setVolume(pLeftVolume, pRightVolume);
                result = true;
            }
        }
        return result;
    }

    public boolean seekTo(int pMilliseconds) {
        boolean result = false;
        if (this.mMediaPlayer != null) {
            int currentState = this.mMediaPlayer.getCurrentState();
            if (currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED ||
                    currentState == CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED) {
                this.mMediaPlayer.seekTo(pMilliseconds);
                result = true;
            }
        }
        return result;
    }

    public static class CustomMediaPlayer extends MediaPlayer
            implements MediaPlayer.OnPreparedListener,
            MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener {
        public static final int MEDIA_PLAYER_STATE__ERROR = -1;
        public static final int MEDIA_PLAYER_STATE__IDLE = 0;
        public static final int MEDIA_PLAYER_STATE__INITIALIZED = 1;
        public static final int MEDIA_PLAYER_STATE__PREPARING = 2;
        public static final int MEDIA_PLAYER_STATE__PREPARED = 3;
        public static final int MEDIA_PLAYER_STATE__STARTED = 4;
        public static final int MEDIA_PLAYER_STATE__PAUSED = 5;
        public static final int MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED = 6;
        public static final int MEDIA_PLAYER_STATE__STOPPED = 7;
        public static final int MEDIA_PLAYER_STATE__END = 8;

        private int mCurrentState;

        private IOnMediaPlayerActionResultListener mListener;

        public CustomMediaPlayer() {
            this.setOnPreparedListener(this);
            this.setOnCompletionListener(this);
            this.setOnErrorListener(this);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE;
        }

        @Override
        public void reset() {
            super.reset();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__IDLE;
        }

        @Override
        public void setDataSource(String pPath) throws IOException {
            super.setDataSource(pPath);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED;
        }

        @Override
        public void setDataSource(Context pContext, Uri pUri, Map<String, String> pHeaders) throws IOException {
            super.setDataSource(pContext, pUri, pHeaders);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED;
        }

        @Override
        public void setDataSource(Context pContext, Uri pUri) throws IOException {
            super.setDataSource(pContext, pUri);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED;
        }

        @Override
        public void setDataSource(FileDescriptor pFileDescriptor) throws IOException {
            super.setDataSource(pFileDescriptor);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED;
        }

        @Override
        public void setDataSource(FileDescriptor pFileDescriptor, long pOffset, long pLength) throws IOException {
            super.setDataSource(pFileDescriptor, pOffset, pLength);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED;
        }

        @Override
        public void setDataSource(MediaDataSource pDataSource) {
            super.setDataSource(pDataSource);
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__INITIALIZED;
        }

        @Override
        public void prepareAsync() {
            super.prepareAsync();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARING;
        }

        @Override
        public void prepare() {
            super.prepareAsync();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED;
        }

        @Override
        public void start() {
            super.start();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__STARTED;
        }

        @Override
        public void pause() {
            super.pause();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__PAUSED;
        }

        @Override
        public void stop() {
            super.stop();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__STOPPED;
        }

        @Override
        public void release() {
            super.release();
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__END;
        }

        @Override
        public void onPrepared(MediaPlayer pMediaPlayer) {
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__PREPARED;
            if (this.mListener != null) {
                this.mListener.onPrepared(this);
            }
        }

        @Override
        public void onCompletion(MediaPlayer pMediaPlayer) {
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__PLAYBACK_COMPLETED;
            if (this.mListener != null) {
                this.mListener.onCompletion(this);
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer pMediaPlayer) {
            if (this.mListener != null) {
                this.mListener.onSeekComplete(this);
            }
        }

        @Override
        public boolean onError(MediaPlayer pMediaPlayer, int pWhat, int pExtra) {
            this.mCurrentState = CustomMediaPlayer.MEDIA_PLAYER_STATE__ERROR;
            if (this.mListener != null) {
                return this.mListener.onError(this, pWhat, pExtra);
            }
            return false;
        }

        public int getCurrentState() {
            return this.mCurrentState;
        }

        public void setOnMediaPlayerActionResultListener(IOnMediaPlayerActionResultListener pListener) {
            this.mListener = pListener;
        }

        public interface IOnMediaPlayerActionResultListener {
            void onPrepared(CustomMediaPlayer pMediaPlayer);
            void onSeekComplete(CustomMediaPlayer pMediaPlayer);
            void onCompletion(CustomMediaPlayer pMediaPlayer);
            boolean onError(CustomMediaPlayer pMediaPlayer, int pWhat, int pExtra);
        }
    }
}
