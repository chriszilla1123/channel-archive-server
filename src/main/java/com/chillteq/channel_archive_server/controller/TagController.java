package com.chillteq.channel_archive_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/tag")
public class TagController {

    @PutMapping(value = "set")
    public ResponseEntity<HttpStatus> setTags() {
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
