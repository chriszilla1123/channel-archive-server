package com.chillteq.channel_archive_server.service;

import com.chillteq.channel_archive_server.exception.YoutubeDownloadException;
import com.chillteq.channel_archive_server.model.*;
import com.chillteq.channel_archive_server.model.request.DownloadRequestModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DownloadService {
    Logger logger = LoggerFactory.getLogger(DownloadService.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private YoutubeService youtubeService;

    @Value("${DEMO:#{false}}")
    private boolean isDemoMode;

    private ConcurrentMap<String, Video> inProgressDownloads = new ConcurrentHashMap<>();
    private BlockingQueue<Video> pendingDownloads = new LinkedBlockingQueue<>();
    private ConcurrentMap<String, Video> completedDownloads = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Video> failedDownloads = new ConcurrentHashMap<>();
    private AtomicBoolean downloadInProgress = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        try {
            DownloadHistory history = configurationService.getHistory();
            completedDownloads = new ConcurrentHashMap<>();
            failedDownloads = new ConcurrentHashMap<>();
            history.getCompleted().forEach(video -> {
                completedDownloads.put(video.getId(), video);
            });
            history.getFailed().forEach(video -> {
                failedDownloads.put(video.getId(), video);
            });
        } catch (FileNotFoundException e) {
            logger.info("History file not found");
        } catch (Exception e) {
            logger.info("History empty or failed to process");
        }
    }

    /**
     * Downloads all videos from the configured list of channels
     * @param request
     * @return the list of channels and videos to be downloaded
     */
    public List<Channel> downloadArchive(DownloadRequestModel request) {
        logger.info("Download Archive process started with request: {}", request);
        List<Channel> channels = configurationService.getChannels();
        for (Channel channel: channels) {
            //Fetch all videos available on channel
            logger.info("Processing channel: {}", channel.toShortString());
            try {
                channel.setVideos(youtubeService.getVideoMetadataByChannel(channel));
            } catch (YoutubeDownloadException ytdlException) {
                logger.error("Failed to process channel, skipping: {}", channel.toShortString());
                continue;
            }

            //Filter out videos already downloaded
            List<Video> filteredVideos = fileService.filterDownloadedVideosFromChannel(channel);
            logger.info("Found {} videos on channel. Downloading {} and skipping {} already downloaded",
                    channel.getVideos().size(), filteredVideos.size(), channel.getVideos().size() - filteredVideos.size());

            //Add videos to the download queue
            channel.setVideos(filteredVideos);
            if(!request.isDryRun()) {
                pendingDownloads.addAll(filteredVideos);
            }
        }
        if(!request.isDryRun()) {
                startDownloadQueue();
        }
        return channels;
    }

    public boolean downloadVideo(DownloadRequestModel request) {
        if ( null == request.getOneOffVideoUrl() || request.getOneOffVideoUrl().isBlank()) {
            String logText = String.format("A video URL is required when downloading an individual video. %s", request);
            logger.error(logText);
            throw new IllegalArgumentException(logText);
        }
        Video video = youtubeService.getVideoMetadataByVideoUrl(request.getOneOffVideoUrl());
        if (null != request.getOneOffVideoDirectory() && !request.getOneOffVideoDirectory().isBlank()) {
            video.setDirectory(request.getOneOffVideoDirectory());
        }
        if(!request.isDryRun()) {
            pendingDownloads.add(video);
            startDownloadQueue();
        }
        return true;
    }

    private void startDownloadQueue() {
        if (!downloadInProgress.compareAndSet(false, true)) {
            return;
        }
        Video video;
        while ((video = pendingDownloads.poll()) != null) {
           inProgressDownloads.put(video.getId(), video);
           try {
               if(isDemoMode) {
                   logger.info("startDownloadQueue() - skipping download because demo mode is set to true: {}", video.getTitle());
                   Thread.sleep(10000);
               } else {
                   youtubeService.downloadVideo(video);
               }
               inProgressDownloads.remove(video.getId());
               completedDownloads.put(video.getId(), video);
               updateHistory();
           } catch (Exception e) {
               inProgressDownloads.remove(video.getId());
               failedDownloads.put(video.getId(), video);
               updateHistory();
           }
        }
        downloadInProgress.set(false);
    }

    public DownloadQueueModel getQueue() {
        return new DownloadQueueModel(pendingDownloads.stream().toList(), inProgressDownloads.values().stream().toList(),
                completedDownloads.values().stream().toList(), failedDownloads.values().stream().toList());
    }

    public DownloadStatisticsModel getStats() {
        return new DownloadStatisticsModel(pendingDownloads.stream().toList().size(), inProgressDownloads.values().stream().toList().size(),
                completedDownloads.values().stream().toList().size(), failedDownloads.values().stream().toList().size());
    }

    public DownloadQueueModel clearPendingDownloads() {
        logger.info("clearPendingDownloads() - Clearing pending downloads upon request");
        this.pendingDownloads.clear();
        return getQueue();
    }

    public DownloadQueueModel clearCompletedDownloads() {
        logger.info("clearCompletedDownloads() - Clearing completed download history upon request");
        this.completedDownloads.clear();
        return getQueue();
    }

    public DownloadQueueModel clearFailedDownloads() {
        logger.info("clearFailedDownloads() - Clearing failed download history upon request");
        this.failedDownloads.clear();
        return getQueue();
    }

    private void updateHistory() {
        try {
            fileService.persistHistory(new DownloadHistory(completedDownloads.values(), failedDownloads.values()));
        } catch (Exception e) {
            logger.error("Exception on persisting history: {}", e.getMessage());
        }
    }
}
