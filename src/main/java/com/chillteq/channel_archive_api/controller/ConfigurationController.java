package com.chillteq.channel_archive_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/config")
public class ConfigurationController {

    @GetMapping()
    public void getConfig() {

    }
}
