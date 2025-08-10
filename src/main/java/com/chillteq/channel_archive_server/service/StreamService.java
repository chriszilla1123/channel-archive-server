package com.chillteq.channel_archive_server.service;

import com.chillteq.channel_archive_server.constant.Constants;
import com.chillteq.channel_archive_server.model.Channel;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class StreamService {
    private static final Logger logger = LoggerFactory.getLogger(StreamService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private ConfigurationService configurationService;

    private static final int BUFFER_SIZE = 1024;

    public StreamingResponseBody streamVideo(String path, String rangeHeader, HttpServletResponse response) throws IOException {
        logger.info("Request to stream video at path '{}' with rangeHeader '{}'", path, rangeHeader);
        Path videoPath = Paths.get(Constants.baseVideoDirectory + File.separator + path);
        File videoFile = videoPath.toFile();

        if (!videoFile.exists()) {
            throw new FileNotFoundException();
        }

        long fileSize = videoFile.length();
        long rangeStart = 0;
        long rangeEnd = fileSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            }
        }

        long contentLength = rangeEnd - rangeStart + 1;

        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Type", "video/mp4"); // adjust MIME type
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);

        RandomAccessFile file = new RandomAccessFile(videoFile, "r");
        file.seek(rangeStart);

        return outputStream -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            long bytesRemaining = contentLength;
            int bytesRead;

            while (bytesRemaining > 0 && (bytesRead = file.read(buffer, 0, (int)Math.min(BUFFER_SIZE, bytesRemaining))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }

            outputStream.flush();
            file.close();
        };
    }
}
