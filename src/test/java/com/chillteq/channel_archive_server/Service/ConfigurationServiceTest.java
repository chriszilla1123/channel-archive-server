package com.chillteq.channel_archive_server.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.chillteq.channel_archive_server.exception.ConfigParseException;
import com.chillteq.channel_archive_server.model.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {
    private ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> listAppender;
    private final String userDefinedConfigFileLocation = "/userDefined.config";

    @Mock
    private FileService fileService;

    @Mock
    private YoutubeService youtubeService;

    @InjectMocks
    private ConfigurationService service;

    @BeforeEach
    public void setup() {
        //Test logs printed by ConfigurationService
        Logger logger = (Logger) LoggerFactory.getLogger(ConfigurationService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    /**
     * Tests basic parsing of a valid channel.
     */
    @Test
    public void testGetChannels() {
        try {
            String mockJson = "[\n" +
                    "    {\n" +
                    "        \"channelName\": \"NASASpaceFlight\",\n" +
                    "        \"channelId\": \"@NASASpaceflight\",\n" +
                    "        \"channelDir\": \"nsf\"\n" +
                    "    }\n" +
                    "]\n";
            InputStream mockInputStream = new ByteArrayInputStream(mockJson.getBytes());
            Mockito.when(fileService.getFileInputStream(userDefinedConfigFileLocation)).thenReturn(mockInputStream);
            List<Channel> channels = service.getChannels();
            Mockito.verify(fileService, Mockito.times(1)).getFileInputStream(userDefinedConfigFileLocation);
            assertEquals(1, channels.size());
            assertEquals("NASASpaceFlight", channels.getFirst().getChannelName());
            assertEquals("@NASASpaceflight", channels.getFirst().getChannelId());
            assertEquals("nsf", channels.getFirst().getChannelDir());
            assertEquals("https://www.youtube.com/@NASASpaceflight/videos", channels.getFirst().getChannelUrl());
            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList)
                    .extracting("message")
                    .contains("Using user defined config file");
        } catch (FileNotFoundException ex) {
            fail();
        }
    }

    /**
     * Verifies ConfigParseException is thrown when user-defined config is present
     * and contains a JSON syntax error.
     */
    @Test
    public void testGetChannels_invalidJson() {
        try {
            String mockJson = "[\n" +
                    "    {\n" +
                    "        \"channelName\": \"NASASpaceFlight\",\n" +
                    "        \"channelId\": \"@NASASpaceflight\",\n" +
                    "        \"channelDir\": \"nsf\"\n" +
                    "    }\n" +
                    "\n"; //Removed closing ]
            InputStream mockInputStream = new ByteArrayInputStream(mockJson.getBytes());
            Mockito.when(fileService.getFileInputStream(userDefinedConfigFileLocation)).thenReturn(mockInputStream);
            List<Channel> channels = service.getChannels();
            Mockito.verify(fileService, Mockito.times(1)).getFileInputStream(userDefinedConfigFileLocation);
            assertEquals(1, channels.size());
            assertEquals("NASASpaceFlight", channels.getFirst().getChannelName());
            assertEquals("@NASASpaceflight", channels.getFirst().getChannelId());
            assertEquals("nsf", channels.getFirst().getChannelDir());
            assertEquals("https://www.youtube.com/@NASASpaceflight/videos", channels.getFirst().getChannelUrl());
            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList)
                    .extracting("message")
                    .contains("Using user defined config file");
        } catch (ConfigParseException ex) {
            //This is the expected exception, pass
        } catch (Exception e) {
            fail();
        }

    }

    /**
     * Verifies that the example config file is used if no user-defined config file is found.
     */
    @Test
    public void testGetChannels_exampleFallback() {
        try {
            Mockito.when(fileService.getFileInputStream(userDefinedConfigFileLocation)).thenThrow(new FileNotFoundException());

            List<Channel> channels = service.getChannels();

            assertEquals(1, channels.size());
            assertEquals("SpaceX", channels.getFirst().getChannelName());
            assertEquals("@SpaceX", channels.getFirst().getChannelId());
            assertEquals("spacex", channels.getFirst().getChannelDir());
            assertEquals("https://www.youtube.com/@SpaceX/videos", channels.getFirst().getChannelUrl());
            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList)
                    .extracting("message")
                    .contains("User defined config file not found, falling back to example file");
        } catch (FileNotFoundException e) {
            fail();
        }
    }

    /**
     * Verifies validation occurs before channel update occurs.
     */
    @Test
    public void testUpdateChannels() {
        Channel channel = new Channel();
        channel.setChannelName("SpaceX");
        channel.setChannelId("@SpaceX");
        List<Channel> channels = List.of(channel);
        Mockito.doNothing().when(youtubeService).validateChannel(channel);
        try {
            Mockito.when(fileService.persistChannels(channels)).thenReturn(channels);
            List<Channel> persistedChannels = service.updateChannels(channels);

            Mockito.verify(youtubeService, Mockito.times(1)).validateChannel(channel);
            Mockito.verify(fileService, Mockito.times(1)).persistChannels(channels);
            assertEquals("@SpaceX", persistedChannels.get(0).getChannelId());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Verifies the new list of channels will not persist if a validation failure occurs.
     */
    @Test
    public void testUpdateChannels_validationFailure() {
        Channel channel = new Channel();
        channel.setChannelName("SpaceX");
        channel.setChannelId("@SpaceX");
        List<Channel> channels = List.of(channel);
        Mockito.doThrow(new RuntimeException()).when(youtubeService).validateChannel(channel);

        try {
            service.updateChannels(channels);
            fail();
        } catch (Exception e) {
            List<ILoggingEvent> logsList = listAppender.list;
            List<String> logMessages = logsList.stream()
                    .map(ILoggingEvent::getMessage)
                    .filter(message -> message.contains("Failed to validate channel"))
                    .collect(Collectors.toList());
            assertThat(logMessages).isNotEmpty();
        }
    }

    /**
     * Verifies that only channels passing the validation will be persisted and failed channels will be ignored.
     */
    @Test
    public void testUpdateChannels_partialValidationFailure() {
        Channel validChannel = new Channel();
        validChannel.setChannelName("Youtube");
        validChannel.setChannelId("@Youtube");

        Channel invalidChannel = new Channel();
        invalidChannel.setChannelName("SpaceX");
        invalidChannel.setChannelId("@SpaceX");
        Mockito.doThrow(new RuntimeException()).when(youtubeService).validateChannel(invalidChannel);
        Mockito.doNothing().when(youtubeService).validateChannel(validChannel);
        try {
            Mockito.when(fileService.persistChannels(Mockito.anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        } catch (Exception e) {
            fail();
        }

        List<Channel> channels = List.of(validChannel, invalidChannel);
        try {
            List<Channel> persistedChannels = service.updateChannels(channels);
            assertEquals(1, persistedChannels.size());
        } catch (Exception e) {
            List<ILoggingEvent> logsList = listAppender.list;
            List<String> logMessages = logsList.stream()
                    .map(ILoggingEvent::getMessage)
                    .filter(message -> message.contains("Failed to validate channel:"))
                    .collect(Collectors.toList());
            assertThat(logMessages).isNotEmpty();
        }
    }

    /**
     * Tests the fallback when no channels are passed and verifies the persist method is never called.
     */
    @Test
    public void testUpdateChannels_emptyList() {
        List<Channel> channels = new ArrayList<>();
        try {
            service.updateChannels(channels);
            fail();
        } catch (Exception e) {
            assertEquals("No channels passed", e.getMessage());
            Mockito.verify(youtubeService, Mockito.never()).validateChannel(Mockito.any());
            try {
                Mockito.verify(fileService, Mockito.never()).persistChannels(Mockito.any());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Tests the fallback when null channels are passed and verifies the persist method is never called.
     */
    @Test
    public void testUpdateChannels_nullList() {
        try {
            service.updateChannels(null);
            fail();
        } catch (Exception e) {
            assertEquals("No channels passed", e.getMessage());
            Mockito.verify(youtubeService, Mockito.never()).validateChannel(Mockito.any());
            try {
                Mockito.verify(fileService, Mockito.never()).persistChannels(Mockito.any());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Tests the URL parsing in the positive scenario.
     */
    @Test
    public void testSetUrls() {
        Channel channel = new Channel();
        channel.setChannelName("SpaceX");
        channel.setChannelId("@SpaceX");
        List<Channel> channels = List.of(channel);
        service.setURLs(channels);
        assertEquals("https://www.youtube.com/@SpaceX/videos", channels.getFirst().getChannelUrl());
    }

    @Test
    public void testSetUrls_invalidChannelId() {
        Channel channel = new Channel();
        channel.setChannelName("NASASpaceFlight");
        channel.setChannelId("NASASpaceFlight"); //no @
        List<Channel> channels = List.of(channel);
        try {
            service.setURLs(channels);
            fail();
        } catch (ConfigParseException e) {
            //This is the expected exception, pass
        } catch (Exception e) {
            fail();
        }
    }
}