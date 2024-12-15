package com.chillteq.channel_archive_server.model;

public class DownloadStatisticsModel {
    long pendingDownloads;
    long inProgressDownloads;
    long completedDownloads;
    long failedDownloads;

    public DownloadStatisticsModel() {

    }

    public DownloadStatisticsModel(long pendingDownloads, long inProgressDownloads,
                                   long completedDownloads, long failedDownloads) {
        this.pendingDownloads = pendingDownloads;
        this.inProgressDownloads = inProgressDownloads;
        this.completedDownloads = completedDownloads;
        this.failedDownloads = failedDownloads;
    }

    public long getPendingDownloads() {
        return pendingDownloads;
    }

    public void setPendingDownloads(long pendingDownloads) {
        this.pendingDownloads = pendingDownloads;
    }

    public long getInProgressDownloads() {
        return inProgressDownloads;
    }

    public void setInProgressDownloads(long inProgressDownloads) {
        this.inProgressDownloads = inProgressDownloads;
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
                "downloadQueue=" + pendingDownloads +
                ", inProgressDownloads=" + inProgressDownloads +
                ", completedDownloads=" + completedDownloads +
                ", failedDownloads=" + failedDownloads +
                '}';
    }
}
