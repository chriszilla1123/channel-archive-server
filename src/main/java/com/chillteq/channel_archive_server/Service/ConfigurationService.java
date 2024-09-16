package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private final String userDefinedConfigFileLocation = "/userDefined.config";
    private final String exampleConfigResourceName = "example.config";

    public List<Channel> getChannels() {
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
            throw new RuntimeException(e);
        }
    }

    public void setURLs(List<Channel> channels) {
        channels.forEach(channel -> {
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
        });
    }
}
