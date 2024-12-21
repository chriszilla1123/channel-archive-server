package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    FileService service;

    @Test
    public void testGetFileInputStream() {
        String fileContent = "Content";
        try (MockedConstruction<FileInputStream> mocked = Mockito.mockConstruction(FileInputStream.class,
                (mock, context) -> Mockito.when(mock.read(Mockito.any(byte[].class))).thenAnswer(invocation -> {
                    byte[] buffer = invocation.getArgument(0);
                    return new ByteArrayInputStream(fileContent.getBytes()).read(buffer);
                }))) {

            InputStream inputStream = service.getFileInputStream("mockPath");

            assertNotNull(inputStream);
            byte[] buffer = new byte[fileContent.length()];
            int bytesRead = inputStream.read(buffer);
            String content = new String(buffer, 0, bytesRead);
            assertEquals(fileContent, content);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testPersistChannels() {
        try {
            Channel channel = new Channel();
            List<Channel> channels = List.of(channel);
            Mockito.doNothing().when(mapper).writeValue(Mockito.any(File.class), Mockito.any());

            List<Channel> result = service.persistChannels(channels);
            assertEquals(channels, result);
            Mockito.verify(mapper, Mockito.times(1)).writeValue(Mockito.any(File.class), Mockito.eq(channels));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testPersistHistory() {
        try {
            Video video = new Video();
            List<Video> videos = List.of(video);
            Mockito.doNothing().when(mapper).writeValue(Mockito.any(File.class), Mockito.any());

            List<Video> result = service.persistHistory(videos);

            assertEquals(videos, result);
            Mockito.verify(mapper, Mockito.times(1)).writeValue(Mockito.any(File.class), Mockito.eq(videos));
        } catch (Exception e) {
            fail();
        }
    }
}