package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.service.BrowseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping(value = "/browse")
public class BrowseController {

    @Autowired
    BrowseService service;

    @GetMapping(value = "/channels")
    public ResponseEntity<List<Channel>> getDownloadedChannels() throws IOException {
        return ResponseEntity.ok(service.getDownloadedChannels());
    }
}
