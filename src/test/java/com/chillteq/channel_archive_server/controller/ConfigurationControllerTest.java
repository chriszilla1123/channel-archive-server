package com.chillteq.channel_archive_server.controller;

import com.chillteq.channel_archive_server.service.ConfigurationService;
import com.chillteq.channel_archive_server.service.YoutubeService;
import com.chillteq.channel_archive_server.exception.ConfigParseException;
import com.chillteq.channel_archive_server.model.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest
class ConfigurationControllerTest {
    @Mock
    private ConfigurationService service;

    @Mock
    private YoutubeService youtubeService;

    @InjectMocks
    private ConfigurationController controller;

    @Value("${USER}")
    public String username;

    @Value("${PASS}")
    public String password;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getChannels() {
        Channel channel = new Channel();
        channel.setChannelName("NASASpaceFlight");
        List<Channel> channels = List.of(channel);
        Mockito.when(service.getChannels()).thenReturn(channels);
        try {
            assertEquals("NASASpaceFlight", ((List<Channel>) controller.getChannels().getBody()).getFirst().getChannelName());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void updateChannels() {
        try {
            Channel channel = new Channel();
            channel.setChannelName("NASASpaceFlight");
            List<Channel> channels = List.of(channel);
            Mockito.when(service.updateChannels(channels)).thenReturn(channels);
            assertEquals("NASASpaceFlight", ((List<Channel>) controller.updateChannels(channels).getBody()).getFirst().getChannelName());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getYtdlVersion() {
        Mockito.when(youtubeService.getYtdlVersion()).thenReturn("The newest one");
        assertEquals("The newest one", controller.getYtdlVersion().getBody());
    }
}