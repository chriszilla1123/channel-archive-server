package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.Service.DownloadService;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> downloadVideos(@RequestBody(required = false) DownloadRequestModel requestModel) {
        service.downloadVideos();
        return ResponseEntity.ok().build();
    }
}
