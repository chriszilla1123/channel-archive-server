package com.chillteq.channel_archive_server.model;

import java.util.List;

public class Channel {
    private String channelName;
    private String channelId;
    private String channelDir;
    private String channelUrl;
    private String playlistUrl;
    private List<Video> videos;

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

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelName='" + channelName + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelDir='" + channelDir + '\'' +
                ", channelUrl='" + channelUrl + '\'' +
                ", playlistUrl='" + playlistUrl + '\'' +
                ", videos=" + videos +
                '}';
    }
}
