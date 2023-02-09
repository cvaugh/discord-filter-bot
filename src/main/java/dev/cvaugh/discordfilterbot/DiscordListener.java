package dev.cvaugh.discordfilterbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Main.logger.info("Ready event received");
        for(Guild guild : Main.jda.getGuilds()) {
            long id = guild.getIdLong();
            if(!Guilds.hasEntry(id)) {
                Main.logger.warn("Creating missing settings.json for guild {}", id);
                Guilds.put(id);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Main.logger.debug("SlashCommandInteractionEvent: [{}, {}, {}]", event.getName(),
                event.getGuild(), event.getUser());
        if(event.getGuild() == null)
            return;
        GuildSettings settings = Guilds.get(event.getGuild().getIdLong());
        switch(event.getName()) {
        case "help" -> event.reply(Main.helpText).setEphemeral(true).queue();
        case "addword" -> {
            OptionMapping option = event.getOption("word");
            if(option != null) {
                List<String> added = new ArrayList<>();
                List<String> skipped = new ArrayList<>();
                String word = option.getAsString();
                if(word.contains(",")) {
                    Arrays.stream(word.split(",")).filter(entry -> !entry.isBlank())
                            .forEach(entry -> {
                                String s = entry.toLowerCase().trim();
                                if(settings.filtered.contains(s)) {
                                    skipped.add(s);
                                } else {
                                    added.add(s);
                                }
                            });
                } else if(!word.isBlank()) {
                    String s = word.toLowerCase().trim();
                    if(settings.filtered.contains(s)) {
                        skipped.add(s);
                    } else {
                        added.add(s);
                    }
                }
                settings.filtered.addAll(added);
                StringBuilder ab = new StringBuilder();
                for(String s : added) {
                    ab.append(", `");
                    ab.append(s);
                    ab.append("`");
                }
                StringBuilder sb = new StringBuilder();
                for(String s : skipped) {
                    sb.append(", `");
                    sb.append(s);
                    sb.append("`");
                }
                event.reply(String.format("%s%s%s", added.size() == 0 ?
                                "" :
                                "The following strings were added to the filter list: " + ab.substring(2),
                        (added.size() > 0 && skipped.size() > 0) ? "\n" : "", skipped.size() == 0 ?
                                "" :
                                "The following strings were already in the filter list: " +
                                        sb.substring(2))).setEphemeral(true).queue();
                return;
            }
            event.reply("Failed to add string(s)").setEphemeral(true).queue();
        }
        case "removeword" -> {
            OptionMapping option = event.getOption("word");
            if(option != null) {
                List<String> removed = new ArrayList<>();
                List<String> skipped = new ArrayList<>();
                String word = option.getAsString();
                if(word.contains(",")) {
                    Arrays.stream(word.split(",")).filter(entry -> !entry.isBlank())
                            .forEach(entry -> {
                                String s = entry.toLowerCase().trim();
                                if(settings.filtered.contains(s)) {
                                    removed.add(s);
                                } else {
                                    skipped.add(s);
                                }
                            });
                } else if(!word.isBlank()) {
                    String s = word.toLowerCase().trim();
                    if(settings.filtered.contains(s)) {
                        removed.add(s);
                    } else {
                        skipped.add(s);
                    }
                }
                settings.filtered.removeAll(removed);
                StringBuilder rb = new StringBuilder();
                for(String s : removed) {
                    rb.append(", `");
                    rb.append(s);
                    rb.append("`");
                }
                StringBuilder sb = new StringBuilder();
                for(String s : skipped) {
                    sb.append(", `");
                    sb.append(s);
                    sb.append("`");
                }
                event.reply(String.format("%s%s%s", removed.size() == 0 ?
                                "" :
                                "The following strings were removed from the filter list: " +
                                        rb.substring(2),
                        (removed.size() > 0 && skipped.size() > 0) ? "\n" : "",
                        skipped.size() == 0 ?
                                "" :
                                "The following strings were not found in the filter list: " +
                                        sb.substring(2))).setEphemeral(true).queue();
                return;
            }
            event.reply("Failed to remove string(s)").setEphemeral(true).queue();
        }
        case "listwords" -> {
            if(settings.filtered.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for(String s : settings.filtered) {
                    sb.append(", `");
                    sb.append(s);
                    sb.append("`");
                }
                event.reply(sb.substring(2)).setEphemeral(true).queue();
            } else {
                event.reply("Filter list is empty").setEphemeral(true).queue();
            }
        }
        case "clearwords" -> {
            int size = settings.filtered.size();
            settings.filtered.clear();
            event.reply(String.format("Filter list cleared (%d entr%s removed)", size,
                    size == 1 ? "y" : "ies")).setEphemeral(true).queue();
        }
        case "filterstrict" -> {
            OptionMapping state = event.getOption("state");
            if(state != null) {
                settings.strict = state.getAsBoolean();
                event.reply("Filtering is now set to " + (settings.strict ? "strict" : "loose"))
                        .setEphemeral(true).queue();
            } else {
                event.reply("Failed to update strictness setting").setEphemeral(true).queue();
            }
        }
        default -> {}
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Main.logger.info("Joined guild: {} (ID {})", event.getGuild().getName(),
                event.getGuild().getIdLong());
        Guilds.put(event.getGuild().getIdLong());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().isBot() || event.getAuthor().isSystem())
            return;
        GuildSettings settings = Guilds.get(event.getGuild().getIdLong());
        String message = event.getMessage().getContentStripped();
        String word = null;
        outer:
        for(String s : settings.filtered) {
            if(settings.strict && message.toLowerCase().contains(s)) {
                word = s;
                break;
            } else {
                String[] split = message.split("[\\p{Punct}\\s]+");
                for(String str : split) {
                    if(str.equalsIgnoreCase(s)) {
                        word = s;
                        break outer;
                    }
                }
            }
        }
        if(word != null) {
            event.getMessage().delete().queue();
            if(settings.notify) {
                final String guildName = event.getGuild().getName();
                final String w = word;
                event.getMessage().getAuthor().openPrivateChannel()
                        .queue(channel -> channel.sendMessage(String.format(
                                "Your message in **%s** was removed because it contained the following text:\n`%s`",
                                guildName, w)).queue());
            }
        }
    }
}
