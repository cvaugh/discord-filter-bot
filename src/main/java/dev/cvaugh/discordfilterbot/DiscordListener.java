package dev.cvaugh.discordfilterbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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
        if(event.getGuild() == null) return;
        GuildSettings settings = Guilds.get(event.getGuild().getIdLong());
        switch(event.getName()) {
        case "help" -> event.reply(Main.helpText).setEphemeral(true).queue();
        case "addword" -> {

        }
        case "removeword" -> {

        }
        case "listwords" -> {

        }
        case "clearwords" -> {

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
        // TODO filter
    }
}
