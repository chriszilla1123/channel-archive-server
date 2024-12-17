package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.DownloadQueueModel;
import com.chillteq.channel_archive_server.model.DownloadStatisticsModel;
import com.chillteq.channel_archive_server.model.Video;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
public class DownloadService {
    Logger logger = LoggerFactory.getLogger(DownloadService.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private YoutubeService youtubeService;

    private BlockingQueue<Video> pendingDownloads = new LinkedBlockingQueue<>();
    private ConcurrentMap<String, Video> inProgressDownloads = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Video> completedDownloads = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Video> failedDownloads = new ConcurrentHashMap<>();

    /**
     * Downloads all videos from the configured list of channels
     * @param request
     * @return the list of channels and videos to be downloaded
     */
    public List<Channel> downloadArchive(DownloadRequestModel request) {
        logger.info("Download Archive process started with request: {}", request);
        List<Channel> channels = configurationService.getChannels();
        channels.forEach(channel -> {
            //Fetch all videos available on channel
            String logMessage = String.format("Processing channel: [name: %s, id: %s, directory: %s]", channel.getChannelName(), channel.getChannelId(), channel.getChannelDir());
            logger.info(logMessage);
            channel.setVideos(youtubeService.getVideoMetadataByChannel(channel));

            //Filter out videos already downloaded
            List<Video> filteredVideos = fileService.filterDownloadedVideosFromChannel(channel);
            channel.setVideos(filteredVideos);
            logMessage = String.format("\tFound %s videos on channel: downloading %s and skipping %s already downloaded",
                    channel.getVideos().size(), filteredVideos.size(), channel.getVideos().size() - filteredVideos.size());
            logger.info(logMessage);

            //Add videos to the download queue
            if(!request.isDryRun()) {
                pendingDownloads.addAll(filteredVideos);
            }
        });
        if(!request.isDryRun()) {
                startDownloadQueue();
        }
        return channels;
    }

    private void startDownloadQueue() {
        Video video;
        while ((video = pendingDownloads.poll()) != null) {
           inProgressDownloads.put(video.getId(), video);
           try {
               youtubeService.downloadVideo(video);
               inProgressDownloads.remove(video.getId());
               completedDownloads.put(video.getId(), video);
           } catch (Exception e) {
               inProgressDownloads.remove(video.getId());
               failedDownloads.put(video.getId(), video);
           }
        }
    }

    public DownloadQueueModel getQueue() {
        return new DownloadQueueModel(pendingDownloads.stream().toList(), inProgressDownloads.values().stream().toList(),
                completedDownloads.values().stream().toList(), failedDownloads.values().stream().toList());
    }

    public DownloadStatisticsModel getStats() {
        return new DownloadStatisticsModel(pendingDownloads.stream().toList().size(), inProgressDownloads.values().stream().toList().size(),
                completedDownloads.values().stream().toList().size(), failedDownloads.values().stream().toList().size());
    }
}
