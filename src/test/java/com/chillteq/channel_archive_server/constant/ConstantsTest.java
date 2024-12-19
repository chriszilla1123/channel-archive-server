package com.chillteq.channel_archive_server.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {

    @Test
    public void testConstants() {
        @SuppressWarnings("InstantiationOfUtilityClass") Constants constants = new Constants();
        assertNotNull(Constants.userDefinedConfigFileLocation);
    }
}