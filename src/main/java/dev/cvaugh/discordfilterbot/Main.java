package dev.cvaugh.discordfilterbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class Main {
    private static final File CONFIG_DIR = new File("discordfilterbot");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");
    private static final File GUILDS_DIR = new File(CONFIG_DIR, "guilds");
    public static Logger logger;
    public static JDA jda;
    public static Gson gson;
    public static String helpText = "";

    public static void main(String[] args) {
        logger = LoggerFactory.getLogger("Bot");
        gson = new Gson();
        try {
            readHelpText();
            loadConfig();
            loadGuilds();
        } catch(IOException e) {
            e.printStackTrace();
        }
        logger.debug("Building JDA instance");
        JDABuilder builder = JDABuilder.createDefault(Config.getBotToken());
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        jda = builder.build();
        logger.debug("Registering commands");
        jda.addEventListener(new DiscordListener());
        jda.updateCommands().addCommands(Commands.slash("help", "Shows the bot's documentation."),
                Commands.slash("addword",
                                "Add a string to the filter list, or a list of string separated by commas.")
                        .addOption(OptionType.STRING, "word",
                                "a string, or list of strings separated by commas, to filter",
                                true), Commands.slash("removeword",
                                "Remove a string from the filter list, or a list of strings separated by commas.")
                        .addOption(OptionType.STRING, "word",
                                "a string, or list of strings separated by commas, to remove",
                                true), Commands.slash("listwords", "Show the filter list"),
                Commands.slash("clearwords", "Remove all words from the filter list"),
                Commands.slash("filterstrict",
                                "Toggle strict filtering. This may cause unintended filtering.")
                        .addOption(OptionType.BOOLEAN, "state",
                                "If true, filters strings even if they are part of a larger string",
                                true),
                Commands.slash("filternotify", "Notify users when their message is removed.")
                        .addOption(OptionType.BOOLEAN, "state",
                                "If true, the user will be told which word caused their message to be removed",
                                true)).queue();
        logger.debug("Registering shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Saving guild settings");
            Guilds.getAll().forEach(GuildSettings::save);
            logger.info("Shutting down");
        }));
    }

    private static void loadConfig() throws IOException {
        logger.info("Loading config");
        if(!CONFIG_DIR.exists()) {
            logger.debug("Creating missing config directory at '{}'", CONFIG_DIR.getAbsolutePath());
            if(!CONFIG_DIR.mkdirs()) {
                logger.error("Failed to create config directory at '{}'",
                        CONFIG_DIR.getAbsolutePath());
                System.exit(1);
            }
        }
        if(!CONFIG_FILE.exists()) {
            writeDefaultConfig();
            logger.error("Please enter your bot token in '{}'", CONFIG_FILE.getAbsolutePath());
            System.exit(1);
        }
        String json = Files.readString(CONFIG_FILE.toPath(), StandardCharsets.UTF_8);
        Config.instance = gson.fromJson(json, Config.class);
    }

    private static void writeDefaultConfig() throws IOException {
        logger.info("Writing default config to '{}'", CONFIG_FILE.getAbsolutePath());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(CONFIG_FILE.toPath(), gson.toJson(Config.instance));
    }

    private static void readHelpText() throws IOException {
        logger.debug("Reading help message from /help.md");
        InputStream in = Main.class.getResourceAsStream("/help.md");
        if(in == null)
            return;
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        helpText = String.join("\n", reader.lines().collect(Collectors.joining()));
        in.close();
    }

    private static void loadGuilds() throws IOException {
        logger.info("Loading guilds");
        if(!GUILDS_DIR.exists() && !GUILDS_DIR.mkdir()) {
            logger.error("Failed to guild data directory at '{}'", GUILDS_DIR.getAbsolutePath());
            System.exit(1);
        }
        File[] files = GUILDS_DIR.listFiles();
        if(files == null)
            return;
        for(File file : files) {
            if(file.isDirectory()) {
                logger.debug("Loading guild " + file.getName());
                File settingsFile = new File(file, "settings.json");
                GuildSettings settings = gson.fromJson(
                        Files.readString(settingsFile.toPath(), StandardCharsets.UTF_8),
                        GuildSettings.class);
                Guilds.put(settings.id, settings, false);
            }
        }
    }

    private static File getGuildDir(long guildId) {
        File dir = new File(GUILDS_DIR, String.valueOf(guildId));
        if(!dir.exists() && !dir.mkdir()) {
            logger.error("Failed to create directory for guild {} at '{}'", guildId,
                    dir.getAbsolutePath());
            System.exit(1);
        }
        return dir;
    }

    public static void writeGuildSettings(long guildId) throws IOException {
        logger.debug("Writing settings for guild {}", guildId);
        Files.writeString(new File(getGuildDir(guildId), "settings.json").toPath(),
                gson.toJson(Guilds.get(guildId)));
    }
}
