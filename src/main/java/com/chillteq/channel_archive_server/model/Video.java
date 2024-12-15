package com.chillteq.channel_archive_server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Video {
    private String id;
    private String title;
    private String url;
    private Date download_date;
    private String directory;
    private String channelName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getDownload_date() {
        return download_date;
    }

    public void setDownload_date(Date download_date) {
        this.download_date = download_date;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", upload_date='" + download_date + '\'' +
                ", directory='" + directory + '\'' +
                ", channelName='" + channelName + '\'' +
                '}';
    }
}
