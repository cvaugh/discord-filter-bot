package dev.cvaugh.discordfilterbot;

import java.awt.Color;
import java.util.List;

public class Config {
    public static Config instance = new Config();

    public String botToken = "YOUR TOKEN HERE";

    public static String getBotToken() {
        return instance.botToken;
    }
}
