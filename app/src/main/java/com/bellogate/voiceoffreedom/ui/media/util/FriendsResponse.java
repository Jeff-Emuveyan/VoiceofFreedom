package com.bellogate.voiceoffreedom.ui.media.util;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FriendsResponse {
    private String title;
    private String url;
    private String thumbnailUrl;
    private String duration;


    public FriendsResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public FriendsResponse(String title, String url, String thumbnailUrl, String duration) {
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
    }
}
