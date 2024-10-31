package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.exception.YoutubeDownloadException;
import com.chillteq.channel_archive_server.model.Channel;
import com.chillteq.channel_archive_server.model.Video;
import com.chillteq.channel_archive_server.util.OutputStreamUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
            throw new IllegalArgumentException("Call to YoutubeService.getVideosByChannel with null channelUrl. " + channel);
        }
        String[] command = {
                YTDL_PATH,
                "--flat-playlist",
                "-j",
                channel.getChannelUrl()
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
    }

    public void downloadVideosByChannel(Channel channel, OutputStream outputStream, boolean dryRun) {
        if(channel.getVideos() == null || channel.getVideos().isEmpty()) {
            throw new IllegalArgumentException("Call to YoutubeService.downloadVideosByChannel with null channelUrl. " + channel);
        }
        channel.getVideos().forEach(video -> {
            String log = String.format("\tDownloading video \"%s\"", video.getTitle());
            logger.info(log);
            OutputStreamUtility.writeLine(outputStream, log);
            String videoUrl = "https://www.youtube.com/watch?v=" + video.getId();

            if(dryRun) {
                return;
            }

            String outputFileLocation = baseDir + "/" + channel.getChannelDir() + "/" + "%(upload_date)s - %(title)s - %(id)s.%(ext)s";
            String[] command = {
                    YTDL_PATH,
                    "-o",
                    outputFileLocation,
                    videoUrl
            };
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
                logger.info("yt-dl - downloadVideosByChannel - process exited with exit code {}", exitCode);
            } else {
                logger.info("yt-dl - downloadVideosByChannel - yt-dl returned a failed exitCode {} using command {}", exitCode, Arrays.toString(command));
                throw new YoutubeDownloadException("yt-dl returned a failed exitCode " + exitCode + " using command " + Arrays.toString(command));
            }
        });
    }

    public void validateChannel(Channel channel) {
        List<Video> videos = getVideoMetadataByChannel(channel);
        logger.info("Successfully validated channel {}, found {} videos", channel.getChannelName(), videos.size());
    }
}
