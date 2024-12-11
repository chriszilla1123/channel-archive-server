package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.Service.DownloadService;
import com.chillteq.channel_archive_server.model.DownloadQueueModel;
import com.chillteq.channel_archive_server.model.DownloadStatisticsModel;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping(value = "/download")
public class DownloadController {

    @Autowired
    private DownloadService service;

    /**
     * Triggers the download process
     * @return
     */
    @PostMapping(consumes = "application/json", produces = "text/plain")
    public ResponseEntity<StreamingResponseBody> downloadArchive(@RequestBody DownloadRequestModel request) {
        StreamingResponseBody stream = outputStream -> {
            service.downloadArchive(request, outputStream);
        };
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.txt")
                .contentType(MediaType.TEXT_PLAIN).body(stream);
    }

    @GetMapping(value="queue")
    public ResponseEntity<DownloadQueueModel> getQueue() {
        return ResponseEntity.ok().body(service.getQueue());
    }

    @GetMapping(value="stats")
    public ResponseEntity<DownloadStatisticsModel> getStats() {
        return ResponseEntity.ok().body(service.getStats());
    }
}
