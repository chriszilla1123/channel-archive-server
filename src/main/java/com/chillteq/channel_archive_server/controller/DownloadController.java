package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.service.DownloadService;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.DownloadQueueModel;
import com.chillteq.channel_archive_server.model.DownloadStatisticsModel;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/download")
public class DownloadController {

    @Autowired
    private DownloadService service;

    /**
     * Triggers the download process
     * @return
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<Channel>> downloadArchive(@RequestBody DownloadRequestModel request) {
        return ResponseEntity.ok().body(service.downloadArchive(request));
    }

    /**
     * Adds an individual video to the download queue and starts it.
     * @return
     */

    @PostMapping(consumes = "application/json", produces = "application/json")
    @RequestMapping(value = "oneoff")
    public ResponseEntity<Boolean> downloadVideo(@RequestBody DownloadRequestModel request) {
        return ResponseEntity.ok().body(service.downloadVideo(request));
    }

    @GetMapping(value="queue")
    public ResponseEntity<DownloadQueueModel> getQueue() {
        return ResponseEntity.ok().body(service.getQueue());
    }

    @GetMapping(value="stats")
    public ResponseEntity<DownloadStatisticsModel> getStats() {
        return ResponseEntity.ok().body(service.getStats());
    }

    @GetMapping(value="clear/pending")
    public ResponseEntity<DownloadQueueModel> clearPendingDownloads() {
        return ResponseEntity.ok().body(service.clearPendingDownloads());
    }

    @GetMapping(value="clear/completed")
    public ResponseEntity<DownloadQueueModel> clearCompletedDownloads() {
        return ResponseEntity.ok().body(service.clearCompletedDownloads());
    }

    @GetMapping(value="clear/failed")
    public ResponseEntity<DownloadQueueModel> clearFailedDownloads() {
        return ResponseEntity.ok().body(service.clearFailedDownloads());
    }
}
