package com.chillteq.channel_archive_server.service;

import com.chillteq.channel_archive_server.exception.YoutubeDownloadException;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class YoutubeService {
    Logger logger = LoggerFactory.getLogger(YoutubeService.class);

    private final String baseDir = "/baseDirectory";
    private final String YTDL_PATH = "/root/.local/bin/yt-dlp";

    public String getYtdlVersion() {
        String[] command = {
                YTDL_PATH,
                "--version"
        };
        Process process = null;
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException e) {
            logger.error("RuntimeException starting yt-dl", e);
            throw new RuntimeException(e);
        }
        BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> processOutput = new ArrayList<>();
        String line;
        while (true) {
            try {
                if ((line = processOutputReader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            processOutput.add(line);
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(exitCode == 0) {
            logger.info("yt-dl - getYtdlVersion - process exited with exit code {}", exitCode);
        } else {
            logger.info("yt-dl - getYtdlVersion - yt-dl returned a failed exitCode {} using command {}", exitCode, Arrays.toString(command));
            throw new YoutubeDownloadException("yt-dl returned a failed exitCode " + exitCode + " using command " + Arrays.toString(command));
        }
        return processOutput.getLast();
    }

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
            throw new IllegalArgumentException("Call to YoutubeService.getVideoMetadataByChannel with null channelUrl. " + channel);
        }
        List<String> processOutput = executeYtdlVideoMetadataCommand(channel.getChannelUrl());
        //Parse JSON response
        List<Video> videos = new ArrayList<>();
        processOutput.forEach(outputLine -> {
            logger.debug(outputLine);
            ObjectMapper mapper = new ObjectMapper();
            try {
                Video video = mapper.readValue(outputLine, Video.class);
                video.setChannelName(channel.getChannelName());
                video.setDownload_date(new Date());
                video.setDirectory(channel.getChannelDir());
                videos.add(video);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return videos;
    }

    /**
     * Fetches the metadata for a given video URL
     *
     * @param videoUrl the url to the video to get metadata for
     * @return the Video object with metadata fields set
     * @see Video
     */
    public Video getVideoMetadataByVideoUrl(String videoUrl) {
        if(null == videoUrl) {
            throw new IllegalArgumentException("Call to YoutubeService.getVideoMetadataByVideoUrl with null URL.");
        }
        String processOutput = executeYtdlVideoMetadataCommand(videoUrl).getFirst();
        //Parse JSON response
        logger.debug(processOutput);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Video video = mapper.readValue(processOutput, Video.class);
            video.setUrl(videoUrl);
            video.setDownload_date(new Date());
            return video;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fetches the metadata for the video(s) for a given video URL, playlist URL, or channel URL
     *
     * @param url the video, playlist, or channel URL
     * @return the list of JSON strings containing metadata for 1 or more videos. Should be mapped to a Video object
     * @see Video
     */
    private List<String> executeYtdlVideoMetadataCommand(String url) {
        String[] command = {
                YTDL_PATH,
                "--flat-playlist",
                "-j",
                url
        };
        Process process = null;
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException e) {
            logger.error("RuntimeException starting yt-dl", e);
            throw new RuntimeException(e);
        }
        BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> processOutput = new ArrayList<>();
        String line;
        while (true) {
            try {
                if ((line = processOutputReader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            processOutput.add(line);
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(exitCode == 0) {
            logger.info("yt-dl - getVideoMetadataByChannel - process exited with exit code {}", exitCode);
        } else {
            logger.info("yt-dl - getVideoMetadataByChannel - yt-dl returned a failed exitCode {} using command {}", exitCode, Arrays.toString(command));
            throw new YoutubeDownloadException("yt-dl returned a failed exitCode " + exitCode + " using command " + Arrays.toString(command));
        }
        return processOutput;
    }

    /**
     * Downloads a single Video. Relies on video.url being set. video.directory can optionally be set.
     * @param video
     */
    public void downloadVideo(Video video) {
        String outputFileLocation = baseDir + "/";
        if(StringUtils.hasText(video.getDirectory())) {
            outputFileLocation += video.getDirectory() + "/";
        }
        outputFileLocation += "%(upload_date)s - %(title)s - %(id)s.%(ext)s";
        String[] command = {
                YTDL_PATH,
                "-f",
                "bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4] / bv*+ba/b", // Download the best mp4 video available, or the best video if no mp4 available
                "-o",
                outputFileLocation,
                video.getUrl(),
        };
        logger.info("Downloading video URL '{}' to location '{}'", video.getUrl(), outputFileLocation);
        Process process = null;
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> processOutput = new ArrayList<>();
        String line;
        while (true) {
            try {
                line = processOutputReader.readLine();
                logger.debug(line);
                if (line == null){
                    break;
                } else {
                    if(isDownloadStatus(line)) {
                        video.setDownloadStatus(toPrettyDownloadStatus(line));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            processOutput.add(line);
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(exitCode == 0) {
            logger.info("yt-dl - downloadVideo - process exited with exit code {}", exitCode);
        } else {
            logger.info("yt-dl - downloadVideo - yt-dl returned a failed exitCode {} using command {}", exitCode, Arrays.toString(command));
            throw new YoutubeDownloadException("yt-dl returned a failed exitCode " + exitCode + " using command " + Arrays.toString(command));
        }
    }

    public void validateChannel(Channel channel) {
        List<Video> videos = getVideoMetadataByChannel(channel);
        logger.info("Successfully validated channel {}, found {} videos", channel.getChannelName(), videos.size());
    }

    public boolean isDownloadStatus(String string) {
        return string.contains("[download") && string.contains("%") && string.contains("ETA");
    }

    public String toPrettyDownloadStatus(String string) {
        return string.replace("[download]", "").trim();
    }
}
