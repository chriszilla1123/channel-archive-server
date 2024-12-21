package com.chillteq.channel_archive_server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents the history.json file storing completed and failed downloads
 */
public class DownloadHistory {
    private Collection<Video> completed;
    private Collection<Video> failed;

    public DownloadHistory() {
        completed = new ArrayList<>();
        failed = new ArrayList<>();
    }

    public DownloadHistory(Collection<Video> completed, Collection<Video> failed) {
        this.completed = completed;
        this.failed = failed;
    }

    public Collection<Video> getCompleted() {
        return completed;
    }

    public void setCompleted(List<Video> completed) {
        this.completed = completed;
    }

    public Collection<Video> getFailed() {
        return failed;
    }

    public void setFailed(Collection<Video> failed) {
        this.failed = failed;
    }

    @Override
    public String toString() {
        return "History{" +
                "completed=" + completed +
                ", failed=" + failed +
                '}';
    }
}
