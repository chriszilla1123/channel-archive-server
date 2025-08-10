package com.chillteq.channel_archive_server.service;

import com.chillteq.channel_archive_server.constant.Constants;
import com.chillteq.channel_archive_server.exception.YoutubeDownloadException;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class YoutubeService {
    Logger logger = LoggerFactory.getLogger(YoutubeService.class);

    @Value("${COOKIES:#{null}}")
    public Strings cookies;

    public String getYtdlVersion() {
        String[] command = {
                Constants.ytdlPath,
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
        if(channel.getChannelUrls() == null || channel.getChannelUrls().isEmpty()) {
            throw new IllegalArgumentException("Call to YoutubeService.getVideoMetadataByChannel with null channelUrl. " + channel);
        }
        List<Video> videos = new ArrayList<>();
        for(String url: channel.getChannelUrls()) {
            List<String> processOutput = executeYtdlVideoMetadataCommand(url);
            //Parse JSON response
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
        }
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
                Constants.ytdlPath,
                "--flat-playlist",
                "-j",
                url
        };

        if(cookiesFileExists()) {
            logger.info("Using user-provided cookies file");
            command = new String[]{
                    Constants.ytdlPath,
                    "--flat-playlist",
                    "-j",
                    "--cookies",
                    Constants.cookiesFileLocation,
                    url
            };
        } else {
            logger.info("No user-provided cookies file found");
        }
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
        String outputFileLocation = Constants.baseVideoDirectory + "/";
        if(StringUtils.hasText(video.getDirectory())) {
            outputFileLocation += video.getDirectory() + "/";
        }
        outputFileLocation += "%(upload_date)s - %(title)s - %(id)s.%(ext)s";
        String[] command = {
                Constants.ytdlPath,
                "-f",
                "bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4] / bv*+ba/b", // Download the best mp4 video available, or the best video if no mp4 available
                "-o",
                outputFileLocation,
                cookiesFileExists() ? "--cookies" : null,
                cookiesFileExists() ? Constants.cookiesFileLocation : null,
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
            logger.error("yt-dl - downloadVideo - yt-dl returned a failed exitCode {} using command {}", exitCode, Arrays.toString(command));
            logger.error(String.valueOf(processOutput));
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

    private boolean cookiesFileExists() {
        return Files.exists(Paths.get(Constants.cookiesFileLocation));
    }
}
