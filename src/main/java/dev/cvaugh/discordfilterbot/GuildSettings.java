package dev.cvaugh.discordfilterbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuildSettings {
    public long id;
    public List<String> filtered = new ArrayList<>();

    public void save() {
        try {
            Main.writeGuildSettings(id);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
