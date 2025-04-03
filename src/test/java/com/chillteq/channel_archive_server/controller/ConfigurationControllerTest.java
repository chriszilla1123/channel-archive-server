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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationControllerTest {
    @Mock
    private ConfigurationService service;

    @Mock
    private YoutubeService youtubeService;

    @InjectMocks
    private ConfigurationController controller;

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
    public void testGetChannels_exception() {
        Mockito.when(service.getChannels()).thenThrow(new ConfigParseException("message"));
        assertEquals("message", controller.getChannels().getBody());
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
    public void testUpdateChannels_exception() {
        try {
            List<Channel> channels = new ArrayList<Channel>();
            Mockito.when(service.updateChannels(channels)).thenThrow(new Exception());
            assertEquals(500, controller.updateChannels(channels).getStatusCode().value());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getYtdlVersion() {
        Mockito.when(youtubeService.getYtdlVersion()).thenReturn("The newest one");
        assertEquals("The newest one", controller.getYtdlVersion().getBody());
    }

    @Test
    public void getYtdlVersion_exception() {
        Mockito.when(youtubeService.getYtdlVersion()).thenThrow(new RuntimeException());
        assertEquals(500, controller.getYtdlVersion().getStatusCode().value());
    }
}