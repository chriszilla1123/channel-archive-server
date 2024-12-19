package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.constant.Constants;
import com.chillteq.channel_archive_server.exception.ConfigParseException;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private YoutubeService youtubeService;

    public List<Channel> getChannels() throws ConfigParseException {
        InputStream in;
        try {
            in = fileService.getFileInputStream(Constants.userDefinedConfigFileLocation);
            logger.info("Using user defined config file");
        } catch (FileNotFoundException e) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.exampleConfigResourceName);
            logger.info("User defined config file not found, falling back to example file");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(in);
            List<Channel> channels = mapper.convertValue(jsonNode, new TypeReference<>(){});
            setURLs(channels);
            return channels;
        } catch (IOException e) {
            logger.error("Error parsing config file: {}", e.getMessage());
            throw new ConfigParseException("Error parsing config file: " + e.getMessage());
        }
    }

    public List<Channel> updateChannels(List<Channel> channels) throws Exception {
        logger.info("Attempting to update channel list with: {}", channels);
        if(channels == null || channels.isEmpty()) {
            throw new Exception("No channels passed");
        }
        try {
            channels.forEach(channel -> {
                try {
                    setURL(channel);
                    youtubeService.validateChannel(channel);
                    channel.prepareForJsonSave();
                } catch (Exception e) {
                    logger.error("Failed to validate channel: {} with the following exception: {}", channel.getChannelName(), e.getMessage());
                    throw e;
                }
            });
        } catch (Exception e) {
            throw new Exception("Failed to validate channels");
        }
        return fileService.persistChannels(channels);
    }

    public void setURLs(List<Channel> channels) {
        channels.forEach(this::setURL);
    }

    public void setURL(Channel channel) {
        if (channel.getChannelId().startsWith("@")) {
            channel.setChannelUrl("https://www.youtube.com/" + channel.getChannelId() + "/videos");
        } else {
            throw new ConfigParseException("Error: Invalid channel ID: " + channel.getChannelId());
        }
    }

    public List<Video> getHistory() throws FileNotFoundException {
        InputStream in = fileService.getFileInputStream(Constants.userDefinedHistoryFileLocation);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(in);
            List<Video> videos = mapper.convertValue(jsonNode, new TypeReference<>(){});
            return videos;
        } catch (IOException e) {
            logger.error("Error parsing history file: {}", e.getMessage());
            throw new ConfigParseException("Error parsing history file: " + e.getMessage());
        }
    }

    public List<Video> updateHistory(List<Video> videos) throws IOException {
        logger.info("Attempting to update history with list: {}", videos);
        if(videos == null || videos.isEmpty()) {
            return null;
        }
        return fileService.persistHistory(videos);
    }
}
