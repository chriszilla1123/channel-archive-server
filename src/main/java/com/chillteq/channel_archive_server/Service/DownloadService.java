package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import com.chillteq.channel_archive_server.util.OutputStreamUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
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

    public List<Channel> downloadVideos(DownloadRequestModel request, OutputStream outputStream) throws IOException {
        if(request.isDryRun()) {
            OutputStreamUtility.writeLine(outputStream, "Starting in dry-run mode. no videos will actually be downloaded\n");
        }
        List<Channel> channels = configurationService.getChannels();
        channels.forEach(channel -> {
            //Fetch all videos available on channel
            String logMessage = String.format("Processing channel: [name: %s, id: %s, directory: %s]", channel.getChannelName(), channel.getChannelId(), channel.getChannelDir());
            logger.info(logMessage);
            OutputStreamUtility.writeLine(outputStream, logMessage);
            channel.setVideos(youtubeService.getVideoMetadataByChannel(channel));

            //Filter out videos already downloaded
            List<Video> filteredVideos = fileService.filterDownloadedVideosFromChannel(channel);
            logMessage = String.format("\tFound %s videos on channel: downloading %s and skipping %s already downloaded",
                    channel.getVideos().size(), filteredVideos.size(), channel.getVideos().size() - filteredVideos.size());
            logger.info(logMessage);
            OutputStreamUtility.writeLine(outputStream, logMessage);

            //Download videos
            if(!filteredVideos.isEmpty()) {
                channel.setVideos(filteredVideos);
                youtubeService.downloadVideosByChannel(channel, outputStream, request.isDryRun());
            }

        });
        return channels;
    }
}
