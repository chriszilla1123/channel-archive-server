package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.constant.Constants;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.DownloadHistory;
import com.chillteq.channel_archive_server.model.Video;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private ObjectMapper mapper;

    public InputStream getFileInputStream(String dir) throws FileNotFoundException {
        return new FileInputStream(dir);
    }

    public List<Channel> persistChannels(List<Channel> channels) throws IOException {
        try {
            mapper.writeValue(new File(Constants.userDefinedConfigFileLocation), channels);
        } catch (IOException e) {
            logger.error("Error writing config file: {}", e.getMessage());
            throw e;
        }
        return channels;
    }

    public DownloadHistory persistHistory(DownloadHistory downloadHistory) throws IOException {
        try {
            mapper.writeValue(new File(Constants.userDefinedHistoryFileLocation), downloadHistory);
        } catch (Exception e) {
            logger.error("Error writing history file: {}", e.getMessage());
            throw e;
        }
        return downloadHistory;
    }

    public boolean folderExists(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }

    public boolean folderExists(String directory) {
        return folderExists(Paths.get(directory));
    }

    /**
     * Filters a channel's video list to exclude downloaded videos
     *
     * @param channel the channel to check. channel.channelDir is required
     * @return the filtered list
     * @see Channel
     * @see Video
     */
    public List<Video> filterDownloadedVideosFromChannel(Channel channel) {
        Path channelPath = Paths.get(Constants.baseVideoDirectory + "/" + channel.getChannelDir());
        if(!folderExists(Constants.baseVideoDirectory)) {
            throw new IllegalStateException("Base directory does not exists or is not a directory");
        }
        if(!folderExists(channelPath)) {
            //Base directory exists but channel directory does not. All videos need to be downloaded.
            return channel.getVideos();
        }

        HashSet<String> incomingVideosToBeDownloaded = new HashSet<>();
        channel.getVideos().forEach(video -> {
            incomingVideosToBeDownloaded.add(video.getId());
        });
        HashSet<String> filesFoundOnSystem = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(channelPath)) {
            for (Path foundPath : directoryStream) {
                filesFoundOnSystem.add(foundPath.getFileName().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error open directory " + channelPath, e);
        }

        List<Video> filteredVideos = channel.getVideos().stream().filter(video -> {
            AtomicBoolean videoAlreadyDownloaded = new AtomicBoolean(false);
            filesFoundOnSystem.forEach(file -> {
                if(file.contains(video.getId())) {
                    videoAlreadyDownloaded.set(true);
                }
            });
            return !videoAlreadyDownloaded.get();
        }).toList();
        return filteredVideos;
    }
}
