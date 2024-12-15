package com.chillteq.channel_archive_server.model;

import java.util.List;

public class DownloadQueueModel {
    List<Video> pendingDownloads;
    List<Video> inProgressDownloads;
    List<Video> completedDownloads;
    List<Video> failedDownloads;

    public DownloadQueueModel() {

    }

    public DownloadQueueModel(List<Video> pendingDownloads, List<Video> inProgressDownloads,
                              List<Video> completedDownloads, List<Video> failedDownloads) {
        this.pendingDownloads = pendingDownloads;
        this.inProgressDownloads = inProgressDownloads;
        this.completedDownloads = completedDownloads;
        this.failedDownloads = failedDownloads;
    }

    public List<Video> getPendingDownloads() {
        return pendingDownloads;
    }

    public void setPendingDownloads(List<Video> pendingDownloads) {
        this.pendingDownloads = pendingDownloads;
    }

    public List<Video> getInProgressDownloads() {
        return inProgressDownloads;
    }

    public void setInProgressDownloads(List<Video> inProgressDownloads) {
        this.inProgressDownloads = inProgressDownloads;
    }

    public List<Video> getCompletedDownloads() {
        return completedDownloads;
    }

    public void setCompletedDownloads(List<Video> completedDownloads) {
        this.completedDownloads = completedDownloads;
    }

    public List<Video> getFailedDownloads() {
        return failedDownloads;
    }

    public void setFailedDownloads(List<Video> failedDownloads) {
        this.failedDownloads = failedDownloads;
    }

    @Override
    public String toString() {
        return "DownloadStatistics{" +
                "pendingDownloads=" + pendingDownloads +
                ", inProgressDownloads=" + inProgressDownloads +
                ", completedDownloads=" + completedDownloads +
                ", failedDownloads=" + failedDownloads +
                '}';
    }
}
