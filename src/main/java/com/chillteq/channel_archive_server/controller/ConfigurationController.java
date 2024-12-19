package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.Service.ConfigurationService;
import com.chillteq.channel_archive_server.Service.YoutubeService;
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
    public ResponseEntity<Object> getChannels() {
        try {
            List<Channel> channels = service.getChannels();
            return ResponseEntity.ok(channels);
        } catch (ConfigParseException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/channels/update")
    public ResponseEntity<Object> updateChannels(@RequestBody List<Channel> channels) {
        try {
            List<Channel> updatedChannels = service.updateChannels(channels);
            return ResponseEntity.ok(updatedChannels);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/ytdl/version")
    public ResponseEntity<Object> getYtdlVersion() {
        try {
            String version = youtubeService.getYtdlVersion();
            return ResponseEntity.ok(version);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
