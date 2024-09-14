package com.chillteq.channel_archive_api.Service;

import com.chillteq.channel_archive_api.model.Channel;
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
            return channels;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
