package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.Service.DownloadService;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
    @PostMapping(consumes = "application/json", produces = "text/plain")
    public ResponseEntity<StreamingResponseBody> downloadVideos(@RequestBody(required = false) DownloadRequestModel requestModel) {
        StreamingResponseBody stream = outputStream -> {
            service.downloadVideos(outputStream);
        };
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.txt")
                .contentType(MediaType.TEXT_PLAIN).body(stream);
    }
}
