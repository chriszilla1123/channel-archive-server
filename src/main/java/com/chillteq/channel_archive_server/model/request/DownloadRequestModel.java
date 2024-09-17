package com.chillteq.channel_archive_server.model.request;

public class DownloadRequestModel {
    boolean dryRun = true;

    public boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public String toString() {
        return "DownloadRequestModel{" +
                "dryRun=" + dryRun +
                '}';
    }
}
