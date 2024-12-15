package com.chillteq.channel_archive_server.Service;

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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private YoutubeService youtubeService;

    private final String userDefinedConfigFileLocation = "/userDefined.config";
    private final String userDefinedHistoryFileLocation = "/history.json";
    private final String exampleConfigResourceName = "example.config";

    public List<Channel> getChannels() throws ConfigParseException {
        InputStream in;
        try {
            in = new FileInputStream(userDefinedConfigFileLocation);
            logger.info("Using user defined config file");
        } catch (FileNotFoundException e) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(exampleConfigResourceName);
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
        return persistChannels(channels);
    }

    public List<Channel> persistChannels(List<Channel> channels) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(userDefinedConfigFileLocation), channels);
        } catch (IOException e) {
            logger.error("Error writing config file: {}", e.getMessage());
        }
        return channels;
    }

    public List<Channel> validateChannels() throws Exception {
        List<Channel> channels = getChannels();
        AtomicReference<String> channelName = new AtomicReference<>();
        try {
            channels.forEach(channel -> {
                channelName.set(channel.getChannelName());
                youtubeService.validateChannel(channel);
            });
        } catch (Exception e) {
            logger.error("Failed to validate channel: {} with the following exception: {}", channelName, e.getMessage());
            throw new Exception("Failed to validate channel: " + channelName + " with the following exception: " + e);
        }
        return channels;
    }

    public void setURLs(List<Channel> channels) {
        channels.forEach(this::setURL);
    }

    public void setURL(Channel channel) {
        if (channel.getChannelId().startsWith("http")
                || channel.getChannelId().startsWith("www")
                || channel.getChannelId().startsWith("youtube.com")) {
            //If user sets the full channel URL
            channel.setChannelUrl(channel.getChannelId() + "/videos");
        }
        else if (channel.getChannelId().startsWith("@")) {
            //If user sets just the @Name
            channel.setChannelUrl("https://youtube.com/" + channel.getChannelId() + "/videos");
        }
        else {
            //If user sets the channel ID
            channel.setChannelUrl("http://youtube.com" + channel.getChannelId() + "/videos");
        }
    }

    public List<Video> getHistory() throws FileNotFoundException {
        InputStream in = new FileInputStream(userDefinedHistoryFileLocation);
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

    public void setHistory(List<Video> videos) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(userDefinedHistoryFileLocation), videos);
        } catch (Exception e) {
            logger.error("Error writing history file: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Video> updateHistory(List<Video> videos) {
        logger.info("Attempting to update history with list: {}", videos);
        if(videos == null || videos.isEmpty()) {
            return null;
        }
        return persistHistory(videos);
    }

    public List<Video> persistHistory(List<Video> videos) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(userDefinedHistoryFileLocation), videos);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
        }
        return videos;
    }
}
