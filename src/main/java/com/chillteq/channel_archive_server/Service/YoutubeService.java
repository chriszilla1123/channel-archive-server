package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.exception.YoutubeDownloadException;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class YoutubeService {
    Logger logger = LoggerFactory.getLogger(YoutubeService.class);

    private final String YTDL_PATH = "yt-dlp";

    /**
     * Fetches the metadata for every video available on a given channel
     *
     * @param channel the channel to get video metadata for, requires channel.channelUrl to be set
     * @return the list of Video objects with metadata fields set
     * @see Channel
     * @see Video
     */
    public List<Video> getVideoMetadataByChannel(Channel channel) {
        if(channel.getChannelUrl() == null) {
            throw new IllegalArgumentException("Call to YoutubeService.getVideosByChannel with null channelUrl. " + channel);
        }
        String[] command = {
                YTDL_PATH,
                "--flat-playlist",
                "-j",
                channel.getChannelUrl()
        };
        try {
            Process process = new ProcessBuilder(command).start();
            BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> processOutput = new ArrayList<>();
            String line;
            while ((line = processOutputReader.readLine()) != null) {
                processOutput.add(line);
            }
            int exitCode = process.waitFor();
            if(exitCode == 0) {
                logger.info("yt-dl - getVideosByChannelUrl - process exited with exit code {}", exitCode);
            } else {
                logger.info("yt-dl - yt-dl returned a failed exitCode {} using command {}", exitCode, Arrays.toString(command));
                throw new YoutubeDownloadException("yt-dl returned a failed exitCode " + exitCode + " using command " + Arrays.toString(command));
            }
            //Parse JSON response
            List<Video> videos = new ArrayList<>();
            processOutput.forEach(outputLine -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Video video = mapper.readValue(outputLine, Video.class);
                    video.setChannelName(channel.getChannelName());
                    videos.add(video);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            return videos;
        } catch (IOException e) {
            logger.error("IOException starting yt-dl process - getVideosByChannelUrl - {}", channel.getChannelUrl());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("InterruptedException running yt-dl process - getVideosByChannelUrl - {}", channel.getChannelUrl());
            throw new RuntimeException(e);
        }
    }

    public void downloadVideosByChannel(Channel channel) {
        if(channel.getVideos() == null || channel.getVideos().isEmpty()) {
            throw new IllegalArgumentException("Call to YoutubeService.downloadVideosByChannel with null channelUrl. " + channel);
        }
    }

    public void validateChannel(Channel channel) {
        List<Video> videos = getVideoMetadataByChannel(channel);
        logger.info("Successfully validated channel {}, found {} videos", channel.getChannelName(), videos.size());
    }
}
