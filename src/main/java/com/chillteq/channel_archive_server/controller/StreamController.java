package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.service.BrowseService;
import com.chillteq.channel_archive_server.service.StreamService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

@RestController()
@RequestMapping(value = "/stream")
public class StreamController {
    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);


    @Autowired
    StreamService service;

    @GetMapping(value = "/video")
    public ResponseEntity<StreamingResponseBody> streamVideo(
            @RequestParam String path,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletResponse response) throws IOException {

        try {
            StreamingResponseBody stream = service.streamVideo(path, rangeHeader, response);
            return ResponseEntity
                    .status(response.getStatus())
                    .header("Content-Type", "video/mp4")
                    .header("Accept-Ranges", "bytes")
                    .body(stream);
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", path);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "text/plain")
                    .body(outputStream -> outputStream.write("Video not found.".getBytes()));
        } catch (Exception e) {
            logger.error("Error streaming video", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "text/plain")
                    .body(outputStream -> outputStream.write("Internal server error.".getBytes()));
        }
    }
}
