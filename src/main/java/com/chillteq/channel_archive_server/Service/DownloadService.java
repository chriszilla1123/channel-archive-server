package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.DownloadQueueModel;
import com.chillteq.channel_archive_server.model.DownloadStatisticsModel;
import com.chillteq.channel_archive_server.model.Video;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import com.chillteq.channel_archive_server.util.OutputStreamUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
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

    private BlockingQueue<Video> downloadQueue = new LinkedBlockingQueue<>();
    private ConcurrentMap<String, Video> pendingDownloads = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Video> completedDownloads = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Video> failedDownloads = new ConcurrentHashMap<>();

    /**
     * Downloads all videos from the configured list of channels
     * @param request
     * @param outputStream
     * @return the list of channels and videos to be downloaded
     */
    public List<Channel> downloadArchive(DownloadRequestModel request, OutputStream outputStream) {
        logger.info("Download Archive process started with request: {}", request);
        if(request.isDryRun()) {
            OutputStreamUtility.writeLine(outputStream, "Starting in dry-run mode. no videos will actually be downloaded\n");
        }
        try {
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

                //Add videos to the download queue
                if(!filteredVideos.isEmpty()) {
                    channel.setVideos(filteredVideos);
                    if(!request.isDryRun()) {
                        downloadQueue.addAll(filteredVideos);
                    }
                }
            });
            if(!request.isDryRun()) {
                startDownloadQueue();
            }
            return channels;
        } catch (Exception e) {
            OutputStreamUtility.writeLine(outputStream, e.getMessage());
            throw e;
        }
    }

    private void startDownloadQueue() {
        Video video;
        while ((video = downloadQueue.poll()) != null) {
           pendingDownloads.put(video.getId(), video);
           try {
               youtubeService.downloadVideo(video.getUrl(), video.getDirectory());
               pendingDownloads.remove(video.getId());
               completedDownloads.put(video.getId(), video);
           } catch (Exception e) {
               pendingDownloads.remove(video.getId());
               failedDownloads.put(video.getId(), video);
           }
        }
    }

    public DownloadQueueModel getQueue() {
        return new DownloadQueueModel(downloadQueue.stream().toList(), pendingDownloads.values().stream().toList(),
                completedDownloads.values().stream().toList(), failedDownloads.values().stream().toList());
    }

    public DownloadStatisticsModel getStats() {
        return new DownloadStatisticsModel(downloadQueue.stream().toList().size(), pendingDownloads.values().stream().toList().size(),
                completedDownloads.values().stream().toList().size(), failedDownloads.values().stream().toList().size());
    }
}
