package com.dreamdigitizers.androidbaselibrary.views.classes.services.support;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

public class CustomQueueItem {
    private MediaSessionCompat.QueueItem mQueueItem;
    private MediaMetadataCompat mMediaMetadata;
    private String mStreamUrl;

    public CustomQueueItem(MediaSessionCompat.QueueItem pQueueItem, MediaMetadataCompat pMediaMetadata, String pStreamUrl) {
        this.mQueueItem = pQueueItem;
        this.mMediaMetadata = pMediaMetadata;
        this.mStreamUrl = pStreamUrl;
    }

    public MediaSessionCompat.QueueItem getQueueItem() {
        return this.mQueueItem;
    }

    public void setQueueItem(MediaSessionCompat.QueueItem pQueueItem) {
        this.mQueueItem = pQueueItem;
    }

    public MediaMetadataCompat getMediaMetadata() {
        return this.mMediaMetadata;
    }

    public void setMediaMetadata(MediaMetadataCompat pMediaMetadata) {
        this.mMediaMetadata = pMediaMetadata;
    }

    public String getStreamUrl() {
        return this.mStreamUrl;
    }

    public void setStreamUrl(String pStreamUrl) {
        this.mStreamUrl = pStreamUrl;
    }
}
