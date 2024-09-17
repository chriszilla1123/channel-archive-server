package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DownloadService {
    Logger logger = LoggerFactory.getLogger(DownloadService.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private YoutubeService youtubeService;

    public void downloadVideos() {
        List<Channel> channels = configurationService.getChannels();
        channels.forEach(channel -> {
           channel.setVideos(youtubeService.getVideoMetadataByChannel(channel));
            List<Video> filteredVideos = fileService.filterDownloadedVideosFromChannel(channel);
            logger.info("{} - found {} videos on channel, {} need to be downloaded", channel, channel.getVideos().size(), filteredVideos.size());
        });
    }
}
