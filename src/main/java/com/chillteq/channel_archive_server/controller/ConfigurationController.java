package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.service.ConfigurationService;
import com.chillteq.channel_archive_server.service.YoutubeService;
import com.chillteq.channel_archive_server.exception.ConfigParseException;
import com.chillteq.channel_archive_server.model.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping(value = "/config")
public class ConfigurationController {

    @Autowired
    private ConfigurationService service;

    @Autowired
    private YoutubeService youtubeService;

    @GetMapping(value = "/channels")
    public ResponseEntity<List<Channel>> getChannels() {
        List<Channel> channels = service.getChannels();
        return ResponseEntity.ok(channels);
    }

    @PutMapping("/channels/update")
    public ResponseEntity<List<Channel>> updateChannels(@RequestBody List<Channel> channels) {
        List<Channel> updatedChannels = service.updateChannels(channels);
        return ResponseEntity.ok(updatedChannels);
    }

    @GetMapping("/ytdl/version")
    public ResponseEntity<String> getYtdlVersion() {
        String version = youtubeService.getYtdlVersion();
        return ResponseEntity.ok(version);
    }
}
