package com.chillteq.channel_archive_api.model;

import java.util.ArrayList;

public class Channel {
    private String channelName;
    private String channelId;
    private String channelDir;
    private String channelUrl;
    private String playlistUrl;
    private ArrayList<Video> videos;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelDir() {
        return channelDir;
    }

    public void setChannelDir(String channelDir) {
        this.channelDir = channelDir;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }
}
