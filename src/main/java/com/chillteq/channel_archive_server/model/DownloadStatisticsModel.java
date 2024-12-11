package com.chillteq.channel_archive_server.model;

import java.util.List;

public class DownloadStatisticsModel {
    long downloadQueue;
    long pendingDownloads;
    long completedDownloads;
    long failedDownloads;

    public DownloadStatisticsModel() {

    }

    public DownloadStatisticsModel(long downloadQueue, long pendingDownloads,
                                   long completedDownloads, long failedDownloads) {
        this.downloadQueue = downloadQueue;
        this.pendingDownloads = pendingDownloads;
        this.completedDownloads = completedDownloads;
        this.failedDownloads = failedDownloads;
    }

    public long getDownloadQueue() {
        return downloadQueue;
    }

    public void setDownloadQueue(long downloadQueue) {
        this.downloadQueue = downloadQueue;
    }

    public long getPendingDownloads() {
        return pendingDownloads;
    }

    public void setPendingDownloads(long pendingDownloads) {
        this.pendingDownloads = pendingDownloads;
    }

    public long getCompletedDownloads() {
        return completedDownloads;
    }

    public void setCompletedDownloads(long completedDownloads) {
        this.completedDownloads = completedDownloads;
    }

    public long getFailedDownloads() {
        return failedDownloads;
    }

    public void setFailedDownloads(long failedDownloads) {
        this.failedDownloads = failedDownloads;
    }

    @Override
    public String toString() {
        return "DownloadStatistics{" +
                "downloadQueue=" + downloadQueue +
                ", pendingDownloads=" + pendingDownloads +
                ", completedDownloads=" + completedDownloads +
                ", failedDownloads=" + failedDownloads +
                '}';
    }
}
