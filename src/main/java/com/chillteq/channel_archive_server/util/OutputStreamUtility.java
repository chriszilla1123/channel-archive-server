package com.chillteq.channel_archive_server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamUtility {

    /**
     * Writes a String message to a provided outputStream, if it exists
     *
     * @param outputStream silently returns if null
     * @param message defaults to a newline if the message is null
     */
    public static void write(OutputStream outputStream, String message) {
        if(outputStream == null) {
            return;
        }
        try {
            outputStream.write(message != null ? message.getBytes() : "".getBytes());
            outputStream.write("\n".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a String message to a provided outputStream, if it exists
     *
     * @param outputStream silently returns if null
     * @param message defaults to a newline if the message is null
     */
    public static void writeObjectAsJson(OutputStream outputStream, Object object) {
        if(outputStream == null) {
            return;
        }
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
