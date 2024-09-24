package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.Service.ConfigurationService;
import com.chillteq.channel_archive_server.exception.ConfigParseException;
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
    public ResponseEntity<Object> getChannels() {
        try {
            List<Channel> channels = service.getChannels();
            return ResponseEntity.ok(channels);
        } catch (ConfigParseException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/channels/validate")
    public ResponseEntity<Object> validateChannels() {
        try {
            List<Channel> channels = service.validateChannels();
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
