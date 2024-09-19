package com.chillteq.channel_archive_server.model.request;

public class DownloadRequestModel {
    boolean dryRun = true;

    public boolean isDryRun() {
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
