package com.chillteq.channel_archive_server.service;

import com.chillteq.channel_archive_server.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BrowseService {
    private static final Logger logger = LoggerFactory.getLogger(BrowseService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private ConfigurationService configurationService;

    /**
     * Gets all channels saved in the defined downloads folder, by directory name
     * Includes all videos in the channels folder, and includes channels not configured in the user-config.
     * If the directory name matches with a configured channel, gets the user-defined channel name from that.
     *
     * @return list of saved channels with their videos.
     */
    public List<Channel> getDownloadedChannels() throws IOException {
        List<Channel> configuredChannels = configurationService.getChannels();
        List<Channel> downloadedChannels = fileService.getDownloadedChannels();
        downloadedChannels.forEach((Channel downloadedChannel) -> {
            Channel matchedChannel = configuredChannels.stream().filter((Channel configuredChannel) -> configuredChannel.getChannelDir().equals(downloadedChannel.getChannelDir())).findFirst().orElse(null);
            if (null != matchedChannel) {
                downloadedChannel.setChannelName(matchedChannel.getChannelName());
                downloadedChannel.setChannelId(matchedChannel.getChannelId());
            }
        });
        return downloadedChannels;
    }
}
