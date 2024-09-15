package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class ConfigurationService {
    private String userDefinedConfigFileLocation = null;
    private String exampleConfigFileLocation = "example.config";

    public List<Channel> getChannels() {
        String configFileLocation = userDefinedConfigFileLocation != null ? userDefinedConfigFileLocation : exampleConfigFileLocation;
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileLocation);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(in);
            List<Channel> channels = mapper.convertValue(jsonNode, new TypeReference<List<Channel>>(){});
            setURLs(channels);
            return channels;
        } catch (Exception e) {
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
