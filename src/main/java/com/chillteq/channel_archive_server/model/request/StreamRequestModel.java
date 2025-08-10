package com.chillteq.channel_archive_server.model.request;

public class StreamRequestModel {
    String path;
    String rangeHeader;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRangeHeader() {
        return rangeHeader;
    }

    public void setRangeHeader(String rangeHeader) {
        this.rangeHeader = rangeHeader;
    }

    @Override
    public String toString() {
        return "StreamRequestModel{" +
                "path='" + path + '\'' +
                ", rangeHeader='" + rangeHeader + '\'' +
                '}';
    }
}
