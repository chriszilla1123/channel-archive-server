package com.chillteq.channel_archive_server.model;

import java.util.List;

public class DownloadQueueModel {
    List<Video> downloadQueue;
    List<Video> pendingDownloads;
    List<Video> completedDownloads;
    List<Video> failedDownloads;

    public DownloadQueueModel() {

    }

    public DownloadQueueModel(List<Video> downloadQueue, List<Video> pendingDownloads,
                              List<Video> completedDownloads, List<Video> failedDownloads) {
        this.downloadQueue = downloadQueue;
        this.pendingDownloads = pendingDownloads;
        this.completedDownloads = completedDownloads;
        this.failedDownloads = failedDownloads;
    }

    public List<Video> getDownloadQueue() {
        return downloadQueue;
    }

    public void setDownloadQueue(List<Video> downloadQueue) {
        this.downloadQueue = downloadQueue;
    }

    public List<Video> getPendingDownloads() {
        return pendingDownloads;
    }

    public void setPendingDownloads(List<Video> pendingDownloads) {
        this.pendingDownloads = pendingDownloads;
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
                "downloadQueue=" + downloadQueue +
                ", pendingDownloads=" + pendingDownloads +
                ", completedDownloads=" + completedDownloads +
                ", failedDownloads=" + failedDownloads +
                '}';
    }
}
