package com.chillteq.channel_archive_server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamUtility {
    public static void writeObjectAsJson(OutputStream outputStream, Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            outputStream.write(mapper.writeValueAsBytes(object));
            outputStream.write("\n".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
