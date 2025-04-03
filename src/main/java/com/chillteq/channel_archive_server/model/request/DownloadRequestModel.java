package com.chillteq.channel_archive_server.model.request;

public class DownloadRequestModel {
    boolean dryRun = true;
    String oneOffVideoUrl;
    String oneOffVideoDirectory;

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public String getOneOffVideoUrl() {
        return oneOffVideoUrl;
    }

    public void setOneOffVideoUrl(String oneOffVideoUrl) {
        this.oneOffVideoUrl = oneOffVideoUrl;
    }

    public String getOneOffVideoDirectory() {
        return oneOffVideoDirectory;
    }

    public void setOneOffVideoDirectory(String oneOffVideoDirectory) {
        this.oneOffVideoDirectory = oneOffVideoDirectory;
    }

    @Override
    public String toString() {
        return "DownloadRequestModel{" +
                "dryRun=" + dryRun +
                ", oneOffVideoUrl='" + oneOffVideoUrl + '\'' +
                ", oneOffVideoDirectory='" + oneOffVideoDirectory + '\'' +
                '}';
    }
}
