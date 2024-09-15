package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.Service.ConfigurationService;
import com.chillteq.channel_archive_server.model.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping(value = "/config")
public class ConfigurationController {

    @Autowired
    private ConfigurationService service;

    @GetMapping(value = "/channels")
    public ResponseEntity<List<Channel>> getChannels() {
        List<Channel> channels = service.getChannels();
        return ResponseEntity.ok(channels);
    }
}
