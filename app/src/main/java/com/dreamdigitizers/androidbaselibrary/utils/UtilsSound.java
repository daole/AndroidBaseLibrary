package com.dreamdigitizers.androidbaselibrary.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.IOException;

public class UtilsSound {
    private static MediaPlayer mediaPlayer;

    public static boolean isPlaying() {
        if (UtilsSound.mediaPlayer == null) {
            return false;
        }
        return UtilsSound.mediaPlayer.isPlaying();
    }

    public static void playCameraShutterSound(Context pContext) {
        AudioManager audioManager = (AudioManager) pContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
    }

    public static void playAlarmSound(Context pContext) {
        try {
            Uri uri = UtilsSound.getAlarmUri();

            final AudioManager audioManager = (AudioManager) pContext.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                if (UtilsSound.mediaPlayer == null) {
                    UtilsSound.mediaPlayer = new MediaPlayer();
                }
                UtilsSound.mediaPlayer.setDataSource(pContext, uri);
                UtilsSound.mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                UtilsSound.mediaPlayer.prepare();
                UtilsSound.mediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (UtilsSound.mediaPlayer != null) {
            UtilsSound.mediaPlayer.stop();
        }
    }

    public static void reset() {
        if (UtilsSound.mediaPlayer != null) {
            UtilsSound.mediaPlayer.reset();
        }
    }

    public static void release() {
        if (UtilsSound.mediaPlayer != null) {
            UtilsSound.mediaPlayer.release();
            UtilsSound.mediaPlayer = null;
        }
    }

    private static Uri getAlarmUri() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        return uri;
    }
}
