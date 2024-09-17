package com.chillteq.channel_archive_server.model.response;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;

import java.util.List;

public class DownloadResponseModel {
    private DownloadRequestModel downloadRequestModel;
    private List<Channel> downloadDetails;
    private String message;

    public DownloadRequestModel getDownloadRequestModel() {
        return downloadRequestModel;
    }

    public void setDownloadRequestModel(DownloadRequestModel downloadRequestModel) {
        this.downloadRequestModel = downloadRequestModel;
    }

    public List<Channel> getDownloadDetails() {
        return downloadDetails;
    }

    public void setDownloadDetails(List<Channel> downloadDetails) {
        this.downloadDetails = downloadDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DownloadResponseModel{" +
                "downloadRequestModel=" + downloadRequestModel +
                ", downloadDetails=" + downloadDetails +
                ", message='" + message + '\'' +
                '}';
    }
}
