package com.chillteq.channel_archive_server.Service;

import com.chillteq.channel_archive_server.model.Channel;

public class YoutubeService {
    private String YTDL_PATH = "C:\\Users\\Chris\\Downloads\\yt-dlp.exe";

    public void testChannel(Channel channel) {
        String[] command = {
                YTDL_PATH,
                "--flat-playlist",
                "-j",
                channel.getChannelUrl()
        };
//        Process process = Runetime
    }
}
