package com.chillteq.channel_archive_server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthCheck() {
        try {
            ResultActions response = mockMvc.perform(get("/health"));
            response.andExpect(status().isOk());
        } catch (Exception e) {
            fail();
        }
    }
}